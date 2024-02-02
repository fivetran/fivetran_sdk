# SDK Destination Tester

## Pre-requisites
- gRPC server is running for the particular example (see [example readme's](/examples/destination/))
- Docker version > 4.23.0

## How To Run
1. Pull the latest docker image from [fivetrandocker/sdk-destination-tester](https://hub.docker.com/repository/docker/fivetrandocker/sdk-destination-tester/general) on Docker Hub.

2. Run a container using the image with the following command. Make sure to map a local directory for the tool by replacing `<local-data-folder>` placeholders in the command, and replace `<version>` with the version of the image you pulled.

```
docker run --mount type=bind,source=<local-data-folder>,target=/data -a STDIN -a STDOUT -a STDERR -it -e WORKING_DIR=<local-data-folder> -e GRPC_HOSTNAME=host.docker.internal --network=host fivetrandocker/sdk-destination-tester:<version> 
```

3. To rerun the container from step #2, use the following command:

```
docker start -i <container-id>
```

## Input File Format

Destination tester simulates operations from a source by reading input files from the local data folder. Each of these input files represent a batch of operations, encoded in JSON format. Data types in [common.proto](https://github.com/fivetran/fivetran_sdk/blob/main/common.proto#L73) file can be used as column data types.

Here is an example input file named `input_1.json`:

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
                "name": "STRING"
                "num": {"type": "DECIMAL", "precision": 6, "scale": 7},
            },
            "primary_key": []
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
                    {"id":2, "amount": 150.33, "desc": "two"}
                ],
                "campaign": [
                    {"_fivetran_id": "abc-123-xyz", "name": "Christmas", "num": 100.23},
                    {"_fivetran_id": "vbn-543-hjk", "name": "New Year", "num": 200.56}
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
                    {"_fivetran_id": "dfg-890-lkj", "name": "Christmas 2", "num": 400.32}
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
                    {"_fivetran_id": "abc-123-xyz"},
                    {"_fivetran_id": "dfg-890-lkj"}
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

## CLI Arguments

The tester supports the following optional CLI arguments to alter its default behavior. You can append these options to the end of the `docker run` command provided in step 2 of [How To Run](https://github.com/fivetran/fivetran_sdk/tree/main/tools/destination-tester#how-to-run) section above.

#### --port
This option tells the tester to use a different port than the default 50052.

#### --plain-text
This option disables encryption and compression of batch files for debugging purposes.

#### --input-file
The tester by default reads all input files from local data folder and executes them in the alphabetical order they appear. You can specify a single input file to be read and executed using this option. Providing just the filename is sufficient.
