package com.latdata.collect.source;

public class NetcatSourceConfigurationConstants {

    /**
     * Hostname to bind to.
     */
    public static final String CONFIG_HOSTNAME = "bind";

    /**
     * Port to bind to.
     */
    public static final String CONFIG_PORT = "port";

    /**
     * Ack every event received with an "OK" back to the sender
     */
    public static final String CONFIG_ACKEVENT = "ack-every-event";

    /**
     * Maximum line length per event.
     */
    public static final String CONFIG_MAX_LINE_LENGTH = "max-line-length";
    public static final int DEFAULT_MAX_LINE_LENGTH = 512;

}
