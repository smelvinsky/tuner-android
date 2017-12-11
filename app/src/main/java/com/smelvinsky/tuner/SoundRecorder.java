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
    final private int SAMPLE_RATE = 44100;
    final private String LOG_TAG = "SoundRecorder class";

    private AudioRecord audioRecord;

    private int bufferSize;

    SoundRecorder()
    {
        bufferSize = AudioRecord.getMinBufferSize(SAMPLE_RATE, AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_16BIT);
        Log.i(LOG_TAG, "Given buffer length: " + bufferSize);

        if (bufferSize == AudioRecord.ERROR || bufferSize == AudioRecord.ERROR_BAD_VALUE)
        {
            bufferSize = SAMPLE_RATE * 2;
        }

        audioRecord = new AudioRecord(MediaRecorder.AudioSource.DEFAULT, SAMPLE_RATE, AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_16BIT, bufferSize);

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
        int bytesRead = 0;

        bytesRead = audioRecord.read(buffer, 0, sizeInShorts);

        return bytesRead;
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
