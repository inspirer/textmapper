package gen

import (
	"math"
	"sort"
	"strconv"
	"strings"
	"text/template"
)

var funcMap = template.FuncMap{
	"hex":              hex,
	"bits":             bits,
	"bits_per_element": bitsPerElement,
	"int_array":        intArray,
	"str_literal":      strconv.Quote,
	"title":            strings.Title,
	"sum":              sum,
	"string_switch":    asStringSwitch,
	"quote":            strconv.Quote,
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
