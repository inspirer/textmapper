package compiler

import (
	"fmt"
	"sort"
	"strconv"
	"strings"

	"github.com/inspirer/textmapper/grammar"
	"github.com/inspirer/textmapper/lalr"
	"github.com/inspirer/textmapper/parsers/tm/ast"
	"github.com/inspirer/textmapper/parsers/tm/selector"
	"github.com/inspirer/textmapper/status"
	"github.com/inspirer/textmapper/syntax"
	"github.com/inspirer/textmapper/util/container"
	"github.com/inspirer/textmapper/util/ident"
)

type syntaxLoader struct {
	resolver *resolver
	*status.Status

	out      *syntax.Model
	sets     []*grammar.NamedSet
	prec     []lalr.Precedence
	mapping  []syntax.RangeToken
	expectSR int
	expectRR int

	namedSets map[string]int // -> index in source.Sets
	asserts   []assert

	params    map[string]int // -> index in source.Params
	nonterms  map[string]int // -> index in source.Nonterms
	cats      map[string]int // -> index in source.Cats
	paramPerm []int          // for parameter permutations
	rhsPos    int            // Counter for positional index of a reference in the current rule.
	rhsNames  map[string]int
}

func newSyntaxLoader(resolver *resolver, s *status.Status) *syntaxLoader {
	return &syntaxLoader{
		resolver: resolver,
		Status:   s,

		namedSets: make(map[string]int),
		params:    make(map[string]int),
		nonterms:  make(map[string]int),
		cats:      make(map[string]int),
	}
}

func (c *syntaxLoader) collectParams(p ast.ParserSection) {
	for _, part := range p.GrammarPart() {
		if param, ok := part.(*ast.TemplateParam); ok {
			name := param.Name().Text()
			if _, exists := c.params[name]; exists {
				c.Errorf(param.Name(), "redeclaration of '%v'", name)
				continue
			}
			if _, exists := c.resolver.syms[name]; exists {
				c.Errorf(param.Name(), "template parameters cannot be named after terminals '%v'", name)
				continue
			}
			p := syntax.Param{Name: name, Origin: param.Name()}
			if val, ok := param.ParamValue(); ok {
				switch val := val.(type) {
				case *ast.BooleanLiteral:
					p.DefaultValue = val.Text()
				default:
					c.Errorf(val.TmNode(), "unsupported default value")
				}
			}
			if mod, ok := param.Modifier(); ok {
				if mod.Text() == "lookahead" {
					p.Lookahead = true
				} else {
					c.Errorf(mod, "unsupported syntax")
				}
			}
			c.params[name] = len(c.out.Params)
			c.out.Params = append(c.out.Params, p)
		}
	}
}

type nontermImpl struct {
	nonterm int // in source.Nonterms
	def     ast.Nonterm
}

func (c *syntaxLoader) collectNonterms(p ast.ParserSection) []nontermImpl {
	var ret []nontermImpl
	for _, part := range p.GrammarPart() {
		if nonterm, ok := part.(*ast.Nonterm); ok {
			name := nonterm.Name().Text()
			if _, ok := nonterm.Extend(); ok {
				// Nonterminal extensions have to reference an existing one.
				if index, exists := c.nonterms[name]; exists {
					ret = append(ret, nontermImpl{index, *nonterm})
					continue
				}
				c.Errorf(nonterm.Name(), "unresolved nonterminal '%v' to extend", name)

				// Note: we keep going here to avoid cascading errors.
			}

			if _, exists := c.resolver.syms[name]; exists {
				c.Errorf(nonterm.Name(), "redeclaration of '%v'", name)
				continue
			}
			if _, exists := c.nonterms[name]; exists {
				c.Errorf(nonterm.Name(), "redeclaration of '%v'", name)
				continue
			}
			if _, exists := c.params[name]; exists {
				c.Errorf(nonterm.Name(), "redeclaration of a template parameter '%v'", name)
				continue
			}
			id := ident.Produce(name, ident.CamelCase)
			if prev, exists := c.resolver.ids[id]; exists {
				c.Errorf(nonterm.Name(), "%v and %v get the same ID in generated code", name, prev)
			}
			if ann, ok := nonterm.Annotations(); ok {
				c.Errorf(ann.TmNode(), "unsupported syntax")
			}

			nt := &syntax.Nonterm{
				Name:   name,
				Origin: nonterm.Name(),
			}
			if rt, ok := nonterm.RawType(); ok {
				nt.Type = strings.TrimSuffix(strings.TrimPrefix(rt.Text(), "{"), "}")
			}

			var seen map[string]bool
			params, _ := nonterm.Params()
			for _, param := range params.List() {
				if seen == nil {
					seen = make(map[string]bool)
				}
				switch param := param.(type) {
				case *ast.ParamRef:
					name := param.Identifier().Text()
					if seen[name] {
						c.Errorf(param, "duplicate parameter reference '%v'", name)
						continue
					}
					seen[name] = true
					if i, ok := c.params[name]; ok {
						if !c.out.Params[i].Lookahead {
							nt.Params = append(nt.Params, i)
						} else {
							c.Errorf(param, "lookahead parameters cannot be declared '%v'", name)
						}
						continue
					}
					c.Errorf(param, "unresolved parameter reference '%v'", name)
				case *ast.InlineParameter:
					name := param.Name().Text()
					if _, exists := c.params[name]; exists {
						c.Errorf(param.Name().TmNode(), "redeclaration of '%v'", name)
						continue
					}
					p := syntax.Param{Name: name, Origin: param.Name().TmNode()}
					if val, ok := param.ParamValue(); ok {
						switch val := val.(type) {
						case *ast.BooleanLiteral:
							p.DefaultValue = val.Text()
						default:
							c.Errorf(val.TmNode(), "unsupported default value")
						}
					}
					nt.Params = append(nt.Params, len(c.out.Params))
					c.out.Params = append(c.out.Params, p)
				}
			}
			c.nonterms[name] = len(c.out.Nonterms)
			ret = append(ret, nontermImpl{len(c.out.Nonterms), *nonterm})
			c.out.Nonterms = append(c.out.Nonterms, nt)
		}
	}
	return ret
}

func (c *syntaxLoader) collectInputs(p ast.ParserSection, header status.SourceNode) {
	for _, part := range p.GrammarPart() {
		if input, ok := part.(*ast.DirectiveInput); ok {
			for _, ref := range input.InputRefs() {
				name := ref.Reference().Name()
				nonterm, found := c.nonterms[name.Text()]
				if !found {
					c.Errorf(name, "unresolved nonterminal '%v'", name.Text())
					continue
				}
				if len(c.out.Nonterms[nonterm].Params) > 0 {
					c.Errorf(name, "input nonterminals cannot be parametrized")
				}
				_, noeoi := ref.NoEoi()
				c.out.Inputs = append(c.out.Inputs, syntax.Input{Nonterm: nonterm, NoEoi: noeoi})
			}
		}
	}

	if len(c.out.Inputs) > 0 {
		return
	}

	input, found := c.nonterms["input"]
	if found && len(c.out.Nonterms[input].Params) > 0 {
		c.Errorf(c.out.Nonterms[input].Origin, "the 'input' nonterminal cannot be parametrized")
		found = false
	}
	if !found {
		c.Errorf(header, "the grammar does not specify an input nonterminal, use '%%input' to declare one")
		return
	}
	c.out.Inputs = append(c.out.Inputs, syntax.Input{Nonterm: input})
}

func (c *syntaxLoader) collectDirectives(p ast.ParserSection) {
	precTerms := container.NewBitSet(c.resolver.NumTokens)
	injected := container.NewBitSet(c.resolver.NumTokens)
	var seenSR, seenRR bool

	for _, part := range p.GrammarPart() {
		switch part := part.(type) {
		case *ast.DirectiveAssert:
			set := c.convertSet(part.RhsSet().Expr(), nil /*nonterm*/)
			index := len(c.out.Sets)
			c.out.Sets = append(c.out.Sets, set)
			_, empty := part.Empty()
			c.asserts = append(c.asserts, assert{index: index, empty: empty})
		case *ast.DirectiveInterface:
			for _, id := range part.Ids() {
				text := id.Text()
				if _, exists := c.cats[text]; exists {
					c.Errorf(id, "duplicate interface declaration of '%v'", text)
					continue
				}
				index := len(c.out.Cats)
				c.out.Cats = append(c.out.Cats, text)
				c.cats[text] = index
			}
		case *ast.DirectivePrio:
			var assoc lalr.Associativity
			switch part.Assoc().Text() {
			case "left":
				assoc = lalr.Left
			case "right":
				assoc = lalr.Right
			case "nonassoc":
				assoc = lalr.NonAssoc
			default:
				c.Errorf(part, "unsupported associativity")
				continue
			}
			prec := lalr.Precedence{Associativity: assoc}
			for _, ref := range part.Symbols() {
				name := ref.Name()
				sym, ok := c.resolver.syms[name.Text()]
				if !ok || sym >= c.resolver.NumTokens {
					c.Errorf(name, "unresolved reference '%v'", name.Text())
					continue
				}
				if precTerms.Get(sym) {
					c.Errorf(name, "second precedence rule for '%v'", name.Text())
					continue
				}
				precTerms.Set(sym)
				prec.Terminals = append(prec.Terminals, lalr.Sym(sym))
			}
			if len(prec.Terminals) > 0 {
				c.prec = append(c.prec, prec)
			}
		case *ast.DirectiveExpect:
			if seenSR {
				c.Errorf(part, "duplicate %%expect directive")
				continue
			}
			seenSR = true
			c.expectSR, _ = strconv.Atoi(part.Child(selector.IntegerLiteral).Text())
		case *ast.DirectiveExpectRR:
			if seenRR {
				c.Errorf(part, "duplicate %%expect-rr directive")
				continue
			}
			seenRR = true
			c.expectRR, _ = strconv.Atoi(part.Child(selector.IntegerLiteral).Text())
		case *ast.DirectiveInject:
			name := part.Symref().Name()
			sym, ok := c.resolver.syms[name.Text()]
			if !ok || sym >= c.resolver.NumTokens {
				c.Errorf(name, "unresolved reference '%v'", name.Text())
				break
			}
			if injected.Get(sym) {
				c.Errorf(name, "second %%inject directive for '%v'", name.Text())
				break
			}
			injected.Set(sym)
			if as, ok := part.ReportClause().ReportAs(); ok {
				c.Errorf(as, "reporting terminals 'as' some category is not supported")
			}

			var flags []string
			for _, id := range part.ReportClause().Flags() {
				flags = append(flags, id.Text())
			}
			c.mapping = append(c.mapping, syntax.RangeToken{
				Token:  sym,
				Name:   part.ReportClause().Action().Text(),
				Flags:  flags,
				Origin: part,
			})

		case *ast.DirectiveSet:
			name := part.Name()
			if name.Text() == "afterErr" {
				c.Errorf(name, "'afterErr' is reserved for built-in error recovery")
				continue
			}
			if _, ok := c.namedSets[name.Text()]; ok {
				c.Errorf(name, "redeclaration of token set '%v'", name.Text())
				continue
			}

			set := c.convertSet(part.RhsSet().Expr(), nil /*nonterm*/)
			c.namedSets[name.Text()] = len(c.out.Sets)
			c.sets = append(c.sets, &grammar.NamedSet{
				Name: name.Text(),
				Expr: part.RhsSet().Text(), // Note: this gets replaced later with instantiated names
			})
			c.out.Sets = append(c.out.Sets, set)
		}
	}
	for _, mapping := range c.mapping {
		if _, ok := c.cats[mapping.Name]; ok {
			c.Errorf(mapping.Origin, "selector clauses (%v) cannot be used with injected terminals", mapping.Name)
		}
	}
}

// TODO remove the second parameter and disallow templating sets
func (c *syntaxLoader) convertSet(expr ast.SetExpression, nonterm *syntax.Nonterm) *syntax.TokenSet {
	switch expr := expr.(type) {
	case *ast.SetAnd:
		return &syntax.TokenSet{
			Kind:   syntax.Intersection,
			Sub:    []*syntax.TokenSet{c.convertSet(expr.Left(), nonterm), c.convertSet(expr.Right(), nonterm)},
			Origin: expr,
		}
	case *ast.SetComplement:
		return &syntax.TokenSet{
			Kind:   syntax.Complement,
			Sub:    []*syntax.TokenSet{c.convertSet(expr.Inner(), nonterm)},
			Origin: expr,
		}
	case *ast.SetCompound:
		return c.convertSet(expr.Inner(), nonterm)
	case *ast.SetOr:
		return &syntax.TokenSet{
			Kind:   syntax.Union,
			Sub:    []*syntax.TokenSet{c.convertSet(expr.Left(), nonterm), c.convertSet(expr.Right(), nonterm)},
			Origin: expr,
		}
	case *ast.SetSymbol:
		ret := &syntax.TokenSet{Kind: syntax.Any}
		if op, ok := expr.Operator(); ok {
			switch op.Text() {
			case "first":
				ret.Kind = syntax.First
			case "last":
				ret.Kind = syntax.Last
			case "precede":
				ret.Kind = syntax.Precede
			case "follow":
				ret.Kind = syntax.Follow
			default:
				c.Errorf(op, "operator must be one of: first, last, precede or follow")
			}
		}
		ret.Symbol, ret.Args = c.resolveRef(expr.Symbol(), nonterm)
		return ret
	default:
		c.Errorf(expr.TmNode(), "syntax error")
		return &syntax.TokenSet{} // == eoi
	}
}

func (c *syntaxLoader) pred(ref ast.ParamRef, nonterm *syntax.Nonterm, op syntax.PredicateOp, val string, origin ast.PredicateExpression) *syntax.Predicate {
	param, ok := c.resolveParam(ref, nonterm)
	if !ok {
		return nil
	}
	return &syntax.Predicate{
		Op:     op,
		Param:  param,
		Value:  val,
		Origin: origin.TmNode(),
	}
}

func (c *syntaxLoader) predNot(pred *syntax.Predicate, origin ast.PredicateExpression) *syntax.Predicate {
	if pred == nil {
		return nil
	}
	return &syntax.Predicate{
		Op:     syntax.Not,
		Sub:    []*syntax.Predicate{pred},
		Origin: origin.TmNode(),
	}
}

func (c *syntaxLoader) predLiteral(ref ast.ParamRef, nonterm *syntax.Nonterm, literal ast.Literal, origin ast.PredicateExpression) *syntax.Predicate {
	lit, ok := literal.(*ast.StringLiteral)
	if !ok {
		c.Errorf(literal.TmNode(), "string is expected")
		return nil
	}
	val, err := strconv.Unquote(lit.Text())
	if err != nil {
		c.Errorf(lit, "cannot parse string literal: %v", err)
		return nil
	}
	return c.pred(ref, nonterm, syntax.Equals, val, origin)
}

func (c *syntaxLoader) predList(op syntax.PredicateOp, list []ast.PredicateExpression, nonterm *syntax.Nonterm, origin ast.PredicateExpression) *syntax.Predicate {
	var out []*syntax.Predicate
	for _, expr := range list {
		if p := c.convertPredicate(expr, nonterm); p != nil {
			out = append(out, p)
		}
	}
	if len(out) == 0 {
		return nil
	}
	return &syntax.Predicate{
		Op:     op,
		Sub:    out,
		Origin: origin.TmNode(),
	}
}

func (c *syntaxLoader) convertPredicate(expr ast.PredicateExpression, nonterm *syntax.Nonterm) *syntax.Predicate {
	switch expr := expr.(type) {
	case *ast.PredicateOr:
		return c.predList(syntax.Or, []ast.PredicateExpression{expr.Left(), expr.Right()}, nonterm, expr)
	case *ast.PredicateAnd:
		return c.predList(syntax.And, []ast.PredicateExpression{expr.Left(), expr.Right()}, nonterm, expr)
	case *ast.ParamRef:
		return c.pred(*expr, nonterm, syntax.Equals, "true", expr)
	case *ast.PredicateNot:
		return c.predNot(c.pred(expr.ParamRef(), nonterm, syntax.Equals, "true", expr), expr)
	case *ast.PredicateEq:
		return c.predLiteral(expr.ParamRef(), nonterm, expr.Literal(), expr)
	case *ast.PredicateNotEq:
		return c.predNot(c.predLiteral(expr.ParamRef(), nonterm, expr.Literal(), expr), expr)
	default:
		c.Errorf(expr.TmNode(), "syntax error")
		return nil
	}
}

func (c *syntaxLoader) resolveParam(ref ast.ParamRef, nonterm *syntax.Nonterm) (int, bool) {
	name := ref.Identifier().Text()
	for _, p := range nonterm.Params {
		if name == c.out.Params[p].Name {
			return p, true
		}
	}

	if p, ok := c.params[name]; ok && c.out.Params[p].Lookahead {
		// Lookahead parameters don't have to be declared.
		return p, true
	}

	c.Errorf(ref.Identifier(), "unresolved parameter reference '%v' (in %v)", name, nonterm.Name)
	return 0, false
}

func (c *syntaxLoader) instantiateOpt(name string, origin ast.Symref) (int, bool) {
	nt := &syntax.Nonterm{
		Name:   name,
		Origin: origin,
	}

	var ref *syntax.Expr
	target := name[:len(name)-3]
	if index, ok := c.resolver.syms[target]; ok {
		nt.Type = c.resolver.Syms[index].Type
		ref = &syntax.Expr{Kind: syntax.Reference, Symbol: index, Origin: origin, Model: c.out}
	} else if nonterm, ok := c.nonterms[target]; ok {
		nt.Type = c.out.Nonterms[nonterm].Type
		nt.Params = c.out.Nonterms[nonterm].Params
		ref = &syntax.Expr{Kind: syntax.Reference, Symbol: c.resolver.NumTokens + nonterm, Origin: origin, Model: c.out}
		for _, param := range nt.Params {
			ref.Args = append(ref.Args, syntax.Arg{Param: param, TakeFrom: param})
		}
	} else {
		// Unresolved.
		return 0, false
	}
	nt.Value = &syntax.Expr{Kind: syntax.Optional, Sub: []*syntax.Expr{ref}, Origin: origin}

	c.nonterms[name] = len(c.out.Nonterms)
	index := c.resolver.NumTokens + len(c.out.Nonterms)
	c.out.Nonterms = append(c.out.Nonterms, nt)
	return index, true
}

func (c *syntaxLoader) resolveRef(ref ast.Symref, nonterm *syntax.Nonterm) (int, []syntax.Arg) {
	name := ref.Name()
	text := name.Text()
	index, ok := c.resolver.syms[text]
	if !ok {
		index, ok = c.nonterms[text]
		if ok {
			index += c.resolver.NumTokens
		}
	}
	if !ok && len(text) > 3 && strings.HasSuffix(text, "opt") {
		index, ok = c.instantiateOpt(text, ref)
	}
	if !ok {
		c.Errorf(name, "unresolved reference '%v'", text)
		return 0, nil // == eoi
	}

	if index < c.resolver.NumTokens {
		if args, ok := ref.Args(); ok {
			c.Errorf(args, "terminals cannot be parametrized")
		}
		return index, nil
	}

	target := c.out.Nonterms[index-c.resolver.NumTokens]
	required := container.NewBitSet(len(c.out.Params))
	populated := container.NewBitSet(len(c.out.Params))
	for _, p := range target.Params {
		required.Set(p)
	}
	var args []syntax.Arg
	if arguments, ok := ref.Args(); ok {
		for _, arg := range arguments.ArgList() {
			var ref ast.ParamRef
			out := syntax.Arg{Origin: arg.TmNode()}
			switch arg := arg.(type) {
			case *ast.ArgumentFalse:
				ref = arg.Name()
				out.Value = "false"
			case *ast.ArgumentTrue:
				ref = arg.Name()
				out.Value = "true"
			case *ast.ArgumentVal:
				ref = arg.Name()
				if val, ok := arg.Val(); ok {
					switch val := val.(type) {
					case *ast.BooleanLiteral:
						out.Value = val.Text()
					case *ast.ParamRef:
						if nonterm == nil {
							c.Errorf(val.Identifier(), "unresolved parameter reference '%v'", val.Identifier().Text())
							continue
						}
						out.TakeFrom, ok = c.resolveParam(*val, nonterm)
						if !ok {
							continue
						}
					default:
						c.Errorf(val.TmNode(), "unsupported value")
						continue
					}
					break
				}
				if nonterm == nil {
					c.Errorf(ref, "missing value")
					continue
				}
				// Note: matching by name enables value propagation between inline parameters.
				out.TakeFrom, ok = c.resolveParam(ref, nonterm)
				if !ok {
					continue
				}
			default:
				c.Errorf(arg.TmNode(), "syntax error")
				continue
			}
			param, ok := c.resolveParam(ref, target)
			if !ok {
				continue
			}
			if populated.Get(param) {
				c.Errorf(ref, "second argument for '%v'", c.out.Params[param].Name)
				continue
			}
			populated.Set(param)
			required.Clear(param)
			out.Param = param
			out.Origin = ref
			args = append(args, out)
		}
	}

	var uninitialized []string
	for _, p := range required.Slice(nil) {
		param := c.out.Params[p]
		if nonterm != nil {
			var found bool
			for _, from := range nonterm.Params {
				// Note: matching by name enables value propagation between inline parameters.
				if param.Name == c.out.Params[from].Name {
					args = append(args, syntax.Arg{
						Param:    p,
						TakeFrom: from,
					})
					found = true
					break
				}
			}
			if found {
				continue
			}
		}
		if param.DefaultValue != "" {
			args = append(args, syntax.Arg{
				Param: p,
				Value: param.DefaultValue,
			})
			continue
		}
		uninitialized = append(uninitialized, param.Name)
	}
	if len(uninitialized) > 0 {
		c.Errorf(ref.Name(), "uninitialized parameters: %v", strings.Join(uninitialized, ", "))
	}
	c.sortArgs(target.Params, args)
	return index, args
}

func (c *syntaxLoader) sortArgs(params []int, args []syntax.Arg) {
	if len(args) < 2 {
		return
	}
	pos := c.paramPerm
	for i := range pos {
		pos[i] = -1
	}
	for i, param := range params {
		pos[param] = i
	}
	e := len(params)
	for i, index := range pos {
		if index == -1 {
			pos[i] = e
			e++
		}
	}
	sort.Slice(args, func(i, j int) bool { return pos[args[i].Param] < pos[args[j].Param] })
}

func (c *syntaxLoader) convertReportClause(n ast.ReportClause) report {
	action := n.Action().Text()
	if len(action) == 0 {
		return report{}
	}
	var flags []string
	for _, id := range n.Flags() {
		flags = append(flags, id.Text())
	}
	if len(flags) > 0 && c.isSelector(action) {
		c.Errorf(n.Action(), "selector clauses cannot be used together with flags")
		flags = nil
	}
	ret := report{
		node: &syntax.Expr{Kind: syntax.Arrow, Name: action, ArrowFlags: flags, Origin: n},
	}
	if c.isSelector(action) {
		ret.selector = ret.node
		ret.node = nil
	}
	if as, ok := n.ReportAs(); ok {
		if ret.selector != nil {
			c.Errorf(as, "reporting a selector 'as' some other node is not supported")
			return ret
		}
		if !c.isSelector(as.Identifier().Text()) {
			c.Errorf(as, "'as' expects a selector")
			return ret
		}
		ret.selector = &syntax.Expr{Kind: syntax.Arrow, Name: as.Identifier().Text(), Origin: as}
	}
	return ret
}

func (c *syntaxLoader) convertSeparator(sep ast.ListSeparator) *syntax.Expr {
	var subs []*syntax.Expr
	for _, ref := range sep.Separator() {
		sym, _ := c.resolveRef(ref, nil /*nonterm*/)
		if sym >= c.resolver.NumTokens {
			c.Errorf(ref, "separators must be terminals")
			continue
		}
		expr := &syntax.Expr{Kind: syntax.Reference, Symbol: sym, Origin: ref, Model: c.out}
		subs = append(subs, expr)
	}
	switch len(subs) {
	case 0:
		return &syntax.Expr{Kind: syntax.Empty, Origin: sep}
	case 1:
		return subs[0]
	}
	return &syntax.Expr{
		Kind:   syntax.Sequence,
		Sub:    subs,
		Origin: sep,
	}
}

func (c *syntaxLoader) allocatePos() int {
	ret := c.rhsPos
	c.rhsPos++
	return ret
}

func (c *syntaxLoader) pushName(name string, pos int) {
	if c.rhsNames == nil {
		c.rhsNames = make(map[string]int)
	}
	var index int
	if _, ok := c.rhsNames[name+"#0"]; ok {
		for {
			index++
			if _, ok := c.rhsNames[fmt.Sprintf("%v#%v", name, index)]; !ok {
				break
			}
		}
	} else if val, ok := c.rhsNames[name]; ok {
		c.rhsNames[name+"#0"] = val
		delete(c.rhsNames, name)
		index = 1
	}
	if index > 0 {
		name = fmt.Sprintf("%v#%v", name, index)
	}
	c.rhsNames[name] = pos
}

func (c *syntaxLoader) convertPart(p ast.RhsPart, nonterm *syntax.Nonterm) *syntax.Expr {
	switch p := p.(type) {
	case *ast.Command:
		args := &syntax.CmdArgs{MaxPos: c.rhsPos}
		if len(c.rhsNames) > 0 {
			// Only names and references preceding the command are available to its code.
			// Note: the list below can include entities from a different alternative but
			// they'll be automatically filtered later on.
			args.Names = make(map[string]int)
			for k, v := range c.rhsNames {
				args.Names[k] = v
			}
		}
		text := p.Text()
		return &syntax.Expr{Kind: syntax.Command, Name: text, CmdArgs: args, Origin: p}
	case *ast.RhsAssignment:
		// Ignore any names within the assigned expression.
		old := c.rhsNames
		c.rhsNames = nil
		inner := c.convertPart(p.Inner(), nonterm)
		c.rhsNames = old

		name := p.Id().Text()
		if inner.Pos > 0 {
			c.pushName(name, inner.Pos)
		}
		subs := []*syntax.Expr{inner}
		return &syntax.Expr{Kind: syntax.Assign, Name: name, Sub: subs, Origin: p}
	case *ast.RhsPlusAssignment:
		subs := []*syntax.Expr{c.convertPart(p.Inner(), nonterm)}
		return &syntax.Expr{Kind: syntax.Append, Name: p.Id().Text(), Sub: subs, Origin: p}
	case *ast.RhsCast:
		// TODO implement
	case *ast.RhsLookahead:
		var subs []*syntax.Expr
		for _, pred := range p.Predicates() {
			sym, args := c.resolveRef(pred.Symref(), nonterm)
			if sym < c.resolver.NumTokens {
				c.Errorf(pred.Symref(), "lookahead expressions do not support terminals")
				continue
			}
			expr := &syntax.Expr{Kind: syntax.Reference, Symbol: sym, Args: args, Origin: pred.Symref(), Model: c.out}
			if _, not := pred.Not(); not {
				expr = &syntax.Expr{Kind: syntax.LookaheadNot, Sub: []*syntax.Expr{expr}, Origin: pred}
			}
			subs = append(subs, expr)
		}
		if len(subs) == 0 {
			return &syntax.Expr{Kind: syntax.Empty, Origin: p}
		}
		return &syntax.Expr{Kind: syntax.Lookahead, Sub: subs, Origin: p}
	case *ast.RhsNested:
		return c.convertRules(p.Rule0(), nonterm, report{} /*defaultReport*/, false /*topLevel*/, p)
	case *ast.RhsOptional:
		subs := []*syntax.Expr{c.convertPart(p.Inner(), nonterm)}
		return &syntax.Expr{Kind: syntax.Optional, Sub: subs, Origin: p}
	case *ast.RhsPlusList:
		seq := c.convertSequence(p.RuleParts(), nonterm, p)
		subs := []*syntax.Expr{seq}
		if sep := c.convertSeparator(p.ListSeparator()); sep.Kind != syntax.Empty {
			subs = []*syntax.Expr{seq, sep}
		}
		return &syntax.Expr{Kind: syntax.List, Sub: subs, ListFlags: syntax.OneOrMore, Pos: c.allocatePos(), Origin: p}
	case *ast.RhsStarList:
		seq := c.convertSequence(p.RuleParts(), nonterm, p)
		subs := []*syntax.Expr{seq}
		if sep := c.convertSeparator(p.ListSeparator()); sep.Kind != syntax.Empty {
			subs = []*syntax.Expr{seq, sep}
		}
		return &syntax.Expr{Kind: syntax.List, Sub: subs, Pos: c.allocatePos(), Origin: p}
	case *ast.RhsPlusQuantifier:
		subs := []*syntax.Expr{c.convertPart(p.Inner(), nonterm)}
		return &syntax.Expr{Kind: syntax.List, Sub: subs, ListFlags: syntax.OneOrMore, Pos: c.allocatePos(), Origin: p}
	case *ast.RhsStarQuantifier:
		subs := []*syntax.Expr{c.convertPart(p.Inner(), nonterm)}
		return &syntax.Expr{Kind: syntax.List, Sub: subs, Pos: c.allocatePos(), Origin: p}
	case *ast.RhsSet:
		set := c.convertSet(p.Expr(), nonterm)
		index := len(c.out.Sets)
		c.out.Sets = append(c.out.Sets, set)
		return &syntax.Expr{Kind: syntax.Set, Pos: c.allocatePos(), SetIndex: index, Origin: p, Model: c.out}
	case *ast.RhsSymbol:
		sym, args := c.resolveRef(p.Reference(), nonterm)
		c.pushName(p.Reference().Name().Text(), c.rhsPos)
		return &syntax.Expr{Kind: syntax.Reference, Symbol: sym, Args: args, Pos: c.allocatePos(), Origin: p, Model: c.out}
	case *ast.StateMarker:
		return &syntax.Expr{Kind: syntax.StateMarker, Name: p.Name().Text(), Origin: p}
	case *ast.SyntaxProblem:
		c.Errorf(p, "syntax error")
		return &syntax.Expr{Kind: syntax.Empty, Origin: p}
	}
	c.Errorf(p.TmNode(), "unsupported syntax (%T)", p)
	return &syntax.Expr{Kind: syntax.Empty, Origin: p.TmNode()}
}

func (c *syntaxLoader) convertSequence(parts []ast.RhsPart, nonterm *syntax.Nonterm, origin status.SourceNode) *syntax.Expr {
	var subs []*syntax.Expr
	for _, p := range parts {
		subs = append(subs, c.convertPart(p, nonterm))
	}
	switch len(subs) {
	case 0:
		return &syntax.Expr{Kind: syntax.Empty, Origin: origin}
	case 1:
		return subs[0]
	}
	return &syntax.Expr{
		Kind:   syntax.Sequence,
		Sub:    subs,
		Origin: origin,
	}
}

type report struct {
	node     *syntax.Expr
	selector *syntax.Expr
}

func (r report) withDefault(def report) report {
	if r.node == nil && r.selector == nil {
		return def
	}
	if r.selector == nil && def.selector != nil {
		return report{r.node, def.selector}
	}
	return r
}

func (r report) apply(expr *syntax.Expr) *syntax.Expr {
	if r.node != nil {
		e := *r.node
		e.Sub = []*syntax.Expr{expr}
		expr = &e
	}
	if r.selector != nil {
		e := *r.selector
		e.Sub = []*syntax.Expr{expr}
		expr = &e
	}
	return expr
}

func (c *syntaxLoader) isSelector(name string) bool {
	_, ok := c.cats[name]
	return ok
}

func (c *syntaxLoader) convertRules(rules []ast.Rule0, nonterm *syntax.Nonterm, defaultReport report, topLevel bool, origin status.SourceNode) *syntax.Expr {
	var subs []*syntax.Expr
	for _, rule0 := range rules {
		rule, ok := rule0.(*ast.Rule)
		if !ok {
			c.Errorf(rule0.TmNode(), "syntax error")
			continue
		}

		if topLevel {
			// Counting of RHS symbols does not restart for inline alternatives.
			c.rhsPos = 1
			c.rhsNames = nil
		}
		expr := c.convertSequence(rule.RhsPart(), nonterm, rule)
		clause, _ := rule.ReportClause()
		expr = c.convertReportClause(clause).withDefault(defaultReport).apply(expr)
		if suffix, ok := rule.RhsSuffix(); ok {
			switch suffix.Name().Text() {
			case "prec":
				sym, _ := c.resolveRef(suffix.Symref(), nonterm)
				if sym < c.resolver.NumTokens {
					expr = &syntax.Expr{Kind: syntax.Prec, Symbol: sym, Sub: []*syntax.Expr{expr}, Model: c.out, Origin: suffix}
				} else {
					c.Errorf(suffix.Symref(), "terminal is expected")
				}
			default:
				c.Errorf(suffix, "unsupported syntax")
			}
		}
		if pred, ok := rule.Predicate(); ok {
			p := c.convertPredicate(pred.PredicateExpression(), nonterm)
			if p != nil {
				expr = &syntax.Expr{Kind: syntax.Conditional, Predicate: p, Sub: []*syntax.Expr{expr}, Model: c.out, Origin: pred}
			}
		}

		subs = append(subs, expr)
	}
	switch len(subs) {
	case 0:
		return &syntax.Expr{Kind: syntax.Empty, Origin: origin}
	case 1:
		return subs[0]
	}
	return &syntax.Expr{
		Kind:   syntax.Choice,
		Sub:    subs,
		Origin: origin,
	}
}

func (c *syntaxLoader) load(p ast.ParserSection, header status.SourceNode) {
	c.out = new(syntax.Model)
	for _, sym := range c.resolver.Syms {
		c.out.Terminals = append(c.out.Terminals, sym.ID)
	}
	c.collectParams(p)
	nonterms := c.collectNonterms(p)
	c.paramPerm = make([]int, len(c.out.Params))

	c.collectInputs(p, header)
	c.collectDirectives(p)

	if errSym, ok := c.resolver.syms["error"]; ok {
		// %generate afterErr = set(follow error);
		const name = "afterErr"
		c.namedSets[name] = len(c.out.Sets)
		c.sets = append(c.sets, &grammar.NamedSet{
			Name: name,
			Expr: "set(follow error)",
		})
		c.out.Sets = append(c.out.Sets, &syntax.TokenSet{
			Kind:   syntax.Follow,
			Symbol: errSym,
		})
	}

	for _, nt := range nonterms {
		clause, _ := nt.def.ReportClause()
		defaultReport := c.convertReportClause(clause)
		expr := c.convertRules(nt.def.Rule0(), c.out.Nonterms[nt.nonterm], defaultReport, true /*topLevel*/, nt.def)
		c.out.Nonterms[nt.nonterm].Value = or(c.out.Nonterms[nt.nonterm].Value, expr)
	}
}

func or(a, b *syntax.Expr) *syntax.Expr {
	switch {
	case a == nil:
		return b
	case b == nil:
		return a
	case a.Kind == syntax.Choice && b.Kind == syntax.Choice:
		a.Sub = append(a.Sub, b.Sub...)
		return a
	case a.Kind == syntax.Choice:
		a.Sub = append(a.Sub, b)
		return a
	case b.Kind == syntax.Choice:
		b.Sub = append([]*syntax.Expr{a}, b.Sub...)
		return b
	}
	return &syntax.Expr{Kind: syntax.Choice, Sub: []*syntax.Expr{a, b}, Origin: b.Origin}
}
