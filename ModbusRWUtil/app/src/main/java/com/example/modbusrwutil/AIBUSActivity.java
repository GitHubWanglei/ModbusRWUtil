package com.example.modbusrwutil;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.example.modbusrwutil.modbusUtils.Address;
import com.example.modbusrwutil.modbusUtils.AddressGroup;
import com.example.modbusrwutil.modbusUtils.ModbusManager;
import com.example.modbusrwutil.modbusUtils.YDAddress;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import tp.xmaihh.serialport.bean.ComBean;

public class AIBUSActivity extends AppCompatActivity {

    private EditText et_device_id;
    private EditText et_param_num;
    private EditText et_value;
    private TextView tv_response;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_aibusactivity);

        et_device_id = findViewById(R.id.aibus_device_id);
        et_param_num = findViewById(R.id.aibus_param_num);
        et_value = findViewById(R.id.aibus_value);
        tv_response = findViewById(R.id.tv_reponse);

        Button writeBtn = findViewById(R.id.aibus_btn_write);
        writeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (validateData()) {

                    Timer timer = new Timer();
                    timer.schedule(new TimerTask() {
                        @Override
                        public void run() {
                            new Handler(Looper.getMainLooper()).post(new Runnable() {
                                @Override
                                public void run() {
                                    CYToast.show(AIBUSActivity.this, "写入取超时", CYToast.ERROR);
                                }
                            });
                        }
                    }, 500);

                    tv_response.setText("");
                    int device_id = Integer.parseInt(et_device_id.getText().toString().trim());
                    int param_num = Integer.parseInt(et_param_num.getText().toString().trim());
                    int value = Integer.parseInt(et_value.getText().toString().trim());
                    List<Address> addressList = new ArrayList<>();
                    addressList.add(new YDAddress(device_id, param_num, value));
                    AddressGroup group = new AddressGroup("yd_write", addressList);
                    group.setListener(new AddressGroup.AddressGroupListener() {
                        @Override
                        public void onDataReceived(ComBean comBean, Address address, int[][] bitsArray, String[] values) {
                            if (!(address instanceof YDAddress)) return;
                            YDAddress ydAddress = (YDAddress) address;
                            if (ydAddress.getParamNo() == param_num) {
                                timer.cancel();
                                CYToast.show(AIBUSActivity.this, "写入成功", CYToast.SUCCESS);
                            }
                        }
                    });
                    ModbusManager.getInstance().sendWrite(group, false);
                }
            }
        });

        Button readBtn = findViewById(R.id.aibus_btn_read);
        readBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (validateData()) {

                    Timer timer = new Timer();
                    timer.schedule(new TimerTask() {
                        @Override
                        public void run() {
                            new Handler(Looper.getMainLooper()).post(new Runnable() {
                                @Override
                                public void run() {
                                    CYToast.show(AIBUSActivity.this, "读取超时", CYToast.ERROR);
                                }
                            });
                        }
                    }, 500);

                    tv_response.setText("");
                    int device_id = Integer.parseInt(et_device_id.getText().toString().trim());
                    int param_num = Integer.parseInt(et_param_num.getText().toString().trim());
                    int value = Integer.parseInt(et_value.getText().toString().trim());
                    List<Address> addressList = new ArrayList<>();
                    addressList.add(new YDAddress(device_id, param_num));
                    AddressGroup group = new AddressGroup("yd_read", addressList);
                    group.setListener(new AddressGroup.AddressGroupListener() {
                        @Override
                        public void onDataReceived(ComBean comBean, Address address, int[][] bitsArray, String[] values) {
                            if (!(address instanceof YDAddress)) return;
                            YDAddress ydAddress = (YDAddress) address;
                            if (ydAddress.getParamNo() == param_num) {
                                timer.cancel();
                                CYToast.show(AIBUSActivity.this, "读取成功", CYToast.SUCCESS);
                                tv_response.setText(ydAddress.parseValue()+"");
                            }
                        }
                    });
                    ModbusManager.getInstance().sendRead(group, false);
                }
            }
        });

    }

    private boolean validateData() {
        if (!et_device_id.getText().toString().trim().matches("\\d+$")) {
            CYToast.show(AIBUSActivity.this, "设备地址输入不合法", CYToast.ERROR);
            return false;
        }
        if (!et_value.getText().toString().trim().matches("^-?\\d*$")) {
            CYToast.show(AIBUSActivity.this, "写入值输入不合法", CYToast.ERROR);
            return false;
        }
        return true;
    }

}