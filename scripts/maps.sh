#!/bin/bash

device="${1}"
build="${2}"
SERIAL_NO=""
PIXEL4A="08051JECB08812"
PIXEL7PRO="28091FDH3009B7"
DEVICE_NAME=""

case "${device}" in
    "pixel4a")
        DEVICE_NAME="Pixel 4a 5G"
        SERIAL_NO=${PIXEL4A}
        ;;
    "pixel7pro")
        DEVICE_NAME="Pixel 7 Pro"
        SERIAL_NO=${PIXEL7PRO}
        ;;
    *)
        echo "Unknown device. Please select one of pixel4a or pixel7pro"
        exit 1
        ;;
esac

# This makes all adb commands use the specified device
export ANDROID_SERIAL=${SERIAL_NO}

echo "===== Running Maps for ${build} ====="
# adb install build/${build}/com.android.art.apex
# adb reboot
#
# adb wait-for-device
# sleep 60
#
# # Unlock phone
# adb shell input keyevent 82
# sleep 5
#
# # Setup device for benchmarking
# ./setup-device.sh ${device}
# sleep 5

echo adb uninstall com.google.android.apps.maps
adb uninstall com.google.android.apps.maps
sleep 5

pushd ~/git/benchmarks/GoogleMaps &> /dev/null
echo adb install-multiple *.apk
adb install-multiple *.apk
sleep 5
popd &> /dev/null

echo running -w "adb shell" runbms ~/rough ~/git/evaluation/configs/ismm24/ismm24-android-maps.yml -s 1.0 -i 5 -p ${device}-${build}-jit-profiling
running -w "adb shell" runbms ~/rough ~/git/evaluation/configs/ismm24/ismm24-android-maps.yml -s 1.0 -i 5 -p ${device}-${build}-jit-profiling

# AOT-compile all benchmark applications
echo adb shell cmd package compile -m speed-profile -f com.google.android.apps.maps
adb shell cmd package compile -m speed-profile -f com.google.android.apps.maps

echo running -w "adb shell" runbms ~/git/evaluation/results/log ~/git/evaluation/configs/ismm24/${device}/lbo-${build}-maps.yml -s 1.000,1.100,1.242,1.428,1.657,1.928,2.242,2.600,3.000 -i 15 -p `date +%F`-${device}-${build}-maps-lbo
running -w "adb shell" runbms ~/git/evaluation/results/log ~/git/evaluation/configs/ismm24/${device}/lbo-${build}-maps.yml -s 1.000,1.100,1.242,1.428,1.657,1.928,2.242,2.600,3.000 -i 15 -p `date +%F`-${device}-${build}-maps-lbo

echo "===== Finished Maps for ${build} ====="
