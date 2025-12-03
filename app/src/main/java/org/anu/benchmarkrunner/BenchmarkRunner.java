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

import android.app.Activity;
import android.app.Instrumentation;
import android.os.Bundle;
import android.util.Log;

import androidx.test.runner.MonitoringInstrumentation;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.lang.reflect.Constructor;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.TreeSet;

public class BenchmarkRunner extends MonitoringInstrumentation {
    public static final String LOG_TAG = "BenchmarkRunner";
    static String selectedBenchmark;
    static String[] selectedAdversaries;
    static String tasksetMask;
    static int tasksetWaitTime;

    @Override
    public void onCreate(Bundle arguments) {
        super.onCreate(arguments);
        Log.i(LOG_TAG, "OnCreate " + arguments.toString());
        selectedBenchmark = "org.anu.benchmarkrunner.bms." + arguments.getString("bm");
        tasksetMask = arguments.getString("taskset");
        String tasksetWaitString = arguments.getString("tasksetWait");
        if (tasksetWaitString != null) {
            tasksetWaitTime = Integer.parseInt(tasksetWaitString);
        } else {
            tasksetWaitTime = -1;
        }

        if (arguments.getString("adv") != null) {
            String[] tmp = arguments.getString("adv").split(",");
            // Use a LinkedHashSet to guarantee insertion order
            LinkedHashSet<String> tmpSet = new LinkedHashSet<>(Arrays.asList(tmp));

            selectedAdversaries = new String[tmpSet.size()];
            selectedAdversaries = tmpSet.toArray(selectedAdversaries);
            for (int i = 0; i < selectedAdversaries.length; i++) {
                selectedAdversaries[i] = "org.anu.benchmarkrunner.adversaries." + selectedAdversaries[i];
            }
        } else {
            selectedAdversaries = null;
        }
        start();
    }

    @Override
    public void onStart() {
        super.onStart();

        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        PrintStream writer = new PrintStream(byteArrayOutputStream);

        try {
            Log.i(LOG_TAG, "Starting benchmark " + selectedBenchmark);
            Class<?> clazz = Class.forName(selectedBenchmark);
            Constructor<?> cons = clazz.getConstructor(PrintStream.class);
            Benchmark benchmark = (Benchmark) cons.newInstance(writer);

            if (selectedAdversaries != null) {
                Adversary[] adversaries = new Adversary[selectedAdversaries.length];
                for (int i = 0; i < adversaries.length; i++) {
                    clazz = Class.forName(selectedAdversaries[i]);
                    cons = clazz.getConstructor(PrintStream.class);
                    adversaries[i] = (Adversary) cons.newInstance(writer);
                }
                benchmark.runWithAdversaries(adversaries, tasksetMask, tasksetWaitTime);
            } else {
                benchmark.run(tasksetMask, tasksetWaitTime);
            }
        } catch (Throwable t) {
            writer.println(String.format(
                    "Benchmark run aborted due to unexpected exception: %s",
                    t.getMessage()));
            t.printStackTrace(writer);
        } finally {
            Bundle results = new Bundle();
            writer.close();
            results.putString(Instrumentation.REPORT_KEY_STREAMRESULT,
                    String.format("%s",
                            byteArrayOutputStream));
            finish(Activity.RESULT_OK, results);
        }
    }
}
