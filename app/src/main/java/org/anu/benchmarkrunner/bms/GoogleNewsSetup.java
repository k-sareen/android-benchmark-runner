/*
 * Copyright 2026 Kunal Sareen
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

public class GoogleNewsSetup extends Benchmark {
    static String PACKAGE_NAME = "com.google.android.apps.magazines";
    static String ACTIVITY_NAME = "com.google.apps.dots.android.app.activity.CurrentsStartActivity";

    public GoogleNewsSetup(PrintStream writer) {
        super(PACKAGE_NAME, ACTIVITY_NAME, writer);
    }

    @Override
    public boolean iterate() {
        try {
            Thread.sleep(10000);
            boolean found = this.denyNotificationPermissions();
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
