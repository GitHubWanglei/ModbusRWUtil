package com.example.modbusrwutil.modbusUtils;

import java.util.List;

import tp.xmaihh.serialport.bean.ComBean;

public class AddressGroup {
    private String identifier;
    private List<Address> addressList;
    private AddressGroupListener listener;

    public AddressGroup(String identifier, List<Address> addressList) {
        this.identifier = identifier;
        this.addressList = addressList;
    }

    public String getIdentifier() {
        return identifier;
    }

    public void setIdentifier(String identifier) {
        this.identifier = identifier;
    }

    public List<Address> getAddressList() {
        return addressList;
    }

    public void setAddressList(List<Address> addressList) {
        this.addressList = addressList;
    }

    public AddressGroupListener getListener() {
        return listener;
    }

    public void setListener(AddressGroupListener listener) {
        this.listener = listener;
    }

    public interface AddressGroupListener {
        /**
         *
         * @param comBean 从机返回的完整数据
         * @param address  寄存器信息
         * @param bitsArray 如果是功能码01、02，返回的多个bit数组（将每个字节打散成bit位数组）; 写操作返回null
         * @param values 如果是功能码03，返回的多个数值；写操作返回null
         */
        public void onDataRecieved(ComBean comBean, Address address, int[][] bitsArray, String[] values);
    }
}
