package org.anu.benchmarkrunner.bms;

import static org.anu.benchmarkrunner.BenchmarkRunner.LOG_TAG;

import android.util.Log;

import androidx.test.uiautomator.By;
import androidx.test.uiautomator.UiObject2;
import androidx.test.uiautomator.Until;

import org.anu.benchmarkrunner.Benchmark;

import java.io.PrintStream;

public class SpotifyListenAndSearchTest extends Benchmark {
    static String PACKAGE_NAME = "com.spotify.music";
    static String ACTIVITY_NAME = "com.spotify.music.MainActivity";
    static String CURRENT_TRACK_INFO = "com.spotify.music:id/track_info_view_title";
    static String FIND_SEARCH_FIELD = "com.spotify.music:id/find_search_field_text";
    static String SEARCH_BUTTON = "com.spotify.music:id/search_tab";

    public SpotifyListenAndSearchTest(PrintStream writer) {
        super(PACKAGE_NAME, writer);
        activityName = ACTIVITY_NAME;
    }

    @Override
    public boolean iterate() {
        try {
            boolean found = device.wait(Until.hasObject(By.res(SEARCH_BUTTON)), 2500);
            if (!found) {
                Log.i(LOG_TAG, "Main page did not load in time");
                return false;
            }

            UiObject2 searchButton = device.findObject(By.res(SEARCH_BUTTON));
            searchButton.click();

            found = device.wait(Until.hasObject(By.res(FIND_SEARCH_FIELD)), 2500);
            if (!found) {
                Log.i(LOG_TAG, "Search page did not load in time 1");
                return false;
            }

            UiObject2 searchBar = device.findObject(By.res(FIND_SEARCH_FIELD));
            searchBar.click();
            Thread.sleep(500);
            simulateTyping("Paranoid Android");
            device.pressEnter();

            found = device.wait(Until.hasObject(By.text("Song • Radiohead")), 2500);
            if (!found) {
                Log.i(LOG_TAG, "Search did not complete in time");
                return false;
            }

            UiObject2 result = device.findObject(By.text("Song • Radiohead"));
            result.click();
            Thread.sleep(100);

            device.pressBack();
            Thread.sleep(100);

            found = device.wait(Until.hasObject(By.res(FIND_SEARCH_FIELD)), 2500);
            if (!found) {
                Log.i(LOG_TAG, "Search page did not load in time 2");
                return false;
            }
            searchBar = device.findObject(By.res(FIND_SEARCH_FIELD));
            searchBar.click();
            Thread.sleep(500);
            simulateTyping("Pink Floyd");
            device.pressEnter();

            found = device.wait(Until.hasObject(By.text("Featuring Pink Floyd")), 2500);
            if (!found) {
                Log.i(LOG_TAG, "Search did not complete in time");
                return false;
            }
            Thread.sleep(200);
            device.swipe(deviceWidth / 2, 70 * deviceHeight / 100,
                    deviceWidth / 2, 30 * deviceHeight / 100, 30);
            Thread.sleep(2000);
            device.swipe(deviceWidth / 2, 70 * deviceHeight / 100,
                    deviceWidth / 2, 30 * deviceHeight / 100, 30);
            Thread.sleep(2000);
            device.swipe(deviceWidth / 2, 70 * deviceHeight / 100,
                    deviceWidth / 2, 30 * deviceHeight / 100, 30);
            Thread.sleep(2000);
            device.swipe(deviceWidth / 2, 30 * deviceHeight / 100,
                    deviceWidth / 2, 80 * deviceHeight / 100, 10);
            Thread.sleep(2000);
            device.swipe(deviceWidth / 2, 30 * deviceHeight / 100,
                    deviceWidth / 2, 80 * deviceHeight / 100, 30);
            Thread.sleep(1000);

            found = device.wait(Until.hasObject(By.text("Artist")), 2500);
            if (!found) {
                Log.i(LOG_TAG, "Can't find artist");
                return false;
            }
            result = device.findObject(By.text("Artist"));
            result.click();
            Thread.sleep(100);

            found = device.wait(Until.hasObject(By.text("Another Brick in the Wall, Pt. 2")), 2500);
            if (!found) {
                Log.i(LOG_TAG, "Could not find song");
                return false;
            }
            Thread.sleep(100);

            result = device.findObject(By.text("Another Brick in the Wall, Pt. 2"));
            result.click();
            Thread.sleep(1000);

            found = device.wait(Until.hasObject(By.res(CURRENT_TRACK_INFO)), 2500);
            if (!found) {
                Log.i(LOG_TAG, "Currently playing track info not found");
                return false;
            }
            result = device.findObject(By.res(CURRENT_TRACK_INFO));
            if (!result.getText().equals("Another Brick in the Wall, Pt. 2")) {
                Log.i(LOG_TAG, "Incorrect currently playing track");
                return false;
            }

            device.pressBack();
            device.pressBack();
            Thread.sleep(1000);

            found = device.wait(Until.hasObject(By.res(SEARCH_BUTTON)), 2500);
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
