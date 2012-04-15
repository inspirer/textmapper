---
layout: default
title: Patterns - Manual
---

Patterns
========


Characters

	c				match the character `c'
	.				any character except newline
	\a				alert (bell, '\x07')
	\b				backspace ('\x08')
	\f				form-feed ('\x0c')
	\n				newline (line feed, '\x0a')
	\r				carriage-return ('\x0d')
	\t				tab ('\x09')
	\v				vertical tab ('\x0b')
	\ooo			the character with octal value 0ooo
	\xhh			the character with hexadecimal value 0xhh
	\uhhhh			the character with hexadecimal value 0xhhhh
	\				quote the following non-alphabetic character (to escape brackets, slashes, quantifiers etc.)
	\\				backslash

Character classes

	[abc]			a, b, or c (class)
	[_a-zA-Z]		underscore, a through z or A through Z, inclusive (range)
	[^abc]			any character except a, b, or c (negation)
	\d				a digit: [0-9]
	\D				a non-digit: [^0-9]
	\s				a whitespace character: [ \t\n\v\f\r]
	\S				a non-whitespace character: [^\s]
	\w				a word character: [a-zA-Z_0-9]
	\W				a non-word character: [^\w]
	\p{prop}		any character in the unicode category or block `prop' (see below)
	\P{prop}		a non-prop character: [^\p{prop}]
	\p{Lu}			an uppercase letter (simple category)

Logical operators

	RS				R followed by S (concatenation)
	R|S				either R or S (union)
	(R)				match an `R' (parentheses are used to override precedence)

Quantifiers

	R?				R, once or not at all
	R*				R, zero or more times
	R+				R, one or more times
	R{n}			R, exactly n times
	R{n,}			R, at least n times
	R{n,m}			R, at least n but not more than m times


Pattern definitions

	{name}			the substitution of the `name' pattern definition

Supported unicode categories (in `\p{xx}`)

	Lu				Letter, uppercase
	Ll				Letter, lowercase
	Lt				Letter, titlecase
	Lm				Letter, modifier
	Lo				Letter, other
	Mn				Mark, nonspacing
	Mc				Mark, spacing combining
	Me				Mark, enclosing
	Nd				Number, decimal digit
	Nl				Number, letter
	No				Number, other
	Pc				Punctuation, connector
	Pd				Punctuation, dash
	Ps				Punctuation, open
	Pe				Punctuation, close
	Pi				Punctuation, initial quote (may behave like Ps or Pe depending on usage)
	Pf				Punctuation, final quote (may behave like Ps or Pe depending on usage)
	Po				Punctuation, other
	Sm				Symbol, math
	Sc				Symbol, currency
	Sk				Symbol, modifier
	So				Symbol, other
	Zs				Separator, space
	Zl				Separator, line
	Zp				Separator, paragraph
	Cc				Other, control
	Cf				Other, format
	Cs				Other, surrogate
	Co				Other, private use
	Cn				Other, not assigned (including non-characters)

Pattern modifiers (in `/pattern/si`)

	s				change `.' to match any character (even a newline)
	i				case-insensitive pattern matching

Unsupported (planned)

	^				the beginning of a line
	$				the end of a line
	{eoi}			an end-of-input (can appear at the end of a pattern only)
	[\w]{-}[A-Z]	difference of two character sets (left associative operator)
	{+}				union
	{&&}			intersection (has lower priority than {+} or {-})
	\Q				quotes all characters until \E
	(?is:R)			match an `R' with the given modifiers (see Pattern modifiers)
	(?-i:R)			turns off modifier

Notes

	[^a-z] matches a newline, unless '\n' is explicitly added into the negated class: [^a-z\n]
	bar* is equivalent to ba(r*), use parentheses to override precedence: (bar)*
	
