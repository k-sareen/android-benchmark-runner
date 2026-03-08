pixel6pro_set_battery_charging_state () {
    # adb shell "echo 80 > /sys/devices/platform/soc/soc:google,charger/charge_stop_level"
    # adb shell "echo 100 > /sys/devices/platform/soc/soc:google,charger/charge_start_level"
    echo "=== Did not set battery charging state! ==="
}

pixel6pro_get_memory_frequency_value_for_cpu () {
    check_device_and_fail "pixel6pro" "getting gs_memlat_devfreq"

    local i=${1}
    local file=${2}
    local print_str=${3}
    echo "=== ${print_str} for gs_memlat_devfreq cpu${i} ==="
    adb shell cat "/sys/class/devfreq/gs_memlat_devfreq:devfreq_mif_cpu${i}_memlat@17000010/${file}"
}

pixel6pro_set_memory_frequency_value_for_cpu () {
    check_device_and_fail "pixel6pro" "setting gs_memlat_devfreq"

    local i=${1}
    local file=${2}
    local value=${3}
    local print_str=${4}
    echo "=== Setting ${value} ${print_str} for gs_memlat_devfreq cpu${i} ==="
    adb shell "echo ${value} > /sys/class/devfreq/gs_memlat_devfreq:devfreq_mif_cpu${i}_memlat@17000010/${file}"
}

pixel6pro_get_memory_frequencies_and_governors () {
    check_device_and_fail "pixel6pro" "getting gs_memlat_devfreq"

    local cpus=${1}
    for i in ${cpus}; do
        pixel6pro_get_memory_frequency_value_for_cpu ${i} "available_frequencies" "Available frequencies"
        pixel6pro_get_memory_frequency_value_for_cpu ${i} "max_freq" "Current max frequency"
        pixel6pro_get_memory_frequency_value_for_cpu ${i} "available_governors" "Available governors"
        pixel6pro_get_memory_frequency_value_for_cpu ${i} "governor" "Current governor"
    done
}

pixel6pro_set_memory_frequencies_and_governors () {
    check_device_and_fail "pixel6pro" "setting gs_memlat_devfreq"

    local cpus=${1}
    local frequency=${2}
    local governor=${3}
    for i in ${cpus}; do
        pixel6pro_set_memory_frequency_value_for_cpu ${i} "min_freq" ${frequency} "min frequency"
        pixel6pro_set_memory_frequency_value_for_cpu ${i} "max_freq" ${frequency} "max frequency"
        pixel6pro_set_memory_frequency_value_for_cpu ${i} "governor" ${governor} "governor"
    done
}

pixel6pro_set_odpm_sampling_rate () {
    check_device_and_fail "pixel6pro" "setting ODPM"

    do_command adb shell "cd /sys/bus/iio/devices/iio:device0 && chmod -R 777 ."
    do_command adb shell "cd /sys/bus/iio/devices/iio:device1 && chmod -R 777 ."

    echo "=== Setting sampling_rate for ODPM to 1000 Hz ==="
    do_command adb shell "echo 1000 > /sys/bus/iio/devices/iio:device0/sampling_rate"
    do_command adb shell "echo 1000 > /sys/bus/iio/devices/iio:device1/sampling_rate"
}

pixel6pro_isolate_cores () {
    check_device_and_fail "pixel6pro" "isolating cores"

    echo "=== Checking /proc/cmdline for isolcpus ==="
    cmdline=$(adb shell "cat /proc/cmdline")
    echo ${cmdline}

    if [[ "${cmdline}" != *"isolcpus=4,5,6,7"* ]]; then
        echo "=== isolcpus not found in kernel cmdline! ==="
    fi

    echo "=== Setting all cgroups to use little cores ==="
    do_command adb shell "echo '0-3' > /dev/cpuset/camera-daemon/cpus"
    do_command adb shell "echo '0-3' > /dev/cpuset/camera-daemon-high-group/cpus"
    do_command adb shell "echo '0-3' > /dev/cpuset/camera-daemon-mid-high-group/cpus"
    do_command adb shell "echo '0-3' > /dev/cpuset/camera-daemon-mid-group/cpus"
    do_command adb shell "echo '0-3' > /dev/cpuset/foreground/cpus"

    echo "=== Clearing top apps cgroup ==="
    do_command adb shell /data/local/reset_top_apps.sh
}
