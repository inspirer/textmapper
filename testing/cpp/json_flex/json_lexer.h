#ifndef EXAMPLES_JSON_FLEX_LEXER_H_
#define EXAMPLES_JSON_FLEX_LEXER_H_

#include <cstdint>
#include <ostream>
#include <tuple>

#include "absl/strings/string_view.h"
#include "json_token.h"

namespace json {

// Lexer is a handwritten adapter that is supposed to wrap a flex-generated
// lexer.
class Lexer {
 public:
  struct Location {
    Location(int64_t b = 0, int64_t e = 0) : begin(b), end(e) {}
    friend inline std::ostream& operator<<(std::ostream& os,
                                           const Location& l) {
      return os << "[" << l.begin << "-" << l.end << "]";
    }
    // Byte offsets into input buffer.
    int64_t begin;
    int64_t end;
  };

  explicit Lexer(absl::string_view input_source ABSL_ATTRIBUTE_LIFETIME_BOUND);

  // Next finds and returns the next token in source. The stream end is
  // indicated by Token.EOI.
  //
  // The token text can be retrieved later by calling the Text() method.
  ABSL_MUST_USE_RESULT Token Next();

  // Location of the last token returned by Next().
  ABSL_MUST_USE_RESULT Location LastTokenLocation() const {
    return Location(token_offset_, offset_);
  }
  // LastTokenLine returns the line number of the last token returned by Next()
  // (1-based).
  ABSL_MUST_USE_RESULT int64_t LastTokenLine() const { return token_line_; }

  // Text returns the substring of the input corresponding to the last token.
  ABSL_MUST_USE_RESULT absl::string_view Text() const
      ABSL_ATTRIBUTE_LIFETIME_BOUND {
    return source_.substr(token_offset_, offset_ - token_offset_);
  }

 private:
  absl::string_view source_;

  int64_t offset_ = 0;        // character offset
  int64_t token_offset_ = 0;  // last token byte offset
  int64_t token_line_ = 1;    // last token line
};

}  // namespace json

#endif  // EXAMPLES_JSON_FLEX_LEXER_H_
