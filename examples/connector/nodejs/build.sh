npm install
npm i @grpc/grpc-js
npm i esbuild

cd src
mkdir -p protos
cp ../../../../*.proto protos/
cd ..

npm run build

echo '{ "main": "out.js", "output": "sea-prep.blob" }' > sea-config.json

node --experimental-sea-config sea-config.json

cp $(command -v node) hello

codesign --remove-signature hello

npx postject hello NODE_SEA_BLOB sea-prep.blob \
    --sentinel-fuse NODE_SEA_FUSE_fce680ab2cc467b6e072b8b5df1996b2 \
    --macho-segment-name NODE_SEA

codesign --sign - hello

./hello