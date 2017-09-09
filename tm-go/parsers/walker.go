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
)

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

func (f file) tryParse() string {
	defer func() {
		if r := recover(); r != nil {
			fmt.Printf("%v: recovered: %v\n", f.name, r)
		}
	}()

	l := new(js.Lexer)
	p := new(js.Parser)
	l.Init(f.content)
	switch f.ext() {
	case "js":
		l.Dialect = js.Javascript
	case "ts":
		l.Dialect = js.Typescript
	case "tsx":
		l.Dialect = js.TypescriptJsx
	}
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
		ext := filepath.Ext(path)
		if ext != ".ts" && ext != ".js" {
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
