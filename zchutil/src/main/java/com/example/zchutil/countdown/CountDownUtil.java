package com.example.zchutil.countdown;

import android.os.Handler;
import android.os.Message;

import com.example.zchutil.log.LogUtil;

public class CountDownUtil implements ICountDown {
    private static CountDownUtil util;
    private long mTime;
    private long mEndTime;
    private long mDelay;
    private int mStatus;
    private Handler mHandler;
    private CountDownUtil.CountDownListener countDownListener;

    public CountDownUtil(long time, long delay) {
        this(time, delay, (CountDownUtil.CountDownListener)null);
    }

    public CountDownUtil(long time, long delay, CountDownUtil.CountDownListener listener) {
        this.mTime = time;
        this.mDelay = delay;
        this.countDownListener = listener;
    }

    public static CountDownUtil getInstance() {
        return getInstance(0L);
    }

    public static CountDownUtil getInstance(long allTime) {
        return getInstance(allTime, 1000L);
    }

    public static CountDownUtil getInstance(long allTime, long delay) {
        return getInstance(allTime, delay, (CountDownUtil.CountDownListener)null);
    }

    public static CountDownUtil getInstance(long allTime, long delay, CountDownUtil.CountDownListener listener) {
        return new CountDownUtil(allTime, delay, listener);
    }

    public CountDownUtil setTime(long time) {
        if (time != 0L) {
            this.mTime = time;
        } else {
            LogUtil.logE(this, "setDelay", "倒计时总时长不能为0");
        }

        return this;
    }

    public CountDownUtil setDelay(long delay) {
        if (delay != 0L) {
            this.mDelay = delay;
        } else {
            LogUtil.logE(this, "setDelay", "倒计时间隔不能为0");
        }

        return this;
    }

    public boolean isCountDown() {
        return this.mStatus != 0;
    }

    public void prepare() {
        if (this.mHandler != null) {
            this.mHandler.removeCallbacksAndMessages((Object)null);
        }

        if (this.mTime != 0L) {
            this.mStatus = 1;
            this.useOnPrepare(this.mTime);
        } else {
            LogUtil.logE(this, "start", "倒计时总时长不能为0");
        }

    }

    public void start() {
        if (this.mTime != 0L) {
            this.mStatus = 2;
            this.mEndTime = System.currentTimeMillis() + this.mTime;
            this.getHandler().removeCallbacksAndMessages((Object)null);
            this.getHandler().sendEmptyMessage(2);
        } else {
            LogUtil.logE(this, "start", "倒计时总时长不能为0");
        }

    }

    public void stop() {
        this.mStatus = 0;
        if (this.mHandler != null) {
            this.mHandler.removeCallbacksAndMessages((Object)null);
        }

        this.useOnFinish();
    }

    public void destroy() {
        if (this.mHandler != null) {
            this.mHandler.removeCallbacksAndMessages((Object)null);
            this.mHandler = null;
        }

        this.countDownListener = null;
    }

    public long getRemainTime() {
        return this.mEndTime - System.currentTimeMillis();
    }

    public CountDownUtil setCountDownListener(CountDownUtil.CountDownListener listener) {
        this.countDownListener = listener;
        return this;
    }

    private Handler getHandler() {
        if (this.mHandler == null) {
            this.mHandler = new Handler() {
                public void handleMessage(Message msg) {
                    super.handleMessage(msg);
                    switch(msg.what) {
                        case 2:
                            long remainTime = CountDownUtil.this.getRemainTime();
                            CountDownUtil.this.useOnStart(remainTime);
                            if (remainTime > 0L) {
                                this.sendEmptyMessageDelayed(2, CountDownUtil.this.mDelay);
                            } else {
                                CountDownUtil.this.mStatus = 0;
                                CountDownUtil.this.useOnFinish();
                            }
                        default:
                    }
                }
            };
        }

        return this.mHandler;
    }

    private void useOnPrepare(long remainTime) {
        if (this.countDownListener != null) {
            this.countDownListener.onPrepare(remainTime);
        }

    }

    private void useOnStart(long remainTime) {
        if (this.countDownListener != null) {
            this.countDownListener.onStart(remainTime);
        }

    }

    private void useOnFinish() {
        if (this.countDownListener != null) {
            this.countDownListener.onFinish();
        }

    }

    public interface CountDownListener {
        void onPrepare(long var1);

        void onStart(long var1);

        void onFinish();
    }
}
