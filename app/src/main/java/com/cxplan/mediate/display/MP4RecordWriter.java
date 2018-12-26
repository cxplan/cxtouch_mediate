package com.cxplan.mediate.display;

import android.media.MediaCodec;
import android.media.MediaFormat;
import android.media.MediaMuxer;

import com.cxplan.common.util.LogUtil;
import com.cxplan.mediate.Constant;

import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;

public class MP4RecordWriter implements IRecordWriter {
    private static final String TAG = Constant.TAG_PREFIX + "mp4writer";

    private MediaMuxer muxer;
    private boolean keyFrame;
    private long firstTime;
    private File file;
    private int videoTrack;

    public MP4RecordWriter(File file) {
        this.file = file;
    }

    public File getFile() {
        return file;
    }

    @Override
    public boolean setFormat(MediaFormat mediaFormat, ByteBuffer codecPacket) {
        LogUtil.e(TAG, "mf:" + mediaFormat + ", bb=" + codecPacket);
        if (muxer != null) {
            return false;
        }
        try {
            this.muxer = new MediaMuxer(file.getAbsolutePath(), MediaMuxer.OutputFormat.MUXER_OUTPUT_MPEG_4);
        } catch (IOException e) {
            LogUtil.e(TAG, e.getMessage(), e);
        }
        videoTrack = muxer.addTrack(mediaFormat);
        muxer.start();

        MediaCodec.BufferInfo bufferInfo = new MediaCodec.BufferInfo();
        bufferInfo.flags = 2;
        bufferInfo.size = codecPacket.remaining();
        onOutputBuffer(codecPacket, bufferInfo);

        return true;
    }

    @Override
    public void onOutputBuffer(ByteBuffer byteBuffer, MediaCodec.BufferInfo bufferInfo) {
        if ((bufferInfo.flags & 2) == 0) {
            this.muxer.writeSampleData(this.videoTrack, byteBuffer, bufferInfo);
            return;
        }
        if (!keyFrame && (bufferInfo.flags & 1) == 1) {
            this.firstTime = bufferInfo.presentationTimeUs;
            keyFrame = true;
        }
        if (keyFrame) {
            MediaCodec.BufferInfo bufferInfo2 = new MediaCodec.BufferInfo();
            bufferInfo2.set(bufferInfo.offset, bufferInfo.size, bufferInfo.presentationTimeUs - this.firstTime, bufferInfo.flags);
            this.muxer.writeSampleData(this.videoTrack, byteBuffer, bufferInfo2);
        }
    }

    @Override
    public void stop() {
        this.muxer.stop();
        this.muxer.release();
    }
}
