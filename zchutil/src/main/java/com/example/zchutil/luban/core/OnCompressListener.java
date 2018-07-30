package com.example.zchutil.luban.core;

import java.io.File;

public interface OnCompressListener {
    void onStart();

    void onSuccess(File var1);

    void onError(Throwable var1);
}
