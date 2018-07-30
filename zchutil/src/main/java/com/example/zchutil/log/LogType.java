package com.example.zchutil.log;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.SOURCE)
public @interface LogType {
    int FLAG_LOG_E = 0;
    int FLAG_LOG_D = 1;
    int FLAG_LOG_I = 2;
    int FLAG_LOG_V = 3;
    int FLAG_LOG_W = 4;
}