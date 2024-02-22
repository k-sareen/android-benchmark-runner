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
import android.util.Log;

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
    public int pid = -1;
    private JankCollector jankCollector;

    public static String RECENT_APPS_SNAPSHOTS = "com.android.launcher3:id/snapshot";
    private static final Pattern STATS_HEADER =
            Pattern.compile("(\\S+\\s+){6}(============================ Tabulate Statistics ============================)\n");
    private static final Pattern STATS_FOOTER =
            Pattern.compile("(\\S+\\s+){6}(-------------------------- End Tabulate Statistics --------------------------)\n");
    private static final Pattern STATS_ROW = Pattern.compile("(\\S+\\s+){6}(.+)");

    public Benchmark(String benchmark, String activityName, PrintStream writer) {
        this.benchmark = benchmark;
        this.activityName = activityName;
        this.writer = writer;
        instrumentation = InstrumentationRegistry.getInstrumentation();
        device = UiDevice.getInstance(instrumentation);
        this.deviceHeight = device.getDisplayHeight();
        this.deviceWidth = device.getDisplayWidth();
        this.jankCollector = new JankCollector(device, benchmark);
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

    public final void stopBenchmark() {
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

    public final void harnessBegin() throws Exception {
        if (pid == -1) {
            String pidString = device.executeShellCommand("pidof " + benchmark);
            pid = Integer.parseInt(pidString.trim());
        }

        assert pid > 1;

        device.executeShellCommand("kill -s USR2 " + pid);
        jankCollector.harnessBegin();
        Thread.sleep(250);
        writer.println("===== BenchmarkRunner " + benchmark + " starting =====");
    }

    public final void harnessEnd(long duration, boolean passed) throws Exception {
        device.executeShellCommand("kill -s USR2 " + pid);
        Thread.sleep(300);

        if (passed) {
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
                (passed ? " PASSED " : " FAILED ") + "in " + duration + " msec =====");
    }

    public final void simulateTyping(String text) {
        instrumentation.sendStringSync(text);
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
