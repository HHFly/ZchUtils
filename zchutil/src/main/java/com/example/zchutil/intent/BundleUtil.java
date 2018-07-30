package com.example.zchutil.intent;

import android.content.Intent;
import android.os.Bundle;

import java.io.Serializable;

public class BundleUtil {
    private Bundle mBundle = new Bundle();

    public BundleUtil() {
    }

    public static BundleUtil getInstance() {
        return new BundleUtil();
    }

    public BundleUtil putString(String key, String value) {
        this.mBundle.putString(key, value);
        return this;
    }

    public BundleUtil putInt(String key, int value) {
        this.mBundle.putInt(key, value);
        return this;
    }

    public BundleUtil putLong(String key, long value) {
        this.mBundle.putLong(key, value);
        return this;
    }

    public BundleUtil putSerializable(String key, Serializable value) {
        this.mBundle.putSerializable(key, value);
        return this;
    }

    public Bundle getBundle() {
        return this.mBundle;
    }

    public void addIntent(Intent intent) {
        intent.putExtras(this.getBundle());
    }
}
