package com.cxplan.mediate.inputer;

import com.cxplan.mediate.io.ClientConnection;
/**
 * The client connection for IME process.
 *
 * Created on 2018/12/19.
 * @author kenny
 */
public class IMEConnection extends ClientConnection {

    private CXTouchIME ime;
    public IMEConnection() {
        this(null);
    }
    public IMEConnection(CXTouchIME ime) {
        this.ime = ime;
    }

    @Override
    public void close() {
        super.close();
        if (ime != null) {
            ime.switchOtherIME();
            ime = null;
        }
    }
}
