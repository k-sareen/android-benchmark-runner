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

benchmarks="com.adobe.reader=AcrobatSearchAndScrollTest
com.google.android.apps.magazines=GoogleNewsScrollTest
com.google.android.apps.maps=MapsRoutePreviewTest
com.zhiliaoapp.musically=TikTokScrollTest
com.twitter.android=TwitterScrollTest
"

heap_factors="1.0 2.0 4.0 1.25 2.5 1.125 1.5"

builds=""
if [[ "${device}" == "pixel7pro" ]] || [[ "${device}" == "pixel6pro" ]]; then
    # builds="art-cc-gc-thread-energy art-cmc-gc-thread-energy art-mmtk-multithread-immix-energy art-mmtk-singlethread-immix-energy art-mmtk-multithread-ss-energy art-mmtk-singlethread-ss-energy"
    builds="art-cc-gc-thread-energy art-cmc-gc-thread-energy art-mmtk-singlethread-immix-energy art-mmtk-singlethread-ss-energy art-mmtk-singlethread-nogc-energy art-mmtk-multithread-immix-energy art-mmtk-multithread-ss-energy"
else
    echo -e "${RED}===== Can't run experiment on specified device ${device}! =====${NORM}"
    exit 1
fi

filtered_benchmarks=$(filter_benchmarks "${benchmarks}")
cp ~/git/evaluation/configs/energy-stress/lbo-apps-base.yml /tmp/$USER

echo "===== Starting benchmark run on ${DEVICE_NAME} ====="
for hfac in ${heap_factors}; do
    for bmpair in ${filtered_benchmarks}; do
        # bmpair is a string of form "<apk>=<bm name>"
        # We split the string on "="
        apk=${bmpair%%=*}
        bm=${bmpair#*=}

        for build in ${builds}; do
            echo "===== Running ${build} ====="

            tmp_file=$(mktemp -p /tmp/$USER/)
            do_command cp ~/git/evaluation/configs/energy-stress/lbo-${build}.yml ${tmp_file}
            echo -e "\nbenchmarks:\n  android-apps:" >> ${tmp_file}
            echo "    - ${bm}" >> ${tmp_file}

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
            clean_apk_code_and_profile_data ${apk}

            echo "===== Pushing JIT profiling data ====="
            push_cached_apk_profile_data ${apk}

            # AOT-compile all benchmark applications
            echo "===== AOT-compiling benchmarks ====="
            aot_compile_apk_from_profile_data ${apk}

            ${SCRIPTS_DIR}/apps/reset-app-data-single.sh ${orig_device} ${apk}

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

            echo -e ${GREEN}running -w "adb shell" runbms ~/git/evaluation/results/log ~/git/evaluation/configs/energy-stress/lbo-${build}.yml --reset-top-apps -i 10 -p energy-hfac-${build}-${orig_device} -s ${hfac}${NORM}
            running -w "adb shell" runbms ~/git/evaluation/results/log ${tmp_file} --reset-top-apps -i 10 -p energy-hfac-${build}-${orig_device} -s ${hfac}

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
    done
done
echo "===== Finished benchmark run on ${DEVICE_NAME} ====="
