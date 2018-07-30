package com.example.zchutil.luban.model;

import android.net.Uri;

import java.io.File;

public class LubanRequestData {
    protected Uri uri;
    protected File beforeFile;
    protected int what;

    public LubanRequestData() {
    }

    public String toString(Object obj) {
        return obj == null ? null : obj.toString();
    }

    public String toString() {
        return "[Uri]" + this.toString(this.uri) + "---[beforeFile]" + this.toString(this.beforeFile) + "---[what]" + this.what;
    }

    public LubanRequestData(Uri uri) {
        this(uri, 0);
    }

    public LubanRequestData(Uri uri, int what) {
        this.uri = uri;
        this.what = what;
    }

    public Uri getUri() {
        return this.uri;
    }

    public LubanRequestData setUri(Uri uri) {
        this.uri = uri;
        return this;
    }

    public int getWhat() {
        return this.what;
    }

    public LubanRequestData setWhat(int what) {
        this.what = what;
        return this;
    }

    public File getBeforeFile() {
        return this.beforeFile;
    }

    public LubanRequestData setBeforeFile(File beforeFile) {
        this.beforeFile = beforeFile;
        return this;
    }
}
