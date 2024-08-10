package main

import (
	"context"
	"fmt"
	"os"
	"path/filepath"
	"time"

	"github.com/inspirer/textmapper/compiler"
	"github.com/inspirer/textmapper/lalr"
	"github.com/inspirer/textmapper/status"
)

var debugCmd = &command{
	Name:  "debug",
	Title: "print out automaton statistics and parsing tables in a human-readable format",
	Usage: " [flags] [grammars...]",
	Help: `By default, Textmapper prints out debug info for all grammars in the current directory.

Flags:`,
}

var (
	stats  = debugCmd.Flags.Bool("stats", false, "output generated table statistics")
	tables = debugCmd.Flags.Bool("tables", false, "dump generated tables in a human-readable format")
)

func init() {
	debugCmd.Run = debug
}

func debug(ctx context.Context, files []string) error {
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

	var s status.Status
	for _, path := range files {
		err := debugFile(ctx, path)
		if err != nil {
			s.AddError(err)
		}
	}
	return s.Err()
}

func debugFile(ctx context.Context, path string) error {
	content, err := os.ReadFile(path)
	if err != nil {
		return err
	}

	start := time.Now()
	g, err := compiler.Compile(ctx, path, string(content), compiler.Params{DebugTables: *tables})
	if g == nil {
		return err
	}
	if err != nil {
		status.Print(os.Stderr, err)
		fmt.Fprintln(os.Stderr)
	}

	if *stats {
		fmt.Printf("Compiled %v in %v\n", path, time.Since(start))
	}

	if *stats && g.Lexer != nil {
		fmt.Println()
		fmt.Print(g.Lexer.TableStats())
	}

	if *stats && g.Parser != nil && g.Parser.Tables != nil {
		fmt.Print(g.Parser.TableStats())

		start = time.Now()
		newEnc := lalr.Optimize(g.Parser.Tables.DefaultEnc, g.NumTokens, len(g.Parser.Tables.RuleLen), g.Options.DefaultReduce)
		fmt.Printf("Optimized tables in %v\n", time.Since(start))

		fmt.Print(newEnc.TableStats())
	}

	if *tables && g.Parser != nil && g.Parser.Tables != nil {
		for _, info := range g.Parser.Tables.DebugInfo {
			fmt.Println(info)
		}
	}

	return nil
}
