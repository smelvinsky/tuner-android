package com.smelvinsky.tuner;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
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

    /* GUI components */
    private TextView freqIndicator;
    private TextView noteIndicator;
    private TextView sharpNoteIndicator;
    private ImageView greenDot;

    /* binary semaphore for thread sync */
    private final Semaphore binarySemaphore = new Semaphore(1);

    /* recording thread */
    private Runnable recordingRunnable = new Runnable()
    {
        @Override
        public void run()
        {
            int samplesRead;

            while (!Thread.currentThread().isInterrupted())
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
                    new Thread(noteIndicationRunnable).start();
                }

                Log.i(LOG_TAG, Arrays.toString(postFFTsignal));
            }
        }
    };

    /* GUI - note indication thread                     */
    /* This thread is being run from recordingThread    */
    private Runnable noteIndicationRunnable = new Runnable()
    {
        @Override
        public void run()
        {
            try
            {
                binarySemaphore.acquire();
            }
            catch (InterruptedException ie)
            {
                Log.e(LOG_TAG, "gui updating thread (noteIndicationThread) interrupted");
                finish();
            }
            finally
            {
                final double dominantFreq = pitchmeter.findDominantFreq(100, 1200, postFFTsignal, soundRecorder.getSampleRate(), audioDataBuffer.length, 40000.0);

                if (dominantFreq != 0)
                {
                    final NoteObject noteObject = pitchmeter.getNoteByFreq(dominantFreq);

                    runOnUiThread(new Runnable()
                    {
                        @Override
                        public void run()
                        {
                            freqIndicator.setText(String.format("%.2f", dominantFreq));
                            noteIndicator.setText(noteObject.getNote().toString());
                            if (noteObject.getNote() == Note.Cis ||
                                    noteObject.getNote() == Note.Dis ||
                                    noteObject.getNote() == Note.Fis ||
                                    noteObject.getNote() == Note.Gis ||
                                    noteObject.getNote() == Note.Ais)
                            {
                                sharpNoteIndicator.setText("#");
                            }
                            else
                            {
                                sharpNoteIndicator.setText("");
                            }

                            pitchmeter.checkCorrectness(greenDot, dominantFreq, noteObject);

                            Log.i(LOG_TAG, "Dominant frequency is: " + dominantFreq);
                            Log.i(LOG_TAG, "Note is:" + noteObject.getNote().toString() + " " + noteObject.getOctave().toString());
                        }
                    });
                }
                else
                {
                    runOnUiThread(new Runnable()
                    {
                        @Override
                        public void run()
                        {
                            freqIndicator.setText(String.format("%.2f", dominantFreq));
                            noteIndicator.setText("?");
                            sharpNoteIndicator.setText("");
                            greenDot.setVisibility(View.INVISIBLE);
                        }
                    });
                }

                binarySemaphore.release();
            }
        }
    };

    /* Threads */
    private Thread recordingThread = new Thread(recordingRunnable);

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tuner);

        Log.wtf(LOG_TAG, "On Create");

        /* Check AUDIO permission and request if necessary */
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED)
        {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.RECORD_AUDIO}, REQUEST_AUDIO_PERMISSION);
        }

        noteIndicator = findViewById(R.id.note_indicator);
        freqIndicator = findViewById(R.id.freq_indicator);
        sharpNoteIndicator = findViewById(R.id.sharp_note_indicator);
        greenDot =findViewById(R.id.green_dot);

        greenDot.setVisibility(View.INVISIBLE);

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

        recordingThread.start();

    }

    @Override
    protected void onDestroy()
    {
        super.onDestroy();
        Log.wtf(LOG_TAG, "On destroy");

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

    @Override
    protected void onStart()
    {
        super.onStart();
        Log.wtf(LOG_TAG, "On Start");
        try
        {
            soundRecorder.startRecording();
            try
            {
                recordingThread.start();
            }
            catch (IllegalThreadStateException ile)
            {
                Log.w(LOG_TAG, "Thread already started");
            }
        }
        catch (IllegalStateException ise)
        {
            Log.e(LOG_TAG, "Couldn't restart recording.");
        }
    }

    @Override
    protected void onResume()
    {
        super.onResume();
        try
        {
            soundRecorder.startRecording();
            try
            {
                recordingThread.start();
            }
            catch (IllegalThreadStateException ile)
            {
                Log.w(LOG_TAG, "Thread already started");
            }
        }
        catch (IllegalStateException ise)
        {
            Log.e(LOG_TAG, "Couldn't restart recording.");
        }
    }

    @Override
    protected void onPause()
    {
        super.onPause();
        try
        {
            soundRecorder.stop();
            recordingThread.interrupt();
        }
        catch (IllegalStateException ise)
        {
            Log.e(LOG_TAG, "Couldn't stop recording.");
        }
    }

    @Override
    protected void onStop()
    {
        super.onStop();
        try
        {
            soundRecorder.stop();
            recordingThread.interrupt();
        }
        catch (IllegalStateException ise)
        {
            Log.e(LOG_TAG, "Couldn't stop recording.");
        }
    }

    public void spectrometerOn(View v)
    {
        Intent intent = new Intent(Tuner.this, Spectrometer.class);
        startActivity(intent);
    }
}
