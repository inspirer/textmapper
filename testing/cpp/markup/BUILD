package(default_visibility = ["//visibility:public"])

cc_library(
    name = "markup",
    srcs = ["markup.cc"],
    hdrs = ["markup.h"],
    copts = ["-std=c++17"],
    deps = [
        "@abseil-cpp//absl/log:check",
        "@abseil-cpp//absl/strings",
    ],
)

cc_test(
    name = "markup_test",
    size = "small",
    srcs = ["markup_test.cc"],
    copts = ["-std=c++17"],
    deps = [
        ":markup",
        "@googletest//:gtest_main",
    ],
)
