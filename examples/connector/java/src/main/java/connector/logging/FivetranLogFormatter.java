package connector.logging;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import java.io.IOException;
import java.util.logging.LogRecord;
import java.util.logging.SimpleFormatter;

public class FivetranLogFormatter extends SimpleFormatter {

    private static final ObjectMapper OBJECT_MAPPER =
            new ObjectMapper().registerModule(new JavaTimeModule())
                    .registerModule(new SimpleModule().addSerializer(new FivetranLogRecordSerializer()));

    @Override
    public String format(LogRecord logRecord) {
        if (logRecord instanceof FivetranLogRecord) {
            try {
                return OBJECT_MAPPER.writeValueAsString(logRecord);
            } catch (JsonProcessingException e) {
                throw new RuntimeException(e);
            }
        }

        return super.format(logRecord);
    }

    private static final class FivetranLogRecordSerializer extends StdSerializer<FivetranLogRecord> {

        public FivetranLogRecordSerializer() {
            this(FivetranLogRecord.class);
        }

        protected FivetranLogRecordSerializer(Class<FivetranLogRecord> t) {
            super(t);
        }

        @Override
        public void serialize(FivetranLogRecord value, JsonGenerator gen, SerializerProvider provider) throws IOException {
            gen.writeStartObject();
            gen.writeStringField("level", value.getLevel().getName());
            gen.writeStringField("message", value.getMessage());
            gen.writeStringField("message-origin", value.getMessageOrigin().getLabel());
        }
    }
}
