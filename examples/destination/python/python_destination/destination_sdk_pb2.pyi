from google.protobuf import timestamp_pb2 as _timestamp_pb2
import common_pb2 as _common_pb2
from google.protobuf.internal import containers as _containers
from google.protobuf.internal import enum_type_wrapper as _enum_type_wrapper
from google.protobuf import descriptor as _descriptor
from google.protobuf import message as _message
from typing import ClassVar as _ClassVar, Iterable as _Iterable, Mapping as _Mapping, Optional as _Optional, Union as _Union

DESCRIPTOR: _descriptor.FileDescriptor

class Encryption(int, metaclass=_enum_type_wrapper.EnumTypeWrapper):
    __slots__ = []
    NONE: _ClassVar[Encryption]
    AES: _ClassVar[Encryption]

class Compression(int, metaclass=_enum_type_wrapper.EnumTypeWrapper):
    __slots__ = []
    OFF: _ClassVar[Compression]
    ZSTD: _ClassVar[Compression]
    GZIP: _ClassVar[Compression]
NONE: Encryption
AES: Encryption
OFF: Compression
ZSTD: Compression
GZIP: Compression

class DescribeTableRequest(_message.Message):
    __slots__ = ["configuration", "schema_name", "table_name"]
    class ConfigurationEntry(_message.Message):
        __slots__ = ["key", "value"]
        KEY_FIELD_NUMBER: _ClassVar[int]
        VALUE_FIELD_NUMBER: _ClassVar[int]
        key: str
        value: str
        def __init__(self, key: _Optional[str] = ..., value: _Optional[str] = ...) -> None: ...
    CONFIGURATION_FIELD_NUMBER: _ClassVar[int]
    SCHEMA_NAME_FIELD_NUMBER: _ClassVar[int]
    TABLE_NAME_FIELD_NUMBER: _ClassVar[int]
    configuration: _containers.ScalarMap[str, str]
    schema_name: str
    table_name: str
    def __init__(self, configuration: _Optional[_Mapping[str, str]] = ..., schema_name: _Optional[str] = ..., table_name: _Optional[str] = ...) -> None: ...

class DescribeTableResponse(_message.Message):
    __slots__ = ["not_found", "failure", "table"]
    NOT_FOUND_FIELD_NUMBER: _ClassVar[int]
    FAILURE_FIELD_NUMBER: _ClassVar[int]
    TABLE_FIELD_NUMBER: _ClassVar[int]
    not_found: bool
    failure: str
    table: _common_pb2.Table
    def __init__(self, not_found: bool = ..., failure: _Optional[str] = ..., table: _Optional[_Union[_common_pb2.Table, _Mapping]] = ...) -> None: ...

class CreateTableRequest(_message.Message):
    __slots__ = ["configuration", "schema_name", "table"]
    class ConfigurationEntry(_message.Message):
        __slots__ = ["key", "value"]
        KEY_FIELD_NUMBER: _ClassVar[int]
        VALUE_FIELD_NUMBER: _ClassVar[int]
        key: str
        value: str
        def __init__(self, key: _Optional[str] = ..., value: _Optional[str] = ...) -> None: ...
    CONFIGURATION_FIELD_NUMBER: _ClassVar[int]
    SCHEMA_NAME_FIELD_NUMBER: _ClassVar[int]
    TABLE_FIELD_NUMBER: _ClassVar[int]
    configuration: _containers.ScalarMap[str, str]
    schema_name: str
    table: _common_pb2.Table
    def __init__(self, configuration: _Optional[_Mapping[str, str]] = ..., schema_name: _Optional[str] = ..., table: _Optional[_Union[_common_pb2.Table, _Mapping]] = ...) -> None: ...

class CreateTableResponse(_message.Message):
    __slots__ = ["success", "failure"]
    SUCCESS_FIELD_NUMBER: _ClassVar[int]
    FAILURE_FIELD_NUMBER: _ClassVar[int]
    success: bool
    failure: str
    def __init__(self, success: bool = ..., failure: _Optional[str] = ...) -> None: ...

class AlterTableRequest(_message.Message):
    __slots__ = ["configuration", "schema_name", "table"]
    class ConfigurationEntry(_message.Message):
        __slots__ = ["key", "value"]
        KEY_FIELD_NUMBER: _ClassVar[int]
        VALUE_FIELD_NUMBER: _ClassVar[int]
        key: str
        value: str
        def __init__(self, key: _Optional[str] = ..., value: _Optional[str] = ...) -> None: ...
    CONFIGURATION_FIELD_NUMBER: _ClassVar[int]
    SCHEMA_NAME_FIELD_NUMBER: _ClassVar[int]
    TABLE_FIELD_NUMBER: _ClassVar[int]
    configuration: _containers.ScalarMap[str, str]
    schema_name: str
    table: _common_pb2.Table
    def __init__(self, configuration: _Optional[_Mapping[str, str]] = ..., schema_name: _Optional[str] = ..., table: _Optional[_Union[_common_pb2.Table, _Mapping]] = ...) -> None: ...

class AlterTableResponse(_message.Message):
    __slots__ = ["success", "failure"]
    SUCCESS_FIELD_NUMBER: _ClassVar[int]
    FAILURE_FIELD_NUMBER: _ClassVar[int]
    success: bool
    failure: str
    def __init__(self, success: bool = ..., failure: _Optional[str] = ...) -> None: ...

class TruncateRequest(_message.Message):
    __slots__ = ["configuration", "schema_name", "table_name", "synced_column", "utc_delete_before", "soft"]
    class ConfigurationEntry(_message.Message):
        __slots__ = ["key", "value"]
        KEY_FIELD_NUMBER: _ClassVar[int]
        VALUE_FIELD_NUMBER: _ClassVar[int]
        key: str
        value: str
        def __init__(self, key: _Optional[str] = ..., value: _Optional[str] = ...) -> None: ...
    CONFIGURATION_FIELD_NUMBER: _ClassVar[int]
    SCHEMA_NAME_FIELD_NUMBER: _ClassVar[int]
    TABLE_NAME_FIELD_NUMBER: _ClassVar[int]
    SYNCED_COLUMN_FIELD_NUMBER: _ClassVar[int]
    UTC_DELETE_BEFORE_FIELD_NUMBER: _ClassVar[int]
    SOFT_FIELD_NUMBER: _ClassVar[int]
    configuration: _containers.ScalarMap[str, str]
    schema_name: str
    table_name: str
    synced_column: str
    utc_delete_before: _timestamp_pb2.Timestamp
    soft: SoftTruncate
    def __init__(self, configuration: _Optional[_Mapping[str, str]] = ..., schema_name: _Optional[str] = ..., table_name: _Optional[str] = ..., synced_column: _Optional[str] = ..., utc_delete_before: _Optional[_Union[_timestamp_pb2.Timestamp, _Mapping]] = ..., soft: _Optional[_Union[SoftTruncate, _Mapping]] = ...) -> None: ...

class SoftTruncate(_message.Message):
    __slots__ = ["deleted_column"]
    DELETED_COLUMN_FIELD_NUMBER: _ClassVar[int]
    deleted_column: str
    def __init__(self, deleted_column: _Optional[str] = ...) -> None: ...

class TruncateResponse(_message.Message):
    __slots__ = ["success", "failure"]
    SUCCESS_FIELD_NUMBER: _ClassVar[int]
    FAILURE_FIELD_NUMBER: _ClassVar[int]
    success: bool
    failure: str
    def __init__(self, success: bool = ..., failure: _Optional[str] = ...) -> None: ...

class WriteBatchRequest(_message.Message):
    __slots__ = ["configuration", "schema_name", "table", "keys", "replace_files", "update_files", "delete_files", "csv"]
    class ConfigurationEntry(_message.Message):
        __slots__ = ["key", "value"]
        KEY_FIELD_NUMBER: _ClassVar[int]
        VALUE_FIELD_NUMBER: _ClassVar[int]
        key: str
        value: str
        def __init__(self, key: _Optional[str] = ..., value: _Optional[str] = ...) -> None: ...
    class KeysEntry(_message.Message):
        __slots__ = ["key", "value"]
        KEY_FIELD_NUMBER: _ClassVar[int]
        VALUE_FIELD_NUMBER: _ClassVar[int]
        key: str
        value: bytes
        def __init__(self, key: _Optional[str] = ..., value: _Optional[bytes] = ...) -> None: ...
    CONFIGURATION_FIELD_NUMBER: _ClassVar[int]
    SCHEMA_NAME_FIELD_NUMBER: _ClassVar[int]
    TABLE_FIELD_NUMBER: _ClassVar[int]
    KEYS_FIELD_NUMBER: _ClassVar[int]
    REPLACE_FILES_FIELD_NUMBER: _ClassVar[int]
    UPDATE_FILES_FIELD_NUMBER: _ClassVar[int]
    DELETE_FILES_FIELD_NUMBER: _ClassVar[int]
    CSV_FIELD_NUMBER: _ClassVar[int]
    configuration: _containers.ScalarMap[str, str]
    schema_name: str
    table: _common_pb2.Table
    keys: _containers.ScalarMap[str, bytes]
    replace_files: _containers.RepeatedScalarFieldContainer[str]
    update_files: _containers.RepeatedScalarFieldContainer[str]
    delete_files: _containers.RepeatedScalarFieldContainer[str]
    csv: CsvFileParams
    def __init__(self, configuration: _Optional[_Mapping[str, str]] = ..., schema_name: _Optional[str] = ..., table: _Optional[_Union[_common_pb2.Table, _Mapping]] = ..., keys: _Optional[_Mapping[str, bytes]] = ..., replace_files: _Optional[_Iterable[str]] = ..., update_files: _Optional[_Iterable[str]] = ..., delete_files: _Optional[_Iterable[str]] = ..., csv: _Optional[_Union[CsvFileParams, _Mapping]] = ...) -> None: ...

class CsvFileParams(_message.Message):
    __slots__ = ["compression", "encryption", "null_string", "unmodified_string"]
    COMPRESSION_FIELD_NUMBER: _ClassVar[int]
    ENCRYPTION_FIELD_NUMBER: _ClassVar[int]
    NULL_STRING_FIELD_NUMBER: _ClassVar[int]
    UNMODIFIED_STRING_FIELD_NUMBER: _ClassVar[int]
    compression: Compression
    encryption: Encryption
    null_string: str
    unmodified_string: str
    def __init__(self, compression: _Optional[_Union[Compression, str]] = ..., encryption: _Optional[_Union[Encryption, str]] = ..., null_string: _Optional[str] = ..., unmodified_string: _Optional[str] = ...) -> None: ...

class WriteBatchResponse(_message.Message):
    __slots__ = ["success", "failure"]
    SUCCESS_FIELD_NUMBER: _ClassVar[int]
    FAILURE_FIELD_NUMBER: _ClassVar[int]
    success: bool
    failure: str
    def __init__(self, success: bool = ..., failure: _Optional[str] = ...) -> None: ...
