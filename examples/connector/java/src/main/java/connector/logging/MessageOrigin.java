package connector.logging;

public enum MessageOrigin {
    SDK_CONNECTOR("sdk_connector"), SDK_DESTINATION("sdk_destination");

    private final String label;

    MessageOrigin(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }
}
