# TextMapper

[![Build Status](https://secure.travis-ci.org/inspirer/textmapper.png)](http://travis-ci.org/inspirer/textmapper)

## Introduction

TextMapper is a tool for language development. It generates bottom-up parsers with complete infrastructure from a high-level, declarative specification. TextMapper spreads the generative approach onto different aspects of language design and tries to generate as much derived, boilerplate code as possible. With a little effort you get an abstract syntax tree, code formatters, and even full-featured editor plug-ins for major IDEs.

TextMapper takes an annotated context-free grammar and outputs a program able to parse the language defined by the grammar. Generated parsers are deterministic and employ LALR(1) parser tables. In grammar handling aspects it is very similar to Bison, with some additional features on top. If you are familiar with Bison, you won't get lost. TextMapper includes quite a sophisticated scanner generator (Unicode-aware, specified using regular expressions, almost flex/JFlex compatible).
