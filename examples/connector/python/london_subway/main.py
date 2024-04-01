import grpc
from concurrent import futures
import requests
import json
import google.protobuf.timestamp_pb2 as timestamp_pb2
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
        t1.name = "london_subway"
        c1 = t1.columns.add()
        c1.name = "id"
        c1.type = common__pb2.DataType.STRING
        c1.primary_key = True

        c2 = t1.columns.add()
        c2.name = "linestatus"
        c2.type = common__pb2.DataType.STRING

        c3 = t1.columns.add()
        c3.name = "timestamp"
        c3.type = common__pb2.DataType.UTC_DATETIME
        c3.primary_key = True

        response = connector__sdk__pb2.SchemaResponse(without_schema=table_list)
        return response


    def Update(self, request, context):
        state_json = "{}"
        if request.HasField('state_json'):
            state_json = request.state_json

        # Read london subway API response and update the table with line status
        result = requests.get('https://api.tfl.gov.uk/line/mode/tube/status',
                              headers={"content-type": "application/json", "charset": "utf-8"})


        timeline = json.loads(result.text)

        for t in timeline:
            response = connector__sdk__pb2.UpdateResponse()
            operation = response.operation
            val1 = common__pb2.ValueType()
            val1.string = t["id"]

            val2 = common__pb2.ValueType()
            val2.string = t["lineStatuses"][0]["statusSeverityDescription"]

            val3 = common__pb2.ValueType()
            time_in_sec = convert_string_to_utc_seconds(t["created"])
            utc_datetime = timestamp_pb2.Timestamp(seconds=time_in_sec)
            val3.utc_datetime.CopyFrom(utc_datetime)

            print("Values are assigned", val1.string)

            record = connector__sdk__pb2.Record()
            record.type = common__pb2.OpType.UPSERT
            record.table_name="london_subway"
            record.data["id"].CopyFrom(val1)
            record.data["linestatus"].CopyFrom(val2)
            record.data["timestamp"].CopyFrom(val3)


            operation.record.CopyFrom(record)
            yield connector__sdk__pb2.UpdateResponse(operation=operation)



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
