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

public class GmailScrollAndClickTest extends Benchmark {
    static String PACKAGE_NAME = "com.google.android.gm";
    static String ACTIVITY_NAME = "com.google.android.gm.ConversationListActivityGmail";
    static String CONVERSATION_CONTAINER = "com.google.android.gm:id/conversation_container";
    static String CONVERSATION_LIST = "com.google.android.gm:id/conversation_list_folder_header";
    static String EMAIL_SUBJECT = "com.google.android.gm:id/subject";
    static String EMAIL_SUBJECT_FOLDER_VIEW = "com.google.android.gm:id/subject_and_folder_view";
    static String REPLY_BUTTON = "com.google.android.gm:id/reply_button";

    public GmailScrollAndClickTest(PrintStream writer) {
        super(PACKAGE_NAME, ACTIVITY_NAME, writer);
    }

    @Override
    public boolean iterate() {
        try {
            boolean found = device.wait(Until.hasObject(By.res(CONVERSATION_LIST)), 6000);
            if (!found) {
                Log.i(LOG_TAG, "Main page did not load in time");
                return false;
            }

            UiObject2 email = device.wait(
                    Until.findObject(By.res(EMAIL_SUBJECT).text("Email for Benchmarking!")), 1000);
            if (email == null) {
                Log.i(LOG_TAG, "Could not find the email");
                return false;
            }

            email.click();
            device.waitForIdle();

            found = device.wait(Until.hasObject(
                    By.res(EMAIL_SUBJECT_FOLDER_VIEW).text("Email for Benchmarking! Inbox ")), 6000);
            if (!found) {
                Log.i(LOG_TAG, "Could not open email");
                return false;
            }

            device.waitForIdle();

            for (int i = 0; i < 7; i++) {
                device.swipe(deviceWidth / 2, 70 * deviceHeight / 100,
                        deviceWidth / 2, 30 * deviceHeight / 100, 20);
                device.waitForIdle();
                Thread.sleep(500);
                found = device.wait(Until.hasObject(By.res(CONVERSATION_CONTAINER)), 500);
                if (!found) {
                    Log.i(LOG_TAG, "Could not find the email body");
                    return false;
                }
            }

            found = device.wait(Until.hasObject(By.res(REPLY_BUTTON)), 2000);
            if (!found) {
                Log.i(LOG_TAG, "Did not reach end of email");
                return false;
            }

            return true;
        } catch (Throwable t) {
            t.printStackTrace(writer);
            return false;
        }
    }
}
