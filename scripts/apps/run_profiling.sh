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

benchmarks="com.adobe.reader
com.google.android.apps.magazines
com.google.android.apps.maps
com.zhiliaoapp.musically
com.twitter.android
"

filtered_benchmarks=$(filter_benchmarks "${benchmarks}")

builds="art-mmtk-singlethread-ss-energy"

echo "===== Starting benchmark run on ${DEVICE_NAME} ====="
for build in ${builds}; do
    echo "===== Running ${build} ====="
    turn_on_display
    unlock_device
    press_home_button

    # Set up device for benchmarking
    $SCRIPTS_DIR/setup-device.sh ${orig_device}
    sleep 5

    set_airplane_mode "disable"

    # Remove all AOT-compiled code for benchmark applications
    echo "===== Cleaning AOT-compiled code ====="
    for apk in ${filtered_benchmarks}; do
        clean_apk_code_and_profile_data ${apk}
    done

    # Run applications with default heap sizes to generate JIT profile data
    echo "===== Generating JIT profiling data ====="
    echo -e ${GREEN}running -w "adb shell" runbms ~/rough ~/git/evaluation/configs/profiling/profiling-pixel7pro.yml --reset-top-apps -i 60 -p `date +%F`-jit-profiling-${orig_device}-${build} -s 1.0${NORM}
    running -w "adb shell" runbms ~/rough ~/git/evaluation/configs/profiling/profiling-pixel7pro.yml --reset-top-apps -i 60 -p `date +%F`-jit-profiling-${orig_device}-${build} -s 1.0

    screen_state=$(get_display_state)
    if [[ "${screen_state}" == "ON_UNLOCKED" ]]; then
        echo -e "${GREEN}Screen in correct state. Continuing loop${NORM}"
        turn_off_display
    elif [[ "${screen_state}" == "ON_LOCKED" ]]; then
        echo -e "${RED}Screen in bad state (${screen_state}). Turning off display and breaking out of loop${NORM}"
        turn_off_display
        break
    elif [[ "${screen_state}" == "OFF_LOCKED" ]]; then
        echo -e "${RED}Screen in bad state (${screen_state}). Breaking out of loop${NORM}"
        break
    fi

    echo "===== Finished ${build} ====="
done
echo "===== Finished benchmark run on ${DEVICE_NAME} ====="
