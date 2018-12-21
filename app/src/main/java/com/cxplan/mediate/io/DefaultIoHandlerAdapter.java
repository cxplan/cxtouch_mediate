package com.cxplan.mediate.io;

import android.util.Log;

import com.cxplan.common.util.LogUtil;
import com.cxplan.mediate.CXApplication;
import com.cxplan.mediate.Constant;
import com.cxplan.mediate.message.Message;
import com.cxplan.mediate.message.MessageException;
import com.cxplan.mediate.message.MessageUtil;
import com.cxplan.mediate.message.handler.CommandHandlerFactory;
import com.cxplan.mediate.message.handler.ICommandHandler;
import com.cxplan.mediate.model.DeviceInfo;
import com.cxplan.mediate.util.NetUtil;
import com.cxplan.mediate.util.WindowManagerUtil;

import org.apache.mina.core.service.IoHandlerAdapter;
import org.apache.mina.core.session.IoSession;

import java.net.SocketException;

/**
 * Created on 2017/4/20.
 *
 * @author kenny
 */
public class DefaultIoHandlerAdapter extends IoHandlerAdapter {

    private static final String TAG = Constant.TAG_PREFIX + "IoHandlerAdapter";
    public static final String CLIENT_SESSION = "session";

    @Override
    public void sessionCreated(IoSession session) {
    }

    @Override
    public void messageReceived(IoSession session, Object message) throws Exception {

        if (!(message instanceof Message)) {
            throw new MessageException("The message format is illegal: " + message.getClass());
        }

        ClientConnection connection = (ClientConnection) session.getAttribute(CLIENT_SESSION);
        Message msg = (Message) message;
        if (!msg.getCommand().equals(MessageUtil.CMD_DEVICE_MONKEY)) {
            if (msg.getCommand().equals(MessageUtil.CMD_PING)) {
                LogUtil.v(TAG, "received ping command");
            } else {
                LogUtil.i(TAG, "received command: " + msg.getCommand());
            }
        }

        if (MessageUtil.CMD_DEVICE_CREATE_SESSION.equals(msg.getCommand())) {
            preparePhone(session, msg);
            return;
        }

        //1. Then span message collector.
        if (connection == null && !msg.getCommand().equals(MessageUtil.CMD_DEVICE_INIT_SESSION)) {
            LogUtil.e("The session is not initialized, but received a message:" + msg.getCommand());
            return;
        }
        // Loop through all collectors and notify the appropriate ones.
        if (connection != null) {
            boolean ret = false;
            for (MessageCollector collector : connection.getPacketCollectors()) {
                try {
                    if (collector.processMessage(msg)) {
                        ret = true;
                    }
                } catch (Exception e) {
                    processException(e, msg, session);
                    return;
                }
            }
            if (ret) {
                return;
            }
        }

        //unresponsive command has some errors.
        if (msg.getError() != null) {
            LogUtil.e("Executing command failed: cmd=" + msg.getCommand() + ",id=" + msg.getId()
                    + ",error=" + msg.getError());
            return;
        }

        //2 phone server need do something.
        ICommandHandler handler = CommandHandlerFactory.getHandler(msg.getCommand());
        if (handler == null) {
            throw new RuntimeException("The handler is missing: " + msg.getCommand());
        }
        try {
            handler.process(session, msg);
        } catch (Exception e) {
            processException(e, msg, session);
        }

    }

    protected void processException(Exception e, Message msg, IoSession session) throws Exception {
        LogUtil.e(e.getMessage(), e);
        Message ret = Message.createResultMessage(msg);
        if (e instanceof MessageException) {
            ret.setError(e.getMessage());
            MessageUtil.sendMessage(session, ret);

            return;
        } else {
            ret.setError(e.getMessage());
            MessageUtil.sendMessage(session, ret);
            throw e;
        }
    }

    @Override
    public void exceptionCaught(IoSession session, Throwable cause) throws Exception {
        LogUtil.e(TAG, cause.getMessage(), cause);
    }

    @Override
    public void sessionClosed(IoSession session) throws Exception {
        ClientConnection connection = (ClientConnection)session.getAttribute(CLIENT_SESSION);
        if (connection != null) {
            connection.close();
        }
    }

    private void preparePhone(IoSession session, Message msg) {
        Integer imageQuality = msg.getParameter("iq");
        Float zoomRate = msg.getParameter("zr");
        if (imageQuality != null) {
            CXApplication.getInstance().setImageQuality(imageQuality);
        }
        if (zoomRate != null) {
            CXApplication.getInstance().getDeviceInfo().setZoomRate(zoomRate);
        }

        short rotation = 0;
        try {
            rotation = (short) WindowManagerUtil.getRotation();
        } catch (Exception e) {
            LogUtil.e(TAG, e.getMessage(), e);
        }

        DeviceInfo phoneInfo = CXApplication.getInstance().getDeviceInfo();
        Message message = Message.createResultMessage(msg);
        message.setParameter("id", phoneInfo.getId());
        message.setParameter("ro", rotation);
        String ip = null;
        try {
            ip = NetUtil.getLocalIp();
        } catch (SocketException e) {
            LogUtil.e(TAG, "Retrieving IP failed:" + e.getMessage(), e);
            return;
        }
        message.setParameter("host", ip);
        message.setParameter("port", phoneInfo.getVideoPort());
        message.setParameter("phone", phoneInfo.getPhone());
        message.setParameter("sw", phoneInfo.getScreenWidth());
        message.setParameter("sh", phoneInfo.getScreenHeight());

        //device information
        message.setParameter("mediateVersion", "1.0");
        message.setParameter("mediateVersionCode", 1);
        try {
            MessageUtil.sendMessage(session, message);
        } catch (MessageException e) {
            LogUtil.e(TAG, e.getMessage(), e);
            return;
        }

        ControllerConnection connection = CXApplication.getInstance().getControllerConnection();
        connection.setHost(session.getRemoteAddress().toString());

        session.setAttribute(CLIENT_SESSION, connection);
        connection.setSession(session);

        Log.i(TAG, "Send ip and port successfully:ip=" + ip + ",port=" + phoneInfo.getVideoPort());
    }
}
