How to use MockFivetran to test your connector:

1. Download the latest `mock-fivetran` docker image.

2. Load the image to docker
```
docker load --input mock-fivetran.tar
```

3. Run a container using `mock-fivetran` image with the following command. Make sure to map a local directory for storing files that MockFivetran generates by replacing `<local-data-folder>` in the command. Currently the image is built for `linux/arm64` only.

```
docker run --mount type=bind,source=<local-data-folder>,target=/data -a STDIN -a STDOUT -a STDERR -it -e GRPC_HOSTNAME=host.docker.internal --network=host mock-fivetran

```

4. Once the sync is done running, it will persist the records in a `warehouse.db` database file. This an instance of the [DuckDB](https://duckdb.org/) database. You can connect to it to validate the results of your sync using [DuckDB CLI](https://duckdb.org/docs/api/cli) or [DBeaver](https://duckdb.org/docs/guides/sql_editors/dbeaver)

5. To rerun the container from step #3, use the following command:

```
docker start -i <container-id>
```