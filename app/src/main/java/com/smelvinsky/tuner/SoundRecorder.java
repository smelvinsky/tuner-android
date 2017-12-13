package com.smelvinsky.tuner;

import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.util.Log;

import java.nio.ByteBuffer;

/**
 * Created by smelvinsky on 10.12.17.
 */

public class SoundRecorder
{
    final private int SAMPLE_RATE = 11025;
    final private String LOG_TAG = "SoundRecorder class";

    private AudioRecord audioRecord;

    private int bufferSize;

    SoundRecorder()
    {
        bufferSize = 8192 * 4;

        audioRecord = new AudioRecord(MediaRecorder.AudioSource.MIC, SAMPLE_RATE, AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_16BIT, bufferSize);

        if (audioRecord.getState() != AudioRecord.STATE_INITIALIZED)
        {
            Log.e(LOG_TAG, "Cannot initialize audio recording hardware");
        }
    }

    int getBufferSize()
    {
        return bufferSize;
    }

    void startRecording() throws IllegalStateException
    {
        audioRecord.startRecording();
        Log.i(LOG_TAG, "Recording started");
    }

    public int read(short[] buffer, int sizeInShorts)
    {
        return audioRecord.read(buffer, 0, sizeInShorts);
    }

    void stop() throws IllegalStateException
    {
        audioRecord.stop();
    }

    void release() throws IllegalStateException
    {
        audioRecord.release();
    }

    int getSampleRate()
    {
        return SAMPLE_RATE;
    }
}
