#!/bin/sh

(cd tm-tool ; ant clean deploy go)

# Note: using gotip for Unicode 15.0.0
CMD=gotip

$CMD fmt ../...
find .. -type f -name '*.go' | xargs -I '{}' goimports -w -local github.com '{}'
$CMD build ../... && $CMD test ../...
