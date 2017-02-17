package ast

import (
	"github.com/inspirer/textmapper/tm-go/parsers/test"
	"github.com/inspirer/textmapper/tm-go/parsers/test/selector"
)

type NodeImpl struct {
	tp       test.NodeType
	Offset   int32
	Length   int32
	children []NodeImpl
	Parent   *NodeImpl
}

func (n *NodeImpl) Type() test.NodeType {
	return n.tp
}

func (n *NodeImpl) Child(sel selector.Selector) Node {
	//index := 0
	//for _, child := range n.Children {
	//
	//}
	return nil
}

func (n *NodeImpl) Children(sel selector.Selector) []Node {
	return nil
}
