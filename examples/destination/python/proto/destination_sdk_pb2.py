# -*- coding: utf-8 -*-
# Generated by the protocol buffer compiler.  DO NOT EDIT!
# source: destination_sdk.proto
# Protobuf Python Version: 4.25.3
"""Generated protocol buffer code."""
from google.protobuf import descriptor as _descriptor
from google.protobuf import descriptor_pool as _descriptor_pool
from google.protobuf import symbol_database as _symbol_database
from google.protobuf.internal import builder as _builder
# @@protoc_insertion_point(imports)

_sym_db = _symbol_database.Default()


from google.protobuf import timestamp_pb2 as google_dot_protobuf_dot_timestamp__pb2
import common_pb2 as common__pb2


DESCRIPTOR = _descriptor_pool.Default().AddSerializedFile(b'\n\x15\x64\x65stination_sdk.proto\x12\x0c\x66ivetran_sdk\x1a\x1fgoogle/protobuf/timestamp.proto\x1a\x0c\x63ommon.proto\"\xc3\x01\n\x14\x44\x65scribeTableRequest\x12L\n\rconfiguration\x18\x01 \x03(\x0b\x32\x35.fivetran_sdk.DescribeTableRequest.ConfigurationEntry\x12\x13\n\x0bschema_name\x18\x02 \x01(\t\x12\x12\n\ntable_name\x18\x03 \x01(\t\x1a\x34\n\x12\x43onfigurationEntry\x12\x0b\n\x03key\x18\x01 \x01(\t\x12\r\n\x05value\x18\x02 \x01(\t:\x02\x38\x01\"q\n\x15\x44\x65scribeTableResponse\x12\x13\n\tnot_found\x18\x01 \x01(\x08H\x00\x12\x11\n\x07\x66\x61ilure\x18\x02 \x01(\tH\x00\x12$\n\x05table\x18\x03 \x01(\x0b\x32\x13.fivetran_sdk.TableH\x00\x42\n\n\x08response\"\xcf\x01\n\x12\x43reateTableRequest\x12J\n\rconfiguration\x18\x01 \x03(\x0b\x32\x33.fivetran_sdk.CreateTableRequest.ConfigurationEntry\x12\x13\n\x0bschema_name\x18\x02 \x01(\t\x12\"\n\x05table\x18\x03 \x01(\x0b\x32\x13.fivetran_sdk.Table\x1a\x34\n\x12\x43onfigurationEntry\x12\x0b\n\x03key\x18\x01 \x01(\t\x12\r\n\x05value\x18\x02 \x01(\t:\x02\x38\x01\"G\n\x13\x43reateTableResponse\x12\x11\n\x07success\x18\x01 \x01(\x08H\x00\x12\x11\n\x07\x66\x61ilure\x18\x02 \x01(\tH\x00\x42\n\n\x08response\"\xcd\x01\n\x11\x41lterTableRequest\x12I\n\rconfiguration\x18\x01 \x03(\x0b\x32\x32.fivetran_sdk.AlterTableRequest.ConfigurationEntry\x12\x13\n\x0bschema_name\x18\x02 \x01(\t\x12\"\n\x05table\x18\x03 \x01(\x0b\x32\x13.fivetran_sdk.Table\x1a\x34\n\x12\x43onfigurationEntry\x12\x0b\n\x03key\x18\x01 \x01(\t\x12\r\n\x05value\x18\x02 \x01(\t:\x02\x38\x01\"F\n\x12\x41lterTableResponse\x12\x11\n\x07success\x18\x01 \x01(\x08H\x00\x12\x11\n\x07\x66\x61ilure\x18\x02 \x01(\tH\x00\x42\n\n\x08response\"\xbf\x02\n\x0fTruncateRequest\x12G\n\rconfiguration\x18\x01 \x03(\x0b\x32\x30.fivetran_sdk.TruncateRequest.ConfigurationEntry\x12\x13\n\x0bschema_name\x18\x02 \x01(\t\x12\x12\n\ntable_name\x18\x03 \x01(\t\x12\x15\n\rsynced_column\x18\x04 \x01(\t\x12\x35\n\x11utc_delete_before\x18\x05 \x01(\x0b\x32\x1a.google.protobuf.Timestamp\x12-\n\x04soft\x18\x06 \x01(\x0b\x32\x1a.fivetran_sdk.SoftTruncateH\x00\x88\x01\x01\x1a\x34\n\x12\x43onfigurationEntry\x12\x0b\n\x03key\x18\x01 \x01(\t\x12\r\n\x05value\x18\x02 \x01(\t:\x02\x38\x01\x42\x07\n\x05_soft\"&\n\x0cSoftTruncate\x12\x16\n\x0e\x64\x65leted_column\x18\x03 \x01(\t\"D\n\x10TruncateResponse\x12\x11\n\x07success\x18\x01 \x01(\x08H\x00\x12\x11\n\x07\x66\x61ilure\x18\x02 \x01(\tH\x00\x42\n\n\x08response\"\xb1\x03\n\x11WriteBatchRequest\x12I\n\rconfiguration\x18\x01 \x03(\x0b\x32\x32.fivetran_sdk.WriteBatchRequest.ConfigurationEntry\x12\x13\n\x0bschema_name\x18\x02 \x01(\t\x12\"\n\x05table\x18\x03 \x01(\x0b\x32\x13.fivetran_sdk.Table\x12\x37\n\x04keys\x18\x04 \x03(\x0b\x32).fivetran_sdk.WriteBatchRequest.KeysEntry\x12\x15\n\rreplace_files\x18\x05 \x03(\t\x12\x14\n\x0cupdate_files\x18\x06 \x03(\t\x12\x14\n\x0c\x64\x65lete_files\x18\x07 \x03(\t\x12*\n\x03\x63sv\x18\x08 \x01(\x0b\x32\x1b.fivetran_sdk.CsvFileParamsH\x00\x1a\x34\n\x12\x43onfigurationEntry\x12\x0b\n\x03key\x18\x01 \x01(\t\x12\r\n\x05value\x18\x02 \x01(\t:\x02\x38\x01\x1a+\n\tKeysEntry\x12\x0b\n\x03key\x18\x01 \x01(\t\x12\r\n\x05value\x18\x02 \x01(\x0c:\x02\x38\x01\x42\r\n\x0b\x66ile_params\"\x9d\x01\n\rCsvFileParams\x12.\n\x0b\x63ompression\x18\x01 \x01(\x0e\x32\x19.fivetran_sdk.Compression\x12,\n\nencryption\x18\x02 \x01(\x0e\x32\x18.fivetran_sdk.Encryption\x12\x13\n\x0bnull_string\x18\x03 \x01(\t\x12\x19\n\x11unmodified_string\x18\x04 \x01(\t\"F\n\x12WriteBatchResponse\x12\x11\n\x07success\x18\x01 \x01(\x08H\x00\x12\x11\n\x07\x66\x61ilure\x18\x02 \x01(\tH\x00\x42\n\n\x08response*\x1f\n\nEncryption\x12\x08\n\x04NONE\x10\x00\x12\x07\n\x03\x41\x45S\x10\x01**\n\x0b\x43ompression\x12\x07\n\x03OFF\x10\x00\x12\x08\n\x04ZSTD\x10\x01\x12\x08\n\x04GZIP\x10\x02\x32\xdb\x04\n\x0b\x44\x65stination\x12\x66\n\x11\x43onfigurationForm\x12&.fivetran_sdk.ConfigurationFormRequest\x1a\'.fivetran_sdk.ConfigurationFormResponse\"\x00\x12?\n\x04Test\x12\x19.fivetran_sdk.TestRequest\x1a\x1a.fivetran_sdk.TestResponse\"\x00\x12Z\n\rDescribeTable\x12\".fivetran_sdk.DescribeTableRequest\x1a#.fivetran_sdk.DescribeTableResponse\"\x00\x12T\n\x0b\x43reateTable\x12 .fivetran_sdk.CreateTableRequest\x1a!.fivetran_sdk.CreateTableResponse\"\x00\x12Q\n\nAlterTable\x12\x1f.fivetran_sdk.AlterTableRequest\x1a .fivetran_sdk.AlterTableResponse\"\x00\x12K\n\x08Truncate\x12\x1d.fivetran_sdk.TruncateRequest\x1a\x1e.fivetran_sdk.TruncateResponse\"\x00\x12Q\n\nWriteBatch\x12\x1f.fivetran_sdk.WriteBatchRequest\x1a .fivetran_sdk.WriteBatchResponse\"\x00\x42\x1fH\x01P\x01Z\x19\x66ivetran.com/fivetran_sdkb\x06proto3')

_globals = globals()
_builder.BuildMessageAndEnumDescriptors(DESCRIPTOR, _globals)
_builder.BuildTopDescriptorsAndMessages(DESCRIPTOR, 'destination_sdk_pb2', _globals)
if _descriptor._USE_C_DESCRIPTORS == False:
  _globals['DESCRIPTOR']._options = None
  _globals['DESCRIPTOR']._serialized_options = b'H\001P\001Z\031fivetran.com/fivetran_sdk'
  _globals['_DESCRIBETABLEREQUEST_CONFIGURATIONENTRY']._options = None
  _globals['_DESCRIBETABLEREQUEST_CONFIGURATIONENTRY']._serialized_options = b'8\001'
  _globals['_CREATETABLEREQUEST_CONFIGURATIONENTRY']._options = None
  _globals['_CREATETABLEREQUEST_CONFIGURATIONENTRY']._serialized_options = b'8\001'
  _globals['_ALTERTABLEREQUEST_CONFIGURATIONENTRY']._options = None
  _globals['_ALTERTABLEREQUEST_CONFIGURATIONENTRY']._serialized_options = b'8\001'
  _globals['_TRUNCATEREQUEST_CONFIGURATIONENTRY']._options = None
  _globals['_TRUNCATEREQUEST_CONFIGURATIONENTRY']._serialized_options = b'8\001'
  _globals['_WRITEBATCHREQUEST_CONFIGURATIONENTRY']._options = None
  _globals['_WRITEBATCHREQUEST_CONFIGURATIONENTRY']._serialized_options = b'8\001'
  _globals['_WRITEBATCHREQUEST_KEYSENTRY']._options = None
  _globals['_WRITEBATCHREQUEST_KEYSENTRY']._serialized_options = b'8\001'
  _globals['_ENCRYPTION']._serialized_start=2062
  _globals['_ENCRYPTION']._serialized_end=2093
  _globals['_COMPRESSION']._serialized_start=2095
  _globals['_COMPRESSION']._serialized_end=2137
  _globals['_DESCRIBETABLEREQUEST']._serialized_start=87
  _globals['_DESCRIBETABLEREQUEST']._serialized_end=282
  _globals['_DESCRIBETABLEREQUEST_CONFIGURATIONENTRY']._serialized_start=230
  _globals['_DESCRIBETABLEREQUEST_CONFIGURATIONENTRY']._serialized_end=282
  _globals['_DESCRIBETABLERESPONSE']._serialized_start=284
  _globals['_DESCRIBETABLERESPONSE']._serialized_end=397
  _globals['_CREATETABLEREQUEST']._serialized_start=400
  _globals['_CREATETABLEREQUEST']._serialized_end=607
  _globals['_CREATETABLEREQUEST_CONFIGURATIONENTRY']._serialized_start=230
  _globals['_CREATETABLEREQUEST_CONFIGURATIONENTRY']._serialized_end=282
  _globals['_CREATETABLERESPONSE']._serialized_start=609
  _globals['_CREATETABLERESPONSE']._serialized_end=680
  _globals['_ALTERTABLEREQUEST']._serialized_start=683
  _globals['_ALTERTABLEREQUEST']._serialized_end=888
  _globals['_ALTERTABLEREQUEST_CONFIGURATIONENTRY']._serialized_start=230
  _globals['_ALTERTABLEREQUEST_CONFIGURATIONENTRY']._serialized_end=282
  _globals['_ALTERTABLERESPONSE']._serialized_start=890
  _globals['_ALTERTABLERESPONSE']._serialized_end=960
  _globals['_TRUNCATEREQUEST']._serialized_start=963
  _globals['_TRUNCATEREQUEST']._serialized_end=1282
  _globals['_TRUNCATEREQUEST_CONFIGURATIONENTRY']._serialized_start=230
  _globals['_TRUNCATEREQUEST_CONFIGURATIONENTRY']._serialized_end=282
  _globals['_SOFTTRUNCATE']._serialized_start=1284
  _globals['_SOFTTRUNCATE']._serialized_end=1322
  _globals['_TRUNCATERESPONSE']._serialized_start=1324
  _globals['_TRUNCATERESPONSE']._serialized_end=1392
  _globals['_WRITEBATCHREQUEST']._serialized_start=1395
  _globals['_WRITEBATCHREQUEST']._serialized_end=1828
  _globals['_WRITEBATCHREQUEST_CONFIGURATIONENTRY']._serialized_start=230
  _globals['_WRITEBATCHREQUEST_CONFIGURATIONENTRY']._serialized_end=282
  _globals['_WRITEBATCHREQUEST_KEYSENTRY']._serialized_start=1770
  _globals['_WRITEBATCHREQUEST_KEYSENTRY']._serialized_end=1813
  _globals['_CSVFILEPARAMS']._serialized_start=1831
  _globals['_CSVFILEPARAMS']._serialized_end=1988
  _globals['_WRITEBATCHRESPONSE']._serialized_start=1990
  _globals['_WRITEBATCHRESPONSE']._serialized_end=2060
  _globals['_DESTINATION']._serialized_start=2140
  _globals['_DESTINATION']._serialized_end=2743
# @@protoc_insertion_point(module_scope)
