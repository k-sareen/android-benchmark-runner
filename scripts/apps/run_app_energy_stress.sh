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

benchmark_names="AcrobatSearchAndScrollTest
GoogleNewsScrollTest
MapsRoutePreviewTest
TikTokScrollTest
TwitterScrollTest
"

builds=""
if [[ "${device}" == "pixel7pro" ]] || [[ "${device}" == "pixel6pro" ]]; then
    builds="art-cc-gc-thread-energy art-cmc-gc-thread-energy"
    # builds="art-mmtk-singlethread-nogc-energy"
    # builds="art-mmtk-singlethread-ss-energy art-mmtk-multithread-ss-energy"
    # builds="art-mmtk-singlethread-immix-energy art-mmtk-multithread-immix-energy"
else
    echo -e "${RED}===== Can't run experiment on specified device ${device}! =====${NORM}"
    exit 1
fi

filtered_benchmarks=$(filter_benchmarks "${benchmarks}")
filtered_benchmark_names=$(filter_benchmarks "${benchmark_names}")

cp ~/git/evaluation/configs/energy-stress/lbo-apps-base.yml /tmp/$USER

echo "===== Starting benchmark run on ${DEVICE_NAME} ====="
for build in ${builds}; do
    echo "===== Running ${build} ====="
    tmp_file=$(mktemp -p /tmp/$USER/)
    do_command cp ~/git/evaluation/configs/energy-stress/lbo-${build}.yml ${tmp_file}
    echo -e "\nbenchmarks:\n  android-apps:" >> ${tmp_file}
    for bm in ${filtered_benchmark_names}; do
        echo "    - ${bm}" >> ${tmp_file}
    done

    install_build ${build}

    wait_for_device 90

    reboot_bootloader
    fastboot_wait_for_device 15

    fastboot_visible ${SERIAL_NO}
    ret=${?}
    if [[ ${ret} != 0 ]]; then
        exit 1
    fi

    fastboot_temporary_boot_cmdline ~/git/evaluation/device/${DEVICE_HOSTNAME}/boot.img "isolcpus=4,5,6,7"
    wait_for_device 60

    turn_on_display
    unlock_device
    press_home_button

    # Set up device for benchmarking
    $SCRIPTS_DIR/setup-device.sh ${orig_device}
    sleep 5

    set_airplane_mode "disable"

    check_kernel_cmdline_contains_value "isolcpus=4,5,6,7" "isolcpus"
    ret=${?}
    if [[ ${ret} != 0 ]]; then
        exit 1
    fi

    # Remove all AOT-compiled code for benchmark applications
    echo "===== Cleaning AOT-compiled code ====="
    for apk in ${filtered_benchmarks}; do
        clean_apk_code_and_profile_data ${apk}
    done

    echo "===== Pushing JIT profiling data ====="
    for apk in ${filtered_benchmarks}; do
        push_cached_apk_profile_data ${apk}
    done

    # AOT-compile all benchmark applications
    echo "===== AOT-compiling benchmarks ====="
    for apk in ${filtered_benchmarks}; do
        aot_compile_apk_from_profile_data ${apk}
    done

    ${SCRIPTS_DIR}/apps/reset-app-data.sh ${orig_device}

    # This will run the stress factor experiment, i.e. trying to estimate the baseline
    # energy consumption by using linear regression
    #
    # if [[ "${build}" == *"mmtk"* ]]; then
    #     tmp_file=$(mktemp -p /tmp/$USER/)
    #     do_command cp ~/git/evaluation/configs/energy-stress/lbo-${build}-stress.yml ${tmp_file}
    #     echo -e "\nbenchmarks:\n  android-apps:" >> ${tmp_file}
    #     for bm in ${filtered_benchmark_names}; do
    #         echo "    - ${bm}" >> ${tmp_file}
    #     done

    #     echo -e ${GREEN}running -w "adb shell" runbms ~/git/evaluation/results/log ~/git/evaluation/configs/energy-stress/lbo-${build}-stress.yml --reset-top-apps -i 5 -p `date +%F`-energy-stress-apps-${device}-${build} -s 1.0${NORM}
    #     running -w "adb shell" runbms ~/git/evaluation/results/log ${tmp_file} --reset-top-apps -i 5 -p `date +%F`-energy-stress-apps-${device}-${build} -s 1.0

    #     do_command rm ${tmp_file}

    #     sleep 90
    # fi
    # sleep 120

    if [[ "${build}" == *"art-cc"* ]]; then
        echo -e ${GREEN}running -w "adb shell" runbms ~/git/evaluation/results/log ~/git/evaluation/configs/energy-stress/lbo-${build}.yml --reset-top-apps -i 10 -p `date +%F`-energy-hfac-${device}-${build} --invert-hfacs -s 1.0,1.125,1.25,2.0${NORM}
        running -w "adb shell" runbms ~/git/evaluation/results/log ${tmp_file} --reset-top-apps -i 10 -p `date +%F`-energy-hfac-${orig_device}-${build} --invert-hfacs -s 1.0,1.125,1.25,2.0

    else
        echo -e ${GREEN}running -w "adb shell" runbms ~/git/evaluation/results/log ~/git/evaluation/configs/energy-stress/lbo-${build}.yml --reset-top-apps -i 10 -p `date +%F`-energy-hfac-${device}-${build} --invert-hfacs -s 1.0,1.125,1.25,1.5,2.0,2.5,4.0${NORM}
        running -w "adb shell" runbms ~/git/evaluation/results/log ${tmp_file} --reset-top-apps -i 10 -p `date +%F`-energy-hfac-${orig_device}-${build} --invert-hfacs -s 1.0,1.125,1.25,1.5,2.0,2.5,4.0
    fi

    do_command rm ${tmp_file}

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
