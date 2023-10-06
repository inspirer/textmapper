package compiler

import (
	"strconv"
	"strings"

	"github.com/inspirer/textmapper/grammar"
	"github.com/inspirer/textmapper/parsers/tm/ast"
	"github.com/inspirer/textmapper/status"
	"github.com/inspirer/textmapper/syntax"
	"github.com/inspirer/textmapper/util/ident"
)

type optionsParser struct {
	// intermediate data that needs to be resolved
	reportList []ast.Identifier

	target string // target language
	out    *grammar.Options
	*status.Status
}

func newOptionsParser(s *status.Status) *optionsParser {
	return &optionsParser{
		out: &grammar.Options{
			TokenLine:         true,
			GenParser:         true,
			AbslIncludePrefix: "absl",
		},
		Status: s,
	}
}

func (p *optionsParser) parseFrom(file ast.File) {
	target, _ := file.Header().Target()
	p.target = target.Text()

	opts := p.out
	seen := make(map[string]int)
	for _, opt := range file.Options() {
		name := opt.Key().Text()
		if line, ok := seen[name]; ok {
			p.Errorf(opt.Key(), "reinitialization of '%v', previously declared on line %v", name, line)
		}
		line, _ := opt.Key().LineColumn()
		seen[name] = line

		switch name {
		case "package":
			p.validLangs(opt.Key(), "go")
			opts.Package = p.parseExpr(opt.Value(), opts.Package).(string)
		case "genCopyright":
			opts.Copyright = p.parseExpr(opt.Value(), opts.Copyright).(bool)
		case "tokenLine":
			opts.TokenLine = p.parseExpr(opt.Value(), opts.TokenLine).(bool)
		case "tokenLineOffset":
			opts.TokenLineOffset = p.parseExpr(opt.Value(), opts.TokenLineOffset).(bool)
		case "tokenColumn":
			opts.TokenColumn = p.parseExpr(opt.Value(), opts.TokenColumn).(bool)
		case "nonBacktracking":
			opts.NonBacktracking = p.parseExpr(opt.Value(), opts.NonBacktracking).(bool)
		case "genParser":
			opts.GenParser = p.parseExpr(opt.Value(), opts.GenParser).(bool)
		case "cancellable":
			p.validLangs(opt.Key(), "go")
			opts.Cancellable = p.parseExpr(opt.Value(), opts.Cancellable).(bool)
		case "cancellableFetch":
			p.validLangs(opt.Key(), "go")
			opts.CancellableFetch = p.parseExpr(opt.Value(), opts.CancellableFetch).(bool)
		case "writeBison":
			opts.WriteBison = p.parseExpr(opt.Value(), opts.WriteBison).(bool)
		case "recursiveLookaheads":
			opts.RecursiveLookaheads = p.parseExpr(opt.Value(), opts.RecursiveLookaheads).(bool)
		case "eventBased":
			opts.EventBased = p.parseExpr(opt.Value(), opts.EventBased).(bool)
		case "genSelector":
			p.validLangs(opt.Key(), "go")
			opts.GenSelector = p.parseExpr(opt.Value(), opts.GenSelector).(bool)
		case "fixWhitespace":
			p.validLangs(opt.Key(), "go")
			opts.FixWhitespace = p.parseExpr(opt.Value(), opts.FixWhitespace).(bool)
		case "debugParser":
			opts.DebugParser = p.parseExpr(opt.Value(), opts.DebugParser).(bool)
		case "optimizeTables":
			opts.OptimizeTables = p.parseExpr(opt.Value(), opts.OptimizeTables).(bool)
		case "defaultReduce":
			opts.DefaultReduce = p.parseExpr(opt.Value(), opts.DefaultReduce).(bool)
		case "eventFields":
			p.validLangs(opt.Key(), "go")
			opts.EventFields = p.parseExpr(opt.Value(), opts.EventFields).(bool)
		case "eventAST":
			p.validLangs(opt.Key(), "go")
			opts.EventAST = p.parseExpr(opt.Value(), opts.EventAST).(bool)
		case "reportTokens":
			p.reportList = p.parseTokenList(opt.Value())
		case "extraTypes":
			opts.ExtraTypes = p.parseExpr(opt.Value(), opts.ExtraTypes).([]syntax.ExtraType)
		case "customImpl":
			opts.CustomImpl = p.parseExpr(opt.Value(), opts.CustomImpl).([]string)
		case "fileNode":
			opts.FileNode = p.parseExpr(opt.Value(), opts.FileNode).(string)
		case "nodePrefix":
			opts.NodePrefix = p.parseExpr(opt.Value(), opts.NodePrefix).(string)
		case "lang":
			// This option often occurs in existing grammars. Ignore it.
			p.parseExpr(opt.Value(), "")
		case "namespace":
			p.validLangs(opt.Key(), "cc")
			opts.Namespace = p.parseExpr(opt.Value(), opts.Namespace).(string)
		case "includeGuardPrefix":
			p.validLangs(opt.Key(), "cc")
			opts.IncludeGuardPrefix = p.parseExpr(opt.Value(), opts.IncludeGuardPrefix).(string)
		case "filenamePrefix":
			p.validLangs(opt.Key(), "cc")
			opts.FilenamePrefix = p.parseExpr(opt.Value(), opts.FilenamePrefix).(string)
		case "abseilIncludePrefix":
			p.validLangs(opt.Key(), "cc")
			opts.AbslIncludePrefix = p.parseExpr(opt.Value(), opts.AbslIncludePrefix).(string)
		case "dirIncludePrefix":
			p.validLangs(opt.Key(), "cc")
			opts.DirIncludePrefix = p.parseExpr(opt.Value(), opts.DirIncludePrefix).(string)
		case "parseParams":
			p.validLangs(opt.Key(), "cc")
			opts.ParseParams = p.parseExpr(opt.Value(), opts.ParseParams).([]string)
		default:
			p.Errorf(opt.Key(), "unknown option '%v'", name)
		}
	}
}

func (p *optionsParser) validLangs(id ast.Identifier, langs ...string) {
	for _, l := range langs {
		if p.target == l {
			return
		}
	}
	p.Errorf(id, "option %v cannot be used when generating into %v", id.Text(), p.target)
}

func (p *optionsParser) parseExpr(e ast.Expression, defaultVal any) any {
	switch e := e.(type) {
	case *ast.Array:
		if _, ok := defaultVal.([]string); ok {
			var ret []string
			for _, el := range e.Expression() {
				lit, ok := el.(*ast.StringLiteral)
				if !ok {
					p.Errorf(el.TmNode(), "string is expected")
					continue
				}
				s, err := strconv.Unquote(lit.Text())
				if err != nil {
					p.Errorf(el.TmNode(), "cannot parse string literal: %v", err)
					continue
				}
				ret = append(ret, s)
			}
			return ret
		}
		if _, ok := defaultVal.([]syntax.ExtraType); ok {
			var ret []syntax.ExtraType
			for _, el := range e.Expression() {
				lit, ok := el.(*ast.StringLiteral)
				if !ok {
					p.Errorf(el.TmNode(), "string is expected")
					continue
				}
				s, err := strconv.Unquote(lit.Text())
				if err != nil {
					p.Errorf(el.TmNode(), "cannot parse string literal: %v", err)
					continue
				}
				ids := strings.Split(s, "->")
				for i, id := range ids {
					id := strings.TrimSpace(id)
					if !ident.IsValid(id) {
						p.Errorf(el.TmNode(), "%v is not a valid identifier", id)
						ids = nil
						break
					}
					ids[i] = id
				}
				if len(ids) > 0 {
					out := syntax.ExtraType{
						Name:       ids[0],
						Implements: ids[1:],
						Origin:     el.TmNode(),
					}
					ret = append(ret, out)
				}
			}
			return ret
		}
	case *ast.BooleanLiteral:
		if _, ok := defaultVal.(bool); ok {
			return e.Text() == "true"
		}
	case *ast.StringLiteral:
		if _, ok := defaultVal.(string); ok {
			s, err := strconv.Unquote(e.Text())
			if err != nil {
				p.Errorf(e, "cannot parse string literal: %v", err)
				return defaultVal
			}
			return s
		}
	}
	switch defaultVal.(type) {
	case []int:
		p.Errorf(e.TmNode(), "list of symbols is expected")
	case []syntax.ExtraType:
		p.Errorf(e.TmNode(), `list of strings with names is expected. E.g. ["Foo", "Bar -> Expr"]`)
	default:
		p.Errorf(e.TmNode(), "%T is expected", defaultVal)
	}
	return defaultVal
}

func (p *optionsParser) parseTokenList(e ast.Expression) []ast.Identifier {
	if arr, ok := e.(*ast.Array); ok {
		var ret []ast.Identifier
		for _, el := range arr.Expression() {
			if ref, ok := el.(*ast.Symref); ok {
				if args, ok := ref.Args(); ok {
					p.Errorf(args, "terminals cannot be templated")
					continue
				}
				ret = append(ret, ref.Name())
				continue
			}
			p.Errorf(el.TmNode(), "symbol reference is expected")
		}
		return ret
	}
	p.Errorf(e.TmNode(), "list of symbols is expected")
	return nil
}

func (p *optionsParser) resolve(resolver *resolver) {
	opts := p.out
	opts.ReportTokens = make([]int, 0, len(p.reportList))
	for _, id := range p.reportList {
		sym, ok := resolver.syms[id.Text()]
		if !ok {
			p.Errorf(id, "unresolved reference '%v'", id.Text())
			continue
		}
		opts.ReportTokens = append(opts.ReportTokens, sym)
	}
}
