package com.example.modbusrwutil.modbusUtils;

import com.example.modbusrwutil.logConfig.LogUtils;

import java.util.Arrays;

import tp.xmaihh.serialport.bean.ComBean;
import tp.xmaihh.serialport.utils.ByteUtil;

// 数据粘包处理
public class ModbusStickPackageHelper {

    public static byte[] ydReplyData; // 宇电
    public static byte[] ycReplyData; // 玉川

    public static byte[] writeReplyData;
    public static byte[] writeExpectReplyData;

    public static byte[] readReplyData;

    public static boolean processYcDataIsComplete(ComBean comBean) {
        if (comBean.bRec == null) return false;
        if (ycReplyData == null) { // 首次拼接
            if (comBean.bRec[0] != 0x3A) {
                LogUtils.log("test", "不是以冒号开头, 丢弃!");
                return false;
            }
            ycReplyData = comBean.bRec;
//            LogUtils.log("test", "首次拼接："+ByteUtil.ByteArrToHex(comBean.bRec));
        } else {
            ycReplyData = concatArray(ycReplyData, comBean.bRec);
//            LogUtils.log("test", "再次拼接："+ByteUtil.ByteArrToHex(ycReplyData));
        }
        boolean isNotNull = ycReplyData != null && ycReplyData.length > 7;
        boolean isStart_0X3A = isNotNull && ycReplyData[0] == 0x3A;
        boolean isEnd_0x0D = isNotNull && ycReplyData[ycReplyData.length-1] == 0x0D;
        if (isStart_0X3A && isEnd_0x0D) {
//            LogUtils.log("test", "拼接完成!");
            comBean.bRec = ycReplyData;
            ycReplyData = null;
            return true;
        }
        return false;
    }

    public static boolean processYdDataIsComplete(ComBean comBean) {
        if (comBean.bRec == null) return false;
        if (comBean.bRec.length < 10) {
            if (ydReplyData == null) { // 开始拼接新数据
                ydReplyData = comBean.bRec;
                return false;
            } else if (ydReplyData.length + comBean.bRec.length < 10) { // 拼接后依然不完整
                ydReplyData = concatArray(ydReplyData, comBean.bRec);
                return false;
            } else if (ydReplyData.length + comBean.bRec.length == 10) { // 长度等于10，拼接完成
                ydReplyData = concatArray(ydReplyData, comBean.bRec);
                comBean.bRec = ydReplyData;
            }
        } else if (comBean.bRec.length > 10) { // 大于10个字节长度，丢弃
            ydReplyData = null;
            LogUtils.log("test", "大于10个字节长度，丢弃: ");
            return false;
        }
        ydReplyData = null;
        return true;
    }

    public static boolean processModbusWriteReplyDataIsComplete(ComBean comBean, byte[] expectData) {
        writeExpectReplyData = expectData;
        if (comBean.bRec == null || writeExpectReplyData == null) return false;
        if (comBean.bRec.length < writeExpectReplyData.length) { // 数据不完整，需要拼接
            if (writeReplyData != null && writeReplyData.length > 0) { // 上次的也不完整，直接拼接数据
                int totalLength = writeExpectReplyData.length;
                if (writeReplyData.length + comBean.bRec.length < totalLength) { // 小于总长度，直接拼接
                    writeReplyData = concatArray(writeReplyData, comBean.bRec);
//                        LogUtils.log("test", "拼接后: "+ ByteUtil.ByteArrToHex(writeReplyData));
                    return false;
                } else if (writeReplyData.length + comBean.bRec.length == totalLength) { // 等于总长度，拼接后就是完成数据
                    writeReplyData = concatArray(writeReplyData, comBean.bRec);
//                        LogUtils.log("test", "拼接完成: "+ByteUtil.ByteArrToHex(writeReplyData));
                } else  { // 超出总长度，直接丢弃
                    writeReplyData = null;
                    return false;
                }
            } else { // 上次数据完整，新收的数据不完整，开始从头拼接
                writeReplyData = comBean.bRec;
//                LogUtils.log("test", "首次拼接: "+ByteUtil.ByteArrToHex(writeReplyData));
                return false;
            }
            // 已拼接完整
            comBean.bRec = writeReplyData;
        } else if (comBean.bRec.length > writeExpectReplyData.length) { // 超出总长度，直接丢弃
            writeReplyData = null;
            return false;
        }
        writeReplyData = null; // 拼接完成后，置空
        // 长度正确，但是数据不正确(和预期不一致)，丢弃
        if (!Arrays.equals(comBean.bRec, writeExpectReplyData)) {
            LogUtils.log("test", "长度正确，但是数据不正确(和预期不一致)，丢弃"+ ByteUtil.ByteArrToHex(comBean.bRec));
            return false;
        }
        return true;
    }

    public static boolean processModbusReadReplyDataIsComplete(ComBean comBean) {
        if (comBean.bRec == null) return false;
        if (!Modbus.isCompleteRead(comBean.bRec) || (readReplyData != null && readReplyData.length > 0)) { // 数据不完整
            if (readReplyData != null && readReplyData.length > 0) { // 上次的也不完整，直接拼接数据
                if (readReplyData.length >= 3) {
                    // 不是读操作，丢弃
                    if (readReplyData[1] != 0x01 && readReplyData[1] != 0x02 && readReplyData[1] != 0x03 && readReplyData[1] != 0x04) {
                        LogUtils.log("test", "不是读操作，丢弃: " + ByteUtil.ByteArrToHex(comBean.bRec));
                        readReplyData = null; // 清空
                        return false;
                    }
                    // 计算期望总长度
                    int totalLength = Modbus.expectLength(readReplyData);
                    if (readReplyData.length + comBean.bRec.length < totalLength) { // 小于总长度，直接拼接
                        readReplyData = concatArray(readReplyData, comBean.bRec);
//                        LogUtils.log("test", "拼接后: "+ ByteUtil.ByteArrToHex(readReplyData));
                        return false;
                    } else if (readReplyData.length + comBean.bRec.length == totalLength) { // 等于总长度，拼接后就是完成数据
                        readReplyData = concatArray(readReplyData, comBean.bRec);
//                        LogUtils.log("test", "拼接完成: "+ByteUtil.ByteArrToHex(readReplyData));
                    } else  { // 超出总长度，直接丢弃
                        LogUtils.log("test", "超出总长度，丢弃: " + ByteUtil.ByteArrToHex(comBean.bRec));
                        readReplyData = null;
                        return false;
                    }
                } else { // 上次的收到数据的长度不够3，则直接拼接
                    readReplyData = concatArray(readReplyData, comBean.bRec);
                    if (!Modbus.isCompleteRead(readReplyData)) { // 拼接后依然不完整
                        // 不是读操作，丢弃
                        if (readReplyData.length >= 2 && readReplyData[1] != 0x01 && readReplyData[1] != 0x02 && readReplyData[1] != 0x03 && readReplyData[1] != 0x04) {
                            LogUtils.log("test", "不是读操作，丢弃: " + ByteUtil.ByteArrToHex(comBean.bRec));
                            readReplyData = null; // 清空
                        } else {
//                            LogUtils.log("test", "2长度小于3，直接拼接: "+ByteUtil.ByteArrToHex(readReplyData));
                        }
                        return false;
                    } else {
                        // 拼接完成
//                        LogUtils.log("test", "长度小于3，直接拼接，拼接完成: "+ByteUtil.ByteArrToHex(readReplyData));
                    }
                }
            } else { // 上次数据完整，新收的数据不完整，开始从头拼接
                readReplyData = comBean.bRec;
//                LogUtils.log("test", "首次拼接: "+ByteUtil.ByteArrToHex(readReplyData));

                boolean isNotRead = readReplyData.length >= 2 && readReplyData[1] != 0x01 && readReplyData[1] != 0x02 && readReplyData[1] != 0x03 && readReplyData[1] != 0x04;
                boolean isNotRightLength = readReplyData.length >= 3 && readReplyData.length > (3 + readReplyData[2] + 2);
                if (isNotRead || isNotRightLength) {
                    if (isNotRead) {
                        LogUtils.log("test", "首次拼接检查，不是读操作，丢弃: " + ByteUtil.ByteArrToHex(comBean.bRec));
                    }
                    if (isNotRightLength) {
                        LogUtils.log("test", "首次拼接检查，数据长度不正确，丢弃: " + ByteUtil.ByteArrToHex(comBean.bRec));
                    }
                    readReplyData = null; // 清空
                }

                return false;
            }
            // 拼接完成
            comBean.bRec = readReplyData;
        }
        readReplyData = null; // 拼接完成后，置空
        if (comBean.bRec.length >= 2) { // 检查是否是读操作
            if (comBean.bRec[1] != 0x01 && comBean.bRec[1] != 0x02 && comBean.bRec[1] != 0x03 && comBean.bRec[1] != 0x04) {
                LogUtils.log("test", "拼接完成后再次检查，不是读操作，丢弃: " + ByteUtil.ByteArrToHex(comBean.bRec));
                return false;
            }
        }
        return true;
    }

    private static byte[] concatArray(byte[] a, byte[] b) {
        byte [] bytes = new byte[a.length + b.length];
        System.arraycopy(a, 0, bytes, 0, a.length);
        System.arraycopy(b, 0, bytes, a.length, b.length);
        return bytes;
    }

}
