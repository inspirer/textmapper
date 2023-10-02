load("@hedron_compile_commands//:refresh_compile_commands.bzl", "refresh_compile_commands")

# Note: run `bazel run :refresh_compile_commands` to set up clangd.
refresh_compile_commands(
    name = "refresh_compile_commands",
    exclude_external_sources = True,
    exclude_headers = "external",
    targets = "//...",
)
