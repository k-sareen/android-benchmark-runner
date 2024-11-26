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

package org.anu.benchmarkrunner;

import androidx.test.uiautomator.By;
import androidx.test.uiautomator.Until;

import java.io.PrintStream;

/**
 * An {@code Adversary} is a process that runs in the background, potentially increasing noise and
 * memory pressure for a target benchmark.
 * An {@code Adversary} reuses methods in {@link Benchmark} to setup, run, and teardown the
 * adversary application. We don't use the same class as {@code Benchmark} to avoid issues where we
 * might run the same benchmark as an adversary and also because it's cleaner code.
 */
public abstract class Adversary extends Benchmark {
    public Adversary(String benchmark, String activityName, PrintStream writer) {
        super(benchmark, activityName, writer);
    }

    @Override
    public void teardownIteration() {
        int startX = deviceWidth / 2;
        int startY = 70 * deviceHeight / 100;

        try {
            device.pressHome();
            Thread.sleep(250);

            device.pressRecentApps();
            device.wait(Until.hasObject(By.res(RECENT_APPS_SNAPSHOTS)), 1000);
            Thread.sleep(100);

            device.swipe(startX, startY, startX, 10, 5);
            Thread.sleep(250);

            device.pressHome();
            Thread.sleep(100);

            stopBenchmark();
        } catch (Throwable t) {
            t.printStackTrace(writer);
        }
    }
}
