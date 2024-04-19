package com.example.modbusrwutil.modbusUtils;

import android.os.Handler;
import android.os.Looper;

import com.example.modbusrwutil.logConfig.LogUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import tp.xmaihh.serialport.SerialHelper;
import tp.xmaihh.serialport.bean.ComBean;
import tp.xmaihh.serialport.utils.ByteUtil;

public class ModbusManager {

    private static final ModbusManager INSTANCE = new ModbusManager(); // 单例对象
    public SerialHelper serialHelper;

    private List<AddressGroup> loopData = new ArrayList<>();
    private AddressGroup writeAddressGroup = new AddressGroup("", new ArrayList<>());; // 写操作的地址信息 (不循环)
    private AddressGroup readAddressGroup = new AddressGroup("", new ArrayList<>());; // 读操作的地址信息 (不循环)
    private final ExecutorService loopThreadPool = Executors.newSingleThreadExecutor();
    private final ExecutorService singleThreadPool = Executors.newSingleThreadExecutor();
    private final Handler uihandler = new Handler(Looper.getMainLooper()); // 主线程handler
    private boolean needLoop = false;
    private boolean isLooping = false; // 是否正在循环，解析时使用
    private final Object lock = new Object();

    private boolean isWrite = false; // 是否是写操作
    private byte[] writeExpectReplyData; // 写操作期望返回的数据

    private long key; // 发送标识，用于区分是哪一次发送
    private boolean isYD; // 是否是宇电仪表
    private int timeout_modbus = 500;
    private int timeout_yd = 500;
    private int time_gap = 20;
    private boolean receivedSuccess = false;

    public static ModbusManager getInstance() {
        return INSTANCE;
    }

    private ModbusManager() {}

    public void initSerialPort(String sPort, int iBaudRate) {
        if (serialHelper == null) {
            serialHelper = new SerialHelper(sPort, iBaudRate) {
                @Override
                protected void onDataReceived(ComBean comBean) {
                    if (isYD) { // 宇电仪表
                        processYdReceiveData(comBean);
                    } else {
                        processReceiveData(comBean);
                    }
                }
            };
        }
    }

    public void openSerialPort() {
        if (serialHelper != null) {
            try {
                serialHelper.open();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void reInitSerialPort(String sPort, int iBaudRate) {
        closeSerialPort();
        serialHelper = null;
        serialHelper = new SerialHelper(sPort, iBaudRate) {
            @Override
            protected void onDataReceived(ComBean comBean) {
                if (isYD) { // 宇电仪表
                    processYdReceiveData(comBean);
                } else {
                    processReceiveData(comBean);
                }
            }
        };
        serialHelper.setStopBits(2);
        try {
            serialHelper.open();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void processYdReceiveData(ComBean comBean) {
        if (!ModbusStickPackageHelper.processYdDataIsComplete(comBean)) {
            return;
        }
        if (isWrite) {
//            LogUtils.log("test", "receive yd write bytes: " + ByteUtil.ByteArrToHex(comBean.bRec));
            for (int i = 0; i < writeAddressGroup.getAddressList().size(); i++) {
                Address address = writeAddressGroup.getAddressList().get(i);
                if ((address instanceof YDAddress) && isYD && key == ((YDAddress)address).getParamNo()) {
                    if (writeAddressGroup.getListener() != null) {
                        ((YDAddress) address).setReplyBytes(comBean.bRec);
                        uihandler.post(new Runnable() {
                            @Override
                            public void run() {
                                writeAddressGroup.getListener().onDataRecieved(comBean, (YDAddress)address, new int[][]{}, new String[]{});
                            }
                        });
                        lockNotify();
                        break;
                    }
                }
            }
        } else {
//            LogUtils.log("test", "receive yd read bytes: " + ByteUtil.ByteArrToHex(comBean.bRec));
            if (isLooping) { // 循环读取
                for (int i = 0; i < loopData.size(); i++) {
                    AddressGroup group = loopData.get(i);
                    for (int j = 0; j < group.getAddressList().size(); j++) {
                        Address address = group.getAddressList().get(j);
                        if ((address instanceof YDAddress) && isYD && key == ((YDAddress)address).getParamNo()) {
                            if (group.getListener() != null) {
                                ((YDAddress) address).setReplyBytes(comBean.bRec);
                                uihandler.post(new Runnable() {
                                    @Override
                                    public void run() {
                                        group.getListener().onDataRecieved(comBean, (YDAddress)address, new int[][]{}, new String[]{});
                                    }
                                });
                                lockNotify();
                                break;
                            }
                        }
                    }
                }
            } else if (readAddressGroup.getAddressList().size() > 0) { // 一次性读取
                for (int j = 0; j < readAddressGroup.getAddressList().size(); j++) {
                    Address address = readAddressGroup.getAddressList().get(j);
                    if ((address instanceof YDAddress) && isYD && key == ((YDAddress)address).getParamNo()) {
                        if (readAddressGroup.getListener() != null) {
                            ((YDAddress) address).setReplyBytes(comBean.bRec);
                            uihandler.post(new Runnable() {
                                @Override
                                public void run() {
                                    readAddressGroup.getListener().onDataRecieved(comBean, (YDAddress)address, new int[][]{}, new String[]{});
                                }
                            });
                            lockNotify();
                            break;
                        }
                    }
                }
            }

        }
    }

    private void processReceiveData(ComBean comBean) {
        if (isWrite) {
            if (!ModbusStickPackageHelper.processModbusWriteReplyDataIsComplete(comBean, writeExpectReplyData)) { // 拼接数据
                return;
            }
//            LogUtils.log("test", "receive write bytes: "+ ByteUtil.ByteArrToHex(comBean.bRec));

            for (int i = 0; i < writeAddressGroup.getAddressList().size(); i++) {
                Address address = writeAddressGroup.getAddressList().get(i);
                if (!(address instanceof YDAddress) && !isYD && key == address.getAddress()) {
                    if (writeAddressGroup.getListener() != null) {
                        uihandler.post(new Runnable() {
                            @Override
                            public void run() {
                                writeAddressGroup.getListener().onDataRecieved(comBean, address, new int[][]{}, new String[]{});
                            }
                        });
                        lockNotify();
                        break;
                    }
                }
            }

        } else {
//            LogUtils.log("test", "receive read bytes: "+ ByteUtil.ByteArrToHex(comBean.bRec));
            if (!ModbusStickPackageHelper.processModbusReadReplyDataIsComplete(comBean)) { // 拼接数据
                return;
            }
            if (isLooping) { // 循环读操作
                for (int i = 0; i < loopData.size(); i++) {
                    AddressGroup group = loopData.get(i);
                    processReceivedReadBytes(group,comBean);
                }
            } else if (readAddressGroup.getAddressList().size() > 0) { // 一次性读操作
                processReceivedReadBytes(readAddressGroup,comBean);
            }
        }
    }

    private void processReceivedReadBytes(AddressGroup group, ComBean comBean) {
        for (int j = 0; j < group.getAddressList().size(); j++) {
            Address address = group.getAddressList().get(j);
            if (key == address.getAddress() && comBean.bRec[1] == address.getReadFuncId()) {
                if (address.getReadFuncId() == 0x01 || address.getReadFuncId() == 0x02) { // 位操作
                    int[][] bitsArray = Modbus.parseByteArrayToBitsArray(comBean.bRec);
                    if (group.getListener() != null) {
                        uihandler.post(new Runnable() {
                            @Override
                            public void run() {
                                group.getListener().onDataRecieved(comBean, address, bitsArray, new String[]{}); // 回调
                            }
                        });
                    }
                    lockNotify();
                } else if ((address.getReadFuncId() == 0x03 || address.getReadFuncId() == 0x04) && comBean.bRec[1] == address.getReadFuncId()) { // 字操作
                    int byteCount = comBean.bRec[2]; // 字节数
                    int expectByteCount = 0; // 预期返回的字节数
                    for (int k = 0; k < address.getReadValueTypes().length; k++) {
                        expectByteCount += Address.getAddressCount(address.getReadValueTypes()[k]) * 2;
                    }
                    if (byteCount == expectByteCount) { // 返回字节数与预期长度一致
                        int startIdx = 3;
                        List<String> values = new ArrayList<>();
                        for (int k = 0; k < address.getReadValueTypes().length; k++) {
                            AVT type = address.getReadValueTypes()[k];
                            int registerCount = Address.getAddressCount(type); // 根据数据类型计算所占寄存器数量
                            byte[] valueBytes = new byte[registerCount * 2];
                            System.arraycopy(comBean.bRec, startIdx, valueBytes, 0, registerCount * 2);
                            startIdx += registerCount * 2;
                            // 数值解析
                            if ((type == AVT.t_byte || type == AVT.t_short) && registerCount == 1) {
                                short value = Modbus.bytesToShort(valueBytes);
                                values.add(Short.toString(value));
                            } else if (type == AVT.t_u_short && registerCount == 1) {
                                int value = Modbus.bytesToUshort(valueBytes);
                                values.add(Integer.toString(value));
                            } else if (type == AVT.t_int && registerCount == 2) {
                                int value = Modbus.bytesToInt(valueBytes);
                                values.add(Integer.toString(value));
                            } else if (type == AVT.t_u_int && registerCount == 2) {
                                long value = Modbus.bytesToUint(valueBytes);
                                values.add(Long.toString(value));
                            } else if (type == AVT.t_float && registerCount == 2) {
                                float value = Modbus.bytesToFloat(valueBytes);
                                values.add(Float.toString(value));
                            }   else if (type == AVT.t_long && registerCount == 4) {
                                long value = Modbus.bytesToLong(valueBytes);
                                values.add(Long.toString(value));
                            } else if (type == AVT.t_double && registerCount == 4)  {
                                double value = Modbus.bytesToDouble(valueBytes);
                                values.add(Double.toString(value));
                            }
                        }
                        String[] stringValueArray = new String[values.size()];
                        values.toArray(stringValueArray);
                        if (group.getListener() != null) {
                            uihandler.post(new Runnable() {
                                @Override
                                public void run() {
                                    group.getListener().onDataRecieved(comBean, address, new int[][]{}, stringValueArray); // 回调
                                }
                            });
                        }
                        lockNotify();
                    }
                }
            }
        }
    }

    private void performTaskAfterLoopStop(Runnable runnable, boolean needLoopAfterCompleted) {
        singleThreadPool.execute(new Runnable() {
            @Override
            public void run() {
                needLoop = false;
                // 此处会阻塞等待循环真正停止后，才执行runnable任务
                loopThreadPool.execute(runnable);
                // 执行完继续循环
                if (needLoopAfterCompleted) {
                    loopThreadPool.execute(new Runnable() {
                        @Override
                        public void run() {
                            startLoopRead();
                        }
                    });
                }
            }
        });
    }

    public void startLoop() {
        loopThreadPool.execute(new Runnable() {
            @Override
            public void run() {
                startLoopRead();
            }
        });
    }

    public void stopLoop() {
        needLoop = false;
    }

    public void addLoopData(List<AddressGroup> data) {
        addLoopData(data, true);
    }

    public void addLoopData(List<AddressGroup> data, boolean needLoopAfterCompleted) {
        performTaskAfterLoopStop(new Runnable() {
            @Override
            public void run() {
                boolean isExist = false;
                for (int idx = 0; idx < data.size(); idx++) {
                    AddressGroup addressGroup = data.get(idx);
                    for (int i = 0; i < loopData.size(); i++) {
                        if (loopData.get(i).getIdentifier().equals(addressGroup.getIdentifier())) {
                            isExist = true;
                            break;
                        }
                    }
                    if (!isExist) {
                        loopData.add(addressGroup);
                    }
                    isExist = false;
                }
            }
        }, needLoopAfterCompleted);
    }

    public void removeLoopData(List<AddressGroup> data) {
        removeLoopData(data, true);
    }

    public void removeLoopData(List<AddressGroup> data, boolean needLoopAfterCompleted) {
        performTaskAfterLoopStop(new Runnable() {
            @Override
            public void run() {
                for (int idx = 0; idx < data.size(); idx++) {
                    AddressGroup addressGroup = data.get(idx);
                    for (int i = 0; i < loopData.size(); i++) {
                        if (loopData.get(i).getIdentifier().equals(addressGroup.getIdentifier())) {
                            loopData.remove(i);
                            break;
                        }
                    }
                }
            }
        }, needLoopAfterCompleted);
    }

    public void removeAllLoopData() {
        performTaskAfterLoopStop(new Runnable() {
            @Override
            public void run() {
                loopData.clear();
            }
        }, false);
    }

    public void sendWrite(AddressGroup data) {
        sendWrite(data, true);
    }

    public void sendWrite(AddressGroup data, boolean needLoopAfterCompleted) {
        performTaskAfterLoopStop(new Runnable() {
            @Override
            public void run() {
                LogUtils.log("test", "开始写操作：============================================================================");
                writeAddressGroup = data;
                for (int i = 0; i < data.getAddressList().size(); i++) {
                    Address address = data.getAddressList().get(i);
                    isWrite = true;
                    byte[] bytes;
                    if (address instanceof YDAddress) { // 宇电AIBUS协议
                        isYD = true;
                        bytes = ((YDAddress) address).configWriteData();
                        key = ((YDAddress) address).getParamNo();
                    } else { // Modbus协议
                        isYD = false;
                        bytes = address.configWriteData();
                        writeExpectReplyData = address.configWriteReplyData();
                        key = address.getAddress();
                    }
                    serialHelper.send(bytes);
                    LogUtils.log("test", "send write bytes: "+ByteUtil.ByteArrToHex(bytes));

                    waitUntilSuccessOrTimeout();
                }
                isWrite = false;
                LogUtils.log("test", "写操作结束：============================================================================");
            }
        }, needLoopAfterCompleted);
    }

    public void sendRead(AddressGroup data) {
        sendRead(data, true);
    }

    public void sendRead(AddressGroup data, boolean needLoopAfterCompleted) {
        performTaskAfterLoopStop(new Runnable() {
            @Override
            public void run() {
                readAddressGroup = data;
                LogUtils.log("test", "group ");
                for (int i = 0; i < data.getAddressList().size(); i++) {
                    Address address = data.getAddressList().get(i);
                    byte[] bytes;
                    if (address instanceof YDAddress) { // 宇电AIBUS协议
                        isYD = true;
                        bytes = ((YDAddress) address).configReadData();
                        key = ((YDAddress) address).getParamNo();
                    } else { // Modbus协议
                        isYD = false;
                        bytes = address.configReadData();
                        key = address.getAddress();
                    }

                    serialHelper.send(bytes);

                    LogUtils.log("test", "send read data: "+ByteUtil.ByteArrToHex(bytes));

                    waitUntilSuccessOrTimeout();
                }
            }
        }, needLoopAfterCompleted);
    }

    private void startLoopRead() {
        if (loopData.size() == 0 || isLooping) {
            return;
        }
        needLoop = true;
        isLooping = true;
        while (needLoop) {
            for (int i = 0; i < loopData.size(); i++) {
                AddressGroup group = loopData.get(i);
                LogUtils.log("test", "group ");
                for (int j = 0; group != null && j < group.getAddressList().size(); j++) {
                    Address address = group.getAddressList().get(j);
                    byte[] bytes;
                    if (address instanceof YDAddress) { // 宇电AIBUS协议
                        isYD = true;
                        bytes = ((YDAddress) address).configReadData();
                        key = ((YDAddress) address).getParamNo();
                    } else { // Modbus协议
                        isYD = false;
                        bytes = address.configReadData();
                        key = address.getAddress();
                    }

                    serialHelper.send(bytes);

                    LogUtils.log("test", "send read data: " + ByteUtil.ByteArrToHex(bytes));

                    waitUntilSuccessOrTimeout();

                    if (!needLoop) {
                        break;
                    }
                }
                if (!needLoop) {
                    break;
                }
            }
        }
        isLooping = false;
    }

    private void waitUntilSuccessOrTimeout() {
        receivedSuccess = false;
        synchronized (lock) {
            try {
                lock.wait(isYD ? timeout_yd : timeout_modbus);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        if (receivedSuccess) {
            sleep(time_gap); // 成功，等待20ms再发送下一次操作
        }
    }

    private void lockNotify() {
        receivedSuccess = true;
        synchronized (lock) {
            lock.notifyAll();
        }
    }

    private void sleep(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public final void closeSerialPort() {
        if (serialHelper != null && serialHelper.isOpen()) {
            serialHelper.close();
        }
        serialHelper = null;
    }

    public void setTimeout_modbus(int timeout_modbus) {
        this.timeout_modbus = timeout_modbus;
    }

    public void setTimeout_yd(int timeout_yd) {
        this.timeout_yd = timeout_yd;
    }

    public void setTime_gap(int time_gap) {
        this.time_gap = time_gap;
    }
}
