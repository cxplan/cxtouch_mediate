package com.cxplan.mediate.io;

/**
 * Created by kenny on 2017/5/24.
 */

public class ControllerConnection extends ClientConnection{

    private static final String TAG = "ControllerCon";

    private String host;

    public ControllerConnection() {
        this(null);
    }
    public ControllerConnection(String host) {
        this.host = host;
    }

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }
}
