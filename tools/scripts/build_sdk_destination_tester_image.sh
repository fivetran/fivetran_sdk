#!/bin/bash

set -e

cd "$(git rev-parse --show-toplevel)/tools"

bazel build //testers:run_sdk_destination_tester_deploy.jar

cp -f "$(git rev-parse --show-toplevel)/tools/bazel-bin/testers/run_sdk_destination_tester_deploy.jar" .

docker build -t sdk-destination-tester -f Dockerfile.destination_tester --platform=linux/amd64 .

# clean up
rm run_sdk_destination_tester_deploy.jar
