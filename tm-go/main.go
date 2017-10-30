package main

import (
	"flag"
	"fmt"
	"os"
)

var (
	outputDir          = flag.String("o", "", "output directory")
	importDirs         = flag.String("i", "", "comma-separated list of directories with code generation templates")
	noDefaultTemplates = flag.String("x", "", "do not use built-in templates for code generation")
	debug              = flag.Bool("d", false, "output extra debug info")
)

var (
	exitCode = 0
)

const (
	version     = `0.10.1`
	usageString = `Textmapper is an LALR parser and lexer generator.

Usage:
    textmapper <command> [flags] [grammars ...]

Commands:
    generate  generate grammars
    fmt       format given grammar files in-place (unimplemented)
    html      output a syntax highlighted HTML (unimplemented)
    version   print the tool version

Flags:
`
)

func usage() {
	fmt.Fprint(os.Stderr, usageString)
	flag.PrintDefaults()
}

func main() {
	flag.Usage = usage
	flag.Parse()

	args := flag.Args()
	if len(args) == 0 {
		flag.Usage()
		os.Exit(exitCode)
	}

	switch cmd := args[0]; cmd {
	case "generate":
		generate(args[1:])
	case "version":
		fmt.Fprintf(os.Stderr, "textmapper version %v\n", version)
	default:
		fmt.Fprintf(os.Stderr, "textmapper: unknown command: %q\n", cmd)
		exitCode = 2
	}

	os.Exit(exitCode)
}

func generate(files []string) {
	// TODO implement
}
