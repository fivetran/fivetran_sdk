#### June 2024

-----------
- We have added a new data type, `NAIVE_TIME`.

**ConfigurationForm Changes**
- We have added options to provide default value and placeholder for `Field`.
- We have introduced `FieldSet` support, which allows to group the `Fields` and add visibility condition to control displaying on the form.

**SourceConnector Changes**
- We have removed `LogEntry` from the Update operation. Now, partners can use `STD::OUT` to send logs. For more details, please refer to the [logging](https://github.com/fivetran/fivetran_sdk/blob/main/development-guide.md#logging) section in the SDK Development Guide.
- We have renamed the service definition from `Connector` to `SourceConnector`.
- We have renamed `OpType` to `RecordType`.

**DestinationConnector Changes**
- We have renamed the service definition from `Destination` to `DestinationConnector`.
- We have introduced a new rpc method, `Capabilities`, which brings the following functionalities:
    - Adjust column types: Supports optional adjusting of a Partner data type for each Fivetran type.
    - Define maximum value supported for the column types.
    - We have added support for Parquet batch files.
    - We have introduced a new field, `supports_history_mode`, which indicates whether the destination supports history mode.
- Changes in `AlterTableRequest`:
    - It now uses `table_name` instead of `Table`.
    - We have introduced a new field, `SchemaDiff`, which supports adding new columns, changing column type, and updating primary keys.  
- Changes in WriteBatchRequest:
    - We have added support for Parquet batch files configuration details.
    - We have introduced a new boolean field, `history_mode`, which indicates that history mode is enabled for the table.
