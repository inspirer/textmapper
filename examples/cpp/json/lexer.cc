// generated through human effort; PLEASE DO EDIT

#include "lexer.h"

#include "third_party/absl/log/log.h"
#include "third_party/absl/strings/match.h"

namespace json {

namespace {
constexpr int tmNumClasses = 24;

constexpr uint8_t tmRuneClass[] = {
    1,  1,  1,  1,  1,  1,  1,  1,  1,  2,  2,  1,  1,  2,  1,  1,  1,  1,
    1,  1,  1,  1,  1,  1,  1,  1,  1,  1,  1,  1,  1,  1,  2,  1,  3,  1,
    1,  1,  1,  1,  1,  1,  4,  5,  6,  7,  8,  9,  10, 11, 11, 11, 11, 11,
    11, 11, 11, 11, 12, 1,  1,  1,  1,  1,  1,  13, 13, 13, 13, 14, 13, 15,
    15, 15, 15, 15, 15, 15, 15, 15, 15, 15, 15, 15, 15, 15, 15, 15, 15, 15,
    15, 16, 17, 18, 1,  1,  1,  13, 19, 13, 13, 14, 19, 15, 15, 15, 15, 15,
    15, 15, 20, 15, 15, 15, 20, 15, 20, 21, 15, 15, 15, 15, 15, 22, 1,  23,
};

constexpr int tmRuneClassLen = 126;
constexpr int tmFirstRule = -3;

constexpr int tmStateMap[] = {
    0,
};

constexpr int8_t tmLexerAction[] = {
    -3,  -4,  27,  20,  -4,  -4,  19,  18,  -4,  14,  13,  7,   6,   5,   5,
    5,   4,   -4,  3,   5,   5,   5,   2,   1,   -6,  -6,  -6,  -6,  -6,  -6,
    -6,  -6,  -6,  -6,  -6,  -6,  -6,  -6,  -6,  -6,  -6,  -6,  -6,  -6,  -6,
    -6,  -6,  -6,  -5,  -5,  -5,  -5,  -5,  -5,  -5,  -5,  -5,  -5,  -5,  -5,
    -5,  -5,  -5,  -5,  -5,  -5,  -5,  -5,  -5,  -5,  -5,  -5,  -8,  -8,  -8,
    -8,  -8,  -8,  -8,  -8,  -8,  -8,  -8,  -8,  -8,  -8,  -8,  -8,  -8,  -8,
    -8,  -8,  -8,  -8,  -8,  -8,  -7,  -7,  -7,  -7,  -7,  -7,  -7,  -7,  -7,
    -7,  -7,  -7,  -7,  -7,  -7,  -7,  -7,  -7,  -7,  -7,  -7,  -7,  -7,  -7,
    -15, -15, -15, -15, -15, -15, -15, -15, -15, -15, 5,   5,   -15, 5,   5,
    5,   -15, -15, -15, 5,   5,   5,   -15, -15, -9,  -9,  -9,  -9,  -9,  -9,
    -9,  -9,  -9,  -9,  -9,  -9,  -9,  -9,  -9,  -9,  -9,  -9,  -9,  -9,  -9,
    -9,  -9,  -9,  -14, -14, -14, -14, -14, -14, -14, -14, -1,  -14, 7,   7,
    -14, -14, -2,  -14, -14, -14, -14, -14, -14, -14, -14, -14, -4,  -4,  -4,
    -4,  -4,  10,  -4,  10,  -4,  -4,  9,   9,   -4,  -4,  -4,  -4,  -4,  -4,
    -4,  -4,  -4,  -4,  -4,  -4,  -14, -14, -14, -14, -14, -14, -14, -14, -14,
    -14, 9,   9,   -14, -14, -14, -14, -14, -14, -14, -14, -14, -14, -14, -14,
    -4,  -4,  -4,  -4,  -4,  -4,  -4,  -4,  -4,  -4,  9,   9,   -4,  -4,  -4,
    -4,  -4,  -4,  -4,  -4,  -4,  -4,  -4,  -4,  -4,  -4,  -4,  -4,  -4,  -4,
    -4,  -4,  -4,  -4,  12,  12,  -4,  -4,  -4,  -4,  -4,  -4,  -4,  -4,  -4,
    -4,  -4,  -4,  -14, -14, -14, -14, -14, -14, -14, -14, -14, -14, 12,  12,
    -14, -14, -2,  -14, -14, -14, -14, -14, -14, -14, -14, -14, -14, -14, -14,
    -14, -14, -14, -14, -14, -1,  -14, -14, -14, -14, -14, -2,  -14, -14, -14,
    -14, -14, -14, -14, -14, -14, -4,  -4,  -4,  -4,  15,  -4,  -4,  -4,  -4,
    -4,  -4,  -4,  -4,  -4,  -4,  -4,  -4,  -4,  -4,  -4,  -4,  -4,  -4,  -4,
    -4,  15,  15,  15,  16,  15,  15,  15,  15,  15,  15,  15,  15,  15,  15,
    15,  15,  15,  15,  15,  15,  15,  15,  15,  -4,  15,  15,  15,  16,  15,
    15,  15,  15,  17,  15,  15,  15,  15,  15,  15,  15,  15,  15,  15,  15,
    15,  15,  15,  -12, -12, -12, -12, -12, -12, -12, -12, -12, -12, -12, -12,
    -12, -12, -12, -12, -12, -12, -12, -12, -12, -12, -12, -12, -4,  -4,  -4,
    -4,  -4,  -4,  -4,  -4,  -4,  -4,  13,  7,   -4,  -4,  -4,  -4,  -4,  -4,
    -4,  -4,  -4,  -4,  -4,  -4,  -10, -10, -10, -10, -10, -10, -10, -10, -10,
    -10, -10, -10, -10, -10, -10, -10, -10, -10, -10, -10, -10, -10, -10, -10,
    -4,  20,  20,  26,  20,  20,  20,  20,  20,  20,  20,  20,  20,  20,  20,
    20,  20,  21,  20,  20,  20,  20,  20,  20,  -4,  -4,  -4,  20,  -4,  -4,
    -4,  -4,  -4,  20,  -4,  -4,  -4,  -4,  -4,  -4,  -4,  20,  -4,  20,  20,
    22,  -4,  -4,  -4,  -4,  -4,  -4,  -4,  -4,  -4,  -4,  -4,  -4,  23,  23,
    -4,  23,  23,  -4,  -4,  -4,  -4,  23,  -4,  -4,  -4,  -4,  -4,  -4,  -4,
    -4,  -4,  -4,  -4,  -4,  -4,  -4,  24,  24,  -4,  24,  24,  -4,  -4,  -4,
    -4,  24,  -4,  -4,  -4,  -4,  -4,  -4,  -4,  -4,  -4,  -4,  -4,  -4,  -4,
    -4,  25,  25,  -4,  25,  25,  -4,  -4,  -4,  -4,  25,  -4,  -4,  -4,  -4,
    -4,  -4,  -4,  -4,  -4,  -4,  -4,  -4,  -4,  -4,  20,  20,  -4,  20,  20,
    -4,  -4,  -4,  -4,  20,  -4,  -4,  -4,  -4,  -13, -13, -13, -13, -13, -13,
    -13, -13, -13, -13, -13, -13, -13, -13, -13, -13, -13, -13, -13, -13, -13,
    -13, -13, -13, -11, -11, 27,  -11, -11, -11, -11, -11, -11, -11, -11, -11,
    -11, -11, -11, -11, -11, -11, -11, -11, -11, -11, -11, -11,
};

constexpr int tmBacktracking[] = {
    11, 11,  // in JSONNumber
    11, 8,   // in JSONNumber
};
}  // namespace

Token Lexer::Next() {
restart:
  token_line_ = line_;
  token_offset_ = offset_;

  int state = tmStateMap[start_state_];
  uint32_t hash = 0;
  Token backupToken = Token::EOI;
  uint64_t backupOffset;
  uint32_t backupHash = hash;

  while (state >= 0) {
    int curr_class;
    if (input_rune_ < 0) {
      state = tmLexerAction[state * tmNumClasses];
      if (state > tmFirstRule && state < 0) {
        state = (-1 - state) * 2;
        backupToken = Token(tmBacktracking[state]);
        backupOffset = offset_;
        backupHash = hash;
        state = tmBacktracking[state + 1];
      }
      continue;
    } else if (input_rune_ < tmRuneClassLen) {
      curr_class = tmRuneClass[input_rune_];
    } else {
      curr_class = 1;
    }
    state = tmLexerAction[state * tmNumClasses + curr_class];
    if (state > tmFirstRule) {
      if (state < 0) {
        state = (-1 - state) * 2;
        backupToken = Token(tmBacktracking[state]);
        backupOffset = offset_;
        backupHash = hash;
        state = tmBacktracking[state + 1];
      }
      hash = hash * 31 + static_cast<uint32_t>(input_rune_);

      if (input_rune_ == '\n') {
        line_++;
      }

      // Scan the next character.
      // Note: the following code is inlined to avoid performance
      // implications.
      offset_ = scan_offset_;
      if (offset_ < source_.size()) {
        // UTF8 is not supported.
        input_rune_ = static_cast<uint8_t>(source_[scan_offset_++]);
      } else {
        input_rune_ = -1;
      }
    }
  }

  Token tok = Token(tmFirstRule - state);
recovered:
  switch (tok) {
    case Token::ID: {
      uint32_t hh = hash & 7;
      switch (hh) {
        case 1:
          if (hash == 0x41 && 'A' == source_[token_offset_]) {
            tok = Token::CHAR_A;
          }
          break;
        case 2:
          if (hash == 0x42 && 'B' == source_[token_offset_]) {
            tok = Token::CHAR_B;
          }
          break;
        case 3:
          if (hash == 0x5cb1923 && "false" == Text()) {
            tok = Token::FALSE;
          }
          break;
        case 6:
          if (hash == 0x36758e && "true" == Text()) {
            tok = Token::TRUE;
          }
          break;
        case 7:
          if (hash == 0x33c587 && "null" == Text()) {
            tok = Token::JSON_NULL;
          }
          break;
      }
      break;
    }
    default:
      break;
  }
  switch (tok) {
    case Token::INVALID_TOKEN:
      if (static_cast<int>(backupToken) >= 0) {
        tok = Token(backupToken);
        hash = backupHash;
        Rewind(backupOffset);
      } else if (offset_ == token_offset_) {
        Rewind(scan_offset_);
      }
      if (tok != Token::INVALID_TOKEN) {
        goto recovered;
      }
      break;
    case Token(8):
      goto restart;
    default:
      break;
  }
  VLOG(2) << "Lexer::Next returning next token " << tok << " now " << *this;
  return tok;
}

Lexer::Lexer(absl::string_view input_source) {
  source_ = input_source;
  if (absl::StartsWith(source_, bomSeq)) {
    offset_ += bomSeq.size();
  }
  Rewind(offset_);
}

void Lexer::Rewind(int64_t rewind_offset) {
  if (rewind_offset < offset_) {
    for (int64_t i = rewind_offset; i < offset_; ++i) {
      if (source_[i] == '\n') {
        line_--;
      }
    }
  } else {
    if (rewind_offset > source_.size()) {
      rewind_offset = source_.size();
    }
    for (int64_t i = offset_; i < rewind_offset; ++i) {
      if (source_[i] == '\n') {
        line_++;
      }
    }
  }

  // Scan the next character.
  scan_offset_ = rewind_offset;
  offset_ = rewind_offset;
  if (offset_ < source_.size()) {
    input_rune_ = source_[scan_offset_++];
  } else {
    input_rune_ = -1;  // Invalid rune for end of input
  }
}
}  // namespace json
