#### June 2024

-----------
**ConfigurationForm Changes**
- We added options to provide default value and placeholder for the Field.
- We Introduced  FieldSet  support, which allows to group the Fields and add visibility condition to control displaying on the form.
- The FormField format is updated to support FieldSet. The single attribute should be used to assign the usual fields.

**SourceConnector Changes**
- We removed LogEntry from Update operation. Now, partners can use STD::OUT to send logs. For more details, please refer to this.
- We renamed few enums and services:
  - Renamed the service definition from Connector to SourceConnector.
  - Renamed the OpType field to RecordType.
- Added support for NAIVE_TIME datatype. Partners can now send data with this datatype.

**DestinationConnector Changes**
- Added NAIVE_TIME data type support.
- We renamed the service definition from Destination to DestinationConnector.
- A new rpc method Capabilities is introduced. This brings the functionalities:
    - Adjust column type: Supports adjusting Partner data type for each Fivetran type
    - Define the max value supported for the columns types.
- Changes in AlterTableRequest:
    - Takes table_name instead of Table
    - Introduced SchemaDiff field, which supports adding new columns, change column type, and update primary keys.