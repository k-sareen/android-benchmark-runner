package org.anu.benchmarkrunner;

import android.app.Activity;
import android.app.Instrumentation;
import android.os.Bundle;
import android.util.Log;

import androidx.test.runner.MonitoringInstrumentation;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.lang.reflect.Constructor;

public class BenchmarkRunner extends MonitoringInstrumentation {
    public static String LOG_TAG = "BenchmarkRunner";
    static String selectedBenchmark;

    @Override
    public void onCreate(Bundle arguments) {
        super.onCreate(arguments);
        Log.i(LOG_TAG, "OnCreate " + arguments.toString());
        selectedBenchmark = "org.anu.benchmarkrunner.bms." + arguments.getString("bm");
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
            benchmark.run();
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
