package com.cxplan.mediate.inputer;

import android.content.Context;
import android.content.Intent;
import android.inputmethodservice.InputMethodService;
import android.os.IBinder;
import android.view.View;
import android.view.inputmethod.InputConnection;
import android.view.inputmethod.InputMethodManager;

import com.cxplan.common.util.LogUtil;
import com.cxplan.mediate.Constant;
import com.cxplan.mediate.R;
import com.cxplan.mediate.io.DSCodecFactory;
import com.cxplan.mediate.io.DefaultIoHandlerAdapter;
import com.cxplan.mediate.io.MessageServer;
import com.cxplan.mediate.message.Message;
import com.cxplan.mediate.message.MessageException;
import com.cxplan.mediate.message.MessageUtil;
import com.cxplan.mediate.message.handler.CommandHandlerFactory;

import org.apache.mina.core.future.ConnectFuture;
import org.apache.mina.core.future.ReadFuture;
import org.apache.mina.core.session.IoSession;
import org.apache.mina.filter.codec.ProtocolCodecFilter;
import org.apache.mina.transport.socket.nio.NioSocketConnector;

import java.net.InetSocketAddress;

public class CXTouchIME extends InputMethodService {
    private static final String TAG = Constant.TAG_PREFIX + "ime";
    private static final String ACTION_BIND = "bind";
    private static final String ACTION_UNBIND = "unbind";

    public static CXTouchIME instance = null;

    private IMEConnection connection;
    private boolean isConnecting = false;

    @Override
    public View onCreateInputView() {
        View mInputView = getLayoutInflater().inflate(R.layout.inputer_view, null);

        return mInputView;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        if (CommandHandlerFactory.getHandler(MessageUtil.CMD_DEVICE_INPUTER) == null) {
            InputerCommandHandler ich = new InputerCommandHandler();
            CommandHandlerFactory.addHandler(ich.getCommand(), ich);
        }
        connect2Server();
        instance = this;
    }

    public void onDestroy() {
        LogUtil.i(TAG, "onDestroy");
        if (connection != null) {
            connection.close();
        } else {
            switchOtherIME();
        }
        instance = null;
        super.onDestroy();
    }

    @Override
    public void onBindInput() {
        LogUtil.i(TAG, "onBind");
        sendAction(ACTION_BIND);
    }

    @Override
    public void onUnbindInput() {
        LogUtil.i(TAG, "onUnbind");
        sendAction(ACTION_UNBIND);
    }

    @Override
    public int onStartCommand(Intent paramIntent, int flags, int startId) {
        String text = paramIntent.getStringExtra("s");
        InputConnection inputConnection = getCurrentInputConnection();
        if (inputConnection != null) {
            inputConnection.commitText(text, 1);
        }
        return super.onStartCommand(paramIntent, flags, startId);
    }

    public void switchOtherIME() {
        if (getCurrentInputBinding() == null) {
            return;
        }
        LogUtil.i(TAG, "Switch other IME");
        InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        IBinder binder = getWindow().getWindow().getAttributes().token;
        if (binder != null) {
            boolean ret = inputMethodManager.switchToLastInputMethod(binder);
            if (ret) {
                return;
            }
        }
        //The selection right is transferred to user.
        inputMethodManager.showInputMethodPicker();
    }

    private void sendAction(String action) {
        if (connection == null || !connection.isConnected()) {
            connect2Server();
            return;
        }
        Message message = new Message(MessageUtil.CMD_DEVICE_IME);
        message.setParameter("action", action);
        try {
            connection.sendMessage(message);
        } catch (MessageException e) {
            LogUtil.e(TAG, e.getMessage(), e);
        }
    }

    private void connect2Server() {
        if (isConnecting) {
            LogUtil.i(TAG, "The connecting is in progress.");
            return;
        }
        if (connection != null) {
            if (connection.isConnected()) {
                LogUtil.i(TAG, "The ime connection is ok, it's not necessary to connect again!");
                return;
            }
            connection.close();
            connection = null;
        }

        isConnecting = true;

        Runnable task = new Runnable() {
            @Override
            public void run() {
                try {
                    NioSocketConnector connector = new NioSocketConnector();
                    connector.setHandler(new IMEIoHandlerAdapter());
                    connector.getFilterChain().addLast("codec", new ProtocolCodecFilter(new DSCodecFactory()));

                    ConnectFuture connFuture = connector.connect(new InetSocketAddress("localhost", MessageServer.messagePort));
                    connFuture.awaitUninterruptibly();

                    while (!connFuture.isDone() && !connFuture.isConnected()) {
                        try {
                            Thread.sleep(100);
                        } catch (InterruptedException e) {
                        }
                    }
                    if (!connFuture.isConnected()) {
                        connFuture.cancel();
                        String errorMsg = "Connecting to message server is timeout: forward port=" + MessageServer.messagePort;
                        LogUtil.e(TAG, errorMsg);
                        throw new RuntimeException(errorMsg);
                    }

                    IoSession messageSession = connFuture.getSession();
                    if (messageSession.isConnected()) {
                        LogUtil.i(TAG, "Connect to message server successfully!");
                    } else {
                        LogUtil.e(TAG, "Connecting to message server failed: port=" + MessageServer.messagePort);
                        return;
                    }

                    //initialize session.
                    messageSession.getConfig().setUseReadOperation(true);
                    boolean bind = getCurrentInputBinding() != null;
                    Message createMsg = new Message(MessageUtil.CMD_DEVICE_INIT_SESSION);
                    createMsg.setParameter("bind", bind);
                    try {
                        MessageUtil.sendMessage(messageSession, createMsg);
                    } catch (MessageException e) {
                        throw new RuntimeException(e.getMessage(), e);
                    }
                    messageSession.getConfig().setUseReadOperation(true);
                    ReadFuture rf = messageSession.read();
                    boolean result = rf.awaitUninterruptibly(2000);
                    Message msg = (Message) rf.getMessage();
                    if (msg != null) {
                        LogUtil.i(TAG, "Initialize session successfully!");
                        connection = new IMEConnection(CXTouchIME.this);
                        connection.setSession(messageSession);
                        messageSession.setAttribute(DefaultIoHandlerAdapter.CLIENT_SESSION, connection);
                    } else {
                        LogUtil.e(TAG, "Initializing session failed.");
                    }

                    messageSession.getConfig().setUseReadOperation(false);
                } finally {
                    isConnecting = false;
                    if (connection == null) {
                        LogUtil.i(TAG, "Connecting failed, switch other ime");
                        switchOtherIME();
                    }
                }
            }
        };
        Thread th = new Thread(task, "Connect Thread");
        th.start();
    }

}