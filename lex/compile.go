package lex

import (
	"fmt"
	"log"
	"strings"

	"github.com/inspirer/textmapper/status"
	"github.com/inspirer/textmapper/util/container"
)

type Pattern struct {
	Name   string
	RE     *Regexp
	Text   string // of RE
	Origin status.SourceNode
}

type inst struct {
	consume symlist // An empty list means we cannot advance to the next instruction.
	links   []int   // Relative offsets of other instructions that should be considered at the same time.
	rule    *Rule   // The rule to be accepted.
	trace   trace   // core() instructions only: the trace of this instruction
}

func (i inst) core() bool {
	return i.rule != nil || len(i.consume) != 0
}

func (i inst) String() string {
	var sb strings.Builder
	i.trace.toString(&sb)
	return sb.String()
}

type trace struct {
	pattern *Pattern // The pattern that produced a given instruction.
	offset  int      // The corresponding offset in pattern.Text.
	caller  *trace   // The last pattern in this chain is a Rule.
}

func (t *trace) toString(sb *strings.Builder) {
	if t.caller != nil {
		t.caller.toString(sb)
		sb.WriteString(" -> ")
	} else {
		sb.WriteString(t.pattern.Name)
		sb.WriteString(": ")
	}
	fmt.Fprintf(sb, "/%v<STATE>%v/", t.pattern.Text[:t.offset], t.pattern.Text[t.offset:])
}

// reCompiler translates a set of regular expressions into a single list of instructions.
type reCompiler struct {
	sets       []charset
	out        []inst
	consume    []int
	runes      map[rune]int // index in sets
	inExternal map[string]bool
	err        error
}

func newCompiler() *reCompiler {
	return &reCompiler{
		runes:      make(map[rune]int),
		inExternal: make(map[string]bool),
	}
}

func (c *reCompiler) addPattern(p *Pattern, rule *Rule) (int, error) {
	c.err = nil
	ret := c.next()

	t := trace{pattern: p}
	c.serialize(p.RE, rule.Resolver, t)
	t.offset = len(t.pattern.Text)
	accept := c.emit(nil, t)
	c.out[accept].rule = rule
	transitiveClosure(c.out[ret:])

	for _, delta := range c.out[ret].links {
		dst := ret + delta
		if c.out[dst].rule != nil {
			c.errorf("`%v` accepts empty text", p.Name)
			break
		}
	}

	return ret, c.err
}

func (c *reCompiler) compile() (ins []inst, inputMap []RangeEntry) {
	symlists, inputMap := compressCharsets(c.sets)
	for i, id := range c.consume {
		if id >= 0 {
			c.out[i].consume = symlists[id]
		}
	}
	for src := range c.out {
		nlinks := c.out[src].links[:0]
		for _, delta := range c.out[src].links {
			if c.out[src+delta].core() {
				nlinks = append(nlinks, delta)
			}
		}
		c.out[src].links = nlinks
	}
	return c.out, inputMap
}

func (c *reCompiler) serialize(re *Regexp, resolver Resolver, t trace) {
	if c.err != nil {
		return
	}

	switch re.op {
	case opLiteral:
		for i, r := range re.text {
			t.offset = re.offset + i
			c.emit(charset{r, r}, t)
		}
	case opCharClass:
		t.offset = re.offset
		c.emit(re.charset, t)
	case opExternal:
		t.offset = re.offset
		if re.text == "eoi" {
			eoi := c.emit(nil, t)
			c.out[eoi].consume = symlist{EOI}
			return
		}
		if _, ok := c.inExternal[re.text]; ok {
			c.errorf("named patterns cannot recursively depend on each other (in %s)", re.text)
			return
		}
		pattern := resolver.Resolve(re.text)
		if pattern == nil {
			c.errorf("cannot find named pattern: %s", re.text)
			return
		}
		c.inExternal[re.text] = true
		child := trace{pattern: pattern, caller: &t}
		c.serialize(pattern.RE, resolver, child)
		delete(c.inExternal, re.text)
	case opRepeat:
		if re.min > 16 || re.max > 16 {
			c.errorf("cannot expand the regexp, too many entities to repeat (max. 16)")
			return
		}
		barrier := c.emit(nil, trace{})
		c.link(barrier, c.next())

		var last int
		for i := 0; i < re.min; i++ {
			last = c.next()
			c.serialize(re.sub[0], resolver, t)
		}
		if re.max == -1 {
			if re.min == 0 {
				last = c.next()
				c.serialize(re.sub[0], resolver, t)
			}
			barrier := c.emit(nil, trace{})
			c.link(barrier, last)
			c.link(barrier, c.next())
			if re.min == 0 {
				c.link(last, c.next())
			}
		} else if re.max > re.min {
			var subs []int
			for i := re.max - re.min; i > 0; i-- {
				subs = append(subs, c.next())
				c.serialize(re.sub[0], resolver, t)
			}
			barrier := c.emit(nil, trace{})
			for _, sub := range subs {
				c.link(sub, c.next())
			}
			c.link(barrier, c.next())
		}
	case opAlternate:
		alt := c.emit(nil, trace{})
		var ends []int
		for _, s := range re.sub {
			c.link(alt, c.next())
			c.serialize(s, resolver, t)
			ends = append(ends, c.emit(nil, trace{}))
		}
		for _, end := range ends {
			c.link(end, c.next())
		}
	case opConcat:
		for _, s := range re.sub {
			c.serialize(s, resolver, t)
		}
	default:
		log.Fatal("unknown regexp operation")
	}
}

func (c *reCompiler) errorf(format string, a ...interface{}) {
	if c.err == nil {
		c.err = fmt.Errorf(format, a...)
	}
}

func (c *reCompiler) next() int {
	return len(c.out)
}

func (c *reCompiler) link(src, dst int) {
	c.out[src].links = append(c.out[src].links, dst-src)
}

func (c *reCompiler) emit(cs charset, t trace) int {
	c.out = append(c.out, inst{trace: t})

	id := -1
	if len(cs) != 0 {
		if len(cs) == 2 && cs[0] == cs[1] {
			r := cs[0]
			var ok bool
			id, ok = c.runes[r]
			if !ok {
				id = len(c.sets)
				c.sets = append(c.sets, []rune{r, r})
				c.runes[r] = id
			}
		} else {
			id = len(c.sets)
			c.sets = append(c.sets, cs)
		}
	}

	c.consume = append(c.consume, id)
	return len(c.out) - 1
}

func transitiveClosure(code []inst) {
	seen := container.NewBitSet(len(code))

	var visit func(int, int)
	visit = func(origin, src int) {
		for _, delta := range code[src].links {
			dst := src + delta
			if !seen.Get(dst) {
				code[origin].links = append(code[origin].links, dst-origin)
				seen.Set(dst)
				visit(origin, dst)
			}
		}
	}

	for src, ins := range code {
		if len(ins.links) == 0 {
			continue
		}
		seen.ClearAll(len(code))
		seen.Set(src)
		for _, delta := range ins.links {
			seen.Set(src + delta)
		}
		for _, delta := range ins.links {
			visit(src, src+delta)
		}
	}
}
