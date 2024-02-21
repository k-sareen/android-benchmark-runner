package org.anu.benchmarkrunner.bms;

import org.anu.benchmarkrunner.Benchmark;

import java.io.PrintStream;

public class SettingsTest extends Benchmark {
    static String PACKAGE_NAME = "com.android.settings";
    static String ACTIVITY_NAME = "com.android.settings.Settings";

    public SettingsTest(PrintStream writer) {
        super(PACKAGE_NAME, ACTIVITY_NAME, writer);
    }

    @Override
    public boolean iterate() {
        try {
            Thread.sleep(50);
            device.swipe(deviceWidth / 2, 80 * deviceHeight / 100,
                    deviceWidth / 2, 20 * deviceHeight / 100, 25);
            Thread.sleep(1000);
            return true;
        } catch (InterruptedException e) {
            e.printStackTrace();
            return false;
        }
    }
}
