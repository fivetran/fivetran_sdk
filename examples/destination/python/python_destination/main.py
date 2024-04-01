from concurrent import futures
import grpc
import common_pb2 as comm
import destination_sdk_pb2 as dest
import destination_sdk_pb2_grpc as dest_grpc


class DestinationImpl(dest_grpc.DestinationServicer):
    def ConfigurationForm(self, request, context):

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
        return res

    def Test(self, request, context):
        req: comm.TestRequest = request
        config = req.configuration
        test_name = req.name
        print("test name: " + test_name)
        res: comm.TestResponse = comm.TestResponse(success=True)
        return res
        # return self.stub

    def CreateTable(self, request, context):
        req: dest.CreateTableRequest = request
        config = req.configuration
        print("[CreateTable] :" + str(req.schema_name) + " | " + str(req.table.name) + " | " + str(req.table.columns))
        res: dest.CreateTableResponse = dest.CreateTableResponse(success=True)
        return res

    def AlterTable(self, request, context):
        req: dest.AlterTableRequest = request
        res: dest.AlterTableResponse
        config = req.configuration

        print("[AlterTable]: " + str(req.schema_name) + " | " + str(req.table.name) + " | " + str(req.table.columns))
        res: dest.AlterTableResponse = dest.AlterTableResponse(success=True)
        return res

    def Truncate(self, request, context):
        req: dest.TruncateRequest = request
        res: dest.TruncateResponse = dest.TruncateResponse(success=True)
        print("[TruncateTable]: " + str(req.schema_name) + " | " + str(req.schema_name) + " | soft" + str(req.soft))
        return res

    def WriteBatch(self, request, context):
        req: dest.WriteBatchRequest = request
        for replace_file in req.replace_files:
            print("replace files: " + str(replace_file))
        for update_file in req.update_files:
            print("replace files: " + str(update_file))
        for delete_file in req.delete_files:
            print("replace files: " + str(delete_file))

        for key, value in req.keys.items():
            print(f"{key}: {value}")

        res: dest.WriteBatchResponse = dest.WriteBatchResponse(success=True)
        return res

    def DescribeTable(self, request, context):
        req: dest.DescribeTableRequest = request
        column1 = comm.Column(name="a1", type=comm.DataType.UNSPECIFIED, primary_key=True)
        column2 = comm.Column(name="a2", type=comm.DataType.DOUBLE)
        table: comm.Table = comm.Table(name=req.table_name, columns=[column1, column2])
        res: dest.DescribeTableResponse = dest.DescribeTableResponse(not_found=False, table=table)
        return res


if __name__ == '__main__':
    server = grpc.server(futures.ThreadPoolExecutor(max_workers=5))
    server.add_insecure_port('[::]:50052')
    dest_grpc.add_DestinationServicer_to_server(DestinationImpl(), server)
    server.start()
    print("Destination gRPC server started")
    server.wait_for_termination()
