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

echo "===== Starting benchmark run on ${DEVICE_NAME} ====="

echo running -w "adb shell" runbms ~/git/evaluation/results/log ~/git/evaluation/configs/ismm24/${device}/lbo-java.yml 8 -i 15 -p `date +%F`-${device}-lbo-java
running -w "adb shell" runbms ~/git/evaluation/results/log ~/git/evaluation/configs/ismm24/${device}/lbo-java.yml 8 -i 15 -p `date +%F`-${device}-lbo-java

echo running -w "adb shell" runbms ~/git/evaluation/results/log ~/git/evaluation/configs/ismm24/${device}/lbo-java-nogc.yml -i 15 -p `date +%F`-${device}-lbo-java-nogc
running -w "adb shell" runbms ~/git/evaluation/results/log ~/git/evaluation/configs/ismm24/${device}/lbo-java-nogc.yml -i 15 -p `date +%F`-${device}-lbo-java-nogc

echo "===== Finished benchmark run on ${DEVICE_NAME} ====="
