package com.example.modbusrwutil;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Path;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.Spinner;

import com.example.modbusrwutil.logConfig.LogUtils;
import com.example.modbusrwutil.modbusUtils.ModbusManager;


import butterknife.BindView;
import butterknife.ButterKnife;

public class MainActivity extends AppCompatActivity {

    @SuppressLint("NonConstantResourceId")
    @BindView(R.id.main_btn_modbus)
    public Button btn_modbus;
    @BindView(R.id.main_btn_aibus)
    public Button btn_aibus;
    @BindView(R.id.main_btn_close_serialport)
    public Button close_btn;
    @BindView(R.id.main_btn_open_serialport)
    public Button open_btn;

    @BindView(R.id.main_path_view)
    public FlowPathView pathView;

    private int baudrate;
    private int stopbits;
    private int databits;
    private int parity;

    private boolean start = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);

        btn_modbus.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, ModbusActivity.class);
                startActivity(intent);
            }
        });
        btn_aibus.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, AIBUSActivity.class);
                startActivity(intent);
            }
        });

        btn_modbus.setEnabled(false);
        btn_modbus.setBackground(getApplication().getDrawable(R.drawable.btn_disable_bg));
        btn_aibus.setEnabled(false);
        btn_aibus.setBackground(getApplication().getDrawable(R.drawable.btn_disable_bg));

        close_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ModbusManager.getInstance().closeSerialPort();
                btn_modbus.setEnabled(false);
                btn_modbus.setBackground(getApplication().getDrawable(R.drawable.btn_disable_bg));
                btn_aibus.setEnabled(false);
                btn_aibus.setBackground(getApplication().getDrawable(R.drawable.btn_disable_bg));
                close_btn.setEnabled(false);
                close_btn.setBackground(getApplication().getDrawable(R.drawable.btn_disable_bg));
                open_btn.setEnabled(true);
                open_btn.setBackground(getApplication().getDrawable(R.drawable.btn_bg));
            }
        });
        close_btn.setEnabled(false);
        close_btn.setBackground(getApplication().getDrawable(R.drawable.btn_disable_bg));

        open_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ModbusManager.getInstance().openSerialPort("dev/ttyS2", baudrate, databits, stopbits, parity);
                btn_modbus.setEnabled(true);
                btn_modbus.setBackground(getApplication().getDrawable(R.drawable.btn_bg));
                btn_aibus.setEnabled(true);
                btn_aibus.setBackground(getApplication().getDrawable(R.drawable.btn_bg));
                close_btn.setEnabled(true);
                close_btn.setBackground(getApplication().getDrawable(R.drawable.btn_bg));
                open_btn.setEnabled(false);
                open_btn.setBackground(getApplication().getDrawable(R.drawable.btn_disable_bg));
            }
        });

        Spinner spinner_baudrate = findViewById(R.id.main_spinner_baudrate);
        spinner_baudrate.setSelection(1);
        spinner_baudrate.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                int[] array = new int[]{4800, 9600, 19200, 38400, 57600, 115200};
                LogUtils.log("test", "baudrate: "+array[position]);
                baudrate = array[position];
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
        Spinner spinner_databits = findViewById(R.id.main_spinner_databits);
        spinner_databits.setSelection(3);
        spinner_databits.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                int[] array = new int[]{5, 6, 7, 8};
                LogUtils.log("test", "databits: "+array[position]);
                databits = array[position];
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
        Spinner spinner_parity = findViewById(R.id.main_spinner_parity);
        spinner_parity.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                int[] array = new int[]{0, 1, 2};
                LogUtils.log("test", "parity: "+array[position]);
                parity = array[position];
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
        Spinner spinner_stopbits = findViewById(R.id.main_spinner_stopbits);
        spinner_stopbits.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                int[] array = new int[]{1, 2};
                LogUtils.log("test", "stopbits: "+array[position]);
                stopbits = array[position];
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

//        pathView.setVisibility(View.INVISIBLE);
        drawPathAnimationTest();

    }

    private void drawPathAnimationTest() {

        Path path = new Path();
        path.moveTo(50, 50);
        path.lineTo(50, 150);
        path.lineTo(200, 150);
        path.lineTo(200, 50);
        path.lineTo(250, 50);
        path.lineTo(250, 150);
        path.lineTo(300, 150);
        path.lineTo(300, 100);
        path.lineTo(350, 100);
        path.lineTo(350, 150);


        pathView.setPath(path);
        pathView.setPathWidth(5);
        pathView.setIntervals(new float[]{5, 10});
        pathView.setCornerRadius(30);
        pathView.setSpeed(-1L);
//        pathView.setPathColor(Color.RED);
//        pathView.setPathBackgroundWidth(15);
        pathView.setPathBackgroundColor(Color.BLACK);
        pathView.setBackgroundColor(0x00000000);
        pathView.setClickable(true);
        pathView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                start = !start;
                if (start) {
                    pathView.startFlowAnimation();
                } else {
                    pathView.stopFlowAnimation();
                }

                Intent intent = new Intent(MainActivity.this, TestActivity.class);
                startActivity(intent);
            }
        });

    }


}