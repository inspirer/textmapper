---
layout: default
title: Textmapper &middot; Home
kind: home
---

### Introduction

Textmapper is a tool for language development. It takes a formal language
specification (a grammar) and generates code to parse that language - a
lexer/parser and a set of AST classes. Textmapper supports EBNF-like production
rules and can map a grammar to an AST, or even derive an AST automatically
from the grammar. This means you can get code transforming a sequence of
characters into an AST without any additional coding work. Textmapper has
a sophisticated scanner generator built-in.

Textmapper supports multiple target languages. By avoiding code in the grammar,
Textmapper gives you exactly the same parsing functionality in different
languages as well as clean and concise grammars. By default, the generated
parser code has no runtime dependencies and contains all required algorithms.

### Motivation

The idea behind Textmapper was to come up with an easy-to-use, declarative
tool for bottom-up parsing algorithms (like LALR, IELR or GLR), leveraging
their power to the full. It is similar to what ANTLR is for LL languages.
Textmapper was created with the belief that the bottom-up approach works
better than top-down approaches in most cases, and the only reason people
still tend to use the latter is the lack of proper tools. Textmapper is
going to fill this gap.

### Tooling

Textmapper comes as a separate command-line tool, with integration plugins
for VS Code and IntelliJ IDEA.

### License

Textmapper tool is distributed under the [Apache 2.0 License](http://www.apache.org/licenses/LICENSE-2.0).

The plug-ins for IntelliJ IDEA is made available under the General Public License (version 3 or above).
