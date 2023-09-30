#!/bin/sh

go install ./cmd/textmapper

echo 'Regenerating js'
(cd parsers/js; textmapper generate --compat)
echo 'Regenerating test'
(cd parsers/test; textmapper generate --compat)
echo 'Regenerating tm'
(cd parsers/tm; textmapper generate --compat)
echo 'Regenerating json'
(cd parsers/json; textmapper generate --compat)
echo 'Regenerating simple'
(cd parsers/simple; textmapper generate --compat)

go fmt ./...
find . -type f -name '*.go' | xargs -I '{}' goimports -w -local github.com '{}'
go build ./... && go test ./...
