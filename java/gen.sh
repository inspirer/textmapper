#!/bin/sh

(cd tm-tool ; ant clean deploy go)

go fmt ../...
find .. -type f -name '*.go' | xargs -I '{}' goimports -w -local github.com '{}'
go build ../... && go test ../...
