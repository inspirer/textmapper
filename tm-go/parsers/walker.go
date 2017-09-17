package main

import (
	"fmt"
	"io/ioutil"
	"os"
	"path/filepath"
	"sort"
	"strings"
	"unicode/utf8"

	"github.com/inspirer/textmapper/tm-parsers/js"
	"github.com/inspirer/textmapper/tm-parsers/tm"
)

var supportedExts = map[string]bool{
	"tm": true,
	"js": true,
	"jsx": true,
	"ts": true,
}

type file struct {
	name    string
	content string
}

func (f file) ext() string {
	ext := filepath.Ext(f.name)
	if strings.HasPrefix(ext, ".") {
		ext = ext[1:]
	}
	return ext
}

func parseJS(f file, dialect js.Dialect) string {
	l := new(js.Lexer)
	p := new(js.Parser)
	l.Init(f.content)
	l.Dialect = dialect
	result := "ok"
	errHandler := func(se js.SyntaxError) bool { return false }
	p.Init(errHandler, func(nt js.NodeType, offset, endoffset int) {})
	if err := p.Parse(l); err != nil {
		result = "parse_err"
		var suffix string
		if err, ok := err.(js.SyntaxError); ok {
			suffix = fmt.Sprintf(" on `%v`", f.content[err.Offset:err.Endoffset])
		}
		fmt.Printf("%v: %v%v\n", f.name, err, suffix)
	}
	return result
}

func parseTM(f file) string {
	l := new(tm.Lexer)
	p := new(tm.Parser)
	l.Init(f.content)
	result := "ok"
	errHandler := func(se tm.SyntaxError) bool { return false }
	p.Init(errHandler, func(nt tm.NodeType, offset, endoffset int) {})
	if err := p.ParseInput(l); err != nil {
		result = "parse_err"
		var suffix string
		if err, ok := err.(tm.SyntaxError); ok {
			suffix = fmt.Sprintf(" on `%v`", f.content[err.Offset:err.Endoffset])
		}
		fmt.Printf("%v: %v%v\n", f.name, err, suffix)
	}
	return result
}

func (f file) tryParse() string {
	defer func() {
		if r := recover(); r != nil {
			fmt.Printf("%v: recovered: %v\n", f.name, r)
		}
	}()

	switch f.ext() {
	case "js", "jsx":
		return parseJS(f, js.Javascript)
	case "ts":
		return parseJS(f, js.Typescript)
	case "tsx":
		return parseJS(f, js.TypescriptJsx)
	case "tm":
		return parseTM(f)
	}

	return "no_parser"
}

func preloadAll(root string) ([]file, error) {
	var ret []file
	err := filepath.Walk(root, func(path string, info os.FileInfo, err error) error {
		if err != nil {
			return err
		}
		if !info.Mode().IsRegular() {
			if info.Name() == ".git" {
				fmt.Println("skipping .git")
				return filepath.SkipDir
			}
			return nil
		}

		if ext := strings.TrimPrefix(filepath.Ext(path), "."); !supportedExts[ext] {
			return nil
		}
		data, err := ioutil.ReadFile(path)
		if err != nil {
			return err
		}
		if !utf8.Valid(data) {
			fmt.Printf("skipping a non-utf8 file: %v\n", path)
			return nil
		}
		ret = append(ret, file{path, string(data)})
		return nil
	})
	if err != nil {
		return nil, err
	}
	return ret, nil
}

func main() {
	files, err := preloadAll(os.Args[1])
	if err != nil {
		fmt.Println(err)
		return
	}

	fmt.Printf("loaded %v files\n", len(files))

	results := map[string]int{}
	for _, f := range files {
		outcome := f.tryParse()
		results[outcome+" ("+f.ext()+")"]++
	}

	var keys []string
	for k := range results {
		keys = append(keys, k)
	}
	sort.Strings(keys)

	fmt.Println()
	for _, k := range keys {
		fmt.Printf("%10v %v\n", results[k], k)
	}
}
