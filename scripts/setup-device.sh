#!/bin/bash

device="$1"

SERIAL_NO=""
PIXEL4A="08051JECB08812"
PIXEL7PRO="28091FDH3009B7"

case "${device}" in
    "pixel4a")
        SERIAL_NO=${PIXEL4A}
        ;;
    "pixel7pro")
        SERIAL_NO=${PIXEL7PRO}
        ;;
    *)
        echo "Unknown device. Please select one of pixel4a or pixel7pro"
        exit 1
        ;;
esac

governor="performance"

export ANDROID_SERIAL=${SERIAL_NO}

adb root
adb shell setenforce 0

policies=""
if [[ "${device}" == "pixel4a" ]]; then
    policies="0 6 7"
else
    policies="0 4 6"
fi

# adb shell "echo 80 > /sys/devices/platform/soc/soc:google,charger/charge_stop_level"
# adb shell "echo 100 > /sys/devices/platform/soc/soc:google,charger/charge_start_level"

for i in ${policies}; do
    echo "=== Available frequencies for policy${i} ==="
    adb shell cat "/sys/devices/system/cpu/cpufreq/policy${i}/scaling_available_frequencies"
    echo "=== Current max frequency for policy${i} ==="
    adb shell cat "/sys/devices/system/cpu/cpufreq/policy${i}/scaling_max_freq"
    echo "=== Available governors for policy${i} ==="
    adb shell cat "/sys/devices/system/cpu/cpufreq/policy${i}/scaling_available_governors"
    echo "=== Current governor for policy${i} ==="
    adb shell cat "/sys/devices/system/cpu/cpufreq/policy${i}/scaling_governor"

    if [[ "${device}" == "pixel4a" ]]; then
        if [[ $i == 0 ]]; then
            frequency="1516800"
        elif [[ $i == 6 ]]; then
            frequency="1900800"
        elif [[ $i == 7 ]]; then
            frequency="2188800"
        fi
    else
        if [[ $i == 0 ]]; then
            frequency="1401000"
        elif [[ $i == 4 ]]; then
            frequency="1999000"
        elif [[ $i == 6 ]]; then
            frequency="2401000"
        fi
    fi

    echo "=== Setting ${frequency} max frequency for policy${i} ==="
    adb shell "echo ${frequency} > /sys/devices/system/cpu/cpufreq/policy${i}/scaling_max_freq"
    echo "=== Setting ${governor} governor for policy${i} ==="
    adb shell "echo ${governor} > /sys/devices/system/cpu/cpufreq/policy${i}/scaling_governor"
done;
