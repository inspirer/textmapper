#ifndef JSON_LEXER_H_
#define JSON_LEXER_H_

namespace parsers {
namespace json {

struct JsonSpan {
  void* value;
  int symbol;
  int state;
  int offset;
  int endoffset;
};

class JsonLexer {
 public:
  // Note: input must be in UTF-8.
  JsonLexer(const string* input);

 private:
  const string* input_;
};

}  // namespace json
}  // namespace parsers

#endif  // JSON_LEXER_H_
