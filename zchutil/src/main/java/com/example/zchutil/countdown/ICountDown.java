package com.example.zchutil.countdown;

public interface ICountDown {
    CountDownUtil setTime(long var1);

    CountDownUtil setDelay(long var1);

    boolean isCountDown();

    long getRemainTime();

    void prepare();

    void start();

    void stop();

    void destroy();

    CountDownUtil setCountDownListener(CountDownUtil.CountDownListener var1);
}
