#!/bin/bash

set -e

cd "$(git rev-parse --show-toplevel)/tools"

# Copy the latest proto files
[ -f "common.proto" ] && [ "common.proto" -ot "../common.proto" ] && rm "common.proto"
[ -f "connector_sdk.proto" ] && [ "connector_sdk.proto" -ot "../connector_sdk.proto" ] && rm "connector_sdk.proto"
[ -f "destination_sdk.proto" ] && [ "destination_sdk.proto" -ot "../destination_sdk.proto" ] && rm "destination_sdk.proto"
cp -p ../*.proto .

bazel build //testers:run_sdk_destination_tester_deploy.jar

cp -f "$(git rev-parse --show-toplevel)/tools/bazel-bin/testers/run_sdk_destination_tester_deploy.jar" .

docker build -t sdk-destination-tester -f Dockerfile.destination_tester --platform=linux/amd64 .

# clean up
rm run_sdk_destination_tester_deploy.jar
