package dump_test

import (
	"testing"

	"github.com/inspirer/textmapper/tm-go/util/diff"
	"github.com/inspirer/textmapper/tm-go/util/dump"
)

type int1 interface{}
type int2 interface{ Meth1() }
type str1 struct{ I, E int16 }
type rec struct{ I *rec }
type partial struct {
	A int1
	B int2
	C string
	D int8
	E str1
	F rec
	G [4]int
	H []int1
	I []uint32
	M map[string]str1
	N map[interface{}]bool
}
type str2 struct {
	A *str2
	B int2
}

func (str2) Meth1() {}

var selfRef = func() interface{} {
	ret := str2{A: &str2{A: &str2{}}}
	ret.A.A.A = ret.A
	return ret
}()

var selfRef2 = func() *str2 {
	ret := &str2{A: &str2{}}
	ret.B = ret.A
	return ret
}()

func (p partial) Meth1() {}

type enum1 uint8

const (
	EnumVal1 enum1 = iota
	EnumVal2
)

func (e enum1) GoString() string {
	if e == EnumVal1 {
		return "val1"
	}
	return "val2"
}

type str3 struct{ Val enum1 }

var tests = []struct {
	input interface{}
	want  string
}{
	{nil, `nil`},
	{int1(nil), `nil`},
	{int1(9), `int(9)`},
	{int1(uint(8)), `uint(8)`},
	{int1((*str1)(nil)), `(*dump_test.str1)(nil)`},
	{true, `true`},
	{"", `""`},
	{"abc", `"abc"`},
	{123, `int(123)`},
	{uint8(123), `uint8(123)`},
	{uintptr(0), `uintptr(0)`},
	{1.23e-1, `float64(0.123)`},
	{str1{1, 8}, "dump_test.str1{\n  I: 1,\n  E: 8,\n}"},
	{&str1{1, 8}, "&dump_test.str1{\n  I: 1,\n  E: 8,\n}"},
	{map[interface{}]interface{}{1: nil}, "map[interface {}]interface {}{\n  int(1): interface {}(nil),\n}"},
	{map[int32]interface{}{1: nil}, "map[int32]interface {}{\n  1: interface {}(nil),\n}"},
	{map[int32]string{1: "foo"}, "map[int32]string{\n  1: \"foo\",\n}"},
	{partial{}, "dump_test.partial{}"},
	{partial{A: 8}, "dump_test.partial{\n  A: int(8),\n}"},
	{partial{C: "foo"}, "dump_test.partial{\n  C: \"foo\",\n}"},
	{partial{D: 8}, "dump_test.partial{\n  D: 8,\n}"},
	{partial{F: rec{&rec{}}}, "dump_test.partial{\n  F: {\n    I: &{},\n  },\n}"},
	{partial{G: [4]int{1, 2, 3, 4}}, "dump_test.partial{\n  G: {\n    1,\n    2,\n    3,\n    4,\n  },\n}"},
	{partial{H: []int1{&rec{}}}, "dump_test.partial{\n  H: {\n    &dump_test.rec{},\n  },\n}"},
	{partial{I: []uint32{}}, "dump_test.partial{\n  I: {},\n}"},
	{partial{I: []uint32{4}}, "dump_test.partial{\n  I: {\n    4,\n  },\n}"},
	{partial{I: []uint32{4, 5}}, "dump_test.partial{\n  I: {\n    4,\n    5,\n  },\n}"},
	{partial{M: make(map[string]str1)}, "dump_test.partial{\n  M: {},\n}"},
	{partial{M: map[string]str1{"C": {4, 6}, "A": {}, "B": {I: 3}}}, "dump_test.partial{\n  M: {\n    \"A\": {},\n    \"B\": {\n      I: 3,\n    },\n    \"C\": {\n      I: 4,\n      E: 6,\n    },\n  },\n}"},
	{partial{N: map[interface{}]bool{"aa": true}}, "dump_test.partial{\n  N: {\n    \"aa\": true,\n  },\n}"},
	{partial{N: map[interface{}]bool{int1((*str1)(nil)): false}}, "dump_test.partial{\n  N: {\n    (*dump_test.str1)(nil): false,\n  },\n}"},
	{partial.Meth1, "func(dump_test.partial){...}"},
	{(&partial{}).Meth1, "func(){...}"},
	{str2{}, "dump_test.str2{}"},
	{str2{B: str2{}}, "dump_test.str2{\n  B: dump_test.str2{},\n}"},
	{str2{B: &str2{}}, "dump_test.str2{\n  B: &dump_test.str2{},\n}"},
	{str2{A: &str2{}}, "dump_test.str2{\n  A: &{},\n}"},
	{EnumVal1, "dump_test.enum1(val1)"},
	{str3{EnumVal2}, "dump_test.str3{\n  Val: val2,\n}"},

	// Reference cycles.
	{selfRef, "dump_test.str2{\n  A: &{\n    A: &{\n      A: &{... cycle},\n    },\n  },\n}"},
	{selfRef2, "&dump_test.str2{\n  A: &{},\n  B: &dump_test.str2{... cycle},\n}"},
	{*selfRef2, "dump_test.str2{\n  A: &{},\n  B: &dump_test.str2{... cycle},\n}"},

	// Depth check
	{rec{}, `dump_test.rec{}`},
	{&rec{}, `&dump_test.rec{}`},
	{&rec{&rec{&rec{&rec{&rec{&rec{&rec{&rec{&rec{&rec{&rec{}}}}}}}}}}}, `&dump_test.rec{
  I: &{
    I: &{
      I: &{
        I: &{
          I: &{
            I: &{
              I: &{
                I: &{
                  I: &{
                    I: &...,
                  },
                },
              },
            },
          },
        },
      },
    },
  },
}`},
}

func TestObject(t *testing.T) {
	for _, tc := range tests {
		got := dump.Object(tc.input)
		if diff := diff.LineDiff(tc.want, got); diff != "" {
			t.Errorf("diff for %#v\n--- want\n+++ got\n%v\n", tc.input, diff)
		}
	}
}

var diffTests = []struct {
	a, b interface{}
	want string
}{
	{"", "", ""},
	{"abc", "abc", ""},
	{"abc", "abc\ndef", "@@ -1,1 +1,1 @@\n-\"abc\"\n+\"abc\\ndef\"\n"},
	{1, 2, "@@ -1,1 +1,1 @@\n-int(1)\n+int(2)\n"},
	{[]uint32{1, 2, 3}, []uint32{1, 2, 6, 3}, `@@ -1,5 +1,6 @@
 []uint32{
   1,
   2,
+  6,
   3,
 }
`,
	},
	{str2{A: &str2{}}, str2{A: &str2{}}, ""},
	{str2{A: &str2{}}, str2{B: &str2{}}, `@@ -1,3 +1,3 @@
 dump_test.str2{
-  A: &{},
+  B: &dump_test.str2{},
 }
`,
	},
	{selfRef2, str2{}, `@@ -1,6 +1,1 @@
-&dump_test.str2{
-  A: &{},
-  B: &dump_test.str2{... cycle},
-}
-
-The first object cannot be properly serialized into a string.
+dump_test.str2{}
`},
	{selfRef, selfRef2, `@@ -1,9 +1,6 @@
-dump_test.str2{
-  A: &{
-    A: &{
-      A: &{... cycle},
-    },
-  },
+&dump_test.str2{
+  A: &{},
+  B: &dump_test.str2{... cycle},
 }
 
-The first object cannot be properly serialized into a string.
+The second object cannot be properly serialized into a string.
`,
	},
}

func TestDiff(t *testing.T) {
	for _, tc := range diffTests {
		got := dump.Diff(tc.a, tc.b)
		if got != tc.want {
			t.Errorf("dump.Diff(%v,%v) = \n%v\n\nWANT: \n%v", tc.a, tc.b, got, tc.want)
		}
	}
}
