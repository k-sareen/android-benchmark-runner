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

public class Firefox extends Adversary {
    static String PACKAGE_NAME = "org.mozilla.firefox";
    static String ACTIVITY_NAME = "org.mozilla.firefox.App";
    static String ENGINE_VIEW = "org.mozilla.firefox:id/engineView";
    static String TOOLBAR = "org.mozilla.firefox:id/toolbar";

    public Firefox(PrintStream writer) {
        super(PACKAGE_NAME, ACTIVITY_NAME, writer);
    }

    @Override
    public boolean iterate() {
        try {
            UiObject2 toolBar = device.wait(Until.findObject(By.res(TOOLBAR)), 4000);
            if (toolBar == null) {
                Log.i(LOG_TAG, "FAILED: Could not find toolbar in time");
                return false;
            }

            toolBar.click();
            device.waitForIdle();
            Thread.sleep(500);

            simulateTyping("The Earthsea Cycle");
            device.pressEnter();
            device.waitForIdle();

            UiObject2 results = device.wait(Until.findObject(By.text("Earthsea - Wikipedia")), 3000);
            if (results == null) {
                Log.i(LOG_TAG, "Could not load search results in time");
                return false;
            }

            Thread.sleep(500);

            for (int i = 0; i < 4; i++) {
                device.swipe(deviceWidth / 2, 80 * deviceHeight / 100,
                        deviceWidth / 2, 20 * deviceHeight / 100, 30);
                device.waitForIdle();
                Thread.sleep(750);
            }

            UiObject2 engineView = device.wait(Until.findObject(By.res(ENGINE_VIEW)), 4000);
            if (engineView == null) {
                Log.i(LOG_TAG, "FAILED: Could not find Firefox");
                return false;
            }

            return true;
        } catch (Throwable t) {
            t.printStackTrace(writer);
            return false;
        }
    }
}
