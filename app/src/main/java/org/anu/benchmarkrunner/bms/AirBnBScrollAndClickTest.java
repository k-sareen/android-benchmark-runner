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

import android.graphics.Rect;
import android.util.Log;

import androidx.test.uiautomator.By;
import androidx.test.uiautomator.BySelector;
import androidx.test.uiautomator.UiObject2;
import androidx.test.uiautomator.Until;

import org.anu.benchmarkrunner.Benchmark;

import java.io.PrintStream;
import java.util.List;
import java.util.regex.Pattern;

public class AirBnBScrollAndClickTest extends Benchmark {
    static String PACKAGE_NAME = "com.airbnb.android";
    static String ACTIVITY_NAME = "com.airbnb.android.feat.splashscreen.SplashScreenActivity";
    static String POST = ".*(USD).*";
    static String SEARCH_BAR = "com.airbnb.android:id/search_bar";

    static BySelector POST_SELECTOR = By.clazz("android.view.View")
            .clickable(true).focusable(true).enabled(true)
            .hasDescendant(By.clazz("android.view.View").desc(Pattern.compile(POST)));

    public AirBnBScrollAndClickTest(PrintStream writer) {
        super(PACKAGE_NAME, ACTIVITY_NAME, writer);
    }

    @Override
    public boolean iterate() {
        try {
            boolean found = device.wait(Until.hasObject(By.res(SEARCH_BAR)), 5000);
            if (!found) {
                Log.i(LOG_TAG, "Main page did not load in time");
                return false;
            }

            UiObject2 categories = device.wait(Until.findObject(By.desc("Categories")), 2500);
            if (categories == null) {
                Log.i(LOG_TAG, "Could not find categories");
                return false;
            }

            UiObject2 button;
            List<UiObject2> buttons = categories.findObjects(
                    By.clazz("android.view.View")
                            .hasChild(By.clazz("android.widget.TextView")));
            if (buttons.isEmpty()) {
                Log.i(LOG_TAG, "Could not find category button");
                return false;
            } else {
                button = buttons.get(1);
            }

            // Don't scroll the "Icons" category, instead pick the next one
            button.click();
            device.waitForIdle();
            Thread.sleep(1000);

            List<UiObject2> posts = device.wait(Until.findObjects(POST_SELECTOR), 5000);
            if (posts.isEmpty()) {
                Log.i(LOG_TAG, "Posts did not load in time");
                return false;
            }

            device.swipe(deviceWidth / 2, 70 * deviceHeight / 100,
                    deviceWidth / 2, 30 * deviceHeight / 100, 28);
            device.waitForIdle();
            Thread.sleep(1000);

            for (int i = 0; i < 5; i++) {
                found = scrollPost();
                if (!found) {
                    return false;
                }
            }

            categories = device.wait(Until.findObject(By.desc("Categories")), 2500);
            if (categories == null) {
                Log.i(LOG_TAG, "Could not find categories again");
                return false;
            }

            buttons = categories.findObjects(
                    By.clazz("android.view.View")
                            .hasChild(By.clazz("android.widget.TextView")));
            if (buttons.isEmpty()) {
                Log.i(LOG_TAG, "Could not find category buttons again");
                return false;
            } else {
                button = buttons.get(3);
            }

            button.click();
            device.waitForIdle();
            Thread.sleep(1000);

            device.swipe(deviceWidth / 2, 70 * deviceHeight / 100,
                    deviceWidth / 2, 30 * deviceHeight / 100, 28);
            device.waitForIdle();
            Thread.sleep(1000);

            for (int i = 0; i < 5; i++) {
                found = scrollPost();
                if (!found) {
                    return false;
                }
            }

            return true;
        } catch (Throwable t) {
            t.printStackTrace();
            return false;
        }
    }

    boolean scrollPost() throws InterruptedException {
        UiObject2 post;
        List<UiObject2> posts = device.wait(Until.findObjects(POST_SELECTOR), 5000);
        if (posts == null || posts.isEmpty()) {
            Log.i(LOG_TAG, "No posts visible");
            return false;
        } else if (posts.size() == 1) {
            post = posts.get(0);
        } else {
            post = posts.get(1);
        }

        Rect postBounds = post.getVisibleBounds();
        device.swipe(75 * deviceWidth / 100, postBounds.centerY(),
                25 * deviceWidth / 100, postBounds.centerY(), 15);
        device.waitForIdle();
        Thread.sleep(500);

        device.swipe(75 * deviceWidth / 100, postBounds.centerY(),
                25 * deviceWidth / 100, postBounds.centerY(), 15);
        device.waitForIdle();
        Thread.sleep(500);

        device.swipe(deviceWidth / 2, 70 * deviceHeight / 100,
                deviceWidth / 2, 30 * deviceHeight / 100, 28);
        device.waitForIdle();
        Thread.sleep(750);

        post = device.wait(Until.findObject(POST_SELECTOR), 5000);
        if (post == null) {
            Log.i(LOG_TAG, "No post visible after scrolling");
            return false;
        }

        return true;
    }
}
