package connector;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

public final class LogEntry {

    private final Level level;
    private final String message;

    @JsonProperty(value = "message-origin")
    private final MessageOrigin messageOrigin;

    public LogEntry(Level level, String message, MessageOrigin messageOrigin) {
        this.level = level;
        this.message = message;
        this.messageOrigin = messageOrigin;
    }

    public Level getLevel() {
        return level;
    }

    public String getMessage() {
        return message;
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

    enum Level {
        INFO, WARNING, SEVERE
    }

    enum MessageOrigin {
        @JsonProperty(value = "sdk_connector") SDK_CONNECTOR, @JsonProperty(value = "sdk_destination") SDK_DESTINATION
    }
}
