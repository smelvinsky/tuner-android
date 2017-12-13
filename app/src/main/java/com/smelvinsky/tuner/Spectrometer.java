package com.smelvinsky.tuner;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;

public class Spectrometer extends AppCompatActivity
{

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_spectrometer);
    }

    public void backToTuner(View v)
    {
        Intent intent = new Intent(Spectrometer.this, Tuner.class);
        startActivity(intent);
    }
}
