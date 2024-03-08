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

public class TwitchStreamTest extends Benchmark {
    static String PACKAGE_NAME = "tv.twitch.android.app";
    static String ACTIVITY_NAME = "tv.twitch.android.app.core.LandingActivity";
    static String BROADCAST_BUTTON = "tv.twitch.android.app:id/broadcast_button_animation";
    static String CREATE_BUTTON = "tv.twitch.android.app:id/swap_to_creator_mode_button";
    static String STREAM_IRL = "tv.twitch.android.app:id/option_stream_irl";
    static String STREAM_STATUS = "tv.twitch.android.app:id/live_status_indicator";

    public TwitchStreamTest(PrintStream writer) {
        super(PACKAGE_NAME, ACTIVITY_NAME, writer);
    }

    @Override
    public boolean iterate() {
        try {
            boolean found = device.wait(Until.hasObject(By.text("Channels Recommended For You")), 5000);
            if (!found) {
                Log.i(LOG_TAG, "Main page did not load in time");
                return false;
            }

            UiObject2 createButton = device.findObject(By.res(CREATE_BUTTON));
            createButton.click();
            device.waitForIdle();
            Thread.sleep(500);

            createButton = device.wait(Until.findObject(By.text("Create")), 8000);
            if (createButton == null) {
                Log.i(LOG_TAG, "Could not find create button");
                return false;
            }
            createButton.click();

            UiObject2 streamIrlButton = device.wait(Until.findObject(By.res(STREAM_IRL)), 5000);
            if (streamIrlButton == null) {
                Log.i(LOG_TAG, "Stream IRL button not found");
                return false;
            }

            device.waitForIdle();
            streamIrlButton.click();
            device.waitForIdle();

            UiObject2 broadcastButton = device.wait(Until.findObject(By.res(BROADCAST_BUTTON)), 5000);
            if (broadcastButton == null) {
                Log.i(LOG_TAG, "Broadcast button not found");
                return false;
            }

            UiObject2 streamStatus = device.findObject(
                    By.res(STREAM_STATUS).hasChild(By.clazz("android.widget.TextView")));
            streamStatus = streamStatus.findObject(By.clazz("android.widget.TextView"));
            if (!streamStatus.getText().equals("OFFLINE")) {
                Log.i(LOG_TAG, "Stream not offline");
                broadcastButton.click();
                device.waitForIdle();

                UiObject2 endStreamButton = device.wait(
                        Until.findObject(
                                By.clazz("android.widget.Button").text("End Stream")),
                        5000);
                if (endStreamButton == null) {
                    Log.i(LOG_TAG, "End stream button not found");
                    return false;
                }

                endStreamButton.click();
                device.waitForIdle();
                return false;
            }

            broadcastButton.click();
            device.waitForIdle();

            Thread.sleep(2000);
            found = device.wait(Until.hasObject(By.text("LIVE")), 8000);
            if (!found) {
                Log.i(LOG_TAG, "Stream not live");
                return false;
            }

            Thread.sleep(30000);

            broadcastButton = device.findObject(By.res(BROADCAST_BUTTON));
            broadcastButton.click();
            device.waitForIdle();

            UiObject2 endStreamButton = device.wait(
                    Until.findObject(
                            By.clazz("android.widget.Button").text("End Stream")),
                    5000);
            if (endStreamButton == null) {
                Log.i(LOG_TAG, "End stream button not found");
                return false;
            }

            endStreamButton.click();
            device.waitForIdle();

            device.pressBack();
            device.waitForIdle();
            device.pressBack();
            device.waitForIdle();

            // XXX: Annoyingly, it seems like the Pixel 4a (5G) requires 4 backs to get to the
            // main page, while the Pixel 7 Pro requires only 3 backs
            found = device.wait(Until.hasObject(By.text("Watch")), 500);
            if (!found) {
                device.pressBack();
                device.waitForIdle();
                found = device.wait(Until.hasObject(By.text("Watch")), 6000);
                if (!found) {
                    Log.i(LOG_TAG, "Timed out going back to create page");
                    return false;
                }
            }

            device.pressBack();
            device.waitForIdle();
            found = device.wait(Until.hasObject(By.text("Channels Recommended For You")), 8000);
            if (!found) {
                Log.i(LOG_TAG, "Timed out going back to main page");
                return false;
            }

            return true;
        } catch (Throwable t) {
            t.printStackTrace();
            return false;
        }
    }
}
