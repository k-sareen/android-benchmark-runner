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

package org.anu.benchmarkrunner.adversaries;

import static org.anu.benchmarkrunner.BenchmarkRunner.LOG_TAG;

import android.util.Log;

import androidx.test.uiautomator.By;
import androidx.test.uiautomator.UiObject2;
import androidx.test.uiautomator.Until;

import org.anu.benchmarkrunner.Adversary;

import java.io.PrintStream;

public class AmazonShopping extends Adversary {
    static String PACKAGE_NAME = "com.amazon.mShop.android.shopping";
    static String ACTIVITY_NAME = "com.amazon.mShop.home.HomeActivity";
    static String PAGE_CONTAINER = "com.amazon.mShop.android.shopping:id/mash_web_fragment";
    static String SEARCHBOX = "com.amazon.mShop.android.shopping:id/chrome_search_hint_view";

    public AmazonShopping(PrintStream writer) {
        super(PACKAGE_NAME, ACTIVITY_NAME, writer);
    }

    @Override
    public boolean iterate() {
        try {
            boolean found = device.wait(Until.hasObject(By.res(SEARCHBOX)), 8000);
            if (!found) {
                Log.i(LOG_TAG, "FAILED: Could not load main page in time");
                return false;
            }

            Thread.sleep(500);

            for (int i = 0; i < 3; i++) {
                device.swipe(deviceWidth / 2, 80 * deviceHeight / 100,
                        deviceWidth / 2, 20 * deviceHeight / 100, 30);
                device.waitForIdle();
                Thread.sleep(750);
            }

            found = device.wait(Until.hasObject(By.res(SEARCHBOX)), 4000);
            if (!found) {
                Log.i(LOG_TAG, "FAILED: Could not find main page in time");
                return false;
            }

            return true;
        } catch (Throwable t) {
            t.printStackTrace(writer);
            return false;
        }
    }
}
