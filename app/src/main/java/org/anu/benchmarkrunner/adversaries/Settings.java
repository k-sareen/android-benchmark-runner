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

public class Settings extends Adversary {
    static String PACKAGE_NAME = "com.android.settings";
    static String ACTIVITY_NAME = "com.android.settings.Settings";
    static String HOMEPAGE_CONTAINER = "com.android.settings:id/settings_homepage_container";

    public Settings(PrintStream writer) {
        super(PACKAGE_NAME, ACTIVITY_NAME, writer);
    }

    @Override
    public boolean iterate() {
        try {
            UiObject2 homepage = device.wait(Until.findObject(By.res(HOMEPAGE_CONTAINER)), 3000);
            if (homepage == null) {
                Log.i(LOG_TAG, "Could not find settings homepage");
                return false;
            }

            Thread.sleep(100);

            for (int i = 0; i < 3; i++) {
                device.swipe(deviceWidth / 2, 60 * deviceHeight / 100,
                        deviceWidth / 2, 20 * deviceHeight / 100, 40);
                Thread.sleep(750);
            }

            homepage = device.wait(Until.findObject(By.res(HOMEPAGE_CONTAINER)), 3000);
            if (homepage == null) {
                Log.i(LOG_TAG, "Could not find settings");
                return false;
            }

            return true;
        } catch (InterruptedException e) {
            e.printStackTrace(writer);
            return false;
        }
    }
}
