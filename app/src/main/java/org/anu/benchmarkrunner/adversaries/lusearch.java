/*
 * Copyright 2025 Kunal Sareen
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

package org.anu.benchmarkrunner.adversaries;

import static org.anu.benchmarkrunner.BenchmarkRunner.LOG_TAG;

import android.util.Log;

import androidx.test.uiautomator.By;
import androidx.test.uiautomator.Until;

import org.anu.benchmarkrunner.Adversary;

import java.io.PrintStream;

public class lusearch extends Adversary {
    static String LUSEARCH_ADVERSARY_PATH = "/data/local/lusearch_adversary.sh";
    private int dalvikPid = Integer.MIN_VALUE;
    public lusearch(PrintStream writer) {
        super("dalvikvm64", "", writer);
    }

    @Override
    public void setupIteration() {
        try {
            String pidOut = device.executeShellCommand(LUSEARCH_ADVERSARY_PATH);
            device.waitForIdle();

            dalvikPid = Integer.parseInt(pidOut);
            assert dalvikPid > 0;
        } catch (Throwable t) {
            t.printStackTrace(writer);
            hasError = true;
        }
    }

    @Override
    public boolean iterate() {
        assert dalvikPid > 0;
        pid = dalvikPid;
        return true;
    }

    @Override
    public void teardownIteration() {
        assert dalvikPid > 0;
        assert pid == dalvikPid;
        try {
            device.executeShellCommand("kill -s INT " + pid);
            device.waitForIdle();
            Thread.sleep(500);
        } catch (Throwable t) {
            t.printStackTrace(writer);
            hasError = true;
        }
    }
}