// generated by John; DO EDIT
#ifndef EXPERIMENTAL_USERS_FREMLIN_TEXTMAPPER_PARSERS_JSON_LEXER_H_
#define EXPERIMENTAL_USERS_FREMLIN_TEXTMAPPER_PARSERS_JSON_LEXER_H_

#include <array>
#include <cstdint>
#include <ostream>

#include "absl/strings/string_view.h"

namespace json {
inline constexpr absl::string_view bomSeq = "\xef\xbb\xbf";

// Token is an enum of all terminal symbols of the json language.
enum class Token {
  UNAVAILABLE = -1,
  EOI = 0,
  INVALID_TOKEN,
  LBRACE,  // {
  RBRACE,  // }
  LBRACK,  // [
  RBRACK,  // ]
  COLON,   // :
  COMMA,   // ,
  SPACE,
  MULTILINECOMMENT,
  JSONSTRING,
  JSONNUMBER,
  ID,
  JSON_NULL,  // null
  TRUE,       // true
  FALSE,      // false
  CHAR_A,     // A
  CHAR_B,     // B
  ERROR,
  NumTokens
};

constexpr inline std::array<absl::string_view,
                            static_cast<size_t>(Token::NumTokens)>
    tokenStr = {
        "EOI",        "INVALID_TOKEN",
        "{",          "}",
        "[",          "]",
        ":",          ",",
        "SPACE",      "MULTILINECOMMENT",
        "JSONSTRING", "JSONNUMBER",
        "ID",         "null",
        "true",       "false",
        "A",          "B",
        "ERROR",
};

inline std::ostream& operator<<(std::ostream& os, Token tok) {
  int t = static_cast<int>(tok);
  if (t >= 0 && t < tokenStr.size()) {
    return os << tokenStr[t];
  }
  return os << "token(" << t << ")";
}

class Lexer {
 public:
  using Location = int64_t;  // Byte offset into input buffer.

  // Init prepares the lexer l to tokenize source by performing the full reset
  // of the internal state.
  explicit Lexer(absl::string_view input_source ABSL_ATTRIBUTE_LIFETIME_BOUND);

  // Next finds and returns the next token in source. The source end is
  // indicated by Token.EOI.
  //
  // The token text can be retrieved later by calling the Text() method.
  ABSL_MUST_USE_RESULT Token Next();

  // Start position of the last token returned by Next().
  ABSL_MUST_USE_RESULT Location TokenStartLocation() const {
    return token_offset_;
  }
  // End position of the last token returned by Next().
  ABSL_MUST_USE_RESULT Location TokenEndLocation() const { return offset_; }

  // Line returns the line number of the last token returned by Next()
  // (1-based).
  ABSL_MUST_USE_RESULT int64_t Line() const { return token_line_; }

  // Text returns the substring of the input corresponding to the last token.
  ABSL_MUST_USE_RESULT absl::string_view Text() const
      ABSL_ATTRIBUTE_LIFETIME_BOUND {
    return source_.substr(token_offset_, offset_ - token_offset_);
  }

 private:
  // Rewind can be used in lexer actions to accept a portion of a scanned token,
  // or to include more text into it.
  void Rewind(int64_t rewind_offset);

  absl::string_view source_;

  int input_rune_ = 0;        // current character, -1 means end of input
  int64_t offset_ = 0;        // byte offset
  int64_t token_offset_ = 0;  // last token byte offset
  int64_t line_ = 1;          // current line number (1-based)
  int64_t token_line_ = 1;    // last token line
  int64_t scan_offset_ = 0;   // scanning byte offset

  int start_state_ = 0;  // lexer state, modifiable
};

inline std::ostream& operator<<(std::ostream& os, const Lexer& lexer) {
  return os << "Lexer at line " << lexer.Line() << " location "
            << lexer.TokenStartLocation() << " last token was \""
            << lexer.Text() << "\"";
}

}  // namespace json

#endif  // EXPERIMENTAL_USERS_FREMLIN_TEXTMAPPER_PARSERS_JSON_LEXER_H_
