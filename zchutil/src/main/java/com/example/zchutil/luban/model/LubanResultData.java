package com.example.zchutil.luban.model;

import android.net.Uri;

import java.io.File;

public class LubanResultData extends LubanRequestData {
    protected File afterFile;
    protected int times;
    protected boolean isSuccess;

    public LubanResultData(LubanRequestData data) {
        if (data != null) {
            this.setUri(data.getUri());
            this.setWhat(data.getWhat());
            this.setBeforeFile(data.getBeforeFile());
        }

    }

    public String toString() {
        return "[Uri]" + this.toString(this.uri) + "---[beforeFile]" + this.toString(this.beforeFile) + "---[afterFile]" + this.toString(this.afterFile) + "---[what]" + this.what + "---[times]" + this.times;
    }

    public LubanResultData(Uri uri) {
        super(uri);
    }

    public LubanResultData(Uri uri, int what) {
        super(uri, what);
    }

    public File getAfterFile() {
        return this.afterFile;
    }

    public LubanResultData setAfterFile(File afterFile) {
        this.afterFile = afterFile;
        return this;
    }

    public int getTimes() {
        return this.times;
    }

    public LubanResultData setTimes(int times) {
        this.times = times;
        return this;
    }

    public boolean isSuccess() {
        return this.isSuccess;
    }

    public LubanResultData setSuccess(boolean success) {
        this.isSuccess = success;
        return this;
    }
}
