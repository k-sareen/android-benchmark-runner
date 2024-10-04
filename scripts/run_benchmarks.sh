#!/bin/bash

device="${1}"
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

builds=""
if [[ "${device}" == "pixel7pro" ]]; then
    builds="art-ss-vm_1d1ac36ae8"
    builds="art-ss-stock-ismm24-camera-final-vm_358388732a art-cc-ismm24-camera-final-vm_17ff06ce4a art-ss-ismm24-camera-final-vm_17ff06ce4a art-cmc-ismm24-camera-final-vm_17ff06ce4a"
else
    builds="art-ss-stock-ismm24-pixel4a-camera-final-vm_10deaf58bb art-cc-ismm24-camera-final-vm_17ff06ce4a art-ss-ismm24-pixel4a-camera-final-vm_587897c13e"
fi

echo "===== Starting benchmark run on ${DEVICE_NAME} ====="
for build in ${builds}; do
    echo "===== Running ${build} ====="
    adb install build/${build}/com.android.art.apex
    adb reboot

    adb wait-for-device
    sleep 60

    # Unlock phone
    adb shell input keyevent 82
    sleep 5

    # Setup device for benchmarking
    ./setup-device.sh ${device}
    sleep 5

    # echo adb uninstall com.google.android.apps.maps
    # adb uninstall com.google.android.apps.maps
    # sleep 5

    # pushd ~/git/benchmarks/GoogleMaps &> /dev/null
    # echo adb install-multiple *.apk
    # adb install-multiple *.apk
    # sleep 5
    # popd &> /dev/null

    # Run applications with default heap sizes to generate JIT profile data
    echo running -w "adb shell" runbms ~/rough ~/git/evaluation/configs/ismm24/ismm24-android-${device}.yml -s 1.0 -i 4 -p ${device}-${build}-jit-profiling
    running -w "adb shell" runbms ~/rough ~/git/evaluation/configs/ismm24/ismm24-android-${device}.yml -s 1.0 -i 4 -p ${device}-${build}-jit-profiling

    # echo running -w "adb shell" runbms ~/rough ~/git/evaluation/configs/ismm24/ismm24-android-${device}.yml -s 1.0 -i 5 -p ${device}-${build}-jit-profiling
    # running -w "adb shell" runbms ~/rough ~/git/evaluation/configs/ismm24/ismm24-android-${device}.yml -s 1.0 -i 5 -p ${device}-${build}-jit-profiling

    # # AOT-compile all benchmark applications
    echo adb shell cmd package compile -m speed-profile -f com.adobe.reader
    adb shell cmd package compile -m speed-profile -f com.adobe.reader
    echo adb shell cmd package compile -m speed-profile -f com.airbnb.android
    adb shell cmd package compile -m speed-profile -f com.airbnb.android
    echo adb shell cmd package compile -m speed-profile -f com.discord
    adb shell cmd package compile -m speed-profile -f com.discord
    echo adb shell cmd package compile -m speed-profile -f com.google.android.gm
    adb shell cmd package compile -m speed-profile -f com.google.android.gm
    echo adb shell cmd package compile -m speed-profile -f com.google.android.apps.magazines
    adb shell cmd package compile -m speed-profile -f com.google.android.apps.magazines
    echo adb shell cmd package compile -m speed-profile -f com.instagram.android
    adb shell cmd package compile -m speed-profile -f com.instagram.android
    # echo adb shell cmd package compile -m speed-profile -f com.google.android.apps.maps
    # adb shell cmd package compile -m speed-profile -f com.google.android.apps.maps
    echo adb shell cmd package compile -m speed-profile -f com.zhiliaoapp.musically
    adb shell cmd package compile -m speed-profile -f com.zhiliaoapp.musically
    echo adb shell cmd package compile -m speed-profile -f tv.twitch.android.app
    adb shell cmd package compile -m speed-profile -f tv.twitch.android.app
    echo adb shell cmd package compile -m speed-profile -f com.twitter.android
    adb shell cmd package compile -m speed-profile -f com.twitter.android
    echo adb shell cmd package compile -m speed-profile -f org.anu.benchmarkrunner
    adb shell cmd package compile -m speed-profile -f org.anu.benchmarkrunner

    echo running -w "adb shell" runbms ~/git/evaluation/results/log ~/git/evaluation/configs/ismm24/${device}/lbo-${build}.yml 8 -i 15 -p `date +%F`-${device}-${build}-lbo
    running -w "adb shell" runbms ~/git/evaluation/results/log ~/git/evaluation/configs/ismm24/${device}/lbo-${build}.yml 8 -i 15 -p `date +%F`-${device}-${build}-lbo

    # ./maps.sh "${device}" "${build}"
    echo "===== Finished ${build} ====="
    sleep 20
done
echo "===== Finished benchmark run on ${DEVICE_NAME} ====="

