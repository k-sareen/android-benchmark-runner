/*
 * Copyright 2024 Kunal Sareen
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.anu.benchmarkrunner.bms;

import static org.anu.benchmarkrunner.BenchmarkRunner.LOG_TAG;

import android.util.Log;

import androidx.test.uiautomator.By;
import androidx.test.uiautomator.UiObject2;
import androidx.test.uiautomator.Until;

import org.anu.benchmarkrunner.Benchmark;

import java.io.PrintStream;

public class GoogleNewsScrollTest extends Benchmark {
    static String PACKAGE_NAME = "com.google.android.apps.magazines";
    static String ACTIVITY_NAME = "com.google.apps.dots.android.app.activity.CurrentsStartActivity";
    static String HEADLINES_TAB = "com.google.android.apps.magazines:id/tab_headlines";
    static String MAIN_PAGE_HEADER = "com.google.android.apps.magazines:id/shelf_header_title";
    static String NEWS_IMAGE = "com.google.android.apps.magazines:id/image";

    public GoogleNewsScrollTest(PrintStream writer) {
        super(PACKAGE_NAME, ACTIVITY_NAME, writer);
    }

    @Override
    public boolean iterate() {
        try {
            boolean found = device.wait(Until.hasObject(By.res(MAIN_PAGE_HEADER)), 6000);
            if (!found) {
                Log.i(LOG_TAG, "Main page did not load in time");
                return false;
            }

            UiObject2 headlines = device.wait(Until.findObject(By.res(HEADLINES_TAB)), 6000);
            if (headlines == null) {
                Log.i(LOG_TAG, "Could not find headlines tab");
                return false;
            }

            headlines.click();
            device.waitForIdle();
            found = device.wait(Until.hasObject(By.res(NEWS_IMAGE)), 6000);
            if (!found) {
                Log.i(LOG_TAG, "Could not find headlines");
                return false;
            }

            headlines = device.wait(Until.findObject(By.desc("World")), 6000);
            if (headlines == null) {
                Log.i(LOG_TAG, "Could not find world headlines tab");
                return false;
            }

            // XXX: For some reason the click doesn't properly register at times. So double click
            // to force it to switch to the "World" headlines tab
            device.waitForIdle();
            headlines.click();
            device.waitForIdle();
            headlines.click();
            device.waitForIdle();

            found = device.wait(Until.hasObject(By.res(NEWS_IMAGE)), 6000);
            if (!found) {
                Log.i(LOG_TAG, "Could not find world headlines");
                return false;
            }

            for (int i = 0; i < 10; i++) {
                device.swipe(deviceWidth / 2, 70 * deviceHeight / 100,
                        deviceWidth / 2, 30 * deviceHeight / 100, 20);
                device.waitForIdle();
                Thread.sleep(500);
                found = device.wait(Until.hasObject(By.res(NEWS_IMAGE)), 500);
                if (!found) {
                    Log.i(LOG_TAG, "Could not find news articles");
                    return false;
                }
            }

            found = device.wait(Until.hasObject(By.res(NEWS_IMAGE)), 500);
            if (!found) {
                Log.i(LOG_TAG, "Did not finish benchmark with news articles visible");
                return false;
            }

            return true;
        } catch (Throwable t) {
            t.printStackTrace(writer);
            return false;
        }
    }
}
