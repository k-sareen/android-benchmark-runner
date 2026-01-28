package org.anu.benchmarkrunner.bms;

import static org.anu.benchmarkrunner.BenchmarkRunner.LOG_TAG;

import android.util.Log;

import androidx.test.uiautomator.By;
import androidx.test.uiautomator.UiObject2;
import androidx.test.uiautomator.Until;

import org.anu.benchmarkrunner.Benchmark;

import java.io.PrintStream;

public class GoogleNewsSetup extends Benchmark {
    static String PACKAGE_NAME = "com.google.android.apps.magazines";
    static String ACTIVITY_NAME = "com.google.apps.dots.android.app.activity.CurrentsStartActivity";

    public GoogleNewsSetup(PrintStream writer) {
        super(PACKAGE_NAME, ACTIVITY_NAME, writer);
    }

    @Override
    public boolean iterate() {
        try {
            boolean found = device.wait(Until.hasObject(By.pkg(PACKAGE_NAME)), 8000);
            if (!found) {
                Log.i(LOG_TAG, "FAILED: Main page did not load in time");
                return false;
            }

            Thread.sleep(4000);

            found = this.denyNotificationPermissions();
            if (!found) {
                Log.i(LOG_TAG, "FAILED: Could not deny notification permission");
                return false;
            }
            Thread.sleep(1000);

            found = device.wait(Until.hasObject(By.text("Top stories")), 6000);
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
