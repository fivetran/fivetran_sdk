# SDK Development Guide

Fivetran SDK uses [gRPC](https://grpc.io/) to talk to partner code. The partner side of the interface is always the server side. Fivetran implements the client side and initiates the requests.

## Language

At the moment, partner code should be developed in a language that can generate a statically linked binary executable.

## Command Line Args

The executable needs to accept a `--port` argument that takes an integer as port number to listen. It should listen on both IPV4 (i.e. 0.0.0.0) and IPV6 (i.e ::0), but if only one is possible, it should listen on IPV4.

## General Tips, Conventions, Guidelines

- Don't push anything other than source data to the destination. State will be saved to production DB.
- Don't forget to handle new schemas/tables/columns per the information and user choices in `UpdateRequest#selection`

## Security

- Do not log sensitive data. Ensure only necessary information is kept in logs, and never log any sensitive data. Such data may include credentials (passwords, tokens, keys, etc), customer data, payment information, or PII.
- Encrypt HTTP requests: Things like URLs, URL parameters, and query params are always encrypted for logging, and customer approval is needed to decrypt and examine them.

## Logging

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

## Connector Testing

Some recommended tests:
- Test mapping of all data types between Fivetran data types and source/destination data types (e.g. [Mysql](https://fivetran.com/docs/databases/mysql#typetransformationsandmapping))
- Big data loads
- Big incremental updates
- Narrow event tables
- Wide fact tables
