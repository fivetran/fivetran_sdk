# SDK Connector Tester

## Pre-requisites
- gRPC server is running for the particular example (see [example readme's](/examples/connector/))
- Docker Desktop >= 4.23.0 or [Rancher Desktop](https://rancherdesktop.io/) >= 1.12.1

## How To Run

1. Pull the latest docker image from [fivetrandocker/sdk-connector-tester](https://hub.docker.com/repository/docker/fivetrandocker/sdk-connector-tester/general) on Docker Hub

2. Run a container using the image with the following command. Make sure to map a local directory for storing files that the tool generates by replacing `<local-data-folder>` in the command, and replace <version> with the version of the image you pulled.

```
docker run --mount type=bind,source=<local-data-folder>,target=/data -a STDIN -a STDOUT -a STDERR -it -e GRPC_HOSTNAME=host.docker.internal --network=host fivetrandocker/sdk-connector-tester:<version>
```

3. Once the sync is done running, it will persist the records in a `warehouse.db` database file. This is an instance of [DuckDB](https://duckdb.org/) database. You can connect to it to validate the results of your sync using [DuckDB CLI](https://duckdb.org/docs/api/cli) or [DBeaver](https://duckdb.org/docs/guides/sql_editors/dbeaver)

4. To rerun the container from step #2, use the following command:

```
docker start -i <container-id>
```

## CLI Arguments

The tester supports the following optional CLI arguments to alter its default behavior. You can append these options to the end of the docker run command provided in step 2 of [How To Run](https://github.com/fivetran/fivetran_sdk/blob/main/tools/connector-tester/README.md#how-to-run) section above.

#### --port
This option tells the tester to use a different port than the default 50051.

#### --destination-schema
With this option, you can alter the schema name used in the destination from the default `default_schema`.
