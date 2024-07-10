// generated by Textmapper; DO NOT EDIT

#include "json_parser.h"

#include <cstdint>
#include <string>
#include <unordered_set>

#include "absl/strings/str_format.h"
#include "json_lexer.h"

namespace json {
[[maybe_unused]] constexpr int8_t fooState = 27;

std::unordered_set<int8_t> barStates = {
    0,
    2,
    20,
    32,
};

constexpr inline absl::string_view tmNonterminals[] = {
    "JSONText",
    "JSONValue",
    "JSONValue_A",
    "EmptyObject",
    "lookahead_EmptyObject",
    "JSONObject",
    "lookahead_notEmptyObject",
    "JSONMember",
    "JSONMemberList",
    "JSONArray",
    "JSONElementList",
    "JSONElementListopt",
    "JSONMember$1",
};
constexpr size_t tmNonterminalsLen =
    sizeof(tmNonterminals) / sizeof(tmNonterminals[0]);

std::string symbolName(int32_t sym) {
  if (sym == noToken) {
    return "<no-token>";
  }
  if (sym >= 0 && sym < static_cast<int32_t>(Token::NumTokens)) {
    return std::string(tokenStr[sym]);
  }
  if (sym >= static_cast<int32_t>(Token::NumTokens) &&
      sym - static_cast<int32_t>(Token::NumTokens) < tmNonterminalsLen) {
    return std::string(
        tmNonterminals[sym - static_cast<int32_t>(Token::NumTokens)]);
  }
  return absl::StrFormat("nonterminal(%d)", sym);
}

constexpr int32_t tmDefGoto[] = {
    -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1,
};

constexpr int32_t tmGoto[] = {
    37, 27, 54, 4, 19, 33, 38, 31, 24, 47, 59, 62, 40,
};

constexpr int32_t tmDefAct[] = {
    -1, 20, -1, 17, 18, 10, 11, 12, 13, 0,  15, -1, 14, -1, 16, 29,
    -1, -1, -1, -1, -1, 28, 19, 22, -1, 25, 26, -1, 30, 33, 21, -1,
    -1, 27, 8,  9,  1,  2,  3,  4,  24, 6,  5,  7,  -1, -1, -1,
};

constexpr int32_t tmActionBase = -20;

constexpr int32_t tmAction[] = {
    14,  -20, -2,  -20, -20, -20, -20, -20, -20, -20, -20, -1,
    -20, 5,   -20, -20, 50,  3,   8,   49,  14,  -20, -20, -20,
    11,  -20, -20, 20,  -20, -20, -20, 52,  30,  -20, -20, -20,
    -20, -20, -20, -20, -20, -20, -20, -20, 22,  -20, -20,
};

constexpr int32_t tmTableLen = 80;

constexpr int8_t tmTable[] = {
    34,  -20, -4, 32, 10,  45, 10,  -21, -23, -5,  -6,  -24, -7,  -8,  -9,  -10,
    34,  -31, -4, 11, 11,  11, -48, -32, 10,  -5,  -6,  -33, -7,  -8,  -9,  -10,
    34,  12,  -4, 12, 41,  44, 13,  11,  13,  -36, -37, 27,  -38, -39, -40, 14,
    -41, 14,  26, 11, -25, 12, 9,   31,  15,  -22, 13,  40,  -26, 16,  33,  -26,
    17,  42,  0,  14, -27, 32, 13,  -27, 0,   0,   28,  0,   0,   0,   0,   43,
};

constexpr int8_t tmCheck[] = {
    2,  2,  4,  5,  0,  1,  2,  2,  5,  11, 12, 3,  14, 15, 16, 17,
    2,  6,  4,  0,  1,  2,  0,  3,  20, 11, 12, 7,  14, 15, 16, 17,
    2,  0,  4,  2,  32, 0,  0,  20, 2,  11, 12, 19, 14, 15, 16, 0,
    18, 2,  19, 32, 3,  20, 0,  5,  2,  7,  20, 32, 11, 2,  31, 11,
    2,  32, -1, 20, 19, 29, 32, 19, -1, -1, 20, -1, -1, -1, -1, 32,
};

constexpr int8_t tmRuleLen[] = {
    1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1,
    1, 3, 0, 4, 3, 0, 4, 1, 1, 3, 3, 1, 3, 1, 0, 0, 0,
};

constexpr int32_t tmRuleSymbol[] = {
    20, 21, 21, 21, 21, 21, 21, 21, 21, 21, 22, 22, 22, 22, 22, 22, 22, 22,
    22, 23, 24, 25, 25, 26, 27, 27, 28, 28, 29, 30, 30, 31, 31, 32, 26,
};

constexpr uint32_t tmRuleType[] = {
    static_cast<uint32_t>(NodeType::JSONText),   // JSONText : JSONValue_A
    static_cast<uint32_t>(NodeType::JSONValue),  // JSONValue : kw_null
    static_cast<uint32_t>(NodeType::JSONValue),  // JSONValue : 'true'
    static_cast<uint32_t>(NodeType::JSONValue),  // JSONValue : 'false'
    static_cast<uint32_t>(NodeType::JSONValue),  // JSONValue : 'B'
    static_cast<uint32_t>(NodeType::JSONValue),  // JSONValue : JSONObject
    static_cast<uint32_t>(NodeType::JSONValue),  // JSONValue : EmptyObject
    static_cast<uint32_t>(NodeType::JSONValue),  // JSONValue : JSONArray
    static_cast<uint32_t>(NodeType::JSONValue),  // JSONValue : JSONString
    static_cast<uint32_t>(NodeType::JSONValue),  // JSONValue : JSONNumber
    static_cast<uint32_t>(NodeType::JSONValue),  // JSONValue_A : kw_null
    static_cast<uint32_t>(NodeType::JSONValue),  // JSONValue_A : 'true'
    static_cast<uint32_t>(NodeType::JSONValue),  // JSONValue_A : 'false'
    static_cast<uint32_t>(NodeType::JSONValue),  // JSONValue_A : 'A'
    static_cast<uint32_t>(NodeType::JSONValue),  // JSONValue_A : JSONObject
    static_cast<uint32_t>(NodeType::JSONValue),  // JSONValue_A : EmptyObject
    static_cast<uint32_t>(NodeType::JSONValue),  // JSONValue_A : JSONArray
    static_cast<uint32_t>(NodeType::JSONValue),  // JSONValue_A : JSONString
    static_cast<uint32_t>(NodeType::JSONValue),  // JSONValue_A : JSONNumber
    static_cast<uint32_t>(
        NodeType::EmptyObject),  // EmptyObject : lookahead_EmptyObject '{' '}'
    0,                           // lookahead_EmptyObject :
    static_cast<uint32_t>(NodeType::JSONObject) +
        (static_cast<uint32_t>(NodeFlags::Foo)
         << 16),  // JSONObject : lookahead_notEmptyObject '{' JSONMemberList
                  // '}'
    static_cast<uint32_t>(NodeType::JSONObject) +
        (static_cast<uint32_t>(NodeFlags::Foo)
         << 16),  // JSONObject : lookahead_notEmptyObject '{' '}'
    0,            // lookahead_notEmptyObject :
    static_cast<uint32_t>(NodeType::JSONMember) +
        (static_cast<uint32_t>(NodeFlags::Foo)
         << 16),  // JSONMember : JSONString ':' JSONMember$1 JSONValue
    static_cast<uint32_t>(NodeType::SyntaxProblem),  // JSONMember : error
    0,  // JSONMemberList : JSONMember
    0,  // JSONMemberList : JSONMemberList .foo ',' JSONMember
    static_cast<uint32_t>(NodeType::JSONArray) +
        (static_cast<uint32_t>(NodeFlags::Foo)
         << 16),  // JSONArray : .bar '[' JSONElementListopt ']'
    0,            // JSONElementList : JSONValue_A
    0,            // JSONElementList : JSONElementList ',' JSONValue_A
    0,            // JSONElementListopt : JSONElementList
    0,            // JSONElementListopt :
    0,            // JSONMember$1 :
};

// set(first JSONValue_A) = LBRACE, LBRACK, JSONSTRING, JSONNUMBER, KW_NULL,
// TRUE, FALSE, CHAR_A
[[maybe_unused]] constexpr int32_t Literals[] = {
    2, 4, 11, 12, 14, 15, 16, 17,
};

// set(follow ERROR) = RBRACE, COMMA
[[maybe_unused]] constexpr int32_t afterErr[] = {
    3,
    7,
};

int8_t gotoState(int8_t state, int32_t symbol) {
  if (symbol >= static_cast<int32_t>(Token::NumTokens)) {
    int32_t pos =
        tmGoto[symbol - static_cast<int32_t>(Token::NumTokens)] + state;
    if (pos >= 0 && pos < tmTableLen && tmCheck[pos] == state) {
      return tmTable[pos];
    }
    return tmDefGoto[symbol - static_cast<int32_t>(Token::NumTokens)];
  }

  // Shifting a token.
  int32_t action = tmAction[state];
  if (action == tmActionBase) {
    return -1;
  }
  int32_t pos = action + symbol;
  if (pos >= 0 && pos < tmTableLen && tmCheck[pos] == symbol) {
    action = tmTable[pos];
  } else {
    action = tmDefAct[state];
  }
  if (action < -1) {
    return -2 - action;
  }
  return -1;
}

ABSL_MUST_USE_RESULT int32_t lookaheadNext(Lexer& lexer) {
  Token tok;
restart:
  tok = lexer.Next();
  switch (tok) {
    case Token::MULTILINECOMMENT:
    case Token::INVALID_TOKEN:
      goto restart;
    default:
      break;
  }
  return static_cast<int32_t>(tok);
}

ABSL_MUST_USE_RESULT bool lookahead(Lexer& lexer_to_copy, int32_t next,
                                    int8_t start, int8_t end) {
  Lexer lexer = lexer_to_copy;

  std::vector<stackEntry> stack;
  stack.reserve(64);

  int8_t state = start;
  stack.push_back(stackEntry{.state = state});

  while (state != end) {
    int32_t action = tmAction[state];
    if (action > tmActionBase) {
      // Lookahead is needed.
      if (next == noToken) {
        next = lookaheadNext(lexer);
      }
      int32_t pos = action + next;
      if (pos >= 0 && pos < tmTableLen && tmCheck[pos] == next) {
        action = tmTable[pos];
      } else {
        action = tmDefAct[state];
      }
    } else {
      action = tmDefAct[state];
    }

    if (action >= 0) {
      // Reduce.
      int32_t rule = action;
      auto ln = static_cast<int32_t>(tmRuleLen[rule]);

      stackEntry entry;
      entry.sym.symbol = tmRuleSymbol[rule];
      stack.resize(stack.size() - ln);
      if (debugSyntax) {
        LOG(INFO) << "lookahead reduced to: " << symbolName(entry.sym.symbol);
      }
      state = gotoState(stack.back().state, entry.sym.symbol);
      entry.state = state;
      stack.push_back(std::move(entry));
    } else if (action < -1) {
      // Shift.
      state = -2 - action;
      stack.push_back(stackEntry{
          .sym = symbol{.symbol = next},
          .state = state,
      });
      if (debugSyntax) {
        LOG(INFO) << "lookahead shift: " << symbolName(next) << " ("
                  << lexer.Text() << ")";
      }
      if (state != -1 && next != eoiToken) {
        next = noToken;
      }
    }

    if (action == -1 || state == -1) {
      break;
    }
  }

  if (debugSyntax) {
    LOG(INFO) << "lookahead done: " << ((state == end) ? "true" : "false");
  }
  return state == end;
}

ABSL_MUST_USE_RESULT bool AtEmptyObject(Lexer& lexer, int32_t next) {
  if (debugSyntax) {
    LOG(INFO) << "lookahead EmptyObject; next: " << symbolName(next);
  }
  return lookahead(lexer, next, 1, 45);
}

void Parser::reportIgnoredToken(symbol sym) {
  NodeType t = NodeType::NoType;
  NodeFlags flags = NodeFlags::None;
  switch (Token(sym.symbol)) {
    case Token::MULTILINECOMMENT:
      t = NodeType::MultiLineComment;
      flags = static_cast<NodeFlags>(NodeFlags::Bar | NodeFlags::Foo);
      break;
    case Token::INVALID_TOKEN:
      t = NodeType::InvalidToken;
      break;
    default:
      return;
  }
  if (debugSyntax) {
    LOG(INFO) << "ignored: " << Token(sym.symbol) << " as " << t;
  }
  listener_(t, flags, sym.location);
}

bool Parser::willShift(int32_t symbol, std::vector<stackEntry>& stack, int size,
                       int8_t state) {
  if (state == -1) {
    return false;
  }
  absl::InlinedVector<int8_t, 4> stack2 = {state};

  // parsing_stack = stack[:size] + stack2
  while (state != end_state_) {
    int32_t action = tmAction[state];
    if (action > tmActionBase) {
      int32_t pos = action + symbol;
      if (pos >= 0 && pos < tmTableLen && tmCheck[pos] == symbol) {
        action = tmTable[pos];
      } else {
        action = tmDefAct[state];
      }
    } else {
      action = tmDefAct[state];
    }

    if (action >= 0) {
      // Reduce.
      int32_t rule = action;
      int32_t ln = tmRuleLen[rule];
      int32_t symbol = tmRuleSymbol[rule];

      if (ln > 0) {
        if (ln < stack2.size()) {
          state = stack2[stack2.size() - ln - 1];
          stack2.resize(stack2.size() - ln);
        } else {
          size -= ln - stack2.size();
          state = stack[size - 1].state;
          stack2.clear();
        }
      }
      state = gotoState(state, symbol);
      stack2.push_back(state);
    } else {
      return action < -1;
    }
  }
  return symbol == eoiToken;
}

int64_t Parser::skipBrokenCode(
    Lexer& lexer, std::vector<stackEntry>& stack,
    std::bitset<static_cast<size_t>(Token::NumTokens)>& recover_tokens) {
  int64_t ret = 0;
  while (next_symbol_.symbol != eoiToken &&
         !recover_tokens[next_symbol_.symbol]) {
    if (debugSyntax) {
      LOG(INFO) << "skipped while recovering: "
                << symbolName(next_symbol_.symbol) << " (" << lexer.Text()
                << ")";
    }
    if (!pending_symbols_.empty()) {
      for (const auto& tok : pending_symbols_) {
        reportIgnoredToken(tok);
      }
      pending_symbols_.clear();
    }
    switch (Token(next_symbol_.symbol)) {
      case Token::JSONSTRING:
        listener_(NodeType::JsonString, NodeFlags::None, next_symbol_.location);
        break;
      default:
        break;
    }
    ret = next_symbol_.location.end;
    fetchNext(lexer, stack);
  }
  return ret;
}

bool Parser::recoverFromError(Lexer& lexer, std::vector<stackEntry>& stack) {
  std::bitset<static_cast<size_t>(Token::NumTokens)> recover_tokens;
  std::vector<int> recover_pos;

  if (debugSyntax) {
    LOG(INFO) << "broke at " << symbolName(next_symbol_.symbol) << " ("
              << lexer.Text() << ")";
  }

  for (size_t size = stack.size(); size > 0; size--) {
    if (gotoState(stack[size - 1].state, errSymbol) == -1) {
      continue;
    }
    recover_pos.push_back(size);
  }
  if (recover_pos.empty()) {
    return false;
  }

  for (int32_t v : afterErr) {
    recover_tokens[v] = true;
  }
  if (next_symbol_.symbol == noToken) {
    fetchNext(lexer, stack);
  }
  // By default, insert 'error' in front of the next token.
  int64_t begin = next_symbol_.location.begin;
  int64_t end = begin;
  for (const auto& tok : pending_symbols_) {
    // Try to cover all nearby invalid tokens.
    if (Token(tok.symbol) == Token::INVALID_TOKEN) {
      if (begin > tok.location.begin) {
        begin = tok.location.begin;
      }
      end = tok.location.end;
    }
  }
  for (;;) {
    int64_t skip_end = skipBrokenCode(lexer, stack, recover_tokens);
    if (skip_end > end) {
      end = skip_end;
    }

    int matching_pos = 0;
    if (debugSyntax) {
      LOG(INFO) << "trying to recover on " << symbolName(next_symbol_.symbol);
    }
    for (int pos : recover_pos) {
      if (willShift(next_symbol_.symbol, stack, pos,
                    gotoState(stack[pos - 1].state, errSymbol))) {
        matching_pos = pos;
        break;
      }
    }
    if (matching_pos == 0) {
      if (next_symbol_.symbol == eoiToken) {
        return false;
      }
      recover_tokens[next_symbol_.symbol] = false;
      continue;
    }

    if (matching_pos < stack.size()) {
      if (begin == end) {
        // Avoid producing syntax problems covering trailing whitespace.
        end = stack.back().sym.location.end;
      }
      begin = stack[matching_pos].sym.location.begin;
    } else if (begin == end && !pending_symbols_.empty()) {
      // This means pending tokens don't contain InvalidTokens.
      for (const auto& tok : pending_symbols_) {
        reportIgnoredToken(tok);
      }
      pending_symbols_.clear();
    }
    if (begin != end) {
      // Consume trailing invalid tokens.
      for (const auto& tok : pending_symbols_) {
        if (Token(tok.symbol) == Token::INVALID_TOKEN &&
            tok.location.end > end) {
          end = tok.location.end;
        }
      }
      int consumed = 0;
      for (; consumed < pending_symbols_.size(); consumed++) {
        auto& tok = pending_symbols_[consumed];
        if (tok.location.begin >= end) {
          break;
        }
        reportIgnoredToken(tok);
      }
      pending_symbols_.erase(pending_symbols_.begin(),
                             pending_symbols_.begin() + consumed);
    }
    if (debugSyntax) {
      for (int i = stack.size() - 1; i >= matching_pos; i--) {
        LOG(INFO) << "dropped from stack: " << symbolName(stack[i].sym.symbol);
      }
      LOG(INFO) << "recovered";
    }
    stack.resize(matching_pos);
    stack.push_back(stackEntry{
        .sym =
            symbol{
                .symbol = errSymbol,
                .location = Lexer::Location(begin, end),
            },
        .state = gotoState(stack[matching_pos - 1].state, errSymbol),
    });
    return true;
  }
}

void Parser::fetchNext(Lexer& lexer, std::vector<stackEntry>& stack) {
  Token tok;
  for (;;) {
    tok = lexer.Next();
    switch (tok) {
      case Token::MULTILINECOMMENT:
      case Token::INVALID_TOKEN:
        pending_symbols_.push_back(
            symbol{static_cast<int32_t>(tok), lexer.LastTokenLocation()});
        continue;
      default:
        break;
    }
    break;
  }

  next_symbol_.symbol = static_cast<int32_t>(tok);
  next_symbol_.location = lexer.LastTokenLocation();
}

absl::Status Parser::action0([[maybe_unused]] stackEntry& lhs,
                             [[maybe_unused]] const stackEntry* rhs) {
  { lhs.value.b = rhs[0].value.a; }
  return absl::OkStatus();
}
absl::Status Parser::action3([[maybe_unused]] stackEntry& lhs,
                             [[maybe_unused]] const stackEntry* rhs) {
  { lhs.value.a = 5; }
  return absl::OkStatus();
}
absl::Status Parser::action12([[maybe_unused]] stackEntry& lhs,
                              [[maybe_unused]] const stackEntry* rhs) {
  { lhs.value.a = 5; }
  return absl::OkStatus();
}
absl::Status Parser::action19([[maybe_unused]] stackEntry& lhs,
                              [[maybe_unused]] const stackEntry* rhs) {
  { lhs.sym.location.begin = rhs[1].sym.location.begin; }
  return absl::OkStatus();
}
absl::Status Parser::action21([[maybe_unused]] stackEntry& lhs,
                              [[maybe_unused]] const stackEntry* rhs) {
  { lhs.sym.location.begin = rhs[1].sym.location.begin; }
  return absl::OkStatus();
}
absl::Status Parser::action22([[maybe_unused]] stackEntry& lhs,
                              [[maybe_unused]] const stackEntry* rhs) {
  { lhs.sym.location.begin = rhs[1].sym.location.begin; }
  return absl::OkStatus();
}
absl::Status Parser::action24([[maybe_unused]] stackEntry& lhs,
                              [[maybe_unused]] const stackEntry* rhs) {
  { lhs.value.c = a; }
  return absl::OkStatus();
}
absl::Status Parser::action26([[maybe_unused]] stackEntry& lhs,
                              [[maybe_unused]] const stackEntry* rhs) {
  { lhs.value.d = b; }
  return absl::OkStatus();
}
absl::Status Parser::action33([[maybe_unused]] stackEntry& lhs,
                              [[maybe_unused]] const stackEntry* rhs) {
  { LOG(INFO) << rhs[-1].sym.location.begin; }
  return absl::OkStatus();
}

absl::Status Parser::applyRule(int32_t rule, stackEntry& lhs,
                               [[maybe_unused]] const stackEntry* rhs,
                               Lexer& lexer) {
  switch (rule) {
    case 0:  // JSONText : JSONValue_A
    {
      absl::Status action_result = action0(lhs, rhs);
      if (!action_result.ok()) {
        return action_result;
      }
    } break;
    case 3:  // JSONValue : 'false'
    {
      absl::Status action_result = action3(lhs, rhs);
      if (!action_result.ok()) {
        return action_result;
      }
    } break;
    case 12:  // JSONValue_A : 'false'
    {
      absl::Status action_result = action12(lhs, rhs);
      if (!action_result.ok()) {
        return action_result;
      }
    } break;
    case 19:  // EmptyObject : lookahead_EmptyObject '{' '}'
    {
      absl::Status action_result = action19(lhs, rhs);
      if (!action_result.ok()) {
        return action_result;
      }
    } break;
    case 21:  // JSONObject : lookahead_notEmptyObject '{' JSONMemberList '}'
    {
      absl::Status action_result = action21(lhs, rhs);
      if (!action_result.ok()) {
        return action_result;
      }
    } break;
    case 22:  // JSONObject : lookahead_notEmptyObject '{' '}'
    {
      absl::Status action_result = action22(lhs, rhs);
      if (!action_result.ok()) {
        return action_result;
      }
    } break;
    case 24:  // JSONMember : JSONString ':' JSONMember$1 JSONValue
    {
      absl::Status action_result = action24(lhs, rhs);
      if (!action_result.ok()) {
        return action_result;
      }
    } break;
    case 26:  // JSONMemberList : JSONMember
    {
      absl::Status action_result = action26(lhs, rhs);
      if (!action_result.ok()) {
        return action_result;
      }
    } break;
    case 33:  // JSONMember$1 :
    {
      absl::Status action_result = action33(lhs, rhs);
      if (!action_result.ok()) {
        return action_result;
      }
    } break;

    case 34:
      if (AtEmptyObject(lexer, next_symbol_.symbol)) {
        lhs.sym.symbol = 24; /* lookahead_EmptyObject */
      } else {
        lhs.sym.symbol = 26; /* lookahead_notEmptyObject */
      }
      return absl::OkStatus();
    default:
      break;
  }

  uint32_t nt = tmRuleType[rule];
  if (nt != 0) {
    listener_(static_cast<NodeType>(nt & 0xffff),
              static_cast<NodeFlags>(nt >> 16), lhs.sym.location);
  }
  return absl::OkStatus();
}

absl::Status Parser::Parse(int8_t start, int8_t end, Lexer& lexer) {
  pending_symbols_.clear();
  int8_t state = start;
  absl::Status lastErr = absl::OkStatus();
  Lexer::Location lastLoc;
  int recovering = 0;

  std::vector<stackEntry> stack;
  stack.reserve(startStackSize);
  stack.push_back(stackEntry{.state = state});
  end_state_ = end;
  fetchNext(lexer, stack);

  while (state != end) {
    int32_t action = tmAction[state];
    if (action > tmActionBase) {
      // Lookahead is needed.
      if (next_symbol_.symbol == noToken) {
        fetchNext(lexer, stack);
      }
      int32_t pos = action + next_symbol_.symbol;
      if (pos >= 0 && pos < tmTableLen && tmCheck[pos] == next_symbol_.symbol) {
        action = tmTable[pos];
      } else {
        action = tmDefAct[state];
      }
    } else {
      action = tmDefAct[state];
    }

    if (action >= 0) {
      // Reduce.
      int32_t rule = action;
      int32_t ln = tmRuleLen[rule];
      stackEntry entry;
      entry.sym.symbol = tmRuleSymbol[rule];
      const stackEntry* rhs = &stack[0] + stack.size() - ln;

      if (ln == 0) {
        entry.sym.location = Lexer::Location(stack.back().sym.location.end,
                                             stack.back().sym.location.end);
        entry.value = stack.back().value;
      } else {
        entry.sym.location = Lexer::Location(rhs[0].sym.location.begin,
                                             rhs[ln - 1].sym.location.end);
        entry.value = rhs[0].value;
      }
      absl::Status ret = applyRule(rule, entry, rhs, lexer);
      if (!ret.ok()) {
        return ret;
      }
      // Avoid resizing twice, by keeping an extra token at the end.
      stack.resize(stack.size() - ln + 1);
      if (debugSyntax) {
        LOG(INFO) << "reduced to: " << symbolName(entry.sym.symbol)
                  << " consuming " << ln << " symbols, range "
                  << entry.sym.location;
      }
      state = gotoState(stack[stack.size() - 2].state, entry.sym.symbol);
      entry.state = state;
      stack.back() = std::move(entry);

    } else if (action < -1) {
      // Shift.
      state = -2 - action;
      if (debugSyntax) {
        LOG(INFO) << "shift: " << symbolName(next_symbol_.symbol) << " ("
                  << lexer.Text() << ")";
      }
      stack.emplace_back(stackEntry{
          .sym = std::move(next_symbol_),
          .state = state,
      });
      if (!pending_symbols_.empty()) {
        for (const auto& tok : pending_symbols_) {
          reportIgnoredToken(tok);
        }
        pending_symbols_.clear();
      }
      if (next_symbol_.symbol != eoiToken) {
        switch (Token(next_symbol_.symbol)) {
          case Token::JSONSTRING:
            listener_(NodeType::JsonString, NodeFlags::None,
                      next_symbol_.location);
            break;
          default:
            break;
        }
        next_symbol_.symbol = noToken;
      }
      if (recovering > 0) {
        recovering--;
      }
    }
    if (action == -1 || state == -1) {
      if (recovering == 0) {
        if (next_symbol_.symbol == noToken) {
          fetchNext(lexer, stack);
        }
        lastErr = absl::InvalidArgumentError(absl::StrFormat(
            "Syntax error: line %d: %s", lexer.LastTokenLine(), lexer.Text()));
        if (!error_handler_(lastErr)) {
          if (!pending_symbols_.empty()) {
            for (const auto& tok : pending_symbols_) {
              reportIgnoredToken(tok);
            }
            pending_symbols_.clear();
          }
          return lastErr;
        }
      }

      recovering = 4;
      if (!recoverFromError(lexer, stack)) {
        if (!pending_symbols_.empty()) {
          for (const auto& tok : pending_symbols_) {
            reportIgnoredToken(tok);
          }
          pending_symbols_.clear();
        }
        return lastErr;
      }
      state = stack[stack.size() - 1].state;
    }
  }

  return absl::OkStatus();
}
}  // namespace json
