load("@aspect_rules_ts//ts:defs.bzl", "ts_config", "ts_project")
load("@npm//:defs.bzl", "npm_link_all_packages")
load("@aspect_bazel_lib//lib:copy_to_bin.bzl", "copy_to_bin")

package(default_visibility = ["//visibility:public"])

# Link npm packages at the root level where package.json is located
npm_link_all_packages(name = "node_modules")

ts_config(
    name = "tsconfig",
    src = "tsconfig.json",
    visibility = ["//visibility:public"],
)

copy_to_bin(
    name = "tsconfig_bin",
    srcs = ["tsconfig.json"],
    visibility = ["//ts/json:__subpackages__"],
)

copy_to_bin(
    name = "jestconfig_bin",
    srcs = ["jest.config.js"],
)
