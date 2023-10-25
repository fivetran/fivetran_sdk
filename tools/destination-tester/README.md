# How to use SDK Destination Tester:

1. Download the latest docker image from [this link]().

2. Unzip the file you downloaded to extract the tar file.

3. Load the image to docker
```
docker load --input destination-tester.tar
```

4. Run a container using the image with the following command. Make sure to map a local directory for storing files that the tool will read by replacing `<local-data-folder>` in the command. (Currently the image is built for `linux/arm64` only)

```
docker run --mount type=bind,source=<local-data-folder>,target=/data -a STDIN -a STDOUT -a STDERR -it -e GRPC_HOSTNAME=host.docker.internal --network=host destination-tester

```

5. To rerun the container from step #4, use the following command:

```
docker start -i <container-id>
```

# Batch input format

Destination tester simulates operations from a source by reading input files from the data folder. Each of these input files represent a batch of operations, encoded in JSON format. They will be read and executed in the alphabetical order they appear in the data folder. 

Here is an example input file named `batch_1.json`:

```json
{
    "create_table" : {
        "transaction": {
            "columns": {
                "id": "INTEGER",
                "amount" : "DOUBLE",
                "desc": "STRING"
            },
            "primary_key": ["id"]
        },
        "campaign": {
            "columns": {
                "id": "INTEGER",
                "name": "STRING"
            },
            "primary_key": ["id"]
        }
    },
    "alter_table" : {
        "transaction": {
            "columns": {
                "id": "INTEGER",
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

