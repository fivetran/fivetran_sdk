import datetime
from concurrent import futures

from google.protobuf.timestamp_pb2 import Timestamp
import grpc

import read_csv

sys.path.append('sdk_pb2')

from sdk_pb2 import destination_sdk_pb2
from sdk_pb2 import common_pb2
from sdk_pb2 import destination_sdk_pb2_grpc


class DestinationImpl(destination_sdk_pb2_grpc.DestinationConnectorServicer):
    def ConfigurationForm(self, request, context):

        host = common_pb2.FormField(
            single=common_pb2.Field(name="host", label="Host", required=True, placeholder="my.example.host",
                                    text_field=common_pb2.TextField.PlainText))

        password = common_pb2.FormField(
            single=common_pb2.Field(name="password", label="Password", required=True, placeholder="my_password",
                                    text_field=common_pb2.TextField.Password))

        region = common_pb2.FormField(
            single=common_pb2.Field(name="region", label="AWS Region", required=False, default_value="US-EAST",
                                    dropdown_field=common_pb2.DropdownField(dropdown_field=["US-EAST", "US-WEST"])))

        hidden = common_pb2.FormField(
            single=common_pb2.Field(name="hidden", label="my-hidden-value", text_field=common_pb2.TextField.Hidden))

        is_public = common_pb2.FormField(
            single=common_pb2.Field(name="isPublic", label="Public?", description="Is this public?",
                                    toggle_field=common_pb2.ToggleField()))

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
        print("test name: " + test_name)
        return common_pb2.TestResponse(success=True)

    def CreateTable(self, request, context):
        print("[CreateTable] :" + str(request.schema_name) + " | " + str(request.table.name) + " | " + str(
            request.table.columns))
        return destination_sdk_pb2.CreateTableResponse(success=True)

    def AlterTable(self, request, context):
        changes_list = request.changes
        change_strings = [str(change) for change in changes_list]
        result = ", ".join(change_strings)
        print("[AlterTable]: " + str(request.schema_name) + " | " + str(request.table_name) + " | " + str(
            result))
        return destination_sdk_pb2.AlterTableResponse(success=True)

    def Capabilities(self, request, context):
        max_int_value = 2**31 - 1  # 32-bit signed int
        max_string_length = int(1e6)
        max_binary_length = int(1e6)
        max_decimal_param = common_pb2.DecimalParams(precision=16, scale=16)

        max_timestamp = datetime.datetime(2999, 12, 31, 23, 59, 59, 99999).timestamp()
        timestamp_seconds = int(max_timestamp)
        timestamp_nanos = int(max_timestamp % 1 * 1e9)
        max_timestamp_param = Timestamp(seconds=timestamp_seconds, nanos=timestamp_nanos)

        destination_map_to_1 = destination_sdk_pb2.DestinationType(name="VARCHAR",
                                                                   map_to=common_pb2.DataType.STRING,
                                                                   max_value=destination_sdk_pb2.MaxValue(numeric_param=max_string_length))
        data_type_mapping_1 = destination_sdk_pb2.DataTypeMappingEntry(
            fivetran_type=common_pb2.DataType.STRING,
            map_to=destination_map_to_1)

        destination_map_to_2 = destination_sdk_pb2.DestinationType(name="NUMBER",
                                                                   map_to=common_pb2.DataType.INT,
                                                                   max_value=destination_sdk_pb2.MaxValue(numeric_param=max_int_value))
        data_type_mapping_2 = destination_sdk_pb2.DataTypeMappingEntry(
            fivetran_type=common_pb2.DataType.FLOAT,
            map_to=destination_map_to_2)

        destination_map_to_3 = destination_sdk_pb2.DestinationType(name="DATE",
                                                                   map_to=common_pb2.DataType.UTC_DATETIME,
                                                                   max_value=destination_sdk_pb2.MaxValue(date_param=max_timestamp_param))
        data_type_mapping_3 = destination_sdk_pb2.DataTypeMappingEntry(
            fivetran_type=common_pb2.DataType.UTC_DATETIME,
            map_to=destination_map_to_3)

        destination_map_to_4 = destination_sdk_pb2.DestinationType(name="BLOB",
                                                                   map_to=common_pb2.DataType.BINARY,
                                                                   max_value=destination_sdk_pb2.MaxValue(numeric_param=max_binary_length))
        data_type_mapping_4 = destination_sdk_pb2.DataTypeMappingEntry(
            fivetran_type=common_pb2.DataType.BINARY,
            map_to=destination_map_to_4)

        destination_map_to_5 = destination_sdk_pb2.DestinationType(name="DECIMAL",
                                                                   map_to=common_pb2.DataType.FLOAT,
                                                                   max_value=destination_sdk_pb2.MaxValue(decimal_param=max_decimal_param))
        data_type_mapping_5 = destination_sdk_pb2.DataTypeMappingEntry(
            fivetran_type=common_pb2.DataType.FLOAT,
            map_to=destination_map_to_5)

        return destination_sdk_pb2.CapabilitiesResponse(
            data_type_mappings=[data_type_mapping_1, data_type_mapping_2, data_type_mapping_3, data_type_mapping_4, data_type_mapping_5],
            supports_history_mode=True)

    def Truncate(self, request, context):
        print("[TruncateTable]: " + str(request.schema_name) + " | " + str(request.schema_name) + " | soft" + str(
            request.soft))
        return destination_sdk_pb2.TruncateResponse(success=True)

    def WriteBatch(self, request, context):
        for replace_file in request.replace_files:
            print("replace files: " + str(replace_file))
        for update_file in request.update_files:
            print("replace files: " + str(update_file))
        for delete_file in request.delete_files:
            print("replace files: " + str(delete_file))

        print("Data loading started for table " + request.table.name)
        for key, value in request.keys.items():
            print("----------------------------------------------------------------------------")
            print("Decrypting and printing file :" + str(key))
            print("----------------------------------------------------------------------------")
            read_csv.decrypt_file(key, value)
        print("\nData loading completed for table " + request.table.name + "\n")

        res: destination_sdk_pb2.WriteBatchResponse = destination_sdk_pb2.WriteBatchResponse(success=True)
        return res

    def DescribeTable(self, request, context):
        column1 = common_pb2.Column(name="a1", type=common_pb2.DataType.UNSPECIFIED, primary_key=True)
        column2 = common_pb2.Column(name="a2", type=common_pb2.DataType.DOUBLE)
        table: common_pb2.Table = common_pb2.Table(name=request.table_name, columns=[column1, column2])
        return destination_sdk_pb2.DescribeTableResponse(not_found=False, table=table)


if __name__ == '__main__':
    server = grpc.server(futures.ThreadPoolExecutor(max_workers=1))
    server.add_insecure_port('[::]:50052')
    destination_sdk_pb2_grpc.add_DestinationConnectorServicer_to_server(DestinationImpl(), server)
    server.start()
    print("Destination gRPC server started...")
    server.wait_for_termination()
    print("Destination gRPC server terminated...")
