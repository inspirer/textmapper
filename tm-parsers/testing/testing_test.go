package testing

import (
	"reflect"
	"testing"
)

func TestSplitInput(t *testing.T) {
	res, exp, errors := splitInput("test", `abc“de§f“cdf“q1“q2§`, t)
	if string(res) != `abcdefcdfq1q2` {
		t.Errorf("got: %s, want: abcdefcdfq1q2", res)
	}
	if !reflect.DeepEqual(exp, []node{{3, 6}, {9, 11}}) {
		t.Errorf("got: %v, want: [{3 6} {9 11}]", exp)
	}
	if !reflect.DeepEqual(errors, []int{5, 13}) {
		t.Errorf("got: %v, want: [5 13]", errors)
	}

	res, exp, errors = splitInput("test", `/*no expectations*/`, t)
	if string(res) != `/*no expectations*/` || len(exp) != 0 || len(errors) != 0 {
		t.Errorf("got: %s, %v, %v, want: /*no expectations*/, [], []", res, exp, errors)
	}

	res, exp, errors = splitInput("test", `“abc“ «a«b§«c»»»`, t)
	if string(res) != `abc abc` {
		t.Errorf("got: %s, want: abc abc", res)
	}
	if !reflect.DeepEqual(exp, []node{{0, 3}, {6, 7}, {5, 7}, {4, 7}}) {
		t.Errorf("got: %v, want: [{0 3} {6 7} {5 7} {4 7}]", exp)
	}
	if !reflect.DeepEqual(errors, []int{6}) {
		t.Errorf("got: %v, want: [6]", errors)
	}
}
