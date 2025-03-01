package(default_visibility = ["//visibility:private"])

cc_library(
    name = "lexer",
    srcs = ["json_lexer.cc"],
    hdrs = [
        "json_lexer.h",
        "json_token.h",
    ],
    deps = [
        "@abseil-cpp//absl/log",
        "@abseil-cpp//absl/strings",
    ],
)

cc_test(
    name = "lexer_test",
    size = "small",
    srcs = ["lexer_test.cc"],
    deps = [
        ":lexer",
        "//cpp/markup",
        "@googletest//:gtest_main",
    ],
)

cc_library(
    name = "parser",
    srcs = ["json_parser.cc"],
    hdrs = ["json_parser.h"],
    deps = [
        ":lexer",
        "@abseil-cpp//absl/functional:function_ref",
        "@abseil-cpp//absl/log",
        "@abseil-cpp//absl/status",
        "@abseil-cpp//absl/strings",
    ],
)

cc_test(
    name = "parser_test",
    size = "small",
    srcs = ["parser_test.cc"],
    deps = [
        ":lexer",
        ":parser",
        "//cpp/markup",
        "@googletest//:gtest_main",
    ],
)

cc_binary(
    name = "benchmark",
    srcs = ["benchmark.cc"],
    deps = [
        ":lexer",
        "@google_benchmark//:benchmark",
    ],
)
