package org.anu.benchmarkrunner.bms;

import static org.anu.benchmarkrunner.BenchmarkRunner.LOG_TAG;

import android.util.Log;

import androidx.test.uiautomator.By;
import androidx.test.uiautomator.UiObject2;
import androidx.test.uiautomator.Until;

import org.anu.benchmarkrunner.Benchmark;

import java.io.PrintStream;
import java.util.List;
import java.util.regex.Pattern;

public class TwitterScrollTest extends Benchmark {
    static String PACKAGE_NAME = "com.twitter.android";
    static String ACTIVITY_NAME = "com.twitter.android.StartActivity";
    static String HOME_BUTTON = "Home(. New items)?";
    static String MUTE_BUTTON = "com.twitter.android:id/audio_toggle_view";
    static String POST = "com.twitter.android:id/row";

    boolean muteButtonFound = false;

    public TwitterScrollTest(PrintStream writer) {
        super(PACKAGE_NAME, ACTIVITY_NAME, writer);
    }

    @Override
    public boolean iterate() {
        try {
            boolean found = device.wait(Until.hasObject(By.res(POST)), 6000);
            if (!found) {
                Log.i(LOG_TAG, "Main page did not load in time");
                return false;
            }

            UiObject2 followingFeed = device.wait(Until.findObject(By.text("Following")), 5000);
            if (followingFeed == null) {
                Log.i(LOG_TAG, "Following feed not found");
                return false;
            }
            followingFeed.click();

            UiObject2 homeButton = device.wait(Until.findObject(
                    By.clazz("android.widget.LinearLayout")
                            .desc(Pattern.compile(HOME_BUTTON))), 5000);
            if (homeButton == null) {
                Log.i(LOG_TAG, "Home button not found");
                return false;
            }
            Thread.sleep(500);

            homeButton.click();
            device.waitForIdle();
            Thread.sleep(1500);

            found = device.wait(Until.hasObject(By.res(POST)), 6000);
            if (!found) {
                Log.i(LOG_TAG, "Refresh did not finish in time");
                return false;
            }

            device.swipe(deviceWidth / 2, 70 * deviceHeight / 100,
                    deviceWidth / 2, 30 * deviceHeight / 100, 25);
            device.waitForIdle();
            Thread.sleep(500);

            found = device.wait(Until.hasObject(By.res(MUTE_BUTTON)), 2000);
            if (found) {
                Thread.sleep(250);
                UiObject2 muteButton = device.findObject(By.res(MUTE_BUTTON));
                muteButton.click();
                Thread.sleep(500);
                muteButtonFound = true;
            }

            for (int i = 0; i < 15; i++) {
                found = scrollPost();
                if (!found) {
                    return false;
                }
            }

            return true;
        } catch (Throwable t) {
            t.printStackTrace();
            return false;
        }
    }

    boolean scrollPost() throws InterruptedException {
        List<UiObject2> posts = device.wait(Until.findObjects(By.res(POST)), 6000);
        if (posts == null || posts.size() == 0) {
            Log.i(LOG_TAG, "Posts not found");
            return false;
        }

        boolean found;
        if (!muteButtonFound) {
            found = device.wait(Until.hasObject(By.res(MUTE_BUTTON)), 2000);
            if (found) {
                Thread.sleep(250);
                UiObject2 muteButton = device.findObject(By.res(MUTE_BUTTON));
                muteButton.click();
                Thread.sleep(500);
                muteButtonFound = true;
            }
        }

        device.swipe(deviceWidth / 2, 70 * deviceHeight / 100,
                deviceWidth / 2, 30 * deviceHeight / 100, 20);
        device.waitForIdle();
        Thread.sleep(500);

        found = device.hasObject(By.res(POST));
        if (!found) {
            Log.i(LOG_TAG, "Could not find post after scrolling");
            return false;
        }

        return true;
    }
}
