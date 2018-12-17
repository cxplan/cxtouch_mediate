package com.cxplan.mediate.message.handler;

import com.cxplan.common.util.LogUtil;
import com.cxplan.mediate.Constant;
import com.cxplan.mediate.message.Message;
import com.cxplan.mediate.message.MessageException;

import org.apache.mina.core.session.IoSession;

import java.io.IOException;

public class StartProcessCommandHandler extends AbstractCommandHandler {
    private static final String TAG = Constant.TAG_PREFIX + "startProcess";

    public StartProcessCommandHandler() {
        super("");
    }

    @Override
    public void process(IoSession session, Message message) throws MessageException {
        short type = message.getParameter("type");
//        LogUtil.e(TAG, "monkey: " + type);
        switch (type) {
            case 1:
                processType(message);
                break;
            default:
                LogUtil.e(TAG, "The monkey operation is not supported: " + type);
        }
    }

    private void processType(Message message) {
        try {
            Runtime.getRuntime().exec(new String[]{"/bin/sh", "-c","LD_LIBRARY_PATH=/data/local/tmp /data/local/tmp/minicap -S -P 720x1280@360x640/0"});
        } catch (IOException e) {
            LogUtil.e(TAG, e.getMessage(), e);
        }
    }
}
