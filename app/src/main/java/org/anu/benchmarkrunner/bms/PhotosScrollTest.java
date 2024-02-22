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

public class PhotosScrollTest extends Benchmark {
    static String PACKAGE_NAME = "com.google.android.apps.photos";
    static String ACTIVITY_NAME = "com.google.android.apps.photos.home.HomeActivity";
    static String LIBRARY_TAB = "com.google.android.apps.photos:id/tab_library";
    static String PHOTO_CONTAINER = "com.google.android.apps.photos:id/photos_photofragment_components_background_photo_view";

    public PhotosScrollTest(PrintStream writer) {
        super(PACKAGE_NAME, ACTIVITY_NAME, writer);
    }

    @Override
    public boolean iterate() {
        try {
            UiObject2 libraryButton = device.wait(Until.findObject(By.res(LIBRARY_TAB)), 6000);
            if (libraryButton == null) {
                Log.i(LOG_TAG, "Main page did not load in time");
                return false;
            }

            libraryButton.click();
            device.waitForIdle();

            UiObject2 downloadGallery = device.wait(Until.findObject(By.text("Download")), 6000);
            if (downloadGallery == null) {
                Log.i(LOG_TAG, "Could not find downloads library");
                return false;
            }

            downloadGallery.click();
            device.waitForIdle();

            UiObject2 photo = device.wait(Until.findObject(
                    By.clazz("android.widget.ImageView").descContains("taken on")), 8000);
            if (photo == null) {
                Log.i(LOG_TAG, "Could not find picture");
                return false;
            }

            photo.click();
            device.waitForIdle();

            photo = device.wait(Until.findObject(By.res(PHOTO_CONTAINER)), 6000);
            if (photo == null) {
                Log.i(LOG_TAG, "Could not find fullscreen picture");
                return false;
            }

            photo.pinchOpen(0.6f);
            device.waitForIdle();
            photo.pinchOpen(0.5f);
            device.waitForIdle();
            Thread.sleep(1000);

            photo.click();
            Thread.sleep(50);
            photo.click();
            device.waitForIdle();
            Thread.sleep(500);

            for (int i = 0; i < 5; i++) {
                boolean found = device.wait(Until.hasObject(By.res(PHOTO_CONTAINER)), 6000);
                if (!found) {
                    Log.i(LOG_TAG, "Could not find fullscreen picture");
                    return false;
                }

                device.swipe(70 * deviceWidth / 100, deviceHeight / 2,
                        30 * deviceWidth / 100, deviceHeight / 2, 15);
                device.waitForIdle();
                Thread.sleep(500);
            }

            device.pressBack();
            device.waitForIdle();
            device.pressBack();
            device.waitForIdle();

            downloadGallery = device.wait(Until.findObject(By.text("Download")), 6000);
            if (downloadGallery == null) {
                Log.i(LOG_TAG, "Did not return to downloads library");
                return false;
            }

            return true;
        } catch (Throwable t) {
            t.printStackTrace();
            return false;
        }
    }
}
