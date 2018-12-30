package diff

import (
	"fmt"
	"io"
	"log"
	"strings"
)

// Diff returns a human-readable unified diff of two multiline strings.
// This function is supposed to be used in tests, so it does not pretend or needs to be
// a high quality diffing function.
func LineDiff(left, right string) string {
	if left == right {
		// No difference.
		return ""
	}
	var lines []string
	index := make(map[string]int)
	split := func(s string) []int {
		var ret []int
		for _, s := range strings.Split(s, "\n") {
			id, ok := index[s]
			if !ok {
				id = len(lines)
				lines = append(lines, s)
				index[s] = id
			}
			ret = append(ret, id)
		}
		return ret
	}
	a, b := split(left), split(right)

	// Compute the edit script.
	chunks := lcs(a, b)

	// Produce a unified diff with 3 lines of context.
	var buf strings.Builder
	var ai, bi int
	h := hunk{leftLine: 1, rightLine: 1}
	add := func(c byte, list []int) {
		var strs []string
		for _, el := range list {
			strs = append(strs, lines[el])
		}
		h.add(c, strs...)
	}
	for i, c := range chunks {
		add('-', a[ai:ai+c.del])
		add('+', b[bi:bi+c.ins])
		ai += c.eq + c.del
		bi += c.eq + c.ins
		switch {
		case i == 0 && c.del == 0 && c.ins == 0 && c.eq > 3:
			h.leftLine = c.eq - 2
			h.rightLine = h.leftLine
			add(' ', a[ai-3:ai])
		case c.eq > 6:
			add(' ', a[ai-c.eq:ai-c.eq+3])
			h.writeTo(&buf)
			h = hunk{leftLine: ai - 2, rightLine: bi - 2}
			add(' ', a[ai-3:ai])
		case i+1 == len(chunks):
			max := ai - c.eq + 3
			if max > len(a) {
				max = len(a)
			}
			add(' ', a[ai-c.eq:max])
			h.writeTo(&buf)
		default:
			add(' ', a[ai-c.eq:ai])
		}
	}
	return buf.String()
}

type chunk struct {
	del, ins, eq int
}

func (c *chunk) merge(oth chunk) bool {
	if c.eq == 0 || oth.ins == 0 && oth.del == 0 {
		c.del += oth.del
		c.ins += oth.ins
		c.eq += oth.eq
		return true
	}
	return false
}

// See https://en.wikipedia.org/wiki/Diff#Unified_format
type hunk struct {
	leftLine  int
	rightLine int
	leftSize  int
	rightSize int
	intro     []byte // '+', '-', or ' '
	lines     []string
}

func (h *hunk) add(c byte, lines ...string) {
	if len(lines) > 14 {
		skipped := len(lines) - 13
		h.add(c, lines[:10]...)
		h.add(c, fmt.Sprintf("  ... %v lines skipped ...", skipped))
		h.add(c, lines[len(lines)-3:]...)
		if c != '+' {
			h.leftSize += skipped
		}
		if c != '-' {
			h.rightSize += skipped
		}
		return
	}
	if c != '+' {
		h.leftSize += len(lines)
	}
	if c != '-' {
		h.rightSize += len(lines)
	}
	for _, line := range lines {
		h.intro = append(h.intro, c)
		h.lines = append(h.lines, line)
	}
}

func (h *hunk) writeTo(w io.Writer) {
	if h.leftSize == 0 && h.rightSize == 0 {
		// Empty.
		return
	}
	_, _ = fmt.Fprintf(w, "@@ -%v,%v +%v,%v @@\n", h.leftLine, h.leftSize, h.rightLine, h.rightSize)
	for i, c := range h.intro {
		_, _ = fmt.Fprintf(w, "%c%v\n", c, h.lines[i])
	}
}

func lcs(a, b []int) []chunk {
	// Find the common prefix and suffix.
	ln := len(a)
	if len(b) < ln {
		ln = len(b)
	}
	var p, s int
	for p < ln && a[p] == b[p] {
		p++
	}
	ln -= p
	for s < ln && a[len(a)-s-1] == b[len(b)-s-1] {
		s++
	}
	a, b = a[p:len(a)-s], b[p:len(b)-s]

	// Use the linear space refinement of the Myers' algorithm (divide-and-conquer around the
	// middle snake).
	buf := make([]int, 2*(len(a)+len(b)+2))
	var chunks []chunk
	if p > 0 {
		chunks = append(chunks, chunk{eq: p})
	}
	chunks = trace(a, b, buf, chunks)
	if s > 0 {
		chunks = append(chunks, chunk{eq: s})
	}

	// Optimize chunks away.
	ret := chunks[:0]
	for _, c := range chunks {
		if last := len(ret) - 1; last >= 0 && ret[last].merge(c) {
			continue
		}
		ret = append(ret, c)
	}
	return ret
}

// trace is a recursive helper function that finds the smallest edit script.
// It accepts a and b that don't have a common prefix or suffix.
func trace(a, b, buf []int, chunks []chunk) []chunk {
	switch {
	case len(a) == 0:
		return append(chunks, chunk{ins: len(b)})
	case len(b) == 0:
		return append(chunks, chunk{del: len(a)})
	case len(a) == 1:
		for i, v := range b {
			if v == a[0] {
				return append(chunks, chunk{ins: i, eq: 1}, chunk{ins: len(b) - i - 1})
			}
		}
		return append(chunks, chunk{del: len(a), ins: len(b)})
	case len(b) == 1:
		for i, v := range a {
			if v == b[0] {
				return append(chunks, chunk{del: i, eq: 1}, chunk{del: len(a) - i - 1})
			}
		}
		return append(chunks, chunk{del: len(a), ins: len(b)})
	}
	ai, bi, snake := middle(a, b, buf)
	if ai == len(a) && bi == len(b) || ai == 0 && bi == 0 {
		log.Fatalf("middle(%v,%v) no snake", a, b)
	}
	ret := trace(a[:ai], b[:bi], buf, chunks)
	if snake > 0 {
		ret = append(ret, chunk{eq: snake})
	}
	return trace(a[ai+snake:], b[bi+snake:], buf, ret)
}

// middle finds the middle (possibly empty) snake of a diff between a and b (min length 2 for both).
// See "An O(ND) Difference Algorithm and Its Variations" by EUGENE W. MYERS.
// "buf" is used as a temporary storage and is expected to be at least 2*(len(a)+len(b)+2) big.
func middle(a, b, buf []int) (ai, bi, ln int) {
	m, n := len(a), len(b)
	delta := n - m
	odd := delta%2 != 0
	max := (m + n + 2) / 2
	base := max

	v1 := buf[:2*max]
	v2 := buf[2*max:]
	v1[base+1] = 0
	v2[base+1] = 0

	var pstart, plimit int
	for d := 0; d <= max; d++ {
		start, limit := -d, d
		if d > m {
			limit -= 2 * (d - m)
		}
		if d > n {
			start += 2 * (d - n)
		}

		// Forward path.
		for k := start; k <= limit; k += 2 {
			var x int
			if k == -d || k != d && v1[base+k-1] < v1[base+k+1] {
				x = v1[base+k+1]
			} else {
				x = v1[base+k-1] + 1
			}
			y := x - k
			for x < m && y < n && a[x] == b[y] {
				x++
				y++
			}
			v1[base+k] = x
			if odd {
				if k2 := -delta - k; k2 >= pstart && k2 <= plimit {
					x2 := m - v2[base+k2]
					if x >= x2 {
						// Note: x-x2 is the maximum length of the snake but not all of it is necessarily
						// a snake.
						var snake int
						for snake < x-x2 && a[x2+snake] == b[x2-k+snake] {
							snake++
						}
						return x2, x2 - k, snake
					}
				}
			}
		}

		// Reverse path.
		for k := start; k <= limit; k += 2 {
			var x int
			if k == -d || k != d && v2[base+k-1] < v2[base+k+1] {
				x = v2[base+k+1]
			} else {
				x = v2[base+k-1] + 1
			}
			y := x - k
			for x < m && y < n && a[m-x-1] == b[n-y-1] {
				x++
				y++
			}
			v2[base+k] = x
			if !odd {
				if k1 := -delta - k; k1 >= start && k1 <= limit {
					x1 := v1[base+k1]
					if x2 := m - x; x1 >= x2 {
						// Note: x1-x2 is the maximum length of the snake but not all of it is necessarily
						// a snake.
						var snake int
						for snake < x1-x2 && a[x2+snake] == b[n-y+snake] {
							snake++
						}
						return x2, n - y, snake
					}
				}
			}
		}
		pstart, plimit = start, limit
	}
	log.Fatal("no snake")
	return
}
