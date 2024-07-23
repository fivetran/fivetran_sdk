import pandas as pd

from logger import log
from typing import List

from destination import VectorDestination
from embedder import Embedder

from sdk.destination_sdk_pb2_grpc import DestinationServicer
from sdk.common_pb2 import ConfigurationFormRequest, ConfigurationFormResponse, DataType
from sdk.common_pb2 import TestRequest, TestResponse
from sdk.destination_sdk_pb2 import DescribeTableRequest, DescribeTableResponse
from sdk.destination_sdk_pb2 import CreateTableRequest, CreateTableResponse
from sdk.destination_sdk_pb2 import AlterTableRequest, AlterTableResponse
from sdk.destination_sdk_pb2 import TruncateRequest, TruncateResponse
from sdk.destination_sdk_pb2 import WriteBatchRequest, WriteBatchResponse
from sdk.destination_sdk_pb2 import Compression, Encryption

from models.collection import Collection, Row
from csv_reader import CSVReaderAESZSTD
from embedders.open_ai import OpenAIEmbedder

EMBEDDERS: List[Embedder] = [OpenAIEmbedder()]


class VectorDestinationServicer(DestinationServicer):
    vec_dest: VectorDestination
    embedders: dict[str, Embedder]

    def __init__(self, vec_dest: VectorDestination):
        self.vec_dest = vec_dest
        self.embedders = {e.details()[0]: e for e in EMBEDDERS}

    def ConfigurationForm(self, request: ConfigurationFormRequest, context):
        log("Called -> ConfigurationForm")
        dest_config_form = self.vec_dest.configuration_form()

        combined_fields = []
        for f in dest_config_form.fields:
            name = f"vect_dest__{f.name}"
            new_f = f.__deepcopy__()
            new_f.name = name
            combined_fields.append(new_f)

        for _, e in self.embedders.items():
            c = e.configuration_form()
            id, name = e.details()
            for f in c.fields:
                name = f"embedder__{id}__{f.name}"
                new_f = f.__deepcopy__()
                new_f.name = name
                combined_fields.append(new_f)

        combined_tests = []
        for t in dest_config_form.tests:
            name = f"vect_dest__{t.name}"
            new_t = t.__deepcopy__()
            new_t.name = name
            combined_tests.append(new_t)

        for _, e in self.embedders.items():
            c = e.configuration_form()
            id, name = e.details()
            for t in c.tests:
                name = f"embedder__{id}__{t.name}"
                new_t = t.__deepcopy__()
                new_t.name = name
                combined_tests.append(new_t)

        combined_form = ConfigurationFormResponse(
            schema_selection_supported=False,
            table_selection_supported=False,
            fields=combined_fields,
            tests=combined_tests,
        )

        return combined_form

    def Test(self, request: TestRequest, context):
        log(f"Called -> Test({request.name})")

        if request.name.startswith("vect_dest__"):
            prefix = "vect_dest__"
            test_target = self.vec_dest
        elif request.name.startswith("embedder__"):
            embedder_id = request.name.split("__")[1]
            prefix = f"embedder__{embedder_id}__"
            test_target = self.embedders.get(embedder_id)
            if not test_target:
                raise ValueError(f"Invalid embedder ID: {embedder_id}")
        else:
            raise ValueError(f"Invalid test {request.name}")

        config = {k.removeprefix(prefix): v for k, v in request.configuration.items() if k.startswith(prefix)}
        name = request.name.removeprefix(prefix)
        result = test_target.test(name, config)

        return TestResponse(success=bool(result), failure=result if not result else None)

    def DescribeTable(self, request: DescribeTableRequest, context):
        log(f"Called -> DescribeTable({request.schema_name}.{request.table_name})")
        return DescribeTableResponse(not_found=True)

    def _split_configs(self, config_in):
        prefix = "vect_dest__"
        config = {k.removeprefix(prefix): v for k, v in config_in.items() if k.startswith(prefix)}

        # TODO: Remove this hardcoding once `visibility` is implemented in SDK
        embedder_id = "open_ai"

        prefix = f"embedder__{embedder_id}__"
        embedder_config = {k.removeprefix(prefix): v for k, v in config_in.items() if k.startswith(prefix)}

        return config, embedder_id, embedder_config

    def CreateTable(self, request: CreateTableRequest, context):
        log(f"Called -> CreateTable({request.schema_name}.{request.table.name})")

        config, embedder_id, embedder_config = self._split_configs(request.configuration)
        embedder = self.embedders[embedder_id]

        collection = Collection(
            name=self.vec_dest.get_collection_name(request.schema_name, request.table.name),
            metrics=embedder.metrics(embedder_config)
        )
        self.vec_dest.create_collection_if_not_exists(config, collection)
        return CreateTableResponse(success=True)

    def AlterTable(self, request: AlterTableRequest, context):
        log(f"Called -> AlterTable({request.schema_name}.{request.table.name})")
        return AlterTableResponse(success=True)

    def WriteBatch(self, request: WriteBatchRequest, context):
        log(f"Called -> WriteBatch({request.schema_name}.{request.table.name} ({request.csv.encryption}|{request.csv.compression}))")

        if request.csv.compression != Compression.ZSTD:
            raise ValueError(f"Unknown compression{request.csv.compression}")

        if request.csv.encryption != Encryption.AES:
            raise ValueError(f"Unknown encryption{request.csv.encryption}")

        if request.update_files:
            raise NotImplementedError('No support for partial updates yet!')

        config, embedder_id, embedder_config = self._split_configs(request.configuration)
        embedder = self.embedders[embedder_id]

        collection = Collection(
            name=self.vec_dest.get_collection_name(request.schema_name, request.table.name),
            metrics=embedder.metrics(embedder_config)
        )
        timestamp_columns = [c.name for c in request.table.columns if c.type == DataType.UTC_DATETIME]
        csv_reader = CSVReaderAESZSTD()

        for file in request.replace_files:
            df = csv_reader.read_csv(file, request.keys[file], request.csv.null_string, timestamp_columns)

            records = df.to_dict(orient="records")
            records = [{k: v for k, v in row.items() if not pd.isna(row[k])} for row in records]
            records = [{k: (v.to_pydatetime() if k in timestamp_columns else v) for k, v in row.items()} for row in records]

            ids = [r["id"] for r in records]
            documents = [r["document"] for r in records]

            vectors = embedder.embed(embedder_config, documents)

            rows = [Row(
                id=ids[i],
                vector=vectors[i],
                content=documents[i],
                payload=records[i],
            ) for i in range(len(records))]

            self.vec_dest.upsert_rows(config, collection, rows)

        for file in request.delete_files:
            df = csv_reader.read_csv(file, request.keys[file], request.csv.null_string, timestamp_columns)
            records = df.to_dict(orient="records")

            ids = [r["id"] for r in records]

            self.vec_dest.delete_rows(config, collection, ids)

        return WriteBatchResponse(success=True)

    def Truncate(self, request: TruncateRequest, context):
        log(f"Called -> Truncate({request.schema_name}.{request.table_name})")

        config, embedder_id, embedder_config = self._split_configs(request.configuration)
        embedder = self.embedders[embedder_id]

        collection = Collection(
            name=self.vec_dest.get_collection_name(request.schema_name, request.table_name),
            metrics=embedder.metrics(embedder_config)
        )

        delete_before = request.utc_delete_before.ToDatetime()
        self.vec_dest.truncate(config, collection, request.synced_column, delete_before)

        return TruncateResponse(success=True)
