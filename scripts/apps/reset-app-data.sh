#!/bin/bash

SOURCE="${BASH_SOURCE[0]}"
while [ -h "$SOURCE" ]; do
  DIR="$( cd -P "$( dirname "$SOURCE" )" >/dev/null 2>&1 && pwd )"
  SOURCE="$(readlink "$SOURCE")"
  [[ $SOURCE != /* ]] && SOURCE="$DIR/$SOURCE"
done
DIR="$( cd -P "$( dirname "$SOURCE" )" >/dev/null 2>&1 && pwd )"
SCRIPTS_DIR=$DIR/../

source $SCRIPTS_DIR/common.sh

reset_benchmarks="com.adobe.reader
com.google.android.apps.magazines
com.google.android.apps.maps
com.twitter.android
"

reset_benchmark_names="AcrobatSetup
GoogleNewsSetup
TwitterSetup
"

reset_filtered_benchmarks=$(filter_benchmarks "${reset_benchmarks}")
reset_filtered_benchmark_names=$(filter_benchmarks "${reset_benchmark_names}")

do_command "adb shell rm /data/local/heap_sizes.json"
do_command "adb shell rm /data/local/mmtk_stress_factor"
do_command "adb shell rm /data/local/mmtk_thread_affinity"

echo "===== Cleaning previous app data ====="
for apk in ${reset_filtered_benchmarks}; do
    do_command "adb shell pm clear ${apk}"
done

do_command "adb shell pm clear --cache-only com.zhiliaoapp.musically"

echo "===== Setting up all apps ====="
for bm in ${reset_filtered_benchmark_names}; do
    do_command "adb logcat -c"
    echo -e ${GREEN}adb shell cd '/data/local;'  /system/bin/am instrument -e bm ${bm} -e taskset c0 -e tasksetWait 1500 -w org.anu.benchmarkrunner/.BenchmarkRunner${NORM}
    adb shell cd '/data/local;'  /system/bin/am instrument -e bm ${bm} -e taskset c0 -e tasksetWait 1500 -w org.anu.benchmarkrunner/.BenchmarkRunner
    mkdir -p $HOME/git/evaluation/tmp
    adb logcat -d &> $HOME/git/evaluation/tmp/${bm}.out
done

do_command "adb logcat -c"
echo -e ${GREEN}adb shell am start -S -n com.google.android.apps.maps/com.google.android.maps.MapsActivity${NORM}
adb shell am start -S -n com.google.android.apps.maps/com.google.android.maps.MapsActivity
sleep 15

do_command "adb shell input keyevent KEYCODE_HOME"
sleep 5

do_command "adb logcat -c"
echo -e ${GREEN}adb shell cd '/data/local;'  /system/bin/am instrument -e bm MapsRoutePreviewTest -e taskset c0 -e tasksetWait 1500 -w org.anu.benchmarkrunner/.BenchmarkRunner${NORM}
adb shell cd '/data/local;'  /system/bin/am instrument -e bm MapsRoutePreviewTest -e taskset c0 -e tasksetWait 1500 -w org.anu.benchmarkrunner/.BenchmarkRunner
adb logcat -d &> $HOME/git/evaluation/tmp/MapsRoutePreviewTest.out

do_command "adb logcat -c"
echo -e ${GREEN}adb shell cd '/data/local;'  /system/bin/am instrument -e bm TikTokScrollTest -e taskset c0 -e tasksetWait 1500 -w org.anu.benchmarkrunner/.BenchmarkRunner${NORM}
adb shell cd '/data/local;'  /system/bin/am instrument -e bm TikTokScrollTest -e taskset c0 -e tasksetWait 1500 -w org.anu.benchmarkrunner/.BenchmarkRunner
adb logcat -d &> $HOME/git/evaluation/tmp/TikTokScrollTest.out

