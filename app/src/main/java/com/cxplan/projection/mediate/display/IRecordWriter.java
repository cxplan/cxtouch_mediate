package com.cxplan.projection.mediate.display;

import android.media.MediaCodec;
import android.media.MediaFormat;

import java.nio.ByteBuffer;

public interface IRecordWriter {

    boolean setFormat(MediaFormat mediaFormat, ByteBuffer codecPacket);

    void onOutputBuffer(ByteBuffer byteBuffer, MediaCodec.BufferInfo bufferInfo);

    void stop();
}
