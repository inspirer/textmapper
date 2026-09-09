# Parser performance: next steps

This file captures the state of the September 2026 performance work on the Go
target: what landed, what was measured as inconclusive on Apple M4 and needs to
be re-validated on x86, and ideas that were not tried. The provisional changes
live under `patches/` as complete patches (generator + regenerated parsers) that
apply on top of `main` with `git apply patches/NN-*.patch`.

Everything below was measured on an Apple M4 (arm64, 16 MB L2 per cluster, Go
1.26). Numbers that depend on cache sizes or on the x86 memory subsystem are
marked as such; they are the ones to re-measure.

## 1. How to validate a change

1. Corpora. Any directory tree of JavaScript/TypeScript works; the numbers in this
   file come from `~/projects/openclaw` (46 MB) and `~/projects/vscode` (147 MB).
   Files with syntax errors are excluded automatically.

2. Equivalence. A change to the generator must not change what a parser produces.
   Run before and after and compare the two lines:

       TM_JS_CORPUS=dir1,dir2 go test -run TestCorpusHash -v ./parsers/js | grep HASH

   `EVENTHASH` covers listener events, syntax-error ranges and return values;
   `TOKENHASH` covers token kinds, offsets, lines and lexer states. Both include
   mutated inputs (dropped chunks, injected garbage, truncation, invalid UTF-8) so
   that error recovery is exercised. Table-only changes (packing, encodings) must
   keep `EVENTHASH` identical. Baseline of `main` (commit 122b40f4 plus this
   file) on `~/projects/openclaw,~/projects/vscode`:

       EVENTHASH b8d58a7aa1ebe6b028dd89b36a485efff23a2a7ee41fccd00957225546d1b3d4 events=51137959 errors=20343
       TOKENHASH ba694c0b98617f9f4b9643ef22048fe967d579088427b1db7fd907a6639a2942 tokens=49137267

3. Timing. Machine noise is larger than most effects. Always interleave the two
   binaries and compare with benchstat:

       patches/abtest.sh <tree-A> <tree-B> 6 ~/projects/openclaw,~/projects/vscode

   where each tree is a checkout (e.g. `git worktree add ../base main`). Use
   `-cpu 1`, at least 6 rounds, and treat anything with p > 0.05 or under 2% as
   inconclusive. The micro benchmarks (`go test -bench . ./parsers/js`) are useful
   for instruction-level changes but hide all cache effects: the input is 396 bytes.

4. Generation time. `textmapper generate` prints `lalr:` (table construction and
   packing) and `text:` (template rendering and gofmt) for each grammar.

## 2. Provisional patches to validate on x86

Patches are independent unless stated. Each one was regenerated for all grammars
in the repository and passes `go test ./...`.

### 01-js-minimize-dfa.patch

Turns on `minimizeDFA = true` for `parsers/js/js.tm`.

- Effect: 9310 -> 5860 states, JS tables 1190 KB -> 713 KB, `lalr:` phase about 2x
  faster (fewer rows to pack).
- M4 result: corpus parser **+1.6%** slower, lookahead +3.4% (p = 0.002), events
  identical. The 1.1 MB table already fits in L2 there, so the smaller table buys
  nothing and the merged states change which rows share defaults, adding a few
  probe misses.
- On x86 (typically 1-2 MB L2, 32-64 KB L1D) the smaller table may win. Decide by
  the corpus parser benchmark; if it is neutral, prefer it for binary size.
- Note: the minimization itself was made sound in `lalr/minimize.go` (start, final
  and marker states are never merged). That fix is on `main`.

### 02-per-state-goto-bloom.patch

Lemon-style layout for nonterminal transitions: instead of one packed row per
nonterminal indexed by state (which shares the big action table), one row per
*state* listing only its exceptions from `tmDefGoto`, packed into separate
`tmGotoTable`/`tmGotoCheck` arrays, plus a 32-bit Bloom filter per state
(`tmGotoBloom`) so that a zero bit proves the default applies without probing.

- Noise-free effect (JS, 4 MB corpus): goto probes 2.11 M -> 0.83 M (-61%), the
  probed region shrinks from 1109 KB to 106 KB, action and goto probes stop evicting
  each other.
- M4 result: **no measurable change** (corpus parser 1.902 s vs 1.905 s, p = 0.49),
  lookahead +0.8%. The probes were already cheap because everything is in L2.
- Changes `lalr.DisplacementEnc` (new fields `GotoTable`, `GotoCheck`, `GotoBloom`,
  `BloomBits`, `NoGoto`; `Goto` becomes per-state) and both the Go and the C++
  parser templates. Also removes a latent hazard: the old `-syms` sentinel for
  empty goto rows was not excluded from the packer's used bases.
- Expect this to matter more on x86 with a smaller L2 and in processes that keep
  other data hot. Measure `Corpus/parser` and `Lookahead`.

### 03-unsigned-check-columns.patch (includes 02)

Emits the `check` columns of packed tables as `uint8`/`uint16` instead of `int16`
(`gen.checkType`/`checkValues`; the "unused" marker -1 becomes the maximum value
of the type, which no key can take). With 02, the action check column holds
terminal indices (< 255 for JS) and the goto check holds nonterminal indices, so
`tmCheck` for JS drops from 554 KB to 277 KB.

- Not measured for speed at all. Narrower loads can cost a zero-extension in the
  probe; the table halves. Measure on top of 02 (A = 02, B = 03).
- Only the Go template is changed; the C++ template keeps signed types.

### 04-js-shift-free-default-reductions.patch

Adds the grammar option `defaultReduceShiftFree` (with `defaultReduce = true`):
Bison-style default reductions (replace "error" entries of a state by its most
common reduction) but only in states that have no shift actions, which keeps the
parser from ever looking past an erroneous token because of a default reduction.
The patch enables it for `parsers/js`.

- Effect: JS action pairs 246 k -> 179 k, tables 1190 KB -> 687 KB. All repository
  tests pass. Valid input parses identically.
- Semantic change on **invalid** input: `reduceAll` (used by JS semicolon
  insertion) reports a different rejection state, and the listener sees extra
  reduce events before an error is reported, so `EVENTHASH` differs on the mutated
  inputs. Full `defaultReduce = true` additionally breaks four JS tests
  (`if (true)` gets a semicolon inserted and parses as `if (true);`).
- M4 result: corpus parser -4.3%, inconclusive (p > 0.05, measured on a loaded
  machine). Worth an idle-machine measurement on both architectures; if it wins,
  consider it per grammar, and consider the zero-semantics variant in section 3.

### 05-gen-preformatted-tables.patch

`textmapper generate` spends ~75% of its `text:` phase running go/printer over
the 3-4 MB `parser_tables.go`. The integer arrays are already gofmt-canonical;
only keyed entries and trailing comments need alignment. The patch marks the two
table templates as pre-formatted, emits `\v` where gofmt would align, and expands
them with the same `text/tabwriter` configuration gofmt uses (`gen.AlignGo`),
replicating go/printer's section-break heuristic (`alignGoBreaks`). A new
`-checkfmt` flag verifies canonicality by running gofmt and comparing.

- Effect: `text:` for js.tm 0.88 s -> 0.10 s; output byte-identical for all five
  grammars (`-checkfmt` passes).
- Risk: mirrors ~120 lines of go/printer behavior; a future Go release could
  change alignment rules. `-checkfmt` turns that into a loud failure. Overlay
  templates (`-i` include dirs) fall back to gofmt automatically.
- Independent of architecture; needs a code review rather than a benchmark.

### 06-gen-parallel-files.patch (includes 05)

Renders the generated files of a grammar concurrently (bounded by GOMAXPROCS) and
writes them in order. `text:` 0.10 s -> 0.05 s for js.tm. `go test -race ./gen/`
is clean; the only shared mutable state (`cache.nodeIDs`) is now mutex-protected.

### patches/reference/neon-scanset

Hand-written NEON scanner (`scanSet(src, nibbleTable) int`) with a pure-Go
fallback, from the lexer investigation. On the JS corpus it measured 0 +/- 2%
against the 8-bytes-per-iteration table scan that landed (section 4), because
runs average 10 bytes and 67% are 8 bytes or shorter. Kept as a starting point
for an AVX2 variant (shufti-style nibble lookup with `vpshufb`) if a corpus with
long runs (minified bundles, long string literals) ever matters. Go 1.26's `simd`
package is amd64-only and may make the assembly unnecessary there.

## 3. Ideas not prototyped, or measured and rejected

Ideas with potential, in rough order of expected value:

- **Interleave `tmGoto` and `tmDefGoto`.** 74.5% of nonterminal gotos take the
  default path, which today touches two arrays indexed by the same nonterminal.
  A `[nonterm]struct{base int32; def int16}` (9 KB for JS) halves the cache lines
  on that path. Cheap to try; unmeasured.
- **Interleave `tmTable` and `tmCheck`** into one `[]uint32` (check in the low 16
  bits, value in the high 16) so a probe touches one cache line, and **pad the
  table** by `numStates` entries on both ends so that `pos` is always in range and
  the two explicit bounds comparisons disappear. Static instruction counts:
  `gotoNT` 134 -> 118, `reduceAll` 240 -> 235. Corpus timings on M4 were -4.8% and
  -6.4% but with p > 0.14 on a loaded machine; not retried on the idle machine.
  Conflicts with patch 03 (uniform 32-bit cells vs. a uint8 check column).
- **Zero-semantics default reductions.** Keep the fully default-reduced table
  (105 k slots for JS, 420 KB) plus a per-state bitmap of acceptable terminals
  (158 bits = 20 B per state, dedupes heavily) consulted only on the `tmDefAct`
  fallback path. Preserves exact error detection for `reduceAll`, lookaheads and
  recovery. Estimated ~570 KB vs 1190 KB for JS. Adds one dependent load on the
  fallback path, so it needs a speed check. Not prototyped.
- **Keyword recognition without the hash.** A Go `switch` on the identifier
  string (compiled to length dispatch + comparisons) measured +3% lexer time over
  the hash switch in the lexer investigation and would delete the `string_switch`
  machinery. The rune-based hash is still used by the TypeScript and C++ targets.
- **PGO.** `go build -pgo` with a profile from the corpus gave -1.6% to -5.8% on
  the parser for free: it inlines `TokenStream.next` into `parse`, `reduceAll`
  into `next` and `flushSlow` into `flush`, which the templates cannot force.
  Worth documenting for users of generated parsers (a `default.pgo` in the main
  package is picked up automatically).
- **Generation: `buildLA`** is ~22% of `lalr.Compile` after the packing fix, and
  `compiler.gotoState` (a linear scan over `[]*state`) is 37% of it; a parallel
  `shiftSyms []Sym` plus binary search would fix that. Then `updateFollow` /
  `graph.Transpose` allocations and `computeStates`' `BitSet.Slice` buffers.
- **tree-sitter style small-state rows**: a scanned key/value list for action
  rows with very few pairs instead of placing them in the comb vector. Dense rows
  would be worse here (158 terminals x 5767 rows), so only the small-row variant
  is interesting; unmeasured.
- **String tables.** Emitting the big tables as string literals decoded at init
  cut `go build ./parsers/js` from 0.55 s to 0.15 s and the source by 31%, but
  cost ~10% parser runtime (the decode allocates and the compiler loses array
  bounds knowledge). Rejected for runtime; could be revisited for build time
  with `unsafe` reinterpretation if alignment can be guaranteed.

Measured and rejected (do not retry without a new angle):

- Bison's row ordering in the packer (width desc, then tally): <= 0.8% smaller
  tables. The packer's occupancy already equals Bison's (53.5% vs 53.4%).
- Row subsumption in the packer: 0 additional hits.
- Terminal renumbering by frequency (a `yytranslate` table): tables get larger;
  declaration order is already 22% better than random.
- 16-byte stack entries (int32 offsets): 2-4% slower; the stack (max depth 144)
  is always L1-resident and the conversions cost more than the bytes save.
- Packed 32-bit rule table (`tmRuleLen`+`tmRuleSymbol` in one word): +1.3%.
- Specializing unit reductions (71% of all reductions): nothing beat the generic
  ~6 ns path.
- A static "state accepts terminal" filter for JS semicolon insertion:
  `reduceAll` answers "acceptable" 99.99% of the time and only 1 semicolon was
  inserted in 4 MB; a table would cost ~370 KB for under 1%.
- SIMD (128/256-bit windows) in the packer: weakens the early exit (+30%/+60%
  word loads), ~1.5x ceiling on a phase that is now 0.19 s.
- Shift-based DFAs (`shiftdfa` package) for the JS lexer: the implementation
  supports 11 states, 32 tokens, one start condition and ASCII; JS has 240
  states, 158 tokens, 10 start conditions. A carved-out identifier+whitespace
  sub-automaton ran at ~490 MB/s against ~360 MB/s for the whole accelerated
  lexer, still one byte per branch. Only tiny grammars could use it.

Caveat on corpora: the byte distribution that drives the lexer work is
JavaScript's (identifiers 36%, comments 21%, whitespace 20%, strings 7%; runs
average 10 bytes). Other languages will have different ratios but the same
shape; the accelerators are derived from the DFA and do not assume any of it.

## 4. What landed, and why it works

For reference when porting to the C++ or TypeScript templates, or when reading
the generated code. Numbers are from interleaved idle-machine runs against the
preceding commit unless noted. Cumulative on the two corpora: lexer 257 -> 359
MB/s (-28.5% time), parser 87 -> 114 MB/s (-24%), micro parser -21%, lookahead
-14%, zero allocations per parse, JS `lalr:` phase 1.27 s -> 0.19 s.

**Goto defaults among real targets** (`lalr/optimize.go`). The per-nonterminal
default target was chosen by `pickDefault` over the full state column including
"no transition" entries, so -1 won for 531 of 932 nonterminals and their whole
columns were stored explicitly (55.7 k goto pairs instead of 17.3 k). A goto is
only ever queried from a state that has the transition, so "error" entries need
no representation. Tables 375 k -> 277 k entries (-26%), parser -12.8%. This was
the entire gap to Bison; the packing algorithm was already equivalent.

**Parser tables as fixed-size arrays** (`gen/templates/go_parser_tables.go.tmpl`).
A slice costs a load of the data pointer per access, and the compiler cannot use
the `tmTableLen` constant to eliminate bounds checks. With `[...]T` arrays the
first probe of each equally sized table dominates the rest; `gotoNT` drops from
93 to 71 instructions.

**Inlinable nonterminal goto, bounds-check-friendly trim.** `gotoState` handled
terminals and nonterminals and was too large to inline; the reduce path calls it
2.4 M times per 4 MB, all nonterminals. `gotoNT` inlines into `parse`,
`reduceAll` and `lookahead`. The trailing-empty-symbol trim walks a shrinking
slice so that the compiler can prove the later `rhs` accesses in bounds (hot-loop
bounds checks 12 -> 5).

**Skipping `applyRule`** (`tmRuleAction`, `Grammar.ReduceDispatch`). 73% of
reductions have no semantic action and no node to report, but each paid a call
and a binary search through a 200-case switch. The generator emits one `int32`
per rule: 0 nothing, > 0 node type to report over the whole right-hand side
(flags in the upper 16 bits), -1 call `applyRule`. `applyRule` fell from 9.7% to
0.6% of the profile. Grammars where every rule has an action get the plain call
and grammars where nothing has an effect get neither.

**`flush` fast path.** 98.9% of `flush` calls (one per shifted token) have no
pending tokens and a token the listener does not want. The inlinable guard uses a
generated `tmReportedTokens` set; grammars that customize token reporting via the
`customReportNext` template keep the single function.

**Allocation-free lookaheads.** The memoization `map[uint64]bool` was created per
parse and hit once in 3,054 lookups; a 128-entry direct-mapped cache inside the
`session` struct (on the stack) replaces it, and the lookahead stack buffer grew
from 64 to 128 entries. A miss only costs time, never correctness.

**Word-parallel table packing** (`allocator.search`). First-fit placement tested
candidate base offsets one bit at a time. For a row with relative pair offsets
q_k, the viable offsets inside a 64-bit window starting at i are
`free = ^usedBase[i-min] & AND_k ^taken[i+q_k]`, and the lowest set bit is the
answer; the AND chain stops as soon as the window is empty, and the search starts
from a cached first free slot. Same rule, byte-identical tables, ~64x fewer
operations. Row building also stopped clearing the whole value range in
`pickDefault`.

**Sound state minimization** (`lalr/minimize.go`). States were partitioned by
actions and transitions only, but generated code identifies some states by
number: input start states (the start state is the input index), final states
(`for state != end`), and marker states (`noLineBreakStates`, `recoveryScopeStates`
and friends). Merging one of them with an equivalent unmarked state changed
behavior; they now carry a unique signature suffix.

**Pre-multiplied lexer rows, lexer tables as arrays.** Every DFA step computed
`state*tmNumClasses+class` and loaded a slice header; targets are now stored
pre-multiplied by the row stride (`lex.Tables.DfaRows`), so a step is an add.
Lexer -12.8%, parser -5.2%.

**Lexer run accelerators** (`lex/accel.go`, `lex.Tables.Accelerators`). Derived
from the DFA at generation time:

- `tmSpaceSkip[startCondition]`: ASCII bytes that always form a one-character
  "space" token without a semantic action (the byte's transition leads to a state
  whose every column yields that action). A run of them is skipped before the
  DFA starts; 58% of `Next()` calls used to exist only to skip one space.
- `tmScanTables[i]` and the extra last column of each `tmLexerAction` row: for a
  state whose transitions on a byte set return to itself (>= 8 ASCII bytes;
  bytes >= 0x80 only when every non-ASCII rune class loops, so that UTF-8
  sequences can be consumed byte by byte), the lexer scans the run 8 bytes at a
  time with one table lookup per byte and a `TrailingZeros64` to find the stop.
  Newlines in the run are counted; `lineOffset` is maintained for `TokenColumn`.
  Backtracking checkpoints are not self-loops and are unaffected.
- The per-character rolling keyword hash cannot survive skipping, so keyword
  switches now hash the token text once, only for tokens that have a switch, and
  by bytes (`byte_string_switch` in the generator; the TypeScript and C++
  templates keep the rune-based `string_switch`).

83% of corpus bytes never touch the DFA table; lexer -18%, parser -6.4%. Verified
on the corpora, on mutated inputs and on 60,000 random fragment sequences.

## 5. Known issues found on the way

- 241 openclaw files and 62 vscode files fail to parse with the JS grammar,
  mostly newer TypeScript syntax (instantiation expressions such as
  `typeof f<T>`, `using` declarations, `accessor`). They are excluded from the
  benchmarks.
- A grammar with no listener types generates `var tmRuleType = [...]NodeType{...}`
  with `NodeType` undefined (pre-existing; the original generator does the same).
- `go vet` reports unreachable code in the generated `ast/factory.go` files
  (pre-existing).
