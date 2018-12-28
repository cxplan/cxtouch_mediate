package com.cxplan.projection.mediate.inputer;

import com.cxplan.common.util.LogUtil;
import com.cxplan.projection.mediate.Constant;
import com.cxplan.projection.mediate.message.Message;
import com.cxplan.projection.mediate.message.MessageException;
import com.cxplan.projection.mediate.message.MessageUtil;
import com.cxplan.projection.mediate.message.handler.AbstractCommandHandler;

import org.apache.mina.core.session.IoSession;
/**
 * Created on 2018/5/19.
 *
 * @author kenny
 */
public class InputerCommandHandler extends AbstractCommandHandler {
    private static final String TAG = Constant.TAG_PREFIX + "Inputer";

    public InputerCommandHandler() {
        super(MessageUtil.CMD_DEVICE_INPUTER);
    }

    @Override
    public void process(IoSession session, Message message) throws MessageException {
        short type = message.getParameter("type");
        switch (type) {
            case 1://switch other ime.
                processSwitchOtherIME(message);
                break;
            default:
                LogUtil.e(TAG, "The inputer operation is not supported: " + type);
        }
    }

    private void processSwitchOtherIME(Message message) {
        if (CXTouchIME.instance != null) {
            CXTouchIME.instance.switchOtherIME();
        }
    }

}
