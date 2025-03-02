package gen

import (
	"regexp"
	"sort"
	"strings"
)

var qualifierTsRE = regexp.MustCompile(`("([\w\/\.-]+)")\.(\w+)`)

// ExtractTsImports rewrites the content of a generated TypeScript file, deriving imports
// from qualified names that can appear anywhere in src, where one can reference a symbol
// from another module. The format:
//
//	"./foo".Bar
//
// will be transformed into proper TypeScript imports:
//
//	import {Bar} from "./foo"
//
// Multiple imports from the same module will be combined:
//
//	"./foo".Bar, "./foo".Baz -> import {Bar, Baz} from "./foo"
func ExtractTsImports(src string) string {
	var b strings.Builder
	byModule := make(map[string]map[string]bool) // module -> set of symbols

	// First pass: collect all imports and transform the source
	for {
		match := qualifierTsRE.FindStringSubmatchIndex(src)
		if match == nil {
			break
		}

		slice := func(n int) string {
			s := match[2*n]
			if s == -1 {
				return ""
			}
			return src[s:match[2*n+1]]
		}

		modulePath := slice(2)
		symbol := slice(3)

		// Add the symbol to the map of imports for this module
		if _, ok := byModule[modulePath]; !ok {
			byModule[modulePath] = make(map[string]bool)
		}
		byModule[modulePath][symbol] = true

		// Write everything before the match
		b.WriteString(src[:match[0]])
		// Replace the "module".Symbol with just Symbol
		b.WriteString(symbol)

		// Move past the match
		src = src[match[1]:]
	}

	// Add the remaining source
	b.WriteString(src)

	// If no imports were found, return the original source
	if len(byModule) == 0 {
		return b.String()
	}

	// Sort modules to ensure consistent output
	var modules []string
	for m := range byModule {
		modules = append(modules, m)
	}
	sort.Strings(modules)

	// Generate import statements
	var header strings.Builder
	for _, mod := range modules {
		var symbols []string
		for sym := range byModule[mod] {
			symbols = append(symbols, sym)
		}
		sort.Strings(symbols)

		header.WriteString("import {")
		for i, sym := range symbols {
			if i > 0 {
				header.WriteString(", ")
			}
			header.WriteString(sym)
		}
		header.WriteString("} from \"")
		header.WriteString(mod)
		header.WriteString("\";\n")
	}

	source := b.String()
	if header.Len() == 0 {
		return source
	}

	var insert int
	for strings.HasPrefix(source[insert:], "//") {
		if nl := strings.Index(source[insert:], "\n"); nl != -1 {
			insert += nl + 1
		} else {
			break
		}
	}
	if strings.HasPrefix(source[insert:], "\n") {
		// Skip the blank line after the top-level comments.
		insert++
	}
	if !strings.HasPrefix(source[insert:], "import ") {
		header.WriteString("\n")
	}

	// Combine the import statements with the transformed source
	return source[:insert] + header.String() + source[insert:]
}
