package(default_visibility = ["//visibility:public"])

cc_library(
    name = "markup",
    srcs = ["markup.cc"],
    hdrs = ["markup.h"],
    deps = [
        "@com_google_absl//absl/log:check",
        "@com_google_absl//absl/strings",
    ],
)

cc_test(
    name = "markup_test",
    size = "small",
    srcs = ["markup_test.cc"],
    deps = [
        ":markup",
        "@com_google_googletest//:gtest_main",
    ],
)
