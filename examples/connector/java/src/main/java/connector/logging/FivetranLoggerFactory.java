package connector.logging;

public class FivetranLoggerFactory {

    public static FivetranLogger getFivetranLogger(Class<?> loggingClass, MessageOrigin messageOrigin) {
        return getFivetranLogger(loggingClass.getSimpleName(), messageOrigin);
    }

    public static FivetranLogger getFivetranLogger(String name, MessageOrigin messageOrigin) {
        return FivetranLogger.getLogger(name, messageOrigin);
    }

    private FivetranLoggerFactory() {
    }
}
