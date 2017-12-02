package lex

import (
	"fmt"
)

type inst struct {
	consume symlist // An empty list means we cannot advance to the next instruction.
	links   []int   // Relative offsets of other instructions that should be considered at the same time.
	action  int     // A non-negative value accepts the current input.
}

// reCompiler translates a set of regular expressions into a single instruction list.
type reCompiler struct {
	resolver   Resolver
	sets       []charset
	out        []inst
	consume    []int
	runes      map[rune]int // index in sets
	inExternal map[string]bool
	err        error
}

func newCompiler(resolver Resolver) *reCompiler {
	return &reCompiler{
		resolver:   resolver,
		runes:      make(map[rune]int),
		inExternal: make(map[string]bool),
	}
}

func (c *reCompiler) addRegexp(re *Regexp, action int) (int, error) {
	c.err = nil
	ret := c.next()
	c.serialize(re)
	accept := c.emit(nil)
	c.out[accept].action = action
	return ret, c.err
}

func (c *reCompiler) compile() (ins []inst, inputMap []RangeEntry) {
	symlists, inputMap := compressCharsets(c.sets)
	for i, id := range c.consume {
		if id >= 0 {
			c.out[i].consume = symlists[id]
		}
	}
	return c.out, inputMap
}

func (c *reCompiler) serialize(re *Regexp) {
	if c.err != nil {
		return
	}

	switch re.op {
	case opLiteral:
		for _, r := range re.text {
			c.emit(charset{r, r})
		}
	case opCharClass:
		c.emit(re.charset)
	case opExternal:
		if _, ok := c.inExternal[re.text]; ok {
			c.errorf("named patterns cannot recursively depend on each other (in %s)", re.text)
			return
		}
		ext := c.resolver.Resolve(re.text)
		if ext == nil {
			c.errorf("cannot find named pattern: %s", re.text)
			return
		}
		c.inExternal[re.text] = true
		c.serialize(ext)
		delete(c.inExternal, re.text)
	case opRepeat:
		if re.min > 16 || re.max > 16 {
			c.errorf("cannot expand the regexp, too many entities to repeat (max. 16)")
			return
		}

		for i := 0; i < re.min; i++ {
			c.serialize(re.sub[0])
		}
		if re.max == -1 {
			start := c.next()
			c.serialize(re.sub[0])
			barrier := c.emit(nil)
			c.link(barrier, start)
			c.link(start, c.next())
		} else if re.max > re.min {
			var subs []int
			for i := re.max - re.min; i >= 0; i-- {
				subs = append(subs, c.next())
				c.serialize(re.sub[0])
			}
			barrier := c.emit(nil)
			for _, sub := range subs {
				c.link(sub, c.next())
			}
			c.link(barrier, c.next())
		}
	case opAlternate:
		alt := c.emit(nil)
		var ends []int
		for _, s := range re.sub {
			c.link(alt, c.next())
			c.serialize(s)
			ends = append(ends, c.emit(nil))
		}
		for _, end := range ends {
			c.link(end, c.next())
		}
	case opConcat:
		for _, s := range re.sub {
			c.serialize(s)
		}
	default:
		panic("unknown regexp operation")
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

func (c *reCompiler) emit(cs charset) int {
	c.out = append(c.out, inst{action: -1})

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
