package com.example.zchutil;

import com.example.zchutil.log.LogUtil;

public class ExceptionUtil {
    public ExceptionUtil() {
    }

    public static void throwable(Throwable t) {
        LogUtil.logE("ExceptionUtil", "throwable", t.toString());
    }
}
