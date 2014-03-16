describe('Regex lexer', function () {
    function expectTokens(lexer, arr) {
        var n;
        for (var i = 0; i < arr.length; ++i) {
            n = lexer.next();
            expect(n.symbol).toBe(arr[i]);
        }
        n = lexer.next();
        expect(n.symbol).toBe(jsregex.Tokens.eoi);
    }

    it('distinguishes a plus as an operator vs a plus as a character', function () {
        var errorHandler = jasmine.createSpy('errorHandler');
        var lexer = new jsregex.Lexer("\\w++", errorHandler);
        expectTokens(lexer, [jsregex.Tokens.charclass, jsregex.Tokens.Plus, jsregex.Tokens.char]);
        expect(errorHandler).not.toHaveBeenCalled();
    });

    it('distinguishes a quantifier vs an external regexp reference', function () {
        var errorHandler = jasmine.createSpy('errorHandler');
        var lexer = new jsregex.Lexer("(\\011{1,3}{name})", errorHandler);
        expectTokens(lexer, [jsregex.Tokens.Lparen, jsregex.Tokens.escaped, jsregex.Tokens.quantifier,
                            jsregex.Tokens.expand, jsregex.Tokens.Rparen]);
        expect(errorHandler).not.toHaveBeenCalled();
    });

    it('handles character sets', function () {
        var errorHandler = jasmine.createSpy('errorHandler');
        var lexer = new jsregex.Lexer("[^()a-z]", errorHandler);
        expectTokens(lexer, [jsregex.Tokens.LsquareXor, jsregex.Tokens.char, jsregex.Tokens.char,
            jsregex.Tokens.char, jsregex.Tokens.Minus, jsregex.Tokens.char, jsregex.Tokens.Rsquare]);
        expect(errorHandler).not.toHaveBeenCalled();
    });

    it('works with set operators', function () {
        var errorHandler = jasmine.createSpy('errorHandler');
        var lexer = new jsregex.Lexer("a{+}\\p{abc}{-}\\x12{eoi}", errorHandler);
        expectTokens(lexer, [jsregex.Tokens.char, jsregex.Tokens.op_union, jsregex.Tokens.charclass,
            jsregex.Tokens.op_minus, jsregex.Tokens.escaped, jsregex.Tokens.kw_eoi]);
        expect(errorHandler).not.toHaveBeenCalled();
    });

    it('reports bad strings', function () {
        var errorHandler = jasmine.createSpy('errorHandler');
        var lexer = new jsregex.Lexer("a\\", errorHandler);
        expectTokens(lexer, [jsregex.Tokens.char]);
        expect(errorHandler).toHaveBeenCalled();
    });
});