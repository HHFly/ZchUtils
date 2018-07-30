package com.example.zchutil.location;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.location.LocationManager;

import com.example.zchutil.ToastUtil;
import com.example.zchutil.location.listener.LocationCallback;
import com.example.zchutil.location.model.LocationModel;
import com.example.zchutil.log.LogUtil;

import java.util.List;

public class MyCommonLocationListenerUtil {
    public MyCommonLocationListenerUtil() {
    }

    public static boolean checkCallbackAndSelfPermission(String TAG, Activity activity, LocationCallback callback) {
        LogUtil.logI(TAG, "register", "正在进行定位服务校验");
        boolean result = false;
        if (callback == null) {
            LogUtil.logE(TAG, "checkCallbackAndSelfPermission", "callback is null");
        } else if (!isLocationEnabled(activity.getApplicationContext())) {
            ToastUtil.show("无法定位，请打开定位服务");
        } else {
            LogUtil.logI(TAG, "checkCallbackAndSelfPermission", "回调监听不为空");
            LogUtil.logI(TAG, "checkCallbackAndSelfPermission", "定位服务已开启");
            result = true;
        }

        return result;
    }

    public static boolean isGpsEnabled(Context context) {
        LocationManager lm = (LocationManager)context.getSystemService("location");
        return lm.isProviderEnabled("gps");
    }

    public static boolean isNetworkEnabled(Context context) {
        LocationManager lm = (LocationManager)context.getSystemService("location");
        return lm.isProviderEnabled("network");
    }

    public static void openGpsSettings(Context context) {
        Intent intent = new Intent("android.settings.LOCATION_SOURCE_SETTINGS");
        intent.setFlags(268435456);
        context.startActivity(intent);
    }

    public static boolean isLocationEnabled(Context context) {
        LocationManager lm = (LocationManager)context.getSystemService("location");
        return lm.isProviderEnabled("network") || lm.isProviderEnabled("gps");
    }

    public static void sendLocationInfo(String TAG, LocationUtil util, LocationModel location) {
        sendLocationInfo(TAG, util, location, (StringBuilder)null);
    }

    public static synchronized void sendLocationInfo(String TAG, LocationUtil util, LocationModel location, StringBuilder log) {
        if (log == null) {
            log = new StringBuilder();
        }

        if (util != null) {
            util.setNewestLocation(location);
        }

        LocationModel l = new LocationModel(location);
        log.append("坐标改变>>>" + location.toString() + "\n");
        List<LocationCallback> array = getLocationCallbackArray(util);
        if (array != null) {
            int count = array.size();

            for(int i = count - 1; i >= 0; --i) {
                log.append("循环监听列表>>>sendLocationInfo:" + i + "\n");
                LocationCallback callback = (LocationCallback)array.get(i);
                callback.onLocationChanged(new LocationModel(l));
                checkRemoveCallback(TAG, util, callback, log);
            }

            if (array.size() == 0) {
                log.append("已完成全部定位监听的处理，移除系统定位监听\n");
                unRegisterAll(util);
            }
        } else {
            log.append("已完成全部定位监听的处理，移除系统定位监听\n");
            unRegisterAll(util);
        }

    }

    public static void checkRemoveCallback(String TAG, LocationUtil util, LocationCallback callback) {
        checkRemoveCallback(TAG, util, callback, (StringBuilder)null);
    }

    public static synchronized void checkRemoveCallback(String TAG, LocationUtil util, LocationCallback callback, StringBuilder log) {
        if (log == null) {
            log = new StringBuilder();
        }

        if (callback != null) {
            if (callback.isLocationOne()) {
                log.append("该监听只需要定位一次，移除该监听>>>" + callback.getClass().hashCode() + "---" + callback.getClass().getName() + "\n");
                if (util != null) {
                    util.unRegister(callback);
                }
            } else {
                log.append("该监听需要多次定位>>>" + callback.getClass().hashCode() + "---" + callback.getClass().getName() + "\n");
            }
        }

    }

    public static void unRegisterAll(LocationUtil util) {
        if (util != null) {
            util.unRegisterAll();
        }

    }

    public static List<LocationCallback> getLocationCallbackArray(LocationUtil util) {
        return util == null ? null : util.getLocationCallbackArray();
    }
}
