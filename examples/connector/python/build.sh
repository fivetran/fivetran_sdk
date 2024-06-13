#!/bin/bash
# Creates virtual environment to install the packages and to run the connector.
python3 -m venv connector_run
source connector_run/bin/activate
# install the added packages
pip install -r requirements.txt

# copying protos present in the root of directory to `protos` folder
mkdir -p proto
for file in ../../../*v2.proto; do
    file_name=$(basename "$file")
    new_file="proto/${file_name%_v2.proto}.proto"
    cp "$file" "$new_file"
done

# Generates the required gRPC Python files using protos into `sdk_pb2` folder
mkdir -p sdk_pb2
python -m grpc_tools.protoc \
       --proto_path=./proto/ \
       --python_out=sdk_pb2 \
       --pyi_out=sdk_pb2 \
       --grpc_python_out=sdk_pb2 proto/*.proto
deactivate