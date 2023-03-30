package diff

import (
	"fmt"
	"math/rand"
	"strconv"
	"strings"
	"testing"
)

var diffTests = []struct {
	a, b string
	want string
}{
	{"aa", "bb", "@@ -1,1 +1,1 @@\n-aa\n+bb\n"},
	{"x\naa", "x\nbb\nee", "@@ -1,2 +1,3 @@\n x\n-aa\n+bb\n+ee\n"},
	{"a\nb\nc\nd\ne\nX", "a\nb\nc\nd\ne\nY\n", "@@ -3,4 +3,5 @@\n c\n d\n e\n-X\n+Y\n+\n"},
	{"aa\nbb\ncc\ndd\nee\n", "aaaaa\nbb\ncc\nee\n", "@@ -1,6 +1,5 @@\n-aa\n+aaaaa\n bb\n cc\n-dd\n ee\n \n"},
	{"X\na\nb\nc\nd\ne\nX2", "Y\na\nb\nc\nd\ne\nY2\n", "@@ -1,7 +1,8 @@\n-X\n+Y\n a\n b\n c\n d\n e\n-X2\n+Y2\n+\n"},
	{"X\na\nb\nc\nd\ne\nf\ng\nX2", "Y\na\nb\nc\nd\ne\nf\ng\nY2\n", "@@ -1,4 +1,4 @@\n-X\n+Y\n a\n b\n c\n@@ -6,4 +6,5 @@\n e\n f\n g\n-X2\n+Y2\n+\n"},
	{"", "1\n2\n3\n1\n2\n3\n1\n2\n3\n1\n2\n3\n1\n2\n3\n1\n2\n3\n", "@@ -1,1 +1,20 @@\n+1\n+2\n+3\n+1\n+2\n+3\n+1\n+2\n+3\n+1\n+  ... 5 lines skipped ...\n+1\n+2\n+3\n \n"},
}

func TestLineDiff(t *testing.T) {
	for _, tc := range diffTests {
		if got := LineDiff(tc.a, tc.b); got != tc.want {
			t.Errorf("LineDiff(%v,%v) = %q, want: %q", tc.a, tc.b, got, tc.want)
		}
	}
}

var middleTests = []struct {
	a, b []int
	want string
}{
	{[]int{1}, []int{1}, "0,0,1"},
	{[]int{1, 2}, []int{7}, "1,1,0"},
	{[]int{1}, []int{7, 8}, "0,2,0"},
	{[]int{1}, []int{1, 2, 2, 1}, "0,2,0"},
	{[]int{1, 2}, []int{1, 2}, "0,0,2"},
	{[]int{1, 3}, []int{9, 5, 8}, "0,3,0"},
	{[]int{8, 1, 2}, []int{9, 1, 2, 3, 4, 5}, "3,4,0"},
	{[]int{1, 2, 3, 4, 5}, []int{1, 2}, "4,2,0"},
	{[]int{1, 2, 3}, []int{1, 2, 3, 4, 5}, "3,4,0"},
	{[]int{1, 2, 3, 4, 5}, []int{1, 2, 3}, "4,3,0"},
	{[]int{1, 2, 3}, []int{4, 2, 5}, "1,1,1"},
	{[]int{1, 2, 2, 3}, []int{4, 2, 2, 5, 8}, "3,4,0"},
	{[]int{1, 2, 3, 4, 5}, []int{7, 8, 2, 3, 4, 9, 10}, "1,2,3"},
}

func TestMiddle(t *testing.T) {
	for _, tc := range middleTests {
		buf := make([]int, 2*(len(tc.a)+len(tc.b)+2))
		ai, bi, ln := middle(tc.a, tc.b, buf)
		if got := fmt.Sprintf("%v,%v,%v", ai, bi, ln); got != tc.want {
			t.Errorf("middle(%v,%v) = %v, want: %v", tc.a, tc.b, got, tc.want)
		}
	}
}

func TestInvariants(t *testing.T) {
	iterate := func(slice []int, consume func(subslice []int)) {
		for i := 0; i < len(slice); i++ {
			for e := i + 1; e < len(slice); e++ {
				consume(slice[i:e])
			}
		}
	}

	buf := make([]int, 1024)
	check := func(a, b []int) {
		x, y, snake := middle(a, b, buf)
		if x > 0 && y > 0 && a[x-1] == b[y-1] {
			t.Errorf("middle(%v,%v) = %v,%v,%v (eq before snake)", a, b, x, y, snake)
		}
		for i := 0; i < snake; i++ {
			if a[x+i] != b[y+i] {
				t.Errorf("middle(%v,%v) = %v,%v,%v (not eq at #%v)", a, b, x, y, snake, i)
			}
		}
		if x+snake < len(a) && y+snake < len(b) && a[x+snake] == b[y+snake] {
			t.Errorf("middle(%v,%v) = %v,%v,%v (eq after snake)", a, b, x, y, snake)
		}

		var ai, bi int
		for _, c := range lcs(a, b) {
			ai += c.eq + c.del
			bi += c.eq + c.ins
		}
		if ai != len(a) || bi != len(b) {
			t.Errorf("lcs(%v,%v) produced %v deletions and %v insertions", a, b, ai, bi)
		}
	}

	arr1 := []int{1, 3, 5, 8, 4, 5, 7, 4, 6, 2, 1, 2, 1}
	arr2 := []int{1, 9, 5, 8, 4, 2, 1, 99, 1, 2, 1, 1, 2, 1}
	iterate(arr1, func(a []int) {
		iterate(arr2, func(b []int) {
			check(a, b)
		})
	})

	r := rand.New(rand.NewSource(42))
	for i := range arr1 {
		arr1[i] = r.Intn(4)
	}
	for i := range arr2 {
		arr2[i] = r.Intn(4)
	}
	iterate(arr1, func(a []int) {
		iterate(arr2, func(b []int) {
			check(a, b)
		})
	})
}

var lcsTests = []struct {
	a, b []int
	want string
}{
	{[]int{1}, []int{2}, "[1->2]"},
	{[]int{}, []int{1, 88}, "[->1 88]"},
	{[]int{1, 88}, []int{}, "[1 88->]"},
	{[]int{1, 3, 4, 8}, []int{1, 3, 4, 5, 5, 8}, "1 3 4 [->5 5] 8"},
	{[]int{1, 8}, []int{1, 8}, "1 8"},
	{[]int{1}, []int{1, 88}, "1 [->88]"},
	{[]int{1}, []int{9, 1}, "[->9] 1"},
	{[]int{1}, []int{9, 1, 2, 3}, "[->9] 1 [->2 3]"},
	{[]int{1, 88}, []int{1}, "1 [88->]"},
	{[]int{9, 1}, []int{1}, "[9->] 1"},
	{[]int{9, 1, 2, 3}, []int{1}, "[9->] 1 [2 3->]"},
	{[]int{8, 1, 2}, []int{9, 1, 2, 3, 4, 5}, "[8->9] 1 2 [->3 4 5]"},
	{[]int{1, 2, 3}, []int{4, 2, 5}, "[1->4] 2 [3->5]"},
	{[]int{1, 2, 2, 3}, []int{4, 2, 2, 5, 8}, "[1->4] 2 2 [3->5 8]"},
	{[]int{1, 2, 3, 4, 5}, []int{7, 8, 2, 3, 4, 9, 10}, "[1->7 8] 2 3 4 [5->9 10]"},
}

func TestLCS(t *testing.T) {
	for _, tc := range lcsTests {
		chunks := lcs(tc.a, tc.b)
		if got := serialize(tc.a, tc.b, chunks); got != tc.want {
			t.Errorf("lcs(%v,%v) = %v, want: %v", tc.a, tc.b, got, tc.want)
		}
	}
}

func serialize(a, b []int, chunks []chunk) string {
	var buf strings.Builder
	var ai, bi int
	write := func(list []int) {
		for i, val := range list {
			if i > 0 {
				buf.WriteByte(' ')
			}
			buf.WriteString(strconv.Itoa(val))
		}
	}
	for _, c := range chunks {
		if c.ins != 0 || c.del != 0 {
			if buf.Len() > 0 {
				buf.WriteByte(' ')
			}
			buf.WriteByte('[')
			write(a[ai : ai+c.del])
			ai += c.del
			buf.WriteString("->")
			write(b[bi : bi+c.ins])
			bi += c.ins
			buf.WriteByte(']')
		}
		if c.eq != 0 {
			if buf.Len() > 0 {
				buf.WriteByte(' ')
			}
			write(b[bi : bi+c.eq])
			ai += c.eq
			bi += c.eq
		}
	}
	return buf.String()
}
