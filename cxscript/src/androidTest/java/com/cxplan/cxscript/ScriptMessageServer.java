package com.cxplan.cxscript;

import com.cxplan.common.util.LogUtil;
import com.cxplan.cxscript.message.ScriptIoHandlerAdapter;
import com.cxplan.projection.mediate.Constant;
import com.cxplan.projection.mediate.io.DSCodecFactory;
import com.cxplan.projection.mediate.util.NetUtil;

import org.apache.mina.filter.codec.ProtocolCodecFilter;
import org.apache.mina.transport.socket.nio.NioSocketAcceptor;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;

/**
 * Created on 2019/3/19.
 *
 * @author kenny
 */
public class ScriptMessageServer {
    private static String TAG = Constant.TAG_PREFIX + "scriptServer";

    private NioSocketAcceptor acceptor;
    private int port = 2020;
    private boolean isStarted = false;


    public ScriptMessageServer(int port) {
        if (port > 0) {
            this.port = port;
        }
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
        // Attach the business logic to the server
        acceptor.setHandler( new ScriptIoHandlerAdapter() );

        // Configure the buffer size and the idle time
        acceptor.getSessionConfig().setReadBufferSize( 5000 );
        acceptor.setReuseAddress(true);

        String ip;
        InetAddress address;
        try {
            address = InetAddress.getLocalHost();
            ip = NetUtil.getLocalIp();
        } catch (IOException e) {
            LogUtil.e(TAG, "Retrieve local host failed: "+ e.getMessage(), e);
            return;
        }

        int count = 0;
        while (!isStarted) {
            count++;
            try {
                acceptor.bind(new InetSocketAddress(address, port));
                acceptor.bind(new InetSocketAddress(ip, port));
                isStarted = true;
            } catch (Exception e) {
                LogUtil.e(TAG, "Retry message server: " + count, e);
                try {
                    Thread.sleep(3000);
                } catch (InterruptedException e1) {
                }
            }
        }
        String message = "CXScript Server started [" + address.toString() + ":" + port + "]";
        System.out.println(message);
        LogUtil.e(TAG, message);
    }

}
