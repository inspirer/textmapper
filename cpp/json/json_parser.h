// generated by Textmapper; DO NOT EDIT

#ifndef EXAMPLES_JSON_PARSER_H_
#define EXAMPLES_JSON_PARSER_H_

#include <array>
#include <cstdint>
#include <ostream>
#include <string>
#include <utility>
#include <vector>

#include "absl/base/attributes.h"
#include "absl/functional/function_ref.h"
#include "absl/log/log.h"
#include "absl/memory/memory.h"
#include "absl/status/status.h"
#include "absl/strings/match.h"
#include "absl/strings/str_format.h"
#include "absl/strings/str_join.h"
#include "json_lexer.h"

namespace json {

struct symbol {
  int32_t symbol = 0;
  Lexer::Location location = Lexer::Location();
};

struct stackEntry {
  symbol sym;
  int8_t state = 0;
  union {
    bool b;
    int a;
    int c;
    bool d;
  } value;
};

enum class NodeType {
  NoType,
  EmptyObject,
  JSONArray,
  JSONMember,
  JSONObject,
  JSONText,
  JSONValue,
  SyntaxProblem,
  MultiLineComment,
  InvalidToken,
  JsonString,
  NonExistingType,
  NodeTypeMax
};

enum NodeFlags {
  None = 0,
  Bar = 1u << 0,
  Foo = 1u << 1,
};

constexpr inline std::array<absl::string_view,
                            static_cast<size_t>(NodeType::NodeTypeMax)>
    nodeTypeStr = {
        "NONE",         "EmptyObject",   "JSONArray",
        "JSONMember",   "JSONObject",    "JSONText",
        "JSONValue",    "SyntaxProblem", "MultiLineComment",
        "InvalidToken", "JsonString",    "NonExistingType",
};

inline std::ostream& operator<<(std::ostream& os, NodeType t) {
  int i = static_cast<int>(t);
  if (i >= 0 && i < nodeTypeStr.size()) {
    return os << nodeTypeStr[i];
  }
  return os << "node(" << i << ")";
}

constexpr inline bool debugSyntax = true;
constexpr inline int startStackSize = 256;
constexpr inline int startTokenBufferSize = 16;
constexpr inline int32_t noToken = static_cast<int32_t>(Token::UNAVAILABLE);
constexpr inline int32_t eoiToken = static_cast<int32_t>(Token::EOI);
constexpr inline int32_t errSymbol = 19;

ABSL_MUST_USE_RESULT std::string symbolName(int32_t sym);

class Parser final {
 public:
  template <typename Listener>
  explicit Parser(Listener&& listener,
                  absl::FunctionRef<bool(absl::Status)> error_handler,
                  int a_arg, bool b_arg)
      : listener_(std::forward<Listener>(listener)),
        error_handler_(error_handler),
        a(a_arg),
        b(b_arg) {
    pending_symbols_.reserve(startTokenBufferSize);
  }

  absl::Status Parse(Lexer& lexer) { return Parse(0, 46, lexer); }

 private:
  void reportIgnoredToken(symbol sym);
  bool willShift(int32_t symbol, std::vector<stackEntry>& stack, int size,
                 int8_t state);
  int64_t skipBrokenCode(
      Lexer& lexer, std::vector<stackEntry>& stack,
      std::bitset<static_cast<size_t>(Token::NumTokens)>& recover_tokens);
  bool recoverFromError(Lexer& lexer, std::vector<stackEntry>& stack);
  void fetchNext(Lexer& lexer, std::vector<stackEntry>& stack);
  absl::Status action0([[maybe_unused]] stackEntry& lhs,
                       [[maybe_unused]] const stackEntry* rhs);
  absl::Status action3([[maybe_unused]] stackEntry& lhs,
                       [[maybe_unused]] const stackEntry* rhs);
  absl::Status action12([[maybe_unused]] stackEntry& lhs,
                        [[maybe_unused]] const stackEntry* rhs);
  absl::Status action19([[maybe_unused]] stackEntry& lhs,
                        [[maybe_unused]] const stackEntry* rhs);
  absl::Status action21([[maybe_unused]] stackEntry& lhs,
                        [[maybe_unused]] const stackEntry* rhs);
  absl::Status action22([[maybe_unused]] stackEntry& lhs,
                        [[maybe_unused]] const stackEntry* rhs);
  absl::Status action24([[maybe_unused]] stackEntry& lhs,
                        [[maybe_unused]] const stackEntry* rhs);
  absl::Status action26([[maybe_unused]] stackEntry& lhs,
                        [[maybe_unused]] const stackEntry* rhs);
  absl::Status action33([[maybe_unused]] stackEntry& lhs,
                        [[maybe_unused]] const stackEntry* rhs);

  absl::Status applyRule(int32_t rule, stackEntry& lhs,
                         [[maybe_unused]] const stackEntry* rhs, Lexer& lexer);
  absl::Status Parse(int8_t start, int8_t end, Lexer& lexer);

  symbol next_symbol_;
  absl::FunctionRef<void(NodeType, NodeFlags, Lexer::Location)> listener_;
  absl::FunctionRef<bool(absl::Status)>
      error_handler_;  // returns true to attempt recovery
  int a;
  bool b;
  // Tokens to be reported with the next shift. Only non-empty when next.symbol
  // != noToken.
  std::vector<symbol> pending_symbols_;

  int8_t end_state_ = 0;

  friend std::ostream& operator<<(std::ostream& os, const Parser& parser) {
    return os << "json::Parser next " << symbolName(parser.next_symbol_.symbol)
              << " and pending " << parser.pending_symbols_.size()
              << " symbols";
  }
};

}  // namespace json

#endif  // EXAMPLES_JSON_PARSER_H_