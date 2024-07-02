package compiler

import (
	"strings"

	"github.com/inspirer/textmapper/grammar"
	"github.com/inspirer/textmapper/parsers/tm/ast"
	"github.com/inspirer/textmapper/status"
	"github.com/inspirer/textmapper/syntax"
	"github.com/inspirer/textmapper/util/ident"
)

type resolver struct {
	*status.Status
	Syms      []grammar.Symbol
	NumTokens int

	syms  map[string]int
	ids   map[string]string // ID -> name
	tokID map[string]string // ensures unique token IDs consistency
}

func newResolver(s *status.Status) *resolver {
	return &resolver{
		Status: s,
		syms:   make(map[string]int),
		ids:    make(map[string]string),
		tokID:  make(map[string]string),
	}
}

func (c *resolver) addToken(name, id string, t ast.RawType, space ast.LexemeAttribute, n status.SourceNode) int {
	var rawType string
	if t.IsValid() {
		rawType = strings.TrimSuffix(strings.TrimPrefix(t.Text(), "{"), "}")
	}
	if i, exists := c.syms[name]; exists {
		sym := c.Syms[i]
		if sym.Type != rawType {
			anchor := n
			if t.IsValid() {
				anchor = t
			}
			c.Errorf(anchor, "terminal type redeclaration for %v, was %v", name, sym.PrettyType())
		}
		if sym.Space != space.IsValid() {
			anchor := n
			if space.IsValid() {
				anchor = space
			}
			c.Errorf(anchor, "%v is declared as both a space and non-space terminal", name)
		}
		if prev := c.tokID[name]; prev != id {
			c.Errorf(n, "%v is redeclared with a different ID (%q vs %q)", name, prev, id)
		}
		return sym.Index
	}
	c.tokID[name] = id
	if id == "" {
		id = ident.Produce(name, ident.UpperCase)
	}
	if prev, exists := c.ids[id]; exists {
		c.Errorf(n, "%v and %v get the same ID in generated code", name, prev)
	}

	sym := grammar.Symbol{
		Index:  len(c.Syms),
		ID:     id,
		Name:   name,
		Type:   rawType,
		Space:  space.IsValid(),
		Origin: n,
	}
	c.syms[name] = sym.Index
	c.ids[id] = name
	c.Syms = append(c.Syms, sym)
	c.NumTokens++
	return sym.Index
}

func (c *resolver) addNonterms(m *syntax.Model) {
	// TODO error is also nullable - make it so!
	nullable := syntax.Nullable(m)
	nonterms := m.Nonterms

	for _, nt := range nonterms {
		name := nt.Name
		if _, ok := c.syms[name]; ok {
			// TODO come up with a better error message
			c.Errorf(nt.Origin, "duplicate name %v", name)
		}
		id := ident.Produce(name, ident.CamelCase)
		if prev, exists := c.ids[id]; exists {
			c.Errorf(nt.Origin, "%v and %v get the same ID in generated code", name, prev)
		}
		index := len(c.Syms)
		sym := grammar.Symbol{
			Index:     index,
			ID:        id,
			Name:      name,
			Type:      nt.Type,
			CanBeNull: nullable.Get(index),
			Origin:    nt.Origin,
		}
		c.syms[name] = sym.Index
		c.ids[id] = name
		c.Syms = append(c.Syms, sym)
	}
}
