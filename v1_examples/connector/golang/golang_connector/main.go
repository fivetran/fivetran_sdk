package main

import (
	context "context"
	"encoding/json"
	"flag"
	"fmt"
	"log"
	"net"
	"strconv"

	pb "fivetran.com/fivetran_sdk/proto"
	"google.golang.org/grpc"
	_ "google.golang.org/grpc/encoding/gzip"
)

const INFO = "INFO"
const WARNING = "WARNING"
const SEVERE = "SEVERE"

var port = flag.Int("port", 50051, "The server port")

type MyState struct {
	Cursor int32 `json:"cursor"`
}

type server struct {
	pb.UnimplementedConnectorServer
}

func (s *server) Update(in *pb.UpdateRequest, stream pb.Connector_UpdateServer) error {
	config := in.Configuration
	selection := in.Selection
	state_json := in.GetStateJson()
	state := MyState{}
	json.Unmarshal([]byte(state_json), &state)

	message := fmt.Sprintf("config: %s, selection: %s, state_json: %s, mystate: %+v", config, selection, state_json, state)
	LogMessage(INFO, message)

	// -- Send a log message
	stream.Send(&pb.UpdateResponse{
		Response: &pb.UpdateResponse_LogEntry{
			LogEntry: &pb.LogEntry{
				Level:   pb.LogLevel_INFO,
				Message: "Sync STARTING",
			},
		},
	})

	// -- Send UPSERT records
	schemaName := "schema1"
	for i := 0; i < 3; i++ {
		stream.Send(&pb.UpdateResponse{
			Response: &pb.UpdateResponse_Operation{
				Operation: &pb.Operation{
					Op: &pb.Operation_Record{
						Record: &pb.Record{
							SchemaName: &schemaName,
							TableName:  "table1",
							Type:       pb.OpType_UPSERT,
							Data: map[string]*pb.ValueType{
								"a1": {Inner: &pb.ValueType_String_{String_: "a-" + strconv.Itoa(i)}},
								"a2": {Inner: &pb.ValueType_Double{Double: float64(i) * float64(0.234)}},
							},
						},
					},
				},
			},
		})

		state.Cursor++
	}

	LogMessage(INFO, "Completed sending upsert records")

	// -- Send UPDATE record
	stream.Send(&pb.UpdateResponse{
		Response: &pb.UpdateResponse_Operation{
			Operation: &pb.Operation{
				Op: &pb.Operation_Record{
					Record: &pb.Record{
						SchemaName: &schemaName,
						TableName:  "table1",
						Type:       pb.OpType_UPDATE,
						Data: map[string]*pb.ValueType{
							"a1": {Inner: &pb.ValueType_String_{String_: "a-0"}},
							"a2": {Inner: &pb.ValueType_Double{Double: float64(110.234)}},
						},
					},
				},
			},
		},
	})
	state.Cursor++

	LogMessage(INFO, "Completed sending update records")

	// -- Send DELETE record
	stream.Send(&pb.UpdateResponse{
		Response: &pb.UpdateResponse_Operation{
			Operation: &pb.Operation{
				Op: &pb.Operation_Record{
					Record: &pb.Record{
						SchemaName: &schemaName,
						TableName:  "table1",
						Type:       pb.OpType_DELETE,
						Data: map[string]*pb.ValueType{
							"a1": {Inner: &pb.ValueType_String_{String_: "a-2"}},
						},
					},
				},
			},
		},
	})
	state.Cursor++

	LogMessage(WARNING, "Sample warning message: Completed sending delete records")

	// Serialize state from struct to JSON string
	new_state_json, _ := json.Marshal(state)
	new_state := string(new_state_json)
	log.Println("new_state: ", new_state)

	// -- Send Checkpoint
	stream.Send(&pb.UpdateResponse{
		Response: &pb.UpdateResponse_Operation{
			Operation: &pb.Operation{
				Op: &pb.Operation_Checkpoint{
					Checkpoint: &pb.Checkpoint{
						StateJson: new_state,
					},
				},
			},
		},
	})

	// -- Send a log message
	stream.Send(&pb.UpdateResponse{
		Response: &pb.UpdateResponse_LogEntry{
			LogEntry: &pb.LogEntry{
				Level:   pb.LogLevel_INFO,
				Message: "Sync DONE",
			},
		},
	})

	LogMessage(SEVERE, "Sample severe message: Update call completed")
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
				Name:     "apikey",
				Label:    "API key",
				Required: true,
				Type: &pb.FormField_TextField{
					TextField: pb.TextField_PlainText,
				},
			},
			{
				Name:     "password",
				Label:    "User password",
				Required: true,
				Type: &pb.FormField_TextField{
					TextField: pb.TextField_Password,
				},
			},
			{
				Name:  "hidden",
				Label: "my-hidden-value",
				Type: &pb.FormField_TextField{
					TextField: pb.TextField_Hidden,
				},
			},
			{
				Name:        "isPublic",
				Label:       "Public?",
				Description: &toggleDescription,
				Required:    false,
				Type: &pb.FormField_ToggleField{
					ToggleField: &pb.ToggleField{},
				},
			},
			{
				Name:     "region",
				Label:    "Region",
				Required: true,
				Type: &pb.FormField_DropdownField{
					DropdownField: &pb.DropdownField{
						DropdownField: []string{
							"US-EAST", "US-WEST",
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

func LogMessage(level string, message string) {
	log := map[string]interface{}{
		"level":          level,
		"message":        message,
		"message-origin": "sdk_connector",
	}
	logJSON, _ := json.Marshal(log)
	fmt.Println(string(logJSON))
}

func main() {
	flag.Parse()
	lis, err := net.Listen("tcp", fmt.Sprintf(":%d", *port))
	if err != nil {
		log.Fatalf("failed to listen: %v", err)
	}
	s := grpc.NewServer()
	pb.RegisterConnectorServer(s, &server{})
	log.Printf("server listening at %v", lis.Addr())
	if err := s.Serve(lis); err != nil {
		log.Fatalf("failed to serve: %v", err)
	}
}
