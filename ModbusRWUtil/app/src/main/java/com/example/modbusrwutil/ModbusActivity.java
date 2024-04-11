package com.example.modbusrwutil;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

import com.example.modbusrwutil.logConfig.LogUtils;
import com.example.modbusrwutil.modbusUtils.AVT;
import com.example.modbusrwutil.modbusUtils.Address;
import com.example.modbusrwutil.modbusUtils.AddressGroup;
import com.example.modbusrwutil.modbusUtils.ModbusManager;


import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import tp.xmaihh.serialport.bean.ComBean;

public class ModbusActivity extends AppCompatActivity {

    private EditText et_device_id;
    private int functionId;
    EditText et_register;
    EditText et_register_count;
    TextView tv_value_title;
    EditText et_value;
    TextView tv_response;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_modbus);

        et_device_id = findViewById(R.id.modbus_device_id);
        et_register = findViewById(R.id.modubs_register);
        et_register_count = findViewById(R.id.modbus_register_count);
        tv_value_title = findViewById(R.id.modbus_value_title);
        et_value = findViewById(R.id.modbus_value);
        tv_response = findViewById(R.id.tv_reponse);
        Spinner spinner = findViewById(R.id.modbus_spinner_function_id);
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                int[] array = new int[]{0x01, 0x02, 0x03, 0x04, 0x05, 0x06, 0x0F, 0x10};
                functionId = array[position];
                if (functionId == 1 || functionId == 2 || functionId == 3 || functionId == 4) {
                    tv_value_title.setVisibility(View.INVISIBLE);
                    et_value.setVisibility(View.INVISIBLE);
                } else {
                    tv_value_title.setVisibility(View.VISIBLE);
                    et_value.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        Button sendBtn = findViewById(R.id.modbus_send);
        sendBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (et_device_id.getText().toString().trim().length() <= 0 ||
                        et_register.getText().toString().trim().length() <= 0 ||
                        et_register_count.getText().toString().trim().length() <= 0) {
                    CYToast.show(ModbusActivity.this, "缺少必填参数", CYToast.ERROR);
                    return;
                }
                if (et_value.getVisibility() == View.VISIBLE && et_value.getText().toString().trim().length() <= 0) {
                    CYToast.show(ModbusActivity.this, "缺少必填参数", CYToast.ERROR);
                    return;
                }
                String regex = "-?[1-9]\\d*$";
                if (!et_device_id.getText().toString().trim().matches("\\d*$")) {
                    CYToast.show(ModbusActivity.this, "设备地址输入不合法", CYToast.ERROR);
                    return;
                }
                if (!et_register.getText().toString().trim().matches("\\d*$")) {
                    CYToast.show(ModbusActivity.this, "寄存器起始地址输入不合法", CYToast.ERROR);
                    return;
                }
                if (!et_register_count.getText().toString().trim().matches("[1-9]\\d*")) {
                    CYToast.show(ModbusActivity.this, "寄存器数量输入不合法", CYToast.ERROR);
                    return;
                }

                int deviceId = Integer.parseInt(et_device_id.getText().toString().trim());
                int register = Integer.parseInt(et_register.getText().toString().trim());
                int register_count = Integer.parseInt(et_register_count.getText().toString().trim());

                if (et_value.getVisibility() == View.VISIBLE) {
                    String[] strArray = et_value.getText().toString().trim().split(" ");
                    for (int i = 0; i < strArray.length; i++) {
                        String str = strArray[i];
                        if (!str.trim().matches("^-?\\d*$")) {
                            CYToast.show(ModbusActivity.this, "写入值不合法", CYToast.ERROR);
                            LogUtils.log("test", str.trim());
                            return;
                        }
                    }
                    if (strArray.length != register_count) {
                        CYToast.show(ModbusActivity.this, "写入值个数输入不正确", CYToast.ERROR);
                        return;
                    }
                }

                AVT[] avtArray = new AVT[register_count];
                for (int i = 0; i < register_count; i++) {
                    avtArray[i] = AVT.t_short; // 两个字节
                }

                Timer timer = new Timer();
                timer.schedule(new TimerTask() {
                    @Override
                    public void run() {
                        new Handler(Looper.getMainLooper()).post(new Runnable() {
                            @Override
                            public void run() {
                                String msg = (et_value.getVisibility() == View.INVISIBLE) ? "读取超时" : "写入超时";
                                CYToast.show(ModbusActivity.this, msg, CYToast.ERROR);
                            }
                        });
                    }
                }, 500);

                tv_response.setText("");
                if (et_value.getVisibility() == View.INVISIBLE) { // 读
                    Address address = new Address(deviceId,register,
                            functionId,register_count,avtArray,
                            0,0,null,
                            "0");
                    List<Address> list = new ArrayList<>();
                    list.add(address);
                    AddressGroup group = new AddressGroup("read", list);
                    group.setListener(new AddressGroup.AddressGroupListener() {
                        @Override
                        public void onDateRecieved(ComBean comBean, Address address, int[][] bitsArray, String[] values) {
                            if (address.getAddress() == register) {
                                timer.cancel();
                                String str = "";
                                if (bitsArray.length > 0 && bitsArray[0].length > 0) {
                                    for (int i = 0; i < bitsArray[0].length; i++) {
                                        str += ", "+bitsArray[0][i];
                                    }
                                } else if (values.length > 0) {
                                    for (int i = 0; i < values.length; i++) {
                                        str += ", "+values[i];
                                    }
                                }
                                tv_response.setText(str);
                            }
                        }
                    });
                    ModbusManager.getInstance().sendRead(group, false);
                } else { // 写
                    Address address = new Address(deviceId,register,
                            0,0,null,
                            functionId,register_count,avtArray,
                            "0");

                    String[] strArray = et_value.getText().toString().trim().split(" ");
                    Integer[] value = new Integer[strArray.length];
                    for (int i = 0; i < strArray.length; i++) {
                        value[i] = Integer.parseInt(strArray[i].trim());
                    }
                    address.setValueList(value);

                    List<Address> list = new ArrayList<>();
                    list.add(address);
                    AddressGroup group = new AddressGroup("read", list);
                    group.setListener(new AddressGroup.AddressGroupListener() {
                        @Override
                        public void onDateRecieved(ComBean comBean, Address address, int[][] bitsArray, String[] values) {
                            if (address.getAddress() == register) {
                                timer.cancel();
                                String str = "";
                                if (bitsArray.length > 0 && bitsArray[0].length > 0) {
                                    for (int i = 0; i < bitsArray[0].length; i++) {
                                        str += ", "+bitsArray[0][i];
                                    }
                                } else if (values.length > 0) {
                                    for (int i = 0; i < values.length; i++) {
                                        str += ", "+values[i];
                                    }
                                }
                                tv_response.setText(str);
                            }
                        }
                    });
                    ModbusManager.getInstance().sendWrite(group, false);
                }

            }
        });


    }
}