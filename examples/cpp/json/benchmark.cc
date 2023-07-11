// Run with:
//
// $ bazel run --compilation_mode=opt //json:benchmark

#include <benchmark/benchmark.h>

#include "json_lexer.h"

const std::string input = R"({
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

static void BM_Lexer(benchmark::State& state) {
  for (auto _ : state) {
    json::Lexer l(input);
    json::Token next;
    while ((next = l.Next()) != json::Token::EOI) {
    }
  }
  state.SetBytesProcessed(
      static_cast<int64_t>(state.iterations())*input.size());
}
BENCHMARK(BM_Lexer);

BENCHMARK_MAIN();
