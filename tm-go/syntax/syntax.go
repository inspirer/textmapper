// Package syntax analyzes and translates context-free grammars in the Textmapper notation into a
// simpler representation that is understood by a LALR parser generator.
package syntax

import (
	"fmt"
	"log"
	"strings"

	"github.com/inspirer/textmapper/tm-go/status"
)

// Model is a model of a language's syntax built on top of a set of terminals.
type Model struct {
	Terminals []string
	Params    []Param
	Nonterms  []*Nonterm // all params and nonterms must have distinct names
	Inputs    []Input
	Sets      []TokenSet // extra token sets to compute
	Cats      []string   // categories
}

func (m *Model) Ref(sym int, args []Arg) string {
	if sym < len(m.Terminals) {
		return m.Terminals[sym]
	}
	nt := m.Nonterms[sym-len(m.Terminals)]
	if len(args) == 0 {
		return nt.Name
	}
	var list []string
	for _, arg := range args {
		var val string
		if arg.Value == "" {
			val = fmt.Sprintf("%v=%v", m.Params[arg.Param].Name, m.Params[arg.TakeFrom].Name)
		} else {
			val = fmt.Sprintf("%v=%q", m.Params[arg.Param].Name, arg.Value)
		}
		list = append(list, val)
	}
	return fmt.Sprintf("%v<%v>", nt.Name, strings.Join(list, ","))
}

// Input introduces a start nonterminal.
type Input struct {
	Nonterm int // Index in model.Nonterms
	NoEoi   bool
}

// Param is a grammar-wide template parameter.
type Param struct {
	Name         string
	DefaultValue string
	Lookahead    bool
	Origin       status.SourceNode
}

// Nonterm is a grammar nonterminal.
type Nonterm struct {
	Name   string
	Type   string // the type of the associated value
	Params []int
	Value  *Expr // non-nil

	// When non-empty, this is a Lookahead nonterminal (implies Value is Empty). When two or more
	// lookahead nonterminals can be reduced in the same state, their lookahead predicates are
	// evaluated at runtime to determine which one should actually be reduced.
	LA     []LAPredicate
	Origin status.SourceNode
}

// LAPredicate is a restriction on the remaining input stream of tokens.
type LAPredicate struct {
	Input   int
	Negated bool
}

// Expr represents the right-hand side of a production rule (or its part).
type Expr struct {
	Kind      ExprKind
	Name      string
	Sub       []*Expr
	Symbol    int
	Args      []Arg
	Predicate *Predicate
	ListFlags ListFlags
	Pos       int // Positional index of a reference in the original rule.
	Origin    status.SourceNode
	Model     *Model // Kept for some kinds for debugging.
}

func (e *Expr) String() string {
	switch e.Kind {
	case Empty:
		return "%empty"
	case Optional:
		return fmt.Sprintf("%v?", parenthesize(e.Kind, e.Sub[0]))
	case Assign:
		return fmt.Sprintf("%v=%v", e.Name, parenthesize(e.Kind, e.Sub[0]))
	case Append:
		return fmt.Sprintf("%v+=%v", e.Name, parenthesize(e.Kind, e.Sub[0]))
	case Arrow:
		return fmt.Sprintf("%v -> %v", parenthesize(e.Kind, e.Sub[0]), e.Name)
	case Prec:
		return fmt.Sprintf("%v %%prec %v", parenthesize(e.Kind, e.Sub[0]), e.Model.Terminals[e.Symbol])
	case Conditional:
		return fmt.Sprintf("[%v] %v", e.Predicate.String(e.Model), e.Sub[0])
	case List:
		suffix := "*"
		if e.ListFlags&OneOrMore != 0 {
			suffix = "+"
		}
		if e.ListFlags&RightRecursive != 0 {
			suffix += "/rr"
		}
		var sep string
		if len(e.Sub) > 1 {
			sep = fmt.Sprintf(" separator %v", e.Sub[1])
		}
		return fmt.Sprintf("(%v%v)%v", e.Sub[0], sep, suffix)
	case Sequence:
		var buf strings.Builder
		for i, sub := range e.Sub {
			if i > 0 {
				buf.WriteByte(' ')
			}
			buf.WriteString(parenthesize(e.Kind, sub))
		}
		return buf.String()
	case Choice:
		var buf strings.Builder
		for i, sub := range e.Sub {
			if i > 0 {
				buf.WriteString(" | ")
			}
			buf.WriteString(parenthesize(e.Kind, sub))
		}
		return buf.String()
	case Reference:
		return e.Model.Ref(e.Symbol, e.Args)
	case Set:
		return fmt.Sprintf("set(%v)", e.Model.Sets[e.Pos].String(e.Model))
	case StateMarker:
		return "." + e.Name
	case Command:
		return e.Name // includes {}
	case Lookahead:
		var buf strings.Builder
		buf.WriteString("(?= ")
		for i, sub := range e.Sub {
			if i > 0 {
				buf.WriteString(" & ")
			}
			buf.WriteString(parenthesize(e.Kind, sub))
		}
		buf.WriteString(")")
		return buf.String()
	case LookaheadNot:
		return fmt.Sprintf("!%v", e.Sub[0])
	default:
		log.Fatalf("cannot stringify kind=%v", e.Kind)
		return ""
	}
}

func parenthesize(outer ExprKind, sub *Expr) string {
	var paren bool
	switch sub.Kind {
	case Command, Lookahead, Set, Empty, List:
		// no parentheses
	case Choice:
		paren = true
	case Sequence:
		paren = outer != Arrow && outer != Prec && outer != Choice
	case Arrow:
		paren = outer != Arrow && outer != Choice
	default:
		paren = outer == Optional && sub.Kind != Reference
	}
	if paren {
		return fmt.Sprintf("(%v)", sub.String())
	}
	return sub.String()
}

// ListFlags define the layout of list production rules.
type ListFlags int8

// List flags.
const (
	OneOrMore ListFlags = 1 << iota
	RightRecursive
)

// ExprKind is an production rule
type ExprKind int8

// All expression kinds.
const (
	Empty        ExprKind = iota
	Optional              // of {Sub0}
	Choice                // of 2+ Sub
	Sequence              // of 2+ Sub
	Reference             // {Symbol}<{Args}>
	Assign                // field={Sub0}
	Append                // field+={Sub0}
	Arrow                 // {Sub0} -> {Name}
	Set                   // set({Pos = index in Model.Sets})
	StateMarker           // .{Name}
	Command               // stored in {Name}
	Lookahead             // (?= {Sub0} & {Sub1} ...)
	LookaheadNot          // !{Sub0}   inside (?= ...)

	// The following kinds can appear as children of a top-level Choice expression only (or be nested
	// in one another).
	Conditional // [{Predicate}] {Sub0}
	Prec        // {Sub0} %prec {Symbol}

	// Top-level expressions.
	List // of {Sub0}, separator={Sub1} (if present), also {ListFlags}
)

// TokenSet is a grammar expression that resolves to a set of tokens.
type TokenSet struct {
	Kind   SetOp
	Symbol int
	Args   []Arg
	Sub    []TokenSet
	Origin status.SourceNode
}

func (ts TokenSet) String(m *Model) string {
	switch ts.Kind {
	case Any:
		return m.Ref(ts.Symbol, ts.Args)
	case First:
		return "first " + m.Ref(ts.Symbol, ts.Args)
	case Last:
		return "last " + m.Ref(ts.Symbol, ts.Args)
	case Follow:
		return "follow " + m.Ref(ts.Symbol, ts.Args)
	case Precede:
		return "precede " + m.Ref(ts.Symbol, ts.Args)
	case Complement:
		return fmt.Sprintf("~(%v)", ts.Sub[0].String(m))
	case Union, Intersection:
		var buf strings.Builder
		for i, sub := range ts.Sub {
			if i > 0 {
				if ts.Kind == Union {
					buf.WriteString(" & ")
				} else {
					buf.WriteString(" | ")
				}
			}

			text := sub.String(m)
			if sub.Kind == Union || sub.Kind == Intersection {
				text = fmt.Sprintf("(%v)", text)
			}
			buf.WriteString(text)
		}
		return buf.String()
	default:
		log.Fatalf("cannot stringify TokenSet Kind=%v", ts.Kind)
		return ""
	}
}

// SetOp is a set operator.
type SetOp int8

// Set operators.
const (
	Any SetOp = iota
	First
	Last
	Precede
	Follow
	Union
	Intersection
	Complement
)

// Arg provides a value for a template parameter.
type Arg struct {
	Param    int
	Value    string
	TakeFrom int // if Value == "", the value should be taken from this parameter
	Origin   status.SourceNode
}

// Predicate is an expression which, given a template environment, evaluates to true or false.
type Predicate struct {
	Op     PredicateOp
	Sub    []*Predicate
	Param  int
	Value  string
	Origin status.SourceNode
}

func (p *Predicate) String(m *Model) string {
	switch p.Op {
	case Or, And:
		var buf strings.Builder
		for i, sub := range p.Sub {
			if i > 0 {
				if p.Op == And {
					buf.WriteString(" & ")
				} else {
					buf.WriteString(" | ")
				}
			}

			text := sub.String(m)
			if sub.Op == Or || sub.Op == And {
				text = fmt.Sprintf("(%v)", text)
			}
			buf.WriteString(text)
		}
		return buf.String()
	case Not:
		return fmt.Sprintf("!(%v)", p.Sub[0].String(m))
	case Equals:
		return fmt.Sprintf("%v=%q", m.Params[p.Param].Name, p.Value)
	default:
		log.Fatalf("cannot stringify Op=%v", p.Op)
		return ""
	}
}

// PredicateOp is a predicate operator.
type PredicateOp int8

// Predicate operators.
const (
	Or PredicateOp = iota
	And
	Not
	Equals // true if {Param} == {Value}
)

// Simplify flattens lists and removes redundant nodes from a syntax expression (in-place).
func Simplify(e *Expr) *Expr {
	for i, sub := range e.Sub {
		e.Sub[i] = Simplify(sub)
	}
	if e.Kind != Choice && e.Kind != Sequence {
		return e
	}
	var rewrite bool
	for _, sub := range e.Sub {
		rewrite = rewrite || e.Kind == sub.Kind || sub.Kind == Empty && e.Kind == Sequence
	}
	if rewrite {
		var out []*Expr
		for _, sub := range e.Sub {
			if sub.Kind == Empty && e.Kind == Sequence {
				continue
			}
			if e.Kind == sub.Kind {
				out = append(out, sub.Sub...)
			} else {
				out = append(out, sub)
			}
		}
		e.Sub = out
	}
	switch len(e.Sub) {
	case 0:
		return &Expr{Kind: Empty, Origin: e.Origin}
	case 1:
		return e.Sub[0]
	}
	return e
}
