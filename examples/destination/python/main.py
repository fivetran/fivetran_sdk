from concurrent import futures
import grpc
import read_csv
import sys
sys.path.append('sdk_pb2')

from sdk_pb2 import destination_sdk_pb2
from sdk_pb2 import common_pb2
from sdk_pb2 import destination_sdk_pb2_grpc


class DestinationImpl(destination_sdk_pb2_grpc.DestinationServicer):
    def ConfigurationForm(self, request, context):
        print_log("INFO", "Fetching Configuraiton form")
        host = common_pb2.FormField(name="host", label="Host", required=True,
                                     text_field=common_pb2.TextField.PlainText)
        password = common_pb2.FormField(name="password", label="Password", required=True,
                                         text_field=common_pb2.TextField.Password)
        region = common_pb2.FormField(name="region", label="AWS Region", required=False,
                                       dropdown_field=common_pb2.DropdownField(dropdown_field=["US-EAST", "US-WEST"]))
        hidden = common_pb2.FormField(name="hidden", label="my-hidden-value", text_field=common_pb2.TextField.Hidden)
        is_public = common_pb2.FormField(name="isPublic", label="Public?", description="Is this public?",
                                          toggle_field=common_pb2.ToggleField())

        connect_test = common_pb2.ConfigurationTest(name="connect", label="Tests connection")
        select_test = common_pb2.ConfigurationTest(name="select", label="Tests selection")
        return common_pb2.ConfigurationFormResponse(
            schema_selection_supported=True,
            table_selection_supported=True,
            fields=[host, password, region, hidden,
                    is_public],
            tests=[connect_test, select_test]

            )

    def Test(self, request, context):
        test_name = request.name
        print_log("INFO", "test name: " + test_name)
        return common_pb2.TestResponse(success=True)

    def CreateTable(self, request, context):
        print("[CreateTable] :" + str(request.schema_name) + " | " + str(request.table.name) + " | " + str(request.table.columns))
        return destination_sdk_pb2.CreateTableResponse(success=True)

    def AlterTable(self, request, context):
        res: destination_sdk_pb2.AlterTableResponse

        print("[AlterTable]: " + str(request.schema_name) + " | " + str(request.table.name) + " | " + str(request.table.columns))
        return destination_sdk_pb2.AlterTableResponse(success=True)

    def Truncate(self, request, context):
        print("[TruncateTable]: " + str(request.schema_name) + " | " + str(request.schema_name) + " | soft" + str(request.soft))
        return destination_sdk_pb2.TruncateResponse(success=True)

    def WriteBatch(self, request, context):
        for replace_file in request.replace_files:
            print("replace files: " + str(replace_file))
        for update_file in request.update_files:
            print("replace files: " + str(update_file))
        for delete_file in request.delete_files:
            print("replace files: " + str(delete_file))

        print_log("INFO", "Data loading started for table " + request.table.name)
        for key, value in request.keys.items():
            print("----------------------------------------------------------------------------")
            print("Decrypting and printing file :" + str(key))
            print("----------------------------------------------------------------------------")
            read_csv.decrypt_file(key, value)
        print_log("INFO", "\nData loading completed for table " + request.table.name + "\n")

        res: destination_sdk_pb2.WriteBatchResponse = destination_sdk_pb2.WriteBatchResponse(success=True)
        return res

    def DescribeTable(self, request, context):
        column1 = common_pb2.Column(name="a1", type=common_pb2.DataType.UNSPECIFIED, primary_key=True)
        column2 = common_pb2.Column(name="a2", type=common_pb2.DataType.DOUBLE)
        table: common_pb2.Table = common_pb2.Table(name=request.table_name, columns=[column1, column2])
        return destination_sdk_pb2.DescribeTableResponse(not_found=False, table=table)

def print_log(level, message):
    print(f'{{"level":"{level}", "message": "{message}", "message-origin": "sdk_destination"}}')

if __name__ == '__main__':
    server = grpc.server(futures.ThreadPoolExecutor(max_workers=1))
    server.add_insecure_port('[::]:50052')
    destination_sdk_pb2_grpc.add_DestinationServicer_to_server(DestinationImpl(), server)
    server.start()
    print("Destination gRPC server started...")
    server.wait_for_termination()
    print("Destination gRPC server terminated...")
