from concurrent import futures
import grpc
import common_pb2 as common__pb2
import destination_sdk_pb2 as destination__sdk__pb2
import destination_sdk_pb2_grpc as destination__sdk__pb2__grpc
import read_csv


class DestinationImpl(destination__sdk__pb2__grpc.DestinationServicer):
    def ConfigurationForm(self, request, context):

        host = common__pb2.FormField(name="host", label="Host", required=True,
                                     text_field=common__pb2.TextField.PlainText)
        password = common__pb2.FormField(name="password", label="Password", required=True,
                                         text_field=common__pb2.TextField.Password)
        region = common__pb2.FormField(name="region", label="AWS Region", required=False,
                                       dropdown_field=common__pb2.DropdownField(dropdown_field=["US-EAST", "US-WEST"]))
        hidden = common__pb2.FormField(name="hidden", label="my-hidden-value", text_field=common__pb2.TextField.Hidden)
        is_public = common__pb2.FormField(name="isPublic", label="Public?", description="Is this public?",
                                          toggle_field=common__pb2.ToggleField())

        connect_test = common__pb2.ConfigurationTest(name="connect", label="Tests connection")
        select_test = common__pb2.ConfigurationTest(name="select", label="Tests selection")
        res: common__pb2.ConfigurationFormResponse = common__pb2.ConfigurationFormResponse(
            schema_selection_supported=True,
            table_selection_supported=True,
            fields=[host, password, region, hidden,
                    is_public],
            tests=[connect_test, select_test]

            )
        return res

    def Test(self, request, context):
        req: common__pb2.TestRequest = request
        config = req.configuration
        test_name = req.name
        print("test name: " + test_name)
        res: common__pb2.TestResponse = common__pb2.TestResponse(success=True)
        return res
        # return self.stub

    def CreateTable(self, request, context):
        req: destination__sdk__pb2.CreateTableRequest = request
        config = req.configuration
        print("[CreateTable] :" + str(req.schema_name) + " | " + str(req.table.name) + " | " + str(req.table.columns))
        res: destination__sdk__pb2.CreateTableResponse = destination__sdk__pb2.CreateTableResponse(success=True)
        return res

    def AlterTable(self, request, context):
        req: destination__sdk__pb2.AlterTableRequest = request
        res: destination__sdk__pb2.AlterTableResponse
        config = req.configuration

        print("[AlterTable]: " + str(req.schema_name) + " | " + str(req.table.name) + " | " + str(req.table.columns))
        res: destination__sdk__pb2.AlterTableResponse = destination__sdk__pb2.AlterTableResponse(success=True)
        return res

    def Truncate(self, request, context):
        req: destination__sdk__pb2.TruncateRequest = request
        res: destination__sdk__pb2.TruncateResponse = destination__sdk__pb2.TruncateResponse(success=True)
        print("[TruncateTable]: " + str(req.schema_name) + " | " + str(req.schema_name) + " | soft" + str(req.soft))
        return res

    def WriteBatch(self, request, context):
        req: destination__sdk__pb2.WriteBatchRequest = request
        for replace_file in req.replace_files:
            print("replace files: " + str(replace_file))
        for update_file in req.update_files:
            print("replace files: " + str(update_file))
        for delete_file in req.delete_files:
            print("replace files: " + str(delete_file))

        print("Data loading started for table " + req.table.name)
        for key, value in req.keys.items():
            print("----------------------------------------------------------------------------")
            print("Decrypting and printing file :" + str(key))
            print("----------------------------------------------------------------------------")
            read_csv.decrypt_file(key, value)
        print("\nData loading completed for table " + req.table.name + "\n")

        res: destination__sdk__pb2.WriteBatchResponse = destination__sdk__pb2.WriteBatchResponse(success=True)
        return res

    def DescribeTable(self, request, context):
        req: destination__sdk__pb2.DescribeTableRequest = request
        column1 = common__pb2.Column(name="a1", type=common__pb2.DataType.UNSPECIFIED, primary_key=True)
        column2 = common__pb2.Column(name="a2", type=common__pb2.DataType.DOUBLE)
        table: common__pb2.Table = common__pb2.Table(name=req.table_name, columns=[column1, column2])
        res: destination__sdk__pb2.DescribeTableResponse = destination__sdk__pb2.DescribeTableResponse(not_found=False,
                                                                                                       table=table)
        return res


if __name__ == '__main__':
    server = grpc.server(futures.ThreadPoolExecutor(max_workers=1))
    server.add_insecure_port('[::]:50052')
    destination__sdk__pb2__grpc.add_DestinationServicer_to_server(DestinationImpl(), server)
    server.start()
    print("Destination gRPC server started...")
    server.wait_for_termination()
    print("Destination gRPC server terminated...")
