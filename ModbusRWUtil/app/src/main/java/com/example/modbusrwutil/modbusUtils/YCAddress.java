package com.example.modbusrwutil.modbusUtils;

import java.nio.charset.StandardCharsets;

// 玉川真空计
public class YCAddress extends Address {

    public static final byte[] COMMAND_READ_VACUUM = {0x3A, 0x30, 0x35, 0x44, 0x34, 0x31, 0x0D};

    private int deviceId; // 仪表地址
    private byte[] command; // 指令

    private byte[] replyBytes; // 仪表回应的数据

    public YCAddress(int deviceId, byte[] command) {
        super(deviceId, 0,
                0, 0, new AVT[]{AVT.t_short},
                0, 0, new AVT[]{AVT.t_short},
                null);
        this.deviceId = deviceId;
        this.command = command;
    }

    @Override
    public long generateKey() {
        return 10^7 + deviceId;
    }

    @Override
    public byte[] configReadData() {
        return command;
    }

    @Override
    public byte[] configWriteData() {
        return command;
    }

    public float parseVacuum() {
        if (replyBytes.length < 8) { // 容错，至少8个长度
            return 0.0f;
        }
        String value = new String(replyBytes, 4, replyBytes.length-7, StandardCharsets.US_ASCII);
        float d;
        try {
            d = Float.parseFloat(value);
            return d;
        } catch (Exception e) {
            return 0.0f;
        }
    }

    @Override
    public int getDeviceId() {
        return deviceId;
    }

    @Override
    public void setDeviceId(int deviceId) {
        this.deviceId = deviceId;
    }

    public byte[] getCommand() {
        return command;
    }

    public void setCommand(byte[] command) {
        this.command = command;
    }

    public byte[] getReplyBytes() {
        return replyBytes;
    }

    public void setReplyBytes(byte[] replyBytes) {
        this.replyBytes = replyBytes;
    }
}
