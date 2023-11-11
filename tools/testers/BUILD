load("@rules_java//java:defs.bzl", "java_binary", "java_library", "java_test")

java_library(
    name = "main",
    srcs = glob(["src/**/*.java"]),
    runtime_deps = [
        "@io_grpc_grpc_java//netty",
    ],
    deps = [
        "//:fivetran_sdk_java_proto",
        "//client:main",
        "@io_grpc_grpc_java//api",
        "@maven//:com_fasterxml_jackson_core_jackson_annotations",
        "@maven//:com_fasterxml_jackson_core_jackson_core",
        "@maven//:com_fasterxml_jackson_core_jackson_databind",
        "@maven//:com_fasterxml_jackson_dataformat_jackson_dataformat_csv",
        "@maven//:com_fasterxml_jackson_datatype_jackson_datatype_jdk8",
        "@maven//:com_fasterxml_jackson_datatype_jackson_datatype_jsr310",
        "@maven//:com_github_luben_zstd_jni",
        "@maven//:com_google_code_findbugs_jsr305",
        "@maven//:com_google_guava_guava",
        "@maven//:com_google_protobuf_protobuf_java",
        "@maven//:info_picocli_picocli",
        "@maven//:org_apache_commons_commons_lang3",
        "@maven//:org_duckdb_duckdb_jdbc",
    ],
)

java_test(
    name = "tests",
    srcs = glob(["test/**/*.java"]),
    test_class = "testers.RunAllSpecs",
    deps = [
        ":main",
        "//:fivetran_sdk_java_proto",
        "//client:main",
        "@com_google_protobuf//java/core",
        "@maven//:com_google_guava_guava",
        "@maven//:commons_io_commons_io",
        "@maven//:junit_junit",
        "@maven//:org_apache_commons_commons_lang3",
        "@maven//:org_hamcrest_hamcrest",
        "@maven//:org_mockito_mockito_core",
    ],
)

java_binary(
    name = "run_sdk_destination_tester",
    main_class = "com.fivetran.fivetran_sdk.tools.SdkDestinationTester",
    runtime_deps = [":main"],
)

java_binary(
    name = "run_sdk_connector_tester",
    main_class = "com.fivetran.fivetran_sdk.tools.SdkConnectorTester",
    runtime_deps = [":main"],
)
