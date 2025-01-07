import common_pb2 as _common_pb2
from google.protobuf.internal import containers as _containers
from google.protobuf.internal import enum_type_wrapper as _enum_type_wrapper
from google.protobuf import descriptor as _descriptor
from google.protobuf import message as _message
from typing import ClassVar as _ClassVar, Iterable as _Iterable, Mapping as _Mapping, Optional as _Optional, Union as _Union

DESCRIPTOR: _descriptor.FileDescriptor

class LogLevel(int, metaclass=_enum_type_wrapper.EnumTypeWrapper):
    __slots__ = ()
    INFO: _ClassVar[LogLevel]
    WARNING: _ClassVar[LogLevel]
    SEVERE: _ClassVar[LogLevel]
INFO: LogLevel
WARNING: LogLevel
SEVERE: LogLevel

class SchemaRequest(_message.Message):
    __slots__ = ("configuration",)
    class ConfigurationEntry(_message.Message):
        __slots__ = ("key", "value")
        KEY_FIELD_NUMBER: _ClassVar[int]
        VALUE_FIELD_NUMBER: _ClassVar[int]
        key: str
        value: str
        def __init__(self, key: _Optional[str] = ..., value: _Optional[str] = ...) -> None: ...
    CONFIGURATION_FIELD_NUMBER: _ClassVar[int]
    configuration: _containers.ScalarMap[str, str]
    def __init__(self, configuration: _Optional[_Mapping[str, str]] = ...) -> None: ...

class SchemaResponse(_message.Message):
    __slots__ = ("schema_response_not_supported", "with_schema", "without_schema", "selection_not_supported")
    SCHEMA_RESPONSE_NOT_SUPPORTED_FIELD_NUMBER: _ClassVar[int]
    WITH_SCHEMA_FIELD_NUMBER: _ClassVar[int]
    WITHOUT_SCHEMA_FIELD_NUMBER: _ClassVar[int]
    SELECTION_NOT_SUPPORTED_FIELD_NUMBER: _ClassVar[int]
    schema_response_not_supported: bool
    with_schema: _common_pb2.SchemaList
    without_schema: _common_pb2.TableList
    selection_not_supported: bool
    def __init__(self, schema_response_not_supported: bool = ..., with_schema: _Optional[_Union[_common_pb2.SchemaList, _Mapping]] = ..., without_schema: _Optional[_Union[_common_pb2.TableList, _Mapping]] = ..., selection_not_supported: bool = ...) -> None: ...

class UpdateRequest(_message.Message):
    __slots__ = ("configuration", "selection", "state_json")
    class ConfigurationEntry(_message.Message):
        __slots__ = ("key", "value")
        KEY_FIELD_NUMBER: _ClassVar[int]
        VALUE_FIELD_NUMBER: _ClassVar[int]
        key: str
        value: str
        def __init__(self, key: _Optional[str] = ..., value: _Optional[str] = ...) -> None: ...
    CONFIGURATION_FIELD_NUMBER: _ClassVar[int]
    SELECTION_FIELD_NUMBER: _ClassVar[int]
    STATE_JSON_FIELD_NUMBER: _ClassVar[int]
    configuration: _containers.ScalarMap[str, str]
    selection: Selection
    state_json: str
    def __init__(self, configuration: _Optional[_Mapping[str, str]] = ..., selection: _Optional[_Union[Selection, _Mapping]] = ..., state_json: _Optional[str] = ...) -> None: ...

class Selection(_message.Message):
    __slots__ = ("without_schema", "with_schema")
    WITHOUT_SCHEMA_FIELD_NUMBER: _ClassVar[int]
    WITH_SCHEMA_FIELD_NUMBER: _ClassVar[int]
    without_schema: TablesWithNoSchema
    with_schema: TablesWithSchema
    def __init__(self, without_schema: _Optional[_Union[TablesWithNoSchema, _Mapping]] = ..., with_schema: _Optional[_Union[TablesWithSchema, _Mapping]] = ...) -> None: ...

class TablesWithNoSchema(_message.Message):
    __slots__ = ("tables", "include_new_tables")
    TABLES_FIELD_NUMBER: _ClassVar[int]
    INCLUDE_NEW_TABLES_FIELD_NUMBER: _ClassVar[int]
    tables: _containers.RepeatedCompositeFieldContainer[TableSelection]
    include_new_tables: bool
    def __init__(self, tables: _Optional[_Iterable[_Union[TableSelection, _Mapping]]] = ..., include_new_tables: bool = ...) -> None: ...

class TablesWithSchema(_message.Message):
    __slots__ = ("schemas", "include_new_schemas")
    SCHEMAS_FIELD_NUMBER: _ClassVar[int]
    INCLUDE_NEW_SCHEMAS_FIELD_NUMBER: _ClassVar[int]
    schemas: _containers.RepeatedCompositeFieldContainer[SchemaSelection]
    include_new_schemas: bool
    def __init__(self, schemas: _Optional[_Iterable[_Union[SchemaSelection, _Mapping]]] = ..., include_new_schemas: bool = ...) -> None: ...

class SchemaSelection(_message.Message):
    __slots__ = ("included", "schema_name", "tables", "include_new_tables")
    INCLUDED_FIELD_NUMBER: _ClassVar[int]
    SCHEMA_NAME_FIELD_NUMBER: _ClassVar[int]
    TABLES_FIELD_NUMBER: _ClassVar[int]
    INCLUDE_NEW_TABLES_FIELD_NUMBER: _ClassVar[int]
    included: bool
    schema_name: str
    tables: _containers.RepeatedCompositeFieldContainer[TableSelection]
    include_new_tables: bool
    def __init__(self, included: bool = ..., schema_name: _Optional[str] = ..., tables: _Optional[_Iterable[_Union[TableSelection, _Mapping]]] = ..., include_new_tables: bool = ...) -> None: ...

class TableSelection(_message.Message):
    __slots__ = ("included", "table_name", "columns", "include_new_columns")
    class ColumnsEntry(_message.Message):
        __slots__ = ("key", "value")
        KEY_FIELD_NUMBER: _ClassVar[int]
        VALUE_FIELD_NUMBER: _ClassVar[int]
        key: str
        value: bool
        def __init__(self, key: _Optional[str] = ..., value: bool = ...) -> None: ...
    INCLUDED_FIELD_NUMBER: _ClassVar[int]
    TABLE_NAME_FIELD_NUMBER: _ClassVar[int]
    COLUMNS_FIELD_NUMBER: _ClassVar[int]
    INCLUDE_NEW_COLUMNS_FIELD_NUMBER: _ClassVar[int]
    included: bool
    table_name: str
    columns: _containers.ScalarMap[str, bool]
    include_new_columns: bool
    def __init__(self, included: bool = ..., table_name: _Optional[str] = ..., columns: _Optional[_Mapping[str, bool]] = ..., include_new_columns: bool = ...) -> None: ...

class UpdateResponse(_message.Message):
    __slots__ = ("log_entry", "operation")
    LOG_ENTRY_FIELD_NUMBER: _ClassVar[int]
    OPERATION_FIELD_NUMBER: _ClassVar[int]
    log_entry: LogEntry
    operation: Operation
    def __init__(self, log_entry: _Optional[_Union[LogEntry, _Mapping]] = ..., operation: _Optional[_Union[Operation, _Mapping]] = ...) -> None: ...

class LogEntry(_message.Message):
    __slots__ = ("level", "message")
    LEVEL_FIELD_NUMBER: _ClassVar[int]
    MESSAGE_FIELD_NUMBER: _ClassVar[int]
    level: LogLevel
    message: str
    def __init__(self, level: _Optional[_Union[LogLevel, str]] = ..., message: _Optional[str] = ...) -> None: ...

class Operation(_message.Message):
    __slots__ = ("record", "schema_change", "checkpoint")
    RECORD_FIELD_NUMBER: _ClassVar[int]
    SCHEMA_CHANGE_FIELD_NUMBER: _ClassVar[int]
    CHECKPOINT_FIELD_NUMBER: _ClassVar[int]
    record: Record
    schema_change: SchemaChange
    checkpoint: Checkpoint
    def __init__(self, record: _Optional[_Union[Record, _Mapping]] = ..., schema_change: _Optional[_Union[SchemaChange, _Mapping]] = ..., checkpoint: _Optional[_Union[Checkpoint, _Mapping]] = ...) -> None: ...

class SchemaChange(_message.Message):
    __slots__ = ("with_schema", "without_schema")
    WITH_SCHEMA_FIELD_NUMBER: _ClassVar[int]
    WITHOUT_SCHEMA_FIELD_NUMBER: _ClassVar[int]
    with_schema: _common_pb2.SchemaList
    without_schema: _common_pb2.TableList
    def __init__(self, with_schema: _Optional[_Union[_common_pb2.SchemaList, _Mapping]] = ..., without_schema: _Optional[_Union[_common_pb2.TableList, _Mapping]] = ...) -> None: ...

class Record(_message.Message):
    __slots__ = ("schema_name", "table_name", "type", "data")
    class DataEntry(_message.Message):
        __slots__ = ("key", "value")
        KEY_FIELD_NUMBER: _ClassVar[int]
        VALUE_FIELD_NUMBER: _ClassVar[int]
        key: str
        value: _common_pb2.ValueType
        def __init__(self, key: _Optional[str] = ..., value: _Optional[_Union[_common_pb2.ValueType, _Mapping]] = ...) -> None: ...
    SCHEMA_NAME_FIELD_NUMBER: _ClassVar[int]
    TABLE_NAME_FIELD_NUMBER: _ClassVar[int]
    TYPE_FIELD_NUMBER: _ClassVar[int]
    DATA_FIELD_NUMBER: _ClassVar[int]
    schema_name: str
    table_name: str
    type: _common_pb2.OpType
    data: _containers.MessageMap[str, _common_pb2.ValueType]
    def __init__(self, schema_name: _Optional[str] = ..., table_name: _Optional[str] = ..., type: _Optional[_Union[_common_pb2.OpType, str]] = ..., data: _Optional[_Mapping[str, _common_pb2.ValueType]] = ...) -> None: ...

class Checkpoint(_message.Message):
    __slots__ = ("state_json",)
    STATE_JSON_FIELD_NUMBER: _ClassVar[int]
    state_json: str
    def __init__(self, state_json: _Optional[str] = ...) -> None: ...
