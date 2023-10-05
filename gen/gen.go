// Package gen generates code for compiled grammars.
package gen

import (
	"context"
	"fmt"
	"os"
	"path"
	"path/filepath"
	"sort"
	"strings"
	"text/template"
	"time"

	"github.com/inspirer/textmapper/compiler"
	"github.com/inspirer/textmapper/grammar"
)

// Writer provides a way to save generated files to disk.
type Writer interface {
	Write(filename, content string) error
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
			tmpl, err = tmpl.Parse(lang.SharedDefs)
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

		// Post-process generated content and write it do disk.
		outName := g.Options.FilenamePrefix + f.name
		src := buf.String()
		switch g.TargetLang {
		case "go":
			src = FormatGo(outName, ExtractGoImports(src), opts.Compat)
		}
		if err := w.Write(outName, src); err != nil {
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
	Optimized  bool
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
		var suffix string
		if s.Optimized {
			suffix = " (optimized)"
		}
		ret = append(ret, fmt.Sprintf("%v KB%v", s.ParserSize/1024, suffix))
	}
	return strings.Join(ret, ", ")
}

// GenerateFile reads, compiles, and generates code for a grammar stored in a file.
func GenerateFile(ctx context.Context, path string, w Writer, opts Options) (Stats, error) {
	var ret Stats
	content, err := os.ReadFile(path)
	if err != nil {
		return ret, err
	}

	start := time.Now()
	g, err := compiler.Compile(ctx, path, string(content), compiler.Params{Compat: opts.Compat})
	ret.Compiling = time.Since(start)
	if err != nil {
		return ret, err
	}

	if g.Parser != nil && g.Parser.Tables != nil {
		ret.States = g.Parser.Tables.NumStates
		ret.ParserSize = g.Parser.Tables.SizeBytes()
		ret.Optimized = g.Parser.Tables.Optimized != nil
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
	c := &fileContext{
		filename: filename,
		Grammar:  g,
		noPrefix: make(map[string]bool),
	}
	ret := template.FuncMap{}
	for _, opt := range strings.Split(g.Options.WithoutPrefix, ",") {
		opt = strings.TrimSpace(opt)
		if strings.HasSuffix(opt, "*") {
			p := strings.TrimSuffix(opt, "*")
			c.reservedPrefixes = append(c.reservedPrefixes, p)
		} else {
			c.noPrefix[opt] = true
		}
	}
	if g.Parser != nil && g.Parser.Types != nil {
		c.cats = make(map[string]int)
		for i, cat := range g.Parser.Types.Categories {
			c.cats[cat.Name] = i
		}
		ret["is_cat"] = c.isCat
		ret["expand_selector"] = c.expandSelector
	}
	switch g.TargetLang {
	case "go":
		ret["pkg"] = c.goPackage
		ret["node_id"] = c.nodeID
		ret["is_file_node"] = c.isFileNode
	case "cc":
		ret["is_file_node"] = c.isFileNode
	}
	return ret
}

type fileContext struct {
	filename string
	*grammar.Grammar
	cats map[string]int

	// Expanded content of the WithoutPrefix option.
	noPrefix         map[string]bool
	reservedPrefixes []string
}

func (c *fileContext) goPackage(targetPkg string) string {
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

func (c *fileContext) nodeID(name string) string {
	if c.Options.NodePrefix == "" || c.noPrefix[name] {
		return name
	}
	for _, reserved := range c.reservedPrefixes {
		if strings.HasPrefix(name, reserved) {
			return name
		}
	}
	return c.Options.NodePrefix + name
}

func (c *fileContext) isFileNode(name string) bool {
	return name == c.Options.FileNode
}

func (c *fileContext) isCat(name string) bool {
	_, ok := c.cats[name]
	return ok
}

func (c *fileContext) expandSelector(sel []string) []string {
	var ret []string
	seen := make(map[string]bool)
	add := func(s string) {
		if !seen[s] {
			seen[s] = true
			ret = append(ret, s)
		}
	}
	for _, name := range sel {
		i, isCat := c.cats[name]
		if !isCat {
			add(name)
			continue
		}
		for _, name := range c.Parser.Types.Categories[i].Types {
			add(name)
		}
	}
	sort.Strings(ret)
	return ret
}
