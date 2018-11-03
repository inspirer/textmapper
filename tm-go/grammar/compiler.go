package grammar

import (
	"github.com/inspirer/textmapper/tm-go/lex"
	"github.com/inspirer/textmapper/tm-go/status"
	"github.com/inspirer/textmapper/tm-parsers/tm/ast"
	"strconv"
)

// Compile validates and compiles grammar files.
func Compile(file ast.File) (*Grammar, error) {
	c := &compiler{
		file: file,
		out: &Grammar{
			Lexer: &Lexer{},
		},
		syms: make(map[string]int),
	}
	c.compileLexer()
	return c.out, c.s.Err()
}

type compiler struct {
	file ast.File
	out  *Grammar
	s    status.Status

	syms        map[string]int
	conds       map[string]int
	inclusiveSC []int
}

func (c *compiler) compileLexer() {
	c.addToken(Eoi, nil, nil)
	c.addToken(InvalidToken, nil, nil)

	c.collectStartConds()
	c.traverseLexer(c.file.Lexer().LexerPart(), c.inclusiveSC, nil /*parent patterns*/)
}

func (c *compiler) collectStartConds() {
	conds := make(map[string]bool)
	var names []string

	insert := func(n *ast.Node, excl bool) {
		name := n.Text()
		if _, exists := conds[name]; exists {
			c.errorf(n, "redeclaration of %v", name)
			return
		}
		conds[name] = excl
		names = append(names, name)
	}
	for _, p := range c.file.Lexer().LexerPart() {
		switch p := p.(type) {
		case *ast.ExclusiveStartConds:
			for _, s := range p.States() {
				insert(s.Node, false)
			}
		case *ast.InclusiveStartConds:
			for _, s := range p.States() {
				insert(s.Node, true)
			}
		}
	}

	if _, exists := conds[lex.Initial]; !exists {
		names = append(names, lex.Initial)
		copy(names[1:], names)
		names[0] = lex.Initial
	}

	c.out.StartConditions = names
	c.conds = make(map[string]int)
	for i, name := range names {
		c.conds[name] = i
		if !conds[name] {
			c.inclusiveSC = append(c.inclusiveSC, i)
		}
	}
}

func (c *compiler) resolveSC(sc ast.StartConditions) []int {
	var ret []int
	for _, ref := range sc.Stateref() {
		name := ref.Name().Text()
		if val, ok := c.conds[name]; ok {
			ret = append(ret, val)
			continue
		}
		c.errorf(ref.Name(), "unresolved reference %v", name)
	}
	return ret
}

func (c *compiler) addToken(name string, t *ast.RawType, n status.SourceNode) int {
	var rawType string
	if t != nil {
		rawType = t.Text()
	}
	if i, exists := c.syms[name]; exists {
		sym := c.out.Syms[i]
		if sym.Type != rawType {
			anchor := n
			if t != nil {
				anchor = t
			}
			c.errorf(anchor, "terminal type redeclaration for %v, was %v", name, sym.PrettyType())
		}
		return sym.Index
	}

	sym := Symbol{
		Index:  len(c.syms),
		Type:   rawType,
		Name:   name,
		Origin: n,
	}
	c.syms[name] = sym.Index
	c.out.Syms = append(c.out.Syms, sym)
	return sym.Index
}

func (c *compiler) traverseLexer(parts []ast.LexerPart, defaultSCs []int, p *patterns) {
	ps := &patterns{
		parent: p,
		set:    make(map[string]*lex.Regexp),
		used:   make(map[string]bool),
	}
	for _, p := range parts {
		switch p := p.(type) {
		case *ast.Lexeme:
			tok := c.addToken(p.Name().Text(), p.RawType(), p.Name())
			if pat := p.Pattern(); pat != nil {
				re, err := parsePattern(*pat)
				c.s.AddError(err)
				rule := &lex.Rule{RE: re, StartConditions: defaultSCs, Origin: p, OriginName: p.Name().Text()}
				if prio := p.Priority(); prio != nil {
					rule.Precedence, _ = strconv.Atoi(prio.Text())
				}
				if sc := p.StartConditions(); sc != nil {
					rule.StartConditions = c.resolveSC(*sc)
				}
				// TODO take the action instead
				rule.Action = tok
				// TODO do something with the rule
			}
		case *ast.NamedPattern:
			c.s.AddError(ps.add(p))
		case *ast.StartConditionsScope:
			newDefaults := c.resolveSC(p.StartConditions())
			c.traverseLexer(p.LexerPart(), newDefaults, ps)
		case *ast.SyntaxProblem, *ast.DirectiveBrackets:
			c.errorf(p.TmNode(), "syntax error")
		}
	}
}

func (c *compiler) errorf(n status.SourceNode, format string, a ...interface{}) {
	c.s.Errorf(n, format, a...)
}

type patterns struct {
	parent *patterns
	set    map[string]*lex.Regexp
	used   map[string]bool
}

func (p *patterns) Resolve(name string) *lex.Regexp {
	if v, ok := p.set[name]; ok {
		return v
	}
	if p.parent != nil {
		return p.parent.Resolve(name)
	}
	return nil
}

var emptyRE = lex.MustParse("")

func (p *patterns) add(np *ast.NamedPattern) error {
	name := np.Name().Text()
	if _, exists := p.set[name]; exists {
		return status.Errorf(np.Name(), "redeclaration of %v", name)
	}

	re, err := parsePattern(np.Pattern())
	p.set[name] = re
	return err
}

func parsePattern(p ast.Pattern) (*lex.Regexp, error) {
	text := p.Text()
	text = text[1 : len(text)-1]
	re, err := lex.ParseRegexp(text)
	if err != nil {
		rng := p.SourceRange()
		err := err.(lex.ParseError)
		if err.Offset <= err.EndOffset && err.EndOffset <= len(text) && err.Offset < len(text) {
			if err.Offset < err.EndOffset {
				rng.EndOffset = rng.Offset + err.EndOffset + 1
			} else {
				rng.EndOffset--
			}
			rng.Offset += err.Offset + 1
			rng.Column += err.Offset + 1
		}
		return emptyRE, &status.Error{Origin: rng, Msg: err.Error()}
	}
	return re, nil
}
