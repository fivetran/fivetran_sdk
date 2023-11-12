#!/bin/bash

set -e

cd "$(git rev-parse --show-toplevel)/tools"

# Copy the latest proto files
mkdir -p "protos"
[ -f "protos/common.proto" ] && [ "protos/common.proto" -ot "../common.proto" ] && rm "protos/common.proto"
[ -f "protos/connector_sdk.proto" ] && [ "protos/connector_sdk.proto" -ot "../connector_sdk.proto" ] && rm "protos/connector_sdk.proto"
[ -f "protos/destination_sdk.proto" ] && [ "protos/destination_sdk.proto" -ot "../destination_sdk.proto" ] && rm "protos/destination_sdk.proto"
cp -p ../*.proto ./protos/

bazel build //testers:run_sdk_connector_tester_deploy.jar

cp -f "$(git rev-parse --show-toplevel)/tools/bazel-bin/testers/run_sdk_connector_tester_deploy.jar" .

docker build -t sdk-connector-tester -f Dockerfile.connector_tester --platform=linux/amd64 .

# clean up
rm run_sdk_connector_tester_deploy.jar
