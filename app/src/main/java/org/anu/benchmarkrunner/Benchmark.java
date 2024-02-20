package org.anu.benchmarkrunner;

import static org.anu.benchmarkrunner.BenchmarkRunner.LOG_TAG;

import android.app.Instrumentation;
import android.util.Log;

import androidx.test.platform.app.InstrumentationRegistry;
import androidx.test.uiautomator.By;
import androidx.test.uiautomator.Configurator;
import androidx.test.uiautomator.UiDevice;
import androidx.test.uiautomator.Until;

import java.io.PrintStream;

public abstract class Benchmark {
    public String benchmark;
    public String activityName;
    public PrintStream writer;
    public UiDevice device;
    public Instrumentation instrumentation;
    public int deviceHeight;
    public int deviceWidth;
    public int pid = -1;

    public static String RECENT_APPS_SNAPSHOTS = "com.android.launcher3:id/snapshot";

    public Benchmark(String benchmark, PrintStream writer) {
        this.benchmark = benchmark;
        this.writer = writer;
        instrumentation = InstrumentationRegistry.getInstrumentation();
        device = UiDevice.getInstance(instrumentation);
        this.deviceHeight = device.getDisplayHeight();
        this.deviceWidth = device.getDisplayWidth();
    }

    public final void run() throws Exception {
        String pidString = device.executeShellCommand("pidof " + benchmark);
        int prevPid = pidString.equals("") ? -1 : Integer.parseInt(pidString.trim());

        if (prevPid > 0) {
            device.executeShellCommand("kill -s KILL " + prevPid);
            Thread.sleep(250);
            Log.i(LOG_TAG, "Killed previous pid " + prevPid + " for benchmark " + benchmark);
        }

        setupIteration();
        harnessBegin();

        final long start = System.currentTimeMillis();
        boolean passed = iterate();
        final long duration = System.currentTimeMillis() - start;

        harnessEnd(duration, passed);
        teardownIteration();
    }

    public void setupIteration() {
        try {
            // Set the wait for idle and selector timeouts to be 1s because it takes too
            // long otherwise
            Configurator configurator = Configurator.getInstance();
            configurator.setWaitForIdleTimeout(1000);
            configurator.setWaitForSelectorTimeout(1000);

            // Start benchmark application
            device.executeShellCommand("am start -n " + benchmark + "/" + activityName);
            Thread.sleep(250);
        } catch (Throwable t) {
            t.printStackTrace();
        }
    }

    public abstract boolean iterate();

    public void stopBenchmark() {
        try {
            device.executeShellCommand("kill -s KILL " + pid);
            Thread.sleep(100);
        } catch (Throwable t) {
            t.printStackTrace();
        }
    }

    public void teardownIteration() {
        int startX = deviceWidth / 2;
        int startY = 70 * deviceHeight / 100;

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

            device.pressHome();
            Thread.sleep(100);

            stopBenchmark();
        } catch (Throwable t) {
            t.printStackTrace();
        }
    }

    public void harnessBegin() throws Exception {
        if (pid == -1) {
            String pidString = device.executeShellCommand("pidof " + benchmark);
            pid = Integer.parseInt(pidString.trim());
        }

        assert pid > 1;

        device.executeShellCommand("kill -s USR2 " + pid);
        Thread.sleep(250);
        writer.println("===== BenchmarkRunner " + benchmark + " starting =====");
    }

    public void harnessEnd(long duration, boolean passed) throws Exception {
        device.executeShellCommand("kill -s USR2 " + pid);
        Thread.sleep(500);
        writer.println("===== BenchmarkRunner " + benchmark +
                (passed ? " PASSED " : " FAILED ") + "in " + duration + " msec =====");
        Thread.sleep(500);
    }

    public final void simulateTyping(String text) {
        instrumentation.sendStringSync(text);
    }
}
