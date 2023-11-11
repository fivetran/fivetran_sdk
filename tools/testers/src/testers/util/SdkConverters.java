package testers.util;

import com.google.protobuf.ByteString;
import com.google.protobuf.Timestamp;
import fivetran_sdk.DataType;
import fivetran_sdk.ValueType;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.LinkedHashMap;
import java.util.Map;

public class SdkConverters {
    public static final Clock SYS_CLOCK = Clock.tick(Clock.systemUTC(), Duration.of(1, ChronoUnit.MILLIS));

    private static final int MILLIS_IN_NANOS = 1_000_000;

    public static ValueType objectToValueType(Object raw) {
        if (raw == null) {
            return ValueType.newBuilder().setNull(true).build();
        }

        Class<?> c = raw.getClass();
        if (c == Boolean.class) return ValueType.newBuilder().setBool((Boolean) raw).build();
        else if (c == Short.class) return ValueType.newBuilder().setShort((Short) raw).build();
        else if (c == Integer.class) return ValueType.newBuilder().setInt((Integer) raw).build();
        else if (c == Long.class) return ValueType.newBuilder().setLong((Long) raw).build();
        else if (c == Float.class) return ValueType.newBuilder().setFloat((Float) raw).build();
        else if (c == Double.class) return ValueType.newBuilder().setDouble((Double) raw).build();
        else if (c == java.math.BigDecimal.class || c == java.math.BigInteger.class)
            return ValueType.newBuilder().setDecimal(raw.toString()).build();
        else if (c == java.time.LocalDate.class) {
            Instant instant = ((LocalDate) raw).atStartOfDay().toInstant(ZoneOffset.UTC);
            return ValueType.newBuilder().setNaiveDate(instantToTimestamp(instant)).build();
        } else if (c == java.sql.Date.class) {
            Instant instant = LocalDate.parse(raw.toString()).atStartOfDay().toInstant(ZoneOffset.UTC);
            return ValueType.newBuilder().setNaiveDate(instantToTimestamp(instant)).build();
        } else if (c == java.time.LocalDateTime.class) {
            Instant instant = ((LocalDateTime) raw).toInstant(ZoneOffset.UTC);
            return ValueType.newBuilder().setNaiveDatetime(instantToTimestamp(instant)).build();
        } else if (c == java.sql.Timestamp.class) {
            Instant instant = ((java.sql.Timestamp) raw).toInstant();
            return ValueType.newBuilder().setNaiveDatetime(instantToTimestamp(instant)).build();
        } else if (c == java.time.OffsetDateTime.class) {
            Instant instant = ((OffsetDateTime) raw).toInstant();
            return ValueType.newBuilder().setUtcDatetime(instantToTimestamp(instant)).build();
        } else if (c == java.time.Instant.class)
            return ValueType.newBuilder().setUtcDatetime(instantToTimestamp((Instant) raw)).build();
        else if (ByteString.class.isAssignableFrom(c) || c == Byte[].class || c == byte[].class)
            return ValueType.newBuilder().setBinary(ByteString.copyFrom((byte[]) raw)).build();
        else if (c == String.class) return ValueType.newBuilder().setString(raw.toString()).build();
        else throw new RuntimeException("Unsupported data type: " + c.getName());
    }

    public static Timestamp instantToTimestamp(Instant instant) {
        // msec precision
        int nanos = instant.getNano();
        int nanosMsecPrecision = (nanos / MILLIS_IN_NANOS) * MILLIS_IN_NANOS;
        return Timestamp.newBuilder().setSeconds(instant.getEpochSecond()).setNanos(nanosMsecPrecision).build();
    }

    public static DataType valueTypeToDataType(ValueType valueType) {
        ValueType.InnerCase innerCase = valueType.getInnerCase();

        switch (innerCase) {
            case NULL:
                return DataType.UNSPECIFIED;
            case BOOL:
                return DataType.BOOLEAN;
            case SHORT:
                return DataType.SHORT;
            case INT:
                return DataType.INT;
            case LONG:
                return DataType.LONG;
            case FLOAT:
                return DataType.FLOAT;
            case DOUBLE:
                return DataType.DOUBLE;
            case NAIVE_DATE:
                return DataType.NAIVE_DATE;
            case NAIVE_DATETIME:
                return DataType.NAIVE_DATETIME;
            case UTC_DATETIME:
                return DataType.UTC_DATETIME;
            case DECIMAL:
                return DataType.DECIMAL;
            case STRING:
                return DataType.STRING;
            case JSON:
                return DataType.JSON;
            case BINARY:
                return DataType.BINARY;
            case XML:
                return DataType.XML;
            default:
                throw new RuntimeException("Unknown value type: " + innerCase);
        }
    }

    public static Map<String, String> valueTypeToString(Map<String, ValueType> incoming) {
        Map<String, String> row = new LinkedHashMap<>();

        for (Map.Entry<String, ValueType> entry : incoming.entrySet()) {
            String column = entry.getKey();
            ValueType value = entry.getValue();

            ValueType.InnerCase innerCase = value.getInnerCase();
            switch (innerCase) {
                case NULL:
                    row.put(column, "null");
                    break;
                case BOOL:
                    row.put(column, (value.getBool()) ? "true" : "false");
                    break;
                case SHORT:
                    row.put(column, String.valueOf(value.getShort()));
                    break;
                case INT:
                    row.put(column, String.valueOf(value.getInt()));
                    break;
                case LONG:
                    row.put(column, String.valueOf(value.getLong()));
                    break;
                case FLOAT:
                    row.put(column, String.valueOf(value.getFloat()));
                    break;
                case DOUBLE:
                    row.put(column, String.valueOf(value.getDouble()));
                    break;
                case NAIVE_DATE:
                    Timestamp tsDate = value.getNaiveDate();
                    LocalDate naiveDate =
                            LocalDate.ofInstant(
                                    Instant.ofEpochSecond(tsDate.getSeconds(), tsDate.getNanos()),
                                    ZoneId.from(ZoneOffset.UTC));
                    row.put(column, singleQuotes(naiveDate.toString()));
                    break;
                case NAIVE_DATETIME:
                    Timestamp tsDateTime = value.getNaiveDatetime();
                    LocalDateTime naiveDateTime =
                            LocalDateTime.ofEpochSecond(tsDateTime.getSeconds(), tsDateTime.getNanos(), ZoneOffset.UTC);
                    row.put(
                            column,
                            singleQuotes(naiveDateTime.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME).toString()));
                    break;
                case UTC_DATETIME:
                    Timestamp tsInstant = value.getUtcDatetime();
                    row.put(
                            column,
                            singleQuotes(
                                    Instant.ofEpochSecond(tsInstant.getSeconds(), tsInstant.getNanos()).toString()));
                    break;
                case DECIMAL:
                    row.put(column, value.getDecimal());
                    break;
                case STRING:
                    row.put(column, singleQuotes(value.getString().replace("'", "''")));
                    break;
                case JSON:
                    row.put(column, singleQuotes(value.getJson()));
                    break;
                case BINARY:
                    row.put(column, bytesToHex(value.getBinary().toByteArray()));
                    break;
                case XML:
                    row.put(column, singleQuotes(value.getXml()));
                    break;
                default:
                    throw new RuntimeException("Unknown value type: " + innerCase);
            }
        }

        return row;
    }

    private static String singleQuotes(String incoming) {
        return "'" + incoming + "'";
    }

    private static String bytesToHex(byte[] bytes) {
        StringBuilder builder = new StringBuilder();
        for (byte b : bytes) {
            builder.append(String.format("\\x%02x", b));
        }
        return singleQuotes(builder.toString()) + "::BLOB";
    }
}
