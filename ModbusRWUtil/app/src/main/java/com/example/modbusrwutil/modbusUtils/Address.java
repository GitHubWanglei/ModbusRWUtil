package com.example.modbusrwutil.modbusUtils;

public class Address {

    private int deviceId;
    private int address; // 寄存器地址或起始地址
    private int readFuncId; // 读功能码： 0x01, 0x02, 0x03，0x04
    private int readCount; // 读几个寄存器, 0则不读
    private AVT[] readValueTypes; // 要读的每个值的类型
    private int writeFuncId; // 写功能码：0x05, 0x06, 0x0F, 0x10
    private int writeCount; // 写几个寄存器，0则不写
    private AVT[] writeValueTypes; // 要写的每个值的类型
    private String desc; // 寄存器描述

    private Object[] valueList; // 写操作时的传值，类型必须与writeValueTypes匹配

    public Address(int deviceId, int address,
                   int readFuncId, int readCount, AVT[] readValueTypes,
                   int writeFuncId, int writeCount, AVT[] writeValueTypes,
                   String desc) {
        this.deviceId = deviceId;
        this.address = address;
        this.readFuncId = readFuncId;
        this.readCount = readCount;
        this.readValueTypes = readValueTypes;
        this.writeFuncId = writeFuncId;
        this.writeCount = writeCount;
        this.writeValueTypes = writeValueTypes;
        this.desc = desc;
    }

    public long generateKey() {
        return getAddress() + (long) getDeviceId() * (10^5);
    }

    public byte[] getValueBytes(AVT valueType, Object value) {
        byte[] valueBytes = new byte[0];
        if (value instanceof Integer) {
            int i = ((Integer) value).intValue();
            if (valueType == AVT.t_byte) {
                valueBytes = new byte[]{(byte) i};
            } else if (valueType == AVT.t_short) {
                valueBytes = Modbus.shortToBytes((short) i);
            } else if (valueType == AVT.t_u_short) {
                valueBytes = Modbus.ushortToBytes(i);
            } else if (valueType == AVT.t_int) {
                valueBytes = Modbus.intToBytes(i);
            } else if (valueType == AVT.t_u_int) {
                long l = ((Integer) value).longValue();
                valueBytes = Modbus.uintToBytes(l);
            }
        } else if (value instanceof Long) {
            long l = ((Long) value).longValue();
            valueBytes = Modbus.longToBytes(l);
        } else if ((value instanceof Float) && valueType == AVT.t_float) {
            float f = ((Float) value).floatValue();
            valueBytes = Modbus.floatToBytes(f);
        } else if ((value instanceof Double) && valueType == AVT.t_double) {
            double d = ((Double) value).doubleValue();
            valueBytes = Modbus.doubleToBytes(d);
        }
        return valueBytes;
    }

    private byte[] getValueListBytes(Object[] valueList) {
        if (getWriteValueTypes().length != valueList.length) return new byte[]{};
        byte[] bytes = new byte[0];
        for (int i = 0; i < valueList.length; i++) {
            AVT valueType = getWriteValueTypes()[i];
            Object value = valueList[i];
            byte[] valueBytes = getValueBytes(valueType, value);
            byte[] newBytes = new byte[bytes.length + valueBytes.length];
            System.arraycopy(bytes, 0, newBytes, 0, bytes.length);
            System.arraycopy(valueBytes, 0, newBytes, bytes.length, valueBytes.length);
            bytes = newBytes;
        }
        return bytes;
    }

    public byte[] configWriteData() {
        byte[] bytes = getValueListBytes(valueList);
        if (bytes == null || bytes.length == 0) return new byte[]{};
        if (writeFuncId == 0x05) {
            return Modbus.configWriteSingleData_05_(getDeviceId(), getAddress(),bytes);
        } else if (writeFuncId == 0x06) {
            return Modbus.configWriteSingleData_06_(getDeviceId(), getAddress(),bytes);
        }  else if (writeFuncId == 0x0F) {
            return Modbus.configWriteMultipleData_0F_(getDeviceId(), getAddress(),getWriteCount(),bytes);
        } else if (writeFuncId == 0x10) {
            return Modbus.configWriteMultipleData_10_(getDeviceId(), getAddress(),getWriteCount(),bytes);
        }
        return new byte[]{};
    }

    // 写操作成功，期望返回的数据
    public byte[] configWriteReplyData() {
        if (writeFuncId == 0x05 || writeFuncId == 0x06) {
            return configWriteData();
        } else if (writeFuncId == 0x0F) {
            return Modbus.configWriteMultipleData_reply_0F_(getDeviceId(),getAddress(),getWriteCount());
        } else if (writeFuncId == 0x10) {
            return Modbus.configWriteMultipleData_reply_10_(getDeviceId(),getAddress(),getWriteCount());
        }
        return new byte[]{};
    }

    public byte[] configReadData() {
        return Modbus.configReadMultipleData(getDeviceId(),
                getReadFuncId(),
                getAddress(),
                getReadCount());
    }

    // 计算数值所占寄存器数量
    public static int getAddressCount(AVT type) {
        if (type.equals(AVT.t_byte) || type.equals(AVT.t_short) || type.equals(AVT.t_u_short)) { // 最长2个字节
            return 1;
        } else if (type.equals(AVT.t_int) || type.equals(AVT.t_u_int) || type.equals(AVT.t_float)) { // 最长4个字节
            return 2;
        } else { // long, double，最长8个字节
            return 4;
        }
    }

    public int getDeviceId() {
        return deviceId;
    }

    public void setDeviceId(int deviceId) {
        this.deviceId = deviceId;
    }

    public int getAddress() {
        return address;
    }

    public void setAddress(int address) {
        this.address = address;
    }

    public int getReadFuncId() {
        return readFuncId;
    }

    public void setReadFuncId(int readFuncId) {
        this.readFuncId = readFuncId;
    }

    public int getReadCount() {
        return readCount;
    }

    public void setReadCount(int readCount) {
        this.readCount = readCount;
    }

    public AVT[] getReadValueTypes() {
        return readValueTypes;
    }

    public void setReadValueTypes(AVT[] readValueTypes) {
        this.readValueTypes = readValueTypes;
    }

    public int getWriteFuncId() {
        return writeFuncId;
    }

    public void setWriteFuncId(int writeFuncId) {
        this.writeFuncId = writeFuncId;
    }

    public int getWriteCount() {
        return writeCount;
    }

    public void setWriteCount(int writeCount) {
        this.writeCount = writeCount;
    }

    public AVT[] getWriteValueTypes() {
        return writeValueTypes;
    }

    public void setWriteValueTypes(AVT[] writeValueTypes) {
        this.writeValueTypes = writeValueTypes;
    }

    public String getDesc() {
        return desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }

    public Object[] getValueList() {
        return valueList;
    }

    public void setValueList(Object[] valueList) {
        this.valueList = valueList;
    }
}


















