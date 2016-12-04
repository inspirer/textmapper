#!/bin/sh

ant clean deploy go

go fmt ../tm-go/parsers/json/ ../tm-go/parsers/tm/ ../tm-parsers/js/ ../tm-parsers/js/ast/
go test ../tm-go/parsers/json/ ../tm-go/parsers/tm/ ../tm-parsers/js/
