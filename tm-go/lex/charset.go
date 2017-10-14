package lex

import (
	"bytes"
	"fmt"
	"sort"
	"unicode"
	"unicode/utf8"
)

// charset is a sorted list of non-overlapping ranges.
type charset []rune

// newCharset creates a new character set from an unsorted list of ranges.
func newCharset(r []rune) charset {
	sort.Sort(rangeOrder{r})

	if len(r) < 2 {
		return nil
	}

	l := 2
	for i := 2; i < len(r); i += 2 {
		lo, hi := r[i], r[i+1]
		if end := r[l-1] + 1; lo <= end {
			if hi >= end {
				r[l-1] = hi
			}
			continue
		}
		r[l] = lo
		r[l+1] = hi
		l += 2
	}
	return r[:l]
}

// invert inverts a character set
func (c *charset) invert() {
	r := *c
	out := r[:0]
	var next rune
	for i := 0; i < len(r); i += 2 {
		lo, hi := r[i], r[i+1]
		if next <= lo-1 {
			out = append(out, next, lo-1)
		}
		next = hi + 1
	}
	if next <= unicode.MaxRune {
		out = append(out, next, unicode.MaxRune)
	}
	*c = out
}

// subtract removes all elements found in the other set
func (c *charset) subtract(oth charset) {
	r := *c
	out := r[:0]
	var allocated bool
mainLoop:
	for i := 0; i < len(r); i += 2 {
		lo, hi := r[i], r[i+1]
		for len(oth) > 0 && hi >= oth[0] {
			if oth[1] < lo {
				oth = oth[2:]
				continue
			}
			if lo < oth[0] {
				out = append(out, lo, oth[0]-1)
			}
			lo = oth[1] + 1
			if lo > hi {
				continue mainLoop
			}
			oth = oth[2:]
			if !allocated && len(out) > i {
				allocated = true
				out = append(charset(nil), out...)
			}
		}
		out = append(out, lo, hi)
	}
	*c = out
}

func (c charset) String() string {
	var b bytes.Buffer
	for i := 0; i < len(c); i += 2 {
		lo, hi := c[i], c[i+1]
		writeEscaped(&b, lo)
		if lo != hi {
			b.WriteRune('-')
			writeEscaped(&b, hi)
		}
	}
	return b.String()
}

func writeEscaped(b *bytes.Buffer, r rune) {
	switch {
	case r < ' ':
		switch r {
		case '\n':
			b.WriteString(`\n`)
		case '\r':
			b.WriteString(`\r`)
		case '\t':
			b.WriteString(`\t`)
		default:
			fmt.Fprintf(b, `\x%02x`, r)
		}
	case r == '\\' || r == '-':
		b.WriteRune('\\')
		b.WriteRune(r)
	case r < 0x80:
		b.WriteRune(r)
	case r > utf8.MaxRune:
		r = 0xFFFD
		fallthrough
	case r < 0x10000:
		fmt.Fprintf(b, `\u%04x`, r)
	default:
		fmt.Fprintf(b, `\U%08x`, r)
	}
}

func intersect(a, b charset) charset {
	var i, e int
	var out charset
	for i < len(a) && e < len(b) {
		switch {
		case a[i+1] < b[e]:
			i += 2
		case b[e+1] < a[i]:
			e += 2
		default:
			lo, hi := a[i], a[i+1]
			if b[e] > lo {
				lo = b[e]
			}
			if b[e+1] < hi {
				hi = b[e+1]
				e += 2
			} else {
				i += 2
			}
			if lo <= hi {
				out = append(out, lo, hi)
			}
		}
	}
	return out
}

func appendRange(r []rune, lo, hi rune) []rune {
	l := len(r)
	if l < 2 {
		return append(r, lo, hi)
	}
	if start, end := r[l-2], r[l-1]; lo <= end+1 && start <= hi+1 {
		// Overlaps with the previous range - merge them.
		if hi > end {
			r[l-1] = hi
		}
		if lo < start {
			r[l-2] = lo
		}
		return r
	}
	return append(r, lo, hi)
}

func appendTable(r []rune, x *unicode.RangeTable) []rune {
	for _, xr := range x.R16 {
		lo, hi, stride := rune(xr.Lo), rune(xr.Hi), rune(xr.Stride)
		if stride == 1 {
			r = appendRange(r, lo, hi)
			continue
		}
		for c := lo; c <= hi; c += stride {
			r = appendRange(r, c, c)
		}
	}
	for _, xr := range x.R32 {
		lo, hi, stride := rune(xr.Lo), rune(xr.Hi), rune(xr.Stride)
		if stride == 1 {
			r = appendRange(r, lo, hi)
			continue
		}
		for c := lo; c <= hi; c += stride {
			r = appendRange(r, c, c)
		}
	}
	return r
}

// ranges implements sort.Interface and provides a natural order for a list of closed intervals.
type rangeOrder struct {
	r []rune
}

func (ro rangeOrder) Less(i, j int) bool {
	r := ro.r
	i, j = i*2, j*2
	return r[i] < r[j] || r[i] == r[j] && r[i+1] > r[j+1]
}

func (o rangeOrder) Len() int {
	return len(o.r) / 2
}

func (o rangeOrder) Swap(i, j int) {
	r := o.r
	i, j = i*2, j*2
	r[i], r[i+1], r[j], r[j+1] = r[j], r[j+1], r[i], r[i+1]
}
