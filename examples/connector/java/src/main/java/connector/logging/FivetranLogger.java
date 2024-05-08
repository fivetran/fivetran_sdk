package connector.logging;

import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.StreamHandler;

public class FivetranLogger extends Logger {

    private final MessageOrigin messageOrigin;

    public static FivetranLogger getLogger(String name, MessageOrigin messageOrigin) {
        final FivetranLogger fivetranLogger = new FivetranLogger(name, messageOrigin);
        fivetranLogger.setUseParentHandlers(false);
        fivetranLogger.addHandler(new StreamHandler(System.out, new FivetranLogFormatter()));

        return fivetranLogger;
    }

    protected FivetranLogger(String name, MessageOrigin messageOrigin) {
        super(name, null);
        this.messageOrigin = messageOrigin;
    }

    @Override
    public void info(String msg) {
        super.log(new FivetranLogRecord(Level.INFO, msg, messageOrigin));
    }

    @Override
    public void warning(String msg) {
        super.log(new FivetranLogRecord(Level.WARNING, msg, messageOrigin));
    }

    @Override
    public void severe(String msg) {
        super.log(new FivetranLogRecord(Level.SEVERE, msg, messageOrigin));
    }
}
