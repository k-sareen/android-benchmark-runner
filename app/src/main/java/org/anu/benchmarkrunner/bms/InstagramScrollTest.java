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
import androidx.test.uiautomator.BySelector;
import androidx.test.uiautomator.UiObject2;
import androidx.test.uiautomator.Until;

import org.anu.benchmarkrunner.Benchmark;

import java.io.PrintStream;
import java.util.List;
import java.util.regex.Pattern;

public class InstagramScrollTest extends Benchmark {
    static String PACKAGE_NAME = "com.instagram.android";
    static String ACTIVITY_NAME = "com.instagram.android.activity.MainTabActivity";
    static String FOLLOWING_BUTTON = "com.instagram.android:id/context_menu_item";
    static String MUTE_BUTTON = "com.instagram.android:id/indicator";
    static String POST = "com.instagram.android:id/(((carousel_(video_)?)?media_group)|carousel_image)";
    static String REEL = "com.instagram.android:id/clips_swipe_refresh_container";
    static String REELS_LIST = "reels_tray_container";
    static String REELS_TAB = "com.instagram.android:id/clips_tab";
    static String REELS_TITLE = "com.instagram.android:id/action_bar_large_title";
    static BySelector POST_SELECTOR = By.res(Pattern.compile(POST)).desc(Pattern.compile(".*(likes|Liked).*"));

    public InstagramScrollTest(PrintStream writer) {
        super(PACKAGE_NAME, ACTIVITY_NAME, writer);
        activityName = ACTIVITY_NAME;
    }

    @Override
    public boolean iterate() {
        try {
            boolean found = device.wait(Until.hasObject(By.desc(REELS_LIST)), 5000);
            if (!found) {
                Log.i(LOG_TAG, "Main page did not load in time");
                return false;
            }

            UiObject2 reels = device.wait(Until.findObject(By.res(REELS_TAB)), 5000);
            if (reels == null) {
                Log.i(LOG_TAG, "Could not find reels tab");
                return false;
            }

            reels.click();
            device.waitForIdle();
            Thread.sleep(100);

            // Pull down to refresh feed
            device.swipe(deviceWidth / 2, 40 * deviceHeight / 100,
                    deviceWidth / 2, 70 * deviceHeight / 100, 50);
            Thread.sleep(2750);

            for (int i = 0; i < 14; i++) {
                found = scrollPost();
                if (!found) {
                    return false;
                }
            }

            device.pressBack();
            device.waitForIdle();

            found = device.wait(Until.hasObject(By.text("Your story")), 2000);
            if (!found) {
                Log.i(LOG_TAG, "Did not make it back to top of feed");
                return false;
            }

            return true;
        } catch (Throwable t) {
            t.printStackTrace();
            return false;
        }
    }

    boolean scrollPost() throws InterruptedException {
        List<UiObject2> posts = device.wait(Until.findObjects(By.res(REEL)), 2000);
        if (posts == null || posts.isEmpty()) {
            Log.i(LOG_TAG, "No posts visible");
            return false;
        }

        device.swipe(deviceWidth / 2, 80 * deviceHeight / 100,
                deviceWidth / 2, 20 * deviceHeight / 100, 10);
        device.waitForIdle();
        Thread.sleep(600);

        return true;
    }
}
