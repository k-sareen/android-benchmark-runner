package org.anu.benchmarkrunner.bms;

import static org.anu.benchmarkrunner.BenchmarkRunner.LOG_TAG;

import android.graphics.Rect;
import android.util.Log;

import androidx.test.uiautomator.By;
import androidx.test.uiautomator.UiObject2;
import androidx.test.uiautomator.Until;

import org.anu.benchmarkrunner.Benchmark;

import java.io.PrintStream;
import java.util.List;
import java.util.regex.Pattern;

public class InstagramScrollTest extends Benchmark {
    static String PACKAGE_NAME = "com.instagram.android";
    static String ACTIVITY_NAME = "com.instagram.android.activity.MainTabActivity";
    static String MUTE_BUTTON = "com.instagram.android:id/indicator";
    static String POST = "com.instagram.android:id/(carousel_video_)?media_group";
    static String POST_HEADER = "com.instagram.android:id/top_legibility_gradient";
    static String PROFILE_HEADER = "com.instagram.android:id/media_header_location";
    static String PROFILE_NAME = "com.instagram.android:id/row_feed_photo_profile_name";
    static String REFRESHABLE_CONTAINER = "com.instagram.android:id/refreshable_container";
    static String SUGGESTED_FOR_YOU_HEADER = "com.instagram.android:id/row_feed_topic_label";

    String previousPostDesc = "";
    boolean muteButtonFound = false;

    public InstagramScrollTest(PrintStream writer) {
        super(PACKAGE_NAME, writer);
        activityName = ACTIVITY_NAME;
    }

    @Override
    public boolean iterate() {
        try {
            boolean found = device.wait(Until.hasObject(By.res(SUGGESTED_FOR_YOU_HEADER)), 5000);
            if (!found) {
                Log.i(LOG_TAG, "Main page did not load in time");
                return false;
            }

            device.swipe(deviceWidth / 2, 50 * deviceHeight / 100,
                    deviceWidth / 2, 70 * deviceHeight / 100, 70);
            Thread.sleep(2500);

            found = device.wait(Until.hasObject(By.res(SUGGESTED_FOR_YOU_HEADER)), 5000);
            if (!found) {
                Log.i(LOG_TAG, "Refresh did not finish in time");
                return false;
            }

            UiObject2 post = device.findObject(By.res(Pattern.compile(POST)));
            Rect postBounds = post.getVisibleBounds();
            device.swipe(deviceWidth / 2, postBounds.bottom - deviceHeight / 100,
                    deviceWidth / 2, 40 * deviceHeight / 100, 35);
            device.waitForIdle();
            Thread.sleep(750);

            found = device.wait(Until.hasObject(By.res(MUTE_BUTTON)), 2000);
            if (found) {
                muteButtonFound = true;
                UiObject2 muteButton = device.findObject(By.res(MUTE_BUTTON));
                muteButton.click();
                Thread.sleep(750);
            }

            for (int i = 0; i < 9; i++) {
                found = scrollPost();
                if (!found) {
                    return false;
                }
            }

            device.pressBack();
            device.waitForIdle();

            found = device.wait(Until.hasObject(By.text("Your story")), 2000);
            if (!found) {
                Log.i(LOG_TAG, "Did not make it back to top of feed");
                return false;
            }

            return true;
        } catch (Throwable t) {
            t.printStackTrace();
            return false;
        }
    }

    boolean scrollPost() throws InterruptedException {
        boolean found;
        UiObject2 post;
        List<UiObject2> posts = device.wait(Until.findObjects(By.res(Pattern.compile(POST))), 2000);
        if (posts == null || posts.size() == 0) {
            Log.i(LOG_TAG, "No posts visible");
            return false;
        } else if (posts.size() == 1) {
            post = posts.get(0);
        } else {
            post = posts.get(1);
        }

        String postDesc = post.getContentDescription();
        if (postDesc == null) {
            Log.i(LOG_TAG, "Could not get post content description");
            return false;
        }

        Rect postBounds = post.getVisibleBounds();
        int start = posts.size() == 1 ? postBounds.bottom - deviceHeight / 100 : postBounds.top;
        device.swipe(deviceWidth / 2, start,
                deviceWidth / 2, 8 * deviceHeight / 100, 40);
        device.waitForIdle();
        Thread.sleep(750);

        if (!muteButtonFound) {
            found = device.wait(Until.hasObject(By.res(MUTE_BUTTON)), 2000);
            if (found) {
                muteButtonFound = true;
                UiObject2 muteButton = device.findObject(By.res(MUTE_BUTTON));
                muteButton.click();
                Thread.sleep(750);
            }
        }

        if (previousPostDesc.equals(postDesc)) {
            Log.i(LOG_TAG, "Could not display new post");
            return false;
        } else {
            previousPostDesc = postDesc;
        }

        found = device.hasObject(By.res(Pattern.compile(POST)));
        if (!found) {
            Log.i(LOG_TAG, "Could not find post after scrolling");
            return false;
        }

        return true;
    }
}
