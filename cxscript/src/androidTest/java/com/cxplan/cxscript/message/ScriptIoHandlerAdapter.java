package com.cxplan.cxscript.message;

import com.cxplan.projection.mediate.Constant;
import com.cxplan.projection.mediate.io.ClientConnection;
import com.cxplan.projection.mediate.message.Message;
import com.cxplan.projection.mediate.message.MessageException;
import com.cxplan.projection.mediate.message.MessageUtil;
import com.cxplan.projection.mediate.message.handler.CommandHandlerFactory;
import com.cxplan.projection.mediate.message.handler.ICommandHandler;

import org.apache.mina.core.service.IoHandlerAdapter;
import org.apache.mina.core.session.IoSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created on 2017/4/20.
 *
 * @author kenny
 */
public class ScriptIoHandlerAdapter extends IoHandlerAdapter {

    private static final Logger logger = LoggerFactory.getLogger(ScriptIoHandlerAdapter.class);

    @Override
    public void sessionCreated(IoSession session) {
    }

    @Override
    public void messageReceived(IoSession session, Object message) throws Exception {

        if (!(message instanceof Message)) {
            throw new MessageException("The message format is illegal: " + message.getClass());
        }

        ClientConnection connection = (ClientConnection) session.getAttribute(Constant.CLIENT_SESSION);
        Message msg = (Message) message;
        if (msg.getCommand().equals(MessageUtil.CMD_PING)) {
            logger.debug("received ping command");
        } else {
            logger.info("received command: " + msg.getCommand());
        }

        //1. Then span message collector.
        if (connection != null) {
            // Loop through all collectors and notify the appropriate ones.
            boolean ret = connection.visitMessageCollectors(msg);
            if (ret) {
                return;
            }
        }

        //unresponsive command has some errors.
        if (msg.getError() != null) {
            logger.error("Executing command failed: cmd=" + msg.getCommand() + ",id=" + msg.getId()
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
        logger.error(e.getMessage(), e);
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
        logger.error(cause.getMessage(), cause);
    }

    @Override
    public void sessionClosed(IoSession session) throws Exception {
        ClientConnection connection = (ClientConnection)session.getAttribute(Constant.CLIENT_SESSION);
        if (connection != null) {
            connection.close();
        }
    }

}
