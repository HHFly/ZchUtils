package com.example.zchutil.intent;

import android.content.Intent;
import android.os.Bundle;

public class IntentUtil {
    public IntentUtil() {
    }

    public static String getString(Intent intent, String key, String def) {
        if (intent != null) {
            Bundle bundle = intent.getExtras();
            if (bundle != null) {
                return bundle.getString(key, def);
            }
        }

        return def;
    }

    public static int getInt(Intent intent, String key, int def) {
        if (intent != null) {
            Bundle bundle = intent.getExtras();
            if (bundle != null) {
                return bundle.getInt(key, def);
            }
        }

        return def;
    }

    public static long getLong(Intent intent, String key, long def) {
        if (intent != null) {
            Bundle bundle = intent.getExtras();
            if (bundle != null) {
                return bundle.getLong(key, def);
            }
        }

        return def;
    }

    public static <T> T getSerializable(Intent intent, String key) {
        if (intent != null) {
            Bundle bundle = intent.getExtras();
            if (bundle != null) {
                return (T) bundle.getSerializable(key);
            }
        }

        return null;
    }
}
