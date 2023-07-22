package main

import (
	"bytes"
	"context"
	"flag"
	"fmt"
	"log"
	"os"
	"text/template"

	"github.com/inspirer/textmapper/status"
)

const (
	version       = `0.10.1`
	usageTemplate = `Textmapper is an LALR parser and lexer generator.

Usage:
    textmapper [command] [flags] [arguments]

Commands:
{{- range .}}{{if ne .Name "help"}}
    {{.Name | printf "%-10s"}} {{.Title}}{{end}}{{end}}

Use "textmapper help [command]" for more information about a command.

`
)

var commands = []*command{
	genCmd,
	debugCmd,
	versionCmd,
	helpCmd,
}

func main() {
	flag.Usage = usage
	flag.Parse()

	ctx := context.Background()
	args := flag.Args()
	if len(args) == 0 {
		flag.Usage()
		os.Exit(2)
	}

	cmd := findCommand(args[0])
	if cmd == nil {
		fmt.Fprintf(os.Stderr, "textmapper: unknown command %q\n", args[0])
		fmt.Fprint(os.Stderr, "Run 'textmapper help' for usage.\n")
		os.Exit(2)
	}

	cmd.Flags.Usage = func() {
		cmd.usage()
		os.Exit(2)
	}
	cmd.Flags.Parse(args[1:])
	args = cmd.Flags.Args()
	err := cmd.Run(ctx, args)
	if err != nil {
		status.Print(os.Stderr, err)
		os.Exit(2)
	}
}

func usage() {
	var buf bytes.Buffer
	t := template.Must(template.New("main").Parse(usageTemplate))
	if err := t.Execute(&buf, commands); err != nil {
		log.Fatal(err)
	}

	fmt.Fprint(os.Stderr, buf.String())
}

var versionCmd = &command{
	Name:  "version",
	Title: "print the Textmapper version",
	Help:  `This command prints the Textmapper version.`,
	Run: func(ctx context.Context, args []string) error {
		fmt.Fprintf(os.Stderr, "textmapper ver %v\n", version)
		return nil
	},
}

var helpCmd = &command{
	Name:  "help",
	Title: "generate grammars",
	Usage: " [command]",
	Help:  `Outputs command-specific flags and usage help.`,
}

func init() {
	helpCmd.Run = help
}

func help(ctx context.Context, args []string) error {
	if len(args) == 0 {
		flag.Usage()
		return nil
	}

	cmd := findCommand(args[0])
	if cmd == nil {
		return fmt.Errorf("unknown command %q. Run 'textmapper help'", args[0])
	}

	cmd.usage()
	return nil
}

type command struct {
	Name  string
	Title string
	Usage string
	Help  string
	Run   func(ctx context.Context, args []string) error
	Flags flag.FlagSet
}

func (c *command) usage() {
	fmt.Fprintf(os.Stderr, "Usage:\n    textmapper %s%s\n\n%s\n", c.Name, c.Usage, c.Help)
	c.Flags.PrintDefaults()
}

func findCommand(name string) *command {
	for _, cmd := range commands {
		if cmd.Name == name {
			return cmd
		}
	}
	return nil
}
