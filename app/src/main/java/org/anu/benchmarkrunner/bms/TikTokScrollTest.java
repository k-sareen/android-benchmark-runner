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

public class TikTokScrollTest extends Benchmark {
    static String PACKAGE_NAME = "com.zhiliaoapp.musically";
    static String ACTIVITY_NAME = "com.ss.android.ugc.aweme.splash.SplashActivity";
    static String POST = "com.zhiliaoapp.musically:id/view_rootview";

    public TikTokScrollTest(PrintStream writer) {
        super(PACKAGE_NAME, ACTIVITY_NAME, writer);
    }

    @Override
    public boolean iterate() {
        try {
            boolean found = device.wait(Until.hasObject(By.res(POST)), 8000);
            if (!found) {
                Log.i(LOG_TAG, "FAILED: Main page did not load in time");
                return false;
            }

            UiObject2 following = device.wait(Until.findObject(By.text("Following")), 6000);
            if (following == null) {
                Log.i(LOG_TAG, "FAILED: Could not find following tab");
                return false;
            }

            following.click();
            device.waitForIdle();

            found = device.wait(Until.hasObject(By.res(POST)), 8000);
            if (!found) {
                Log.i(LOG_TAG, "FAILED: Following tab did not load in time");
                return false;
            }

            device.swipe(deviceWidth / 2, 30 * deviceHeight / 100,
                    deviceWidth / 2, 70 * deviceHeight / 100, 20);
            Thread.sleep(1500);

            found = device.wait(Until.hasObject(By.res(POST)), 8000);
            if (!found) {
                Log.i(LOG_TAG, "FAILED: Following tab did not reload in time");
                return false;
            }

            for (int i = 0; i < 12; i++) {
                device.swipe(deviceWidth / 2, 70 * deviceHeight / 100,
                        deviceWidth / 2, 30 * deviceHeight / 100, 15);
                device.waitForIdle();
                Thread.sleep(500);

                found = device.wait(Until.hasObject(By.res(POST)), 100);
                if (!found) {
                    Log.i(LOG_TAG, "FAILED: Could not find post");
                    return false;
                }
            }

            return true;
        } catch (Throwable t) {
            t.printStackTrace(writer);
            return false;
        }
    }
}
