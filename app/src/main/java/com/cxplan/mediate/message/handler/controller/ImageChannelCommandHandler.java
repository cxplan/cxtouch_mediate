package com.cxplan.mediate.message.handler.controller;

import android.graphics.Bitmap;

import com.cxplan.common.util.LogUtil;
import com.cxplan.mediate.CXApplication;
import com.cxplan.mediate.Constant;
import com.cxplan.mediate.io.ClientConnection;
import com.cxplan.mediate.message.Message;
import com.cxplan.mediate.message.MessageException;
import com.cxplan.mediate.message.MessageUtil;
import com.cxplan.mediate.message.handler.AbstractCommandHandler;
import com.cxplan.mediate.process.Main;
import com.cxplan.mediate.service.DeviceService;
import com.cxplan.mediate.util.WindowManagerUtil;

import org.apache.mina.core.session.IoSession;

import java.io.ByteArrayOutputStream;
/**
 * Created on 2018/5/19.
 *
 * @author kenny
 */
public class ImageChannelCommandHandler extends AbstractCommandHandler {
    private static final String TAG = Constant.TAG_PREFIX + "Image";

    public ImageChannelCommandHandler() {
        super(MessageUtil.CMD_DEVICE_IMAGE);
    }

    @Override
    public void process(IoSession session, Message message) throws MessageException {
        ClientConnection connection = getConnection(session);
        short type = message.getParameter("type");
//        LogUtil.e(TAG, "monkey: " + type);
        switch (type) {
            case 1://start projection
                processStartProjection(message);
                break;
            case 2://end projection.
                processEndProjection(message);
                break;
            case 3:
                processStartImageService(message);
                break;
            case 4:
                taskScreenshot(message, connection);
                break;
            default:
                LogUtil.e(TAG, "The image operation is not supported: " + type);
        }
    }

    private void processStartProjection(Message message) {
        CXApplication.getInstance().setInProjection(true);
        LogUtil.i(TAG, "State of projection: true");
    }
    private void processEndProjection(Message message) {
        CXApplication.getInstance().setInProjection(false);
        Main.shutdownMinicap();

        //switch inputer
        new DeviceService().disableCXIME();
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

    private void taskScreenshot(Message message, ClientConnection connection) throws MessageException {
        //The range of value is 0.0 - 1.0
        float zoomRate = message.getParameter("zr");
        //0 - 100
        int quality = message.getParameter("q");

        LogUtil.i(TAG, "take a screenshot: zoomRate=" + zoomRate + ", quality=" + quality);
        ByteArrayOutputStream bout = new ByteArrayOutputStream();
        Bitmap bitmap = null;
        try {
            bitmap = WindowManagerUtil.screenshot(zoomRate);
            bitmap.compress(Bitmap.CompressFormat.JPEG, quality, bout);
        } catch (Exception e) {
            LogUtil.e(TAG, e.getMessage(), e);
            throw new MessageException(e.getMessage());
        }
        Message retMsg = Message.createResultMessage(message);
        retMsg.setParameter("img", bout.toByteArray());

        connection.sendMessage(retMsg);
    }

}
