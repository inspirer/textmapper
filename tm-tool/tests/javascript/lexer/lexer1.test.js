describe('Lexer for identifiers, numbers, keywords and comments', function () {
    it('breaks simple string into tokens', function () {
        var errorHandler = jasmine.createSpy('errorHandler');
        var lexer = new lexer1.Lexer(' // comment\n' +
            '    abcd 98 run \n' +
            '    \tclass method method1 ', errorHandler);
        var n = lexer.next();
        expect(n.symbol).toBe(lexer1.Lexemes.id);
        expect(n.value).toBe('abcd');
        n = lexer.next();
        expect(n.symbol).toBe(lexer1.Lexemes.icon);
        expect(lexer.token).toBe('98');
        n = lexer.next();
        expect(n.symbol).toBe(lexer1.Lexemes.run);
        n = lexer.next();
        expect(n.symbol).toBe(lexer1.Lexemes._class);
        n = lexer.next();
        expect(n.symbol).toBe(lexer1.Lexemes.id);
        n = lexer.next();
        expect(n.symbol).toBe(lexer1.Lexemes.id);
        n = lexer.next();
        expect(n.symbol).toBe(lexer1.Lexemes.eoi);
        expect(errorHandler).not.toHaveBeenCalled();
    });
});