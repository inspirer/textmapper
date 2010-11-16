// generated parser

(function(window) {

var JsTest = {
	DEBUG_SYNTAX: false
};

JsTest.Lexems = {
	eoi: 0,
	Lid: 1,
	_skip: 2,
	error: 3
};

JsTest.Lexer = function(text,errorHandler) {
	this.reset(text);
	this.errorHandler = errorHandler;

	this.tokenLine = 1;
	this.currLine = 1;
	this.currColumn = 1;
	this.currOffset = 0;
};

JsTest.Lexer.prototype = {

	reset: function(text) {
		this.text = text;
		this.group = 0;
		this.chr = text.length > 0 ? text.charCodeAt(0) : 0;
		this.offset = 1;
		this.token = "";
	},

	getState: function() {
		return this.group;
	},

	setState: function(state) {
		this.group = state;
	},

	lapg_char2no: [
		0, 1, 1, 1, 1, 1, 1, 1, 1, 6, 3, 1, 1, 6, 1, 1,
		1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1,
		6, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 2,
		5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 1, 1, 1, 1, 1, 1,
		1, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4,
		4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 1, 1, 1, 1, 4,
		1, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4,
		4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 1, 1, 1, 1, 1
	],

	lapg_lexem: [
		[ -2, -1, 1, 2, 3, -1, 2],
		[ -1, -1, 4, -1, -1, -1, -1],
		[ -4, -4, -4, 2, -4, -4, 2],
		[ -3, -3, -3, -3, 3, 3, -3],
		[ -4, 4, 4, -4, 4, 4, 4]
	],

	mapCharacter: function(chr) {
		if (chr >= 0 && chr < 128) {
			return this.lapg_char2no[chr];
		}
		return 1;
	},

	next: function() {
		var lapg_n = {};
		var state;

		do {
			lapg_n.offset = this.currOffset;
			this.tokenLine = lapg_n.line = this.currLine;
			lapg_n.column = this.currColumn;
			this.token = "";
			var tokenStart = this.offset - 1;

			for (state = this.group; state >= 0;) {
				state = this.lapg_lexem[state][this.mapCharacter(this.chr)];
				if (state >= -1 && this.chr != 0) {
					this.currOffset++;
					this.currColumn++;
					if (this.chr == 10) {
						this.currColumn = 1;
						this.currLine++;
					}
					this.chr = this.offset < this.text.length ? this.text.charCodeAt(this.offset++) : 0;
				}
			}
			lapg_n.endoffset = this.currOffset;
			lapg_n.endline = this.currLine;
			lapg_n.endcolumn = this.currColumn;

			if (state == -1) {
				if (this.chr == 0) {
					this.errorHandler(lapg_n.offset, lapg_n.endoffset, this.currLine, "Unexpected end of file reached");
					break;
				}
				this.errorHandler(lapg_n.offset, lapg_n.endoffset, this.currLine, "invalid lexem at line " + this.currLine + ": `" + this.token + "`, skipped");
				lapg_n.lexem = -1;
				continue;
			}

			if (this.offset - 1 > tokenStart) {
				this.token = this.text.slice(tokenStart, this.offset - 1);
			}

			lapg_n.lexem = - state - 2;
			lapg_n.sym = null;

		} while (lapg_n.lexem == -1 || !this.createToken(lapg_n));
		return lapg_n;
	},

	createToken: function(lapg_n) {
		switch (lapg_n.lexem) {
			case 1:
				 lapg_n.sym = this.token; break; 
			case 2:
				 return false; 
		}
		return true;
	}
};

JsTest.NonTerm = {
	input: 4
};

JsTest.Parser = function() {
};

JsTest.Parser.prototype = {
	lapg_action: [
		-1, 0, -1, -2
	],

	lapg_sym_goto: [
		0, 1, 2, 2, 2, 3
	],

	lapg_sym_from: [
		2, 0, 0
	],

	lapg_sym_to: [
		3, 1, 2
	],

	lapg_rlen: [
		1
	],

	lapg_rlex: [
		4
	],

	lapg_syms: [
		"eoi",
		"Lid",
		"_skip",
		"error",
		"input"
	],

	lapg_next: function(state,symbol) {
		return this.lapg_action[state];
	},

	lapg_state_sym: function(state, symbol) {
		var min = this.lapg_sym_goto[symbol], max = this.lapg_sym_goto[symbol + 1] - 1;
		var i, e;

		while (min <= max) {
			e = (min + max) >> 1;
			i = this.lapg_sym_from[e];
			if (i == state) {
				return this.lapg_sym_to[e];
			} else if (i < state) {
				min = e + 1;
			} else {
				max = e - 1;
			}
		}
		return -1;
	},

	parse: function(lexer) {
		this.lapg_m = new Array(1024);
		this.lapg_head = 0;
		var lapg_symbols_ok = 4;

		this.lapg_m[0] = {
			state: 0
		};
		this.lapg_n = lexer.next();

		while (this.lapg_m[this.lapg_head].state != 3) {
			var lapg_i = this.lapg_next(this.lapg_m[this.lapg_head].state, this.lapg_n.lexem);

			if (lapg_i >= 0) {
				this.reduce(lapg_i);
			} else if (lapg_i == -1) {
				this.shift(lexer);
				lapg_symbols_ok++;
			}

			if (lapg_i == -2 || this.lapg_m[this.lapg_head].state == -1) {
				if (this.lapg_n.lexem == 0) {
					break;
				}
				while (this.lapg_head >= 0 && this.lapg_state_sym(this.lapg_m[this.lapg_head].state, 3) == -1) {
					this.lapg_m[this.lapg_head] = null; // TODO dispose?
					this.lapg_head--;
				}
				if (this.lapg_head >= 0) {
					this.lapg_m[++this.lapg_head] = {
						lexem: 3,
						state: this.lapg_state_sym(this.lapg_m[this.lapg_head - 1].state, 3),
						line: this.lapg_n.line,
						column: this.lapg_n.column,
						offset: this.lapg_n.offset,
						endline: this.lapg_n.endline,
						endcolumn: this.lapg_n.endcolumn,
						endoffset: this.lapg_n.endoffset,
						sym: null
					};
					if (lapg_symbols_ok >= 4) {
						lexer.errorHandler(this.lapg_n.offset, this.lapg_n.endoffset, lexer.tokenLine, "syntax error before line " + lexer.tokenLine + ", column " + this.lapg_n.column);
					}
					if (lapg_symbols_ok <= 1) {
						this.lapg_n = lexer.next();
					}
					lapg_symbols_ok = 0;
					continue;
				} else {
					this.lapg_head = 0;
					this.lapg_m[0] = {
						state: 0
					};
				}
				break;
			}
		}

		if (this.lapg_m[this.lapg_head].state != 3) {
			if (lapg_symbols_ok >= 4) {
				lexer.errorHandler(this.lapg_n.offset, this.lapg_n.endoffset, lexer.tokenLine, "syntax error before line " + lexer.tokenLine + ", column " + this.lapg_n.column);
			}
			return null;
		}
		return this.lapg_m[this.lapg_head - 1].sym;
	},

	shift: function(lexer) {
		this.lapg_m[++this.lapg_head] = this.lapg_n;
		this.lapg_m[this.lapg_head].state = this.lapg_state_sym(this.lapg_m[this.lapg_head - 1].state, this.lapg_n.lexem);
		if (JsTest.DEBUG_SYNTAX) {
			JsTest.DEBUG_SYNTAX("shift: " + this.lapg_syms[this.lapg_n.lexem] + " (" + lexer.token + ")");
		}
		if (this.lapg_m[this.lapg_head].state != -1 && this.lapg_n.lexem != 0) {
			this.lapg_n = lexer.next();
		}
	},

	reduce: function(rule) {
		var lapg_gg = {};
		lapg_gg.sym = (this.lapg_rlen[rule] != 0) ? this.lapg_m[this.lapg_head + 1 - this.lapg_rlen[rule]].sym : null;
		lapg_gg.lexem = this.lapg_rlex[rule];
		lapg_gg.state = 0;
		if (JsTest.DEBUG_SYNTAX) {
			JsTest.DEBUG_SYNTAX("reduce to " + this.lapg_syms[this.lapg_rlex[rule]]);
		}
		var startsym = (this.lapg_rlen[rule] != 0) ? this.lapg_m[this.lapg_head + 1 - this.lapg_rlen[rule]] : this.lapg_n;
		lapg_gg.line = startsym.line;
		lapg_gg.column = startsym.column;
		lapg_gg.offset = startsym.offset;
		lapg_gg.endline = (this.lapg_rlen[rule] != 0) ? this.lapg_m[this.lapg_head].endline : this.lapg_n.line;
		lapg_gg.endcolumn = (this.lapg_rlen[rule] != 0) ? this.lapg_m[this.lapg_head].endcolumn : this.lapg_n.column;
		lapg_gg.endoffset = (this.lapg_rlen[rule] != 0) ? this.lapg_m[this.lapg_head].endoffset : this.lapg_n.offset;
		for (var e = this.lapg_rlen[rule]; e > 0; e--) {
			this.lapg_m[this.lapg_head--] = null;
		}
		this.lapg_m[++this.lapg_head] = lapg_gg;
		this.lapg_m[this.lapg_head].state = this.lapg_state_sym(this.lapg_m[this.lapg_head-1].state, lapg_gg.lexem);
	}
};

window.JsTest = JsTest;

})(window);
