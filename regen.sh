#!/bin/sh

go install ./cmd/textmapper

echo 'Regenerating js'
(cd parsers/js; textmapper generate)
echo 'Regenerating test'
(cd parsers/test; textmapper generate)
echo 'Regenerating tm'
(cd parsers/tm; textmapper generate)
echo 'Regenerating json'
(cd parsers/json; textmapper generate)
echo 'Regenerating simple'
(cd parsers/simple; textmapper generate)

go fmt ./...
find . -type f -name '*.go' | xargs -I '{}' goimports -w -local github.com,go.lsp.dev,go.uber.org '{}'
go build ./... && go test ./...
