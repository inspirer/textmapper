package js_test

import (
	"context"
	"crypto/sha256"
	"encoding/binary"
	"fmt"
	"io/fs"
	"os"
	"path/filepath"
	"sort"
	"strings"
	"testing"

	"github.com/inspirer/textmapper/parsers/js"
	"github.com/inspirer/textmapper/parsers/js/token"
)

// TM_JS_CORPUS points to one or more directories (comma-separated) with
// JavaScript/TypeScript sources. When set, BenchmarkCorpus loads all
// supported files into memory and measures realistic lexer and parser
// throughput on them. Files with syntax errors are excluded from the
// measurement (and reported) so that error recovery does not skew results.
//
// Example:
//
//	TM_JS_CORPUS=~/projects/openclaw go test -run '^$' -bench Corpus ./parsers/js
const corpusEnv = "TM_JS_CORPUS"

type corpusFile struct {
	name    string
	content string
	dialect js.Dialect
}

var corpusExts = map[string]js.Dialect{
	".js":  js.Javascript,
	".mjs": js.Javascript,
	".cjs": js.Javascript,
	".jsx": js.TypescriptJsx,
	".ts":  js.Typescript,
	".tsx": js.TypescriptJsx,
}

var skipDirs = map[string]bool{
	"node_modules": true,
	".git":         true,
	"dist":         true,
	"build":        true,
}

func loadCorpus(tb testing.TB) []corpusFile {
	dirs := os.Getenv(corpusEnv)
	if dirs == "" {
		tb.Skipf("%s is not set", corpusEnv)
	}
	var ret []corpusFile
	for _, dir := range strings.Split(dirs, ",") {
		dir = strings.TrimSpace(dir)
		if strings.HasPrefix(dir, "~/") {
			home, _ := os.UserHomeDir()
			dir = filepath.Join(home, dir[2:])
		}
		err := filepath.WalkDir(dir, func(path string, d fs.DirEntry, err error) error {
			if err != nil {
				return err
			}
			if d.IsDir() {
				if skipDirs[d.Name()] && path != dir {
					return filepath.SkipDir
				}
				return nil
			}
			dialect, ok := corpusExts[filepath.Ext(path)]
			if !ok {
				return nil
			}
			// Skip .d.ts and similar only if they would be ambiguous; keep everything else.
			data, err := os.ReadFile(path)
			if err != nil {
				return err
			}
			ret = append(ret, corpusFile{name: path, content: string(data), dialect: dialect})
			return nil
		})
		if err != nil {
			tb.Fatalf("cannot read corpus %q: %v", dir, err)
		}
	}
	if len(ret) == 0 {
		tb.Skipf("no supported files found in %s", dirs)
	}
	sort.Slice(ret, func(i, j int) bool { return ret[i].name < ret[j].name })
	return ret
}

// filterCorpus parses every file once and drops the ones with syntax errors.
func filterCorpus(tb testing.TB, files []corpusFile) (ok []corpusFile, total int64) {
	var s js.TokenStream
	var p js.Parser
	ctx := context.Background()
	var failed int
	var failedBytes int64
	for _, f := range files {
		errs := 0
		p.Init(func(js.SyntaxError) bool { errs++; return true }, noListener)
		s.Init(f.content, noListener)
		s.SetDialect(f.dialect)
		p.ParseModule(ctx, &s)
		if errs > 0 {
			failed++
			failedBytes += int64(len(f.content))
			if testing.Verbose() {
				tb.Logf("skipping %s: %d syntax errors", f.name, errs)
			}
			continue
		}
		ok = append(ok, f)
		total += int64(len(f.content))
	}
	tb.Logf("corpus: %d files, %.1f MB; skipped %d files with syntax errors (%.1f MB)",
		len(ok), float64(total)/(1<<20), failed, float64(failedBytes)/(1<<20))
	return ok, total
}

func TestCorpus(t *testing.T) {
	files := loadCorpus(t)
	ok, _ := filterCorpus(t, files)
	if len(ok) == 0 {
		t.Fatalf("no files in the corpus can be parsed without errors")
	}
}

func BenchmarkCorpus(b *testing.B) {
	files, total := filterCorpus(b, loadCorpus(b))

	b.Run("lexer", func(b *testing.B) {
		l := new(js.Lexer)
		b.SetBytes(total)
		b.ResetTimer()
		for i := 0; i < b.N; i++ {
			for _, f := range files {
				l.Init(f.content)
				l.Dialect = f.dialect
				for next := l.Next(); next != token.EOI; next = l.Next() {
				}
			}
		}
	})

	b.Run("parser", func(b *testing.B) {
		var s js.TokenStream
		var p js.Parser
		ctx := context.Background()
		onError := func(se js.SyntaxError) bool {
			b.Errorf("unexpected: %v", se)
			return false
		}
		p.Init(onError, noListener)
		b.SetBytes(total)
		b.ResetTimer()
		for i := 0; i < b.N; i++ {
			for _, f := range files {
				s.Init(f.content, noListener)
				s.SetDialect(f.dialect)
				p.ParseModule(ctx, &s)
			}
		}
	})
}

// TestCorpusHash prints hashes over everything the parser and the lexer produce for the
// corpus (listener events, syntax errors, return values; token kinds, offsets, lines and
// lexer states), for the original files and for mutated variants of them. Run it before
// and after a change to the generator and compare the two outputs: identical hashes mean
// the change is unobservable.
//
//	TM_JS_CORPUS=... go test -run TestCorpusHash -v ./parsers/js | grep HASH
func TestCorpusHash(t *testing.T) {
	files := loadCorpus(t)
	var buf [40]byte
	word := func(h interface{ Write([]byte) (int, error) }, vals ...int) {
		for i, v := range vals {
			binary.LittleEndian.PutUint64(buf[8*i:], uint64(v))
		}
		h.Write(buf[:8*len(vals)])
	}

	events := sha256.New()
	var numEvents, numErrors int
	var s js.TokenStream
	var p js.Parser
	ctx := context.Background()
	parse := func(src string, d js.Dialect) {
		p.Init(func(se js.SyntaxError) bool {
			numErrors++
			word(events, -1, se.Offset, se.Endoffset)
			return true
		}, func(nt js.NodeType, o, e int) {
			numEvents++
			word(events, int(nt), o, e)
		})
		s.Init(src, func(nt js.NodeType, o, e int) {
			word(events, int(nt)+1000, o, e)
		})
		s.SetDialect(d)
		fmt.Fprintf(events, "ret=%v\n", p.ParseModule(ctx, &s))
	}

	tokens := sha256.New()
	var numTokens int
	l := new(js.Lexer)
	lex := func(src string, d js.Dialect) {
		l.Init(src)
		l.Dialect = d
		for {
			tok := l.Next()
			start, end := l.Pos()
			word(tokens, int(tok), start, end, l.Line(), l.State)
			numTokens++
			if tok == token.EOI {
				break
			}
		}
	}

	for i, f := range files {
		parse(f.content, f.dialect)
		lex(f.content, f.dialect)
		if i%5 == 0 && len(f.content) > 100 {
			// Mutations: drop a chunk, insert garbage, truncate.
			src := f.content
			k := (i * 7919) % (len(src) - 50)
			for _, m := range []string{src[:k] + src[k+30:], src[:k] + " ) } ] ;; if ( " + src[k:], src[:k], src[:k] + " /* \" ' ` \\ \xff\xfe é "} {
				parse(m, f.dialect)
				lex(m, f.dialect)
			}
		}
	}
	fmt.Printf("EVENTHASH %x events=%d errors=%d\n", events.Sum(nil), numEvents, numErrors)
	fmt.Printf("TOKENHASH %x tokens=%d\n", tokens.Sum(nil), numTokens)
}
