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
import androidx.test.uiautomator.UiObject2;
import androidx.test.uiautomator.Until;

import org.anu.benchmarkrunner.Adversary;

import java.io.PrintStream;

public class Spotify extends Adversary {
    static String PACKAGE_NAME = "com.spotify.music";
    static String ACTIVITY_NAME = "com.spotify.music.MainActivity";
    static String MAIN_PAGE = "com.spotify.music:id/content";
    static String SEARCH_BUTTON = "com.spotify.music:id/search_tab";
    static String FIND_SEARCH_FIELD = "com.spotify.music:id/browse_search_bar_container";
    static String TRACK_TITLE = "com.spotify.music:id/title";

    public Spotify(PrintStream writer) { super(PACKAGE_NAME, ACTIVITY_NAME, writer); }

    @Override
    public boolean iterate() {
        try {
            UiObject2 search = device.wait(Until.findObject(By.descContains("Search,")), 2500);
            if (search == null) {
                Log.i(LOG_TAG, "FAILED: Main page did not load in time");
                return false;
            }

            search.click();
            device.waitForIdle();

            search = device.wait(Until.findObject(By.res(FIND_SEARCH_FIELD)), 2500);
            if (search == null) {
                Log.i(LOG_TAG, "FAILED: Search page did not load in time");
                return false;
            }
            search.click();
            Thread.sleep(500);
            simulateTyping("Paranoid Android");
            device.pressEnter();
            device.waitForIdle();

            UiObject2 result = device.wait(Until.findObject(
                    By.res(TRACK_TITLE).text("Paranoid Android")), 2500);
            if (result == null) {
                Log.i(LOG_TAG, "FAILED: Search did not complete in time");
                return false;
            }

            device.pressBack();
            device.waitForIdle();
            device.pressBack();
            device.waitForIdle();

            boolean found = device.wait(Until.hasObject(By.res(SEARCH_BUTTON)), 2500);
            if (!found) {
                Log.i(LOG_TAG, "FAILED: Did not return to main page");
                return false;
            }

            return true;
        } catch (Throwable t) {
            t.printStackTrace(writer);
            return false;
        }
    }
}
