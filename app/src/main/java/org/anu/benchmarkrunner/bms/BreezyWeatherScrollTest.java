package org.anu.benchmarkrunner.bms;

import static org.anu.benchmarkrunner.BenchmarkRunner.LOG_TAG;

import android.graphics.Rect;
import android.util.Log;

import androidx.test.uiautomator.By;
import androidx.test.uiautomator.UiObject2;
import androidx.test.uiautomator.Until;

import org.anu.benchmarkrunner.Benchmark;

import java.io.PrintStream;

public class BreezyWeatherScrollTest extends Benchmark {
    static String PACKAGE_NAME = "org.breezyweather";
    static String ACTIVITY_NAME = "org.breezyweather.main.MainActivity";
    static String SCROLL_DAILY_TREND = "org.breezyweather:id/item_trend_daily";
    static String SCROLL_HOURLY_TREND = "org.breezyweather:id/item_trend_hourly";
    static String TOOLBAR = "org.breezyweather:id/toolbar";

    public BreezyWeatherScrollTest(PrintStream writer) {
        super(PACKAGE_NAME, ACTIVITY_NAME, writer);
    }

    @Override
    public boolean iterate() {
        try {
            boolean found = device.wait(Until.hasObject(By.res(TOOLBAR)), 2000);
            if (!found) {
                Log.i(LOG_TAG, "App did not open in time");
                return false;
            }

            device.drag(deviceWidth / 2, 30 * deviceHeight / 100,
                    deviceWidth / 2, 50 * deviceHeight / 100, 20);
            Thread.sleep(2000);

            device.swipe(deviceWidth / 2, 70 * deviceHeight / 100,
                    deviceWidth / 2, 30 * deviceHeight / 100, 25);
            Thread.sleep(1500);
            device.swipe(deviceWidth / 2, 70 * deviceHeight / 100,
                    deviceWidth / 2, 30 * deviceHeight / 100, 25);
            Thread.sleep(1500);

            found = device.wait(Until.hasObject(By.text("Hourly forecast")), 2000);
            if (!found) {
                Log.i(LOG_TAG, "Could not find hourly forecast container in time");
                return false;
            }
            Thread.sleep(100);

            UiObject2 hourly = device.findObject(By.res(SCROLL_HOURLY_TREND));
            Rect hourlyBounds = hourly.getVisibleBounds();
            device.swipe(60 * deviceWidth / 100, hourlyBounds.centerY(),
                    hourlyBounds.centerX(), hourlyBounds.centerY(), 25);
            Thread.sleep(750);
            device.swipe(60 * deviceWidth / 100, hourlyBounds.centerY(),
                    hourlyBounds.centerX(), hourlyBounds.centerY(), 25);
            Thread.sleep(750);

            device.swipe(deviceWidth / 2, 30 * deviceHeight / 100,
                    deviceWidth / 2, 70 * deviceHeight / 100, 25);
            Thread.sleep(1500);

            UiObject2 daily = device.findObject(By.res(SCROLL_DAILY_TREND));
            Rect dailyBounds = daily.getVisibleBounds();
            device.swipe(60 * deviceWidth / 100, dailyBounds.centerY(),
                    dailyBounds.centerX(), dailyBounds.centerY(), 25);
            Thread.sleep(1000);

            device.swipe(deviceWidth / 2, 70 * deviceHeight / 100,
                    deviceWidth / 2, 30 * deviceHeight / 100, 5);
            Thread.sleep(1500);

            found = device.wait(Until.hasObject(By.text("Sun & moon")), 2000);
            if (!found) {
                Log.i(LOG_TAG, "Could not find sun & moon container in time");
                return false;
            }
            Thread.sleep(1000);

            return true;
        } catch (Throwable t) {
            t.printStackTrace();
            return false;
        }
    }
}
