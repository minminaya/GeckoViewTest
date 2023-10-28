package com.minminaya.geckoviewtest;

import android.os.Handler;
import android.os.Looper;

/**
 * 在异步线程中,可通过此帮助类直接向主线程发送消息
 *
 * @author: lgm
 * @email: liguangmin@insta360.com
 * @date: 2022/8/22 16:18
 */
public class UIHelper {

    private volatile static Handler sHandler = null;

    private static boolean isTesting = false;

    private static Handler getInstance() {
        if (sHandler == null) {
            synchronized (UIHelper.class) {
                if (sHandler == null) {
                    sHandler = new Handler(Looper.getMainLooper());
                }
            }
        }
        return sHandler;
    }

    public static void removeCallback(Runnable runnable) {
        if (null != sHandler) {
            sHandler.removeCallbacks(runnable);
        }
    }

    public static void removeCallbacksAndMessages(Object token) {
        if (null != sHandler) {
            sHandler.removeCallbacksAndMessages(token);
        }
    }

    /**
     * 在主线程中执行
     */
    public static void runOnUiThread(Runnable runnable) {
        if (isRunningMainThread()) {
            runnable.run();
        } else {
            getInstance().post(runnable);
        }
    }

    /**
     * 在主线程中延时执行
     */
    public static void runOnUiThreadDelay(long delayMillis, Runnable runnable) {
        getInstance().postDelayed(runnable, delayMillis);
    }

    public static boolean isRunningMainThread() {
        return Looper.getMainLooper() == Looper.myLooper();
    }
}
