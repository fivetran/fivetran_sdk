package connector.logging;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.logging.Level;
import java.util.logging.LogRecord;

public final class FivetranLogRecord extends LogRecord {

    @JsonProperty(value = "message-origin")
    private final MessageOrigin messageOrigin;

    public FivetranLogRecord(Level level, String message, MessageOrigin messageOrigin) {
        super(level, message);
        this.messageOrigin = messageOrigin;
    }

    public MessageOrigin getMessageOrigin() {
        return messageOrigin;
    }

    @Override
    public String toString() {
        try {
            return new ObjectMapper().writeValueAsString(this);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }
}
