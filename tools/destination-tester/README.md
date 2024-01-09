# SDK Destination Tester

## Pre-requisites
- gRPC server is running for the particular example (see [example readme's](/examples/destination/))
- Docker version > 4.23.0

## Steps
1. Pull the latest docker image from [it5t/fivetran-sdk-destination-tester](https://hub.docker.com/repository/docker/it5t/fivetran-sdk-destination-tester/general) on Docker Hub.

2. Run a container using the image with the following command. Make sure to map a local directory for the tool by replacing `<local-data-folder>` placeholders in the command.

```
docker run --mount type=bind,source=<local-data-folder>,target=/data -a STDIN -a STDOUT -a STDERR -it -e WORKING_DIR=<local-data-folder> -e GRPC_HOSTNAME=host.docker.internal --network=host fivetran-sdk-destination-tester
```

Note that it is possible to disable encryption and compression of batch files for debugging purposes by passing `--plain-text` CLI argument to the destination tester.

3. To rerun the container from step #2, use the following command:

```
docker start -i <container-id>
```

# Batch input format

Destination tester simulates operations from a source by reading input files from the data folder. Each of these input files represent a batch of operations, encoded in JSON format. They will be read and executed in the alphabetical order they appear in the data folder. Data types in [common.proto](https://github.com/fivetran/fivetran_sdk/blob/main/common.proto#L73) file can be used as column data types.

Here is an example input file named `batch_1.json`:

```json
{
    "create_table" : {
        "transaction": {
            "columns": {
                "id": "INT",
                "amount" : "DOUBLE",
                "desc": "STRING"
            },
            "primary_key": ["id"]
        },
        "campaign": {
            "columns": {
                "id": "INT",
                "name": "STRING"
            },
            "primary_key": ["id"]
        }
    },
    "alter_table" : {
        "transaction": {
            "columns": {
                "id": "INT",
                "amount" : "FLOAT",
                "desc": "STRING"
            },
            "primary_key": ["id"]
        }
    },
    "describe_table" : [
        "transaction"
    ],
    "ops" : [
        {
            "truncate": [
                "transaction"
            ]
        },
        {
            "upsert": {
                "transaction": [
                    {"id":1, "amount": 100.45, "desc": null},
                    {"id":2, "amount": 50.33, "desc": "two"}
                ],
                "campaign": [
                    {"id":101, "name": "Christmas"},
                    {"id":102, "name": "New Year"}
                ]
            }
        },
        {
            "update": {
                "transaction": [
                    {"id":1, "amount": 200}
                ]
            }
        },
        {
            "upsert": {
                "transaction": [
                    {"id":10, "amount": 100, "desc": "thee"},
                    {"id":20, "amount": 50, "desc": "mone"}
                ],
                "campaign": [
                    {"id":201, "name": "Christmas 2"},
                    {"id":202, "name": "New Year 2"}
                ]
            }
        },
        {
            "delete": {
                "transaction": [
                    {"id":3},
                    {"id":4}
                ],
                "campaign": [
                    {"id":103},
                    {"id":104}
                ]
            }
        },
        {
            "truncate": [
                "transaction"
            ]
        }
    ]
}

```

