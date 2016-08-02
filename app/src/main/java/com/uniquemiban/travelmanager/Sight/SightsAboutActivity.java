package com.uniquemiban.travelmanager.Sight;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.widget.TextView;

import com.uniquemiban.travelmanager.R;

public class SightsAboutActivity extends AppCompatActivity {
    TextView aboutText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sights_about);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        aboutText = (TextView) findViewById(R.id.about_text);

        setAboutContent();
    }

    public void setAboutContent() {


    }
}
