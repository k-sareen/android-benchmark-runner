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
    cpus="0 6 7"
else
    policies="0 4 6"
    cpus="0 1 2 3 4 5 6 7"
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

if [[ "${device}" == "pixel7pro" ]]; then
    for i in ${cpus}; do
        echo "=== Available frequencies for gs_memlat_devfreq cpu${i} ==="
        adb shell cat "/sys/class/devfreq/gs_memlat_devfreq:devfreq_mif_cpu${i}_memlat@17000010/available_frequencies"
        echo "=== Current max frequency for gs_memlat_devfreq cpu${i} ==="
        adb shell cat "/sys/class/devfreq/gs_memlat_devfreq:devfreq_mif_cpu${i}_memlat@17000010/max_freq"
        echo "=== Available governors for gs_memlat_devfreq cpu${i} ==="
        adb shell cat "/sys/class/devfreq/gs_memlat_devfreq:devfreq_mif_cpu${i}_memlat@17000010/available_governors"
        echo "=== Current governor for gs_memlat_devfreq cpu${i} ==="
        adb shell cat "/sys/class/devfreq/gs_memlat_devfreq:devfreq_mif_cpu${i}_memlat@17000010/governor"

        frequency="3172000"
        echo "=== Setting ${frequency} max frequency for gs_memlat_devfreq cpu${i} ==="
        adb shell "echo ${frequency} > /sys/class/devfreq/gs_memlat_devfreq:devfreq_mif_cpu${i}_memlat@17000010/max_freq"
        echo "=== Setting ${governor} governor for gs_memlat_devfreq cpu${i} ==="
        adb shell "echo ${governor} > /sys/class/devfreq/gs_memlat_devfreq:devfreq_mif_cpu${i}_memlat@17000010/governor"
    done;
else
    for i in ${cpus}; do
        echo "=== Available frequencies for devfreq-l3 cpu${i} ==="
        adb shell cat "/sys/class/devfreq/18321000.qcom,devfreq-l3:qcom,cpu${i}-cpu-l3-lat/available_frequencies"
        echo "=== Current max frequency for devfreq-l3 cpu${i} ==="
        adb shell cat "/sys/class/devfreq/18321000.qcom,devfreq-l3:qcom,cpu${i}-cpu-l3-lat/max_freq"
        echo "=== Available governors for devfreq-l3 cpu${i} ==="
        adb shell cat "/sys/class/devfreq/18321000.qcom,devfreq-l3:qcom,cpu${i}-cpu-l3-lat/available_governors"
        echo "=== Current governor for devfreq-l3 cpu${i} ==="
        adb shell cat "/sys/class/devfreq/18321000.qcom,devfreq-l3:qcom,cpu${i}-cpu-l3-lat/governor"

        frequency="1516800000"
        echo "=== Setting ${frequency} max frequency for devfreq-l3 cpu${i} ==="
        adb shell "echo ${frequency} > /sys/class/devfreq/18321000.qcom,devfreq-l3:qcom,cpu${i}-cpu-l3-lat/max_freq"
        echo "=== Setting ${governor} governor for devfreq-l3 cpu${i} ==="
        adb shell "echo ${governor} > /sys/class/devfreq/18321000.qcom,devfreq-l3:qcom,cpu${i}-cpu-l3-lat/governor"

        if [[ $i != 7 ]]; then
            echo "=== Available frequencies for cpu-llcc-lat cpu${i} ==="
            adb shell cat "/sys/class/devfreq/soc:qcom,cpu${i}-cpu-llcc-lat/available_frequencies"
            echo "=== Current max frequency for cpu-llcc-lat cpu${i} ==="
            adb shell cat "/sys/class/devfreq/soc:qcom,cpu${i}-cpu-llcc-lat/max_freq"
            echo "=== Available governors for cpu-llcc-lat cpu${i} ==="
            adb shell cat "/sys/class/devfreq/soc:qcom,cpu${i}-cpu-llcc-lat/available_governors"
            echo "=== Current governor for cpu-llcc-lat cpu${i} ==="
            adb shell cat "/sys/class/devfreq/soc:qcom,cpu${i}-cpu-llcc-lat/governor"

            frequency="14236"
            echo "=== Setting ${frequency} max frequency for cpu-llcc-lat cpu${i} ==="
            adb shell "echo ${frequency} > /sys/class/devfreq/soc:qcom,cpu${i}-cpu-llcc-lat/max_freq"
            echo "=== Setting ${governor} governor for cpu-llcc-lat cpu${i} ==="
            adb shell "echo ${governor} > /sys/class/devfreq/soc:qcom,cpu${i}-cpu-llcc-lat/governor"

            echo "=== Available frequencies for llcc-ddr-lat cpu${i} ==="
            adb shell cat "/sys/class/devfreq/soc:qcom,cpu${i}-llcc-ddr-lat/available_frequencies"
            echo "=== Current max frequency for llcc-ddr-lat cpu${i} ==="
            adb shell cat "/sys/class/devfreq/soc:qcom,cpu${i}-llcc-ddr-lat/max_freq"
            echo "=== Available governors for llcc-ddr-lat cpu${i} ==="
            adb shell cat "/sys/class/devfreq/soc:qcom,cpu${i}-llcc-ddr-lat/available_governors"
            echo "=== Current governor for llcc-ddr-lat cpu${i} ==="
            adb shell cat "/sys/class/devfreq/soc:qcom,cpu${i}-llcc-ddr-lat/governor"

            frequency="7980"
            echo "=== Setting ${frequency} max frequency for llcc-ddr-lat cpu${i} ==="
            adb shell "echo ${frequency} > /sys/class/devfreq/soc:qcom,cpu${i}-llcc-ddr-lat/max_freq"
            echo "=== Setting ${governor} governor for llcc-ddr-lat cpu${i} ==="
            adb shell "echo ${governor} > /sys/class/devfreq/soc:qcom,cpu${i}-llcc-ddr-lat/governor"
        fi
    done;
fi
