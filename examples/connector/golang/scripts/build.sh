#!/bin/bash
mkdir proto
./scripts/copy_protos.sh
./scripts/compile_protos.sh 
go build golang_connector/main.go