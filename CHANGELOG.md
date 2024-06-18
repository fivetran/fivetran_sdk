#### June 2024

-----------
- Added a new `NAIVE_TIME` data type.

**ConfigurationForm Changes**
- We have added options to provide default value and placeholder for `Field`.
- We have introduced `FieldSet` support, which allows to group the `Fields` and add visibility condition to control displaying on the form.
- The `FormField` format is updated to support `FieldSet`. A single attribute should be used to assign the regular fields.

**SourceConnector Changes**
- We have removed `LogEntry` from the Update operation. Now, partners can use `STD::OUT` to send logs. For more details, please refer to the [logging](https://github.com/fivetran/fivetran_sdk/blob/main/development-guide.md#logging) section in the SDK Development Guide.
- We have renamed service definition from `Connector` to `SourceConnector`.
- We have renamed `OpType` to `RecordType`.

**DestinationConnector Changes**
- We have renamed the service definition from `Destination` to `DestinationConnector`.
- A new rpc method `Capabilities` is introduced. This brings the following functionalities:
    - Adjust column types: Supports optionally adjusting Partner data type for each Fivetran type.
    - Define the maximal value supported for the column types.
    - We have added support for providing parquet batch files. Now, partner can choose the file type to create batch files.
    - We have introduced a new field, `supports_history_mode`, which indicates whether the destination supports history mode.
- Changes in `AlterTableRequest`:
    - It now uses `table_name` instead of `Table`
    - We have introduced a new field, `SchemaDiff`, which supports adding new columns, changing column type, and updating primary keys.  
- Changes in WriteBatchRequest:
  - We have added support for parquet batch files configuration details.
  - We have introduced new field `HistoryMode`, which indicates that history mode is enabled for the table.