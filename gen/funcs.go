package gen

import (
	"errors"
	"fmt"
	"math"
	"sort"
	"strconv"
	"strings"
	"text/template"
	"unicode"
	"unicode/utf8"

	"github.com/inspirer/textmapper/grammar"
	"github.com/inspirer/textmapper/status"
	"github.com/inspirer/textmapper/syntax"
)

var funcMap = template.FuncMap{
	"hex":                 hex,
	"bits":                bits,
	"bits_per_element":    bitsPerElement,
	"int_array":           intArray,
	"int_array_columns":   intArrayColumns,
	"str_literal":         strconv.Quote,
	"stringify":           stringify,
	"title":               strings.Title,
	"lower":               strings.ToLower,
	"first_lower":         firstLower,
	"has_prefix":          strings.HasPrefix,
	"sum":                 sum,
	"string_switch":       asStringSwitch,
	"quote":               strconv.Quote,
	"join":                strings.Join,
	"concat":              concat,
	"lexer_action":        lexerAction,
	"ref":                 ref,
	"minus1":              minus1,
	"sub":                 sub,
	"go_parser_action":    goParserAction,
	"cc_parser_action":    ccParserAction,
	"cc_action_func":      ccActionFunc,
	"bison_parser_action": bisonParserAction,
	"short_pkg":           shortPkg,
	"list":                func(vals ...string) []string { return vals },
	"set":                 set,
	"additionalTypes":     additionalTypes,
	"last_id":             lastID,
	"escape_reserved":     escapeReserved,
	"unwrap_with_default": unwrapWithDefault,
}

func stringify(s string) string {
	if len(s) >= 2 && s[0] == '"' && s[len(s)-1] == '"' {
		return strconv.Quote(s[1 : len(s)-1])
	}
	return strconv.Quote(s)
}

func sum(a, b int) int {
	return a + b
}

func firstLower(s string) string {
	r, w := utf8.DecodeRuneInString(s)
	if r != utf8.RuneError {
		return string(utf8.AppendRune(nil, unicode.ToLower(r))) + s[w:]
	}
	return s
}

func hex(val uint32) string {
	if val < 10 {
		return strconv.FormatUint(uint64(val), 10)
	}
	return "0x" + strconv.FormatUint(uint64(val), 16)
}

func bits(i int) int {
	if i < math.MinInt8 || i > math.MaxInt8 {
		if i < math.MinInt16 || i > math.MaxInt16 {
			return 32
		}
		return 16
	}
	return 8
}

func bitsPerElement(arr []int) int {
	ret := 8
	for _, i := range arr {
		if i < math.MinInt8 || i > math.MaxInt8 {
			if i < math.MinInt16 || i > math.MaxInt16 {
				return 32
			}
			ret = 16
		}
	}
	return ret
}

func intArray(arr []int, padding string, maxWidth int) string {
	var buf [21]byte
	var b strings.Builder
	b.Grow(len(arr) * 10)
	col := maxWidth
	for _, val := range arr {
		str := strconv.AppendInt(buf[:0], int64(val), 10)
		str = append(str, ',')
		col += len(str) + 1
		if col < maxWidth {
			b.WriteString(" ")
		} else {
			b.WriteByte('\n')
			b.WriteString(padding)
			col = len(padding) + len(str)
		}
		b.Write(str)
	}
	if len(arr) > 0 {
		b.WriteByte('\n')
	}
	return b.String()
}

func intArrayColumns(arr []int, padding string, cols int) string {
	var buf [21]byte
	var b strings.Builder
	b.Grow(len(arr) * 9)
	col := cols
	for _, val := range arr {
		str := strconv.AppendInt(buf[:0], int64(val), 10)
		str = append(str, ',')
		if col >= cols {
			b.WriteString("\n")
			b.WriteString(padding)
			col = 0
		}
		b.WriteByte(' ')
		if len(str) < 6 {
			b.WriteString("      "[len(str):])
		}
		b.Write(str)
		col++
	}
	if len(arr) > 0 {
		b.WriteByte('\n')
	}
	return b.String()
}

type stringSwitch struct {
	Size  uint32 // power of two
	Cases []stringHashCase
}

func (s stringSwitch) Mask() uint32 {
	return s.Size - 1
}

type stringHashCase struct {
	Value    uint32
	Subcases []stringSwitchCase
}

type stringSwitchCase struct {
	Hash   uint32
	Str    string
	Action int
}

func asStringSwitch(m map[string]int) stringSwitch {
	size := uint32(8)
	for int(size) < len(m) {
		size *= 2
	}

	var list []string
	for s := range m {
		list = append(list, s)
	}
	sort.Strings(list)

	index := make(map[uint32]int)
	ret := stringSwitch{Size: size}
	for _, str := range list {
		hash := stringHash(str)
		rng := hash % size
		i, ok := index[rng]
		if !ok {
			i = len(ret.Cases)
			index[rng] = i
			ret.Cases = append(ret.Cases, stringHashCase{Value: rng})
		}
		ret.Cases[i].Subcases = append(ret.Cases[i].Subcases, stringSwitchCase{
			Hash:   hash,
			Str:    str,
			Action: m[str],
		})
	}
	sort.Slice(ret.Cases, func(i, j int) bool {
		return ret.Cases[i].Value < ret.Cases[j].Value
	})
	return ret
}

func stringHash(s string) uint32 {
	var hash uint32
	for _, r := range s {
		hash = hash*uint32(31) + uint32(r)
	}
	return hash
}

func concat(a, b string) string {
	return a + b
}

func lexerAction(s string) string {
	return strings.Replace(s, "$$", "l.value", -1)
}

func ref(name string) string {
	return name
}

func minus1(a int) int {
	return a - 1
}

func sub(a, b int) int {
	return a - b
}

func reverseLookup(i int, remap map[int]int) int {
	for pos, idx := range remap {
		if idx == i {
			return pos
		}
	}
	return 0
}

func goParserAction(s string, args *grammar.ActionVars, origin status.SourceNode) (string, error) {
	var decls strings.Builder
	var sb strings.Builder
	seen := make(map[int]bool)
	for len(s) > 0 {
		d := strings.IndexByte(s, '$')
		if d == -1 {
			sb.WriteString(s)
			break
		}
		sb.WriteString(s[:d])
		s = s[d+1:]
		if len(s) == 0 {
			return "", status.Errorf(origin, "found $ at the end of the stream")
		}

		size, id, prop, err := parseMeta(s)
		s = s[size:]
		if err != nil {
			return "", status.Errorf(origin, "%s", err.Error())
		}

		var index int
		var pos int
		switch id {
		case "left()", "leftRaw()":
			index = -2
		case "first()":
			if args.SymRefCount == 0 {
				index = -1
			}
		case "last()":
			if args.SymRefCount == 0 {
				index = -1
			} else {
				index = args.SymRefCount - 1
			}
		default:
			if strings.HasPrefix(id, "self[") && strings.HasSuffix(id, "]") {
				id = id[5 : len(id)-1]
				if _, err := strconv.ParseUint(id, 10, 32); err != nil {
					return "", status.Errorf(origin, "invalid self reference %q", id)
				}
			}

			ref, err := args.Resolve(id, origin)
			if err != nil {
				return "", err
			}
			index = ref.Index
			pos = ref.Pos
		}

		// We are trying to locate the first or last symbol from RHS.
		if pos == 0 && index >= 0 {
			pos = reverseLookup(index, args.Remap)
			if pos == 0 {
				return "", status.Errorf(origin, "internal error: cannot find the position for index %v", index)
			}
		}

		if index == -1 {
			if prop == "value" || prop == "sym" {
				sb.WriteString("nil")
			} else {
				sb.WriteString("-1")
			}
			continue
		}
		var v string
		if index == -2 {
			v = "lhs"
		} else {
			v = fmt.Sprintf("stack[len(stack)-%v]", args.SymRefCount-index)
		}
		switch {
		case prop == "sym":
			fmt.Fprintf(&sb, "(&%v.sym)", v)
		case prop == "value":
			v += ".value"
			switch {
			case index >= 0 && args.Types[pos] != "":
				varName := fmt.Sprintf("nn%v", index)
				if !seen[index] {
					fmt.Fprintf(&decls, "%v, _ := %v.(%v)\n", varName, v, args.Types[pos])
					seen[index] = true
				}
				v = varName
			case index == -2 && args.LHSType != "" && id != "leftRaw()":
				if !seen[index] {
					fmt.Fprintf(&decls, "nn, _ := %v.(%v)\n", v, args.LHSType)
					seen[index] = true
				}
				v = "nn"
			}
			sb.WriteString(v)
		default:
			sb.WriteString(v)
			sb.WriteString(".sym.")
			sb.WriteString(prop)
		}
	}
	return decls.String() + sb.String(), nil
}

func ccWrapInOptional(argType, input string) string {
	return fmt.Sprintf("std::optional<%v>(%v)", argType, input)
}

// ccTypeFromUnion returns the type of the union field, without the last ID, e.g. the "int" in
// "int x".
func ccTypeFromUnion(unionField string) string {
	return strings.TrimSpace(unionField[:len(unionField)-len(lastID(unionField))])
}

func ccParserAction(s string, args *grammar.ActionVars, origin status.SourceNode, opts *grammar.Options) (ret string, err error) {
	defer func(s string) {
		if r := recover(); r != nil {
			err = fmt.Errorf("crashed with %v in %q with %v", err, s, args)
		}
	}(s)

	var sb strings.Builder
	sb.WriteString("#line " + strconv.Itoa(origin.SourceRange().Line) + " \"" + origin.SourceRange().Filename + "\"\n")
	for len(s) > 0 {
		next := strings.IndexAny(s, "@$")
		if next == -1 {
			sb.WriteString(s)
			break
		}

		ch := s[next]
		sb.WriteString(s[:next])
		s = s[next+1:]
		if len(s) == 0 {
			return "", status.Errorf(origin, "found $ or @ at the end of the semantic action")
		}

		// Handle the rest of this '$' or '@'

		// $$ --> lhs.value
		if s[0] == '$' {
			var replacement string
			if ch == '@' {
				replacement = "lhs.sym.location"
			} else {
				t := args.LHSType
				if t == "" {
					return "", status.Errorf(origin, "$$ cannot be used inside a nonterminal semantic action without a type")
				}
				if opts.VariantStackEntry {
					replacement = "std::get<" + t + ">(lhs.value)"
				} else {
					replacement = "lhs.value." + lastID(t)
				}
			}
			s = s[1:]
			sb.WriteString(replacement)
			continue
		}

		// RHS symbol references, e.g. $1, @a.
		var d int
		r, w := utf8.DecodeRuneInString(s)
		for unicode.IsDigit(r) || unicode.IsLetter(r) || r == '_' {
			d += w
			r, w = utf8.DecodeRuneInString(s[d:])
		}
		if d == 0 {
			return "", status.Errorf(origin, "%c should be followed by a number or identifier", ch)
		}
		val := s[:d]
		s = s[d:]

		// cc uses 1-based indexing.
		ref, err := args.ResolveOneBased(val, opts.MaxRuleSizeForOrdinalRef, string(ch)+val, origin)
		if err != nil {
			return "", err
		}

		index := ref.Index
		pos := ref.Pos

		argType := args.Types[pos]

		// The symbol reference is valid in the original rule but is not present in the expanded
		// rule, so it references an optional symbol either expanded from a Choice or an Optional.
		if index == -1 {
			// Use std::optional<T>() as the semantic value for the non-present symbol.
			if ch == '@' {
				sb.WriteString(ccWrapInOptional("decltype(lhs.sym.location)", ""))
				continue
			}

			if argType == "" {
				return "", status.Errorf(origin, "symbol %c%q does not have an associated type", ch, val)
			}
			if !opts.VariantStackEntry {
				argType = ccTypeFromUnion(argType)
			}
			sb.WriteString(ccWrapInOptional(argType, ""))
			continue
		}

		// The referenced symbol is present in the expanded rule.
		var replacement string
		target := fmt.Sprintf("rhs[%v]", index+args.Delta)
		if ch == '@' {
			replacement = target + ".sym.location"
			argType = "decltype(lhs.sym.location)"
		} else {
			if argType == "" {
				return "", status.Errorf(origin, "%c%q does not have an associated type", ch, val)
			}
			if opts.VariantStackEntry {
				replacement = "std::get<" + argType + ">(" + target + ".value)"
			} else {
				replacement = target + ".value." + lastID(argType)
				argType = ccTypeFromUnion(argType)
			}
		}

		if argRef := args.ArgRefs[pos]; argRef.Optional {
			// This symbol reference is optional in the original rule, so we wrap it inside a
			// std::optional to unify the semantic actions for the expanded rules.
			replacement = ccWrapInOptional(argType, replacement)
		}
		sb.WriteString(replacement)
	}
	return sb.String(), nil
}

// Generate a C++ function name for a reduce action that encodes information useful for
// understanding stack traces including the nonterminal name and line number. We also include the
// rule index because there can be multiple actions on the same line in the grammar file.
func ccActionFunc(idx int, act *grammar.SemanticAction) string {
	// Include the index in the name to differentiate between actions of extracted non-terminals and
	// also actions of named non-terminals that happen to be on the same line of the grammar file.
	return fmt.Sprintf("Action%v__ReduceOf_%v__AtLine_%v_Column_%v", idx, act.NtName, act.Origin.SourceRange().Line, act.Origin.SourceRange().Column)
}

func bisonParserAction(s string, args *grammar.ActionVars, origin status.SourceNode) (string, error) {
	var sb strings.Builder
	for len(s) > 0 {
		d := strings.IndexByte(s, '$')
		if d == -1 {
			sb.WriteString(s)
			break
		}
		sb.WriteString(s[:d])
		s = s[d+1:]
		if len(s) == 0 {
			return "", status.Errorf(origin, "found $ at the end of the stream")
		}

		size, id, prop, err := parseMeta(s)
		s = s[size:]
		if err != nil {
			return "", status.Errorf(origin, "%s", err.Error())
		}

		var index int
		var pos int
		switch id {
		case "left()", "leftRaw()":
			index = -2
		case "first()":
			if args.SymRefCount == 0 {
				index = -1
			}
		case "last()":
			if args.SymRefCount == 0 {
				index = -1
			} else {
				index = args.SymRefCount - 1
			}
		default:
			if strings.HasPrefix(id, "self[") && strings.HasSuffix(id, "]") {
				id = id[5 : len(id)-1]
				if _, err := strconv.ParseUint(id, 10, 32); err != nil {
					return "", status.Errorf(origin, "invalid self reference %q", id)
				}
			}

			ref, err := args.Resolve(id, origin)
			if err != nil {
				return "", err
			}
			index = ref.Index
			pos = ref.Pos
		}

		if pos == 0 && index >= 0 {
			pos = reverseLookup(index, args.Remap)
			if pos == 0 {
				return "", status.Errorf(origin, "internal error: cannot find the position for index %v", index)
			}
		}

		if index == -1 {
			if prop == "value" || prop == "sym" {
				sb.WriteString("NULL")
			} else {
				sb.WriteString("-1")
			}
			continue
		}
		var needsParen bool
		if prop == "value" {
			switch {
			case index < 0 && args.LHSType != "" && id != "leftRaw()":
				needsParen = true
				fmt.Fprintf(&sb, "(/*%v*/", args.LHSType)
			case index >= 0 && args.Types[pos] != "":
				needsParen = true
				fmt.Fprintf(&sb, "(/*%v*/", args.Types[pos])
			}
			sb.WriteByte('$')
		} else {
			sb.WriteByte('@')
		}
		if index == -2 {
			sb.WriteByte('$')
		} else {
			sb.WriteString(strconv.Itoa(index))
		}
		if prop != "value" {
			sb.WriteByte('.')
			sb.WriteString(prop)
		}
		if needsParen {
			sb.WriteByte(')')
		}
	}
	return sb.String(), nil
}

// parseMeta parses a meta expression after the dollar sign and returns its length.
// The "prop" value is validated upon successful return.
func parseMeta(s string) (d int, id, prop string, err error) {
	switch {
	case s[0] >= '0' && s[0] <= '9':
		d = 1
		for d < len(s) && '0' <= s[d] && s[d] <= '9' {
			d++
		}
		return d, s[:d], "value", nil
	case s[0] >= 'a' && s[0] <= 'z' || s[0] >= 'A' && s[0] <= 'Z' || s[0] == '_':
		d = 1
		for d < len(s) && ('0' <= s[d] && s[d] <= '9' || s[d] >= 'a' && s[d] <= 'z' || s[d] >= 'A' && s[d] <= 'Z' || s[d] == '_' || s[d] == '-') {
			d++
		}
		for s[d-1] == '-' {
			d--
		}
		return d, s[:d], "value", nil
	case s[0] == '{':
		d = strings.IndexByte(s, '}') + 1
		if d == 0 {
			return 0, "", "", errors.New("cannot find the matching }")
		}
		var ok bool
		id, prop, ok = strings.Cut(s[1:d-1], ".")
		if !ok {
			prop = "value"
		}
		switch prop {
		case "value", "sym", "offset", "endoffset":
		default:
			return 0, "", "", fmt.Errorf("unrecognized property %q", prop)
		}
		return d, id, prop, nil
	case s[0] == '$':
		return 1, "leftRaw()", "value", nil
	}
	return 0, "", "", errors.New("unrecognized sequence after $")
}

func shortPkg(packageName string) string {
	if i := strings.LastIndexByte(packageName, '/'); i != -1 {
		return packageName[i+1:]
	}
	return packageName
}

func set(vals ...string) map[string]bool {
	ret := make(map[string]bool)
	for _, val := range vals {
		for _, s := range strings.Fields(val) {
			ret[s] = true
		}
	}
	return ret
}

// additionalTypes takes a list of additional node types, such as whitespace, punctuation, etc, and returns
// those of them that have not been deduced from the AST.
func additionalTypes(t *syntax.Types, names []string) []string {
	m := make(map[string]bool)
	for _, cat := range t.Categories {
		m[cat.Name] = true
	}
	for _, t := range t.RangeTypes {
		m[t.Name] = true
	}
	var ret []string
	for _, name := range names {
		if !m[name] {
			ret = append(ret, name)
		}
	}
	return ret
}

func lastID(s string) string {
	s = strings.TrimSpace(s)
	start := len(s)
	for {
		r, d := utf8.DecodeLastRuneInString(s[:start])
		if !unicode.IsLetter(r) && !unicode.IsDigit(r) && r != '_' {
			break
		}
		start -= d
	}
	return s[start:]
}

var reserved = asSet(
	// Go reserved keywords.
	"break", "default", "func", "interface", "select",
	"case", "defer", "go", "map", "struct",
	"chan", "else", "goto", "package", "switch",
	"const", "fallthrough", "if", "range", "type",
	"continue", "for", "import", "return", "var",

	// Go builtins.
	"bool", "string", "byte", "int", "rune",
	"iota", "nil", "true", "false",
	"fmt", "strconv",

	// Textmapper-reserved
	"Token", "Nonterminal", "Pos", "Node", "Offset", "Endoffset", "Start", "End",
)

func asSet(list ...string) map[string]bool {
	ret := make(map[string]bool)
	for _, id := range list {
		ret[id] = true
	}
	return ret
}

func escapeReserved(s string) string {
	if reserved[s] {
		return "_" + s
	}
	return s
}

func unwrapWithDefault(s []string, def string) string {
	if len(s) == 1 {
		return s[0]
	}
	return def
}
