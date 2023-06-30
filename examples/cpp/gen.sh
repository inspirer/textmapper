#!/bin/sh

go run ../../cmd/textmapper generate -o json/ -compat json/json.tm
clang-format -i --style=google json/lexer* json/parser* markup/markup*
buildifier json/BUILD markup/BUILD ./BUILD
