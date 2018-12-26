package com.cxplan.mediate.display;

import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.Point;
import android.media.MediaCodec;
import android.media.MediaCodecInfo;
import android.media.MediaCodecList;
import android.media.MediaFormat;
import android.os.Bundle;
import android.util.Log;
import android.view.Surface;

import com.cxplan.common.util.LogUtil;
import com.cxplan.mediate.Constant;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.concurrent.TimeUnit;

public class ScreenRecorder {
    private static final String TAG = Constant.TAG_PREFIX + "recorder";

    private static ScreenRecorder instance;

    public static ScreenRecorder getRecorder(int width, int height, boolean created) {
        if (created && instance == null) {
            instance = new ScreenRecorder(width, height);
        }

        if (instance != null) {
            instance.registerVirtualDisplay(null);
        }
        return instance;
    }

    private ByteBuffer codecPacket;
    private MediaFormat outputFormat;
    private Point encodeSize;
    private int width;
    private int height;
    private MediaCodec videoCodec;
    private IRecordWriter recordWriter;
    int colorFormat;

    private WriterTask recorderThread;

    private ScreenRecorder(int width, int height) {
        this(width, height, null);
    }

    public ScreenRecorder(int width, int height, IRecordWriter recordWriter) {
        this.recordWriter = recordWriter;
        this.width = width;
        this.height = height;
    }

    public IRecordWriter getRecordWriter() {
        return recordWriter;
    }

    public void setRecordWriter(IRecordWriter recordWriter) {
        this.recordWriter = recordWriter;
        if (recordWriter != null && codecPacket != null) {
            if (recordWriter.setFormat(outputFormat, codecPacket)) {
                requestSyncFrame();
            }
        }
    }

    public MediaFormat getOutputFormat() {
        return outputFormat;
    }

    public ByteBuffer getCodecPacket() {
        return codecPacket;
    }

    public Surface createSurface() {
        MediaCodecInfo mediaCodecInfo;
        videoCodec = null;
        MediaCodecInfo tmpMCI = null;
        try {
            int codecCount = MediaCodecList.getCodecCount();
            tmpMCI = null;
            for (int i = 0; i < codecCount; i++) {
                MediaCodecInfo codecInfoAt = MediaCodecList.getCodecInfoAt(i);
                if (codecInfoAt.isEncoder()) {
                    for (String equalsIgnoreCase : codecInfoAt.getSupportedTypes()) {
                        if (equalsIgnoreCase.equalsIgnoreCase("video/avc")) {
                            if (tmpMCI == null) {
                                tmpMCI = codecInfoAt;
                            }
                            LogUtil.i(TAG, codecInfoAt.getName());
                            MediaCodecInfo.CodecCapabilities capabilitiesForType = codecInfoAt.getCapabilitiesForType("video/avc");
                            for (int i3 : capabilitiesForType.colorFormats) {
                                LogUtil.i(TAG, "colorFormat: " + i3);
                            }
                            for (MediaCodecInfo.CodecProfileLevel codecProfileLevel : capabilitiesForType.profileLevels) {
                                LogUtil.i(TAG, "profile/level: " + codecProfileLevel.profile + "/" + codecProfileLevel.level);
                            }
                            break;
                        }
                    }
                    continue;
                }
            }
            mediaCodecInfo = tmpMCI;
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage(), e);
        }
        try {
            this.width = this.width / 16 * 16;
            this.height = this.height / 16 * 16;
            LogUtil.i(TAG, "Width: " + this.width + " Height: " + this.height);
            this.encodeSize = new Point(this.width, this.height);
            MediaFormat videoFormat = MediaFormat.createVideoFormat("video/avc", this.width, this.height);
            int bitrate = 2000000;
            int frameRate = 30;

            videoFormat.setInteger("bitrate", bitrate);
            videoFormat.setInteger("frame-rate", frameRate);
            videoFormat.setLong("repeat-previous-frame-after", TimeUnit.MILLISECONDS.toMicros((long) (1000 / frameRate)));
            videoFormat.setInteger("i-frame-interval", 30);

            LogUtil.i(TAG, "Bitrate: " + bitrate + ", Frame rate: " + frameRate);
            LogUtil.i(TAG, "Creating encoder");
            setSurfaceFormat(videoFormat);
            try {
                this.videoCodec = MediaCodec.createEncoderByType("video/avc");
            } catch (Throwable e2) {
                LogUtil.e(TAG, "Unable to create codec by type, attempting explicit creation", e2);
                this.videoCodec = MediaCodec.createByCodecName(mediaCodecInfo.getName());
            }
            LogUtil.i(TAG, "Created encoder");
            LogUtil.i(TAG, "Configuring encoder");
            this.videoCodec.configure(videoFormat, null, null, MediaCodec.CONFIGURE_FLAG_ENCODE);
            LogUtil.i(TAG, "Creating input surface");
            Surface surface = this.videoCodec.createInputSurface();
            LogUtil.i(TAG, "Starting Encoder");
            this.videoCodec.start();
            LogUtil.i(TAG, "Surface ready");
            this.recorderThread = new WriterTask();
            this.recorderThread.start();
            LogUtil.i(TAG, "Encoder ready");
            return surface;
        } catch (Throwable ex) {
            LogUtil.e(TAG, ex.getMessage(), ex);
            return null;
        }
    }

    @TargetApi(19)
    public void requestSyncFrame() {
        Bundle bundle = new Bundle();
        bundle.putInt("request-sync", 0);
        this.videoCodec.setParameters(bundle);
    }

    void destroySurface(MediaCodec mediaCodec) {
        if (mediaCodec != null) {
            try {
                mediaCodec.stop();
                mediaCodec.release();
            } catch (Exception e) {
            }
            if (this.videoCodec == mediaCodec) {
                this.videoCodec = null;
                CXVirtualDisplay.release();
            }
        }
    }

    public void registerVirtualDisplay(Context context) {
        if (!CXVirtualDisplay.isCreated()) {
            Surface surface = createSurface();
            if (surface == null) {
                LogUtil.e(TAG, "Unable to create surface");
                return;
            }
            LogUtil.e(TAG, "Created surface");
            CXVirtualDisplay.create("CXTouch", this.width, this.height, surface);
        }
    }

    @TargetApi(18)
    void setSurfaceFormat(MediaFormat mediaFormat) {
        this.colorFormat = 2130708361;
        mediaFormat.setInteger("color-format", this.colorFormat);
    }

    /**
     * Stop recording and release all resources.
     */
    public void stop() {
        if (recordWriter != null) {
            recordWriter.stop();
        }
        recorderThread.toStop();
        CXVirtualDisplay.release();
        instance = null;
    }

    class WriterTask extends Thread {
        private boolean quit;
        private MediaCodec.BufferInfo bufferInfo = new MediaCodec.BufferInfo();

        public WriterTask() {
            super("Video Encoder");
            quit = false;
        }

        public void toStop() {
            quit = true;
        }

        public final void run() {
            try {
                encode();
            } catch (Throwable e) {
                LogUtil.e(TAG, "Encoder error", e);
            }
            cleanup();
        }

        protected void cleanup() {
            destroySurface(ScreenRecorder.this.videoCodec);
        }

        protected void encode() throws Exception {
            LogUtil.i(TAG, "Writer started.");

            while (!quit) {
                int index = videoCodec.dequeueOutputBuffer(bufferInfo, -1);
                //Log.i(TAG, "dequeue output buffer index=" + index);
                if (index == MediaCodec.INFO_OUTPUT_FORMAT_CHANGED) {
                    try {
                        resetOutputFormat();
                    } catch (Exception e) {
                        Log.e("ScreenRecorder", "Transferring data failed:" + e.getMessage(), e);
                        break;
                    }

                } else if (index == MediaCodec.INFO_TRY_AGAIN_LATER) {
//                Log.d(TAG, "retrieving buffers time out!");
                    try {
                        // wait 10ms
                        Thread.sleep(5);
                    } catch (InterruptedException e) {
                    }
                } else if (index >= 0) {
                    try {
                        encodeToVideoTrack(index);
                    } catch (IOException e) {
                        Log.e("ScreenRecorder", "Transferring data failed:" + e.getMessage(), e);
                        break;
                    }

                    videoCodec.releaseOutputBuffer(index, false);
                }
            }
            LogUtil.i(TAG, "The recorder is finished!");
        }

        private void encodeToVideoTrack(int index) throws IOException {
            ByteBuffer encodedData = getOutputBuffer(index);

            if ((bufferInfo.flags & MediaCodec.BUFFER_FLAG_CODEC_CONFIG) != 0) {
                // The codec config data was pulled out and fed to the muxer when we got
                // the INFO_OUTPUT_FORMAT_CHANGED status.
                encodedData.position(bufferInfo.offset);
                encodedData.limit(bufferInfo.offset + bufferInfo.size);

                ScreenRecorder.this.codecPacket = ByteBuffer.allocate(bufferInfo.size);
                ScreenRecorder.this.codecPacket.put(encodedData);
                ScreenRecorder.this.codecPacket.flip();
                bufferInfo.size = 0;
                if (recordWriter != null) {
                    if (recordWriter.setFormat(outputFormat, ScreenRecorder.this.codecPacket)) {
                        requestSyncFrame();
                    }
                }
                LogUtil.i(TAG, "The codec config is retrieved.");
            }
            if (bufferInfo.size == 0) {
                encodedData = null;
            } else {
//                LogUtil.i(TAG, "got buffer, info: size=" + bufferInfo.size
//                        + ", presentationTimeUs=" + bufferInfo.presentationTimeUs
//                        + ", offset=" + bufferInfo.offset);
            }
            if (encodedData != null) {
                encodedData.position(bufferInfo.offset);
                encodedData.limit(bufferInfo.offset + bufferInfo.size);

                if (recordWriter != null) {
                    recordWriter.onOutputBuffer(encodedData, bufferInfo);
                }
            }
        }

        private void resetOutputFormat() throws IOException {
            // should happen before receiving buffers, and should only happen once
            outputFormat = videoCodec.getOutputFormat();
//            ByteBuffer sps = outputFormat.getByteBuffer("csd-0");    // SPS
//            ByteBuffer pps = outputFormat.getByteBuffer("csd-1");    // PPS

            LogUtil.i(TAG, "The header is ok----");
        }
    }

    private ByteBuffer getOutputBuffer(int index) {
        ByteBuffer[] outputBuffers = videoCodec.getOutputBuffers();
        return outputBuffers[index];
    }
}
