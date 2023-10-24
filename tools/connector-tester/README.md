# How to use SDK Connector Tester:

1. Download the latest docker image from [this link]().

2. Unzip the file you downloaded to extract the tar file.

3. Load the image to docker
```
docker load --input connector-tester.tar
```

4. Run a container using the image with the following command. Make sure to map a local directory for storing files that the tool generates by replacing `<local-data-folder>` in the command. (Currently the image is built for `linux/arm64` only)

```
docker run --mount type=bind,source=<local-data-folder>,target=/data -a STDIN -a STDOUT -a STDERR -it -e GRPC_HOSTNAME=host.docker.internal --network=host connector-tester

```

5. Once the sync is done running, it will persist the records in a `warehouse.db` database file. This is an instance of [DuckDB](https://duckdb.org/) database. You can connect to it to validate the results of your sync using [DuckDB CLI](https://duckdb.org/docs/api/cli) or [DBeaver](https://duckdb.org/docs/guides/sql_editors/dbeaver)

6. To rerun the container from step #4, use the following command:

```
docker start -i <container-id>
```
