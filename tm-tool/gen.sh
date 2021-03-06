#!/bin/sh

ant clean deploy go

go fmt ../tm-go/... ../tm-parsers/...
go build ../tm-go/... ../tm-parsers/... && go test ../tm-go/... ../tm-parsers/...
find ../tm-go -type f -name '*.go' | xargs -I '{}' goimports -w '{}'
find ../tm-parsers -type f -name '*.go' | xargs -I '{}' goimports -w '{}'