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

public class MapsRoutePreviewTest extends Benchmark {
    static String PACKAGE_NAME = "com.google.android.apps.maps";
    static String ACTIVITY_NAME = "com.google.android.maps.MapsActivity";
    static String SEARCH_BOX = "com.google.android.apps.maps:id/search_omnibox_text_box";
    static String SET_TIME_BUTTON = "com.google.android.apps.maps:id/dialog_positive_button";
    static String TIME_PICKER = "android:id/numberpicker_input";
    static BySelector NEXT_BUTTON_SELECTOR = By.desc("Show next");

    public MapsRoutePreviewTest(PrintStream writer) {
        super(PACKAGE_NAME, ACTIVITY_NAME, writer);
    }

    @Override
    public boolean iterate() {
        try {
            UiObject2 searchBar = device.wait(Until.findObject(By.res(SEARCH_BOX)), 6000);
            if (searchBar == null) {
                Log.i(LOG_TAG, "Main page did not load in time");
                return false;
            }

            searchBar.click();
            device.waitForIdle();
            Thread.sleep(500);

            simulateTyping("Canberra Centre");
            device.pressEnter();
            device.waitForIdle();
            Thread.sleep(750);

            UiObject2 directionsButton = device.wait(Until.findObject(By.text("Directions")), 6000);
            if (directionsButton == null) {
                Log.i(LOG_TAG, "Location page did not load in time");
                return false;
            }

            directionsButton.click();
            device.waitForIdle();
            Thread.sleep(250);

            UiObject2 swapButton = device.wait(Until.findObject(By.desc("Swap start and destination")), 6000);
            if (swapButton == null) {
                Log.i(LOG_TAG, "Could not find swap start and destination button");
                return false;
            }

            swapButton.click();
            device.waitForIdle();

            searchBar = device.wait(Until.findObject(By.text("Choose destination")), 6000);
            if (searchBar == null) {
                Log.i(LOG_TAG, "Could not find destination search bar");
                return false;
            }

            searchBar.click();
            device.waitForIdle();
            Thread.sleep(500);

            simulateTyping("Central Station, Pitt Street");
            device.pressEnter();
            device.waitForIdle();
            Thread.sleep(500);

            boolean found = device.wait(
                    Until.hasObject(By.desc("Preview driving navigation")), 6000);
            if (!found) {
                Log.i(LOG_TAG, "Could not find preview route button");
                return false;
            }

            UiObject2 menu = device.wait(Until.findObject(By.desc("Overflow menu")), 1000);
            if (menu == null) {
                Log.i(LOG_TAG, "Could not find overflow menu");
                return false;
            }

            menu.click();
            device.waitForIdle();

            menu = device.wait(Until.findObject(By.text("Set depart or arrive time")), 6000);
            if (menu == null) {
                Log.i(LOG_TAG, "Could not find button to set time");
                return false;
            }

            menu.click();
            device.waitForIdle();

            UiObject2 timePicker = device.wait(Until.findObject(By.res(TIME_PICKER)), 6000);
            if (timePicker == null) {
                Log.i(LOG_TAG, "Could not find button to set hour");
                return false;
            }

            timePicker.click();
            device.waitForIdle();
            Thread.sleep(100);
            simulateTyping("09");

            try {
                timePicker = device.wait(Until.findObjects(By.res(TIME_PICKER)), 6000).get(1);
                if (timePicker == null) {
                    Log.i(LOG_TAG, "Could not find button to set minutes");
                    return false;
                }

                timePicker.click();
                device.waitForIdle();
                Thread.sleep(100);
                simulateTyping("00");
            } catch (Exception e) {
                e.printStackTrace();
                Log.i(LOG_TAG, "Could not find button to set minutes");
                return false;
            }

            timePicker = device.wait(Until.findObject(By.text("Today")), 6000);
            if (timePicker == null) {
                Log.i(LOG_TAG, "Could not find date button");
                return false;
            }

            timePicker.click();
            device.waitForIdle();
            Thread.sleep(100);

            timePicker = device.wait(Until.findObject(By.desc("Next month")), 6000);
            if (timePicker == null) {
                Log.i(LOG_TAG, "Could not find next month button");
                return false;
            }

            for (int i = 0; i < 10; i++) {
                timePicker.click();
                device.waitForIdle();
                Thread.sleep(50);
            }

            timePicker = device.wait(Until.findObject(By.text("1")), 6000);
            if (timePicker == null) {
                Log.i(LOG_TAG, "Could not find 1st of month");
                return false;
            }

            timePicker.click();
            device.waitForIdle();
            Thread.sleep(100);

            timePicker = device.wait(Until.findObject(By.text("OK")), 6000);
            if (timePicker == null) {
                Log.i(LOG_TAG, "Could not find OK button");
                return false;
            }

            timePicker.click();
            device.waitForIdle();
            Thread.sleep(250);

            timePicker = device.wait(Until.findObject(By.res(SET_TIME_BUTTON)), 6000);
            if (timePicker == null) {
                Log.i(LOG_TAG, "Could not find set time button");
                return false;
            }

            timePicker.click();
            device.waitForIdle();

            UiObject2 previewButton = device.wait(
                    Until.findObject(By.desc("Preview driving navigation")), 6000);
            if (previewButton == null) {
                Log.i(LOG_TAG, "Could not find preview route button after setting time");
                return false;
            }

            previewButton.click();
            device.waitForIdle();

            while (true) {
                UiObject2 nextButton = device.wait(Until.findObject(NEXT_BUTTON_SELECTOR), 6000);
                if (nextButton == null) {
                    Log.i(LOG_TAG, "Could not find next button");
                    return false;
                }

                if (nextButton.isEnabled()) {
                    nextButton.click();
                    device.waitForIdle();
                    Thread.sleep(1250);
                } else {
                    break;
                }
            }

            found = device.wait(Until.hasObject(By.text("Eddy Ave")), 6000);
            if (!found) {
                Log.i(LOG_TAG, "Did not finish navigation at destination");
                return false;
            }

            Thread.sleep(750);

            device.pressBack();
            device.waitForIdle();
            found = device.wait(
                    Until.hasObject(By.desc("Preview driving navigation")), 6000);
            if (!found) {
                Log.i(LOG_TAG, "Did not return to route preview screen");
                return false;
            }

            device.pressBack();
            device.waitForIdle();
            found = device.wait(Until.hasObject(By.text("Directions")), 6000);
            if (!found) {
                Log.i(LOG_TAG, "Did not return to location screen");
                return false;
            }

            device.pressBack();
            device.waitForIdle();
            found = device.wait(Until.hasObject(By.res(SEARCH_BOX)), 6000);
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
