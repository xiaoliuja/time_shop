package com.xiaoliu.seckill.base;

import java.io.Serializable;

public class BaseRequest<T> implements Serializable {

    private String deviceType;

    private String deviceNo;

    private String version;

    private String channeId;

    private T data;


    public String getDeviceType() {
        return deviceType;
    }

    public void setDeviceType(String deviceType) {
        this.deviceType = deviceType;
    }

    public String getDeviceNo() {
        return deviceNo;
    }

    public void setDeviceNo(String deviceNo) {
        this.deviceNo = deviceNo;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public String getChanneId() {
        return channeId;
    }

    public void setChanneId(String channeId) {
        this.channeId = channeId;
    }

    public T getData() {
        return data;
    }

    public void setData(T data) {
        this.data = data;
    }
}
