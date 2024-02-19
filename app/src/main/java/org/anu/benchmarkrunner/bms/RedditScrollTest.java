package org.anu.benchmarkrunner.bms;

import static org.anu.benchmarkrunner.BenchmarkRunner.LOG_TAG;

import android.graphics.Rect;
import android.util.Log;

import androidx.test.uiautomator.By;
import androidx.test.uiautomator.StaleObjectException;
import androidx.test.uiautomator.UiObject2;
import androidx.test.uiautomator.Until;

import org.anu.benchmarkrunner.Benchmark;

import java.io.PrintStream;
import java.util.List;
import java.util.regex.Pattern;

public class RedditScrollTest extends Benchmark {
    static String PACKAGE_NAME = "com.reddit.frontpage";
    static String ACTIVITY_NAME = "launcher.default";
    static String FEED_BUTTON = "toolbar_control_feed_label";
    static String FEED_TYPE_LABEL = "toolbar_feed_type_label";
    static String MUTE_BUTTON = "com.reddit.frontpage:id/reddit_video_controls_mute";
    static String POST = "(promoted_)?post_unit";
    static String POST_CAROUSEL = "post_image_gallery_carousel";
    static String TRENDING_CAROUSEL = "trending_carousel";

    boolean muteButtonFound = false;

    public RedditScrollTest(PrintStream writer) {
        super(PACKAGE_NAME, writer);
        activityName = ACTIVITY_NAME;
    }

    @Override
    public boolean iterate() {
        try {
            boolean found = device.wait(Until.gone(By.desc("Loading…")), 8000);
            if (!found) {
                Log.i(LOG_TAG, "Main page did not load in time");
                return false;
            }

            UiObject2 feedButton = device.wait(Until.findObject(By.res(FEED_BUTTON)), 2000);
            if (feedButton == null) {
                Log.i(LOG_TAG, "Feed button not found");
                return false;
            }

            feedButton.click();
            device.waitForIdle();

            feedButton = device.wait(Until.findObject(By.res(FEED_TYPE_LABEL).text("Popular")), 2500);
            if (feedButton == null) {
                Log.i(LOG_TAG, "Popular feed button not found");
                return false;
            }

            feedButton.click();
            device.waitForIdle();

            device.swipe(deviceWidth / 2, 20 * deviceHeight / 100,
                    deviceWidth / 2, 50 * deviceHeight / 100, 20);
            Thread.sleep(1000);

            found = device.wait(Until.gone(By.desc("Loading…")), 8000);
            if (!found) {
                Log.i(LOG_TAG, "Refresh did not finish in time");
                return false;
            }

            for (int i = 0; i < 9; i++) {
                found = scrollPost();
                if (!found) {
                    return false;
                }
            }

            UiObject2 homeButton = device.wait(Until.findObject(
                    By.clazz("android.widget.Button").desc("Home")),
                    2000);
            if (homeButton == null) {
                Log.i(LOG_TAG, "Home button not found");
                return false;
            }

            homeButton.click();
            device.waitForIdle();

            found = device.wait(Until.hasObject(By.res(TRENDING_CAROUSEL)), 3000);
            if (!found) {
                Log.i(LOG_TAG, "Did not return to top of main page in time");
                return false;
            }

            return true;
        } catch (Throwable t) {
            t.printStackTrace();
            return false;
        }
    }

    boolean scrollPost() throws InterruptedException {
        List<UiObject2> posts = device.wait(Until.findObjects(By.res(Pattern.compile(POST))), 6000);
        if (posts == null || posts.size() == 0) {
            Log.i(LOG_TAG, "Posts not found");
            return false;
        }

        boolean found;
        if (!muteButtonFound) {
            found = device.wait(Until.hasObject(By.res(MUTE_BUTTON)), 2000);
            if (found) {
                Thread.sleep(750);
                UiObject2 muteButton = device.findObject(By.res(MUTE_BUTTON));
                muteButton.click();
                Thread.sleep(750);
                muteButtonFound = true;
            }
        }

        UiObject2 post;
        posts = device.wait(Until.findObjects(By.res(POST_CAROUSEL)), 500);
        if (posts != null) {
            if (posts.size() == 1) {
                post = posts.get(0);
            } else {
                post = posts.get(1);
            }

            try {
                Rect postBounds = post.getVisibleBounds();
                device.swipe(80 * deviceWidth / 100, postBounds.centerY(),
                        20 * deviceWidth / 100, postBounds.centerY(), 20);
                device.waitForIdle();
                Thread.sleep(500);
            } catch (StaleObjectException ignored) {}
        }

        device.swipe(deviceWidth / 2, 70 * deviceHeight / 100,
                deviceWidth / 2, 30 * deviceHeight / 100, 20);
        device.waitForIdle();
        Thread.sleep(750);

        found = device.hasObject(By.res(Pattern.compile(POST)));
        if (!found) {
            Log.i(LOG_TAG, "Could not find post after scrolling");
            return false;
        }

        return true;
    }
}