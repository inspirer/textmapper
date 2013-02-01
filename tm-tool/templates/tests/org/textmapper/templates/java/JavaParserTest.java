package org.textmapper.templates.java;

import org.junit.Test;

import java.io.IOException;
import java.io.StringReader;
import java.util.*;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

/**
 * evgeny, 8/10/12
 */
public class JavaParserTest {

    @Test
    public void testAnno() throws Exception {
        AstNode tree = parse(
                "package java.lang.annotation;\n" +
                        "@Documented\n" +
                        "@Retention(RetentionPolicy.RUNTIME)\n" +
                        "@Target(ElementType.ANNOTATION_TYPE)\n" +
                        "public @interface Retention {\n" +
                        "   RetentionPolicy value();\n" +
                        "}\n");
        assertTree(
                "CompilationUnit\n" +
                        "  PackageDeclaration\n" +
                        "    kw_package\n" +
                        "    QualifiedIdentifier\n" +
                        "      'java' '.' 'lang' '.' 'annotation'\n" +
                        "    ';'\n" +
                        "  AnnotationTypeDeclaration\n" +
                        "    Modifiers\n" +
                        "      Annotation\n" +
                        "        '@' 'Documented'\n" +
                        "      Annotation\n" +
                        "        '@'\n" +
                        "        'Retention'\n" +
                        "        '('\n" +
                        "        QualifiedIdentifier\n" +
                        "          'RetentionPolicy' '.' 'RUNTIME'\n" +
                        "        ')'\n" +
                        "      Annotation\n" +
                        "        '@'\n" +
                        "        'Target'\n" +
                        "        '('\n" +
                        "        QualifiedIdentifier\n" +
                        "          'ElementType' '.' 'ANNOTATION_TYPE'\n" +
                        "        ')'\n" +
                        "      kw_public\n" +
                        "    '@'\n" +
                        "    kw_interface\n" +
                        "    'Retention'\n" +
                        "    AnnotationTypeBody\n" +
                        "      '{'\n" +
                        "      AnnotationTypeMemberDeclaration_optlist\n" +
                        "        AnnotationTypeMemberDeclaration\n" +
                        "          'RetentionPolicy' 'value' '(' ')' ';'\n" +
                        "      '}'", tree);
    }


    @Test
    public void testTripleGenerics1() throws Exception {
        AstNode tree = parse(
                "class MapNested<T extends List<Map<Integer,Integer>>> { \n" +
                        "}");
        assertTree(
                "ClassDeclaration\n" +
                        "  kw_class\n" +
                        "  'MapNested'\n" +
                        "  TypeParameters\n" +
                        "    '<'\n" +
                        "    TypeParameter1\n" +
                        "      'T'\n" +
                        "      kw_extends\n" +
                        "      ReferenceType1\n" +
                        "        'List'\n" +
                        "        '<'\n" +
                        "        DeeperTypeArgument\n" +
                        "          'Map'\n" +
                        "          '<'\n" +
                        "          TypeArgumentList\n" +
                        "            'Integer' ',' 'Integer'\n" +
                        "        '>>>'\n" +
                        "  ClassBody\n" +
                        "    '{' '}'", tree);
    }

	@Test
	public void testTripleGenerics2() throws Exception {
		AstNode tree = parse(
				"class MapNested<X, Q extends List<Map<X, ? super X>>> { }");
		assertTree(
				"ClassDeclaration\n" +
						"  kw_class\n" +
						"  'MapNested'\n" +
						"  TypeParameters\n" +
						"    '<'\n" +
						"    'X'\n" +
						"    ','\n" +
						"    TypeParameter1\n" +
						"      'Q'\n" +
						"      kw_extends\n" +
						"      ReferenceType1\n" +
						"        'List'\n" +
						"        '<'\n" +
						"        DeeperTypeArgument\n" +
						"          'Map'\n" +
						"          '<'\n" +
						"          TypeArgumentList\n" +
						"            'X'\n" +
						"            ','\n" +
						"            Wildcard\n" +
						"              '?' kw_super 'X'\n" +
						"        '>>>'\n" +
						"  ClassBody\n" +
						"    '{' '}'", tree);
	}

	@Test
    public void testGenericArgs() throws Exception {
        AstNode tree = parse("interface ConvertibleTo<T> {\n" +
                "    T convert();\n" +
                "}\n" +
                "class ReprChange<T extends ConvertibleTo<S>,\n" +
                "                 S extends ConvertibleTo<T>> { \n" +
                "    T t; \n" +
                "    void set(S s) { int q = 1 + 2 + 3 + 4*5; t = s.convert();    } \n" +
                "    S get()       { return t.convert(); } \n" +
                "}");
        assertTree(
                "TypeDeclaration_list\n" +
                        "  InterfaceDeclaration\n" +
                        "    kw_interface\n" +
                        "    'ConvertibleTo'\n" +
                        "    TypeParameters\n" +
                        "      '<'\n" +
                        "      TypeParameter1\n" +
                        "        'T' '>'\n" +
                        "    InterfaceBody\n" +
                        "      '{'\n" +
                        "      InterfaceMemberDeclaration_optlist\n" +
                        "        AbstractMethodDeclaration\n" +
                        "          MethodHeader\n" +
                        "            'T' 'convert' '(' ')'\n" +
                        "          ';'\n" +
                        "      '}'\n" +
                        "  ClassDeclaration\n" +
                        "    kw_class\n" +
                        "    'ReprChange'\n" +
                        "    TypeParameters\n" +
                        "      '<'\n" +
                        "      TypeParameter\n" +
                        "        'T'\n" +
                        "        kw_extends\n" +
                        "        GenericType\n" +
                        "          'ConvertibleTo'\n" +
                        "          TypeArguments\n" +
                        "            '<' 'S' '>'\n" +
                        "      ','\n" +
                        "      TypeParameter1\n" +
                        "        'S'\n" +
                        "        kw_extends\n" +
                        "        ReferenceType1\n" +
                        "          'ConvertibleTo' '<' 'T' '>>'\n" +
                        "    ClassBody\n" +
                        "      '{'\n" +
                        "      ClassBodyDeclaration_optlist\n" +
                        "        FieldDeclaration\n" +
                        "          'T'\n" +
                        "          VariableDeclaratorId\n" +
                        "            't'\n" +
                        "          ';'\n" +
                        "        MethodDeclaration\n" +
                        "          MethodHeader\n" +
                        "            kw_void\n" +
                        "            'set'\n" +
                        "            '('\n" +
                        "            FormalParameter\n" +
                        "              'S'\n" +
                        "              VariableDeclaratorId\n" +
                        "                's'\n" +
                        "            ')'\n" +
                        "          Block\n" +
                        "            '{'\n" +
                        "            BlockStatement_optlist\n" +
                        "              LocalVariableDeclarationStatement\n" +
                        "                LocalVariableDeclaration\n" +
                        "                  kw_int\n" +
                        "                  VariableDeclarator\n" +
                        "                    VariableDeclaratorId\n" +
                        "                      'q'\n" +
                        "                    '='\n" +
                        "                    ArithmeticExpressionNotName\n" +
                        "                      ArithmeticExpressionNotName\n" +
                        "                        ArithmeticExpressionNotName\n" +
                        "                          IntegerLiteral '+' IntegerLiteral\n" +
                        "                        '+'\n" +
                        "                        IntegerLiteral\n" +
                        "                      '+'\n" +
                        "                      ArithmeticExpressionNotName\n" +
                        "                        IntegerLiteral '*' IntegerLiteral\n" +
                        "                ';'\n" +
                        "              ExpressionStatement\n" +
                        "                Assignment\n" +
                        "                  't'\n" +
                        "                  '='\n" +
                        "                  MethodInvocation\n" +
                        "                    QualifiedIdentifier\n" +
                        "                      's' '.' 'convert'\n" +
                        "                    '('\n" +
                        "                    ')'\n" +
                        "                ';'\n" +
                        "            '}'\n" +
                        "        MethodDeclaration\n" +
                        "          MethodHeader\n" +
                        "            'S' 'get' '(' ')'\n" +
                        "          Block\n" +
                        "            '{'\n" +
                        "            BlockStatement_optlist\n" +
                        "              ReturnStatement\n" +
                        "                kw_return\n" +
                        "                MethodInvocation\n" +
                        "                  QualifiedIdentifier\n" +
                        "                    't' '.' 'convert'\n" +
                        "                  '('\n" +
                        "                  ')'\n" +
                        "                ';'\n" +
                        "            '}'\n" +
                        "      '}'", tree);
    }

    @Test
    public void testGenericMethod() throws Exception {
        AstNode tree = parse("class CollectionConverter {\n" +
                "    <T> List<T> toList(Collection<T> c) { c = (Collection<?>) null;}\n" +
                "}\n");
        assertTree(
                "ClassDeclaration\n" +
                        "  kw_class\n" +
                        "  'CollectionConverter'\n" +
                        "  ClassBody\n" +
                        "    '{'\n" +
                        "    ClassBodyDeclaration_optlist\n" +
                        "      MethodDeclaration\n" +
                        "        MethodHeader\n" +
                        "          TypeParameters\n" +
                        "            '<'\n" +
                        "            TypeParameter1\n" +
                        "              'T' '>'\n" +
                        "          GenericType\n" +
                        "            'List'\n" +
                        "            TypeArguments\n" +
                        "              '<' 'T' '>'\n" +
                        "          'toList'\n" +
                        "          '('\n" +
                        "          FormalParameter\n" +
                        "            GenericType\n" +
                        "              'Collection'\n" +
                        "              TypeArguments\n" +
                        "                '<' 'T' '>'\n" +
                        "            VariableDeclaratorId\n" +
                        "              'c'\n" +
                        "          ')'\n" +
                        "        Block\n" +
                        "          '{'\n" +
                        "          BlockStatement_optlist\n" +
                        "            ExpressionStatement\n" +
                        "              Assignment\n" +
                        "                'c'\n" +
                        "                '='\n" +
                        "                CastExpression\n" +
                        "                  '('\n" +
                        "                  'Collection'\n" +
                        "                  TypeArguments\n" +
                        "                    '<' '?' '>'\n" +
                        "                  ')'\n" +
                        "                  NullLiteral\n" +
                        "              ';'\n" +
                        "          '}'\n" +
                        "    '}'", tree);

    }

    @Test
    public void testCast1() throws Exception {
        AstNode tree = parse(
                "class A {\n" +
                        "        static final int q = 44;\n" +
                        "        int f = ((int)A.q); }");
        assertTree("ClassDeclaration\n" +
                "  kw_class\n" +
                "  'A'\n" +
                "  ClassBody\n" +
                "    '{'\n" +
                "    ClassBodyDeclaration_optlist\n" +
                "      FieldDeclaration\n" +
                "        Modifiers\n" +
                "          kw_static kw_final\n" +
                "        kw_int\n" +
                "        VariableDeclarator\n" +
                "          VariableDeclaratorId\n" +
                "            'q'\n" +
                "          '='\n" +
                "          IntegerLiteral\n" +
                "        ';'\n" +
                "      FieldDeclaration\n" +
                "        kw_int\n" +
                "        VariableDeclarator\n" +
                "          VariableDeclaratorId\n" +
                "            'f'\n" +
                "          '='\n" +
                "          ParenthesizedExpression\n" +
                "            '('\n" +
                "            CastExpression\n" +
                "              '('\n" +
                "              kw_int\n" +
                "              ')'\n" +
                "              QualifiedIdentifier\n" +
                "                'A' '.' 'q'\n" +
                "            ')'\n" +
                "        ';'\n" +
                "    '}'", tree);
    }

    @Test
    public void testExpr1() throws Exception {
        AstNode tree = parse(
                "class A {\n" +
                        "        static final int q = 44;\n" +
                        "        int f = (A.q); }");
        assertTree("ClassDeclaration\n" +
                "  kw_class\n" +
                "  'A'\n" +
                "  ClassBody\n" +
                "    '{'\n" +
                "    ClassBodyDeclaration_optlist\n" +
                "      FieldDeclaration\n" +
                "        Modifiers\n" +
                "          kw_static kw_final\n" +
                "        kw_int\n" +
                "        VariableDeclarator\n" +
                "          VariableDeclaratorId\n" +
                "            'q'\n" +
                "          '='\n" +
                "          IntegerLiteral\n" +
                "        ';'\n" +
                "      FieldDeclaration\n" +
                "        kw_int\n" +
                "        VariableDeclarator\n" +
                "          VariableDeclaratorId\n" +
                "            'f'\n" +
                "          '='\n" +
                "          ParenthesizedExpression\n" +
                "            '('\n" +
                "            QualifiedIdentifier\n" +
                "              'A' '.' 'q'\n" +
                "            ')'\n" +
                "        ';'\n" +
                "    '}'", tree);
    }

    @Test
    public void testExpr2() throws Exception {
        AstNode tree = parse(
                "    class A {\n" +
                        "        static final boolean q = 1 + 2 << 3 < 4 + 4 * 5 / 6;\n" +
                        "        int f = (A.q?1:(3) + 3*((A[])(null))[1].f + (this instanceof A ?1:0));\n" +
                        "    }\n");
        assertTree("ClassDeclaration\n" +
                "  kw_class\n" +
                "  'A'\n" +
                "  ClassBody\n" +
                "    '{'\n" +
                "    ClassBodyDeclaration_optlist\n" +
                "      FieldDeclaration\n" +
                "        Modifiers\n" +
                "          kw_static kw_final\n" +
                "        kw_boolean\n" +
                "        VariableDeclarator\n" +
                "          VariableDeclaratorId\n" +
                "            'q'\n" +
                "          '='\n" +
                "          RelationalExpressionNotName\n" +
                "            ArithmeticExpressionNotName\n" +
                "              ArithmeticExpressionNotName\n" +
                "                IntegerLiteral '+' IntegerLiteral\n" +
                "              '<<'\n" +
                "              IntegerLiteral\n" +
                "            '<'\n" +
                "            ArithmeticExpressionNotName\n" +
                "              IntegerLiteral\n" +
                "              '+'\n" +
                "              ArithmeticExpressionNotName\n" +
                "                ArithmeticExpressionNotName\n" +
                "                  IntegerLiteral '*' IntegerLiteral\n" +
                "                '/'\n" +
                "                IntegerLiteral\n" +
                "        ';'\n" +
                "      FieldDeclaration\n" +
                "        kw_int\n" +
                "        VariableDeclarator\n" +
                "          VariableDeclaratorId\n" +
                "            'f'\n" +
                "          '='\n" +
                "          ParenthesizedExpression\n" +
                "            '('\n" +
                "            ConditionalExpressionNotName\n" +
                "              QualifiedIdentifier\n" +
                "                'A' '.' 'q'\n" +
                "              '?'\n" +
                "              IntegerLiteral\n" +
                "              ':'\n" +
                "              ArithmeticExpressionNotName\n" +
                "                ArithmeticExpressionNotName\n" +
                "                  ParenthesizedExpression\n" +
                "                    '(' IntegerLiteral ')'\n" +
                "                  '+'\n" +
                "                  ArithmeticExpressionNotName\n" +
                "                    IntegerLiteral\n" +
                "                    '*'\n" +
                "                    FieldAccess\n" +
                "                      ArrayAccess\n" +
                "                        ParenthesizedExpression\n" +
                "                          '('\n" +
                "                          CastExpression\n" +
                "                            '('\n" +
                "                            'A'\n" +
                "                            Dims$1\n" +
                "                              '[' ']'\n" +
                "                            ')'\n" +
                "                            ParenthesizedExpression\n" +
                "                              '(' NullLiteral ')'\n" +
                "                          ')'\n" +
                "                        '['\n" +
                "                        IntegerLiteral\n" +
                "                        ']'\n" +
                "                      '.'\n" +
                "                      'f'\n" +
                "                '+'\n" +
                "                ParenthesizedExpression\n" +
                "                  '('\n" +
                "                  ConditionalExpressionNotName\n" +
                "                    RelationalExpressionNotName\n" +
                "                      kw_this kw_instanceof 'A'\n" +
                "                    '?'\n" +
                "                    IntegerLiteral\n" +
                "                    ':'\n" +
                "                    IntegerLiteral\n" +
                "                  ')'\n" +
                "            ')'\n" +
                "        ';'\n" +
                "    '}'", tree);
    }

    private void assertTree(String expected, AstNode actual) {
        StringBuilder sb = new StringBuilder();
        actual.toString(sb, 0);
        assertEquals(expected, sb.toString());
    }

    private AstNode parse(final String text) throws IOException {
        final TreeRecorder recorder = new TreeRecorder();
        JavaLexer.ErrorReporter reporter = new JavaLexer.ErrorReporter() {
            public void error(int start, int end, int line, String s) {
                fail(line + ": " + s);
            }
        };
        JavaParser p = new JavaParser(reporter) {
            @Override
            protected void reduce(int rule) {
                int before = lapg_head;
                super.reduce(rule);
                int rulelen = lapg_head - before + 1;
                JavaLexer.LapgSymbol r = lapg_m[lapg_head];
                recorder.add(lapg_syms[r.symbol], text.substring(r.offset, r.endoffset), r.offset, r.endoffset, rulelen == 1);
            }

            @Override
            protected void shift() throws IOException {
                recorder.add(lapg_syms[lapg_n.symbol], text.substring(lapg_n.offset, lapg_n.endoffset), lapg_n.offset, lapg_n.endoffset, false);
                super.shift();
            }
        };
        JavaLexer lexer = new JavaLexer(new StringReader(text), reporter);
        try {
            p.parseCompilationUnit(lexer);
        } catch (JavaParser.ParseException e) {
            e.printStackTrace();
            fail(e.toString());
        }
        return recorder.getRoot();
    }

    private static Set<String> doNotMerge = new HashSet<String>();

    static {
        doNotMerge.add("ArithmeticExpressionNotName");
        doNotMerge.add("RelationalExpressionNotName");
        doNotMerge.add("LogicalExpressionNotName");
    }

    private static class TreeRecorder {

        private Stack<AstNode> stack = new Stack<AstNode>();

        public void add(String symbol, String text, int offset, int endoffset, boolean simpleRule) {
            if (endoffset == offset) return;
            AstNode node = new AstNode(symbol, text, offset, endoffset);
            while (!stack.isEmpty() && node.contains(stack.peek())) {
                node.addChild(stack.pop());
            }
            if (node.children != null) {
                Collections.reverse(node.children);
            }
            if (node.children != null && node.children.get(0).symbol.equals(symbol) && !doNotMerge.contains(node.symbol)) {
                // flatten lists
                AstNode first = node.children.get(0);
                node.children.remove(first);
                if (first.children != null) {
                    node.children.addAll(0, first.children);
                }
            } else if (simpleRule && node.children != null && node.children.size() == 1) {
                // get rid of simple reduces
                AstNode first = node.children.get(0);
                if (text.substring(0, first.offset - node.offset).trim().isEmpty() &&
                        text.substring(text.length() - (node.endoffset - first.endoffset)).trim().isEmpty()) {
                    node = first;
                }
            }
            stack.push(node);
        }

        public AstNode getRoot() {
            assertEquals(1, stack.size());
            return stack.peek();
        }
    }

    private static class AstNode {
        String symbol;
        String text;
        int offset;
        int endoffset;
        List<AstNode> children;

        private AstNode(String symbol, String text, int offset, int endoffset) {
            this.symbol = symbol;
            this.text = text;
            this.offset = offset;
            this.endoffset = endoffset;
        }

        public boolean contains(AstNode inner) {
            return offset <= inner.offset && endoffset >= inner.endoffset;
        }

        public void addChild(AstNode node) {
            if (children == null) {
                children = new ArrayList<AstNode>();
            }
            children.add(node);
        }

        public void toString(StringBuilder sb, int indent) {
            if (symbol.equals("Identifier")) {
                sb.append("'").append(text).append("'");
            } else {
                sb.append(symbol);
            }
            if (children != null) {
                boolean nested = false;
                for (AstNode child : children) {
                    if (child.children != null) {
                        nested = true;
                        break;
                    }
                }
                if (nested) {
                    for (AstNode child : children) {
                        newline(sb, indent + 1);
                        child.toString(sb, indent + 1);
                    }
                } else {
                    boolean first = true;
                    for (AstNode child : children) {
                        if (first) {
                            newline(sb, indent + 1);
                            first = false;
                        } else {
                            sb.append(' ');
                        }
                        child.toString(sb, indent + 1);
                    }
                }
            }
        }

        private void newline(StringBuilder sb, int indent) {
            sb.append('\n');
            while (indent > 0) {
                sb.append("  ");
                indent--;
            }
        }

        @Override
        public String toString() {
            if (children == null) {
                return symbol;
            }
            return symbol + "(" + children.size() + ")";
        }
    }
}
