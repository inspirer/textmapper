#!/bin/sh

go run ../cmd/textmapper generate -o json/ json/json.tm
go run ../cmd/textmapper generate -o json_flex/ json_flex/json.tm
clang-format -i --style=google json/json_lexer* json/json_parser* markup/markup* json_flex/json_lexer*
buildifier json/BUILD json_flex/BUILD markup/BUILD ./BUILD
