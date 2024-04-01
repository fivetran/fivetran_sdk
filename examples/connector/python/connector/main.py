import grpc
from concurrent import futures
import json
import datetime


import connector_sdk_pb2_grpc as connector__sdk__pb2__grpc
import common_pb2 as common__pb2
import connector_sdk_pb2 as connector__sdk__pb2


class ConnectorService(connector__sdk__pb2__grpc.ConnectorServicer):
    def ConfigurationForm(self, request, context):
        form_fields = common__pb2.ConfigurationFormResponse(schema_selection_supported=True,
                                                           table_selection_supported=True)
        api_key_field = form_fields.fields.add()
        api_key_field.name = "apiKey"
        api_key_field.label = "API Key"
        api_key_field.required = True
        api_key_field.text_field = common__pb2.TextField.PlainText
        connection_test = form_fields.tests.add()
        connection_test.name = "connection_test"
        connection_test.label = "Tests connection"
        return form_fields

    def Test(self, request, context):
        res = common__pb2.TestResponse(success=True)
        return res

    def Schema(self, request, context):
        table_list = common__pb2.TableList()
        t1 = table_list.tables.add()
        t1.name = "table1"
        c1 = t1.columns.add()
        c1.name = "a1"
        c1.type = common__pb2.DataType.STRING
        c1.primary_key = True
        c2 = t1.columns.add()
        c2.name = "a2"
        c2.type = common__pb2.DataType.DOUBLE

        t2 = table_list.tables.add()
        t2.name = "table2"
        c1 = t2.columns.add()
        c1.name = "b1"
        c1.type = common__pb2.DataType.STRING
        c1.primary_key = True
        c2 = t1.columns.add()
        c2.name = "b2"
        c2.type = common__pb2.DataType.UNSPECIFIED


        response = connector__sdk__pb2.SchemaResponse(without_schema=table_list)
        return response


    def Update(self, request, context):
        state_json = "{}"
        if request.HasField('state_json'):
            state_json = request.state_json

        # # Read london subway API response and update the table with line status
        # result = requests.get('https://api.tfl.gov.uk/line/mode/tube/status',
        #                       headers={"content-type": "application/json", "charset": "utf-8"})
        #
        #
        # timeline = json.loads(result.text)

        # -- Send UPSERT records
        for t in range(0, 3):
            response = connector__sdk__pb2.UpdateResponse()
            operation = response.operation
            val1 = common__pb2.ValueType()
            val1.string = "a-" + str(t)

            val2 = common__pb2.ValueType()
            val2.double = t * 0.234

            print("Values are assigned", val1.string)

            record = connector__sdk__pb2.Record()
            record.type = common__pb2.OpType.UPSERT
            record.table_name="table1"
            record.data["a1"].CopyFrom(val1)
            record.data["a2"].CopyFrom(val2)

            operation.record.CopyFrom(record)
            yield connector__sdk__pb2.UpdateResponse(operation=operation)

        print("Upserted records")

        # -- Send UPDATE record
        response = connector__sdk__pb2.UpdateResponse()
        operation = response.operation
        val1 = common__pb2.ValueType()
        val1.string = "a-0"

        val2 = common__pb2.ValueType()
        val2.double = 110.234

        record = connector__sdk__pb2.Record()
        record.type = common__pb2.OpType.UPDATE
        record.table_name = "table1"
        record.data["a1"].CopyFrom(val1)
        record.data["a2"].CopyFrom(val2)

        operation.record.CopyFrom(record)
        yield connector__sdk__pb2.UpdateResponse(operation=operation)

        print("Updated record")

        # -- Send DELETE record
        response = connector__sdk__pb2.UpdateResponse()
        operation = response.operation
        val1 = common__pb2.ValueType()
        val1.string = "a-2"

        record = connector__sdk__pb2.Record()
        record.type = common__pb2.OpType.DELETE
        record.table_name = "table1"
        record.data["a1"].CopyFrom(val1)

        operation.record.CopyFrom(record)
        yield connector__sdk__pb2.UpdateResponse(operation=operation)
        print("deleted the record")



def convert_string_to_utc_seconds(datetime_str):
    utc_datetime = datetime.datetime.strptime(datetime_str, "%Y-%m-%dT%H:%M:%S.%fZ")
    utc_seconds = int(utc_datetime.timestamp())
    return utc_seconds

def start_server():
    server = grpc.server(futures.ThreadPoolExecutor(max_workers=1))
    connector__sdk__pb2__grpc.add_ConnectorServicer_to_server(ConnectorService(), server)
    server.add_insecure_port('[::]:50051')
    server.start()
    print("Server started...")
    server.wait_for_termination()
    print("Server terminated.")


if __name__ == '__main__':
    print("Starting the server...")
    start_server()
