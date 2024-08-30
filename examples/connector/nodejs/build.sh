# Install the necessary npm packages.
# `npm install` installs dependencies listed in package.json.
npm install

# Change directory to the 'src' folder where source code is located.
cd src

# Create a 'protos' directory if it doesn't already exist.
# This directory will be used to store protocol buffer (.proto) files.
mkdir -p protos

# Copy all .proto files from the parent directory (5 levels up) to the 'protos' directory.
# These files are used for defining the gRPC services and messages.
cp ../../../../*.proto protos/

# Return to the previous directory (the project root).
cd ..

# Run the build script defined in package.json.
# This command bundles the source code.
npm run build

# Create a configuration file building a blob that can be injected into the single executable application
echo '{ "main": "bundle.js", "output": "sea-prep.blob" }' > sea-config.json

# Generate the blob to be injected
node --experimental-sea-config sea-config.json

# Create a copy of the node executable named "binary" (OS dependent).
cp $(command -v node) binary

# Remove the signature of the binary
codesign --remove-signature binary

# Inject the blob into the copied binary by running postject (OS dependent)
npx postject binary NODE_SEA_BLOB sea-prep.blob \
    --sentinel-fuse NODE_SEA_FUSE_fce680ab2cc467b6e072b8b5df1996b2 \
    --macho-segment-name NODE_SEA

# Re-sign the `binary` file with the default identity.
# This step finalizes the binary by signing it for execution (OS dependent).
codesign --sign - binary

# Remove unnecessary files
rm sea-config.json
rm sea-prep.blob
rm bundle.js

# Please note that all commands in this file are specific to MacOS.
# For instructions on creating executables for other operating systems, please see: https://nodejs.org/api/single-executable-applications.html#single-executable-applications