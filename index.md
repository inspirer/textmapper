---
layout: default
title: Textmapper &middot; Home
kind: home
---

Textmapper is a tool for language development. It generates bottom-up parsers with complete infrastructure from a high-level, declarative specification. Textmapper spreads the generative approach onto different aspects of language design and tries to generate as much derived, boilerplate code as possible. With a little effort you get an abstract syntax tree, code formatters, and even full-featured editor plug-ins for major IDEs.

Textmapper takes annotated context-free grammar and outputs a program able to parse the language defined by that grammar. Generated parsers are deterministic and employ LALR(1) parser tables. In grammar handling aspects it is very similar to Bison, with some additional features on top. If you are familiar with Bison, you won't get lost. Textmapper includes quite a sophisticated scanner generator (Unicode-aware, specified using regular expressions, almost flex/JFlex compatible).

Some of the features are:

* lalr(1), error recovery, operator precedence
* unicode support
* multiple input symbols
* soft keywords
* smart AST generation
* option to parse without an end-of-input marker

License:

* Textmapper - Apache 2
* IntelliJ IDEA plug-in - GNU General Public License
* Eclipse plug-in - Eclipse Public license
