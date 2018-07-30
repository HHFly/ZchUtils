package com.example.zchutil.luban;

import android.content.Context;
import android.net.Uri;

import com.example.zchutil.ExceptionUtil;
import com.example.zchutil.log.LogUtil;
import com.example.zchutil.luban.core.Luban;
import com.example.zchutil.luban.core.OnCompressListener;
import com.example.zchutil.luban.model.LubanRequestData;
import com.example.zchutil.luban.model.LubanResultData;
import com.example.zchutil.permisstion.deflistener.PathUtil;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class LubanUtil {
    private final String TAG = "LubanUtils";
    private Context mAppContext;
    private LubanRequestData[] mRequestData;
    private List<LubanResultData> mResultData;
    private boolean isStarting;
    private int mSuccessCount;
    private int mFailCount;
    private LubanUtil.OnLubanListener onLubanListener;

    public LubanUtil(Context context) {
        this.mAppContext = context.getApplicationContext();
    }

    public static LubanUtil getInstance(Context context) {
        LubanUtil utils = new LubanUtil(context);
        return utils;
    }

    public void startForUri(List<Uri> data) {
        Uri[] uris = null;
        if (data != null) {
            int count = data.size();
            uris = (Uri[])data.toArray(new Uri[count]);
        }

        this.startForUri(uris);
    }

    public void startForUri(Uri... data) {
        LubanRequestData[] requestData = null;
        if (data != null) {
            int len = data.length;
            requestData = new LubanRequestData[len];

            for(int i = 0; i < len; ++i) {
                LubanRequestData request = new LubanRequestData();
                request.setUri(data[i]);
                requestData[i] = request;
            }
        }

        this.startForRequest(requestData);
    }

    public void startForRequest(List<LubanRequestData> data) {
        LubanRequestData[] uris = null;
        if (data != null) {
            int count = data.size();
            uris = (LubanRequestData[])data.toArray(new LubanRequestData[count]);
        }

        this.startForRequest(uris);
    }

    public void startForRequest(LubanRequestData... data) {
        if (data != null) {
            if (this.isStarting) {
                LogUtil.logE("LubanUtils", "startForRequest", "已经有任务在处理了，本次任务无效");
            } else {
                LogUtil.logI("LubanUtils", "startForRequest", "开始处理，正在初始化数据");
                this.isStarting = true;
                this.mRequestData = data;
                if (this.mResultData != null) {
                    this.mResultData.clear();
                }

                this.mSuccessCount = 0;
                this.mFailCount = 0;
                this.start();
            }
        } else {
            LogUtil.logE("LubanUtils", "startForRequest", "Uri列表为空");
        }

    }

    private void start() {
        LubanRequestData request = this.getNextRequest();
        if (request == null) {
            this.isStarting = false;
            this.useOnAllFinish();
        } else {
            File file = this.getFileByUri(request.getUri());
            request.setBeforeFile(file);
            if (file == null) {
                this.useOnFail(request);
                this.start();
            } else {
                this.launch(this.mAppContext, request);
            }
        }

    }

    private void launch(Context context, final LubanRequestData requestData) {
        if (context != null && requestData != null && requestData.getBeforeFile() != null) {
            Luban.get(context).load(requestData.getBeforeFile()).putGear(3).setCompressListener(new OnCompressListener() {
                public void onStart() {
                    LogUtil.logI("LubanUtils", "onStart", "正在压缩:");
                }

                public void onSuccess(File file) {
                    LogUtil.logI("LubanUtils", "onSuccess", "压缩成功:" + file.getPath());
                    LubanUtil.this.useOnSuccess(requestData, file);
                    LubanUtil.this.start();
                }

                public void onError(Throwable e) {
                    LogUtil.logI("LubanUtils", "onSuccess", "压缩异常");
                    ExceptionUtil.throwable(e);
                    LubanUtil.this.useOnFail(requestData);
                    LubanUtil.this.start();
                }
            }).launch();
        } else {
            this.useOnFail(requestData);
            this.start();
        }
    }

    private int getCurSecond() {
        return this.mSuccessCount + this.mFailCount;
    }

    private File getFileByUri(Uri uri) {
        return PathUtil.getFileByUri(this.mAppContext, uri);
    }

    private LubanRequestData getNextRequest() {
        if (this.mRequestData != null) {
            int finishCount = this.getCurSecond();
            int dataCount = this.mRequestData.length;
            if (finishCount >= 0 && finishCount < dataCount) {
                return this.mRequestData[finishCount];
            }
        }

        return null;
    }

    private void addResultData(LubanResultData data) {
        if (this.mResultData == null) {
            this.mResultData = new ArrayList();
        }

        if (data != null) {
            this.mResultData.add(data);
        }

    }

    public LubanUtil setOnLubanListener(LubanUtil.OnLubanListener listener) {
        this.onLubanListener = listener;
        return this;
    }

    private void useOnFail(LubanRequestData data) {
        ++this.mFailCount;
        int curSecond = this.getCurSecond();
        LubanResultData result = new LubanResultData(data);
        result.setTimes(curSecond).setSuccess(false);
        this.addResultData(result);
        LogUtil.logI("LubanUtils", "useOnFail", "第" + result.getTimes() + "次压缩失败");
        LogUtil.logI("LubanUtils", "useOnFail", result.toString());
        if (this.onLubanListener != null) {
            this.onLubanListener.onFail(result);
        }

    }

    private void useOnSuccess(LubanRequestData data, File afterFile) {
        ++this.mSuccessCount;
        int curSecond = this.getCurSecond();
        LubanResultData result = new LubanResultData(data);
        result.setTimes(curSecond).setAfterFile(afterFile).setSuccess(true);
        LogUtil.logI("LubanUtils", "useOnSuccess", "第" + result.getTimes() + "次压缩成功");
        LogUtil.logI("LubanUtils", "useOnSuccess", result.toString());
        this.addResultData(result);
        if (this.onLubanListener != null) {
            this.onLubanListener.onSuccess(result);
        }

    }

    private void useOnAllFinish() {
        LogUtil.logI("LubanUtils", "useOnAllFinish", "全部压缩完毕");
        if (this.onLubanListener != null) {
            this.onLubanListener.onAllFinish(this.mResultData);
        }

    }

    public abstract static class DefOnLubanListener implements LubanUtil.OnLubanListener {
        public DefOnLubanListener() {
        }

        public void onFail(LubanResultData data) {
        }

        public void onAllFinish(List<LubanResultData> data) {
        }
    }

    public interface OnLubanListener {
        void onSuccess(LubanResultData var1);

        void onFail(LubanResultData var1);

        void onAllFinish(List<LubanResultData> var1);
    }
}
