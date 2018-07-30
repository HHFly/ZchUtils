package com.example.zchutil;

import android.content.Context;
import android.os.Build;
import android.support.annotation.ColorRes;
import android.support.annotation.StringRes;

public class AppResUtil {
    private static Context mContext;

    public AppResUtil() {
    }

    public static void init(Context context) {
        mContext = context.getApplicationContext();
    }

    public static String getString(@StringRes int id) {
        return mContext == null ? "" : mContext.getString(id);
    }

    public static int getColor(@ColorRes int id) {
        if (mContext != null) {
            return Build.VERSION.SDK_INT >= 23 ? mContext.getColor(id) : mContext.getResources().getColor(id);
        } else {
            return 0;
        }
    }
}
