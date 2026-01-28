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

public class AcrobatSetup extends Benchmark {
    static String PACKAGE_NAME = "com.adobe.reader";
    static String ACTIVITY_NAME = "com.adobe.reader.AdobeReader";
    static String BOTTOM_TOOL_BAR = "com.adobe.reader:id/quick_tool_items_container";
    static String CONTEXT_MENU = "com.adobe.reader:id/context_board";
    static String PROFILE_BUTTON = "com.adobe.reader:id/profile";
    static String SKIP_BUTTON = "com.adobe.reader:id/skip_button";

    public AcrobatSetup(PrintStream writer) {
        super(PACKAGE_NAME, ACTIVITY_NAME, writer);
    }

    @Override
    public boolean iterate() {
        try {
            boolean found = device.wait(Until.hasObject(By.pkg(PACKAGE_NAME)), 5000);
            if (!found) {
                Log.i(LOG_TAG, "FAILED: Main page did not load in time");
                return false;
            }

            device.waitForIdle();
            Thread.sleep(1000);

            UiObject2 continueButton;
            for (int i = 0; i < 3; i++) {
                continueButton = device.wait(Until.findObject(By.textContains("Continue")), 10000);
                if (continueButton == null) {
                    Log.i(LOG_TAG, "FAILED: Continue button couldn't be found " + i);
                    return false;
                }

                continueButton.click();
                device.waitForIdle();

                Thread.sleep(1000);
            }

            Thread.sleep(1000);

            found = this.denyNotificationPermissions();
            if (!found) {
                Log.i(LOG_TAG, "FAILED: Could not deny notification permission");
                return false;
            }
            Thread.sleep(1000);

            continueButton = device.wait(Until.findObject(By.textContains("Continue")), 1000);
            if (continueButton == null) {
                Log.i(LOG_TAG, "FAILED: Continue button couldn't be found");
                return false;
            }

            continueButton.click();
            device.waitForIdle();

            Thread.sleep(1000);

            UiObject2 permission = device.wait(Until.findObject(By.textContains("Not now")), 1000);
            if (permission != null) {
                permission.click();
                device.waitForIdle();
                Thread.sleep(1000);
            }

            Thread.sleep(1000);

            device.click(deviceWidth / 2, deviceHeight / 2);
            device.waitForIdle();

            Thread.sleep(1000);

            permission = device.wait(Until.findObject(By.descContains("Files")), 5000);
            if (permission == null) {
                Log.i(LOG_TAG, "FAILED: Did not end up on main page after login workflow");
                return false;
            }

            Thread.sleep(1000);

            UiObject2 profile = device.wait(Until.findObject(By.res(PROFILE_BUTTON)), 2000);
            if (profile == null) {
                Log.i(LOG_TAG, "FAILED: Could not find Profile button");
                return false;
            }

            profile.click();
            device.waitForIdle();
            Thread.sleep(1000);

            profile = device.wait(Until.findObject(By.textContains("Preferences")), 2000);
            if (profile == null) {
                Log.i(LOG_TAG, "FAILED: Could not find Preferences button");
                return false;
            }

            profile.click();
            device.waitForIdle();
            Thread.sleep(1000);

            profile = device.wait(Until.findObject(By.textContains("Enable generative AI features in Acrobat")), 2000);
            if (profile == null) {
                Log.i(LOG_TAG, "FAILED: Could not find Gen AI button");
                return false;
            }

            profile.click();
            device.waitForIdle();
            Thread.sleep(1000);

            device.pressBack();
            device.waitForIdle();
            Thread.sleep(500);

            device.pressBack();
            device.waitForIdle();
            Thread.sleep(500);

            UiObject2 fileButton = device.wait(Until.findObject(By.descContains("Files")), 2000);
            if (fileButton == null) {
                Log.i(LOG_TAG, "FAILED: Files page did not load in time");
                return false;
            }

            fileButton.click();
            device.waitForIdle();

            fileButton = device.wait(Until.findObject(By.text("On this device")), 2000);
            if (fileButton == null) {
                Log.i(LOG_TAG, "FAILED: Could not find file selection button");
                return false;
            }

            fileButton = device.findObject(By.text("On this device"));
            fileButton.click();
            device.waitForIdle();

            UiObject2 pdfResult = device.wait(Until.findObject(By.text("SICP")), 2000);
            if (pdfResult == null) {
                Log.i(LOG_TAG, "FAILED: Could not find PDF");
                return false;
            }
            pdfResult.click();

            found = device.wait(Until.hasObject(By.res(BOTTOM_TOOL_BAR)), 5000);
            if (!found) {
                Log.i(LOG_TAG, "FAILED: PDF did not load in time");
                return false;
            }
            Thread.sleep(500);

            fileButton = device.wait(Until.findObject(By.res(SKIP_BUTTON).text("Skip")), 6000);
            fileButton.click();
            device.waitForIdle();

            Thread.sleep(1000);

            UiObject2 contextMenu = device.wait(Until.findObject(By.res(CONTEXT_MENU)), 2000);
            if (contextMenu == null) {
                Log.i(LOG_TAG, "FAILED: Context menu button did not load in time");
                return false;
            }
            Thread.sleep(100);
            contextMenu.click();
            device.waitForIdle();

            contextMenu = device.wait(Until.findObject(By.text("Bookmarks & Table of Contents")), 2000);
            if (contextMenu == null) {
                Log.i(LOG_TAG, "FAILED: Bookmarks button did not load in time");
                return false;
            }
            Thread.sleep(100);
            contextMenu.click();
            device.waitForIdle();

            contextMenu = device.wait(Until.findObject(By.text("Add bookmark to this page")), 2000);
            if (contextMenu == null) {
                Log.i(LOG_TAG, "FAILED: Bookmarks did not load in time");
                return false;
            }
            Thread.sleep(100);
            contextMenu.click();
            device.waitForIdle();

            return true;
        } catch (Throwable t) {
            t.printStackTrace(writer);
            return false;
        }
    }
}
