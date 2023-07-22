package main

import (
	"context"
	"fmt"
	"log"
	"os"
	"path/filepath"
	"runtime/pprof"
	"strings"
	"time"

	"github.com/inspirer/textmapper/gen"
	"github.com/inspirer/textmapper/status"
	"github.com/inspirer/textmapper/util/diff"
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
	diffFlag    = genCmd.Flags.Bool("diff", false, "compare generated content against files on disk")
	compatFlag  = genCmd.Flags.Bool("compat", false, "disable optimizations and attempt to produce the same output as the Java version")
	cpuprofile  = genCmd.Flags.String("cpuprofile", "", "write a CPU profile into a given file")
)

func init() {
	genCmd.Run = generate
}

func generate(ctx context.Context, files []string) error {
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
	start := time.Now()
	opts := gen.Options{
		Compat:      *compatFlag,
		IncludeDirs: includes,
		NoBuiltins:  *noBuiltins,
	}

	for _, path := range files {
		stats, err := gen.GenerateFile(ctx, path, writer{OutDir: *outputDir}, opts)
		if msg := stats.String(); msg != "" {
			fmt.Printf("%v (%v)\n", msg, path)
		}
		if err != nil {
			s.AddError(err)
		}
	}

	if *cpuprofile != "" {
		elapsed := time.Since(start)
		f, err := os.Create(*cpuprofile)
		if err != nil {
			log.Fatal(err)
		}
		pprof.StartCPUProfile(f)
		defer pprof.StopCPUProfile()

		n := int((30*time.Second)/elapsed) + 1
		fmt.Printf("First run completed in %v. Running it %v times.\n", elapsed, n)
		for i := 0; i < n; i++ {
			for _, path := range files {
				gen.GenerateFile(ctx, path, writer{OutDir: *outputDir}, opts)
			}
		}
	}
	return s.Err()
}

type writer struct {
	OutDir string
}

func (w writer) Write(genfile, content string) error {
	path := filepath.Join(w.OutDir, genfile)
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
	err := os.MkdirAll(filepath.Dir(path), 0755)
	if err != nil {
		return fmt.Errorf("Error creating directory: %w", err)
	}
	return os.WriteFile(path, []byte(content), 0644)
}
