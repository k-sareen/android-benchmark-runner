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

public class SpotifyListenAndSearchTest extends Benchmark {
    static String PACKAGE_NAME = "com.spotify.music";
    static String ACTIVITY_NAME = "com.spotify.music.MainActivity";
    static String CURRENT_TRACK_INFO = "com.spotify.music:id/track_info_view_title";
    static String FIND_SEARCH_FIELD = "com.spotify.music:id/find_search_field_text";
    static String SEARCH_BUTTON = "com.spotify.music:id/search_tab";
    static String TRACK_TITLE = "com.spotify.music:id/title";

    public SpotifyListenAndSearchTest(PrintStream writer) {
        super(PACKAGE_NAME, ACTIVITY_NAME, writer);
    }

    @Override
    public boolean iterate() {
        try {
            UiObject2 search = device.wait(Until.findObject(By.res(SEARCH_BUTTON)), 2500);
            if (search == null) {
                Log.i(LOG_TAG, "Main page did not load in time");
                return false;
            }

            search.click();
            device.waitForIdle();

            search = device.wait(Until.findObject(By.res(FIND_SEARCH_FIELD)), 2500);
            if (search == null) {
                Log.i(LOG_TAG, "Search page did not load in time");
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
                Log.i(LOG_TAG, "Search did not complete in time");
                return false;
            }

            result.click();
            Thread.sleep(100);
            device.waitForIdle();

            device.pressBack();
            Thread.sleep(100);

            search = device.wait(Until.findObject(By.res(FIND_SEARCH_FIELD)), 2500);
            if (search == null) {
                Log.i(LOG_TAG, "Search page did not reload in time");
                return false;
            }
            search.click();
            Thread.sleep(500);
            simulateTyping("Pink Floyd");
            device.pressEnter();

            boolean found = device.wait(Until.hasObject(By.text("Featuring Pink Floyd")), 2500);
            if (!found) {
                Log.i(LOG_TAG, "Search did not complete in time");
                return false;
            }
            Thread.sleep(200);
            device.swipe(deviceWidth / 2, 70 * deviceHeight / 100,
                    deviceWidth / 2, 30 * deviceHeight / 100, 30);
            Thread.sleep(2000);
            device.swipe(deviceWidth / 2, 70 * deviceHeight / 100,
                    deviceWidth / 2, 30 * deviceHeight / 100, 30);
            Thread.sleep(2000);
            device.swipe(deviceWidth / 2, 70 * deviceHeight / 100,
                    deviceWidth / 2, 30 * deviceHeight / 100, 30);
            Thread.sleep(2000);
            device.swipe(deviceWidth / 2, 30 * deviceHeight / 100,
                    deviceWidth / 2, 80 * deviceHeight / 100, 10);
            Thread.sleep(2000);
            device.swipe(deviceWidth / 2, 30 * deviceHeight / 100,
                    deviceWidth / 2, 80 * deviceHeight / 100, 30);
            Thread.sleep(1000);

            result = device.wait(Until.findObject(By.text("Artist")), 2500);
            if (result == null) {
                Log.i(LOG_TAG, "Can't find artist");
                return false;
            }
            result.click();
            Thread.sleep(100);

            result = device.wait(Until.findObject(By.text("Another Brick in the Wall, Pt. 2")), 2500);
            if (result == null) {
                Log.i(LOG_TAG, "Could not find song");
                return false;
            }
            Thread.sleep(100);

            result.click();
            Thread.sleep(1000);

            result = device.wait(Until.findObject(By.res(CURRENT_TRACK_INFO)), 2500);
            if (result == null) {
                Log.i(LOG_TAG, "Currently playing track info not found");
                return false;
            }
            if (!result.getText().equals("Another Brick in the Wall, Pt. 2")) {
                Log.i(LOG_TAG, "Incorrect currently playing track");
                return false;
            }

            device.pressBack();
            device.pressBack();
            Thread.sleep(1000);

            found = device.wait(Until.hasObject(By.res(SEARCH_BUTTON)), 2500);
            if (!found) {
                Log.i(LOG_TAG, "Did not return to home page");
                return false;
            }

            return true;
        } catch (Throwable t) {
            t.printStackTrace();
            return false;
        }
    }
}
