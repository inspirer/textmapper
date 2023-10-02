#include "json_lexer.h"

#include <sstream>
#include <vector>

#include "gmock/gmock.h"
#include "gtest/gtest.h"
#include "markup/markup.h"

namespace json {
namespace {

struct Test {
  std::string name;
  Token tok;
  std::vector<std::string> cases;
};

inline std::ostream &operator<<(std::ostream &os, Test t) {
  return os << "{" << t.tok << ", " << t.cases.size() << " cases}";
}

const std::vector<Test> tests = {
    {"id",
     Token::ID,
     {
         R"( 0«foo» «barB1»)",
     }},
    {"string",
     Token::JSONSTRING,
     {
         R"(«"foo"» «"b\nar"»)",
     }},
    {"number",
     Token::JSONNUMBER,
     {
         "«1» «534»",
         "«1e9» «1.2» «1e-2»",
     }},
    {"true",
     Token::TRUE,
     {
         "«true»",
         "/* true */ «true» ",
     }},

    {"false", Token::FALSE, {"  «false» "}},
    {"null", Token::KW_NULL, {"  «null» "}},

    {"lbrace", Token::LBRACE, {"«{»"}},
    {"rbrace", Token::RBRACE, {"«}»"}},
    {"lbrack", Token::LBRACK, {"«[»"}},
    {"rbrack", Token::RBRACK, {"«]»"}},
    {"colon", Token::COLON, {"«:»"}},
    {"comma", Token::COMMA, {"«,»"}},

    {"comment", Token::MULTILINECOMMENT, {"  «/*  asda *** */» bar"}},

    // TODO: handle invalid tokens
    //
    // {"invalid",
    //  Token::INVALID_TOKEN,
    //  {
    //      "«1e» ",
    //      "abc   «/*1e  \n»",
    //      "abc   «\"  »\n",
    //  }},
};

class LexerTest : public testing::TestWithParam<Test> {};

TEST_P(LexerTest, Token) {
  const auto &param = GetParam();
  for (const auto &input : param.cases) {
    std::vector<markup::Range> want;
    std::string text;
    tie(want, text) = markup::Parse(input);

    Lexer l(text);
    std::vector<markup::Range> tokens;
    Token next;
    while ((next = l.Next()) != Token::EOI) {
      if (next == param.tok) {
        auto loc = l.LastTokenLocation();
        tokens.push_back(
            markup::Range{loc.begin, loc.end});
      }
    }

    EXPECT_THAT(tokens, testing::ElementsAreArray(want))
        << "lexer produced " << markup::Create(text, tokens) << " instead of "
        << markup::Create(text, want);
  }
}

INSTANTIATE_TEST_SUITE_P(Vals, LexerTest, testing::ValuesIn(tests),
                         [](const ::testing::TestParamInfo<Test> &info) {
                           return info.param.name;
                         });

}  // namespace
}  // namespace json
