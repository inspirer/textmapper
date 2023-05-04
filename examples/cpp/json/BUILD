package(default_visibility = ["//visibility:private"])

cc_library(
    name = "lexer",
    srcs = ["lexer.cc"],
    hdrs = ["lexer.h"],
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
        "@com_google_googletest//:gtest_main",
    ],
)
