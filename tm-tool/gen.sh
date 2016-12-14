#!/bin/sh

ant clean deploy go

go fmt ../tm-go/parsers/... ../tm-parsers/...
go test ../tm-go/parsers/json/ ../tm-go/parsers/tm/ ../tm-parsers/js/
