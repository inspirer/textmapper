#!/bin/sh

# C++
go run ../cmd/textmapper generate -o cpp/json/ cpp/json/json.tm
go run ../cmd/textmapper generate -o cpp/json_flex/ cpp/json_flex/json.tm
clang-format -i --style=google cpp/json/json_lexer* cpp/json/json_parser* cpp/markup/markup* cpp/json_flex/json_lexer*
buildifier cpp/json/BUILD cpp/json_flex/BUILD cpp/markup/BUILD ./BUILD

# TypeScript
go run ../cmd/textmapper generate -o ts/json/ ts/json/json.tm
