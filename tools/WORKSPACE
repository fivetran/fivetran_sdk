workspace(
    name = "fivetran_sdk",
)

load("@bazel_tools//tools/build_defs/repo:http.bzl", "http_archive", "http_file", "http_jar")

http_archive(
    name = "rules_jvm_external",
    sha256 = "d31e369b854322ca5098ea12c69d7175ded971435e55c18dd9dd5f29cc5249ac",
    strip_prefix = "rules_jvm_external-5.3",
    urls = [
        "https://github.com/bazelbuild/rules_jvm_external/releases/download/5.3/rules_jvm_external-5.3.tar.gz",
    ],
)

load("@rules_jvm_external//:defs.bzl", "maven_install")
load("@rules_jvm_external//:specs.bzl", "maven")

http_archive(
    name = "com_google_protobuf",
    sha256 = "22fdaf641b31655d4b2297f9981fa5203b2866f8332d3c6333f6b0107bb320de",
    strip_prefix = "protobuf-21.12",
    urls = [
        "https://github.com/protocolbuffers/protobuf/archive/refs/tags/v21.12.tar.gz",
    ],
)

load("@com_google_protobuf//:protobuf_deps.bzl", "protobuf_deps")

protobuf_deps()

http_archive(
    name = "io_grpc_grpc_java",
    sha256 = "a22b6581878332df2f6c4256ff2438040a4efa5e59e2dfd771836bb7d62eb62f",
    strip_prefix = "grpc-java-1.54.2",
    urls = [
        "https://github.com/grpc/grpc-java/archive/refs/tags/v1.54.2.tar.gz",
    ],
)

load("@io_grpc_grpc_java//:repositories.bzl", "IO_GRPC_GRPC_JAVA_ARTIFACTS")
load("@io_grpc_grpc_java//:repositories.bzl", "IO_GRPC_GRPC_JAVA_OVERRIDE_TARGETS")
load("@io_grpc_grpc_java//:repositories.bzl", "grpc_java_repositories")

grpc_java_repositories()

maven_install(
    name = "maven",
    artifacts = [
        "org.apache.tomcat:annotations-api:6.0.53",
        "com.fasterxml.jackson.core:jackson-annotations:2.12.7",
        "com.fasterxml.jackson.core:jackson-core:2.12.7",
        "com.fasterxml.jackson.core:jackson-databind:2.12.7.1",
        "com.fasterxml.jackson.dataformat:jackson-dataformat-cbor:2.12.7",
        "com.fasterxml.jackson.dataformat:jackson-dataformat-smile:2.12.7",
        "com.fasterxml.jackson.dataformat:jackson-dataformat-xml:2.12.7",
        "com.fasterxml.jackson.dataformat:jackson-dataformat-yaml:2.12.7",
        "com.fasterxml.jackson.dataformat:jackson-dataformat-csv:2.12.7",
        "com.fasterxml.jackson.datatype:jackson-datatype-jdk8:2.12.7",
        "com.fasterxml.jackson.datatype:jackson-datatype-joda:2.12.7",
        "com.fasterxml.jackson.datatype:jackson-datatype-jsr310:2.12.7",
        "com.fasterxml.jackson.module:jackson-module-afterburner:2.12.7",
        "com.google.code.findbugs:jsr305:3.0.2",
        "com.google.guava:guava:32.1.3-jre",
        "com.google.protobuf:protobuf-java-util:3.22.3",
        "com.google.protobuf:protobuf-java:3.22.3",
        "commons-codec:commons-codec:1.15",
        "commons-io:commons-io:2.11.0",
        "commons-logging:commons-logging:1.2",
        "info.picocli:picocli:4.5.1",
        "io.grpc:grpc-api:1.51.3",
        "io.grpc:grpc-auth:1.51.3",
        "io.grpc:grpc-core:1.51.3",
        "io.grpc:grpc-netty-shaded:1.51.3",
        "io.grpc:grpc-protobuf-lite:1.51.3",
        "io.grpc:grpc-protobuf:1.51.3",
        "io.grpc:grpc-stub:1.51.3",
        "io.grpc:grpc-testing:1.51.3",
        "io.netty:netty-all:4.1.79.Final",
        "io.netty:netty-buffer:4.1.79.Final",
        "io.netty:netty-handler:4.1.79.Final",
        "io.netty:netty-transport:4.1.79.Final",
        "joda-time:joda-time:2.9.2",
        "junit:junit:4.13",
        "org.junit.platform:junit-platform-launcher:1.10.0",
        "org.junit.platform:junit-platform-reporting:1.10.0",
        "org.apache.avro:avro:1.11.0",
        "org.apache.commons:commons-collections4:4.4",
        "org.apache.commons:commons-compress:1.21",
        "org.apache.commons:commons-lang3:3.12.0",
        "org.apache.commons:commons-text:1.10.0",
        "org.apache.logging.log4j:log4j-slf4j-impl:2.17.1",
        "org.apache.logging.log4j:log4j-api:2.17.1",
        "org.apache.logging.log4j:log4j-core:2.17.1",
        "org.hamcrest:hamcrest:2.2",
        "org.hamcrest:hamcrest-core:2.2",
        "org.jetbrains:annotations:17.0.0",
        "org.junit.jupiter:junit-jupiter-api:5.10.0",
        "org.junit.jupiter:junit-jupiter-engine:5.10.0",
        "org.junit.jupiter:junit-jupiter-params:5.10.0",
        "org.junit.jupiter:junit-jupiter:5.10.0",
        "org.mockito:mockito-core:5.6.0",
        "org.mockito:mockito-junit-jupiter:5.6.0",
        "org.slf4j:slf4j-api:1.7.36",
        "org.slf4j:slf4j-jdk14:1.7.13",
        "org.duckdb:duckdb_jdbc:0.9.0",
        "org.assertj:assertj-core:3.14.0",
        "com.github.luben:zstd-jni:1.4.9-1",
    ],
    duplicate_version_warning = "error",
    fail_if_repin_required = True,
    fetch_sources = True,
    generate_compat_repositories = True,
    # NOTE anytime you update artifacts or repositories, you need to run `REPIN=1 bazel run @unpinned_maven//:pin` to regenerate maven_install.json
    maven_install_json = "//:maven_install.json",
    repositories = [
        "https://repo1.maven.org/maven2/",
    ],
    #use_credentials_from_home_netrc_file = True,
    version_conflict_policy = "pinned",
)

load("@maven//:compat.bzl", "compat_repositories")

compat_repositories()

load("@maven//:defs.bzl", "pinned_maven_install")

pinned_maven_install()

load("@bazel_tools//tools/jdk:remote_java_repository.bzl", "remote_java_repository")

# Zulu OpenJDK (x86 64-bit, linux)
remote_java_repository(
    name = "zulu17.42.19-ca-jdk17.0.7-linux_x64",
    prefix = "zulu_jdk",
    sha256 = "28861f8292eab43290109c33e1ba7ff3776c44d043e7c8462d6c9702bf9fffe0",
    strip_prefix = "zulu17.42.19-ca-jdk17.0.7-linux_x64",
    target_compatible_with = [
        "@platforms//cpu:x86_64",
        "@platforms//os:linux",
    ],
    urls = [
        "https://cdn.azul.com/zulu/bin/zulu17.42.19-ca-jdk17.0.7-linux_x64.tar.gz",
    ],
    version = "17",
)

# Zulu OpenJDK (x86 64-bit, macos)
remote_java_repository(
    name = "zulu17.42.19-ca-jdk17.0.7-macosx_x64",
    prefix = "zulu_jdk",
    sha256 = "fd8c32cbe06b9ecda87acc25ee212d74b7e221de5edc067f38ab5ba0c43445ae",
    strip_prefix = "zulu17.42.19-ca-jdk17.0.7-macosx_x64",
    target_compatible_with = [
        "@platforms//cpu:x86_64",
        "@platforms//os:macos",
    ],
    urls = [
        "https://cdn.azul.com/zulu/bin/zulu17.42.19-ca-jdk17.0.7-macosx_x64.tar.gz",
    ],
    version = "17",
)

# Zulu OpenJDK (ARM 64-bit, macos)
remote_java_repository(
    name = "zulu17.42.19-ca-jdk17.0.7-macosx_aarch64",
    prefix = "zulu_jdk",
    sha256 = "650be4ed94caa22ec4242b007f90f8f3bad32f66c0a60ff9a18044ce2761a049",
    strip_prefix = "zulu17.42.19-ca-jdk17.0.7-macosx_aarch64",
    target_compatible_with = [
        "@platforms//cpu:aarch64",
        "@platforms//os:macos",
    ],
    urls = [
        "https://cdn.azul.com/zulu/bin/zulu17.42.19-ca-jdk17.0.7-macosx_aarch64.tar.gz",
        "https://sonatype-nexus.it-fivetran.com/repository/azul/zulu/bin/zulu17.42.19-ca-jdk17.0.7-macosx_aarch64.tar.gz",
    ],
    version = "17",
)

# Zulu OpenJDK (ARM 64-bit, linux): for emulation of Engflow-like local machine with Apple Silicon (see https://github.com/fivetran/engineering/pull/112828)
remote_java_repository(
    name = "zulu17.42.19-ca-jdk17.0.7-linux_aarch64",
    prefix = "zulu_jdk",
    sha256 = "650be4ed94caa22ec4242b007f90f8f3bad32f66c0a60ff9a18044ce2761a049",
    strip_prefix = "zulu17.42.19-ca-jdk17.0.7-linux_aarch64",
    target_compatible_with = [
        "@platforms//cpu:arm64",
        "@platforms//os:linux",
    ],
    urls = [
        "https://cdn.azul.com/zulu/bin/zulu17.42.19-ca-jdk17.0.7-macosx_aarch64.tar.gz",
        "https://sonatype-nexus.it-fivetran.com/repository/azul/zulu/bin/zulu17.42.19-ca-jdk17.0.7-macosx_aarch64.tar.gz",
    ],
    version = "17",
)

register_toolchains("//:default_toolchain_definition")
