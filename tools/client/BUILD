load("@rules_java//java:defs.bzl", "java_binary", "java_library", "java_test")

java_library(
    name = "main",
    srcs = glob(["src/**/*.java"]),
    visibility = [
        "//testers:__pkg__",
    ],
    runtime_deps = [
        "@io_grpc_grpc_java//netty",
    ],
    deps = [
        "//:fivetran_sdk_java_grpc",
        "//:fivetran_sdk_java_proto",
        "@io_grpc_grpc_java//api",
        "@io_grpc_grpc_java//protobuf",
        "@io_grpc_grpc_java//stub",
        "@maven//:com_fasterxml_jackson_core_jackson_annotations",
        "@maven//:com_fasterxml_jackson_core_jackson_core",
        "@maven//:com_fasterxml_jackson_core_jackson_databind",
        "@maven//:com_google_code_findbugs_jsr305",
        "@maven//:com_google_protobuf_protobuf_java",
        "@maven//:com_google_protobuf_protobuf_java_util",
        "@maven//:info_picocli_picocli",
        "@maven//:org_apache_commons_commons_lang3",
        "@maven//:org_duckdb_duckdb_jdbc",
    ],
)
