python3 -m venv destination_run
source ../destination_run/bin/activate
mkdir -p ../proto
cp ../../../../*.proto ../proto/
pip install --upgrade pip
pip install -r ../requirements.txt
python -m grpc_tools.protoc \
       -I../proto \
       --python_out=../python_destination \
       --pyi_out=../python_destination \
       --grpc_python_out=../python_destination ../proto/*.proto

deactivate