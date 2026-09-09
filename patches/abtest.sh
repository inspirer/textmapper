#!/bin/sh
# Interleaved A/B benchmark of two checkouts of this repository.
#
#   patches/abtest.sh <treeA> <treeB> [rounds] [corpus dirs]
#
# Builds the parsers/js test binary of each tree, then alternates A and B for the
# given number of rounds (default 6) and compares the results with benchstat.
# Alternating runs cancels machine drift; never compare a block of A runs against a
# block of B runs. Requires TM_JS_CORPUS-style corpus directories (comma-separated).
set -e
A=$1; B=$2; N=${3:-6}; C=${4:-$TM_JS_CORPUS}
OUT=${TMPDIR:-/tmp}/abtest.$$
(cd "$A" && go test -c -o "$OUT.a.test" ./parsers/js)
(cd "$B" && go test -c -o "$OUT.b.test" ./parsers/js)
: > "$OUT.a.txt"; : > "$OUT.b.txt"
i=0
while [ $i -lt "$N" ]; do
  TM_JS_CORPUS=$C "$OUT.a.test" -test.run '^$' -test.bench 'Corpus|Parser$|Lexer$|Lookahead' -test.cpu 1 -test.count 1 | grep Benchmark >> "$OUT.a.txt"
  TM_JS_CORPUS=$C "$OUT.b.test" -test.run '^$' -test.bench 'Corpus|Parser$|Lexer$|Lookahead' -test.cpu 1 -test.count 1 | grep Benchmark >> "$OUT.b.txt"
  i=$((i+1))
done
go run golang.org/x/perf/cmd/benchstat@latest "$OUT.a.txt" "$OUT.b.txt"
