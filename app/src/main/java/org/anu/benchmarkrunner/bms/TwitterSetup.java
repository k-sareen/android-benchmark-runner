package org.anu.benchmarkrunner.bms;

import static org.anu.benchmarkrunner.BenchmarkRunner.LOG_TAG;

import android.util.Log;

import androidx.test.uiautomator.By;
import androidx.test.uiautomator.Until;

import org.anu.benchmarkrunner.Benchmark;

import java.io.PrintStream;

public class TwitterSetup extends Benchmark {
    static String PACKAGE_NAME = "com.twitter.android";
    static String ACTIVITY_NAME = "com.twitter.android.StartActivity";
    static String TIMELINE = "com.twitter.android:id/timeline_container";


    public TwitterSetup(PrintStream writer) {
        super(PACKAGE_NAME, ACTIVITY_NAME, writer);
    }

    @Override
    public boolean iterate() {
        try {
            Thread.sleep(6000);
            boolean found = this.denyNotificationPermissions();
            if (!found) {
                Log.i(LOG_TAG, "FAILED: Could not deny notification permission");
                return false;
            }
            Thread.sleep(1000);

            found = device.wait(Until.hasObject(By.res(TIMELINE)), 6000);
            if (!found) {
                Log.i(LOG_TAG, "FAILED: Did not end up at main page after login workflow");
                return false;
            }

            return true;
        } catch (Throwable t) {
            t.printStackTrace(writer);
            return false;
        }
    }
}
