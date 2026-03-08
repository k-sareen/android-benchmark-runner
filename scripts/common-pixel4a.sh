# L3 cache
pixel4a_get_l3_value_for_cpu () {
    check_device_and_fail "pixel4a" "getting devfreq-l3"

    local i=${1}
    local file=${2}
    local print_str=${3}
    echo "=== ${print_str} for devfreq-l3 cpu${i} ==="
    adb shell cat "/sys/class/devfreq/18321000.qcom,devfreq-l3:qcom,cpu${i}-cpu-l3-lat/${file}"
}

pixel4a_set_l3_value_for_cpu () {
    check_device_and_fail "pixel4a" "setting devfreq-l3"

    local i=${1}
    local file=${2}
    local value=${3}
    local print_str=${4}
    echo "=== Setting ${value} ${print_str} for devfreq-l3 cpu${i} ==="
    adb shell "echo ${value} > /sys/class/devfreq/18321000.qcom,devfreq-l3:qcom,cpu${i}-cpu-l3-lat/${file}"
}

# Last-level cache controller (LLCC)
pixel4a_get_llcc_value_for_cpu () {
    check_device_and_fail "pixel4a" "getting cpu-llcc-lat"

    local i=${1}
    local file=${2}
    local print_str=${3}
    echo "=== ${print_str} for cpu-llcc-lat cpu${i} ==="
    adb shell cat "/sys/class/devfreq/soc:qcom,cpu${i}-cpu-llcc-lat/${file}"
}

pixel4a_set_llcc_value_for_cpu () {
    check_device_and_fail "pixel4a" "setting cpu-llcc-lat"

    local i=${1}
    local file=${2}
    local value=${3}
    local print_str=${4}
    echo "=== Setting ${value} ${print_str} for cpu-llcc-lat cpu${i} ==="
    adb shell "echo ${value} > /sys/class/devfreq/soc:qcom,cpu${i}-cpu-llcc-lat/${file}"
}

pixel4a_get_llcc_bw_value () {
    check_device_and_fail "pixel4a" "getting cpu-llcc-bw"

    local file=${1}
    local print_str=${2}
    echo "=== ${print_str} for cpu-llcc-bw ==="
    adb shell cat "/sys/class/devfreq/soc:qcom,cpu-cpu-llcc-bw/${file}"
}

pixel4a_set_llcc_bw_value () {
    check_device_and_fail "pixel4a" "setting cpu-llcc-bw"

    local file=${1}
    local value=${2}
    local print_str=${3}
    echo "=== Setting ${value} ${print_str} for cpu-llcc-bw ==="
    adb shell "echo ${value} > /sys/class/devfreq/soc:qcom,cpu-cpu-llcc-bw/${file}"
}

# DRAM
pixel4a_get_ddr_value_for_cpu () {
    check_device_and_fail "pixel4a" "getting llcc-ddr-lat"

    local i=${1}
    local file=${2}
    local print_str=${3}
    echo "=== ${print_str} for llcc-ddr-lat cpu${i} ==="
    adb shell cat "/sys/class/devfreq/soc:qcom,cpu${i}-llcc-ddr-lat/${file}"
}

pixel4a_set_ddr_value_for_cpu () {
    check_device_and_fail "pixel4a" "setting llcc-ddr-lat"

    local i=${1}
    local file=${2}
    local value=${3}
    local print_str=${4}
    echo "=== Setting ${value} ${print_str} for llcc-ddr-lat cpu${i} ==="
    adb shell "echo ${value} > /sys/class/devfreq/soc:qcom,cpu${i}-llcc-ddr-lat/${file}"
}

pixel4a_get_ddr_bw_value () {
    check_device_and_fail "pixel4a" "getting llcc-ddr-bw"

    local file=${1}
    local print_str=${2}
    echo "=== ${print_str} for llcc-ddr-bw ==="
    adb shell cat "/sys/class/devfreq/soc:qcom,cpu-llcc-ddr-bw/${file}"
}

pixel4a_set_ddr_bw_value () {
    check_device_and_fail "pixel4a" "setting llcc-ddr-bw"

    local file=${1}
    local value=${2}
    local print_str=${3}
    echo "=== Setting ${value} ${print_str} for llcc-ddr-bw ==="
    adb shell "echo ${value} > /sys/class/devfreq/soc:qcom,cpu-llcc-ddr-bw/${file}"
}

pixel4a_get_memory_frequencies_and_governors () {
    check_device_and_fail "pixel4a" "getting devfreq (l3 llcc ddr)"

    local cpus=${1}

    pixel4a_get_llcc_bw_value "available_frequencies" "Available frequencies"
    pixel4a_get_llcc_bw_value "max_freq" "Current max frequency"
    pixel4a_get_llcc_bw_value "available_governors" "Available governors"
    pixel4a_get_llcc_bw_value "governor" "Current governor"

    pixel4a_get_ddr_bw_value "available_frequencies" "Available frequencies"
    pixel4a_get_ddr_bw_value "max_freq" "Current max frequency"
    pixel4a_get_ddr_bw_value "available_governors" "Available governors"
    pixel4a_get_ddr_bw_value "governor" "Current governor"

    for i in ${cpus}; do
        pixel4a_get_l3_value_for_cpu ${i} "available_frequencies" "Available frequencies"
        pixel4a_get_l3_value_for_cpu ${i} "max_freq" "Current max frequency"
        pixel4a_get_l3_value_for_cpu ${i} "available_governors" "Available governors"
        pixel4a_get_l3_value_for_cpu ${i} "governor" "Current governor"

        if [[ $i != 7 ]]; then
            pixel4a_get_llcc_value_for_cpu ${i} "available_frequencies" "Available frequencies"
            pixel4a_get_llcc_value_for_cpu ${i} "max_freq" "Current max frequency"
            pixel4a_get_llcc_value_for_cpu ${i} "available_governors" "Available governors"
            pixel4a_get_llcc_value_for_cpu ${i} "governor" "Current governor"

            pixel4a_get_ddr_value_for_cpu ${i} "available_frequencies" "Available frequencies"
            pixel4a_get_ddr_value_for_cpu ${i} "max_freq" "Current max frequency"
            pixel4a_get_ddr_value_for_cpu ${i} "available_governors" "Available governors"
            pixel4a_get_ddr_value_for_cpu ${i} "governor" "Current governor"
        fi
    done
}

pixel4a_set_memory_frequencies_and_governors () {
    check_device_and_fail "pixel4a" "setting devfreq (l3 llcc ddr)"

    local cpus=${1}
    local l3_frequency=${2}
    local llcc_frequency=${3}
    local ddr_frequency=${4}
    local governor=${5}

    pixel4a_set_llcc_bw_value "min_freq" ${llcc_frequency} "min frequency"
    pixel4a_set_llcc_bw_value "max_freq" ${llcc_frequency} "max frequency"
    pixel4a_set_llcc_bw_value "governor" ${governor} "governor"

    pixel4a_set_ddr_bw_value "min_freq" ${ddr_frequency} "min frequency"
    pixel4a_set_ddr_bw_value "max_freq" ${ddr_frequency} "max frequency"
    pixel4a_set_ddr_bw_value "governor" ${governor} "governor"

    for i in ${cpus}; do
        pixel4a_set_l3_value_for_cpu ${i} "min_freq" ${l3_frequency} "min frequency"
        pixel4a_set_l3_value_for_cpu ${i} "max_freq" ${l3_frequency} "max frequency"
        pixel4a_set_l3_value_for_cpu ${i} "governor" ${governor} "governor"

        if [[ $i != 7 ]]; then
            pixel4a_set_llcc_value_for_cpu ${i} "min_freq" ${llcc_frequency} "min frequency"
            pixel4a_set_llcc_value_for_cpu ${i} "max_freq" ${llcc_frequency} "max frequency"
            pixel4a_set_llcc_value_for_cpu ${i} "governor" ${governor} "governor"

            pixel4a_set_ddr_value_for_cpu ${i} "min_freq" ${ddr_frequency} "min frequency"
            pixel4a_set_ddr_value_for_cpu ${i} "max_freq" ${ddr_frequency} "max frequency"
            pixel4a_set_ddr_value_for_cpu ${i} "governor" ${governor} "governor"
        fi
    done
}

pixel4a_isolate_cores () {
    check_device_and_fail "pixel4a" "isolating cores"

    echo "=== Checking /proc/cmdline for isolcpus ==="
    cmdline=$(adb shell "cat /proc/cmdline")
    echo ${cmdline}

    if [[ "${cmdline}" != *"isolcpus=6,7"* ]]; then
        echo "=== isolcpus not found in kernel cmdline! ==="
    fi

    echo "=== Setting all cgroups to use little cores ==="
    do_command adb shell "echo '0-5' > /dev/cpuset/camera-daemon/cpus"
    do_command adb shell "echo '0-5' > /dev/cpuset/camera-daemon-dedicated/cpus"
    do_command adb shell "echo '0-7' > /dev/cpuset/foreground/cpus"

    echo "=== Clearing top apps cgroup ==="
    do_command adb shell /data/local/reset_top_apps.sh
}
