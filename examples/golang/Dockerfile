FROM golang:1.18-alpine

WORKDIR /app

# We want to populate the module cache based on the go.{mod,sum} files.
COPY go.mod .
COPY go.sum .

RUN go mod download

COPY . .

# Build the Go app
RUN go build -o ./out/golang_connector ./golang_connector

# This container exposes port to the outside world
EXPOSE 50051

# Run the binary program produced by `go install`
CMD ["./out/golang_connector"]