#include "lexer.h"

#include <sstream>
#include <vector>

#include "gmock/gmock.h"
#include "gtest/gtest.h"
#include "markup/markup.h"

namespace json {
namespace {

struct Test {
  Token tok;
  std::vector<std::string> cases;
};

inline std::ostream& operator<<(std::ostream& os, Test t) {
  return os << "{" << t.tok << ", " << t.cases.size() << " cases}";
}

const std::vector<Test> tests = {
    {Token::JSONNUMBER,
     {
         "«1» «534»",
         "«1e9» «1.2» «1e-2»",
     }},
    {Token::TRUE,
     {
         "«true»",
         "/* true */ «true» ",
     }},
};

class LexerTest : public testing::TestWithParam<Test> {};

TEST_P(LexerTest, Token) {
  const auto& param = GetParam();
  for (const auto& input : param.cases) {
    std::vector<markup::Range> want;
    std::string text;
    tie(want, text) = markup::Parse(input);

    Lexer l(text);
    std::vector<markup::Range> tokens;
    Token next;
    while ((next = l.Next()) != Token::EOI) {
      if (next == param.tok) {
        tokens.push_back(
            markup::Range{l.TokenStartLocation(), l.TokenEndLocation()});
      }
    }

    EXPECT_THAT(tokens, testing::ElementsAreArray(want))
        << "lexer produced " << markup::Create(text, tokens) << " instead of "
        << markup::Create(text, want);
  }
}

INSTANTIATE_TEST_SUITE_P(Vals, LexerTest, testing::ValuesIn(tests),
                         [](const ::testing::TestParamInfo<Test>& info) {
                           std::ostringstream os;
                           os << info.param.tok;
                           return os.str();
                         });

const char* json_example = R"(
{
    "some key": [{
        "title": "example glossary",
        "float value": 1e9,
        "float value 2": -0.9e-5,
		"Gloss \u1234 \nDiv": {
             "title": "S",   			"items": {
                "read": {
                    "ID": "xml",
					"SortAs": "price",
					"type": "Markup Language",
					"Acronym": {},
					"UniqueID": "850257207432",
					"def": {
                "json": "Lorem ipsum dolor sit amet, ad prima imperdiet sea. Homero reprimique no duo, mundi iriure expetenda ei est. No nec denique efficiantur, pri ad oratio adipisci expetendis.",
						"links": ["ABC", "Echo", "a", "b", "c"]
                    },
					"render as": "markup", "null": null, "true": true, "false": false
                }
            }
        }
    }]
}
)";

TEST(LexerTest, Test) {
  Lexer l(json_example);
  std::vector<int> tokens;
  Token next;
  while ((next = l.Next()) != Token::EOI) {
    ASSERT_NE(next, Token::INVALID_TOKEN);
    tokens.push_back(static_cast<int>(next));
  }
  EXPECT_THAT(tokens, testing::ElementsAre(
                          2, 10, 6, 4, 2, 10, 6, 10, 7, 10, 6, 11, 7, 10, 6, 11,
                          7, 10, 6, 2, 10, 6, 10, 7, 10, 6, 2, 10, 6, 2, 10, 6,
                          10, 7, 10, 6, 10, 7, 10, 6, 10, 7, 10, 6, 2, 3, 7, 10,
                          6, 10, 7, 10, 6, 2, 10, 6, 10, 7, 10, 6, 4, 10, 7, 10,
                          7, 10, 7, 10, 7, 10, 5, 3, 7, 10, 6, 10, 7, 10, 6, 13,
                          7, 10, 6, 14, 7, 10, 6, 15, 3, 3, 3, 3, 5, 3));
}
}  // namespace
}  // namespace json
