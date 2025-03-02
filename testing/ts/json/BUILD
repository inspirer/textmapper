load("@aspect_rules_jest//jest:defs.bzl", "jest_test")
load("@aspect_rules_ts//ts:defs.bzl", "ts_project")

package(default_visibility = ["//visibility:public"])

ts_project(
    name = "json_ts_lib",
    srcs = glob(
        ["**/*.ts"],
        exclude = ["**/*.test.ts"],
    ),
    declaration = True,
    resolve_json_module = True,
    source_map = True,
    transpiler = "tsc",
    tsconfig = "//ts:tsconfig_bin",
    deps = [
        "//ts:node_modules/@types/node",
    ],
)

ts_project(
    name = "json_test_lib",
    srcs = glob(["**/*.test.ts"]),
    declaration = True,
    resolve_json_module = True,
    source_map = True,
    transpiler = "tsc",
    tsconfig = "//ts:tsconfig_bin",
    deps = [
        ":json_ts_lib",
        "//ts:node_modules/@types/jest",
        "//ts:node_modules/@types/node",
    ],
)

jest_test(
    name = "json_test",
    config = "//ts:jestconfig_bin",
    data = [
        ":json_test_lib",
        "//ts:node_modules/jest",
        "//ts:node_modules/ts-jest",
    ],
    node_modules = "//ts:node_modules",
)
