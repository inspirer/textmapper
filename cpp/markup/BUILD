package(default_visibility = ["//visibility:public"])

cc_library(
    name = "markup",
    srcs = ["markup.cc"],
    hdrs = ["markup.h"],
    deps = [
        "@abseil-cpp//absl/log:check",
        "@abseil-cpp//absl/strings",
    ],
)

cc_test(
    name = "markup_test",
    size = "small",
    srcs = ["markup_test.cc"],
    deps = [
        ":markup",
        "@googletest//:gtest_main",
    ],
)
