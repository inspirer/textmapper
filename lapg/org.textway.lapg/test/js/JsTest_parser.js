// generated parser

(function(window) {

var JsTest = {};

JsTest.Lexems = {
	eoi: 0,
	Lid: 1,
	_skip: 2
};

JsTest.Lexer = function(text,errorHandler) {
	this.reset(text);
	this.errorHandler = errorHandler;

	this.tokenLine = 1;
	this.currLine = 1;
	this.currColumn = 1;
	this.currOffset = 0;
}

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
					if (this.chr == '\n') {
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
}


window.JsTest = JsTest;

})(window);
