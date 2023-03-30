package ast

import (
	"github.com/inspirer/textmapper/status"
)

// SourceRange returns the full location of the node.
func (n *Node) SourceRange() status.SourceRange {
	if n == nil {
		return status.SourceRange{}
	}
	line, col := n.LineColumn()
	return status.SourceRange{
		Filename:  n.tree.path,
		Offset:    n.offset,
		EndOffset: n.endoffset,
		Line:      line,
		Column:    col,
	}
}
