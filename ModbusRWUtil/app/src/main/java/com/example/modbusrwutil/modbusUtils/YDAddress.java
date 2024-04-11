package com.example.modbusrwutil.modbusUtils;

/**
 * 宇电仪表参数信息
 */
public class YDAddress extends Address{

    private int deviceId; // 仪表地址
    private int paramNo; // 参数代号
    private int value; // 写入数

    private byte[] replyBytes; // 仪表回应的数据

    public YDAddress(int deviceId, int paramNo) {
        super(deviceId, 0,
                0, 0, new AVT[]{AVT.t_short},
                0, 0, new AVT[]{AVT.t_short},
                null);
        this.deviceId = deviceId;
        this.paramNo = paramNo;
        this.value = 0;
    }

    public YDAddress(int deviceId, int paramNo, int value) {
        super(deviceId, 0,
                0, 0, new AVT[]{AVT.t_short},
                0, 0, new AVT[]{AVT.t_short},
                null);
        this.deviceId = deviceId;
        this.paramNo = paramNo;
        this.value = value;
    }

    @Override
    public byte[] configReadData() {
        return YDAddress.getReadBytes(this.getParamNo(), this.getDeviceId());
    }

    @Override
    public byte[] configWriteData() {
        return YDAddress.getWriteBytes(this.getParamNo(), this.getValue(), this.getDeviceId());
    }

    // 测量值PV
    public int parsePV() {
        if (getReplyBytes().length != 10) {
            return 0;
        }
        return Modbus.bytesToShort(new byte[]{this.getReplyBytes()[1], this.getReplyBytes()[0]});
    }

    // 给定值SV
    public int parseSV() {
        if (getReplyBytes().length != 10) {
            return 0;
        }
        return Modbus.bytesToShort(new byte[]{this.getReplyBytes()[3], this.getReplyBytes()[2]});
    }

    // 输出值MV，8位有符号二进制数
    public int parseMV() {
        if (getReplyBytes().length != 10) {
            return 0;
        }
        byte b = this.getReplyBytes()[4];
        return (int) b;
    }

    // 报警状态
    public int[] parseStatus() {
        if (getReplyBytes().length != 10) {
            return new int[]{0,0,0,0,0,0,0,0};
        }
        byte value = this.getReplyBytes()[5];
        int[] bitArray = new int[8];
        for (int i = 0; i < 8; i++) {
            int bitValue = (value >> i) & 0b00000001;
            bitArray[i] = bitValue;
        }
        return bitArray;
    }

    // 所读/写参数值
    public int parseValue() {
        if (getReplyBytes().length != 10) {
            return 0;
        }
        return Modbus.bytesToShort(new byte[]{this.getReplyBytes()[7], this.getReplyBytes()[6]});
    }

    @Override
    public int getDeviceId() {
        return deviceId;
    }

    @Override
    public void setDeviceId(int deviceId) {
        this.deviceId = deviceId;
    }

    public int getParamNo() {
        return paramNo;
    }

    public void setParamNo(int paramNo) {
        this.paramNo = paramNo;
    }

    public int getValue() {
        return value;
    }

    public void setValue(int value) {
        this.value = value;
    }

    public byte[] getReplyBytes() {
        return replyBytes;
    }

    public void setReplyBytes(byte[] replyBytes) {
        this.replyBytes = replyBytes;
    }

    public static byte[] getReadBytes(int paramNo, int devAddr) {
        byte[] bytes = new byte[8];
        bytes[0] = (byte) (0x80 + devAddr);
        bytes[1] = (byte) (0x80 + devAddr);
        bytes[2] = 0x52;
        bytes[3] = (byte) paramNo;
        bytes[4] = 0x00;
        bytes[5] = 0x00;
        bytes[6] = getCrc((byte) paramNo, (byte) devAddr)[0];
        bytes[7] = getCrc((byte) paramNo, (byte) devAddr)[1];
        return bytes;
    }

    public static byte[] getCrc(byte paramNo, byte devAddr) {
        int res = paramNo * 256 + 82 + devAddr;
        byte[] crc = new byte[2];
        crc[0] = (byte) (res & 0xFF); // 低字节
        crc[1] = (byte) ((res >> 8) & 0xFF); // 高字节
        return crc;
    }

    public static byte[] getWriteBytes(int paramNo, int value, int devAddr) {
        byte[] bytes = new byte[8];
        bytes[0] = (byte) (0x80 + devAddr);
        bytes[1] = (byte) (0x80 + devAddr);
        bytes[2] = 0x43;
        bytes[3] = (byte) paramNo;
        byte[] shortBytes = Modbus.shortToBytes((short) value);
        bytes[4] = shortBytes[1]; // 低字节在前
        bytes[5] = shortBytes[0]; // 高字节在后
        bytes[6] = getCrc((byte) paramNo, value, (byte) devAddr)[0];
        bytes[7] = getCrc((byte) paramNo, value, (byte) devAddr)[1];
        return bytes;
    }

    public static byte[] getCrc(byte paramNo, int value, byte devAddr) {
        int res = paramNo * 256 + 67 + value + devAddr;
        byte[] crc = new byte[2];
        crc[0] = (byte) (res & 0xFF); // 低字节
        crc[1] = (byte) ((res >> 8) & 0xFF); // 高字节
        return crc;
    }

}
