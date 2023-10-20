#include "json_parser.h"

#include <cstdint>
#include <ostream>
#include <tuple>
#include <unordered_set>
#include <vector>

#include "absl/strings/string_view.h"
#include "gmock/gmock.h"
#include "gtest/gtest.h"
#include "markup/markup.h"

namespace json {
namespace {

TEST(ParserTest, Instantiate) {
  auto listener = [](auto node, auto flags, Lexer::Location loc) {};
  auto always_recover = [](absl::Status){ return true; };
  Parser parser(listener, always_recover, 0, false);
  LOG(INFO) << parser;
}

TEST(ParserTest, ParseWithLexer) {
  using TupleType = std::tuple<NodeType, int64_t, int64_t>;
  std::vector<TupleType> output;
  auto listener = [&](auto node, auto flags, Lexer::Location loc) {
    output.push_back(std::make_tuple(node, loc.begin, loc.end));
  };
  auto always_recover = [](absl::Status){ return true; };
  Parser parser(listener, always_recover, 8, false);
  Lexer lexer("1.0");
  EXPECT_TRUE(parser.Parse(lexer).ok());
  EXPECT_THAT(output,
              testing::ElementsAre(TupleType{NodeType::JSONValue, 0, 3},
                                   TupleType{NodeType::JSONText, 0, 3}));
}

struct ParserTestCase {
  NodeType nt;
  std::vector<absl::string_view> inputs;
};
inline std::ostream& operator<<(std::ostream& os, const ParserTestCase& t) {
  return os << "{" << t.nt << ", " << t.inputs.size() << " cases}";
}

TEST(ParserTest, NodeTypes) {
  const ParserTestCase tests[] = {

      {NodeType::EmptyObject,
       {
           R"(«{}»)",
           R"(«{ /* comment */ }»)",
           R"({"aa": «{}» })",
       }},
      {NodeType::JSONObject,
       {
           R"(«{ "a" : "b" }»)",
           R"(«{ "a" : ["b"] }»)",
           R"(«{ "a" : {} }»)",
           R"(«{ "a" : «{"q":B}» }»)",
       }},
      {NodeType::JSONArray,
       {
           R"({ "a" : «["b"]» })",
           R"( «[]» )",
       }},
      {NodeType::JSONText,
       {
           R"(«{ "a" : ["b", A] }»)",
           R"( «"aa"» )",
           R"( «A» )",
       }},
      {NodeType::JSONMember,
       {
           R"([{ «"a" : ["b"]», «"q":[]» }])",
       }},
      {NodeType::JSONValue,
       {
           R"(«{ "a" : «[«"b"»]» }»)",
           R"( «"aa"» )",
       }},
      {NodeType::NonExistingType, {}},
      {NodeType::MultiLineComment,
       {
           R"({ "a"«/* abc */» : [] })",
       }},
      {NodeType::JsonString,
       {
           R"({ «"a"» : [«"b"»] })",
       }},
      {NodeType::SyntaxProblem,
       {
           R"({ «"a" "b"» })",
       }},
  };
  std::unordered_set<NodeType> seen;
  for (auto& test_case : tests) {
    LOG(INFO) << "Running parser tests for " << test_case;
    for (const auto& input : test_case.inputs) {
      auto [want, text] = markup::Parse(input);
      Lexer l(text);
      std::vector<markup::Range> got;
      auto listener = [&](auto node, auto flags, Lexer::Location loc) {
        if (node == test_case.nt) {
          seen.insert(node);
          got.push_back(markup::Range{loc.begin, loc.end});
        }
      };
      auto always_recover = [](absl::Status){ return true; };
      Parser parser(listener, always_recover, 9, true);
      EXPECT_TRUE(parser.Parse(l).ok());
      EXPECT_THAT(got, testing::UnorderedElementsAreArray(want))
          << "Node type test failed for " << test_case.nt << " on input:\n"
          << input;
    }
  }
  /* Reports missing tests - but not really reasonable to cover all the
  error cases.
  for (NodeType n = NodeType::NoType; n < NodeType::NodeTypeMax;
       n = static_cast<NodeType>(static_cast<int64_t>(n) + 1)) {
    EXPECT_TRUE(seen.find(n) != seen.end()) << n << " is not tested";
  }
  */
}

}  // namespace
}  // namespace json
