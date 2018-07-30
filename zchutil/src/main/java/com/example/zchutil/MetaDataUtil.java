package com.example.zchutil;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.pm.ActivityInfo;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.ServiceInfo;

import com.example.zchutil.log.LogUtil;

public class MetaDataUtil {
    private static final String TAG = "MetaDataUtil";

    public MetaDataUtil() {
    }

    public static String getApplicationInfo(Context context, String key) {
        ApplicationInfo info = null;
        String log = null;

        try {
            String packageName = context.getPackageName();
            info = context.getPackageManager().getApplicationInfo(packageName, 128);
        } catch (PackageManager.NameNotFoundException var5) {
            log = var5.getMessage();
            var5.printStackTrace();
        }

        if (info == null) {
            LogUtil.logI("MetaDataUtil", "getApplicationInfo", "ApplicationInfo获取失败>>>" + log);
            return "";
        } else {
            return info.metaData.getString(key);
        }
    }

    public static String getActivityInfo(Activity activity, String key) {
        ActivityInfo info = null;
        String log = null;

        try {
            ComponentName componentName = activity.getComponentName();
            info = activity.getPackageManager().getActivityInfo(componentName, 128);
        } catch (PackageManager.NameNotFoundException var5) {
            log = var5.getMessage();
            var5.printStackTrace();
        }

        if (info == null) {
            LogUtil.logI("MetaDataUtil", "getActivityInfo", "ActivityInfo获取失败>>>" + log);
            return "";
        } else {
            return info.metaData.getString(key);
        }
    }

    public static String getServiceInfo(Activity activity, String key) {
        ServiceInfo info = null;
        String log = null;

        try {
            ComponentName componentName = activity.getComponentName();
            info = activity.getPackageManager().getServiceInfo(componentName, 128);
        } catch (PackageManager.NameNotFoundException var5) {
            log = var5.getMessage();
            var5.printStackTrace();
        }

        if (info == null) {
            LogUtil.logI("MetaDataUtil", "getServiceInfo", "ServiceInfo获取失败>>>" + log);
            return "";
        } else {
            return info.metaData.getString(key);
        }
    }

    public static String getReceiverInfo(Activity activity, String key) {
        ActivityInfo info = null;
        String log = null;

        try {
            ComponentName componentName = activity.getComponentName();
            info = activity.getPackageManager().getReceiverInfo(componentName, 128);
        } catch (PackageManager.NameNotFoundException var5) {
            log = var5.getMessage();
            var5.printStackTrace();
        }

        if (info == null) {
            LogUtil.logI("MetaDataUtil", "getReceiverInfo", "ActivityInfo获取失败>>>" + log);
            return "";
        } else {
            return info.metaData.getString(key);
        }
    }
}
