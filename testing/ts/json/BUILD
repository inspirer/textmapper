load("@aspect_rules_ts//ts:defs.bzl", "ts_project")

package(default_visibility = ["//visibility:public"])

ts_project(
    name = "json_ts_lib",
    srcs = glob(["**/*.ts"], exclude = ["**/*.test.ts"]),
    declaration = True,
    resolve_json_module = True,
    source_map = True,
    transpiler = "tsc",
    tsconfig = "//ts:tsconfig_bin",
    deps = [
        "//ts:node_modules/@types/node",
    ],
)