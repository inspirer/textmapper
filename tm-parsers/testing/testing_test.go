package testing

import (
	"testing"
	"reflect"
)

func TestSplitInput(t *testing.T) {
	res, exp := splitInput("test", `abc“def“cdf“q1“q2`, t)
	if string(res) != `abcdefcdfq1q2` {
		t.Errorf("Unexpected result: %s", res)
	}
	if !reflect.DeepEqual(exp, []node{{3, 6}, {9, 11}}) {
		t.Errorf("Unexpected expectations: %v", exp)
	}

	res, exp = splitInput("test", `/*no expectations*/`, t)
	if string(res) != `/*no expectations*/` || len(exp) != 0 {
		t.Errorf("splitInput(``) is broken: %v", res)
	}

	res, exp = splitInput("test", `“abc“ «a«b«c»»»`, t)
	if string(res) != `abc abc` {
		t.Errorf("Unexpected result: %s", res)
	}
	if !reflect.DeepEqual(exp, []node{{0, 3}, {6, 7}, {5, 7}, {4, 7}}) {
		t.Errorf("Unexpected expectations: %v", exp)
	}
}


