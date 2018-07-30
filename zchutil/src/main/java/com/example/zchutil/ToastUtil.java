package com.example.zchutil;

import android.content.Context;
import android.support.annotation.StringRes;
import android.util.Log;
import android.widget.Toast;

import com.example.zchutil.log.LogUtil;

public class ToastUtil {
    private static final String TAG = "ToastUtils";
    private static Context mContext;
    private static Toast mToast;
    private static CharSequence oldMsg;
    private static long oldTime;
    private static long curTime;
    private static long REPEAT_DISPLAY_INTERVAL = 2000L;

    public ToastUtil() {
    }

    public static void init(Context context) {
        if (context != null) {
            mContext = context.getApplicationContext();
        }

    }

    public static void show(@StringRes int resId) {
        show(mContext, resId);
    }

    public static void show(Context context, @StringRes int resId) {
        if (context != null) {
            String msg = context.getString(resId);
            show((Context)context, msg);
        }
    }

    public static void show(CharSequence msg) {
        show(mContext, msg);
    }

    public static void show(Context context, CharSequence msg) {
        if (context == null) {
            LogUtil.logE("ToastUtils", "show", "context is null");
        } else {
            curTime = System.currentTimeMillis();
            if (mToast == null) {
                Class var2 = ToastUtil.class;
                synchronized(ToastUtil.class) {
                    if (mToast == null) {
                        context.getApplicationContext();
                        mToast = Toast.makeText(context, "", 0);
                    }

                    show(mToast, msg);
                }
            } else {
                show(mToast, msg);
            }

        }
    }

    private static void show(Toast toast, CharSequence msg) {
        if (toast == null) {
            Log.e("ToastUtils", "mToast is null");
        } else if (msg == null) {
            Log.e("ToastUtils", "msg is null");
        } else {
            if (msg.equals(oldMsg)) {
                if (curTime > oldTime + REPEAT_DISPLAY_INTERVAL) {
                    toast.show();
                    oldTime = curTime;
                }
            } else {
                oldMsg = msg;
                toast.setText(msg);
                toast.show();
                oldTime = curTime;
            }

        }
    }
}
