#!/bin/sh

go run ../../cmd/textmapper generate -o json/ json/json.tm
clang-format -i --style=google json/lexer* markup/markup*
buildifier json/BUILD markup/BUILD
