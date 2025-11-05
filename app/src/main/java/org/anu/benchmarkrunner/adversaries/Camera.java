/*
 * Copyright 2025 Kunal Sareen
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
import androidx.test.uiautomator.Until;

import org.anu.benchmarkrunner.Adversary;

import java.io.PrintStream;

public class Camera extends Adversary {
    static String PACKAGE_NAME = "org.lineageos.aperture";
    static String ACTIVITY_NAME = "org.lineageos.aperture.CameraLauncher";
    static String FLIP_CAMERA_BUTTON = "org.lineageos.aperture:id/flipCameraButton";

    public Camera(PrintStream writer) { super(PACKAGE_NAME, ACTIVITY_NAME, writer); }

    @Override
    public boolean iterate() {
        try {
            boolean found = device.wait(Until.hasObject(By.res(FLIP_CAMERA_BUTTON)), 6000);
            if (!found) {
                Log.i(LOG_TAG, "FAILED: Could not open camera app");
                return false;
            }
            device.waitForIdle();

            Thread.sleep(3000);

            found = device.wait(Until.hasObject(By.res(FLIP_CAMERA_BUTTON)), 6000);
            if (!found) {
                Log.i(LOG_TAG, "FAILED: Can't find flip camera button");
                return false;
            }
            device.waitForIdle();

            return true;
        } catch (Throwable t) {
            t.printStackTrace(writer);
            return false;
        }
    }
}
