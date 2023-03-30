package status

import (
	"bytes"
	"testing"
)

var statusTests = []struct {
	errs        []Error
	wantPrinted string
	wantErr     string
}{
	{
		errs:    []Error{},
		wantErr: "no errors",
	},
	{
		errs: []Error{
			{SourceRange{}, "I/O err 2"},
			{SourceRange{}, "I/O err 1"},
		},
		wantErr:     "I/O err 1 (and 1 more error(s))",
		wantPrinted: "I/O err 1\nI/O err 2\n",
	},
	{
		errs: []Error{
			{SourceRange{}, "I/O err"},
			{SourceRange{"file2", 0, 100, 1, 1}, "broken file"},
			{SourceRange{"file1", 80, 81, 20, 12}, "invalid utf-8"},
			{SourceRange{"file1", 10, 20, 3, 1}, "invalid identifier"},
			{SourceRange{"file1", 15, 20, 3, 6}, "second error"},
		},
		wantErr: "I/O err (and 3 more error(s))",
		wantPrinted: "I/O err\n" +
			"file1:3:1: invalid identifier\n" +
			"file1:20:12: invalid utf-8\n" +
			"file2:1:1: broken file\n",
	},
}

func TestStatus(t *testing.T) {
	for i, test := range statusTests {
		var s Status
		for _, e := range test.errs {
			s.Add(e.Origin, e.Msg)
		}
		s.Dedupe()
		if got := s.Error(); got != test.wantErr {
			t.Errorf("Error(%v) = %v; want: %v", i, got, test.wantErr)
		}
		var b bytes.Buffer
		Print(&b, s.Err())
		if got := b.String(); got != test.wantPrinted {
			t.Errorf("Print(%v) = %v; want: %v", i, got, test.wantPrinted)
		}
		s2 := FromError(s.Err())
		b.Reset()
		Print(&b, s2.Err())
		if got := b.String(); got != test.wantPrinted {
			t.Errorf("Print(FromError(%v.Err())) = %v; want: %v", i, got, test.wantPrinted)
		}
	}
}

type testNode SourceRange

func (tn testNode) SourceRange() SourceRange {
	return SourceRange(tn)
}

func TestErrorf(t *testing.T) {
	n := testNode(SourceRange{"file1", 80, 81, 20, 12})
	s := Errorf(n, "failure")
	want := "file1:20:12: failure"
	if got := s.Error(); got != want {
		t.Errorf("Errorf(\"failure\") = %v; want: %v", got, want)
	}
}
