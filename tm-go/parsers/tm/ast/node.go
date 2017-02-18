package ast

import (
	"fmt"
	"github.com/inspirer/textmapper/tm-go/parsers/tm"
	"github.com/inspirer/textmapper/tm-go/parsers/tm/selector"
)

type Span struct {
	input *string
	spans []span
	index int
}

type span struct {
	t          tm.NodeType
	offset     int
	endoffset  int
	next       int
	firstChild int
	parent     int
}

type Builder struct {
	input string
	s     []span
	stack []int
	err   error
}

func NewBuilder(input string) *Builder {
	return &Builder{input: input, s: []span{span{offset: -1}}, stack: []int{0}}
}

func (b *Builder) Root() (Span, error) {
	b.s[0].next = 0
	if b.err != nil {
		return Span{&b.input, b.s[:1], 0}, b.err
	}

	return Span{&b.input, b.s, len(b.s) - 1}, nil
}

func (b *Builder) AddError(line, offset, len int, msg string) {
	b.err = fmt.Errorf("%d: %v", line, msg)
}

func (b *Builder) Add(t tm.NodeType, offset, endoffset int) {
	index := len(b.s)
	firstChild := 0

	start := len(b.stack)
	for b.s[b.stack[start-1]].offset >= offset {
		start--
	}
	if start < len(b.stack) {
		firstChild = b.stack[start]
		for _, i := range b.stack[start:] {
			b.s[i].parent = index
		}
	}
	b.s[b.stack[start-1]].next = index
	b.stack = append(b.stack[:start], index)
	b.s = append(b.s, span{
		t:          t,
		offset:     offset,
		endoffset:  endoffset,
		firstChild: firstChild,
	})
}

func (s Span) Type() tm.NodeType {
	return s.spans[s.index].t
}

func (s Span) Child(sel selector.Selector) Node {
	if s.input == nil {
		return Span{}
	}
	for i := s.spans[s.index].firstChild; i > 0; i = s.spans[i].next {
		if sel(s.spans[i].t) {
			return Span{s.input, s.spans, i}
		}
	}
	return Span{}
}

func (s Span) Children(sel selector.Selector) []Node {
	if s.input == nil {
		return nil
	}
	var ret []Node
	for i := s.spans[s.index].firstChild; i > 0; i = s.spans[i].next {
		if sel(s.spans[i].t) {
			ret = append(ret, Span{s.input, s.spans, i})
		}
	}
	return ret
}

func (s Span) Next(sel selector.Selector) Node {
	if s.input == nil {
		return Span{}
	}
	for i := s.spans[s.index].next; i > 0; i = s.spans[i].next {
		if sel(s.spans[i].t) {
			return Span{s.input, s.spans, i}
		}
	}
	return Span{}
}

func (s Span) NextAll(sel selector.Selector) []Node {
	if s.input == nil {
		return nil
	}
	var ret []Node
	for i := s.spans[s.index].next; i > 0; i = s.spans[i].next {
		if sel(s.spans[i].t) {
			ret = append(ret, Span{s.input, s.spans, i})
		}
	}
	return nil
}

func (s Span) Text() string {
	if s.input == nil {
		return ""
	}
	start, end := s.spans[s.index].offset, s.spans[s.index].endoffset
	return (*s.input)[start:end]
}
