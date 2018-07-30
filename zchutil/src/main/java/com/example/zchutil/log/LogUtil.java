package com.example.zchutil.log;

import android.util.Log;

import java.util.Iterator;
import java.util.Map;
import java.util.Set;

public class LogUtil {
    private static String TAG = "LogUtil";
    private static boolean isOpen = false;
    private static LogUtil.LogListener logListener;

    public LogUtil() {
    }

    public static void setOpen(boolean isOpen) {
        isOpen = isOpen;
    }

    public static void log(Object obj, String method, Map<String, String> map) {
        log(1, obj, method, (Map)map);
    }

    public static void log(int type, Object obj, String method, Map<String, String> map) {
        if (isOpen && map != null) {
            Set<String> keys = map.keySet();
            Iterator var5 = keys.iterator();

            while(var5.hasNext()) {
                String key = (String)var5.next();
                String value = (String)map.get(key);
                String desc = "[key]" + key + "---[value]" + value;
                log(type, obj, method, desc);
            }
        }

    }

    public static void log(Object obj, String method, String desc) {
        log(1, obj, method, (String)desc);
    }

    public static void log(int type, Object obj, String method, String desc) {
        switch(type) {
            case 0:
            default:
                logE(obj, method, desc);
                break;
            case 1:
                logD(obj, method, desc);
                break;
            case 2:
                logI(obj, method, desc);
                break;
            case 3:
                logV(obj, method, desc);
                break;
            case 4:
                logW(obj, method, desc);
        }

    }

    public static void libLog(Object obj, String method, String desc) {
        String msg = getLogModel(obj, method, desc);
        Log.i("WhiteFrameWork", msg);
    }

    public static void logV(Object obj, String method, String desc) {
        if (isOpen) {
            String msg = getLogModel(obj, method, desc);
            Log.v(TAG, msg);
        }

    }

    public static void logD(Object obj, String method, String desc) {
        if (isOpen) {
            String msg = getLogModel(obj, method, desc);
            Log.d(TAG, msg);
        }

    }

    public static void logI(Object obj, String method, String desc) {
        if (isOpen) {
            String msg = getLogModel(obj, method, desc);
            Log.i(TAG, msg);
        }

    }

    public static void logW(Object obj, String method, String desc) {
        if (isOpen) {
            String msg = getLogModel(obj, method, desc);
            Log.w(TAG, msg);
        }

    }

    public static void logE(Object obj, String method, String desc) {
        if (isOpen) {
            String msg = getLogModel(obj, method, desc);
            Log.e(TAG, msg);
        }

    }

    private static String getLogModel(Object obj, String method, String desc) {
        String className;
        if (obj != null) {
            if (obj instanceof String) {
                className = (String)obj;
            } else {
                className = obj.getClass().getName();
            }
        } else {
            className = "obj is null";
        }

        String result = "[className]" + className + "---[method]" + method + "---[desc]" + desc;
        useLogListener(result);
        return result;
    }

    public static void setLogListener(LogUtil.LogListener listener) {
        logListener = listener;
    }

    public static void useLogListener(String msg) {
        if (logListener != null) {
            logListener.log(msg);
        }

    }

    public interface LogListener {
        void log(String var1);
    }
}
