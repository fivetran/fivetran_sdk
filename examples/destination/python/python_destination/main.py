from concurrent import futures
import grpc

import common_pb2 as comm
import destination_sdk_pb2 as dest
import destination_sdk_pb2_grpc as dest_grpc


class DestinationImpl(dest_grpc.DestinationServicer):
    def __init__(self):
        self.stub = dest_grpc.DestinationStub()

    def ConfigurationForm(self, request, context):
        req: comm.ConfigurationFormRequest = request

        host = comm.FormField(name="host", label="Host", required=True, text_field=comm.TextField.PlainText)
        password = comm.FormField(name="password", label="Password", required=True, text_field=comm.TextField.Password)
        region = comm.FormField(name="region", label="AWS Region", required=False,
                                dropdown_field=comm.DropdownField(dropdown_field=["US-EAST", "US-WEST"]))
        hidden = comm.FormField(name="hidden", label="my-hidden-value", text_field=comm.TextField.Hidden)
        is_public = comm.FormField(name="isPublic", label="Public?", description="Is this public?",
                                   toggle_field=comm.ToggleField())

        connect_test = comm.ConfigurationTest(name="connect", label="Tests connection")
        select_test = comm.ConfigurationTest(name="select", label="Tests selection")
        res: comm.ConfigurationFormResponse = comm.ConfigurationFormResponse(schema_selection_supported=True,
                                                                             table_selection_supported=True,
                                                                             fields=[host, password, region, hidden,
                                                                                     is_public],
                                                                             tests=[connect_test, select_test]
                                                                             )
        context.set_details(res)

    def Test(self, request, context):
        req: comm.TestRequest = request
        config = req.configuration
        test_name = req.name
        print("test name: " + test_name)
        res: comm.TestResponse = context
        res.success = True
        context.set_details(res)
        # return self.stub

    def CreateTable(self, request, context):
        req: dest.CreateTableRequest = request
        config = req.configuration
        print("[CreateTable] :" + req.schema_name + " | " + req.table.name + " | " + req.table.columns)
        res: dest.CreateTableResponse
        res.success = True
        context.set_details(res)

    def AlterTable(self, request, context):
        req: dest.AlterTableRequest = request
        res: dest.AlterTableResponse
        config = req.configuration

        print("[AlterTable]: " + req.schema_name + " | " + req.table.name + " | " + req.table.columns)
        res.success = True
        context.set_details(res)

    def Truncate(self, request, context):
        req: dest.TruncateRequest = request
        res: dest.TruncateResponse
        print("[TruncateTable]: " + req.schema_name + " | " + req.schema_name + " | soft" + req.soft)
        res.success = True
        context.set_details(res)

    def WriteBatch(self, request, context):
        req: dest.WriteBatchRequest = request
        for replace_file in req.replace_files:
            print("replace files: " + replace_file)
        for update_file in req.update_files:
            print("replace files: " + update_file)
        for delete_file in req.delete_files:
            print("replace files: " + delete_file)

        for key, value in req.keys.items():
            print(f"{key}: {value}")

        res: dest.WriteBatchResponse
        res.success = True
        context.set_details(res)

    def DescribeTable(self, request, context):
        req: dest.DescribeTableRequest = request
        column1 = comm.Column(name="a1", type=comm.DataType.UNSPECIFIED, primary_key=True)
        column2 = comm.Column(name="a2", type=comm.DataType.DOUBLE)
        table: comm.Table = comm.Table(name=req.table_name, columns=[column1, column2])
        res: dest.DescribeTableResponse
        res.table = table
        context.set_details(res)


if __name__ == '__main__':
    server = grpc.server(futures.ThreadPoolExecutor(max_workers=5))
    server.add_insecure_port('[::]:50051')
    
    server.start()
    server.await_termination()