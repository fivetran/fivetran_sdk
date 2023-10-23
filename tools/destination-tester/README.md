# How to use SDK Destination Tester:

1. Download the latest docker image from [this link]().

2. Unzip the file from step 1.

3. Load the image to docker
```
docker load --input destination-tester.tar
```

4. Run a container using the image with the following command. Make sure to map a local directory for storing files that the tool will read by replacing `<local-data-folder>` in the command. Currently the image is built for `linux/arm64` only.

```
docker run --mount type=bind,source=<local-data-folder>,target=/data -a STDIN -a STDOUT -a STDERR -it -e GRPC_HOSTNAME=host.docker.internal --network=host destination-tester

```

5. To rerun the container from step #4, use the following command:

```
docker start -i <container-id>
```

# Batch input format

Destination tester simulates the source by reading files from the data folder. Each of these files represent a batch of operations. They will be read and executed in the alphabetical order they appear in the data folder. 

Batch input files encode destination operations in JSON format. 



Here is an example batch input:

```

```

