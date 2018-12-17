package com.cxplan.mediate.io.image;

import com.cxplan.common.util.CommonUtil;
import com.cxplan.common.util.LogUtil;
import com.cxplan.mediate.Constant;

import java.io.IOException;
import java.io.InputStream;
import java.net.Socket;

public class ImageServer {

    private static final String TAG = Constant.TAG_PREFIX + "imgServer";

    private CXLocalSocket imageSocket;
    private ControllerProcessThread imageProcessThread;

    public ImageServer() {
    }

    public void start() {
    }

    public void stop() {
    }

    public boolean isInProjection() {
        return imageProcessThread.isAlive();
    }

    public boolean isImageChannelAvailable() {
        return imageSocket != null && imageSocket.isClosed();
    }

    private class ControllerProcessThread extends Thread{

        private Socket socket;
        private CXLocalSocket imageChannel;

        private boolean stop = false;
        public ControllerProcessThread(CXLocalSocket imageChannel, Socket socket) {
            super("Image transfer Thread");
            this.socket = socket;
            this.imageChannel = imageChannel;
        }

        public void toStop() {
            stop = true;
            interrupt();
        }

        @Override
        public void run() {
            stop = false;
            InputStream input;
            try {
                input = imageChannel.getInputStream();
            } catch (Exception e) {
                LogUtil.e(TAG, "Retrieving image stream failed: " + e.getMessage(), e);
                return;
            }

            try {
                StringBuilder sb = new StringBuilder();
                sb.append("version: ").append(input.read());
                sb.append("\nlength: ").append(input.read());
                sb.append("\npid: ").append(CommonUtil.readIntLowEndian(input));
                int realWidth = CommonUtil.readIntLowEndian(input);
                int realHeight = CommonUtil.readIntLowEndian(input);
                sb.append("\nreal width: ").append(realWidth);
                sb.append("\nreal height: ").append(realHeight);
                sb.append("\nvirtual width: ").append(CommonUtil.readIntLowEndian(input));
                sb.append("\nvirtual height: ").append(CommonUtil.readIntLowEndian(input));
                sb.append("\nDisplay orientation: ").append(input.read());
                sb.append("\nQuirk bitflags: ").append(input.read());
                LogUtil.i(TAG, sb.toString());

                byte[] buffer = new byte[2048];
                while (!stop && imageChannel.isConnected()) {
                    int count = input.read(buffer);
                    if (count == -1) {
                        break;
                    }
                    socket.getOutputStream().write(buffer, 0, count);
                }
            } catch (Exception ex) {
                LogUtil.e(TAG, ex.getMessage(), ex);
            } finally {
                LogUtil.i(TAG, "The image server is over");
                try {
                    imageChannel.close();
                } catch (IOException e) {
                }
            }
        }
    }
}
