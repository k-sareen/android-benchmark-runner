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
import java.util.List;
import java.util.regex.Pattern;

public class TwitterScrollTest extends Benchmark {
    static String PACKAGE_NAME = "com.twitter.android";
    static String ACTIVITY_NAME = "com.twitter.android.StartActivity";
    static String EMPTY_HOME = "com.twitter.android:id/empty_title";
    static String HOME_BUTTON = "Home(. New items)?";
    static String MUTE_BUTTON = "com.twitter.android:id/audio_toggle_view";
    static String NAVIGATION_BUTTON = "Show navigation drawer";
    static String POST = "com.twitter.android:id/row";

    boolean muteButtonFound = false;

    public TwitterScrollTest(PrintStream writer) {
        super(PACKAGE_NAME, ACTIVITY_NAME, writer);
    }

    @Override
    public boolean iterate() {
        try {
            boolean found = device.wait(Until.hasObject(By.res(EMPTY_HOME)), 6000);
            if (!found) {
                Log.i(LOG_TAG, "Main page did not load in time");
                return false;
            }

            UiObject2 navigationButton = device.wait(Until.findObject(By.desc(NAVIGATION_BUTTON)), 5000);
            if (navigationButton == null) {
                Log.i(LOG_TAG, "Navigation button not found");
                return false;
            }
            navigationButton.click();
            device.waitForIdle();

            // Need to select the correct "Following" button. There are two, but the one we want does not have
            // a resource id. Hence add the extra requirement of an empty resource id.
            navigationButton = device.wait(Until.findObject(By.text("Following").res("")), 5000);
            if (navigationButton == null) {
                Log.i(LOG_TAG, "Following button not found");
                return false;
            }
            navigationButton.click();
            device.waitForIdle();

            navigationButton = device.wait(Until.findObject(By.text("@DreamPhil97")), 5000);
            if (navigationButton == null) {
                Log.i(LOG_TAG, "Followed users not found");
                return false;
            }
            navigationButton.click();
            device.waitForIdle();

            found = device.wait(Until.hasObject(By.res(POST)), 6000);
            if (!found) {
                Log.i(LOG_TAG, "User page did not load in time");
                return false;
            }

            device.swipe(deviceWidth / 2, 70 * deviceHeight / 100,
                    deviceWidth / 2, 50 * deviceHeight / 100, 15);
            device.waitForIdle();
            Thread.sleep(500);

            found = device.wait(Until.hasObject(By.res(MUTE_BUTTON)), 500);
            if (found) {
                Thread.sleep(250);
                UiObject2 muteButton = device.findObject(By.res(MUTE_BUTTON));
                muteButton.click();
                Thread.sleep(500);
                muteButtonFound = true;
            }

            for (int i = 0; i < 15; i++) {
                found = scrollPost();
                if (!found) {
                    return false;
                }
            }

            return true;
        } catch (Throwable t) {
            t.printStackTrace(writer);
            return false;
        }
    }

    boolean scrollPost() throws InterruptedException {
        List<UiObject2> posts = device.wait(Until.findObjects(By.res(POST)), 6000);
        if (posts == null || posts.isEmpty()) {
            Log.i(LOG_TAG, "Posts not found");
            return false;
        }

        boolean found;
        if (!muteButtonFound) {
            found = device.wait(Until.hasObject(By.res(MUTE_BUTTON)), 500);
            if (found) {
                Thread.sleep(250);
                UiObject2 muteButton = device.findObject(By.res(MUTE_BUTTON));
                muteButton.click();
                Thread.sleep(500);
                muteButtonFound = true;
            }
        }

        device.swipe(deviceWidth / 2, 70 * deviceHeight / 100,
                deviceWidth / 2, 30 * deviceHeight / 100, 20);
        device.waitForIdle();
        Thread.sleep(500);

        found = device.hasObject(By.res(POST));
        if (!found) {
            Log.i(LOG_TAG, "Could not find post after scrolling");
            return false;
        }

        return true;
    }
}
