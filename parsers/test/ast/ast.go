// generated by Textmapper; DO NOT EDIT

package ast

import (
	"github.com/inspirer/textmapper/parsers/test"
	"github.com/inspirer/textmapper/parsers/test/selector"
)

type Node interface {
	Type() test.NodeType
	Offset() int
	Endoffset() int
	// Child returns the first child node that matches the selector.
	Child(sel selector.Selector) Node
	Children(sel selector.Selector) []Node
	// Next returns the first element among the following siblings that matches the selector.
	Next(sel selector.Selector) Node
	// NextAll returns all following siblings of the node that match the selector.
	NextAll(sel selector.Selector) []Node
	Text() string
	IsValid() bool
}

// Interfaces.

type TestNode interface {
	TestNode() Node
}

type Token struct {
	Node
}

type NilNode struct{}

var nilInstance = &NilNode{}

// All types implement TestNode.
func (n AsExpr) TestNode() Node        { return n.Node }
func (n Block) TestNode() Node         { return n.Node }
func (n Decl1) TestNode() Node         { return n.Node }
func (n Decl2) TestNode() Node         { return n.Node }
func (n DeclOptQual) TestNode() Node   { return n.Node }
func (n Empty1) TestNode() Node        { return n.Node }
func (n EvalEmpty1) TestNode() Node    { return n.Node }
func (n EvalFoo) TestNode() Node       { return n.Node }
func (n EvalFoo2) TestNode() Node      { return n.Node }
func (n Icon) TestNode() Node          { return n.Node }
func (n If) TestNode() Node            { return n.Node }
func (n Int) TestNode() Node           { return n.Node }
func (n IntExpr) TestNode() Node       { return n.Node }
func (n LastInt) TestNode() Node       { return n.Node }
func (n Negation) TestNode() Node      { return n.Node }
func (n PlusExpr) TestNode() Node      { return n.Node }
func (n Test) TestNode() Node          { return n.Node }
func (n TestClause) TestNode() Node    { return n.Node }
func (n TestIntClause) TestNode() Node { return n.Node }
func (n Int7) TestNode() Node          { return n.Node }
func (n Int9) TestNode() Node          { return n.Node }
func (n Token) TestNode() Node         { return n.Node }
func (NilNode) TestNode() Node         { return nil }

type Decl2Interface interface {
	TestNode
	decl2InterfaceNode()
}

// decl2InterfaceNode() ensures that only the following types can be
// assigned to Decl2Interface.
func (Decl2) decl2InterfaceNode()   {}
func (If) decl2InterfaceNode()      {}
func (NilNode) decl2InterfaceNode() {}

type Declaration interface {
	TestNode
	declarationNode()
}

// declarationNode() ensures that only the following types can be
// assigned to Declaration.
func (AsExpr) declarationNode()        {}
func (Block) declarationNode()         {}
func (Decl1) declarationNode()         {}
func (Decl2) declarationNode()         {}
func (DeclOptQual) declarationNode()   {}
func (Empty1) declarationNode()        {}
func (EvalEmpty1) declarationNode()    {}
func (EvalFoo) declarationNode()       {}
func (EvalFoo2) declarationNode()      {}
func (If) declarationNode()            {}
func (Int) declarationNode()           {}
func (IntExpr) declarationNode()       {}
func (LastInt) declarationNode()       {}
func (PlusExpr) declarationNode()      {}
func (TestClause) declarationNode()    {}
func (TestIntClause) declarationNode() {}
func (NilNode) declarationNode()       {}

type Expr interface {
	TestNode
	exprNode()
}

// exprNode() ensures that only the following types can be
// assigned to Expr.
func (AsExpr) exprNode()   {}
func (Int9) exprNode()     {}
func (IntExpr) exprNode()  {}
func (PlusExpr) exprNode() {}
func (NilNode) exprNode()  {}

// Types.

type AsExpr struct {
	Node
}

func (n AsExpr) Left() Expr {
	child := n.Child(selector.Expr)
	return ToTestNode(child).(Expr)
}

func (n AsExpr) Right() Expr {
	child := n.Child(selector.Expr).Next(selector.Expr)
	return ToTestNode(child).(Expr)
}

type Block struct {
	Node
}

func (n Block) Negation() (Negation, bool) {
	child := n.Child(selector.Negation)
	return Negation{child}, child.IsValid()
}

func (n Block) Declaration() []Declaration {
	nodes := n.Children(selector.Declaration)
	var ret = make([]Declaration, 0, len(nodes))
	for _, node := range nodes {
		ret = append(ret, ToTestNode(node).(Declaration))
	}
	return ret
}

type Decl1 struct {
	Node
}

func (n Decl1) Identifier() []Token {
	nodes := n.Children(selector.Identifier)
	var ret = make([]Token, 0, len(nodes))
	for _, node := range nodes {
		ret = append(ret, Token{node})
	}
	return ret
}

type Decl2 struct {
	Node
}

type DeclOptQual struct {
	Node
}

func (n DeclOptQual) Identifier() []Token {
	nodes := n.Children(selector.Identifier)
	var ret = make([]Token, 0, len(nodes))
	for _, node := range nodes {
		ret = append(ret, Token{node})
	}
	return ret
}

type Empty1 struct {
	Node
}

type EvalEmpty1 struct {
	Node
}

func (n EvalEmpty1) Expr() Expr {
	child := n.Child(selector.Expr)
	return ToTestNode(child).(Expr)
}

type EvalFoo struct {
	Node
}

func (n EvalFoo) Expr() Expr {
	child := n.Child(selector.Expr)
	return ToTestNode(child).(Expr)
}

type EvalFoo2 struct {
	Node
}

func (n EvalFoo2) A() Expr {
	child := n.Child(selector.Expr)
	return ToTestNode(child).(Expr)
}

func (n EvalFoo2) B() Expr {
	child := n.Child(selector.Expr).Next(selector.Expr)
	return ToTestNode(child).(Expr)
}

type Icon struct {
	Node
}

type If struct {
	Node
}

func (n If) Then() Decl2Interface {
	child := n.Child(selector.Decl2Interface)
	return ToTestNode(child).(Decl2Interface)
}

func (n If) Else() (Decl2Interface, bool) {
	child := n.Child(selector.Decl2Interface).Next(selector.Decl2Interface)
	return ToTestNode(child).(Decl2Interface), child.IsValid()
}

type Int struct {
	Node
}

type IntExpr struct {
	Node
}

type LastInt struct {
	Node
}

type Negation struct {
	Node
}

type PlusExpr struct {
	Node
}

func (n PlusExpr) Left() Expr {
	child := n.Child(selector.Expr)
	return ToTestNode(child).(Expr)
}

func (n PlusExpr) Right() Expr {
	child := n.Child(selector.Expr).Next(selector.Expr)
	return ToTestNode(child).(Expr)
}

type Test struct {
	Node
}

func (n Test) Declaration() []Declaration {
	nodes := n.Children(selector.Declaration)
	var ret = make([]Declaration, 0, len(nodes))
	for _, node := range nodes {
		ret = append(ret, ToTestNode(node).(Declaration))
	}
	return ret
}

type TestClause struct {
	Node
}

type TestIntClause struct {
	Node
}

func (n TestIntClause) Icon() Icon {
	child := n.Child(selector.Icon)
	return Icon{child}
}

type Int7 struct {
	Node
}

type Int9 struct {
	Node
}
