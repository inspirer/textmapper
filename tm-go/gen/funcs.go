package gen

import (
	"math"
	"strconv"
	"strings"
	"text/template"
)

var funcMap = template.FuncMap{
	"string_hash":      stringHash,
	"ranged_hash":      rangedHash,
	"bits_per_element": bitsPerElement,
	"int_array":        intArray,
}

func stringHash(s string) string {
	var hash uint32
	for _, r := range s {
		hash = hash*uint32(31) + uint32(r)
	}
	return "0x" + strconv.FormatUint(uint64(hash), 16)
}

func rangedHash(s string, limit uint32) uint32 {
	var hash uint32
	for _, r := range s {
		hash = hash*uint32(31) + uint32(r)
	}
	return hash % limit
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
	var buf [20]byte
	var b strings.Builder
	b.Grow(len(arr) * 10)
	var col int
	for index, val := range arr {
		str := strconv.AppendInt(buf[:0], int64(val), 10)
		col += len(str) + 2
		if index > 0 && col < maxWidth {
			b.WriteString(", ")
		} else {
			if index > 0 {
				b.WriteString(",\n")
			}
			b.WriteString(padding)
			col = len(padding) + len(str)
		}
		b.Write(str)
	}
	return b.String()
}
