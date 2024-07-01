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
