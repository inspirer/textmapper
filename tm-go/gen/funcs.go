package gen

import (
	"errors"
	"fmt"
	"math"
	"sort"
	"strconv"
	"strings"
	"text/template"

	"github.com/inspirer/textmapper/tm-go/grammar"
	"github.com/inspirer/textmapper/tm-go/status"
)

var funcMap = template.FuncMap{
	"hex":              hex,
	"bits":             bits,
	"bits_per_element": bitsPerElement,
	"int_array":        intArray,
	"str_literal":      strconv.Quote,
	"title":            strings.Title,
	"lower":            strings.ToLower,
	"sum":              sum,
	"string_switch":    asStringSwitch,
	"quote":            strconv.Quote,
	"join":             strings.Join,
	"lexer_action":     lexerAction,
	"ref":              ref,
	"minus1":           minus1,
	"go_parser_action": goParserAction,
}

func sum(a, b int) int {
	return a + b
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

func lexerAction(s string) string {
	return strings.Replace(s, "$$", "l.value", -1)
}

func ref(name string) string {
	return name
}

func minus1(a int) int {
	return a - 1
}

func goParserAction(s string, args *grammar.ActionVars, origin status.SourceNode) (string, error) {
	var decls strings.Builder
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
			return "", status.Errorf(origin, err.Error())
		}

		var index int
		switch id {
		case "left()", "leftRaw()":
			index = -2
		case "first()":
			if len(args.Types) == 0 {
				index = -1
			}
		case "last()":
			if len(args.Types) == 0 {
				index = -1
			} else {
				index = len(args.Types) - 1
			}
		default:
			var ok bool
			index, ok = args.Resolve(id)
			if !ok {
				return "", status.Errorf(origin, "invalid reference %q", id)
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
			v = fmt.Sprintf("rhs[%v]", index)
		}
		switch {
		case prop == "sym":
			fmt.Fprintf(&sb, "(&%v.sym)", v)
		case prop == "value":
			v += ".value"
			switch {
			case index >= 0 && args.Types[index] != "":
				varName := fmt.Sprintf("nn%v", index)
				fmt.Fprintf(&decls, "%v, _ := %v.(%v)\n", varName, v, args.Types[index])
				v = varName
			case index == -2 && args.LHSType != "" && id != "leftRaw()":
				fmt.Fprintf(&decls, "nn, _ := %v.(%v)\n", v, args.LHSType)
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
