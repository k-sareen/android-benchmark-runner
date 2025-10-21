/*
 * Copyright 2024 Kunal Sareen
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.anu.benchmarkrunner;

import static org.anu.benchmarkrunner.BenchmarkRunner.LOG_TAG;

import android.app.Instrumentation;
import android.os.SystemClock;
import android.os.Trace;
import android.util.Log;
import android.view.KeyCharacterMap;
import android.view.KeyEvent;

import androidx.test.platform.app.InstrumentationRegistry;
import androidx.test.uiautomator.By;
import androidx.test.uiautomator.Configurator;
import androidx.test.uiautomator.UiDevice;
import androidx.test.uiautomator.Until;

import java.io.PrintStream;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public abstract class Benchmark {
    public String benchmark;
    public String activityName;
    public PrintStream writer;
    public UiDevice device;
    public Instrumentation instrumentation;
    public int deviceHeight;
    public int deviceWidth;
    // XXX(kunals): Don't use -1 for the PID! It will kill all processes!
    public int pid = Integer.MIN_VALUE;
    private String tasksetMask;
    private int tasksetWaitTime;
    private final JankCollector jankCollector;
    private boolean hasError = false;

    public static String RECENT_APPS_SNAPSHOTS = "com.android.launcher3:id/snapshot";
    private static final String ART_STATS_HEADER =
            "============================ Tabulate Statistics ============================";
    private static final String ART_STATS_FOOTER =
            "-------------------------- End Tabulate Statistics --------------------------";
    private static final String MMTK_STATS_HEADER =
            "============================ MMTk Statistics Totals ============================";
    private static final String MMTK_STATS_FOOTER =
            "------------------------------ End MMTk Statistics -----------------------------";
    private static final Pattern STATS_HEADER =
            Pattern.compile("(\\S+\\s+){6}(" + ART_STATS_HEADER + "|" + MMTK_STATS_HEADER +")\n");
    private static final Pattern STATS_FOOTER =
            Pattern.compile("(\\S+\\s+){6}(" + ART_STATS_FOOTER + "|" + MMTK_STATS_FOOTER +")\n");

    private static final Pattern STATS_ROW = Pattern.compile("(\\S+\\s+){6}(.+)");

    private static final Pattern GETAFFINITY_MASK =
            Pattern.compile("pid \\d+'s current affinity mask: (.+)\n");

    public Benchmark(String benchmark, String activityName, PrintStream writer) {
        this.benchmark = benchmark;
        this.activityName = activityName;
        this.writer = writer;
        instrumentation = InstrumentationRegistry.getInstrumentation();
        device = UiDevice.getInstance(instrumentation);
        this.deviceHeight = device.getDisplayHeight();
        this.deviceWidth = device.getDisplayWidth();
        this.jankCollector = new JankCollector(device, benchmark);
        this.tasksetMask = null;
    }

    public final void run(String tasksetMask, int tasksetWaitTime) throws Exception {
        this.tasksetMask = tasksetMask;
        this.tasksetWaitTime = tasksetWaitTime;

        setupConfiguration();
        setupIteration();
        setBenchmarkPid();
        if (hasError) {
            return;
        }

        harnessBegin();

        Trace.beginSection("App Benchmark");
        final long start = System.currentTimeMillis();
        boolean passed = iterate();
        final long duration = System.currentTimeMillis() - start;
        Trace.endSection();

        harnessEnd(duration, passed);
        teardownIteration();
    }

    public final void runWithAdversaries(Adversary[] adversaries, String tasksetMask, int tasksetWaitTime) throws Exception {
        this.tasksetMask = tasksetMask;
        this.tasksetWaitTime = tasksetWaitTime;

        for (Adversary adv: adversaries) {
            // Run the adversary
            adv.setupConfiguration();
            adv.setupIteration();
            adv.setBenchmarkPid();

            if (hasError) {
                return;
            }

            adv.iterate();
            // Put the adversary in the background
            device.pressHome();
            device.waitForIdle();
            Thread.sleep(250);
        }

        setupConfiguration();
        setupIteration();
        setBenchmarkPid();
        if (hasError) {
            return;
        }

        harnessBegin();

        Trace.beginSection("App Benchmark");
        final long start = System.currentTimeMillis();
        boolean passed = iterate();
        final long duration = System.currentTimeMillis() - start;
        Trace.endSection();

        harnessEnd(duration, passed);
        teardownIteration();

        for (Adversary adv: adversaries) {
            adv.teardownIteration();
        }
    }

    public final void setupConfiguration() {
        // Set the wait for idle and selector timeouts to be 1s because it takes too
        // long otherwise
        Configurator configurator = Configurator.getInstance();
        configurator.setWaitForIdleTimeout(1000);
        configurator.setWaitForSelectorTimeout(1000);
    }

    public void setupIteration() {
        // Start benchmark application. We don't taskset the `am` command here since it will
        // end up interfering too much with the application otherwise. Ideally we can schedule
        // it onto the unused cores, but for the time being, we are letting the OS decide
        // where to schedule it.
        // `-S` is used to stop any previous instances of the benchmark
        device.performActionAndWait(() -> {
            try {
                device.executeShellCommand("am start -S -n " + benchmark + "/" + activityName);
            } catch (Throwable t) {
                t.printStackTrace(writer);
                hasError = true;
            }
        }, Until.newWindow(), 2000);
    }

    public final void setBenchmarkPid() {
        try {
            if (pid < 0) {
                String pidString = device.executeShellCommand("pidof " + benchmark);
                pid = Integer.parseInt(pidString.trim());
            }

            assert pid > 1;
        } catch (Throwable t) {
            t.printStackTrace(writer);
            hasError = true;
        }
    }

    public abstract boolean iterate();

    public final void stopBenchmark() {
        try {
            assert pid > 1;
            device.executeShellCommand("kill -s KILL " + pid);
            Thread.sleep(100);
        } catch (Throwable t) {
            t.printStackTrace(writer);
            hasError = true;
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
            t.printStackTrace(writer);
        }
    }

    public final void harnessBegin() throws Exception {
        // taskset the new process if a tasksetMask has been specified
        if (tasksetMask != null) {
            // Hack to get benchmarks to taskset properly. If we don't wait here, benchmark threads
            // somehow ignore the taskset mask and get scheduled to cores not in the mask
            int waitTime = getWaitTime();
            Thread.sleep(waitTime);
            Log.i(LOG_TAG, "taskset mask " + tasksetMask + " specified. " +
                    "Running " + benchmark + " (PID " + pid + ") under taskset.");
            device.executeShellCommand("taskset -ap " + tasksetMask + " " + pid);
            Thread.sleep(500);
        }

        device.executeShellCommand("kill -s USR2 " + pid);
        jankCollector.harnessBegin();
        Thread.sleep(300);
        writer.println("===== BenchmarkRunner " + benchmark + " starting =====");
    }

    private int getWaitTime() {
        int waitTime = Math.max(tasksetWaitTime, 1000);
        // For some reason Gmail needs a longer wait time?
        if (benchmark.equals("com.google.android.gm")) {
            waitTime += 1000;
        }
        // Maximum wait time is 8 seconds (arbitrarily chosen)
        waitTime = Math.min(waitTime, 8000);
        return waitTime;
    }

    public final void checkAffinity(String str) throws Exception {
        if (tasksetMask != null) {
            String getaffinityOut = device.executeShellCommand("taskset -p " + pid);
            Matcher affinityMatcher = GETAFFINITY_MASK.matcher(getaffinityOut);

            if (affinityMatcher.find()) {
                String actualMask = affinityMatcher.group(1);
                if (!tasksetMask.equals(actualMask)) {
                    Log.e(LOG_TAG, "FAILED: " + str + " taskset mask " + tasksetMask + " was specified, but benchmark "
                            + benchmark + " (PID " + pid + ") ran with " + actualMask + " mask! Not printing results!");
                    writer.println("FAILED: " + str + " taskset mask " + tasksetMask + " was specified, but benchmark "
                            + benchmark + " (PID " + pid + ") ran with " + actualMask + " mask! Not printing results!");
                    throw new RuntimeException();
                }
            }
        }
    }

    public final void harnessEnd(long duration, boolean passed) throws Exception {
        assert pid > 1;
        device.executeShellCommand("kill -s USR2 " + pid);
        Thread.sleep(300);

        // If a taskset mask was specified, then check that the benchmark respected that mask.
        //
        // This catches obvious mistakes and/or cases where the app will clear or change the taskset
        // mask, but will not catch a more naughty app that may briefly change the affinity in the
        // middle of the execution and then reset back to the correct/specified taskset mask near
        // the end of the benchmark.
        //
        // Hence, it is recommended that benchmarks be traced with perfetto or an equivalent tool to
        // ensure that the benchmark respects the taskset masks correctly.
        if (passed && tasksetMask != null) {
            String getaffinityOut = device.executeShellCommand("taskset -p " + pid);
            Matcher affinityMatcher = GETAFFINITY_MASK.matcher(getaffinityOut);

            if (affinityMatcher.find()) {
                String actualMask = affinityMatcher.group(1);
                if (!tasksetMask.equals(actualMask)) {
                    passed = false;
                    Log.e(LOG_TAG, "taskset mask " + tasksetMask + " was specified, but benchmark "
                            + benchmark + " (PID " + pid + ") ran with " + actualMask + " mask! Not printing results!");
                    writer.println("taskset mask " + tasksetMask + " was specified, but benchmark "
                            + benchmark + " (PID " + pid + ") ran with " + actualMask + " mask! Not printing results!");
                }
            }
        }

        if (!hasError && passed) {
            String logcatOut = device.executeShellCommand("logcat -sd " + getLogTag());
            Map<String, Double> jankMetrics = jankCollector.harnessEnd();

            Matcher headerMatcher = STATS_HEADER.matcher(logcatOut);
            Matcher footerMatcher = STATS_FOOTER.matcher(logcatOut);

            if (headerMatcher.find() && footerMatcher.find()) {
                writer.println(headerMatcher.group(2));

                String table = logcatOut.substring(headerMatcher.end(), footerMatcher.start());
                String[] tableRows = table.split("\n");

                Matcher statsHeaderMatcher = STATS_ROW.matcher(tableRows[0]);
                Matcher statsValuesMatcher = STATS_ROW.matcher(tableRows[1]);

                StringBuilder statsHeader = new StringBuilder(statsHeaderMatcher.find() ? statsHeaderMatcher.group(2) : tableRows[0]);
                StringBuilder statsValues = new StringBuilder(statsValuesMatcher.find() ? statsValuesMatcher.group(2) : tableRows[1]);

                for (Map.Entry<String, Double> entry : jankMetrics.entrySet()) {
                    statsHeader.append("\t").append(entry.getKey());
                    statsValues.append("\t").append(entry.getValue());
                }

                writer.println(statsHeader);
                writer.println(statsValues);

                writer.println(footerMatcher.group(2));
            } else {
                writer.println("Error occurred when getting stats table");
            }
        }

        writer.print("===== BenchmarkRunner " + benchmark +
                ((!hasError && passed) ? " PASSED " : " FAILED ") + "in " + duration + " msec =====");
    }

    public final void simulateTyping(String text) throws InterruptedException {
        // This function is copied exactly from Instrumentation.sendStringSync except we wait before
        // we send subsequent inputs to allow for more realistic typing
        if (text == null) {
            return;
        }
        KeyCharacterMap keyCharacterMap = KeyCharacterMap.load(KeyCharacterMap.VIRTUAL_KEYBOARD);

        KeyEvent[] events = keyCharacterMap.getEvents(text.toCharArray());

        if (events != null) {
            for (KeyEvent event : events) {
                // We have to change the time of an event before injecting it because
                // all KeyEvents returned by KeyCharacterMap.getEvents() have the same
                // time stamp and the system rejects too old events. Hence, it is
                // possible for an event to become stale before it is injected if it
                // takes too long to inject the preceding ones.
                instrumentation.sendKeySync(KeyEvent.changeTimeRepeat(event, SystemClock.uptimeMillis(), 0));
                Thread.sleep(10);
            }
        }
    }

    public final String getLogTag() {
        String logTag;
        if (benchmark.length() > 15) {
            logTag = benchmark.substring(benchmark.length() - 15);
        } else {
            logTag = benchmark;
        }

        return logTag;
    }
}
