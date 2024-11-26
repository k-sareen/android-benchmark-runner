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

public class RedditScrollTest extends Benchmark {
    static String PACKAGE_NAME = "com.reddit.frontpage";
    static String ACTIVITY_NAME = "launcher.default";
    static String FEED_BUTTON = "toolbar_control_feed_label";
    static String FEED_TYPE_LABEL = "toolbar_feed_type_label";
    static String MUTE_BUTTON = "com.reddit.frontpage:id/reddit_video_controls_mute";
    static String POST = "(promoted_)?post_unit";
    static String POST_CAROUSEL = "post_image_gallery_carousel";
    static String TRENDING_CAROUSEL = "trending_carousel";

    boolean muteButtonFound = false;

    public RedditScrollTest(PrintStream writer) {
        super(PACKAGE_NAME, ACTIVITY_NAME, writer);
    }

    @Override
    public boolean iterate() {
        try {
            boolean found = device.wait(Until.gone(By.desc("Loading…")), 8000);
            if (!found) {
                Log.i(LOG_TAG, "Main page did not load in time");
                return false;
            }
            device.waitForIdle();

            device.swipe(deviceWidth / 2, 20 * deviceHeight / 100,
                    deviceWidth / 2, 50 * deviceHeight / 100, 20);
            Thread.sleep(1000);

            found = device.wait(Until.gone(By.desc("Loading…")), 8000);
            if (!found) {
                Log.i(LOG_TAG, "Refresh did not finish in time");
                return false;
            }

            for (int i = 0; i < 8; i++) {
                found = scrollPost();
                if (!found) {
                    return false;
                }
            }

            UiObject2 homeButton = device.wait(Until.findObject(
                    By.clazz("android.widget.Button").desc("Home")),
                    2000);
            if (homeButton == null) {
                Log.i(LOG_TAG, "Home button not found");
                return false;
            }

            homeButton.click();
            device.waitForIdle();

            found = device.wait(Until.hasObject(By.res(FEED_BUTTON)), 5000);
            if (!found) {
                Log.i(LOG_TAG, "Did not return to top of main page in time");
                return false;
            }

            return true;
        } catch (Throwable t) {
            t.printStackTrace(writer);
            return false;
        }
    }

    boolean scrollPost() throws InterruptedException {
        List<UiObject2> posts = device.wait(Until.findObjects(By.res(Pattern.compile(POST))), 6000);
        if (posts == null || posts.isEmpty()) {
            Log.i(LOG_TAG, "Posts not found");
            return false;
        }

        boolean found;
        if (!muteButtonFound) {
            found = device.wait(Until.hasObject(By.res(MUTE_BUTTON)), 2000);
            if (found) {
                Thread.sleep(250);
                UiObject2 muteButton = device.findObject(By.res(MUTE_BUTTON));
                muteButton.click();
                Thread.sleep(500);
                muteButtonFound = true;
            }
        }

        device.swipe(deviceWidth / 2, 70 * deviceHeight / 100,
                deviceWidth / 2, 30 * deviceHeight / 100, 15);
        device.waitForIdle();
        Thread.sleep(1000);

        found = device.hasObject(By.res(Pattern.compile(POST)));
        if (!found) {
            Log.i(LOG_TAG, "Could not find post after scrolling");
            return false;
        }

        return true;
    }
}
