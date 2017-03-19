package parsertest

import (
	"reflect"
	"testing"
)

func TestSplitInput(t *testing.T) {
	res, exp, errors := splitInput(t, "test", `abc«de§f»cdf«q1»q2§`)
	if string(res) != `abcdefcdfq1q2` {
		t.Errorf("got: %s, want: abcdefcdfq1q2", res)
	}
	want := map[node]int{{3, 6}: 1, {9, 11}: 1}
	if !reflect.DeepEqual(exp, want) {
		t.Errorf("got: %v, want: %v", exp, want)
	}
	if !reflect.DeepEqual(errors, []int{5, 13}) {
		t.Errorf("got: %v, want: [5 13]", errors)
	}

	res, exp, errors = splitInput(t, "test", `/*no expectations*/`)
	if string(res) != `/*no expectations*/` || len(exp) != 0 || len(errors) != 0 {
		t.Errorf("got: %s, %v, %v, want: /*no expectations*/, [], []", res, exp, errors)
	}

	res, exp, errors = splitInput(t, "test", `«abc» «a«b§«c»»»`)
	if string(res) != `abc abc` {
		t.Errorf("got: %s, want: abc abc", res)
	}
	want = map[node]int{{0, 3}: 1, {6, 7}: 1, {5, 7}: 1, {4, 7}: 1}
	if !reflect.DeepEqual(exp, want) {
		t.Errorf("got: %v, want: %v", exp, want)
	}
	if !reflect.DeepEqual(errors, []int{6}) {
		t.Errorf("got: %v, want: [6]", errors)
	}
}
