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
            e.printStackTrace(writer);
            return false;
        }
    }
}
