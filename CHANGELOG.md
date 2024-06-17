#### June 2024

-----------
**ConfigurationForm Changes**
- We added options to provide default value and placeholder for the Field.
- We Introduced FieldSet support, which allows to group the Fields and add visibility condition to control displaying on the form.
- The FormField format is updated to support FieldSet. The single attribute should be used to assign the regular fields.

**SourceConnector Changes**
- We removed `LogEntry` from Update operation. Now, partners can use `STD::OUT` to send logs. For more details, please refer to [this](https://github.com/fivetran/fivetran_sdk/blob/main/development-guide.md#logging).
- We have renamed the few enums and services:
  - Renamed the service definition from `Connector` to `SourceConnector`.
  - Renamed the `OpType` field to `RecordType`.
- Added support for `NAIVE_TIME` datatype. Partners can now send data with this datatype.

**DestinationConnector Changes**
- Added `NAIVE_TIME` data type support.
- We have renamed the service definition from `Destination` to `DestinationConnector`.
- A new rpc method `Capabilities` is introduced. This brings the functionalities:
    - Adjust column types: Supports adjusting Partner data type for each Fivetran type.
    - Define the max value supported for the columns types.
    - We have added support for providing parquet batch files. Now, partner can choose the file type to create batch files.
    - Introduced a new field `supports_history_mode`, which indicates whether the destination supports history mode.
- Changes in AlterTableRequest:
    - Takes `table_name` instead of `Table`
    - Introduced SchemaDiff field, which supports adding new columns, change column type, and update primary keys.  
- Changes in WriteBatchRequest:
  - We have added support for parquet batch files configuration details.
  - Introduced new field `HistoryMode`, which indicates history mode is enabled for table.