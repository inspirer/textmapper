#include "markup.h"

#include <string>
#include <tuple>
#include <vector>

#include "gmock/gmock.h"
#include "gtest/gtest.h"

namespace {

TEST(MarkupTest, Create) {
  EXPECT_EQ(markup::Create("foo", {}), "foo");
  EXPECT_EQ(markup::Create("foo", {{1, 2}}), "f«o»o");
  EXPECT_EQ(markup::Create("foo", {{1, 2}, {1, 3}}), "f««o»o»");
  EXPECT_EQ(markup::Create("foo", {{0, 3}}), "«foo»");
}

TEST(MarkupTest, Parse) {
  EXPECT_EQ(
      markup::Parse("f«o»o"),
      std::make_tuple(std::vector<markup::Range>{{1, 2}}, std::string{"foo"}));
  EXPECT_EQ(markup::Parse("f««o»»o"),
            std::make_tuple(std::vector<markup::Range>{{1, 2}, {1, 2}},
                            std::string{"foo"}));
  EXPECT_EQ(markup::Parse("f««o»o»"),
            std::make_tuple(std::vector<markup::Range>{{1, 2}, {1, 3}},
                            std::string{"foo"}));
}

}  // namespace
