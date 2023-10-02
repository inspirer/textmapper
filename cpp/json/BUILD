package(default_visibility = ["//visibility:private"])

cc_library(
    name = "lexer",
    srcs = ["json_lexer.cc"],
    hdrs = [
        "json_lexer.h",
        "json_token.h",
    ],
    deps = [
        "@com_google_absl//absl/log",
        "@com_google_absl//absl/strings",
    ],
)

cc_test(
    name = "lexer_test",
    size = "small",
    srcs = ["lexer_test.cc"],
    deps = [
        ":lexer",
        "//markup",
        "@com_google_googletest//:gtest_main",
    ],
)

cc_library(
    name = "parser",
    srcs = ["json_parser.cc"],
    hdrs = ["json_parser.h"],
    deps = [
        ":lexer",
        "@com_google_absl//absl/functional:function_ref",
        "@com_google_absl//absl/log",
        "@com_google_absl//absl/status",
        "@com_google_absl//absl/strings",
    ],
)

cc_test(
    name = "parser_test",
    size = "small",
    srcs = ["parser_test.cc"],
    deps = [
        ":lexer",
        ":parser",
        "//markup",
        "@com_google_googletest//:gtest_main",
    ],
)

cc_binary(
    name = "benchmark",
    srcs = ["benchmark.cc"],
    deps = [
        ":lexer",
        "@benchmark",
    ],
)
