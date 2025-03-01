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
