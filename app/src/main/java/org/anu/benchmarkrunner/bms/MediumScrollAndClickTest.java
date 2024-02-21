package org.anu.benchmarkrunner.bms;

import static org.anu.benchmarkrunner.BenchmarkRunner.LOG_TAG;

import android.util.Log;

import androidx.test.uiautomator.By;
import androidx.test.uiautomator.UiObject2;
import androidx.test.uiautomator.Until;

import org.anu.benchmarkrunner.Benchmark;

import java.io.PrintStream;
import java.util.List;

public class MediumScrollAndClickTest extends Benchmark {
    static String PACKAGE_NAME = "com.medium.reader";
    static String ACTIVITY_NAME = "com.medium.android.donkey.start.SplashActivity";
    static String ARTICLE_TITLE = "com.medium.reader:id/common_item_paragraph_text";
    static String HOME_BUTTON = "com.medium.reader:id/splitHomeTabsFragment";
    static String POST = "com.medium.reader:id/post_preview_common_content";
    // static String POST_TITLE = "com.medium.reader:id/post_preview_title";

    public MediumScrollAndClickTest(PrintStream writer) {
        super(PACKAGE_NAME, ACTIVITY_NAME, writer);
    }

    @Override
    public boolean iterate() {
        try {
            boolean found = device.wait(Until.hasObject(By.res(POST)), 5000);
            if (!found) {
                Log.i(LOG_TAG, "Main page did not load in time");
                return false;
            }

            device.swipe(deviceWidth / 2, 50 * deviceHeight / 100,
                    deviceWidth / 2, 70 * deviceHeight / 100, 70);
            Thread.sleep(2500);

            found = device.wait(Until.hasObject(By.res(POST)), 5000);
            if (!found) {
                Log.i(LOG_TAG, "Refresh did not complete in time");
                return false;
            }

            for (int i = 0; i < 2; i++) {
                found = scrollAndInteractWithPosts();
                if (!found) {
                    return false;
                }
            }

            UiObject2 homeButton = device.wait(Until.findObject(By.res(HOME_BUTTON)), 5000);
            if (homeButton == null) {
                Log.i(LOG_TAG, "Could not return to main page in time");
                return false;
            }

            homeButton.click();
            device.waitForIdle();

            found = device.wait(Until.hasObject(By.res(POST)), 5000);
            if (!found) {
                Log.i(LOG_TAG, "Main page posts not found");
                return false;
            }

            return true;
        } catch (Throwable t) {
            t.printStackTrace();
            return false;
        }
    }

    boolean scrollAndInteractWithPosts() throws InterruptedException {
        boolean found;
        for (int i = 0; i < 3; i++) {
            found = device.wait(Until.hasObject(By.res(POST)), 5000);
            if (!found) {
                Log.i(LOG_TAG, "Posts not found");
                return false;
            }

            device.swipe(deviceWidth / 2, 70 * deviceHeight / 100,
                    deviceWidth / 2, 30 * deviceHeight / 100, 25);
            device.waitForIdle();
            Thread.sleep(750);
        }

        UiObject2 post;
        List<UiObject2> posts = device.findObjects(By.res(POST));
        if (posts.size() == 0) {
            Log.i(LOG_TAG, "Posts not found");
            return false;
        } else if (posts.size() == 1) {
            post = posts.get(0);
        } else {
            post = posts.get(1);
        }

        post.click();
        device.waitForIdle();

        found = device.wait(Until.hasObject(By.res(ARTICLE_TITLE)), 5000);
        if (!found) {
            Log.i(LOG_TAG, "Article did not load in time");
            return false;
        }
        Thread.sleep(500);

        for (int i = 0; i < 4; i++) {
            device.swipe(deviceWidth / 2, 70 * deviceHeight / 100,
                    deviceWidth / 2, 30 * deviceHeight / 100, 30);
            device.waitForIdle();
            Thread.sleep(750);
        }

        device.pressBack();
        Thread.sleep(750);

        found = device.wait(Until.hasObject(By.res(POST)), 5000);
        if (!found) {
            Log.i(LOG_TAG, "Did not make it back to main page in time");
            return false;
        }

        return true;
    }
}
