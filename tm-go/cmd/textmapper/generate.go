package main

import (
	"fmt"
	"os"
	"path/filepath"
	"strings"

	"github.com/inspirer/textmapper/tm-go/gen"
	"github.com/inspirer/textmapper/tm-go/status"
	"github.com/inspirer/textmapper/tm-go/util/diff"
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
	diffFlag    = genCmd.Flags.Bool("diff", false, "compare generated content against files on disk")
	compatFlag  = genCmd.Flags.Bool("compat", false, "disable optimizations and attempt to produce the same output as the Java version")
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
		if err := gen.GenerateFile(path, writer{}, *compatFlag); err != nil {
			s.AddError(err)
		}
	}
	return s.Err()
}

type writer struct{}

func (w writer) Write(genfile, content string) error {
	path := filepath.Join(*outputDir, genfile)
	if *diffFlag {
		ondisk, err := os.ReadFile(path)
		if err != nil {
			return err
		}
		if diff := diff.LineDiff(string(ondisk), content); diff != "" {
			fmt.Printf("The on-disk content differs from the generated one.\n--- %v\n+++ %v (generated)\n%v\n", path, genfile, diff)
		}
		return nil
	}

	return os.WriteFile(path, []byte(content), 0644)
}
