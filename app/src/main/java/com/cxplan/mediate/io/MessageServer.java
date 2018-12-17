package com.cxplan.mediate.io;

import com.cxplan.common.util.LogUtil;
import com.cxplan.mediate.CXApplication;
import com.cxplan.mediate.Constant;

import org.apache.mina.filter.codec.ProtocolCodecFilter;
import org.apache.mina.filter.executor.ExecutorFilter;
import org.apache.mina.transport.socket.nio.NioSocketAcceptor;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;

public class MessageServer {
    private static final String TAG = Constant.TAG_PREFIX + "mServer";

    private NioSocketAcceptor acceptor;
    private boolean isStarted = false;


    public MessageServer() {
    }

    public boolean isStarted() {
        return isStarted;
    }

    public void start() {
        if (acceptor != null) {
            acceptor.dispose();
        }
        acceptor = new NioSocketAcceptor();

        // Add two filters : a logger and a codec
        acceptor.getFilterChain().addLast( "codec", new ProtocolCodecFilter( new DSCodecFactory()));
        acceptor.getFilterChain().addLast("threadModel", new ExecutorFilter(CXApplication.getInstance().getThreadPool()));
        // Attach the business logic to the server
        acceptor.setHandler( new DefaultIoHandlerAdapter() );

        // Configure the buffer size and the idle time
        acceptor.getSessionConfig().setReadBufferSize( 5000 );

        InetAddress address;
        try {
            address = InetAddress.getLocalHost();
        } catch (IOException e) {
            LogUtil.e(TAG, "Retrieve local host failed: "+ e.getMessage(), e);
            return;
        }

        isStarted = false;
        int count = 0;
        int port = 2014;
        while (!isStarted) {
            count++;
            try {
                acceptor.bind(new InetSocketAddress(address, port));
                isStarted = true;
            } catch (Exception e) {
                try {
                    Thread.sleep(3000);
                } catch (InterruptedException e1) {
                }
            }
        }
        String message = "Message Server started [" + address.toString() + ":" + port + "]";
        System.out.println(message);
        LogUtil.e(TAG, message);
    }

}
