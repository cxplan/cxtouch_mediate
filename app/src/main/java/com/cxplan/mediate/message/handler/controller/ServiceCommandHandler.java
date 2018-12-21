package com.cxplan.mediate.message.handler.controller;

import com.cxplan.common.util.LogUtil;
import com.cxplan.mediate.CXApplication;
import com.cxplan.mediate.Constant;
import com.cxplan.mediate.message.Message;
import com.cxplan.mediate.message.MessageException;
import com.cxplan.mediate.message.MessageUtil;
import com.cxplan.mediate.message.handler.AbstractCommandHandler;
import com.cxplan.mediate.process.Main;

import org.apache.mina.core.session.IoSession;

public class ServiceCommandHandler extends AbstractCommandHandler {
    private static final String TAG = Constant.TAG_PREFIX + "Service";

    public ServiceCommandHandler() {
        super(MessageUtil.CMD_DEVICE_SERVICE);
    }

    @Override
    public void process(IoSession session, Message message) throws MessageException {
        short type = message.getParameter("type");
//        LogUtil.e(TAG, "monkey: " + type);
        switch (type) {
            case 1://start projection
//                processStartProjection(message);
                break;
            case 2://end projection.
                processEndProjection(message);
                break;
            case 3:
                processStartImageService(message);
                break;
            default:
                LogUtil.e(TAG, "The image operation is not supported: " + type);
        }
    }

    private void processStop(Message message) {
        CXApplication.getInstance().setInProjection(true);
        LogUtil.i(TAG, "State of projection: true");
    }
    private void processEndProjection(Message message) {
        CXApplication.getInstance().setInProjection(false);
        Main.shutdownMinicap();
        LogUtil.i(TAG, "State of projection: false");
    }

    private void processStartImageService(Message message) throws MessageException {
        Integer imageQuality = message.getParameter("iq");
        Float zoomRate = message.getParameter("zr");
        if (imageQuality != null) {
            CXApplication.getInstance().setImageQuality(imageQuality);
        }
        if (zoomRate != null) {
            CXApplication.getInstance().getDeviceInfo().setZoomRate(zoomRate);
        }
        LogUtil.i(TAG, "Restart image service.[image quality=" + imageQuality + ", zoom rate=" + zoomRate + "]");

        //restart minicap service
        Main.shutdownMinicap();
        Main.startMinicap();

        Message retMsg = Message.createResultMessage(message);
        CXApplication.getInstance().getControllerConnection().sendMessage(retMsg);
    }

}
