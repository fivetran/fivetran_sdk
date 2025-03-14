PATH="${PATH}:${HOME}/go/bin" protoc \
    --proto_path=proto \
    --go_out=proto \
    --go_opt=paths=source_relative \
    --go-grpc_out=proto \
    --go-grpc_opt=paths=source_relative \
    common.proto \
    connector_sdk.proto
