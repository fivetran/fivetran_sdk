# NodeJS Connector Example

## Steps
- Run the build.sh file. This script automates the project's build and packaging process by first installing the necessary npm packages for building and running the project. It then copies the proto files to a folder inside the Node.js project, compiles and bundles the source code into a distributable format, and finally creates a Node.js SEA (Single Executable Application) that can be executed on any system without requiring Node.js to be installed.
```commandline
sh build.sh
```

- Execute the binary file created to run the connector
```commandline
./binary
```