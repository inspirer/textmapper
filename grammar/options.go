package grammar

import (
	"strconv"
	"strings"

	"github.com/inspirer/textmapper/parsers/tm/ast"
	"github.com/inspirer/textmapper/status"
	"github.com/inspirer/textmapper/syntax"
	"github.com/inspirer/textmapper/util/ident"
)

type optionsParser struct {
	// intermediate data that needs to be resolved
	reportTokens map[string]bool
	reportList   []ast.Identifier

	out *Options
	s   status.Status
}

func (p *optionsParser) parseFrom(file ast.File) error {
	opts := p.out
	seen := make(map[string]int)
	for _, opt := range file.Options() {
		name := opt.Key().Text()
		if line, ok := seen[name]; ok {
			p.errorf(opt.Key(), "reinitialization of '%v', previously declared on line %v", name, line)
		}
		line, _ := opt.Key().LineColumn()
		seen[name] = line

		switch name {
		case "package":
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
		case "cancellable":
			opts.Cancellable = p.parseExpr(opt.Value(), opts.Cancellable).(bool)
		case "writeBison":
			opts.WriteBison = p.parseExpr(opt.Value(), opts.WriteBison).(bool)
		case "recursiveLookaheads":
			opts.RecursiveLookaheads = p.parseExpr(opt.Value(), opts.RecursiveLookaheads).(bool)
		case "eventBased":
			opts.EventBased = p.parseExpr(opt.Value(), opts.EventBased).(bool)
		case "genSelector":
			opts.GenSelector = p.parseExpr(opt.Value(), opts.GenSelector).(bool)
		case "fixWhitespace":
			opts.FixWhitespace = p.parseExpr(opt.Value(), opts.FixWhitespace).(bool)
		case "debugParser":
			opts.DebugParser = p.parseExpr(opt.Value(), opts.DebugParser).(bool)
		case "eventFields":
			opts.EventFields = p.parseExpr(opt.Value(), opts.EventFields).(bool)
		case "eventAST":
			opts.EventAST = p.parseExpr(opt.Value(), opts.EventAST).(bool)
		case "reportTokens":
			p.reportList = p.parseTokenList(opt.Value())
			p.reportTokens = make(map[string]bool)
			for _, id := range p.reportList {
				p.reportTokens[id.Text()] = true
			}
		case "extraTypes":
			opts.ExtraTypes = p.parseExpr(opt.Value(), opts.ExtraTypes).([]syntax.ExtraType)
		case "customImpl":
			opts.CustomImpl = p.parseExpr(opt.Value(), opts.CustomImpl).([]string)
		case "fileNode":
			opts.FileNode = p.parseExpr(opt.Value(), opts.FileNode).(string)
		case "lang":
			// This option often occurs in existing grammars. Ignore it.
			p.parseExpr(opt.Value(), "")
		default:
			p.errorf(opt.Key(), "unknown option '%v'", name)
		}
	}

	return p.s.Err()
}

func (p *optionsParser) parseExpr(e ast.Expression, defaultVal any) any {
	switch e := e.(type) {
	case *ast.Array:
		if _, ok := defaultVal.([]string); ok {
			var ret []string
			for _, el := range e.Expression() {
				lit, ok := el.(*ast.StringLiteral)
				if !ok {
					p.errorf(el.TmNode(), "string is expected")
					continue
				}
				s, err := strconv.Unquote(lit.Text())
				if err != nil {
					p.errorf(el.TmNode(), "cannot parse string literal: %v", err)
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
					p.errorf(el.TmNode(), "string is expected")
					continue
				}
				s, err := strconv.Unquote(lit.Text())
				if err != nil {
					p.errorf(el.TmNode(), "cannot parse string literal: %v", err)
					continue
				}
				ids := strings.Split(s, "->")
				for i, id := range ids {
					id := strings.TrimSpace(id)
					if !ident.IsValid(id) {
						p.errorf(el.TmNode(), "%v is not a valid identifier", id)
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
				p.errorf(e, "cannot parse string literal: %v", err)
				return defaultVal
			}
			return s
		}
	}
	switch defaultVal.(type) {
	case []int:
		p.errorf(e.TmNode(), "list of symbols is expected")
	case []syntax.ExtraType:
		p.errorf(e.TmNode(), `list of strings with names is expected. E.g. ["Foo", "Bar -> Expr"]`)
	default:
		p.errorf(e.TmNode(), "%T is expected", defaultVal)
	}
	return defaultVal
}

func (p *optionsParser) parseTokenList(e ast.Expression) []ast.Identifier {
	if arr, ok := e.(*ast.Array); ok {
		var ret []ast.Identifier
		for _, el := range arr.Expression() {
			if ref, ok := el.(*ast.Symref); ok {
				if args, ok := ref.Args(); ok {
					p.errorf(args, "terminals cannot be templated")
					continue
				}
				ret = append(ret, ref.Name())
				continue
			}
			p.errorf(el.TmNode(), "symbol reference is expected")
		}
		return ret
	}
	p.errorf(e.TmNode(), "list of symbols is expected")
	return nil
}

func (p *optionsParser) resolve(syms map[string]int) error {
	opts := p.out
	var s status.Status
	opts.ReportTokens = make([]int, 0, len(p.reportList))
	for _, id := range p.reportList {
		sym, ok := syms[id.Text()]
		if !ok {
			s.Errorf(id, "unresolved reference '%v'", id.Text())
			continue
		}
		opts.ReportTokens = append(opts.ReportTokens, sym)
	}
	return s.Err()
}

func (p *optionsParser) errorf(n status.SourceNode, format string, a ...interface{}) {
	p.s.Errorf(n, format, a...)
}
