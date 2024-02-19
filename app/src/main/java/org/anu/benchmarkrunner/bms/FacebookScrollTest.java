package org.anu.benchmarkrunner.bms;

import static org.anu.benchmarkrunner.BenchmarkRunner.LOG_TAG;

import android.util.Log;

import androidx.test.uiautomator.By;
import androidx.test.uiautomator.UiObject2;
import androidx.test.uiautomator.Until;

import org.anu.benchmarkrunner.Benchmark;

import java.io.PrintStream;
import java.util.List;

public class FacebookScrollTest extends Benchmark {
    static String PACKAGE_NAME = "com.facebook.katana";
    static String ACTIVITY_NAME = "com.facebook.katana.LoginActivity";

    public FacebookScrollTest(PrintStream writer) {
        super(PACKAGE_NAME, writer);
        activityName = ACTIVITY_NAME;
    }

    @Override
    public boolean iterate() {
        try {
            boolean found = device.wait(
                    Until.hasObject(By.clazz("android.view.ViewGroup")
                            .desc("Stories")), 8000);
            if (!found) {
                Log.i(LOG_TAG, "Main page did not load in time");
                return false;
            }

//            UiObject2 topBar = device.wait(Until.findObject(By.clazz("android.widget.LinearLayout")
//                    .hasChild(By.clazz("android.widget.FrameLayout")
//                            .hasChild(By.clazz("android.widget.LinearLayout")
//                                    .hasChild(By.desc("Messaging"))))), 6000);
//            if (topBar == null) {
//                Log.i(LOG_TAG, "Could not find top bar in time");
//                return false;
//            }
//
//            UiObject2 videosButton = topBar.findObjects(By.clazz("android.widget.FrameLayout")).get(1)
//                    .findObject(By.clazz("android.widget.LinearLayout"))
//                    .findObjects(By.clazz("android.widget.View")).get(2);
//            videosButton.click();
//            device.click(450, 330);
//            device.waitForIdle();
//
//            found = device.wait(Until.hasObject(By.desc("Video, Heading")), 6000);
//            if (!found) {
//                Log.i(LOG_TAG, "Video tab did not open in time");
//                return false;
//            }

            device.swipe(deviceWidth / 2, 20 * deviceHeight / 100,
                    deviceWidth / 2, 50 * deviceHeight / 100, 20);
            Thread.sleep(2500);

            for (int i = 0; i < 10; i++) {
                found = scrollPost();
                if (!found) {
                    return false;
                }
            }

            device.pressBack();
            device.waitForIdle();

            found = device.wait(
                    Until.hasObject(By.clazz("android.view.ViewGroup")
                            .desc("Stories")), 8000);
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

    boolean scrollPost() throws InterruptedException {
        List<UiObject2> posts = device.wait(Until.findObjects(By.desc("Post menu")), 6000);
        if (posts == null || posts.size() == 0) {
            Log.i(LOG_TAG, "Posts not found");
            return false;
        }

        device.swipe(deviceWidth / 2, 70 * deviceHeight / 100,
                deviceWidth / 2, 30 * deviceHeight / 100, 15);
        device.waitForIdle();
        Thread.sleep(750);

        boolean found = device.hasObject(By.desc("Post menu"));
        if (!found) {
            Log.i(LOG_TAG, "Could not find post after scrolling");
            return false;
        }

        return true;
    }
}
