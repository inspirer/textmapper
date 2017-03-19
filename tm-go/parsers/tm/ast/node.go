package ast

import (
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
	return &Builder{input: input, s: []span{{offset: -1}}, stack: make([]int, 1, 512)}
}

func (b *Builder) Root() (Span, error) {
	b.s[0].next = 0
	if b.err != nil {
		return Span{&b.input, b.s[:1], 0}, b.err
	}

	return Span{&b.input, b.s, len(b.s) - 1}, nil
}

func (b *Builder) AddError(se tm.SyntaxError) bool {
	b.err = se
	return true
}

func (b *Builder) Add(t tm.NodeType, offset, endoffset int) {
	index := len(b.s)

	start := len(b.stack)
	end := start
	for o := b.s[b.stack[start-1]].offset; o >= offset; o = b.s[b.stack[start-1]].offset {
		start--
		if o >= endoffset {
			end--
		}
	}
	firstChild := 0
	if start < end {
		firstChild = b.stack[start]
		for _, i := range b.stack[start:end] {
			b.s[i].parent = index
		}
	}
	b.s[b.stack[start-1]].next = index
	if end == len(b.stack) {
		b.stack = append(b.stack[:start], index)
	} else if start < end {
		b.stack[start] = index
		l := copy(b.stack[start+1:], b.stack[end:])
		b.stack = b.stack[:start+1+l]
	} else {
		b.stack = append(b.stack, 0)
		copy(b.stack[start+1:], b.stack[start:])
		b.stack[start] = index
	}
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

func (s Span) Offset() int {
	return s.spans[s.index].offset
}

func (s Span) Endoffset() int {
	return s.spans[s.index].endoffset
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
