package com.example.modbusrwutil;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.TextureView;

import butterknife.BindView;

public class TestActivity extends AppCompatActivity {

    @BindView(R.id.textureView)
    public TextureView textureView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test);

        

    }
}