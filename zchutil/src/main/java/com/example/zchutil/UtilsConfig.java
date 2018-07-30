package com.example.zchutil;

import android.content.Context;

import com.example.zchutil.log.LogUtil;

public class UtilsConfig {

    private static UtilsConfig utilsConfig;
    private Context mAppContext;
    private String mFileProvider;
    private String mPkgName;

    public UtilsConfig(Context context) {
        this.mAppContext = context.getApplicationContext();
        ToastUtil.init(context);
        AppResUtil.init(context);
        LogUtil.setOpen(false);
        this.mPkgName = MetaDataUtil.getApplicationInfo(context, "WHITE_METADATA_PKGNAME");
        this.mFileProvider = MetaDataUtil.getApplicationInfo(context, "WHITE_METADATA_FILEPROVIDER");
        LogUtil.libLog(this, "UtilsConfig", "获取gradle配置的manifestPlaceholders参数>>>[WHITE_METADATA_PKGNAME]" + this.mPkgName + "---[WHITE_METADATA_FILEPROVIDER]" + this.mFileProvider);
    }

    public static UtilsConfig getInstance(Context context) {
        if (utilsConfig == null) {
            Class var1 = UtilsConfig.class;
            synchronized(UtilsConfig.class) {
                if (utilsConfig == null) {
                    utilsConfig = new UtilsConfig(context);
                }
            }
        }

        return utilsConfig;
    }

    public UtilsConfig setLogOpen(boolean isOpen) {
        LogUtil.setOpen(isOpen);
        return this;
    }

    public UtilsConfig setFileProvider(String data) {
        this.mFileProvider = data;
        return this;
    }

    public String getFileProvider() {
        return this.mFileProvider;
    }
}
