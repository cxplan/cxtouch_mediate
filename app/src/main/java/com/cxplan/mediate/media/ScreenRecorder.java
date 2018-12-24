package com.cxplan.mediate.media;

import android.media.MediaRecorder;

import java.io.IOException;

public class ScreenRecorder {
    MediaRecorder recorder;
    public void start(String file) throws IOException {
        recorder = new MediaRecorder();
        recorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        recorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
        recorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
        recorder.setOutputFile(file);
        recorder.prepare();
        recorder.start();   // Recording is now started

    }

    public void stop() {
        recorder.stop();
        recorder.reset();   // You can reuse the object by going back to setAudioSource() step
        recorder.release(); // Now the object cannot be reused
    }
}
