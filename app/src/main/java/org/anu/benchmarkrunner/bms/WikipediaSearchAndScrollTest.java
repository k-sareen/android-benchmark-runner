package org.anu.benchmarkrunner.bms;

import static org.anu.benchmarkrunner.BenchmarkRunner.LOG_TAG;

import android.util.Log;

import androidx.test.uiautomator.By;
import androidx.test.uiautomator.UiObject2;
import androidx.test.uiautomator.Until;

import org.anu.benchmarkrunner.Benchmark;

import java.io.PrintStream;

public class WikipediaSearchAndScrollTest extends Benchmark {
    static String PACKAGE_NAME = "org.wikipedia";
    static String ACTIVITY_NAME = "org.wikipedia.main.MainActivity";
    static String ARTICLE_IMAGE = "org.wikipedia:id/view_page_header_image";
    static String CLOSE_TAB_BUTTON = "org.wikipedia:id/close_tab_button";
    static String FEATURED_ARTICLE_HEADER = "org.wikipedia:id/view_card_header_title";
    static String PAGE_TOOLBAR_BUTTON_SEARCH = "org.wikipedia:id/page_toolbar_button_search";
    static String SEARCH_CONTAINER = "org.wikipedia:id/search_container";
    static String SEARCH_SRC_TEXT = "org.wikipedia:id/search_src_text";
    static String TABS_BUTTON = "org.wikipedia:id/page_toolbar_button_tabs";

    public WikipediaSearchAndScrollTest(PrintStream writer) {
        super(PACKAGE_NAME, writer);
        activityName = ACTIVITY_NAME;
    }

    @Override
    public boolean iterate() {
        try {
            // Wait for the main page to render
            boolean found = device.wait(Until.hasObject(By.res(FEATURED_ARTICLE_HEADER)), 2000);
            if (!found) {
                Log.i(LOG_TAG, "Main page did not load in time");
                return false;
            }

            // Click on search bar
            UiObject2 searchBar = device.findObject(By.res(SEARCH_CONTAINER));
            searchBar.click();

            // Enter search text
            searchBar = device.findObject(By.res(SEARCH_SRC_TEXT));
            searchBar.click();
            // searchBar.setText("Canberra");
            simulateTyping("Canberra");
            device.pressEnter();

            // Wait until search request is successful and then click on first link
            found = device.wait(Until.hasObject(By.text("Capital city of Australia")), 2000);
            if (!found) {
                Log.i(LOG_TAG, "Cannot find first search result");
                return false;
            }
            device.click(deviceWidth / 2, 400);

            // Wait until webpage has loaded. Perform some scrolling actions
            found = device.wait(Until.hasObject(By.res(ARTICLE_IMAGE)), 2000);
            if (!found) {
                Log.i(LOG_TAG, "Cannot find first webpage");
                return false;
            }
            Thread.sleep(500);
            device.swipe(deviceWidth / 2, 70 * deviceHeight / 100,
                    deviceWidth / 2, 30 * deviceHeight / 100, 30);
            Thread.sleep(1000);
            device.swipe(deviceWidth / 2, 70 * deviceHeight / 100,
                    deviceWidth / 2, 30 * deviceHeight / 100, 30);
            Thread.sleep(1000);
            device.swipe(deviceWidth / 2, 40 * deviceHeight / 100,
                    deviceWidth / 2, 60 * deviceHeight / 100, 25);

            // Click the search bar again
            found = device.wait(Until.hasObject(By.res(PAGE_TOOLBAR_BUTTON_SEARCH)), 2000);
            if (!found) {
                Log.i(LOG_TAG, "Cannot find search bar");
                return false;
            }
            searchBar = device.findObject(By.res(PAGE_TOOLBAR_BUTTON_SEARCH));
            searchBar.click();

            // Enter search text
            device.wait(Until.hasObject(By.res(SEARCH_SRC_TEXT)), 2000);
            searchBar = device.findObject(By.res(SEARCH_SRC_TEXT));
            searchBar.click();
            // searchBar.setText("Australia");
            simulateTyping("Australia");
            device.pressEnter();

            // Wait until search request is successful and then click on first link
            found = device.wait(Until.hasObject(By.text("Country in Oceania")), 2000);
            if (!found) {
                Log.i(LOG_TAG, "Cannot find second search result");
                return false;
            }
            device.click(deviceWidth / 2, 400);

            // Wait until webpage has loaded. Perform some scrolling actions
            found = device.wait(Until.hasObject(By.res(ARTICLE_IMAGE)), 2000);
            if (!found) {
                Log.i(LOG_TAG, "Cannot find second webpage");
                return false;
            }
            Thread.sleep(500);

            device.swipe(deviceWidth / 2, 70 * deviceHeight / 100,
                    deviceWidth / 2, 30 * deviceHeight / 100, 30);
            Thread.sleep(1000);
            device.swipe(deviceWidth / 2, 70 * deviceHeight / 100,
                    deviceWidth / 2, 30 * deviceHeight / 100, 30);
            Thread.sleep(1000);
            device.swipe(deviceWidth / 2, 70 * deviceHeight / 100,
                    deviceWidth / 2, 30 * deviceHeight / 100, 30);
            Thread.sleep(1000);
            device.swipe(deviceWidth / 2, 20 * deviceHeight / 100,
                    deviceWidth / 2, 50 * deviceHeight / 100, 30);
            Thread.sleep(500);

            found = device.wait(Until.hasObject(By.res(TABS_BUTTON)), 2000);
            if (!found) {
                Log.i(LOG_TAG, "Tabs button is not visible");
                return false;
            }

            device.pressBack();
            device.waitForIdle();
            device.pressBack();
            device.waitForIdle();
            device.pressBack();
            device.waitForIdle();
            device.pressBack();
            Thread.sleep(500);

            found = device.wait(Until.hasObject(By.res(FEATURED_ARTICLE_HEADER)), 2000);
            if (!found) {
                Log.i(LOG_TAG, "Did not return to main page in time");
                return false;
            }

            return true;
        } catch (Throwable t) {
            t.printStackTrace();
            return false;
        }
    }
}
