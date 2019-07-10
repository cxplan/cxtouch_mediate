package com.cxplan.projection.mediate.script.message.handler;

import com.cxplan.common.util.LogUtil;
import com.cxplan.projection.mediate.Constant;
import com.cxplan.projection.mediate.message.Message;
import com.cxplan.projection.mediate.message.MessageException;
import com.cxplan.projection.mediate.message.handler.AbstractCommandHandler;
import com.cxplan.projection.mediate.script.ScriptApplication;
import com.cxplan.projection.mediate.script.message.CommandConstant;
import com.cxplan.projection.mediate.script.message.ScriptControllerConnection;

import org.apache.mina.core.session.IoSession;

/**
 * @author kenny
 */
public class CreateSessionCommandHandler extends AbstractCommandHandler {
    private static final String TAG = Constant.TAG_PREFIX + "Session";

    public CreateSessionCommandHandler() {
        super(CommandConstant.CMD_DEVICE_CREATE_SESSION);
    }

    @Override
    public void process(IoSession session, Message message) throws MessageException {
        LogUtil.i(TAG, "to initialize script connection");

        ScriptControllerConnection controllerConnection = new ScriptControllerConnection();
        controllerConnection.setSession(session);

        session.setAttribute(Constant.CLIENT_SESSION, controllerConnection);

        Message retMsg = Message.createResultMessage(message);
        retMsg.setParameter("ret", 0);

        controllerConnection.sendMessage(retMsg);
        ScriptApplication.getInstance().setControllerConnection(controllerConnection);
    }
}
