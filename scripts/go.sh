protoc \
    --go_out=go \
    --go_opt=paths=source_relative \
    --go-grpc_out=go \
    --go-grpc_opt=paths=source_relative \
    fivetran_sdk.proto
