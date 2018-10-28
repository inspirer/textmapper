// Package status stores and carries all kinds of errors that might occur during grammar
// generation.
package status

import (
	"flag"
	"fmt"
	"io"
	"sort"
)

var (
	byteOffsets = flag.Bool("byteoffsets", false, "print error locations in byte offsets")
)

// SourceRange references a piece of source code associated with an error.
type SourceRange struct {
	Filename  string // filename, if any
	Offset    int    // byte offset, 0-based
	EndOffset int    // end byte offset, 0-based
	Line      int    // line number, 1-based
	Column    int    // column number, 1-based (in bytes)
}

// SourceNode is an element of a parse tree with well-defined source positions.
type SourceNode interface {
	SourceRange() SourceRange
}

func (r *SourceRange) String() string {
	if *byteOffsets {
		return fmt.Sprintf("%s:%v:%v", r.Filename, r.Offset, r.EndOffset)
	}
	return fmt.Sprintf("%s:%v:%v", r.Filename, r.Line, r.Column)
}

// Error represents a single error message within a Status.
type Error struct {
	Origin SourceRange
	Msg    string
}

// Error implements the error interface.
func (e *Error) Error() string {
	if e.Origin.Filename == "" {
		// for I/O errors
		return e.Msg
	}
	return e.Origin.String() + ": " + e.Msg
}

// Status is a potentially empty list of errors.
type Status []*Error

// Add adds an Error with given source range and error message to an Status.
func (s *Status) Add(r SourceRange, msg string) {
	*s = append(*s, &Error{r, msg})
}

// AddError adds an error unpacking status error when needed.
func (s *Status) AddError(err error) {
	switch err := err.(type) {
	case nil:
		return
	case Status:
		*s = append(*s, err...)
	case *Error:
		*s = append(*s, err)
	default:
		// I/O errors don't originate in source code.
		*s = append(*s, &Error{SourceRange{}, err.Error()})
	}
}

// Errorf adds a new error to the status.
func (s *Status) Errorf(n SourceNode, format string, a ...interface{}) {
	err := &Error{Origin: n.SourceRange(), Msg: fmt.Sprintf(format, a...)}
	*s = append(*s, err)
}

// Sort sorts errors by their filename, offset, and message.
func (s Status) Sort() {
	sort.Slice(s, func(i, j int) bool {
		if s[i].Origin.Filename != s[j].Origin.Filename {
			return s[i].Origin.Filename < s[j].Origin.Filename
		}
		if s[i].Origin.Offset != s[j].Origin.Offset {
			return s[i].Origin.Offset < s[j].Origin.Offset
		}
		return s[i].Msg < s[j].Msg
	})
}

// Dedupe sorts the Status and removes all but the first error per line.
func (s *Status) Dedupe() {
	s.Sort()
	var last SourceRange
	ret := (*s)[:0]
	for _, e := range *s {
		if e.Origin.Filename == "" || e.Origin.Filename != last.Filename || e.Origin.Line != last.Line {
			last = e.Origin
			ret = append(ret, e)
		}
	}
	*s = ret
}

// Status implements the error interface.
func (s Status) Error() string {
	switch len(s) {
	case 0:
		return "no errors"
	case 1:
		return s[0].Error()
	}
	return fmt.Sprintf("%s (and %d more error(s))", s[0], len(s)-1)
}

// Err returns an error equivalent to this status.
func (s Status) Err() error {
	if len(s) == 0 {
		return nil
	}
	return s
}

// Print is a utility function that unpacks statuses and prints one error per line.
func Print(w io.Writer, err error) {
	if list, ok := err.(Status); ok {
		for _, e := range list {
			fmt.Fprintf(w, "%s\n", e)
		}
	} else if err != nil {
		fmt.Fprintf(w, "%s\n", err)
	}
}

// FromError unpacks Status stored as an error.
func FromError(err error) Status {
	var s Status
	s.AddError(err)
	return s
}

// Errorf returns a Status-compatible error.
func Errorf(n SourceNode, format string, a ...interface{}) *Error {
	return &Error{Origin: n.SourceRange(), Msg: fmt.Sprintf(format, a...)}
}
