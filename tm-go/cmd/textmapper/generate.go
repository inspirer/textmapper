package main

import (
	"fmt"
	"io/ioutil"
	"os"
	"path/filepath"
	"strings"

	"github.com/inspirer/textmapper/tm-go/grammar"
	"github.com/inspirer/textmapper/tm-go/status"
	"github.com/inspirer/textmapper/tm-parsers/tm"
	"github.com/inspirer/textmapper/tm-parsers/tm/ast"
)

var genCmd = &command{
	Name:  "generate",
	Title: "generate grammars",
	Usage: " [flags] [grammars...]",
	Help: `By default, Textmapper generates code for all grammars in the current directory.

Flags:`,
}

var (
	outputDir   = genCmd.Flags.String("o", "", "output directory")
	includeDirs = genCmd.Flags.String("i", "", "comma-separated list of directories with code generation templates")
	noBuiltins  = genCmd.Flags.Bool("x", false, "do not use built-in templates for code generation")
	debug       = genCmd.Flags.Bool("d", false, "output extra debug info")
)

func init() {
	genCmd.Run = generate
}

func generate(files []string) error {
	if len(files) == 0 {
		var err error
		files, err = filepath.Glob("*.tm")
		if err != nil {
			return err
		}

		if len(files) == 0 {
			return fmt.Errorf("no .tm files found in the current directory")
		}
	}

	var includes []string
	for _, dir := range strings.Split(*includeDirs, ",") {
		if dir == "" {
			continue
		}
		fi, err := os.Stat(dir)
		if err != nil {
			return err
		}
		if !fi.IsDir() {
			return fmt.Errorf("%v is not a directory", dir)
		}
		includes = append(includes, dir)
	}

	if *outputDir != "" {
		fi, err := os.Stat(*outputDir)
		if err != nil {
			return err
		}
		if !fi.IsDir() {
			return fmt.Errorf("%v is not a directory", *outputDir)
		}
	}

	var s status.Status
	for _, path := range files {
		content, err := ioutil.ReadFile(path)
		if err != nil {
			s.AddError(err)
			continue
		}

		tree, err := ast.Parse(path, string(content), tm.StopOnFirstError)
		if err != nil {
			s.AddError(err)
			continue
		}

		_, err = grammar.Compile(ast.File{Node: tree.Root()})
		if err != nil {
			s.AddError(err)
			continue
		}

		// TODO
	}
	return s.Err()
}
