#!/bin/bash
python3 -m venv destination_run
source destination_run/bin/activate
mkdir -p protos
cp ../../../../*.proto protos/
pip install -r requirements.txt
python -m grpc_tools.protoc \
       --proto_path=./protos/ \
       --python_out=. \
       --pyi_out=. \
       --grpc_python_out=. protos/*.proto
deactivate