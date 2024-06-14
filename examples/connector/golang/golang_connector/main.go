package main

import (
	context "context"
	"encoding/json"
	"flag"
	"fmt"
	"log"
	"net"
	"strconv"

    "google.golang.org/grpc"
    _ "google.golang.org/grpc/encoding/gzip"

	pb "fivetran.com/fivetran_sdk/proto"
)

var port = flag.Int("port", 50051, "The server port")

type MyState struct {
	Cursor int32 `json:"cursor"`
}

type server struct {
	pb.UnimplementedSourceConnectorServer
}

func (s *server) Update(in *pb.UpdateRequest, stream pb.SourceConnector_UpdateServer) error {
	config := in.Configuration
	selection := in.Selection
	state_json := in.GetStateJson()
	state := MyState{}
	json.Unmarshal([]byte(state_json), &state)

	log.Println("config: ", config, "selection: ", selection, "state_json: ", state_json, "mystate: ", state)

	// -- Send a log message
	logEntry := map[string]interface{}{
		"level":   "INFO",
		"message": "Sync STARTING",
	}
	logJSON, _ := json.Marshal(logEntry)
	fmt.Println(string(logJSON))

	// -- Send UPSERT records
	schemaName := "schema1"
	for i := 0; i < 3; i++ {
		stream.Send(&pb.UpdateResponse{
			Operation: &pb.UpdateResponse_Record{
				Record: &pb.Record{
					SchemaName: &schemaName,
					TableName:  "table1",
					Type:       pb.RecordType_UPSERT,
					Data: map[string]*pb.ValueType{
						"a1": {Inner: &pb.ValueType_String_{String_: "a-" + strconv.Itoa(i)}},
						"a2": {Inner: &pb.ValueType_Double{Double: float64(i) * float64(0.234)}},
					},
				},
			},
		})

		state.Cursor++
	}

	// -- Send UPDATE record
	stream.Send(&pb.UpdateResponse{
		Operation: &pb.UpdateResponse_Record{
			Record: &pb.Record{
				SchemaName: &schemaName,
				TableName:  "table1",
				Type:       pb.RecordType_UPDATE,
				Data: map[string]*pb.ValueType{
					"a1": {Inner: &pb.ValueType_String_{String_: "a-0"}},
					"a2": {Inner: &pb.ValueType_Double{Double: float64(110.234)}},
				},
			},
		},
	})
	state.Cursor++

	// -- Send DELETE record
	stream.Send(&pb.UpdateResponse{
		Operation: &pb.UpdateResponse_Record{
			Record: &pb.Record{
				SchemaName: &schemaName,
				TableName:  "table1",
				Type:       pb.RecordType_DELETE,
				Data: map[string]*pb.ValueType{
					"a1": {Inner: &pb.ValueType_String_{String_: "a-2"}},
				},
			},
		},
	})
	state.Cursor++

	// Serialize state from struct to JSON string
	new_state_json, _ := json.Marshal(state)
	new_state := string(new_state_json)
	log.Println("new_state: ", new_state)

	// -- Send Checkpoint
	stream.Send(&pb.UpdateResponse{
		Operation: &pb.UpdateResponse_Checkpoint{
			Checkpoint: &pb.Checkpoint{
				StateJson: new_state,
			},
		},
	})

	// -- Send a log message
	syncEndLog := map[string]interface{}{
		"level":   "INFO",
		"message": "Sync DONE",
	}
	syncEndLogJson, _ := json.Marshal(syncEndLog)
	fmt.Println(string(syncEndLogJson))

	// End the RPC call
	return nil
}

func (s *server) Schema(ctx context.Context, in *pb.SchemaRequest) (*pb.SchemaResponse, error) {
	config := in.Configuration
	log.Println(config)

	return &pb.SchemaResponse{
		Response: &pb.SchemaResponse_WithSchema{
			WithSchema: &pb.SchemaList{
				Schemas: []*pb.Schema{
					{
						Name: "schema1",
						Tables: []*pb.Table{
							{
								Name: "table1",
								Columns: []*pb.Column{
									{
										Name:       "a1",
										Type:       pb.DataType_UNSPECIFIED,
										PrimaryKey: true,
									},
									{
										Name:       "a2",
										Type:       pb.DataType_DOUBLE,
										PrimaryKey: false,
									},
								},
							},
							{
								Name: "table2",
								Columns: []*pb.Column{
									{
										Name:       "b1",
										Type:       pb.DataType_STRING,
										PrimaryKey: true,
									},
								},
							},
						},
					},
				},
			},
		},
	}, nil
}

func (s *server) ConfigurationForm(ctx context.Context, in *pb.ConfigurationFormRequest) (*pb.ConfigurationFormResponse, error) {
	toggleDescription := "Is this public?"

	return &pb.ConfigurationFormResponse{
		SchemaSelectionSupported: true,
		TableSelectionSupported:  true,
		Fields: []*pb.FormField{
			{
				Field: &pb.FormField_Single{
					Single: &pb.Field{
						Name:     "apikey",
						Label:    "API key",
						Required: true,
						Type: &pb.Field_TextField{
							TextField: pb.TextField_PlainText,
						},
					},
				},
			},
			{
				Field: &pb.FormField_Single{
					Single: &pb.Field{
						Name:     "password",
						Label:    "User password",
						Required: true,
						Type: &pb.Field_TextField{
							TextField: pb.TextField_Password,
						},
					},
				},
			},
			{
				Field: &pb.FormField_Single{
					Single: &pb.Field{
						Name:  "hidden",
						Label: "my-hidden-value",
						Type: &pb.Field_TextField{
							TextField: pb.TextField_Hidden,
						},
					},
				},
			},
			{
				Field: &pb.FormField_Single{
					Single: &pb.Field{
						Name:        "isPublic",
						Label:       "Public?",
						Description: &toggleDescription,
						Required:    false,
						Type: &pb.Field_ToggleField{
							ToggleField: &pb.ToggleField{},
						},
					},
				},
			},
			{
				Field: &pb.FormField_Single{
					Single: &pb.Field{
						Name:     "region",
						Label:    "Region",
						Required: true,
						Type: &pb.Field_DropdownField{
							DropdownField: &pb.DropdownField{
								DropdownField: []string{
									"US-EAST", "US-WEST",
								},
							},
						},
					},
				},
			},
			{
				Field: &pb.FormField_FieldSet{
					FieldSet: &pb.FieldSet{
						Fields: []*pb.FormField{
							{
								Field: &pb.FormField_Single{
									Single: &pb.Field{
										Name:     "connectionString",
										Label:    "ConnectionString",
										Required: false,
										Type: &pb.Field_TextField{
											TextField: pb.TextField_Password,
										},
									},
								},
							},
							{
								Field: &pb.FormField_Single{
									Single: &pb.Field{
										Name:     "sshTunnel",
										Label:    "SSH Tunnel",
										Required: false,
										Type: &pb.Field_TextField{
											TextField: pb.TextField_PlainText,
										},
									},
								},
							},
						},
						Condition: &pb.VisibilityCondition{
							FieldName: "isPublic",
							Condition: &pb.VisibilityCondition_HasStringValue{
								HasStringValue: "false",
							},
						},
					},
				},
			},
		},
		Tests: []*pb.ConfigurationTest{
			{
				Name:  "connect",
				Label: "Test connection",
			},
			{
				Name:  "select",
				Label: "Test selection",
			},
		},
	}, nil
}

func (s *server) Test(ctx context.Context, in *pb.TestRequest) (*pb.TestResponse, error) {
	config := in.Configuration
	log.Println(config)

	log.Printf("test name: %v", in.Name)
	return &pb.TestResponse{
		Response: &pb.TestResponse_Success{
			Success: true,
		},
	}, nil
}

func main() {
	flag.Parse()
	lis, err := net.Listen("tcp", fmt.Sprintf(":%d", *port))
	if err != nil {
		log.Fatalf("failed to listen: %v", err)
	}
	s := grpc.NewServer()
	pb.RegisterSourceConnectorServer(s, &server{})
	log.Printf("server listening at %v", lis.Addr())
	if err := s.Serve(lis); err != nil {
		log.Fatalf("failed to serve: %v", err)
	}
}
