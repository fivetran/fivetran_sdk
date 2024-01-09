# SDK Connector Tester

## Pre-requisites
- gRPC server is running for the particular example (see [example readme's](/examples/connector/))
- Docker version > 4.23.0

## Steps

1. Pull the latest docker image from [it5t/fivetran-sdk-connector-tester](https://hub.docker.com/repository/docker/it5t/fivetran-sdk-connector-tester/general) on Docker Hub

2. Run a container using the image with the following command. Make sure to map a local directory for storing files that the tool generates by replacing `<local-data-folder>` in the command. 

```
docker run --mount type=bind,source=<local-data-folder>,target=/data -a STDIN -a STDOUT -a STDERR -it -e GRPC_HOSTNAME=host.docker.internal --network=host sdk-connector-tester

```

3. Once the sync is done running, it will persist the records in a `warehouse.db` database file. This is an instance of [DuckDB](https://duckdb.org/) database. You can connect to it to validate the results of your sync using [DuckDB CLI](https://duckdb.org/docs/api/cli) or [DBeaver](https://duckdb.org/docs/guides/sql_editors/dbeaver)

4. To rerun the container from step #2, use the following command:

```
docker start -i <container-id>
```
