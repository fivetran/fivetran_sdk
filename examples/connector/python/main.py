import grpc
from concurrent import futures
import json
import sys
sys.path.append('sdk_pb2')

from sdk_pb2 import connector_sdk_pb2_grpc
from sdk_pb2 import common_pb2
from sdk_pb2 import connector_sdk_pb2


class ConnectorService(connector_sdk_pb2_grpc.SourceConnectorServicer):
    def ConfigurationForm(self, request, context):
        response = common_pb2.ConfigurationFormResponse(schema_selection_supported=True,
                                                           table_selection_supported=True)
        response.fields.add().single.CopyFrom(
            common_pb2.Field(
                name="apiKey",
                label="API Key",
                required=True,
                text_field=common_pb2.TextField.PlainText
            )
        )

        response.fields.add().single.CopyFrom(
            common_pb2.Field(
                name="password",
                label="User Password",
                required=True,
                text_field=common_pb2.TextField.Password
            )
        )

        response.fields.add().single.CopyFrom(
            common_pb2.Field(
                name="region",
                label="AWS Region",
                required=False,
                dropdown_field=common_pb2.DropdownField(dropdown_field=["US-EAST", "US-WEST"])
            )
        )

        response.fields.add().single.CopyFrom(
            common_pb2.Field(
                name="hidden",
                label="my-hidden-value",
                text_field=common_pb2.TextField.Hidden
            )
        )

        response.fields.add().single.CopyFrom(
            common_pb2.Field(
                name="isPublic",
                label="Public?",
                toggle_field=common_pb2.ToggleField()
            )
        )

        response.tests.add(name="connection_test", label="Tests connection")

        return response

    def Test(self, request, context):
        configuration = request.configuration
        # Name of the test to be run
        test_name = request.name
        print("Configuration: ", configuration)
        print("Test name: ", test_name)
        return common_pb2.TestResponse(success=True)

    def Schema(self, request, context):
        table_list = common_pb2.TableList()
        t1 = table_list.tables.add(name="table1")
        t1.columns.add(name="a1", type=common_pb2.DataType.UNSPECIFIED, primary_key=True)
        t1.columns.add(name="a2", type=common_pb2.DataType.DOUBLE)

        t2 = table_list.tables.add(name="table2")
        t2.columns.add(name="b1", type=common_pb2.DataType.UNSPECIFIED, primary_key=True)
        t2.columns.add(name="b2", type=common_pb2.DataType.UNSPECIFIED)

        return connector_sdk_pb2.SchemaResponse(without_schema=table_list)

    def Update(self, request, context):

        state_json = "{}"
        if request.HasField('state_json'):
            state_json = request.state_json

        state = json.loads(state_json)
        if state.get("cursor") is None:
            state["cursor"] = 0

        # -- Send UPSERT records
        for t in range(0, 3):
            val1 = common_pb2.ValueType()
            val1.string = "a-" + str(t)

            val2 = common_pb2.ValueType()
            val2.double = t * 0.234

            record = connector_sdk_pb2.Record()
            record.type = common_pb2.UPSERT
            record.table_name = "table1"
            record.data["a1"].CopyFrom(val1)
            record.data["a2"].CopyFrom(val2)
            state["cursor"] += 1

            yield connector_sdk_pb2.UpdateResponse(record=record)

        # -- Send UPSERT record for table2
        val1 = common_pb2.ValueType()
        val1.string = "b1"
        val2 = common_pb2.ValueType()
        val2.string = "ben"
        record = connector_sdk_pb2.Record()
        record.type = common_pb2.UPSERT
        record.table_name = "table2"
        record.data["b1"].CopyFrom(val1)
        record.data["b2"].CopyFrom(val2)
        state["cursor"] += 1

        yield connector_sdk_pb2.UpdateResponse(record=record)

        # -- Send UPDATE record
        val1 = common_pb2.ValueType()
        val1.string = "a-0"

        val2 = common_pb2.ValueType()
        val2.double = 110.234

        record = connector_sdk_pb2.Record()
        record.type = common_pb2.UPDATE
        record.table_name = "table1"
        record.data["a1"].CopyFrom(val1)
        record.data["a2"].CopyFrom(val2)

        yield connector_sdk_pb2.UpdateResponse(record=record)
        state["cursor"] += 1

        # -- Send DELETE record
        val1 = common_pb2.ValueType()
        val1.string = "a-2"

        record = connector_sdk_pb2.Record()
        record.type = common_pb2.DELETE
        record.table_name = "table1"
        record.data["a1"].CopyFrom(val1)

        yield connector_sdk_pb2.UpdateResponse(record=record)
        state["cursor"] += 1

        checkpoint = connector_sdk_pb2.Checkpoint()
        checkpoint.state_json = json.dumps(state)
        yield connector_sdk_pb2.UpdateResponse(checkpoint=checkpoint)

        log_msg = {
            "level": "INFO",
            "message": "Sync DONE",
        }
        print(log_msg)


def start_server():
    server = grpc.server(futures.ThreadPoolExecutor(max_workers=1))
    connector_sdk_pb2_grpc.add_SourceConnectorServicer_to_server(ConnectorService(), server)
    server.add_insecure_port('[::]:50051')
    server.start()
    print("Server started...")
    server.wait_for_termination()
    print("Server terminated.")


if __name__ == '__main__':
    print("Starting the server...")
    start_server()
