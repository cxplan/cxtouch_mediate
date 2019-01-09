package com.cxplan.projection.mediate.io.image;

import android.net.LocalSocketAddress;

import com.cxplan.common.util.CommonUtil;
import com.cxplan.common.util.LogUtil;
import com.cxplan.projection.mediate.Constant;
import com.cxplan.projection.mediate.process.Main;
import com.cxplan.projection.mediate.util.NetUtil;

import java.io.IOException;
import java.io.InputStream;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * This image server is used for wireless device to received image data.
 * Created on 2019/1/6.
 *
 * @author kenny
 */
public class ImageServer {

    private static final String TAG = Constant.TAG_PREFIX + "imgServer";

    public static final int DEFAULT_PORT = 2015;

    private CXLocalSocket imageSocket;
    private ControllerProcessThread imageProcessThread;
    private volatile boolean stop;
    private Thread serverThread;

    public ImageServer() {
    }

    public synchronized void start() {
        if (serverThread != null && serverThread.isAlive()) {
            LogUtil.i(TAG, "The image server is started already!");
            return;
        }
        stop = false;
        Runnable task = new Runnable() {
            @Override
            public void run() {
                ServerSocket serverSocket;
                try {
                    String ip = NetUtil.getLocalIp();
                    serverSocket = new ServerSocket();
                    serverSocket.setReuseAddress(true);
                    serverSocket.bind(new InetSocketAddress(ip, DEFAULT_PORT));
                } catch (IOException e) {
                    LogUtil.e(TAG, "Starting image server failed: " + e.getMessage(), e);
                    return;
                }

                String info = "The image server is started successfully.";
                LogUtil.i(TAG, info);
                System.out.println(info);

                while (!stop) {
                    try {
                        Socket socket = serverSocket.accept();
                        LogUtil.i(TAG, "A new image client is connected.");
                        processClient(socket);
                    } catch (IOException e) {
                        LogUtil.e(TAG, "Accepting client error: " + e.getMessage(), e);
                        return;
                    }
                }
            }
        };

        serverThread = new Thread(task, "Image Server");
        serverThread.start();
    }

    public void stop() {
        stop = true;
    }

    public boolean isInProjection() {
        return imageProcessThread.isAlive();
    }

    public boolean isImageChannelAvailable() {
        return imageSocket != null && !imageSocket.isClosed();
    }

    protected void processClient(Socket socket) {
        if (imageProcessThread != null && imageProcessThread.isAlive()) {
            imageProcessThread.toStop();
        }
        try {
            openMinicapService();
        } catch (Exception e) {
            LogUtil.e(TAG, e.getMessage(), e);
            return;
        }
        imageProcessThread = new ControllerProcessThread(imageSocket, socket);
        imageProcessThread.start();
    }

    protected void openMinicapService() {
        if (isImageChannelAvailable()) {
            return;
        }
        if (!Main.isMinicapActive()) {
            Main.startMinicap();
        }
        LogUtil.i(TAG, "Starting to connect to minicap...");
        imageSocket = new CXLocalSocket();
        int count = 0;
        boolean success = false;
        while (count < 20) {
            try {
                imageSocket.connect(new LocalSocketAddress("minicap", LocalSocketAddress.Namespace.ABSTRACT));
                success = true;
                break;
            } catch (IOException e) {
            }
            count++;
            LogUtil.e(TAG, "try to connect minicap: " + count);
            try {
                Thread.sleep(200);
            } catch (InterruptedException e) {
            }
        }

        if (!success) {
            LogUtil.e(TAG, "Connecting to minicap service is timeout");
        } else {
        }
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
            try {
                socket.close();
            } catch (IOException e) {
            }
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
                    socket.getOutputStream().flush();
                    LogUtil.i(TAG, "new image data: " + count);
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
