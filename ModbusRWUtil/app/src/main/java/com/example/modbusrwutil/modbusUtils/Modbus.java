package com.example.modbusrwutil.modbusUtils;

import java.nio.ByteBuffer;

public class Modbus {

    public static byte[] configReadMultipleData(int id,
                                                int funcId,
                                                int registerStartAddress,
                                                int registerCount) {
        if (funcId == 0x01) {
            return configReadMultipleData_01_(id, registerStartAddress, registerCount);
        } else if (funcId == 0x02) {
            return configReadMultipleData_02_(id, registerStartAddress, registerCount);
        } else if (funcId == 0x03) {
            return configReadMultipleData_03_(id, registerStartAddress, registerCount);
        } else if (funcId == 0x04) {
            return configReadMultipleData_04_(id, registerStartAddress, registerCount);
        }
        return null;
    }

    // 功能码01H读取Modbus从机中线圈寄存器的状态，可以是单个寄存器，或者多个连续的寄存器。
    public static byte[] configReadMultipleData_01_(int id,
                                                    int registerStartAddress,
                                                    int registerCount) {
        byte[] data = new byte[6];
        data[0] = (byte) id;
        data[1] = 0x01;
        data[2] = highByte(registerStartAddress);
        data[3] = lowByte(registerStartAddress);
        data[4] = highByte(registerCount);
        data[5] = lowByte(registerCount);
        int crc = getCRC(data);
        byte[] bytes = new byte[8];
        System.arraycopy(data, 0, bytes, 0, data.length);
        bytes[6] = lowByte(crc);
        bytes[7] = highByte(crc);
        return bytes;
    }

    // 功能码02H读取Modbus从机中离散输入寄存器的状态，可以是单个寄存器，或者多个连续的寄存器。
    public static byte[] configReadMultipleData_02_(int id,
                                                    int registerStartAddress,
                                                    int registerCount) {
        byte[] data = new byte[6];
        data[0] = (byte) id;
        data[1] = 0x02;
        data[2] = highByte(registerStartAddress);
        data[3] = lowByte(registerStartAddress);
        data[4] = highByte(registerCount);
        data[5] = lowByte(registerCount);
        int crc = getCRC(data);
        byte[] bytes = new byte[8];
        System.arraycopy(data, 0, bytes, 0, data.length);
        bytes[6] = lowByte(crc);
        bytes[7] = highByte(crc);
        return bytes;
    }

    // 功能码03H读取Modbus从机中保持寄存器的数据，可以是单个寄存器，或者多个连续的寄存器。
    public static byte[] configReadMultipleData_03_(int id,
                                                    int registerStartAddress,
                                                    int registerCount) {
        byte[] data = new byte[6];
        data[0] = (byte) id;
        data[1] = 0x03;
        data[2] = highByte(registerStartAddress);
        data[3] = lowByte(registerStartAddress);
        data[4] = highByte(registerCount);
        data[5] = lowByte(registerCount);
        int crc = getCRC(data);
        byte[] bytes = new byte[8];
        System.arraycopy(data, 0, bytes, 0, data.length);
        bytes[6] = lowByte(crc);
        bytes[7] = highByte(crc);
        return bytes;
    }

    // 功能码04H读取Modbus从机中输入寄存器的数据，可以是单个寄存器，或者多个连续的寄存器。
    public static byte[] configReadMultipleData_04_(int id,
                                                    int registerStartAddress,
                                                    int registerCount) {
        byte[] data = new byte[6];
        data[0] = (byte) id;
        data[1] = 0x04;
        data[2] = highByte(registerStartAddress);
        data[3] = lowByte(registerStartAddress);
        data[4] = highByte(registerCount);
        data[5] = lowByte(registerCount);
        int crc = getCRC(data);
        byte[] bytes = new byte[8];
        System.arraycopy(data, 0, bytes, 0, data.length);
        bytes[6] = lowByte(crc);
        bytes[7] = highByte(crc);
        return bytes;
    }

    // 功能码05H写单个线圈寄存器，FF00H请求线圈处于ON状态，0000H请求线圈处于OFF状态。
    public static byte[] configWriteSingleData_05_(int id,
                                                   int registerStartAddress,
                                                   byte[] value) {
        byte[] data = new byte[4 + value.length];
        data[0] = (byte) id;
        data[1] = 0x05;
        data[2] = highByte(registerStartAddress);
        data[3] = lowByte(registerStartAddress);
        System.arraycopy(value, 0, data, 4, value.length);
        int crc = getCRC(data);
        byte[] bytes = new byte[data.length + 2];
        System.arraycopy(data, 0, bytes, 0, data.length);
        bytes[4 + value.length] = lowByte(crc);
        bytes[4 + value.length + 1] = highByte(crc);
        return bytes;
    }

    // 功能码06H写单个保持寄存器。
    public static byte[] configWriteSingleData_06_(int id,
                                                   int registerAddress,
                                                   byte[] value) {
        byte[] data = new byte[4 + value.length];
        data[0] = (byte) id;
        data[1] = 0x06;
        data[2] = highByte(registerAddress);
        data[3] = lowByte(registerAddress);
        System.arraycopy(value, 0, data, 4, value.length);
        int crc = getCRC(data);
        byte[] bytes = new byte[data.length + 2];
        System.arraycopy(data, 0, bytes, 0, data.length);
        bytes[4 + value.length] = lowByte(crc);
        bytes[4 + value.length + 1] = highByte(crc);
        return bytes;
    }

    // 功能码0FH写多个线圈寄存器。如果对应的数据位为1，表示线圈状态为ON；如果对应的数据位为0，表示线圈状态为OFF。
    // 线圈寄存器之间，低地址寄存器先传输，高地址寄存器后传输。
    // 单个线圈寄存器，高字节数据先传输，低字节数据后传输。
    // 如果写入的线圈寄存器的个数不是8的倍数，则在最后一个字节的高位补0。
    public static byte[] configWriteMultipleData_0F_(int id,
                                                     int registerStartAddress,
                                                     int registerCount,
                                                     byte[] values) {
        byte[] data = new byte[7 + values.length];
        data[0] = (byte) id;
        data[1] = 0x0F;
        data[2] = highByte(registerStartAddress);
        data[3] = lowByte(registerStartAddress);
        data[4] = highByte(registerCount);
        data[5] = lowByte(registerCount);
        data[6] = (byte) (values.length); // 字节数
        System.arraycopy(values, 0, data, 7, values.length);
        int crc = getCRC(data);
        byte[] bytes = new byte[data.length + 2];
        System.arraycopy(data, 0, bytes, 0, data.length);
        bytes[7 + values.length] = lowByte(crc);
        bytes[7 + values.length + 1] = highByte(crc);
        return bytes;
    }

    public static byte[] configWriteMultipleData_reply_0F_(int id,
                                                           int registerStartAddress,
                                                           int registerCount) {
        byte[] data = new byte[6];
        data[0] = (byte) id;
        data[1] = 0x0F;
        data[2] = highByte(registerStartAddress);
        data[3] = lowByte(registerStartAddress);
        data[4] = highByte(registerCount);
        data[5] = lowByte(registerCount);
        int crc = getCRC(data);
        byte[] bytes = new byte[data.length + 2];
        System.arraycopy(data, 0, bytes, 0, data.length);
        bytes[6] = lowByte(crc);
        bytes[7] = highByte(crc);
        return bytes;
    }

    // 功能码10H写多个保持寄存器，其中每个保持寄存器的长度为两个字节。
    public static byte[] configWriteMultipleData_10_(int id,
                                                     int registerStartAddress,
                                                     int registerCount,
                                                     byte[] values) {
        byte[] data = new byte[7 + values.length];
        data[0] = (byte) id;
        data[1] = 0x10;
        data[2] = highByte(registerStartAddress);
        data[3] = lowByte(registerStartAddress);
        data[4] = highByte(registerCount);
        data[5] = lowByte(registerCount);
        data[6] = (byte) (values.length); // 字节数
        System.arraycopy(values, 0, data, 7, values.length);
        int crc = getCRC(data);
        byte[] bytes = new byte[data.length + 2];
        System.arraycopy(data, 0, bytes, 0, data.length);
        bytes[7 + values.length] = lowByte(crc);
        bytes[7 + values.length + 1] = highByte(crc);
        return bytes;
    }

    public static byte[] configWriteMultipleData_reply_10_(int id,
                                                     int registerStartAddress,
                                                     int registerCount) {
        byte[] data = new byte[6];
        data[0] = (byte) id;
        data[1] = 0x10;
        data[2] = highByte(registerStartAddress);
        data[3] = lowByte(registerStartAddress);
        data[4] = highByte(registerCount);
        data[5] = lowByte(registerCount);
        int crc = getCRC(data);
        byte[] bytes = new byte[data.length + 2];
        System.arraycopy(data, 0, bytes, 0, data.length);
        bytes[6] = lowByte(crc);
        bytes[7] = highByte(crc);
        return bytes;
    }

    // 取十六进制(2个字节长度)中的高两位
    public static byte highByte(int value) {
        return (byte) ((value & 0xFF00) >> 8);
    }

    // 取十六进制(2个字节长度)中的低两位
    public static byte lowByte(int value) {
        return (byte) (value & 0x00FF);
    }

    public static int getCRC(byte[] data) {
        int crc = 0xFFFF;
        for (byte b : data) {
            crc ^= (int) b & 0xFF;
            for (int i = 0; i < 8; i++) {
                if ((crc & 0x0001) != 0) {
                    crc = (crc >> 1) ^ 0xA001;
                } else {
                    crc = crc >> 1;
                }
            }
        }
        return crc;
    }

    // long转字节数组
    public static  byte[] longToBytes(long i) {
        ByteBuffer byteBuffer = ByteBuffer.allocate(8);
        byteBuffer.putLong(i);
        return byteBuffer.array();
    }

    // int转字节数组
    public static  byte[] intToBytes(int i) {
        ByteBuffer byteBuffer = ByteBuffer.allocate(4);
        byteBuffer.putInt(i);
        return byteBuffer.array();
    }

    // short转字节数组
    public static byte[] shortToBytes(short i) {
        ByteBuffer byteBuffer = ByteBuffer.allocate(2);
        byteBuffer.putShort(i);
        return byteBuffer.array();
    }

    // float转字节数组
    public static byte[] floatToBytes(float f) {
        ByteBuffer byteBuffer = ByteBuffer.allocate(4);
        byteBuffer.putFloat(f);
        return byteBuffer.array();
    }

    // double转字节数组
    public static byte[] doubleToBytes(double d) {
        ByteBuffer byteBuffer = ByteBuffer.allocate(8);
        byteBuffer.putDouble(d);
        return byteBuffer.array();
    }

    // 字节数组转float
    public static float bytesToFloat(byte[] bytes) {
        ByteBuffer byteBuffer = ByteBuffer.wrap(bytes);
        return byteBuffer.getFloat();
    }

    // 字节数组转double
    public static double bytesToDouble(byte[] bytes) {
        ByteBuffer byteBuffer = ByteBuffer.wrap(bytes);
        return byteBuffer.getDouble();
    }

    // 字节数组转int
    public static int bytesToInt(byte[] bytes) {
        ByteBuffer byteBuffer = ByteBuffer.wrap(bytes);
        return byteBuffer.getInt();
    }

    // 字节数组转short
    public static short bytesToShort(byte[] bytes) {
        ByteBuffer byteBuffer = ByteBuffer.wrap(bytes);
        return byteBuffer.getShort();
    }

    // 字节数组转long
    public static long bytesToLong(byte[] bytes) {
        ByteBuffer byteBuffer = ByteBuffer.wrap(bytes);
        return byteBuffer.getLong();
    }

    // 字节数组转 unsigned short
    public static int bytesToUshort(byte[] bytes) {
        byte[] newBytes = new byte[4];
        System.arraycopy(bytes, 0, newBytes, 2, bytes.length);
        return Modbus.bytesToInt(newBytes);
    }

    // 字节数组转 unsigned int
    public static long bytesToUint(byte[] bytes) {
        byte[] newBytes = new byte[8];
        System.arraycopy(bytes, 0, newBytes, 4, bytes.length);
        return Modbus.bytesToLong(newBytes);
    }

    // unsigned short 转字节数组
    public static byte[] ushortToBytes(int i) {
        byte[] intBytes = Modbus.intToBytes(i);
        byte[] ushortBytes = new byte[2];
        System.arraycopy(intBytes, 2, ushortBytes, 0, ushortBytes.length);
        return ushortBytes;
    }

    // unsigned int 转字节数组
    public static byte[] uintToBytes(long i) {
        byte[] longBytes = Modbus.longToBytes(i);
        byte[] uintBytes = new byte[4];
        System.arraycopy(longBytes, 4, uintBytes, 0, uintBytes.length);
        return uintBytes;
    }

    // 将功能码0x01,0x02读操作返回的数据解析成bit数组
    public static int[][] parseByteArrayToBitsArray(byte[] bytes) {
        int byteCount = bytes[2];
        if (bytes.length - 3 - 2 != byteCount) {
            return new int[][]{};
        }
        int[][] bitsArray = new int[byteCount][];
        for (int i = 0; i < byteCount; i++) {
            byte value = bytes[3 + i]; // 从第4个字节开始解析
            int[] bitArray = new int[8];
            for (int j = 0; j < 8; j++) {
                int bitValue = (value >> j) & 0x00000001;
                bitArray[7 - j] = bitValue;
            }
            bitsArray[i] = bitArray;
        }
        return bitsArray;
    }

    // 校验读操作返回数据的完整性
    public static boolean isCompleteRead(byte[] bytes) {
        if (bytes.length < 3) {
            return false;
        }
        return bytes.length == expectLength(bytes);
    }

    // 根据部分数据，返回完整的数据长度
    public static int expectLength(byte[] bytes) {
        if (bytes.length < 3) return 0;
        int byteCount = bytes[2];
        return  3 + byteCount + 2;
    }
}
