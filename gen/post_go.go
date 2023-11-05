package gen

import (
	"bytes"
	"fmt"
	"go/format"
	"go/parser"
	"go/token"
	"regexp"
	"sort"
	"strings"
)

// FormatGo formats the content of a generated file. Formats Go files only.
func FormatGo(filename, content string) string {
	if !strings.HasSuffix(filename, ".go") || len(content) > 6e6 {
		// Note: formatting is quite slow on large files, leave them unformatted.
		return content
	}
	fset := token.NewFileSet()
	file, err := parser.ParseFile(fset, filename, content, parser.ParseComments)
	if err != nil {
		return fmt.Sprintf("// go fmt failed with: %v\n%s", err, content)
	}

	var buf bytes.Buffer
	err = format.Node(&buf, fset, file)
	if err != nil {
		return fmt.Sprintf("// go fmt failed with: %v\n%s", err, content)
	}
	return buf.String()
}

var qualifierRE = regexp.MustCompile(`("((?:[\w-\.]+/)*([\w-]+))(?:\s*as\s*(\w+))?")\.\w+`)
var packageRE = regexp.MustCompile(`(?m)^package\s*\w+`)

// ExtractGoImports rewrites the content of a generated Go file, deriving imports from "special"
// qualified names that can appear anywhere in src, where one can reference a symbol from another
// package. The format:
//
//	"fmt".Stringer
//	"unicode/utf8".ValidString         (multi-segment package)
//	"encoding/json as enc".Marshal     (with an optional "as" clause for explicit aliases)
func ExtractGoImports(src string) string {
	var buf strings.Builder
	type imp struct {
		alias string
		path  string
	}
	toInsert := make(map[string]imp)
	for {
		match := qualifierRE.FindStringSubmatchIndex(src)
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
		path, lastSeg, alias := slice(2), slice(3), slice(4)
		if alias == "" {
			alias = lastSeg
		}
		toInsert[path] = imp{alias, path}
		buf.WriteString(src[:match[2]])
		src = src[match[3]:]
		buf.WriteString(alias)
	}
	buf.WriteString(src)

	if len(toInsert) == 0 {
		return buf.String()
	}

	var list []imp
	for _, v := range toInsert {
		list = append(list, v)
	}
	sort.Slice(list, func(i, j int) bool {
		if si, sj := isStdPackage(list[i].path), isStdPackage(list[j].path); si != sj {
			return si
		}
		return list[i].path < list[j].path
	})
	src = buf.String()

	var insertOffset int
	if loc := packageRE.FindStringIndex(src); loc != nil {
		insertOffset = loc[1]
	}

	buf.Reset()
	if insertOffset > 0 {
		buf.WriteString("\n\n")
	}
	buf.WriteString("import (\n")
	var lastStd bool
	for _, imp := range list {
		// Separate standard packages from application packages.
		std := isStdPackage(imp.path)
		if !std && lastStd {
			buf.WriteByte('\n')
		}
		lastStd = std

		buf.WriteByte('\t')
		if imp.path != imp.alias && !strings.HasSuffix(strings.TrimSuffix(imp.path, imp.alias), "/") {
			buf.WriteString(imp.alias)
			buf.WriteByte(' ')
		}
		fmt.Fprintf(&buf, "\"%v\"\n", imp.path)
	}
	buf.WriteString(")")
	if insertOffset == 0 {
		buf.WriteString("\n\n")
	}
	imports := buf.String()

	return src[:insertOffset] + imports + src[insertOffset:]
}

func isStdPackage(path string) bool {
	first, _, _ := strings.Cut(path, "/")
	return !strings.ContainsAny(first, ".-_0123456789")
}
