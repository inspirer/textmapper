package js_test

import (
	"context"
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
