package com.cxplan.mediate.inputer;

import com.cxplan.common.util.LogUtil;
import com.cxplan.mediate.Constant;
import com.cxplan.mediate.io.ClientConnection;
import com.cxplan.mediate.message.Message;
import com.cxplan.mediate.message.MessageException;
import com.cxplan.mediate.message.MessageUtil;
import com.cxplan.mediate.message.handler.CommandHandlerFactory;
import com.cxplan.mediate.message.handler.ICommandHandler;

import org.apache.mina.core.service.IoHandlerAdapter;
import org.apache.mina.core.session.IoSession;

/**
 * Created on 2017/4/20.
 *
 * @author kenny
 */
public class IMEIoHandlerAdapter extends IoHandlerAdapter {

    private static final String TAG = Constant.TAG_PREFIX + "IMEIoHandler";
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
        if (msg.getCommand().equals(MessageUtil.CMD_PING)) {
            LogUtil.v(TAG, "received ping command");
        } else {
            LogUtil.i(TAG, "received command: " + msg.getCommand());
        }

        if (MessageUtil.CMD_DEVICE_INIT_SESSION.equals(msg.getCommand())) {
            return;
        }

        //1. Then span message collector.
        if (connection == null) {
            LogUtil.e("The session is not initialized, but received a message:" + msg.getCommand());
            return;
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
        LogUtil.e(cause.getMessage(), cause);
    }

    @Override
    public void sessionClosed(IoSession session) throws Exception {
        ClientConnection connection = (ClientConnection)session.getAttribute(CLIENT_SESSION);
        if (connection != null) {
            connection.close();
        }
    }
}
