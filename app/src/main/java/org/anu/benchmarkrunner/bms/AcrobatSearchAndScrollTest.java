package org.anu.benchmarkrunner.bms;

import static org.anu.benchmarkrunner.BenchmarkRunner.LOG_TAG;

import android.graphics.Rect;
import android.util.Log;
import android.view.InputEvent;

import androidx.test.uiautomator.By;
import androidx.test.uiautomator.UiObject2;
import androidx.test.uiautomator.Until;

import org.anu.benchmarkrunner.Benchmark;

import java.io.PrintStream;

public class AcrobatSearchAndScrollTest extends Benchmark {
    static String PACKAGE_NAME = "com.adobe.reader";
    static String ACTIVITY_NAME = "com.adobe.reader.AdobeReader";
    static String BOTTOM_TOOL_BAR = "com.adobe.reader:id/quick_tool_items_container";
    static String FILES_BUTTON = "com.adobe.reader:id/bottombaritem_document_connectors";
    static String PAGE_VIEW = "com.adobe.reader:id/pageView";
    static String SCROLL_BAR = "com.adobe.reader:id/verticalScrubber";
    static String SEARCH_TEXT_BUTTON = "com.adobe.reader:id/document_view_search_icon";
    static String TOOLBAR_CONTAINER = "com.adobe.reader:id/toolbar_items_container";

    static int CHAPTER_2_2_LINK_X_PER_MILLE = 277;
    static int CHAPTER_2_2_LINK_Y_PER_MILLE = 721;
    static int SCROLL_BAR_BUTTON_GAP = 6;
    static int SCROLL_BAR_BUTTON_HEIGHT = 85;
    static int TOTAL_PAGES = 883;

    public AcrobatSearchAndScrollTest(PrintStream writer) {
        super(PACKAGE_NAME, writer);
        activityName = ACTIVITY_NAME;
    }

    @Override
    public boolean iterate() {
        try {
            UiObject2 filesButton = device.wait(Until.findObject(By.res(FILES_BUTTON)), 2000);
            if (filesButton == null) {
                Log.i(LOG_TAG, "Main page did not load in time");
                return false;
            }

            filesButton.click();
            device.waitForIdle();

            filesButton = device.wait(Until.findObject(By.text("On this device")), 2000);
            if (filesButton == null) {
                Log.i(LOG_TAG, "Could not find file selection button");
                return false;
            }

            filesButton = device.findObject(By.text("On this device"));
            filesButton.click();
            device.waitForIdle();

            UiObject2 pdfResult = device.wait(Until.findObject(By.text("SICP")), 2000);
            if (pdfResult == null) {
                Log.i(LOG_TAG, "Could not find PDF");
                return false;
            }
            pdfResult.click();

            boolean found = device.wait(Until.hasObject(By.res(BOTTOM_TOOL_BAR)), 5000);
            if (!found) {
                Log.i(LOG_TAG, "PDF did not load in time");
                return false;
            }
            Thread.sleep(500);

            clickScrollBarAtPage(1);
            UiObject2 textBar = device.wait(Until.findObject(By.text("Enter page number")), 2000);
            if (textBar == null) {
                Log.i(LOG_TAG, "Page selection text box did not load in time");
                return false;
            }
            Thread.sleep(100);

            textBar.click();
            Thread.sleep(100);
            simulateTyping("4");
            device.pressEnter();
            Thread.sleep(500);

            // XXX: Sigh. Annoyingly device.click() seems to just end up selecting text instead of
            // actually clicking the link. So use the `input` command. This is a _really_ big hack.
            device.executeShellCommand("input tap 310 1265");
            Thread.sleep(1000);

            device.swipe(deviceWidth / 2, 70 * deviceHeight / 100,
                    deviceWidth / 2, 30 * deviceHeight / 100, 25);
            Thread.sleep(1000);
            device.swipe(deviceWidth / 2, 70 * deviceHeight / 100,
                    deviceWidth / 2, 30 * deviceHeight / 100, 20);
            Thread.sleep(1000);

            UiObject2 searchButton = device.wait(Until.findObject(By.res(SEARCH_TEXT_BUTTON)), 2000);
            if (searchButton == null) {
                Log.i(LOG_TAG, "Could not find the search button");
                return false;
            }

            searchButton.click();
            Thread.sleep(500);

            simulateTyping("Garbage Collection");
            device.pressEnter();
            Thread.sleep(1000);

            found = device.wait(Until.gone(By.text("Tap to Cancel")), 12000);
            if (!found) {
                Log.i(LOG_TAG, "Search did not complete in time");
                return false;
            }
            Thread.sleep(100);
            device.pressBack();
            Thread.sleep(500);

            device.swipe(deviceWidth / 2, 70 * deviceHeight / 100,
                    deviceWidth / 2, 30 * deviceHeight / 100, 25);
            Thread.sleep(1600);
            device.swipe(deviceWidth / 2, 70 * deviceHeight / 100,
                    deviceWidth / 2, 30 * deviceHeight / 100, 20);
            Thread.sleep(1600);
            device.swipe(deviceWidth / 2, 70 * deviceHeight / 100,
                    deviceWidth / 2, 30 * deviceHeight / 100, 20);
            Thread.sleep(1600);

            found = device.wait(Until.hasObject(By.res(TOOLBAR_CONTAINER)), 2000);
            if (!found) {
                Log.i(LOG_TAG, "Can't find toolbar container");
                return false;
            }

            clickScrollBarAtPage(754);
            textBar = device.wait(Until.findObject(By.text("Enter page number")), 2000);
            if (textBar == null) {
                Log.i(LOG_TAG, "Page selection text box did not load in time");
                return false;
            }
            Thread.sleep(100);

            textBar.click();
            Thread.sleep(100);
            simulateTyping("1");
            device.pressEnter();
            Thread.sleep(500);

            device.pressBack();
            Thread.sleep(100);
            device.pressBack();
            Thread.sleep(100);
            device.pressBack();
            Thread.sleep(100);

            found = device.wait(Until.hasObject(By.res(FILES_BUTTON)), 2000);
            if (!found) {
                Log.i(LOG_TAG, "Timed out while going back to main page");
                return false;
            }

            return true;
        } catch (Throwable t) {
            t.printStackTrace();
            return false;
        }
    }

    void clickScrollBarAtPage(int page) throws InterruptedException {
        UiObject2 scrollBar = device.findObject(By.res(SCROLL_BAR));
        Rect bounds = scrollBar.getVisibleBounds();

        int buttonMiddle = bounds.top + SCROLL_BAR_BUTTON_GAP + (SCROLL_BAR_BUTTON_HEIGHT / 2) - 1;
        int actualScrollHeight = bounds.bottom - buttonMiddle;

        device.click(bounds.centerX(), buttonMiddle + ((page * actualScrollHeight) / TOTAL_PAGES));
        Thread.sleep(100);
    }

    @Override
    public void teardownIteration() {
        int startX = deviceWidth / 2;
        int startY = 60 * deviceHeight / 100;

        // We override the default implementation since Adobe Acrobat opens two activities that
        // need to be closed at the end if the benchmark fails
        try {
            device.pressBack();
            Thread.sleep(250);

            device.pressHome();
            Thread.sleep(250);

            device.pressRecentApps();
            device.wait(Until.hasObject(By.res(RECENT_APPS_SNAPSHOTS)), 1000);
            Thread.sleep(100);

            device.swipe(startX, startY, startX, 10, 5);
            Thread.sleep(250);

            device.swipe(startX, startY, startX, 10, 5);
            Thread.sleep(250);

            device.pressHome();
            Thread.sleep(100);

            stopBenchmark();
        } catch (Throwable t) {
            t.printStackTrace();
        }
    }
}
