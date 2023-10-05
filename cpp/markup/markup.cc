#include "markup.h"

#include <algorithm>
#include <cstdint>
#include <stack>
#include <string>
#include <tuple>
#include <vector>

#include "absl/log/check.h"
#include "absl/strings/match.h"
#include "absl/strings/string_view.h"

namespace markup {

namespace {
inline constexpr absl::string_view opening = u8"«";
inline constexpr absl::string_view closing = u8"»";
}  // namespace

std::tuple<std::vector<Range>, std::string> Parse(absl::string_view text) {
  std ::string out;
  std::vector<Range> ranges;
  std::stack<Range> stack;

  int pos = 0;
  for (int i = 0; i < text.size(); i++) {
    auto b = text[i];
    if ((b & 0xC0) == 0x80) continue;  // skip continuation bytes
    if (absl::StartsWith(text.substr(i), opening)) {
      out += text.substr(pos, i - pos);
      pos = i + opening.size();
      stack.push({static_cast<int64_t>(out.size()), -1});
    } else if (absl::StartsWith(text.substr(i), closing)) {
      out += text.substr(pos, i - pos);
      pos = i + closing.size();
      CHECK(!stack.empty()) << "unexpected closing guillemets in " << text;
      auto rng = stack.top();
      stack.pop();
      rng.end = static_cast<int64_t>(out.size());
      ranges.push_back(rng);
    }
  }
  CHECK(stack.empty()) << "missing closing guillemets in " << text;
  out += text.substr(pos, text.size() - pos);
  return std::make_tuple(ranges, out);
}

std::string Create(absl::string_view text,
                   const std::vector<markup::Range> &ranges) {
  struct Bracket {
    int64_t offset;
    std::string_view insert;

    bool operator<(const Bracket &rhs) const {
      return offset < rhs.offset ||
             (offset == rhs.offset && insert == "»" && rhs.insert != insert);
    }
  };

  std::vector<Bracket> brackets;
  for (const auto &r : ranges) {
    CHECK_LE(0, r.start);
    CHECK_LE(r.start, r.end);
    CHECK_LE(r.end, text.size());
    brackets.push_back({r.start, opening});
    brackets.push_back({r.end, closing});
  }
  std::sort(brackets.begin(), brackets.end());

  int64_t i = 0;
  std::string out;
  for (const auto &b : brackets) {
    if (i < b.offset) {
      out += text.substr(i, b.offset - i);
    }
    out += b.insert;
    i = b.offset;
  }
  if (i < text.size()) {
    out += text.substr(i, text.size() - i);
  }
  return out;
}

}  // namespace markup
