package org.anu.benchmarkrunner.bms;

import static org.anu.benchmarkrunner.BenchmarkRunner.LOG_TAG;

import android.util.Log;

import androidx.test.uiautomator.By;
import androidx.test.uiautomator.UiObject2;
import androidx.test.uiautomator.Until;

import org.anu.benchmarkrunner.Benchmark;

import java.io.PrintStream;

public class GCBench20xTest extends Benchmark {
    static String PACKAGE_NAME = "org.anu.gcbenchtest";
    static String ACTIVITY_NAME = "org.anu.gcbenchtest.MainActivity";

    public GCBench20xTest(PrintStream writer) {
        super(PACKAGE_NAME, ACTIVITY_NAME, writer);
    }

    @Override
    public boolean iterate() {
        try {
            UiObject2 gcbench = device.wait(Until.findObject(By.text("GCBench 20x")), 6000);
            if (gcbench == null) {
                Log.i(LOG_TAG, "FAILED: Main page did not load in time");
                return false;
            }

            device.waitForIdle();
            gcbench.click();
            device.waitForIdle();

            boolean finished = device.wait(Until.hasObject(By.textContains("Finished GCBench 20x")), 60000);
            if (!finished) {
                Log.i(LOG_TAG, "FAILED: Did not finish GCBench 20x in time!");
                return false;
            }

            return true;
        } catch (Throwable t) {
            t.printStackTrace(writer);
            return false;
        }
    }
}
