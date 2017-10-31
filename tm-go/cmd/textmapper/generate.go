package main

import (
	"path/filepath"
	"os"
	"io/ioutil"
	"fmt"
	"strings"
)

var genCmd = &command{
	Name: "generate",
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

	var inputs []input
	for _, path := range files {
		content, err := ioutil.ReadFile(path)
		if err != nil {
			return err
		}
		inputs = append(inputs, input{
			path:       path,
			content:    string(content),
			outputDir:  *outputDir,
			noBuiltins: *noBuiltins,
			includeDirs: includes,
			debug: *debug,
		})
	}

	// TODO
	return nil
}

type input struct {
	path        string
	content     string
	outputDir   string
	includeDirs []string
	noBuiltins  bool
	debug       bool
}
