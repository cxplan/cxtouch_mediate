package com.cxplan.cxscript.message.handler;

import com.cxplan.common.util.LogUtil;
import com.cxplan.cxscript.ScriptApplication;
import com.cxplan.cxscript.message.CommandConstant;
import com.cxplan.projection.mediate.Constant;
import com.cxplan.projection.mediate.io.ClientConnection;
import com.cxplan.projection.mediate.message.Message;
import com.cxplan.projection.mediate.message.MessageException;
import com.cxplan.projection.mediate.message.handler.AbstractCommandHandler;

import org.apache.mina.core.session.IoSession;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

/**
 * @author kenny
 */
public class DumpHierarchyCommandHandler extends AbstractCommandHandler {
    private static final String TAG = Constant.TAG_PREFIX + "Span";
    public DumpHierarchyCommandHandler() {
        super(CommandConstant.CMD_DEVICE_DUMP);
    }

    @Override
    public void process(IoSession session, Message message) throws MessageException {
        ClientConnection connection = getConnection(session);
        LogUtil.i(TAG, "Dump the hierarchy of view");

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        try {
            ScriptApplication.getInstance().getDevice().dumpWindowHierarchy(outputStream);
        } catch (IOException e) {
            throw new MessageException(e.getMessage(), e);
        }

        Message retMsg = Message.createResultMessage(message);
        retMsg.setParameter("data", outputStream.toByteArray());
        connection.sendMessage(retMsg);
    }
}
