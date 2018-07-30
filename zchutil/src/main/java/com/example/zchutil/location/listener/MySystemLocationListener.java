package com.example.zchutil.location.listener;

import android.location.Location;
import android.location.LocationListener;
import android.os.Bundle;

import com.example.zchutil.location.MyCommonLocationListenerUtil;
import com.example.zchutil.location.SystemLocationUtil;
import com.example.zchutil.location.model.LocationModel;
import com.example.zchutil.log.LogUtil;

public class MySystemLocationListener implements LocationListener {
    private final String TAG = "MyLocationListener";
    String mProvider;
    SystemLocationUtil mUtil;

    public MySystemLocationListener(SystemLocationUtil util, String provider) {
        this.mUtil = util;
        this.mProvider = provider;
    }

    private void unRegisterNetwork() {
        if (this.mUtil != null) {
            this.mUtil.unRegisterNetwork();
        }

    }

    private void unRegisterGps() {
        if (this.mUtil != null) {
            this.mUtil.unRegisterGps();
        }

    }

    private void registerCallback() {
        if (this.mUtil != null) {
            this.mUtil.registerCallback((LocationCallback)null);
        }

    }

    public void onLocationChanged(Location location) {
        StringBuilder log = new StringBuilder();
        if ("gps".equals(this.mProvider)) {
            log.append("\n成功获取gps经纬度，注销网络定位");
            this.unRegisterNetwork();
        }

        log.append("\n[mProvider]" + this.mProvider + "\n");
        MyCommonLocationListenerUtil.sendLocationInfo("MyLocationListener", this.mUtil, new LocationModel(location), log);
        LogUtil.logI("MyLocationListener", "onLocationChanged", log.toString());
    }

    public void onStatusChanged(String provider, int status, Bundle extras) {
        StringBuilder log = new StringBuilder();
        log.append("[provider]" + provider + "---[status]" + status + "\n");
        if ("gps".equals(this.mProvider)) {
            switch(status) {
                case 0:
                    log.append("gps无服务，重新注册系统定位监听");
                    LogUtil.logI("MyLocationListener", "onStatusChanged", log.toString());
                    this.registerCallback();
                    break;
                case 1:
                    log.append("gps暂时不可用，重新注册系统定位监听");
                    LogUtil.logI("MyLocationListener", "onStatusChanged", log.toString());
                    this.registerCallback();
                    break;
                case 2:
                    log.append(this.mProvider + "定位成功,注销unRegisterNetwork\n");
            }
        } else {
            LogUtil.logI("MyLocationListener", "onStatusChanged", log.toString());
        }

    }

    public void onProviderEnabled(String provider) {
        LogUtil.logI("MyLocationListener", "onProviderEnabled", "比如GPS被打开");
    }

    public void onProviderDisabled(String provider) {
        LogUtil.logI("MyLocationListener", "onProviderDisabled", "比如GPS被关闭");
    }
}
