#include "lexer.h"

#include <vector>

#include "gtest/gtest.h"
#include "gmock/gmock.h"

namespace json {
namespace {

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
