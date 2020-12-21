package dump

import (
	"fmt"
	"reflect"
	"sort"
	"strconv"
	"strings"

	"github.com/inspirer/textmapper/tm-go/util/diff"
)

// Object renders a given data object into a string, reusing the Go initialization syntax where
// possible.
func Object(v interface{}) string {
	var d dumper
	d.serialize(reflect.ValueOf(v), nil, 0)
	return d.String()
}

// Diff produces a diff of two data objects, or returns an empty string if their textual
// representations exist and are equal.
func Diff(a, b interface{}) string {
	var d dumper
	d.serialize(reflect.ValueOf(a), nil, 0)
	left := d.String()
	if d.incomplete {
		left += "\n\nThe first object cannot be properly serialized into a string."
	}

	d = dumper{}
	d.serialize(reflect.ValueOf(b), nil, 0)
	right := d.String()
	if d.incomplete {
		right += "\n\nThe second object cannot be properly serialized into a string."
	}

	return diff.LineDiff(left, right)
}

type dumper struct {
	strings.Builder
	seenPtrs   map[uintptr]bool
	indent     int
	incomplete bool // true if the data object is too deep, has cycles, or contains channels/functions
}

func (d *dumper) nl() {
	d.WriteByte('\n')
	for i := 0; i < d.indent*2; i++ {
		d.WriteByte(' ')
	}
}

func (d *dumper) serialize(val reflect.Value, storedAs reflect.Type, depth int) {
	if depth > 20 {
		d.WriteString("...")
		d.incomplete = true
		return
	}

	kind := val.Kind()
	switch kind {
	case reflect.Bool:
		fmt.Fprintf(d, "%v", val.Bool())
		return
	case reflect.String:
		d.WriteString(strconv.Quote(val.String()))
		return
	case reflect.Ptr:
		e := val.Elem()
		if !e.IsValid() {
			fmt.Fprintf(d, "(%v)(nil)", val.Type().String())
			return
		}
		d.WriteByte('&')
		var target reflect.Type
		if storedAs != nil && storedAs.Kind() == reflect.Ptr {
			target = storedAs.Elem()
		}
		d.serialize(e, target, depth+1)
		return
	case reflect.Chan:
		fmt.Fprintf(d, "%v(...)", val.Type().String())
		d.incomplete = true
		return
	case reflect.Func:
		fmt.Fprintf(d, "%v{...}", val.Type().String())
		d.incomplete = true
		return
	case reflect.Invalid:
		d.WriteString("nil")
		return
	}

	if storedAs != val.Type() {
		d.WriteString(val.Type().String())
		if kind >= reflect.Int && kind <= reflect.Float64 {
			d.WriteByte('(')
			defer d.WriteByte(')')
		}
	}
	if val.CanInterface() {
		if s, ok := val.Interface().(fmt.GoStringer); ok {
			d.WriteString(s.GoString())
			return
		}
	}
	switch kind {
	case reflect.Int, reflect.Int8, reflect.Int16, reflect.Int32, reflect.Int64:
		fmt.Fprintf(d, "%v", val.Int())
	case reflect.Uint, reflect.Uint8, reflect.Uint16, reflect.Uint32, reflect.Uint64, reflect.Uintptr:
		fmt.Fprintf(d, "%v", val.Uint())
	case reflect.Float32, reflect.Float64:
		fmt.Fprintf(d, "%v", val.Float())
	case reflect.Complex64, reflect.Complex128:
		fmt.Fprintf(d, "%v", val.Complex())
	case reflect.Interface:
		v := val.Elem()
		if !v.IsValid() {
			fmt.Fprintf(d, "%v(nil)", val.Type().String())
			return
		}
		d.serialize(v, val.Type(), depth+1)
	case reflect.Struct:
		if val.CanAddr() {
			addr := val.UnsafeAddr()
			if d.seenPtrs[addr] {
				d.WriteString("{... cycle}")
				d.incomplete = true
				return
			}
			if d.seenPtrs == nil {
				d.seenPtrs = make(map[uintptr]bool)
			}
			d.seenPtrs[addr] = true
			defer func() {
				delete(d.seenPtrs, addr)
			}()
		}
		d.WriteString("{")
		t := val.Type()
		empty := true
		d.indent++
		for i := 0; i < val.NumField(); i++ {
			val := val.Field(i)
			if isZero(val) {
				continue
			}
			f := t.Field(i)
			d.nl()
			d.WriteString(f.Name)
			d.WriteString(": ")
			d.serialize(val, f.Type, depth+1)
			d.WriteByte(',')
			empty = false
		}
		d.indent--
		if !empty {
			d.nl()
		}
		d.WriteString("}")
	case reflect.Array, reflect.Slice:
		d.WriteString("{")
		t := val.Type().Elem()
		empty := true
		d.indent++
		for i := 0; i < val.Len(); i++ {
			val := val.Index(i)
			d.nl()
			d.serialize(val, t, depth+1)
			d.WriteByte(',')
			empty = false
		}
		d.indent--
		if !empty {
			d.nl()
		}
		d.WriteString("}")
	case reflect.Map:
		d.WriteString("{")
		t := val.Type()
		empty := true
		d.indent++
		keys := val.MapKeys()
		switch t.Key().Kind() {
		case reflect.String:
			sort.Slice(keys, func(i, j int) bool { return keys[i].String() < keys[j].String() })
		case reflect.Int, reflect.Int8, reflect.Int16, reflect.Int32, reflect.Int64:
			sort.Slice(keys, func(i, j int) bool { return keys[i].Int() < keys[j].Int() })
		case reflect.Uint, reflect.Uint8, reflect.Uint16, reflect.Uint32, reflect.Uint64:
			sort.Slice(keys, func(i, j int) bool { return keys[i].Uint() < keys[j].Uint() })
		}
		for _, key := range keys {
			val := val.MapIndex(key)
			d.nl()
			d.serialize(key, t.Key(), depth+1)
			d.WriteString(": ")
			d.serialize(val, t.Elem(), depth+1)
			d.WriteByte(',')
			empty = false
		}
		d.indent--
		if !empty {
			d.nl()
		}
		d.WriteString("}")
	default:
		fmt.Fprintf(d, "unknownValue{%v}", val.Kind())
		d.incomplete = true
	}
}

func isZero(val reflect.Value) bool {
	switch val.Kind() {
	case reflect.Bool:
		return !val.Bool()
	case reflect.String:
		return val.String() == ""
	case reflect.Int, reflect.Int8, reflect.Int16, reflect.Int32, reflect.Int64:
		return val.Int() == 0
	case reflect.Uint, reflect.Uint8, reflect.Uint16, reflect.Uint32, reflect.Uint64, reflect.Uintptr:
		return val.Uint() == 0
	case reflect.Float32, reflect.Float64:
		return val.Float() == 0
	case reflect.Complex64, reflect.Complex128:
		return val.Complex() == complex(0, 0)
	case reflect.Chan, reflect.Func, reflect.Map, reflect.Ptr, reflect.UnsafePointer, reflect.Interface, reflect.Slice:
		return val.IsNil()
	case reflect.Struct:
		for i := 0; i < val.NumField(); i++ {
			val := val.Field(i)
			if !isZero(val) {
				return false
			}
		}
		return true
	case reflect.Array:
		for i := 0; i < val.Len(); i++ {
			val := val.Index(i)
			if !isZero(val) {
				return false
			}
		}
		return true
	}
	return false
}
