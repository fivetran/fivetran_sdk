const grpc = require("@grpc/grpc-js");
const PROTO_PATH_COMMON = "./protos/common.proto";
const PROTO_PATH_CONNECTOR = "./protos/connector_sdk.proto";
var protoLoader = require("@grpc/proto-loader");

const options = {
  keepCase: true,
  longs: String,
  enums: String,
  defaults: true,
  oneofs: true,
};
var packageDefinitionCommon = protoLoader.loadSync(PROTO_PATH_COMMON, options);
var packageDefinitionConnector = protoLoader.loadSync(PROTO_PATH_CONNECTOR, options);

const commonProto = grpc.loadPackageDefinition(packageDefinitionCommon);
const protoDescriptor = grpc.loadPackageDefinition(packageDefinitionConnector);

const connectorSdkProto = protoDescriptor.fivetran_sdk;

const server = new grpc.Server();

const configurationForm = (call, callback) => {
    callback(null, {
      schema_selection_supported: true,
      table_selection_supported: true,
      fields: [
        { name: "apikey", label: "API key", required: true, text_field: "PlainText" },
        { name: "password", label: "User Password", required: true, text_field: "Password" },
        // { name: "region", label: "AWS Region", required: false, dropdown_field: [ "US-EAST", "US-WEST" ] },
        { name: "hidden", label: "my-hidden-value", text_field:"Hidden" },
        { name: "isPublic", label: "Public?", description: "Is this public?", toggle_field: {} }
      ],
      tests: [
        { name: "connect", label: "Tests connection" },
        { name: "select", label: "Tests selection" }
      ]
    });
  };

  // Implement the Test RPC method
  const test = (call, callback) => {
    const configuration = call.request.configuration;
    const testName = call.request.name;
    console.log(`Test name: ${testName}`);
    callback(null, { success: true });
  };

  // Implement the Schema RPC method
  const schema = (call, callback) => {
    const tableList = {
      tables: [
        {
          name: "table1",
          columns: [
            { name: "a1", type: "UNSPECIFIED", primary_key: true },
            { name: "a2", type: "DOUBLE" }
          ]
        },
        {
          name: "table2",
          columns: [
            { name: "b1", type: "STRING", primary_key: true },
            { name: "b2", type: "UNSPECIFIED" }
          ]
        }
      ]
    };
    callback(null, { without_schema: tableList });
  };


  // Implement the update RPC method
  const update = (call, callback) => {
    const { configuration, state_json = '{}', selection } = call.request;
    
    const state = JSON.parse(state_json);
  
    const sendResponse = (response) => {
      call.write(response);
    };
  
    const sendLogEntry = (message) => {
      sendResponse({
        log_entry: {
          level: "INFO",
          message: message
        }
      });
    };
  
    const sendOperation = (operation) => {
      sendResponse({
        operation: operation
      });
    };
  
    try {
      // Send a log message
      sendLogEntry("Sync STARTING");
  
      // Send UPSERT records
      for (let i = 0; i < 3; i++) {
        sendOperation({
          record: {
            table_name: "table1",
            type: "UPSERT",
            data: {
              a1: { string_value: `a-${i}` },
              a2: { double_value: i * 0.234 }
            }
          }
        });
        state.cursor = (state.cursor || 0) + 1;
      }
  
      // Send UPDATE record
      sendOperation({
        record: {
          table_name: "table1",
          type: "UPDATE",
          data: {
            a1: { string_value: "a-0" },
            a2: { double_value: 110.234 }
          }
        }
      });
      state.cursor = (state.cursor || 0) + 1;
  
      // Send DELETE record
      sendOperation({
        record: {
          table_name: "table1",
          type: "DELETE",
          data: {
            a1: { string_value: "a-2" }
          }
        }
      });
      state.cursor = (state.cursor || 0) + 1;
  
      // Send checkpoint
      const newState = JSON.stringify(state);
      sendOperation({
        checkpoint: {
          state_json: newState
        }
      });
  
      // Send a log message
      sendLogEntry("Sync DONE");
  
    } catch (error) {
      callback(error);
    }
  
    // End the streaming RPC call
    call.end();
  };


server.addService(connectorSdkProto.Connector.service, {configurationForm, test, schema, update})

server.bindAsync(
  "127.0.0.1:50051",
  grpc.ServerCredentials.createInsecure(),
  (error, port) => {
    console.log("Server running at http://127.0.0.1:50051");
    server.start();
  }
);
