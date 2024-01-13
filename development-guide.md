# SDK Development Guide

Fivetran SDK uses [gRPC](https://grpc.io/) to talk to partner code. The partner side of the interface is always the server side. Fivetran implements the client side and initiates the requests.

## General Guidelines

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
    - **Tracing entries and exits in methods** - Don't do this unless it is absolutely necessary. If you must do it, consider using the `NOTICE` level.
    - **Logging inside tight loops** - be careful about what you are logging inside loops, especially if the loop runs for many iterations.
    - **Including benign errors** - when a successful execution flow includes handling errors
    - **Repeating errors** - For instance, if you log an exception trace before each retry, you might end up logging the exception trace unnecessarily or too many times
- Consider Logging The Timing Data: Logging the time taken for time-sensitive operations like network calls can make it easier to debug performance issues in production. Consider if logging timing data can be useful in your connector.

### Error Handling
- Partner code should handle any source/destination errors, retry any transient errors internally without deferring them to Fivetran.
- Partner code should use [gRPC built-in error mechanism](https://grpc.io/docs/guides/error/#language-support) to relay errors instead of throwing exceptions and abruptly closing the connection.

### Security

- Do not decrypt the batch files to disk. Fivetran does not allow unencrypted files at rest. If you need to upload batch files in plaintext, do the decryption in "streaming" mode. 
- Do not log sensitive data. Ensure only necessary information is kept in logs, and never log any sensitive data. Such data may include credentials (passwords, tokens, keys, etc), customer data, payment information, or PII.
- Encrypt HTTP requests: Things like URLs, URL parameters, and query params are always encrypted for logging, and customer approval is needed to decrypt and examine them.


## Connector Guidelines

- Don't push anything other than source data to the destination. State will be saved to production DB and returned in `UpdateRequest`.
- Don't forget to handle new schemas/tables/columns per the information and user choices in `UpdateRequest#selection`.

## Destination Guidelines

- Do not push anything other than source data to the destination.

### System Columns
- In addition to source columns, Fivetran will send the following additional system columns:
    - `_fivetran_synced`: This is a `UTC_DATETIME` column that represents the start of sync
    - `_fivetran_deleted`: Fivetran does soft deletes. This column is used to indicate whether a given row is deleted at the source or not.
    - `_fivetran_id`: Fivetran supports primary-keyless source tables by adding a pseudo primary key column. Therefore, tables in batch files will always have a primary key.

### Compression
Batch files are compressed using [ZSTD](https://en.wikipedia.org/wiki/Zstd) and encrypted using [AES-256](https://en.wikipedia.org/wiki/Advanced_Encryption_Standard) in [CBC](https://en.wikipedia.org/wiki/Block_cipher_mode_of_operation) mode. It is possible to disable encryption and compression of batch files for debugging purposes by passing `--plain-text` CLI argument to the destination tester.

### Encryption
- Each file is encrypted separately. You can find the encryption keys in `WriteBatchRequest#keys` field.
- First 16 bytes of each batch file holds the IV vector.

### CreateTable RPC call:
This call should fail if it is asked to create a table that already exists.

### WriteBatchRequest
- `replace_files` is for `upsert` operation where the rows should be inserted if they don't exist or updated if they do. Each row will always provide values for all columns.
- `update_files` is for `update` operation where modified columns have actual values whereas unmodified columns have the special value `unmodified_string` in `CsvFileParams`. 
- `delete_files` is for `soft delete` operation. Only primary key columns should be used to set `_fivetran_deleted` column of the corresponding rows to `true` in the destination.
- `CsvFileParams`:
    - `null_string` value is used to represent `NULL` value in all batch files.
    - `unmodified_string` value is used to indicate columns in `update_files` where the values did not change.

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
    - NAIVE_DATE: Date without a timezone in ISO-8601 calendar system, e.g. 2007-12-03
    - NAIVE_DATETIME: A date-time without timezone in ISO-8601 calendar system, e.g. 2007-12-03T10:15:30.
    - UTC_DATETIME: An instantaneous point on the timeline, always in UTC timezone, e.g. 2007-12-03T10:15:30.123Z
    - BINARY: Binary data is represented as Google Protobuf [ByteString](https://protobuf.dev/reference/java/api-docs/com/google/protobuf/ByteString)
    - XML: "<tag>This is xml</tag>"
    - STRING: "This is text"
    - JSON: "{\"a\": 123}"

## Testing
The following are a list of test scenarios we recommend you consider:
- Test mapping of all data types between Fivetran data types and source/destination data types (e.g. [Mysql](https://fivetran.com/docs/databases/mysql#typetransformationsandmapping))
- Big data loads
- Big incremental updates
- Narrow event tables
- Wide fact tables
