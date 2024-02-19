package org.anu.benchmarkrunner.bms;

import static org.anu.benchmarkrunner.BenchmarkRunner.LOG_TAG;

import android.util.Log;

import androidx.test.uiautomator.By;
import androidx.test.uiautomator.UiObject2;
import androidx.test.uiautomator.Until;

import org.anu.benchmarkrunner.Benchmark;

import java.io.PrintStream;
import java.util.List;

public class BBCScrollAndClickTest extends Benchmark {
    static String PACKAGE_NAME = "bbc.mobile.news.ww";
    static String ACTIVITY_NAME = "uk.co.bbc.news.app.ui.GnlActivity";
    static String ARTICLE_LISTING = "bbc.mobile.news.ww:id/horizontal_promo_container";
    static String ARTICLE_LISTING_IMAGE = "bbc.mobile.news.ww:id/image";
    static String ARTICLE_READ_TIME = "bbc.mobile.news.ww:id/headline_read_time";
    static String COLLECTION_HEADER_TITLE = "bbc.mobile.news.ww:id/collection_header_title";
    static String FRONT_PAGE_ARTICLE = "bbc.mobile.news.ww:id/large_promo_container";
    static String TOPICS_BUTTON = "bbc.mobile.news.ww:id/topics_graph";

    public BBCScrollAndClickTest(PrintStream writer) {
        super(PACKAGE_NAME, writer);
        activityName = ACTIVITY_NAME;
    }

    @Override
    public boolean iterate() {
        try {
            boolean found = device.wait(Until.hasObject(By.clazz("android.widget.ImageView")), 8000);
            if (!found) {
                Log.i(LOG_TAG, "Main page did not load in time");
                return false;
            }

            device.swipe(deviceWidth / 2, 30 * deviceHeight / 100,
                    deviceWidth / 2, 50 * deviceHeight / 100, 20);
            Thread.sleep(3000);

            device.swipe(deviceWidth / 2, 70 * deviceHeight / 100,
                    deviceWidth / 2, 30 * deviceHeight / 100, 20);
            Thread.sleep(1500);
            device.swipe(deviceWidth / 2, 70 * deviceHeight / 100,
                    deviceWidth / 2, 30 * deviceHeight / 100, 20);
            Thread.sleep(1500);
            device.swipe(deviceWidth / 2, 70 * deviceHeight / 100,
                    deviceWidth / 2, 30 * deviceHeight / 100, 20);
            Thread.sleep(1500);

            List<UiObject2> newsResults = device.findObjects(By.res(ARTICLE_LISTING_IMAGE));
            UiObject2 newsResult;
            if (newsResults.size() > 1) {
                newsResult = newsResults.get(1);
            } else if (newsResults.size() > 0) {
                newsResult = newsResults.get(0);
            } else {
                Log.i(LOG_TAG, "Could not find news result");
                return false;
            }

            newsResult.click();
            found = device.wait(Until.hasObject(By.res(ARTICLE_READ_TIME)), 8000);
            if (!found) {
                Log.i(LOG_TAG, "Article did not load in time");
                return false;
            }

            device.swipe(deviceWidth / 2, 70 * deviceHeight / 100,
                    deviceWidth / 2, 30 * deviceHeight / 100, 20);
            Thread.sleep(1500);
            device.swipe(deviceWidth / 2, 70 * deviceHeight / 100,
                    deviceWidth / 2, 30 * deviceHeight / 100, 20);
            Thread.sleep(1500);
            device.swipe(deviceWidth / 2, 70 * deviceHeight / 100,
                    deviceWidth / 2, 30 * deviceHeight / 100, 20);
            Thread.sleep(1500);

            device.pressBack();
            found = device.wait(Until.hasObject(By.clazz("android.widget.ImageView")), 8000);
            if (!found) {
                Log.i(LOG_TAG, "Did not return to main page in time");
                return false;
            }

            UiObject2 exploreButton = device.findObject(By.res(TOPICS_BUTTON));
            exploreButton.click();
            Thread.sleep(200);

            found = device.wait(Until.hasObject(By.res(ARTICLE_LISTING_IMAGE)), 8000);
            if (!found) {
                Log.i(LOG_TAG, "Topics page did not load in time");
                return false;
            }

            newsResult = device.findObjects(By.res(ARTICLE_LISTING_IMAGE)).get(3);
            newsResult.click();
            Thread.sleep(2000);

            found = device.wait(Until.hasObject(By.res(ARTICLE_READ_TIME)), 8000);
            if (!found) {
                Log.i(LOG_TAG, "Article did not load in time");
                return false;
            }

            device.swipe(deviceWidth / 2, 70 * deviceHeight / 100,
                    deviceWidth / 2, 30 * deviceHeight / 100, 20);
            Thread.sleep(1500);
            device.swipe(deviceWidth / 2, 70 * deviceHeight / 100,
                    deviceWidth / 2, 30 * deviceHeight / 100, 20);
            Thread.sleep(1500);
            device.swipe(deviceWidth / 2, 70 * deviceHeight / 100,
                    deviceWidth / 2, 30 * deviceHeight / 100, 20);
            Thread.sleep(1500);

            device.pressBack();
            Thread.sleep(200);

            found = device.wait(Until.hasObject(By.res(COLLECTION_HEADER_TITLE)), 8000);
            if (!found) {
                Log.i(LOG_TAG, "Did not make it back to topics page in time");
                return false;
            }

            return true;
        } catch (Throwable t) {
            t.printStackTrace();
            return false;
        }
    }
}
