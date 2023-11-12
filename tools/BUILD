load("@rules_java//java:defs.bzl", "java_binary", "java_library", "java_test")
load("@rules_proto//proto:defs.bzl", "proto_library")
load("@io_grpc_grpc_java//:java_grpc_library.bzl", "java_grpc_library")
load(
    "@bazel_tools//tools/jdk:default_java_toolchain.bzl",
    "JDK9_JVM_OPTS",
    "default_java_toolchain",
)

default_java_toolchain(
    name = "default_toolchain",
    java_runtime = "@bazel_tools//tools/jdk:remotejdk_17",
    jvm_opts = JDK9_JVM_OPTS + ["-Xss4m"],
    source_version = "17",
    target_version = "17",
)

java_test(
    name = "jre_version_17",
    srcs = ["VerifyJRE17Spec.java"],
    test_class = "VerifyJRE17Spec",
    deps = [
        "@maven//:junit_junit",
        "@maven//:org_assertj_assertj_core",
    ],
)

proto_library(
    name = "fivetran_sdk_proto",
    srcs = [
        "common.proto",
        "connector_sdk.proto",
        "destination_sdk.proto",
    ],
    visibility = ["//:__subpackages__"],
    deps = [
        "@com_google_protobuf//:any_proto",
        "@com_google_protobuf//:timestamp_proto",
    ],
)

java_proto_library(
    name = "fivetran_sdk_java_proto",
    visibility = ["//:__subpackages__"],
    deps = [":fivetran_sdk_proto"],
)

java_grpc_library(
    name = "fivetran_sdk_java_grpc",
    srcs = [":fivetran_sdk_proto"],
    visibility = ["//:__subpackages__"],
    deps = [":fivetran_sdk_java_proto"],
)
