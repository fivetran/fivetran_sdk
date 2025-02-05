#!/bin/bash

#Create virtual environment
python3 -m venv destination_run

#Activate virtual environment
source destination_run/bin/activate

# Make a directory protos
mkdir -p protos

# Copy proto files t oprotos directory
cp ../../*.proto protos/

# Install the required packages
pip install -r requirements.txt

# Make a directory sdk_pb2
mkdir -p sdk_pb2

# Generate grpc python code and store it in sdk_pb2
python -m grpc_tools.protoc \
       --proto_path=./protos/ \
       --python_out=sdk_pb2 \
       --pyi_out=sdk_pb2 \
       --grpc_python_out=sdk_pb2 protos/*.proto

# Deactivate virtual environment
deactivate