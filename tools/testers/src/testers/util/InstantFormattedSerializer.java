package testers.util;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.datatype.jsr310.ser.InstantSerializerBase;
import java.time.Instant;
import java.time.format.DateTimeFormatter;

public class InstantFormattedSerializer extends InstantSerializerBase<Instant> {
    public InstantFormattedSerializer(DateTimeFormatter formatter) {
        super(Instant.class, Instant::toEpochMilli, Instant::getEpochSecond, Instant::getNano, formatter);
    }

    private InstantFormattedSerializer(
            InstantSerializerBase<Instant> base, Boolean useTimestamp, DateTimeFormatter formatter) {
        super(base, useTimestamp, formatter);
    }

    @Override
    protected InstantFormattedSerializer withFormat(
            Boolean useTimestamp, DateTimeFormatter formatter, JsonFormat.Shape shape) {
        return new InstantFormattedSerializer(this, useTimestamp, formatter);
    }
}
