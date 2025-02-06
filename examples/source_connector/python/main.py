import grpc
from concurrent import futures
import json
import sys
sys.path.append('sdk_pb2')

from sdk_pb2 import connector_sdk_v2_pb2_grpc
from sdk_pb2 import common_v2_pb2
from sdk_pb2 import connector_sdk_v2_pb2

INFO = "INFO"
WARNING = "WARNING"
SEVERE = "SEVERE"

class ConnectorService(connector_sdk_v2_pb2_grpc.SourceConnectorServicer):
    def ConfigurationForm(self, request, context):
        log_message(INFO, "Fetching configuration form")
        form_fields = common_v2_pb2.ConfigurationFormResponse(schema_selection_supported=True,
                                                           table_selection_supported=True)
        # Add the 'apiBaseURL' field
        form_fields.fields.add(
            name="apiBaseURL",
            label="API base URL",
            description="Enter the base URL for the API you're connecting to",
            required=True,
            text_field=common_v2_pb2.TextField.PlainText,
            placeholder="api_base_url"
        )

        # Add the 'authenticationMethod' dropdown field
        form_fields.fields.add(
            name="authenticationMethod",
            label="Authentication Method",
            description="Choose the preferred authentication method to securely access the API",
            dropdown_field=common_v2_pb2.DropdownField(dropdown_field=["OAuth2.0", "API Key", "Basic Auth", "None"]),
            default_value="None"
        )

        # Add the 'api_key' field (for API Key authentication method)
        api_key = common_v2_pb2.FormField(
            name="api_key",
            label="API Key",
            text_field=common_v2_pb2.TextField.Password,
            placeholder="your_api_key_here"
        )

        # Add the 'client_id' field (for OAuth authentication method)
        client_id = common_v2_pb2.FormField(
            name="client_id",
            label="Client ID",
            text_field=common_v2_pb2.TextField.Password,
            placeholder="your_client_id_here"
        )

        # Add the 'client_secret' field (for OAuth authentication method)
        client_secret = common_v2_pb2.FormField(
            name="client_secret",
            label="Client Secret",
            text_field=common_v2_pb2.TextField.Password,
            placeholder="your_client_secret_here"
        )

        # Add the 'userName' field (for Basic Auth authentication method)
        username = common_v2_pb2.FormField(
            name="username",
            label="Username",
            text_field=common_v2_pb2.TextField.PlainText,
            placeholder="your_username_here"
        )

        # Add the 'password' field (for Basic Auth authentication method)
        password = common_v2_pb2.FormField(
            name="password",
            label="Password",
            text_field=common_v2_pb2.TextField.Password,
            placeholder="your_password_here"
        )

        # Define the Visibility Conditions for Conditional Fields

        # For OAuth2.0 authentication
        visibility_condition_oauth = common_v2_pb2.VisibilityCondition(
            condition_field="authenticationMethod",
            string_value="OAuth2.0"
        )

        # Create conditional fields for OAuth2.0
        conditional_oauth_fields = common_v2_pb2.ConditionalFields(
            condition=visibility_condition_oauth,
            fields=[client_id, client_secret]
        )

        # Add conditional fields for OAuth2.0 to the form
        form_fields.fields.add(
            name="conditionalOAuthFields",
            label="OAuth2.0 Conditional Fields",
            conditional_fields=conditional_oauth_fields
        )

        # For API Key authentication
        visibility_condition_api_key = common_v2_pb2.VisibilityCondition(
            condition_field="authenticationMethod",
            string_value="API Key"
        )

        # Create conditional fields for API Key
        conditional_api_key_fields = common_v2_pb2.ConditionalFields(
            condition=visibility_condition_api_key,
            fields=[api_key]
        )

        # Add conditional fields for API Key to the form
        form_fields.fields.add(
            name="conditionalApiKeyFields",
            label="API Key Conditional Fields",
            conditional_fields=conditional_api_key_fields
        )

        # For Basic Auth authentication
        visibility_condition_basic_auth = common_v2_pb2.VisibilityCondition(
            condition_field="authenticationMethod",
            string_value="Basic Auth"
        )

        # Create conditional fields for Basic Auth
        conditional_basic_auth_fields = common_v2_pb2.ConditionalFields(
            condition=visibility_condition_basic_auth,
            fields=[username, password]
        )

        # Add conditional fields for Basic Auth to the form
        form_fields.fields.add(
            name="conditionalBasicAuthFields",
            label="Basic Auth Conditional Fields",
            conditional_fields=conditional_basic_auth_fields
        )

        # Add the 'apiVersion' dropdown field
        form_fields.fields.add(
            name="apiVersion",
            label="API Version",
            dropdown_field=common_v2_pb2.DropdownField(dropdown_field=["v1", "v2", "v3"]),
            default_value="v2"
        )

        # Add the 'shouldEnableMetrics' toggle field
        form_fields.fields.add(
            name="shouldEnableMetrics",
            label="Enable Metrics?",
            toggle_field=common_v2_pb2.ToggleField()
        )

        # Add the 'connect' and 'select' tests to the form
        form_fields.tests.add(
            name="connect",
            label="Tests connection"
        )

        form_fields.tests.add(
            name="select",
            label="Tests selection"
        )

        # Return or send the populated form
        return form_fields

    def Test(self, request, context):
        configuration = request.configuration
        # Name of the test to be run
        test_name = request.name

        log_message(INFO, "Test Name: " + str(test_name))
        
        return common_v2_pb2.TestResponse(success=True)

    def Schema(self, request, context):
        table_list = common_v2_pb2.TableList()
        t1 = table_list.tables.add(name="table1")
        t1.columns.add(name="a1", type=common_v2_pb2.DataType.UNSPECIFIED, primary_key=True)
        t1.columns.add(name="a2", type=common_v2_pb2.DataType.DOUBLE)

        t2 = table_list.tables.add(name="table2")
        t2.columns.add(name="b1", type=common_v2_pb2.DataType.UNSPECIFIED, primary_key=True)
        t2.columns.add(name="b2", type=common_v2_pb2.DataType.UNSPECIFIED)

        return connector_sdk_v2_pb2.SchemaResponse(without_schema=table_list)

    def Update(self, request, context):

        log_message(WARNING, "Sync Start")
        state_json = "{}"
        if request.HasField('state_json'):
            state_json = request.state_json

        state = json.loads(state_json)
        if state.get("cursor") is None:
            state["cursor"] = 0

        # -- Send UPSERT records
        for t in range(0, 3):
            val1 = common_v2_pb2.ValueType()
            val1.string = "a-" + str(t)

            val2 = common_v2_pb2.ValueType()
            val2.double = t * 0.234

            record = connector_sdk_v2_pb2.Record()
            record.type = common_v2_pb2.RecordType.UPSERT
            record.table_name = "table1"
            record.data["a1"].CopyFrom(val1)
            record.data["a2"].CopyFrom(val2)
            state["cursor"] += 1

            yield connector_sdk_v2_pb2.UpdateResponse(record=record)

        # -- Send UPSERT record for table2
        val1 = common_v2_pb2.ValueType()
        val1.string = "b1"
        val2 = common_v2_pb2.ValueType()
        val2.string = "ben"
        record = connector_sdk_v2_pb2.Record()
        record.type = common_v2_pb2.RecordType.UPSERT
        record.table_name = "table2"
        record.data["b1"].CopyFrom(val1)
        record.data["b2"].CopyFrom(val2)
        state["cursor"] += 1

        yield connector_sdk_v2_pb2.UpdateResponse(record=record)

        # -- Send UPDATE record
        val1 = common_v2_pb2.ValueType()
        val1.string = "a-0"

        val2 = common_v2_pb2.ValueType()
        val2.double = 110.234

        record = connector_sdk_v2_pb2.Record()
        record.type = common_v2_pb2.RecordType.UPDATE
        record.table_name = "table1"
        record.data["a1"].CopyFrom(val1)
        record.data["a2"].CopyFrom(val2)

        yield connector_sdk_v2_pb2.UpdateResponse(record=record)
        state["cursor"] += 1

        log_message(WARNING, "Completed sending update records")

        # -- Send DELETE record
        val1 = common_v2_pb2.ValueType()
        val1.string = "a-2"

        record = connector_sdk_v2_pb2.Record()
        record.type = common_v2_pb2.RecordType.DELETE
        record.table_name = "table1"
        record.data["a1"].CopyFrom(val1)

        yield connector_sdk_v2_pb2.UpdateResponse(record=record)
        state["cursor"] += 1

        checkpoint = connector_sdk_v2_pb2.Checkpoint()
        checkpoint.state_json = json.dumps(state)
        yield connector_sdk_v2_pb2.UpdateResponse(checkpoint=checkpoint)

        log_message(SEVERE, "Sending severe log: Completed Update method")


def log_message(level, message):
    print(f'{{"level":"{level}", "message": "{message}", "message-origin": "sdk_connector"}}')


def start_server():
    server = grpc.server(futures.ThreadPoolExecutor(max_workers=1))
    connector_sdk_v2_pb2_grpc.add_SourceConnectorServicer_to_server(ConnectorService(), server)
    server.add_insecure_port('[::]:50051')
    server.start()
    print("Server started...")
    server.wait_for_termination()
    print("Server terminated.")


if __name__ == '__main__':
    print("Starting the server...")
    start_server()