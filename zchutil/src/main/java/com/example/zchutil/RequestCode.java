package com.example.zchutil;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.SOURCE)
public @interface RequestCode {
    int REQUEST_LOCATION = 101;
    int REQUEST_CALL_PHONE = 100;
    int CAMERA = 1;
    int ALBUM = 2;
}
