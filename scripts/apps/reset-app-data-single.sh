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

reset_benchmarks="com.adobe.reader=AcrobatSetup
com.google.android.apps.magazines=GoogleNewsSetup
com.google.android.apps.maps=MapsRoutePreviewTest
com.zhiliaoapp.musically=TikTokScrollTest
com.twitter.android=TwitterSetup
"

apk=${2}

if [[ "${apk}" == "" ]]; then
    echo -e "${RED}===== Need to provide an apk to reset! =====${NORM}"
    exit 1
fi

reset_filtered_benchmarks=$(filter_benchmarks "${reset_benchmarks}")

do_command "adb shell rm /data/local/heap_sizes.json"
do_command "adb shell rm /data/local/mmtk_stress_factor"
do_command "adb shell rm /data/local/mmtk_thread_affinity"

echo "===== Cleaning previous app data ====="
if [[ "${apk}" != "com.zhiliaoapp.musically" ]]; then
    do_command "adb shell pm clear ${apk}"
else
    do_command "adb shell pm clear --cache-only com.zhiliaoapp.musically"
fi

echo "===== Setting up app ====="

if [[ "${apk}" == "com.google.android.apps.maps" ]]; then
    do_command "adb logcat -c"

    echo -e ${GREEN}adb shell am start -S -n com.google.android.apps.maps/com.google.android.maps.MapsActivity${NORM}
    adb shell am start -S -n com.google.android.apps.maps/com.google.android.maps.MapsActivity

    sleep 15

    do_command "adb shell input keyevent KEYCODE_HOME"
    sleep 5
fi

bm=""
for bmpair in ${reset_filtered_benchmarks}; do
    # bmpair is a string of form "<apk>=<bm name>"
    # We split the string on "="
    tmp_apk=${bmpair%%=*}
    tmp_bm=${bmpair#*=}

    if [[ "${apk}" == "${tmp_apk}" ]]; then
        bm=${tmp_bm}
    fi
done

if [[ "${bm}" == "" ]]; then
    echo -e "${RED}===== Could not find benchmark to reset! =====${NORM}"
    exit 1
fi

do_command "adb logcat -c"

echo -e ${GREEN}adb shell cd '/data/local;'  /system/bin/am instrument -e bm ${bm} -e taskset c0 -e tasksetWait 1500 -w org.anu.benchmarkrunner/.BenchmarkRunner${NORM}
adb shell cd '/data/local;'  /system/bin/am instrument -e bm ${bm} -e taskset c0 -e tasksetWait 1500 -w org.anu.benchmarkrunner/.BenchmarkRunner

mkdir -p $HOME/git/evaluation/tmp
adb logcat -d &> $HOME/git/evaluation/tmp/${bm}.out
