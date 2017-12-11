package com.smelvinsky.tuner;

import android.Manifest;
import android.content.pm.PackageManager;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Arrays;
import java.util.concurrent.Semaphore;

public class Tuner extends AppCompatActivity
{
    private static final int REQUEST_AUDIO_PERMISSION = 1;
    private final String LOG_TAG = "Tuner class";
    private SoundRecorder soundRecorder;
    private short[] audioDataBuffer;
    private double[] postFFTsignal;

    private Pitchmeter pitchmeter = new Pitchmeter(Note.A, 440, Octave.oneLine);

    /* binary semaphore for thread sync */
    private final Semaphore binarySemaphore = new Semaphore(1);

    /* recording thread */
    private Runnable recordingThread = new Runnable()
    {
        @Override
        public void run()
        {
            int samplesRead = 0;

            while (true)
            {
                samplesRead = soundRecorder.read(audioDataBuffer, audioDataBuffer.length);
                Log.i(LOG_TAG, samplesRead + " samples read");

                try
                {
                    binarySemaphore.acquire();
                    for (int i = 0; i < audioDataBuffer.length; i++)
                    {
                        postFFTsignal[i] = audioDataBuffer[i];
                    }

                    postFFTsignal = SoundProcessing.fft(postFFTsignal);
                }
                catch (InterruptedException ie)
                {
                    Log.e(LOG_TAG, "audio recording thread (recordingThread) interrupted");
                    finish();
                }
                finally
                {
                    binarySemaphore.release();
                    new Thread(noteIndicationThread).start();
                }

                Log.i(LOG_TAG, Arrays.toString(postFFTsignal));
            }
        }
    };

    /* GUI - note indication thread                     */
    /* This thread is being run from recordingThread    */
    private Runnable noteIndicationThread = new Runnable()
    {
        @Override
        public void run()
        {
            try
            {
                binarySemaphore.acquire();
                double dominantFreq = pitchmeter.findDominantFreq(100, 1200, postFFTsignal, soundRecorder.getSampleRate(), audioDataBuffer.length);
                Log.i(LOG_TAG, "Dominant frequency is: " + dominantFreq);
            }
            catch (InterruptedException ie)
            {
                Log.e(LOG_TAG, "gui updating thread (noteIndicationThread) interrupted");
                finish();
            }
            finally
            {
                binarySemaphore.release();
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tuner);

        /* Check AUDIO permission and request if necessary */
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED)
        {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.RECORD_AUDIO}, REQUEST_AUDIO_PERMISSION);
        }

        TextView noteIndicator = findViewById(R.id.note_indicator);
        noteIndicator.setText("L");

        try
        {
            soundRecorder = new SoundRecorder();
            audioDataBuffer = new short[soundRecorder.getBufferSize()];
            postFFTsignal = new double[audioDataBuffer.length];
            soundRecorder.startRecording();

            Log.i(LOG_TAG, "Recording started");
        }
        catch (IllegalStateException ise)
        {
            Log.wtf(LOG_TAG, "Cannot start recording!");
            Toast audioRecorderToast = Toast.makeText(getApplicationContext(), "Error: Cannot start recording (Check audio recording permission in settings)", Toast.LENGTH_LONG);
            audioRecorderToast.show();
            finish();
        }

        new Thread(recordingThread).start();

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        try
        {
            soundRecorder.stop();
            Log.i(LOG_TAG, "Recording stopped");
            soundRecorder.release();
            Log.i(LOG_TAG, "Audio resources released");
        }
        catch (IllegalStateException ise)
        {
            Log.wtf(LOG_TAG, "Couldn't release resources");
        }
    }
}
