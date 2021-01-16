package com.example.coursecatchr;

import androidx.appcompat.app.AppCompatActivity;

import android.app.DownloadManager;

import android.content.Intent;
import android.os.Bundle;

import android.view.View;
import android.widget.Button;

public class MainActivity extends AppCompatActivity {

    private Button button;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Intent activity2Intent = new Intent(getApplicationContext(), ImageUpload.class);
        startActivity(activity2Intent);
    }




}


