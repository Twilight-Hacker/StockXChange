package com.example.galadar.stockxchange;

import android.app.IntentService;
import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v4.content.LocalBroadcastManager;
import android.widget.Toast;

/**
 * Created by Galadar on 1/10/2015.
 */

public class Update implements Runnable {

    private Object mPauseLock;
    private boolean mPaused;
    private boolean mFinished;
    private static final Daytime time = MainActivity.getClock();

    public Update(){
        mPauseLock = new Object();
        mPaused = false;
        mFinished = false;
    }

    @Override
    public void run() {
        while (!mFinished) {
            // Do stuff.
            try {
                synchronized (this) {
                    //TODO change wait from 5000ms to 10000ms (10 seconds)
                    wait(5000);
                }
            } catch (InterruptedException e) {
            }
            time.increment(10);

            synchronized (mPauseLock) {
                while (mPaused) {
                    try {
                        mPauseLock.wait();
                    } catch (InterruptedException e) {
                    }
                }
            }
        }
    }


    /**
     * Call this on pause.
     */
    public void onPause() {
        synchronized (mPauseLock) {
            mPaused = true;
        }
    }

    /**
     * Call this on resume.
     */
    public void onResume() {
        synchronized (mPauseLock) {
            mPaused = false;
            mPauseLock.notifyAll();
        }
    }

}