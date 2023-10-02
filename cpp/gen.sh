#!/bin/sh

go run ../cmd/textmapper generate -o json/ -compat json/json.tm
clang-format -i --style=google json/json_lexer* json/json_parser* markup/markup*
buildifier json/BUILD markup/BUILD ./BUILD
