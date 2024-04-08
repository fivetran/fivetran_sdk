#!/bin/bash
python3 -m venv connector_run
source connector_run/bin/activate
mkdir -p protos
cp ../../../*.proto protos/
pip install -r requirements.txt
mkdir -p sdk_pb2
python -m grpc_tools.protoc \
       --proto_path=./protos/ \
       --python_out=sdk_pb2 \
       --pyi_out=sdk_pb2 \
       --grpc_python_out=sdk_pb2 protos/*.proto
deactivate