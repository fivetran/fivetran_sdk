# SDK Development Guide

Fivetran SDK uses [gRPC](https://grpc.io/) to talk to partner code. The partner side of the interface is always the server side. Fivetran implements the client side and initiates the requests.

## General Guidelines

### Versions
* gRPC: 1.61.1
* protobuf: 3.25.2

### Language

At the moment, partner code should be developed in a language that can generate a statically linked binary executable.

### Command Line Args
The executable needs to:
* Accept a `--port` argument that takes an integer as port number to listen.
* Listen on both IPV4 (i.e. 0.0.0.0) and IPV6 (i.e ::0), but if only one is possible, it should listen on IPV4.

### Proto files

Partners should not add the proto files to their repos. Proto files should be pulled in from this repo at build time and added to `.gitignore` so they are excluded.

### Logging

- Write logs out to STDOUT in the following JSON format. Accepted levels are INFO, WARNING and SEVERE. `Message-origin` can be `sdk_connector` or `sdk_destination`.

```
{
    "level":"INFO",
    "message": "Your log message goes here"
    "message-origin": "sdk_connector"
}
```

- Try to make log messages as _precise_ as possible, which can make it easier to debug issues. 
- Provide context in log messages. Contextual information can make it much easier to debug issues
- Write a separate error message for each exception
- Log _after_ an action. When you log after an action, additional context can be provided.
- Include details about "what went wrong" in your error message
- Manage log volume. Ask yourself if a consumer of the log message will find it useful to solve a problem and whether that need justifies the cost of storing and managing that log. Sources of excessive logging include: 
    - **Tracing entries and exits in methods** - Don't do this unless it is absolutely necessary. 
    - **Logging inside tight loops** - be careful about what you are logging inside loops, especially if the loop runs for many iterations.
    - **Including benign errors** - when a successful execution flow includes handling errors
    - **Repeating errors** - For instance, if you log an exception trace before each retry, you might end up logging the exception trace unnecessarily or too many times
- Consider Logging The Timing Data: Logging the time taken for time-sensitive operations like network calls can make it easier to debug performance issues in production. Consider if logging timing data can be useful in your connector.

### Error Handling
- Partner code should handle any source/destination errors, retry any transient errors internally without deferring them to Fivetran.
- Partner code should use [gRPC built-in error mechanism](https://grpc.io/docs/guides/error/#language-support) to relay errors instead of throwing exceptions and abruptly closing the connection.

### Retries
- Partner code should retry transient problems internally
- Fivetran will not be able to handle any problems that the partner code runs into
- If an error is raised to Fivetran's side, the sync will be terminated and retried from the last good known spot according to saved cursors from the last successful batch.

### Security
The following are hard requirements to be able to deploy Partner code to Fivetran production:
- Do not decrypt batch files to disk. Fivetran does not allow unencrypted files at rest. If you need to upload batch files in plaintext, do the decryption in "streaming" mode. 
- Do not log sensitive data. Ensure only necessary information is kept in logs, and never log any sensitive data. Such data may include credentials (passwords, tokens, keys, etc), customer data, payment information, or PII.
- Encrypt HTTP requests: Things like URLs, URL parameters, and query params are always encrypted for logging, and customer approval is needed to decrypt and examine them.


## Setup Form Guidelines
- Keep the form clear and concise, only requesting essential information for successful connector setup.
- Use clear and descriptive labels for each form field. Avoid technical jargon if possible.
- Organize the fields in a logical order that reflects the setup process.

### RPC Calls
#### ConfigurationForm
This operation retrieves all the setup form fields and tests information. You can provide various parameters for the fields to enhance the user experience, such as descriptions, optional fields, and more.

#### Test
The previous RPC call retrieves the tests that need to be executed during connection setup. This operation then invokes the test with the customer's credentials as parameters. Finally, it should return a success or failure indication for the test execution.

## Source Connector Guidelines

- Don't push anything other than source data to the destination. State will be saved to production DB and returned in `UpdateRequest`.
- Don't forget to handle new schemas/tables/columns per the information and user choices in `UpdateRequest#selection`.
- Make sure you checkpoint at least once an hour. The more frequently you do it, the better.

### RPC Calls
#### Schema
This operation retrieves the customer's schemas, tables, and columns. It also includes an optional `selection_not_supported` field that indicates whether customers can select or deselect tables and columns within the Fivetran dashboard.

#### Update
This operation should retrieve data from the source. We provide a request using the `UpdateRequest` message, which includes the customer's state, credentials, and schema information. The response, delivered through the `UpdateResponse` message, should contain data records or other supported operations.

## Destination Connector Guidelines

- Do not push anything other than source data to the destination.

### System Columns
- In addition to source columns, Fivetran will send the following additional system columns if and when required:
    - `_fivetran_synced`: This is a `UTC_DATETIME` column that represents the start of sync. Every table will have this system column.
    - `_fivetran_deleted`: This column is used to indicate whether a given row is deleted at the source or not. If the source soft-deletes a row or a table, this system column will get added to the table.
    - `_fivetran_id`: Fivetran supports primary-keyless source tables by adding this column as a stand-in pseudo primary key column so that all destination tables have a primary key.

### Compression
Batch files are compressed using [ZSTD](https://en.wikipedia.org/wiki/Zstd)  

### Encryption
- Each batch file is encrypted separately using [AES-256](https://en.wikipedia.org/wiki/Advanced_Encryption_Standard) in [CBC](https://en.wikipedia.org/wiki/Block_cipher_mode_of_operation) mode and with `PKCS5Padding`
- You can find the encryption key for each batch file in `WriteBatchRequest#keys` field
- First 16 bytes of each batch file holds the IV vector

### Batch Files
- Each batch file is size limited to 100MB
- Number of records in each batch file can vary depending on row size
- Currently we only support CSV file format

#### CSV
- Fivetran creates batch files using `com.fasterxml.jackson.dataformat.csv.CsvSchema` which by default doesn't consider backslash as escape character. If you are reading the batch file then make sure that you do not consider backslash as escape character.
- BINARY data will be written to batch files using base64 encoding. You will need to decode it to get back the original byte array.

### RPC Calls
#### CreateTable
This operation should fail if it is asked to create a table that already exists. However, it should not fail if the target schema is missing. The destination should create the missing schema.

#### DescribeTable
This operation should report all columns in the destination table, including Fivetran system columns such as `_fivetran_synced` and `_fivetran_deleted`. It should also provide other additional information as applicable such as data type, `primary_key` and `DecimalParams`.

#### Truncate
- This operation might be requested for a table that does not exist in the destination. In that case, it should NOT fail, simply ignore the request and return `success = true`.
- `utc_delete_before` has millisecond precision.

#### WriteBatch
This operation provides details about the batch files containing the retrieved data. We provide a parameter `WriteBatchRequest` which contains all the information required for you to read the batch files. Here are some of the fields included in the request message:
- `replace_files` is for `upsert` operation where the rows should be inserted if they don't exist or updated if they do. Each row will always provide values for all columns. Set the `_fivetran_synced` column in the destination with the values coming in from the csv files.

- `update_files` is for `update` operation where modified columns have actual values whereas unmodified columns have the special value `unmodified_string` in `CsvFileParams`. Soft-deleted rows will arrive in here as well. Update the `_fivetran_synced` column in the destination with the values coming in from the csv files.

- `delete_files` is for `hard delete` operation. Use primary key columns (or `_fivetran_id` system column for primary-keyless tables) to perform `DELETE FROM`.

- `keys` is a map that provides a list of secret keys that can be used to decrypt batch files.

- `file_params` provides information about the file type and any configurations applied to it, such as encryption or compression.

Also, Fivetran will deduplicate operations such that each primary key will show up only once in any of the operations

Do not assume order of columns in the batch files. Always read the CSV file header to determine column order.

- `CsvFileParams`:
    - `null_string` value is used to represent `NULL` value in all batch files.
    - `unmodified_string` value is used to indicate columns in `update_files` where the values did not change.

#### Capabilities
This operation offers several additional features, as listed below:

- Datatype Mappings: Supports adjusting partner data types for each Fivetran data type.
- Max value for columns: Provides an option to specify the maximum value for each data type.

#### AlterTable
This operation is used to communicate updates to a table. The `SchemaDiff` message within the `AlterTableRequest` parameter provides details about these updates:
- Adding a column (`add_column`): Fivetran uses this field to provide information about a new column to be added in destination table.
- Update Column type (`change_column_type`): This field provides information on updated type of a column in the source that needs to be reflected in a destination table.
- Primary key updates (`new_primary_key`):  If the primary key has changed, this field lists all the new columns used in the updated primary key.


### Examples of Data Types
Examples of each [DataType](https://github.com/fivetran/fivetran_sdk/blob/main/common.proto#L73C6-L73C14) as they would appear in CSV batch files are as follows:
- UNSPECIFIED: This data type will never appear in batch files
- BOOLEAN: "true", "false"
- SHORT: -32768 .. 32767
- INT: -2147483648 .. 2147483647
- LONG: -9223372036854776000 .. 9223372036854775999
- DECIMAL: Floating point values with max precision of 38 and max scale of 37
- FLOAT: Single-precision 32-bit IEEE 754 values, e.g. 3.4028237E+38
- DOUBLE: Double-precision 64-bit IEEE 754 values, e.g. -2.2250738585072014E-308
- NAIVE_TIME: Time without a timezone in the ISO-8601 calendar system, e.g. 10:15:30 
- NAIVE_DATE: Date without a timezone in the ISO-8601 calendar system, e.g. 2007-12-03
- NAIVE_DATETIME: A date-time without timezone in the ISO-8601 calendar system, e.g. 2007-12-03T10:15:30
- UTC_DATETIME: An instantaneous point on the timeline, always in UTC timezone, e.g. 2007-12-03T10:15:30.123Z
- BINARY: Binary data is represented as protobuf `bytes` (such as [ByteString](https://github.com/protocolbuffers/protobuf/blob/main/java/core/src/main/java/com/google/protobuf/ByteString.java) in Java), e.g. `[B@7d4991ad` (showing ByteString as bytes)
- XML: "`<tag>`This is xml`</tag>`"
- STRING: "This is text"
- JSON: "{\"a\": 123}"

## Testing
The following are a list of test scenarios we recommend you consider:
### Source
- Test mapping of all data types between Fivetran types and source/destination types (e.g. [Mysql](https://fivetran.com/docs/databases/mysql#typetransformationsandmapping))
- Big data loads
- Big incremental updates
- Narrow event tables
- Wide fact tables
### Destination
In addition to the suggestions above, consider the following as well:
- Make sure to test with at least one of each of the following source connector types:
    - [Database](https://fivetran.com/docs/databases) (Postgres, MongoDB, etc)
    - [Application](https://fivetran.com/docs/applications) (Github, Hubspot, Google Sheets, etc)
    - [File](https://fivetran.com/docs/files) (S3, Google Drive, etc)
    - [Fivetran Platform connector](https://fivetran.com/docs/logs/fivetran-platform)
- Exercise `AlterTable` in various ways:
    - Adding one or more column(s)
    - Change of primary key columns (adding and removing columns from primary key constraint)
    - Changing data type of non-primary key column(s) 
    - Changing data type of primary key column(s)
- Test tables with and without primary-key
 
## FAQ

### Is it possible for me to see the connector log output?
Sort of. We will email you the logs for a failed sync through support but the number of log messages is limited and this is a slow process for debugging in general. What you need to do is add your own logging for your own platform of choice so you are not reliant on us for logs. Plus, that way, you can implement alerts, monitoring etc.

### Is it normal that for a sync, there is an upsert event followed by a truncate event?
Yes definitely. This is most likely an initial sync where there is nothing but upsert operations, all followed by a truncate which is meant to (soft) delete any rows that may have existed prior to the initial sync starting to make sure they are all marked as deleted (since we cannot be sure the initial sync will necessarily overwrite them all). The "before timestamp" is key to the truncate operation so you don't just mark the entire table deleted. It should pick out the rows that existed prior to the sync starting (aka _fivetran_synced < "truncate before timestamp")
