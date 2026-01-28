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

public class TikTokSetup extends Benchmark {
    static String PACKAGE_NAME = "com.zhiliaoapp.musically";
    static String ACTIVITY_NAME = "com.ss.android.ugc.aweme.splash.SplashActivity";
    static String POST = "com.zhiliaoapp.musically:id/view_rootview";

    public TikTokSetup(PrintStream writer) {
        super(PACKAGE_NAME, ACTIVITY_NAME, writer);
    }

    @Override
    public boolean iterate() {
        try {
            boolean found = device.wait(Until.hasObject(By.pkg(PACKAGE_NAME)), 8000);
            if (!found) {
                Log.i(LOG_TAG, "FAILED: Main page did not load in time");
                return false;
            }

            Thread.sleep(6000);

            found = device.wait(Until.hasObject(By.textContains("Use your sign-in for TikTok?")), 6000);
            if (found) {
                device.click(deviceWidth / 2, deviceHeight / 2);
                device.waitForIdle();
                Thread.sleep(1000);
            }

            UiObject2 loginButton =
                    device.wait(Until.findObject(By.textContains("Use phone / email / username")), 6000);
            if (loginButton == null) {
                Log.i(LOG_TAG, "FAILED: Could not find login button");
                return false;
            }
            loginButton.click();
            device.waitForIdle();
            Thread.sleep(500);

            loginButton = device.wait(Until.findObject(By.textContains("Email / Username")), 6000);
            if (loginButton == null) {
                Log.i(LOG_TAG, "FAILED: Could not find email button");
                return false;
            }
            loginButton.click();
            device.waitForIdle();
            Thread.sleep(500);

            String tiktokAccount = device.executeShellCommand("cat /data/local/tmp/tiktok-account");
            String[] splits = tiktokAccount.split("\n");
            String username = splits[0];
            String password = splits[1];

            simulateTyping(username);
            Thread.sleep(500);

            UiObject2 continueButton = device.wait(Until.findObject(By.textContains("Continue")), 4000);
            if (continueButton == null) {
                Log.i(LOG_TAG, "FAILED: Continue button couldn't be found");
                return false;
            }

            continueButton.click();
            device.waitForIdle();
            Thread.sleep(4000);

            found = device.wait(Until.hasObject(By.textContains("Enter password")), 8000);
            if (!found) {
                Log.i(LOG_TAG, "FAILED: Could not find enter password page");
                return false;
            }

            Thread.sleep(2000);

            simulateTyping(password);
            continueButton = device.wait(Until.findObject(By.textContains("Continue")), 4000);
            if (continueButton == null) {
                Log.i(LOG_TAG, "FAILED: Continue button couldn't be found");
                return false;
            }

            continueButton.click();
            device.waitForIdle();
            Thread.sleep(4000);

            found = this.denyNotificationPermissions();
            if (!found) {
                Log.i(LOG_TAG, "FAILED: Could not deny notification permission");
                return false;
            }

            continueButton = device.wait(Until.findObject(By.text("Skip")), 6000);
            continueButton.click();
            device.waitForIdle();

            Thread.sleep(1000);

            found = device.wait(Until.hasObject(By.res(POST)), 8000);
            if (!found) {
                Log.i(LOG_TAG, "FAILED: Main page did not load in time");
                return false;
            }

            device.swipe(deviceWidth / 2, 70 * deviceHeight / 100,
                    deviceWidth / 2, 30 * deviceHeight / 100, 15);
            device.waitForIdle();
            Thread.sleep(500);

            return true;
        } catch (Throwable t) {
            t.printStackTrace(writer);
            return false;
        }
    }
}
