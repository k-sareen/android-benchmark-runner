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
    static BySelector NEXT_BUTTON_SELECTOR = By.desc("Show next");

    public MapsRoutePreviewTest(PrintStream writer) {
        super(PACKAGE_NAME, ACTIVITY_NAME, writer);
    }

    @Override
    public boolean iterate() {
        try {
            UiObject2 searchBar = device.wait(Until.findObject(By.text("Search here")), 6000);
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
            Thread.sleep(500);

            UiObject2 directionsButton = device.wait(Until.findObject(By.text("Directions")), 6000);
            if (directionsButton == null) {
                Log.i(LOG_TAG, "Location page did not load in time");
                return false;
            }

            directionsButton.click();
            device.waitForIdle();

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

            UiObject2 previewButton = device.wait(
                    Until.findObject(By.desc("Preview driving navigation")), 6000);
            if (previewButton == null) {
                Log.i(LOG_TAG, "Could not find preview route button");
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
                    Thread.sleep(1200);
                } else {
                    break;
                }
            }

            boolean found = device.wait(Until.hasObject(By.text("468 Pitt St")), 6000);
            if (!found) {
                Log.i(LOG_TAG, "Did not finish navigation at destination");
                return false;
            }

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
            found = device.wait(Until.hasObject(By.text("Search here")), 6000);
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
