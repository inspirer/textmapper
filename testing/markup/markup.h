#ifndef TEXTMAPPER_MARKUP_H_
#define TEXTMAPPER_MARKUP_H_

#include <cstdint>
#include <ostream>
#include <string>
#include <tuple>
#include <vector>

#include "absl/strings/string_view.h"

namespace markup {

// Range represents a range of byte offsets.
struct Range {
  int64_t start;
  int64_t end;

  bool operator==(const Range& rhs) const {
    return start == rhs.start && end == rhs.end;
  }
};

inline std::ostream& operator<<(std::ostream& os, Range r) {
  return os << "[" << r.start << "," << r.end << "]";
}

// Parse extracts pairs of guillemets from text (sorted by their end offset).
//
// WARNING: the function crashes on invalid input.
std::tuple<std::vector<Range>, std::string> Parse(absl::string_view text);

// Create returns a string with guillemets inserted at the given ranges.
//
// WARNING: the function crashes on invalid input.
std::string Create(absl::string_view text,
                   const std::vector<markup::Range>& ranges);

}  // namespace markup

#endif  // TEXTMAPPER_MARKUP_H_
