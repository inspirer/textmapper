// generated by Textmapper; DO NOT EDIT

#ifndef EXAMPLES_JSON_TOKEN_H_
#define EXAMPLES_JSON_TOKEN_H_

#include <array>
#include <cstdint>
#include <ostream>

#include "absl/strings/string_view.h"

namespace json {

// Token is an enum of all terminal symbols of the json language.
enum class Token {
  UNAVAILABLE = -1,
  EOI = 0,
  YYerror = 1,
  INVALID_TOKEN = 2,
  LBRACE = 3,
  RBRACE = 4,
  LBRACK = 5,
  RBRACK = 6,
  COLON = 7,
  COMMA = 8,  // comma
  MULTILINECOMMENT = 9,
  JSONSTRING = 10,  // "string literal"
  JSONNUMBER = 11,
  ID = 12,
  KW_NULL = 13,
  TRUE = 14,
  FALSE = 15,
  NumTokens = 16
};

constexpr inline std::array<absl::string_view,
                            static_cast<size_t>(Token::NumTokens)>
    tokenStr = {
  "EOI",
  "YYerror",
  "INVALID_TOKEN",
  "LBRACE",
  "RBRACE",
  "LBRACK",
  "RBRACK",
  "COLON",
  "COMMA",  // comma
  "MULTILINECOMMENT",
  "JSONSTRING",  // "string literal"
  "JSONNUMBER",
  "ID",
  "KW_NULL",
  "TRUE",
  "FALSE",
};

constexpr inline std::array<absl::string_view,
                            static_cast<size_t>(Token::NumTokens)>
    tokenName = {
  "eoi",
  "error",
  "invalid_token",
  "'{'",
  "'}'",
  "'['",
  "']'",
  "':'",
  "','",  // comma
  "MultiLineComment",
  "JSONString",  // "string literal"
  "JSONNumber",
  "id",
  "kw_null",
  "'true'",
  "'false'",
};

inline std::ostream& operator<<(std::ostream& os, Token tok) {
  int t = static_cast<int>(tok);
  if (t >= 0 && t < tokenStr.size()) {
    return os << tokenStr[t];
  }
  return os << "token(" << t << ")";
}

}  // namespace json

#endif  // EXAMPLES_JSON_TOKEN_H_