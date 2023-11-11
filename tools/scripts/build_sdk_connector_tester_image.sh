#!/bin/bash

set -e

cd "$(git rev-parse --show-toplevel)/tools"

bazel build //testers:run_sdk_connector_tester_deploy.jar

cp -f "$(git rev-parse --show-toplevel)/tools/bazel-bin/testers/run_sdk_connector_tester_deploy.jar" .

docker build -t sdk-connector-tester -f Dockerfile.connector_tester --platform=linux/amd64 .

docker save --output sdk-connector-tester.tar sdk-connector-tester

# clean up
rm run_sdk_connector_tester_deploy.jar
