// generated by Textmapper; DO NOT EDIT

package test

import (
	"fmt"

	"github.com/inspirer/textmapper/parsers/test/token"
)

var tmNonterminals = [...]string{
	"Declaration_list",
	"Test",
	"Declaration",
	"lookahead_FooLookahead",
	"lookahead_notFooLookahead",
	"setof_not_EOI_or_DOT_or_RBRACE",
	"setof_not_EOI_or_DOT_or_RBRACE_optlist",
	"FooLookahead",
	"setof_foo_la",
	"setof_foo_la_list",
	"empty1",
	"foo_la",
	"foo_nonterm",
	"foo_nonterm_A",
	"QualifiedName",
	"Decl1",
	"Decl2",
	"If",
	"expr",
	"O",
	"elem_list",
	"elem",
	"customPlus",
	"primaryExpr",
	"primaryExpr_WithoutAs",
	"QualifiedName-opt",
}

func symbolName(sym int32) string {
	if sym == noToken {
		return "<no-token>"
	}
	if sym < int32(token.NumTokens) {
		return token.Type(sym).String()
	}
	if i := int(sym) - int(token.NumTokens); i < len(tmNonterminals) {
		return tmNonterminals[i]
	}
	return fmt.Sprintf("nonterminal(%d)", sym)
}

var tmAction = []int32{
	-1, -1, -1, -3, 11, -1, -1, -27, -51, -1, -1, -55, 1, 3, 4, 82, -1, -1, 17,
	62, -75, -1, -81, -1, -1, -1, 10, -1, -1, 0, -1, 12, -1, -1, -1, -1, 78, -1,
	-105, 21, -1, -1, 93, 91, -1, -129, 90, 8, -1, -1, 9, -1, 24, 25, 26, 27, 28,
	29, 30, 31, 32, 33, 34, 35, 36, 37, 38, 14, 39, 40, 41, 42, 43, 44, 45, 46,
	47, 48, 49, 50, 51, 52, 53, 54, 55, 56, 57, 58, 59, 60, 61, -1, 15, 16, 80,
	-1, -1, -1, -137, -1, -1, 86, 87, -1, -1, 89, 6, -1, 7, 64, 65, 66, 67, 68,
	69, 71, -1, -145, 79, -1, -1, 19, -1, 72, -1, -1, 81, -151, 5, 63, 70, -175,
	-181, -1, 18, 85, -187, -1, -1, -193, 84, -1, 20, -1, -1, -2, -2, -2,
}

var tmLalr = []int32{
	19, -1, 0, 13, 6, 13, 7, 13, 8, 13, 9, 13, 10, 13, 11, 13, 13, 13, 15, 13,
	16, 13, -1, -2, 24, -1, 0, 81, 6, 81, 7, 81, 8, 81, 9, 81, 10, 81, 11, 81,
	13, 81, 15, 81, 16, 81, -1, -2, 17, 100, -1, -2, 6, -1, 7, -1, 8, -1, 9, -1,
	10, -1, 11, -1, 13, -1, 15, -1, 0, 2, -1, -2, 6, -1, 18, 72, -1, -2, 4, -1,
	0, 99, 6, 99, 7, 99, 8, 99, 9, 99, 10, 99, 11, 99, 13, 99, 15, 99, 16, 99,
	-1, -2, 21, -1, 0, 98, 6, 98, 7, 98, 8, 98, 9, 98, 10, 98, 11, 98, 13, 98,
	15, 98, 16, 98, -1, -2, 12, -1, 31, -1, 18, 88, -1, -2, 18, 96, 27, 96, 12,
	97, -1, -2, 27, -1, 18, 75, -1, -2, 14, -1, 0, 83, 6, 83, 7, 83, 8, 83, 9,
	83, 10, 83, 11, 83, 13, 83, 15, 83, 16, 83, -1, -2, 27, -1, 18, 76, -1, -2,
	27, -1, 18, 77, -1, -2, 27, -1, 18, 95, -1, -2, 27, 94, 18, 94, -1, -2,
}

var tmGoto = []int32{
	0, 4, 6, 8, 10, 18, 20, 68, 86, 104, 124, 146, 164, 176, 198, 202, 222, 236,
	250, 268, 272, 276, 290, 292, 294, 298, 304, 306, 328, 348, 350, 358, 364,
	366, 368, 370, 372, 374, 376, 378, 380, 388, 390, 406, 408, 410, 412, 414,
	416, 420, 422, 426, 426, 428, 430, 434, 452, 472, 492, 506, 508, 510, 514,
	528, 546, 564, 566,
}

var tmFromTo = []int16{
	143, 146, 144, 147, 32, 52, 32, 53, 32, 54, 21, 36, 22, 36, 32, 55, 95, 118,
	32, 56, 0, 3, 5, 18, 10, 3, 11, 3, 16, 30, 20, 33, 27, 3, 28, 3, 32, 57, 40,
	96, 41, 98, 48, 3, 49, 3, 51, 109, 91, 98, 99, 98, 107, 3, 116, 109, 119, 98,
	120, 98, 124, 98, 125, 98, 133, 98, 138, 98, 0, 4, 10, 4, 11, 4, 27, 4, 28,
	4, 32, 58, 48, 4, 49, 4, 107, 4, 0, 5, 10, 5, 11, 5, 27, 5, 28, 5, 32, 59,
	48, 5, 49, 5, 107, 5, 0, 6, 1, 6, 10, 6, 11, 6, 27, 6, 28, 6, 32, 60, 48, 6,
	49, 6, 107, 6, 0, 7, 10, 7, 11, 7, 27, 7, 28, 7, 32, 61, 48, 7, 49, 7, 104,
	126, 107, 7, 137, 126, 0, 8, 10, 8, 11, 8, 27, 8, 28, 8, 32, 62, 48, 8, 49,
	8, 107, 8, 25, 42, 32, 63, 45, 42, 51, 110, 103, 125, 116, 110, 0, 9, 10, 9,
	11, 9, 27, 9, 28, 9, 32, 64, 48, 9, 49, 9, 104, 9, 107, 9, 137, 9, 32, 65,
	127, 137, 0, 10, 5, 19, 10, 10, 11, 10, 27, 10, 28, 10, 32, 66, 48, 10, 49,
	10, 107, 10, 10, 26, 27, 47, 28, 50, 32, 67, 48, 106, 49, 108, 107, 128, 2,
	16, 5, 20, 6, 21, 9, 25, 23, 40, 24, 41, 32, 68, 32, 69, 34, 92, 35, 93, 37,
	94, 44, 104, 97, 121, 100, 123, 116, 129, 141, 142, 3, 17, 32, 70, 17, 31,
	32, 71, 30, 51, 33, 91, 37, 95, 38, 95, 51, 111, 96, 119, 116, 111, 32, 72,
	32, 73, 7, 22, 32, 74, 10, 27, 27, 48, 32, 75, 32, 76, 32, 77, 51, 112, 100,
	124, 116, 112, 117, 124, 122, 133, 131, 138, 132, 124, 136, 124, 139, 124,
	141, 124, 32, 78, 41, 99, 51, 113, 91, 99, 116, 113, 119, 99, 120, 99, 125,
	99, 133, 99, 138, 99, 32, 79, 32, 80, 51, 114, 96, 120, 116, 114, 25, 43, 32,
	81, 45, 43, 32, 82, 32, 83, 32, 84, 32, 85, 32, 86, 32, 87, 32, 88, 32, 89,
	0, 11, 10, 28, 27, 49, 48, 107, 0, 143, 0, 12, 10, 12, 11, 29, 27, 12, 28,
	29, 48, 12, 49, 29, 107, 29, 8, 23, 8, 24, 32, 90, 19, 32, 2, 145, 51, 115,
	116, 130, 51, 116, 20, 34, 123, 134, 20, 35, 40, 97, 21, 37, 22, 38, 0, 13,
	1, 144, 10, 13, 11, 13, 27, 13, 28, 13, 48, 13, 49, 13, 107, 13, 0, 14, 10,
	14, 11, 14, 27, 14, 28, 14, 48, 14, 49, 14, 104, 127, 107, 14, 137, 140, 0,
	15, 10, 15, 11, 15, 27, 15, 28, 15, 48, 15, 49, 15, 104, 15, 107, 15, 137,
	15, 41, 100, 91, 117, 119, 131, 120, 132, 125, 136, 133, 139, 138, 141, 25,
	44, 25, 45, 25, 46, 45, 105, 41, 101, 91, 101, 119, 101, 120, 101, 125, 101,
	133, 101, 138, 101, 41, 102, 91, 102, 99, 122, 119, 102, 120, 102, 124, 135,
	125, 102, 133, 102, 138, 102, 41, 103, 91, 103, 99, 103, 119, 103, 120, 103,
	124, 103, 125, 103, 133, 103, 138, 103, 22, 39,
}

var tmRuleLen = []int8{
	2, 1, 1, 1, 1, 5, 4, 4, 3, 3, 2, 1, 3, 1, 4, 4, 4, 2, 6, 5, 9, 3, 0, 0, 1, 1,
	1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1,
	1, 1, 1, 1, 1, 1, 1, 1, 1, 2, 0, 5, 1, 1, 1, 1, 1, 1, 2, 1, 0, 3, 3, 3, 3, 3,
	1, 3, 4, 1, 1, 5, 7, 3, 1, 1, 1, 2, 1, 1, 2, 1, 4, 3, 1, 1, 1, 0, 0,
}

var tmRuleSymbol = []int32{
	40, 40, 41, 42, 42, 42, 42, 42, 42, 42, 42, 42, 42, 42, 42, 42, 42, 42, 42,
	42, 42, 42, 43, 44, 45, 45, 45, 45, 45, 45, 45, 45, 45, 45, 45, 45, 45, 45,
	45, 45, 45, 45, 45, 45, 45, 45, 45, 45, 45, 45, 45, 45, 45, 45, 45, 45, 45,
	45, 45, 45, 45, 46, 46, 47, 48, 48, 48, 48, 48, 48, 49, 49, 50, 51, 51, 52,
	53, 53, 54, 54, 55, 56, 56, 57, 57, 58, 58, 58, 59, 60, 60, 61, 61, 61, 62,
	63, 63, 64, 65, 65, 44,
}

var tmRuleType = [...]uint32{
	0,                  // Declaration_list : Declaration_list Declaration
	0,                  // Declaration_list : Declaration
	uint32(Test),       // Test : Declaration_list
	0,                  // Declaration : Decl1
	0,                  // Declaration : Decl2
	uint32(Block),      // Declaration : '{' '-' '-' Declaration_list '}'
	uint32(Block),      // Declaration : '{' '-' '-' '}'
	uint32(Block),      // Declaration : '{' '-' Declaration_list '}'
	uint32(Block),      // Declaration : '{' '-' '}'
	uint32(Block),      // Declaration : '{' Declaration_list '}'
	uint32(Block),      // Declaration : '{' '}'
	uint32(LastInt),    // Declaration : lastInt
	uint32(Int),        // Declaration : IntegerConstant '[' ']'
	uint32(Int),        // Declaration : IntegerConstant
	uint32(TestClause), // Declaration : 'test' '{' setof_not_EOI_or_DOT_or_RBRACE_optlist '}'
	0,                  // Declaration : 'test' '(' empty1 ')'
	0,                  // Declaration : 'test' '(' foo_nonterm ')'
	uint32(TestIntClause) + uint32(InTest|InFoo)<<16, // Declaration : 'test' IntegerConstant
	uint32(EvalEmpty1),  // Declaration : 'eval' lookahead_notFooLookahead '(' expr ')' empty1
	uint32(EvalFoo),     // Declaration : 'eval' lookahead_FooLookahead '(' foo_nonterm_A ')'
	uint32(EvalFoo2),    // Declaration : 'eval' lookahead_FooLookahead '(' IntegerConstant '.' expr '+' .greedy expr ')'
	uint32(DeclOptQual), // Declaration : 'decl2' ':' QualifiedName-opt
	0,                   // lookahead_FooLookahead :
	0,                   // lookahead_notFooLookahead :
	0,                   // setof_not_EOI_or_DOT_or_RBRACE : invalid_token
	0,                   // setof_not_EOI_or_DOT_or_RBRACE : WhiteSpace
	0,                   // setof_not_EOI_or_DOT_or_RBRACE : SingleLineComment
	0,                   // setof_not_EOI_or_DOT_or_RBRACE : Identifier
	0,                   // setof_not_EOI_or_DOT_or_RBRACE : Identifier2
	0,                   // setof_not_EOI_or_DOT_or_RBRACE : IntegerConstant
	0,                   // setof_not_EOI_or_DOT_or_RBRACE : lastInt
	0,                   // setof_not_EOI_or_DOT_or_RBRACE : 'test'
	0,                   // setof_not_EOI_or_DOT_or_RBRACE : 'decl1'
	0,                   // setof_not_EOI_or_DOT_or_RBRACE : 'decl2'
	0,                   // setof_not_EOI_or_DOT_or_RBRACE : 'eval'
	0,                   // setof_not_EOI_or_DOT_or_RBRACE : 'as'
	0,                   // setof_not_EOI_or_DOT_or_RBRACE : 'if'
	0,                   // setof_not_EOI_or_DOT_or_RBRACE : "else"
	0,                   // setof_not_EOI_or_DOT_or_RBRACE : '{'
	0,                   // setof_not_EOI_or_DOT_or_RBRACE : '('
	0,                   // setof_not_EOI_or_DOT_or_RBRACE : ')'
	0,                   // setof_not_EOI_or_DOT_or_RBRACE : '['
	0,                   // setof_not_EOI_or_DOT_or_RBRACE : ']'
	0,                   // setof_not_EOI_or_DOT_or_RBRACE : '...'
	0,                   // setof_not_EOI_or_DOT_or_RBRACE : ','
	0,                   // setof_not_EOI_or_DOT_or_RBRACE : ':'
	0,                   // setof_not_EOI_or_DOT_or_RBRACE : '-'
	0,                   // setof_not_EOI_or_DOT_or_RBRACE : '->'
	0,                   // setof_not_EOI_or_DOT_or_RBRACE : '+'
	0,                   // setof_not_EOI_or_DOT_or_RBRACE : '\\'
	0,                   // setof_not_EOI_or_DOT_or_RBRACE : '_'
	0,                   // setof_not_EOI_or_DOT_or_RBRACE : 'foo_'
	0,                   // setof_not_EOI_or_DOT_or_RBRACE : 'f_a'
	0,                   // setof_not_EOI_or_DOT_or_RBRACE : multiline
	0,                   // setof_not_EOI_or_DOT_or_RBRACE : dquote
	0,                   // setof_not_EOI_or_DOT_or_RBRACE : '\''
	0,                   // setof_not_EOI_or_DOT_or_RBRACE : SharpAtID
	0,                   // setof_not_EOI_or_DOT_or_RBRACE : 'Zfoo'
	0,                   // setof_not_EOI_or_DOT_or_RBRACE : backtrackingToken
	0,                   // setof_not_EOI_or_DOT_or_RBRACE : error
	0,                   // setof_not_EOI_or_DOT_or_RBRACE : MultiLineComment
	0,                   // setof_not_EOI_or_DOT_or_RBRACE_optlist : setof_not_EOI_or_DOT_or_RBRACE_optlist setof_not_EOI_or_DOT_or_RBRACE
	0,                   // setof_not_EOI_or_DOT_or_RBRACE_optlist :
	0,                   // FooLookahead : '(' IntegerConstant '.' setof_foo_la_list ')'
	0,                   // setof_foo_la : IntegerConstant
	0,                   // setof_foo_la : 'as'
	0,                   // setof_foo_la : '.'
	0,                   // setof_foo_la : '+'
	0,                   // setof_foo_la : '\\'
	0,                   // setof_foo_la : 'foo_'
	0,                   // setof_foo_la_list : setof_foo_la_list setof_foo_la
	0,                   // setof_foo_la_list : setof_foo_la
	0,                   // empty1 :
	0,                   // foo_la : IntegerConstant '.' expr
	0,                   // foo_la : IntegerConstant 'foo_' expr
	0,                   // foo_nonterm : IntegerConstant '.' expr
	0,                   // foo_nonterm_A : IntegerConstant '.' expr
	0,                   // foo_nonterm_A : IntegerConstant 'foo_' expr
	0,                   // QualifiedName : Identifier
	0,                   // QualifiedName : QualifiedName '.' Identifier
	uint32(Decl1),       // Decl1 : 'decl1' '(' QualifiedName ')'
	uint32(Decl2),       // Decl2 : 'decl2'
	0,                   // Decl2 : If
	uint32(If),          // If : 'if' '(' O ')' Decl2
	uint32(If),          // If : 'if' '(' O ')' Decl2 "else" Decl2
	uint32(PlusExpr),    // expr : expr '+' primaryExpr
	0,                   // expr : customPlus
	0,                   // expr : primaryExpr
	0,                   // O : elem_list
	0,                   // elem_list : elem_list elem
	0,                   // elem_list : elem
	uint32(Elem),        // elem : 'f_a' .greedy
	uint32(Elem),        // elem : 'f_a' 'as'
	uint32(Elem),        // elem : 'as'
	0,                   // customPlus : '\\' primaryExpr '+' expr
	uint32(AsExpr),      // primaryExpr : primaryExpr_WithoutAs 'as' expr
	uint32(IntExpr),     // primaryExpr : IntegerConstant
	uint32(IntExpr),     // primaryExpr_WithoutAs : IntegerConstant
	0,                   // QualifiedName-opt : QualifiedName
	0,                   // QualifiedName-opt :
}

// set(follow ERROR) =
var afterErr = []token.Type{}
