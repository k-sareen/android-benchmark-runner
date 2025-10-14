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

public class DiscordMessageAndCallTest extends Benchmark {
    static String PACKAGE_NAME = "com.discord";
    static String ACTIVITY_NAME = "com.discord.main.MainDefault";
    static String CHAT_BAR = "com.discord:id/chat_input_edit_text";
    static String IMAGE_CONTAINER = "com.discord:id/container";
    static String POST_ACCESSORIES = "com.discord:id/accessories_view";
    static String UPLOAD_PROGRESS = "com.discord:id/upload_progress";

    public DiscordMessageAndCallTest(PrintStream writer) {
        super(PACKAGE_NAME, ACTIVITY_NAME, writer);
    }

    @Override
    public boolean iterate() {
        try {
            UiObject2 chatBar = device.wait(Until.findObject(By.res(CHAT_BAR)), 6000);
            if (chatBar == null) {
                Log.i(LOG_TAG, "FAILED: Main page did not load in time");
                return false;
            }

            device.waitForIdle();

            chatBar.click();
            device.waitForIdle();
            Thread.sleep(500);

            simulateTyping("Starting the benchmark!");
            device.pressEnter();
            device.waitForIdle();
            Thread.sleep(500);

            device.pressBack();
            device.waitForIdle();

            boolean found = device.wait(Until.hasObject(By.res(CHAT_BAR)), 6000);
            if (!found) {
                Log.i(LOG_TAG, "FAILED: Could not find chat bar");
                return false;
            }

            UiObject2 attach = device.wait(Until.findObject(By.desc("Toggle media keyboard")), 6000);
            if (attach == null) {
                Log.i(LOG_TAG, "FAILED: Could not find attach media button");
                return false;
            }

            attach.click();
            device.waitForIdle();

            attach = device.wait(Until.findObject(By.desc("Photo")), 6000);
            if (attach == null) {
                Log.i(LOG_TAG, "FAILED: Could not find pictures");
                return false;
            }

            attach.click();
            device.waitForIdle();
            Thread.sleep(500);

            chatBar = device.wait(Until.findObject(By.res(CHAT_BAR)), 6000);
            if (chatBar == null) {
                Log.i(LOG_TAG, "FAILED: Could not find chat bar");
                return false;
            }

            chatBar.click();
            device.waitForIdle();
            Thread.sleep(1000);

            simulateTyping("Check this picture out!");
            device.pressEnter();
            device.waitForIdle();

            found = device.wait(Until.hasObject(By.res(UPLOAD_PROGRESS)), 2000);
            if (!found) {
                Log.i(LOG_TAG, "FAILED: Could not find upload progress bar");
                return false;
            }
            device.waitForIdle();

            found = device.wait(Until.gone(By.res(UPLOAD_PROGRESS)), 8000);
            if (!found) {
                Log.i(LOG_TAG, "FAILED: Upload did not finish in timeout");
                return false;
            }
            device.waitForIdle();

            // XXX(kunals): Sometimes the message has not been sent even though the upload progress
            // bar has disappeared. Wait until the message object has refreshed before reacting.
            found = device.wait(
                    Until.gone(By.res(IMAGE_CONTAINER)
                            .hasChild(By.clazz("android.view.ViewGroup")
                                    .desc(""))), 4000);
            if (!found) {
                Log.i(LOG_TAG, "FAILED: Message did not send in timeout");
                return false;
            }
            device.waitForIdle();

            UiObject2 message = device.wait(Until.findObject(By.text("Check this picture out!")), 6000);
            if (message == null) {
                Log.i(LOG_TAG, "FAILED: Could not find message");
                return false;
            }

            message.longClick();
            device.waitForIdle();

            UiObject2 reaction = device.wait(
                    Until.findObject(By.desc("Add Reaction: thumbsup")), 8000);
            if (reaction == null) {
                Log.i(LOG_TAG, "FAILED: Could not find reaction");
                return false;
            }

            reaction.click();
            device.waitForIdle();

            device.pressBack();
            device.waitForIdle();
            device.pressBack();
            device.waitForIdle();

            UiObject2 voice = device.wait(
                    Until.findObject(By.descContains("General (voice channel)")), 6000);
            if (voice == null) {
                Log.i(LOG_TAG, "FAILED: Could not find voice channels");
                return false;
            }

            voice.click();
            device.waitForIdle();

            voice = device.wait(Until.findObject(
                    By.clazz("android.widget.Button").desc("Join Voice")), 6000);
            if (voice == null) {
                Log.i(LOG_TAG, "FAILED: Could not find join voice button");
                return false;
            }

            voice.click();
            device.waitForIdle();

            Thread.sleep(7000);

            // XXX(kunals): Sigh. For the Pixel 4a, the voice button does not disappear, so we must check
            // if it exists before we try to make it appear
            voice = device.wait(Until.findObject(
                    By.clazz("android.widget.Button").desc("Disconnect")), 500);
            if (voice == null) {
                device.click(deviceWidth / 2, deviceHeight / 2);
                device.waitForIdle();

                voice = device.wait(Until.findObject(
                        By.clazz("android.widget.Button").desc("Disconnect")), 6000);
                if (voice == null) {
                    Log.i(LOG_TAG, "FAILED: Could not find disconnect button");
                    return false;
                }
            }

            voice.click();
            device.waitForIdle();

            found = device.wait(Until.hasObject(By.desc("Text Channels")), 6000);
            if (!found) {
                Log.i(LOG_TAG, "FAILED: Did not return to home page");
                return false;
            }

            return true;
        } catch (Throwable t) {
            t.printStackTrace(writer);
            return false;
        }
    }
}
