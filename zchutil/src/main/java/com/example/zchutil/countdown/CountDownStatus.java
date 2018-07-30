package com.example.zchutil.countdown;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.SOURCE)
public @interface CountDownStatus {
    int NONE = 0;
    int CD_PREPARE = 1;
    int CD_START = 2;
}
