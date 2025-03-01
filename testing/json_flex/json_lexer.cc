#include "json_lexer.h"

#include "absl/log/log.h"
#include "absl/strings/match.h"

namespace json {

Lexer::Lexer(absl::string_view input_source) { source_ = input_source; }

Token Lexer::Next() {
  token_line_ = 0;
  offset_ = 0;
  token_offset_ = 0;

  // TODO: implement
  return Token::EOI;
}

}  // namespace json
