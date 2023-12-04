# Pre-requisites
- Mac OSX or Linux operating system
- Bazel version >= 6.4.0
- Docker version >= 4.23.0

# SDK Connector Tester

Make sure the gRPC server for your connector or one of the [connector examples](/examples/connector) is running.

## Steps

1. Build the `sdk-connector-tester` docker image:
```
./tools/scripts/build_sdk_connector_tester_image.sh
```

2. Run a container using the image with the following command. Make sure to map a local directory for storing files that the tool generates by replacing `<local-data-folder>` in the command:
```
docker run --mount type=bind,source=<local-data-folder>,target=/data -a STDIN -a STDOUT -a STDERR -it -e GRPC_HOSTNAME=host.docker.internal --network=host sdk-connector-tester

```

3. Once the sync is done running, it will persist the records in a `warehouse.db` database file. This is an instance of [DuckDB](https://duckdb.org/) database. You can connect to it to validate the results of your sync using [DuckDB CLI](https://duckdb.org/docs/api/cli) or [DBeaver](https://duckdb.org/docs/guides/sql_editors/dbeaver).

4. To rerun the container from step #2, use the following command:

```
docker start -i <container-id>
```

# SDK Destination Tester

Make sure the gRPC server for your destination or one of the [destination examples](/examples/destination) is running.

## Steps
1. Build the `sdk-destination-tester` docker image:
```
./tools/scripts/build_sdk_destination_tester_image.sh
```

2. Prepare your batch file(s) according to the format specified in the next section.

3. Run a container using the image with the following command. Make sure to map a local directory for the tool by replacing placeholders `<local-data-folder>` in the command.

```
docker run --mount type=bind,source=<local-data-folder>,target=/data -a STDIN -a STDOUT -a STDERR -it -e WORKING_DIR=<local-data-folder> -e GRPC_HOSTNAME=host.docker.internal --network=host sdk-destination-tester

```

4. To rerun the container from step #3, use the following command:

```
docker start -i <container-id>
```

## Batch input format

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

