package com.cxplan.projection.mediate.io.image;

import android.net.LocalSocket;
import android.net.LocalSocketAddress;

import java.io.IOException;
/**
 * Created on 2018/5/19.
 *
 * @author kenny
 */
public class CXLocalSocket extends LocalSocket {

    private boolean isClosed = false;
    public CXLocalSocket() {
    }

    public CXLocalSocket(int sockType) {
        super(sockType);
    }

    @Override
    public void connect(LocalSocketAddress endpoint) throws IOException {
        super.connect(endpoint);
        isClosed = false;
    }

    @Override
    public void close() throws IOException {
        super.close();
        isClosed = true;
    }

    @Override
    public boolean isClosed() {
        return isClosed;
    }
}
