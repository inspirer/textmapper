// Package gen generates code for compiled grammars.
package gen

import (
	"fmt"
	"os"
	"path"
	"path/filepath"
	"strings"
	"text/template"
	"time"

	"github.com/inspirer/textmapper/compiler"
	"github.com/inspirer/textmapper/grammar"
	"github.com/inspirer/textmapper/parsers/tm"
	"github.com/inspirer/textmapper/parsers/tm/ast"
)

// Writer provides a way to save generated files to disk.
type Writer interface {
	Write(filename, content string) error
}

var languages = map[string]*language{
	"go": golang,
}

type Options struct {
	Compat      bool
	IncludeDirs []string
	NoBuiltins  bool
}

func loadOverlay(name string, tmpl *template.Template, opts Options) (*template.Template, error) {
	name, _, _ = strings.Cut(path.Base(name), ".")
	name += ".go.tmpl"

	for _, dir := range opts.IncludeDirs {
		overlayPath := filepath.Join(dir, name)
		fi, err := os.Stat(overlayPath)
		if err == nil && !fi.IsDir() {
			content, err := os.ReadFile(overlayPath)
			if err != nil {
				return tmpl, fmt.Errorf("failed to read %s: %v", overlayPath, err)
			}
			tmpl, err = tmpl.Parse(string(content))
			if err != nil {
				return tmpl, fmt.Errorf("failed to parse %s: %v", overlayPath, err)
			}
		}
	}
	return tmpl, nil
}

// Generate generates code for a grammar.
func Generate(g *grammar.Grammar, w Writer, opts Options) error {
	lang, ok := languages[g.TargetLang]
	if !ok {
		return fmt.Errorf("unsupported language: %s", g.TargetLang)
	}

	templates := lang.templates(g)
	for _, f := range templates {
		tmpl := template.New("main").Funcs(funcMap).Funcs(extraFuncs(f.name, g))

		// Load shared templates.
		var err error
		if !opts.NoBuiltins {
			tmpl, err = tmpl.Parse(sharedDefs)
			if err != nil {
				return fmt.Errorf("error in built-in shared_defs: %v", err)
			}
		}
		tmpl, err = loadOverlay(g.TargetLang+"_shared", tmpl, opts)
		if err != nil {
			return err
		}

		// Load templates for the current file.
		if !opts.NoBuiltins {
			tmpl, err = tmpl.Parse(f.template)
			if err != nil {
				return fmt.Errorf("error in built-in %v: %v", f.name, err)
			}
		}
		tmpl, err = loadOverlay(g.TargetLang+"_"+f.name, tmpl, opts)
		if err != nil {
			return err
		}

		// TODO come up with a way to parse this once
		_, err = tmpl.Parse(g.CustomTemplates)
		if err != nil {
			return fmt.Errorf("error in inline template: %v", err)
		}

		var buf strings.Builder
		err = tmpl.Execute(&buf, g)
		if err != nil {
			return fmt.Errorf("error generating %v: %w", f.name, err)
		}
		src := Format(f.name, ExtractImports(buf.String()), opts.Compat)
		if err := w.Write(f.name, src); err != nil {
			return err
		}
	}
	return nil
}

type Stats struct {
	Compiling  time.Duration
	Gen        time.Duration
	States     int
	ParserSize int
}

func (s Stats) String() string {
	var ret []string
	if s.Compiling > 0 {
		ret = append(ret, fmt.Sprintf("lalr: %v", s.Compiling.Round(time.Millisecond)))
	}
	if s.Gen > 0 {
		ret = append(ret, fmt.Sprintf("text: %v", s.Gen.Round(time.Millisecond)))
	}
	if s.States > 0 {
		ret = append(ret, fmt.Sprintf("parser: %v states", s.States))
	}
	if s.ParserSize > 0 {
		ret = append(ret, fmt.Sprintf("%v KB", s.ParserSize/1024))
	}
	return strings.Join(ret, ", ")
}

// GenerateFile reads, compiles, and generates code for a grammar stored in a file.
func GenerateFile(path string, w Writer, opts Options) (Stats, error) {
	var ret Stats
	content, err := os.ReadFile(path)
	if err != nil {
		return ret, err
	}

	tree, err := ast.Parse(path, string(content), tm.StopOnFirstError)
	if err != nil {
		return ret, err
	}

	start := time.Now()
	g, err := compiler.Compile(ast.File{Node: tree.Root()}, opts.Compat)
	ret.Compiling = time.Since(start)
	if err != nil {
		return ret, err
	}

	if g.Parser != nil && g.Parser.Tables != nil {
		ret.States = g.Parser.Tables.NumStates
		ret.ParserSize = g.Parser.Tables.SizeBytes()
	}
	if g.TargetLang == "" {
		// A source-only grammar.
		return ret, fmt.Errorf("no target language")
	}

	start = time.Now()
	err = Generate(g, w, opts)
	ret.Gen = time.Since(start)
	return ret, err
}

func extraFuncs(filename string, g *grammar.Grammar) template.FuncMap {
	c := &context{filename: filename, Grammar: g}
	ret := template.FuncMap{}
	switch g.TargetLang {
	case "go":
		ret["pkg"] = c.goPackage
		ret["node_id"] = c.nodeID
		ret["is_file_node"] = c.isFileNode
	}
	return ret
}

// context provides
type context struct {
	filename string
	*grammar.Grammar
}

func (c *context) goPackage(targetPkg string) string {
	if targetPkg == "main" {
		targetPkg = ""
	}
	var currPkg string
	if i := strings.IndexByte(c.filename, '/'); i >= 0 {
		currPkg = c.filename[:i]
	}
	if currPkg == targetPkg {
		// Same package, no need to import.
		return ""
	}
	ret := c.Options.Package
	if targetPkg != "" {
		ret = path.Join(ret, targetPkg)
	}
	return `"` + ret + `".`
}

func (c *context) nodeID(name string) string {
	return name
}

func (c *context) isFileNode(name string) bool {
	return name == c.Options.FileNode
}
