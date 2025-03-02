import { Lexer } from './lexer';
import { TokenType } from './token';

describe('JSON Lexer', () => {
  test('should tokenize an empty string', () => {
    const lexer = new Lexer('');
    expect(lexer.next()).toBe(TokenType.EOI);
  });

  test('should tokenize simple JSON object', () => {
    const input = '{"key": "value"}';
    const lexer = new Lexer(input);
    
    // {
    expect(lexer.next()).toBe(TokenType.LBRACE);
    expect(lexer.text()).toBe('{');
    
    // "key"
    expect(lexer.next()).toBe(TokenType.JSONSTRING);
    expect(lexer.text()).toBe('"key"');
    
    // :
    expect(lexer.next()).toBe(TokenType.COLON);
    expect(lexer.text()).toBe(':');
    
    // "value"
    expect(lexer.next()).toBe(TokenType.JSONSTRING);
    expect(lexer.text()).toBe('"value"');
    
    // }
    expect(lexer.next()).toBe(TokenType.RBRACE);
    expect(lexer.text()).toBe('}');
    
    // EOI
    expect(lexer.next()).toBe(TokenType.EOI);
  });

  test('should tokenize JSON array', () => {
    const input = '[1, 2, 3]';
    const lexer = new Lexer(input);
    
    // [
    expect(lexer.next()).toBe(TokenType.LBRACK);
    expect(lexer.text()).toBe('[');
    
    // 1
    expect(lexer.next()).toBe(TokenType.JSONNUMBER);
    expect(lexer.text()).toBe('1');
    
    // ,
    expect(lexer.next()).toBe(TokenType.COMMA);
    expect(lexer.text()).toBe(',');
    
    // 2
    expect(lexer.next()).toBe(TokenType.JSONNUMBER);
    expect(lexer.text()).toBe('2');
    
    // ,
    expect(lexer.next()).toBe(TokenType.COMMA);
    expect(lexer.text()).toBe(',');
    
    // 3
    expect(lexer.next()).toBe(TokenType.JSONNUMBER);
    expect(lexer.text()).toBe('3');
    
    // ]
    expect(lexer.next()).toBe(TokenType.RBRACK);
    expect(lexer.text()).toBe(']');
    
    // EOI
    expect(lexer.next()).toBe(TokenType.EOI);
  });

  test('should tokenize JSON boolean values', () => {
    const input = '{"enabled": true, "disabled": false}';
    const lexer = new Lexer(input);
    
    // Skip to the true token
    lexer.next(); // {
    lexer.next(); // "enabled"
    lexer.next(); // :
    
    // true
    expect(lexer.next()).toBe(TokenType.TRUE);
    expect(lexer.text()).toBe('true');
    
    // Skip to the false token
    lexer.next(); // ,
    lexer.next(); // "disabled"
    lexer.next(); // :
    
    // false
    expect(lexer.next()).toBe(TokenType.FALSE);
    expect(lexer.text()).toBe('false');
  });

  test('should tokenize JSON null value', () => {
    const input = '{"value": null}';
    const lexer = new Lexer(input);
    
    // Skip to the null token
    lexer.next(); // {
    lexer.next(); // "value"
    lexer.next(); // :
    
    // null
    expect(lexer.next()).toBe(TokenType.NULL);
    expect(lexer.text()).toBe('null');
  });

  test('should handle whitespace and punctuation correctly', () => {
    const input = ' { \n "key" : \t "value" \r\n } ';
    const lexer = new Lexer(input);
    
    // Whitespace is consumed before tokens
    // {
    expect(lexer.next()).toBe(TokenType.LBRACE);
    expect(lexer.text()).toBe('{');
    
    // "key"
    expect(lexer.next()).toBe(TokenType.JSONSTRING);
    expect(lexer.text()).toBe('"key"');
    
    // :
    expect(lexer.next()).toBe(TokenType.COLON);
    expect(lexer.text()).toBe(':');
    
    // "value"
    expect(lexer.next()).toBe(TokenType.JSONSTRING);
    expect(lexer.text()).toBe('"value"');
    
    // }
    expect(lexer.next()).toBe(TokenType.RBRACE);
    expect(lexer.text()).toBe('}');
    
    // EOI
    expect(lexer.next()).toBe(TokenType.EOI);
  });

  test('should correctly report line numbers', () => {
    const input = '{\n"key": "value"\n}';
    const lexer = new Lexer(input);
    
    // {
    expect(lexer.next()).toBe(TokenType.LBRACE);
    expect(lexer.line()).toBe(1);
    
    // "key" (on line 2) - includes the newline
    expect(lexer.next()).toBe(TokenType.JSONSTRING);
    expect(lexer.line()).toBe(2);
    
    // :
    expect(lexer.next()).toBe(TokenType.COLON);
    expect(lexer.line()).toBe(2);
    
    // "value"
    expect(lexer.next()).toBe(TokenType.JSONSTRING);
    expect(lexer.line()).toBe(2);
    
    // } (on line 3) - includes the newline
    expect(lexer.next()).toBe(TokenType.RBRACE);
    expect(lexer.line()).toBe(3);
  });

  test('should tokenize complex JSON structures', () => {
    const input = '{"array":[1,"text",true,null,{"nested":42}]}';
    const lexer = new Lexer(input);
    
    const expectedTokens = [
      TokenType.LBRACE,        // {
      TokenType.JSONSTRING,    // "array"
      TokenType.COLON,         // :
      TokenType.LBRACK,        // [
      TokenType.JSONNUMBER,    // 1
      TokenType.COMMA,         // ,
      TokenType.JSONSTRING,    // "text"
      TokenType.COMMA,         // ,
      TokenType.TRUE,          // true
      TokenType.COMMA,         // ,
      TokenType.NULL,          // null
      TokenType.COMMA,         // ,
      TokenType.LBRACE,        // {
      TokenType.JSONSTRING,    // "nested"
      TokenType.COLON,         // :
      TokenType.JSONNUMBER,    // 42
      TokenType.RBRACE,        // }
      TokenType.RBRACK,        // ]
      TokenType.RBRACE,        // }
      TokenType.EOI            // end of input
    ];
    
    for (const expectedToken of expectedTokens) {
      const actual = lexer.next();
      expect(actual).toBe(expectedToken);
    }
  });

  test('should handle string with BOM', () => {
    const bomChar = "\xef\xbb\xbf";
    const input = `${bomChar}{"key": "value"}`;
    const lexer = new Lexer(input);
    
    // {
    expect(lexer.next()).toBe(TokenType.LBRACE);
    expect(lexer.text()).toBe('{');
    
    // Continue with rest of tokens...
    expect(lexer.next()).toBe(TokenType.JSONSTRING);
    expect(lexer.text()).toBe('"key"');
  });

  test('should handle JSON lexer copy method', () => {
    const input = '{"key": "value"}';
    const lexer = new Lexer(input);
    
    // Advance to key token
    lexer.next(); // {
    const keyToken = lexer.next(); // "key"
    expect(keyToken).toBe(TokenType.JSONSTRING);
    expect(lexer.text()).toBe('"key"');
    
    // Make a copy of the lexer in its current state
    const lexerCopy = lexer.copy();
    
    // Original lexer continues
    expect(lexer.next()).toBe(TokenType.COLON);
    expect(lexer.next()).toBe(TokenType.JSONSTRING); // "value"
    
    // The copy should be an independent lexer with all methods
    expect(typeof lexerCopy.next).toBe('function');
    expect(typeof lexerCopy.text).toBe('function');
    
    // The copy should continue from the same state as the original
    // when it was copied
    expect(lexerCopy.next()).toBe(TokenType.COLON);
    expect(lexerCopy.next()).toBe(TokenType.JSONSTRING); // "value"
    expect(lexerCopy.text()).toBe('"value"');
  });
}); 