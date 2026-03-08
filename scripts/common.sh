#!/bin/bash

device="${1}"
orig_device="${device}"

SERIAL_NO=""
PIXEL4A="08051JECB08812"
PIXEL6PRO_BLACK_1="22171FDEE001GG"
PIXEL6PRO_BLACK_2="22191FDEE00164"
PIXEL7PRO_WHITE="28091FDH3009B7"
PIXEL7PRO_GREY="29241FDH300J4A"
DEVICE_NAME=""
DEVICE_HOSTNAME=""

CYAN='\033[0;36m'
BLUE='\033[0;34m'
GREEN='\033[0;32m'
YELLOW='\033[0;33m'
RED='\033[0;31m'
NORM='\033[0;00m'

SCRIPTS_SOURCE="${BASH_SOURCE[0]}"
while [ -h "$SCRIPTS_SOURCE" ]; do
  SCRIPTS_DIR="$( cd -P "$( dirname "$SCRIPTS_SOURCE" )" >/dev/null 2>&1 && pwd )"
  SCRIPTS_SOURCE="$(readlink "$SCRIPTS_SOURCE")"
  [[ $SCRIPTS_SOURCE != /* ]] && SCRIPTS_SOURCE="$SCRIPTS_DIR/$SCRIPTS_SOURCE"
done
SCRIPTS_DIR="$( cd -P "$( dirname "$SCRIPTS_SOURCE" )" >/dev/null 2>&1 && pwd )"
EVALUATION_DIR=${SCRIPTS_DIR}/..
APP_PROFILES_DIR=${EVALUATION_DIR}/profiles/apps

ALL_BENCHMARKS="com.adobe.reader
com.airbnb.android
com.discord
com.google.android.gm
com.google.android.apps.magazines
com.instagram.android
com.google.android.apps.maps
com.zhiliaoapp.musically
tv.twitch.android.app
com.twitter.android
"

case "${device}" in
    "pixel4a")
        DEVICE_NAME="Pixel 4a 5G"
        DEVICE_HOSTNAME="bramble"
        SERIAL_NO=${PIXEL4A}
        ;;
    "pixel6pro"|"pixel6pro1")
        DEVICE_NAME="Pixel 6 Pro Black"
        DEVICE_HOSTNAME="raven"
        SERIAL_NO=${PIXEL6PRO_BLACK_1}
        device="pixel6pro"
        ;;
    "pixel6pro2")
        DEVICE_NAME="Pixel 6 Pro Black"
        DEVICE_HOSTNAME="raven"
        SERIAL_NO=${PIXEL6PRO_BLACK_2}
        device="pixel6pro"
        ;;
    "pixel7pro"|"pixel7pro1")
        DEVICE_NAME="Pixel 7 Pro White"
        DEVICE_HOSTNAME="cheetah"
        SERIAL_NO=${PIXEL7PRO_WHITE}
        device="pixel7pro"
        ;;
    "pixel7pro2")
        DEVICE_NAME="Pixel 7 Pro Grey"
        DEVICE_HOSTNAME="cheetah"
        SERIAL_NO=${PIXEL7PRO_GREY}
        device="pixel7pro"
        ;;
    *)
        echo -e "${RED}Unknown device. Please select one of pixel4a, pixel6pro1, pixel6pro2, pixel7pro1, or pixel7pro2${NORM}"
        exit 1
        ;;
esac

# TODO: Add more cmdline options for dry runs etc.

# This makes all adb commands use the specified device
export ANDROID_SERIAL=${SERIAL_NO}

# Check if device is connected
adb shell ls > /dev/null
connect_status=${?}
if [[ ${connect_status} != 0 ]]; then
    echo -e "${RED}${DEVICE_NAME} not connected! Not executing further commands${NORM}"
    exit 1
fi

# Source device specific functions
source $SCRIPTS_DIR/common-pixel7pro.sh
source $SCRIPTS_DIR/common-pixel6pro.sh
source $SCRIPTS_DIR/common-pixel4a.sh

# Skip benchmarks that start with "# ". Note that this will only
# help with skipping cleaning and compiling JIT profile data. The
# running configs will need to be updated separately.
filter_benchmarks () {
    local benchmarks=${1}
    local filtered=""

    filtered=$(
        local f=""
        IFS=$'\n'
        for apk in ${benchmarks}; do
            # Skip benchmarks that start with "# "
            if [[ "${apk}" == *"# "* ]]; then
                continue
            fi
            f="${f} ${apk}"
        done
        echo ${f}
    )
    echo ${filtered}
}

do_command () {
    local cmd=${@}
    echo -e ${GREEN}${cmd}${NORM}
    ${cmd}
}

adb_root () {
    do_command adb root
}

disable_selinux () {
    do_command adb shell setenforce 0
}

wait_for_device () {
    local sleep_time=${1}
    do_command adb wait-for-device
    sleep ${sleep_time}
}

reboot_bootloader () {
    do_command adb reboot bootloader
}

fastboot_wait_for_device () {
    local sleep_time=${1}
    do_command fastboot wait-for-device
    sleep ${sleep_time}
}

fastboot_visible () {
    local serial_no=${1}
    fastboot_out=$(fastboot devices)
    if [[ "${fastboot_out}" != *"${serial_no}"* ]]; then
        echo -e "${RED}Device (${serial_no}) not in fastboot!${NORM}"
        return 1
    else
        echo -e "${GREEN}Device (${serial_no}) in fastboot!${NORM}"
        return 0
    fi
}

# Boot device with new boot image and kernel cmdline temporarily.
fastboot_temporary_boot_cmdline () {
    local boot_img=${1}
    local cmdline=${2}
    do_command fastboot boot --cmdline="${cmdline}" ${boot_img}
}

# Check if the kernel cmdline on the device contains the argument we want.
check_kernel_cmdline_contains_value () {
    local arg=${1}
    local print_str=${2}
    local cmdline=$(adb shell "cat /proc/cmdline")
    if [[ "${cmdline}" != *"${arg}"* ]]; then
        echo ${cmdline}
        echo -e "${RED}=== ${print_str} not found in kernel cmdline! ===${NORM}"
        return 1
    else
        echo -e "${GREEN}=== ${print_str} found in kernel cmdline! ===${NORM}"
        return 0
    fi
}

install_build () {
    local build=${1}
    do_command adb install ${EVALUATION_DIR}/build/${build}/com.android.art.apex
    do_command adb reboot
}

install_apk () {
    local apk=${1}
    do_command adb install ${apk}
    sleep 5
}

install_multi_apk () {
    local apks_dir=${1}
    pushd ${apks_dir} &> /dev/null
    do_command adb install-multiple *.apk
    sleep 5
    popd &> /dev/null
}

uninstall_apk () {
    local apk=${1}
    do_command adb uninstall ${apk}
    sleep 5
}

press_home_button () {
    do_command adb shell input keyevent KEYCODE_HOME
    sleep 5
}

get_display_state () {
    # raw_screen_state is of form "mScreenState=OFF_LOCKED"
    # We split on "=" to extract screen state
    local raw_screen_state=$(adb shell dumpsys nfc | grep 'mScreenState=')
    local screen_state=${raw_screen_state#*=}
    echo ${screen_state}
}

toggle_display () {
    do_command adb shell input keyevent KEYCODE_POWER
    sleep 1
    local display_state=$(get_display_state)
    echo "===== Screen state for device ${device} is ${display_state} now ====="
}

turn_on_display () {
    local state=$(get_display_state)
    if [[ "${state}" == *"OFF"* ]]; then
        toggle_display
    elif [[ "${state}" == *"OFF"* ]]; then
        echo "===== Screen state for device ${device} is already on ====="
    fi
}

turn_off_display () {
    local state=$(get_display_state)
    if [[ "${state}" == *"ON"* ]]; then
        toggle_display
    elif [[ "${state}" == *"OFF"* ]]; then
        echo "===== Screen state for device ${device} is already off ====="
    fi
}

unlock_device () {
    local display_state=$(get_display_state)
    if [[ "${display_state}" == "ON_LOCKED" ]]; then
        do_command adb shell input keyevent KEYCODE_MENU
        sleep 5
    elif [[ "${display_state}" == "ON_UNLOCKED" ]]; then
        echo -e "${GREEN}Device ${orig_device} already unlocked${NORM}"
    elif [[ "${display_state}" == "OFF_LOCKED" ]]; then
        echo -e "${YELLOW}Device ${orig_device} display was off!${NORM}"
        toggle_display
        do_command adb shell input keyevent KEYCODE_MENU
        sleep 5
    fi
}

set_airplane_mode () {
    local state=${1}
    do_command adb shell cmd connectivity airplane-mode ${state}
    sleep 25
}

suppress_ui_automator_logs () {
    do_command adb shell setprop log.tag.ByMatcher SUPPRESS
    do_command adb shell setprop log.tag.UiDevice SUPPRESS
}

disable_bg_dexopt_jobs () {
    do_command adb shell setprop pm.dexopt.disable_bg_dexopt true
    do_command adb shell pm bg-dexopt-job --disable
}

enable_bg_dexopt_jobs () {
    do_command adb shell setprop pm.dexopt.disable_bg_dexopt false
}

clean_apk_code_and_profile_data () {
    local apk=${1}
    do_command adb shell cmd package compile --reset -f ${apk}
}

aot_compile_apk_from_profile_data () {
    local apk=${1}
    do_command adb shell cmd package compile --full -m speed-profile -f ${apk}
}

# u0 = 10000. Assumes that we are working on the main user profile
get_app_id_for_apk () {
    local apk=${1}
    uid_complete=$(adb shell "cmd package list package -U ${apk} | grep -oE '${apk} uid:[0-9]+' | grep -oE 'uid:[0-9]+' | grep -oE '[0-9]+'")
    uid="a${uid_complete:2}"
    echo ${uid}
}

get_location_for_apk () {
    local apk=${1}
    location=$(adb shell "cmd package list package -f ${apk} | grep -oE '.*=${apk}$' | grep -oE '/data/app/.*' | grep -oE -m 5 '.*/'")
    echo ${location}
}

print_dexopt_data_for_apk () {
    local apk=${1}
    do_command adb shell "dumpsys package dexopt | grep -A3 '\[${apk}\]'"
}

pull_apk_profile_data () {
    local apk=${1}
    local apk_profiles_dir=${APP_PROFILES_DIR}/${apk}

    local prof_file=/data/misc/profiles/cur/0/${apk}/primary.prof
    if [[ -f "${apk_profiles_dir}/primary-cur-${device}.prof" ]]; then
        mv "${apk_profiles_dir}/primary-cur-${device}.prof" "${apk_profiles_dir}/primary-cur-${device}.prof.old"
    fi
    do_command adb pull ${prof_file} ${apk_profiles_dir}/primary-cur-${device}.prof

    prof_file=/data/misc/profiles/ref/${apk}/primary.prof
    if [[ -f "${apk_profiles_dir}/primary-ref-${device}.prof" ]]; then
        mv "${apk_profiles_dir}/primary-ref-${device}.prof" "${apk_profiles_dir}/primary-ref-${device}.prof.old"
    fi
    do_command adb pull ${prof_file} ${apk_profiles_dir}/primary-ref-${device}.prof
}

# Assumes we are using the main user u0 for running benchmarks. Removes or overwrites
# previous profile files if there are any. Alternatively, always run this after
# `clean_apk_code_and_profile_data`
push_cached_apk_profile_data () {
    local apk=${1}
    local apk_profiles_dir=${APP_PROFILES_DIR}/${apk}
    local cur_prof_file=/data/misc/profiles/cur/0/${apk}/primary.prof
    local ref_prof_file=/data/misc/profiles/ref/${apk}/primary.prof

    if [[ -f "${apk_profiles_dir}/primary-cur-${device}.prof" ]]; then
        do_command adb push ${apk_profiles_dir}/primary-cur-${device}.prof ${cur_prof_file}

        local uid=$(get_app_id_for_apk ${apk})
        do_command adb shell chmod 600 ${cur_prof_file}
        do_command adb shell chown u0_${uid}:u0_${uid} ${cur_prof_file}
    else
        echo -e "${YELLOW}Unable to find cached cur profile file. Deleting existing file on device!${NORM}"
        do_command adb shell rm ${cur_prof_file}
    fi

    if [[ -f "${apk_profiles_dir}/primary-ref-${device}.prof" ]]; then
        do_command adb push ${apk_profiles_dir}/primary-ref-${device}.prof ${ref_prof_file}

        local uid=$(get_app_id_for_apk ${apk})
        do_command adb shell chmod 640 ${ref_prof_file}
        do_command adb shell chown system:all_${uid} ${ref_prof_file}
    else
        echo -e "${YELLOW}Unable to find cached ref profile file. Deleting existing file on device!${NORM}"
        do_command adb shell rm ${ref_prof_file}
    fi
}

generate_jit_profile_data () {
    local config_file=${1}
    do_command mkdir -p ~/rough
    echo -e ${GREEN}running -w "adb shell" runbms ~/rough ${config_file} -s 1.0 -i 4 -p $(date +%F)-jit-profiling-${orig_device}-${build}${NORM}
    running -w "adb shell" runbms ~/rough ${config_file} -s 1.0 -i 4 -p $(date +%F)-jit-profiling-${orig_device}-${build}
}

check_device_and_fail () {
    local expected_device=${1}
    local print_str=${2}
    if [[ "${device}" != "${expected_device}" ]]; then
        echo "=== Unsupported operation (${print_str}) on device ${device}! ==="
        exit 1
    fi
}

get_values_for_policy () {
    local i=${1}
    local file=${2}
    local print_str=${3}
    echo "=== ${print_str} for policy${i} ==="
    adb shell cat "/sys/devices/system/cpu/cpufreq/policy${i}/${file}"
}

set_value_for_policy () {
    local i=${1}
    local file=${2}
    local value=${3}
    local print_str=${4}
    echo "=== Setting ${value} ${print_str} for policy${i} ==="
    adb shell "echo ${value} > /sys/devices/system/cpu/cpufreq/policy${i}/${file}"
}

get_available_cpu_frequencies_for_policy () {
    local policy=${1}
    get_values_for_policy ${policy} "scaling_available_frequencies" "Available frequencies"
}

get_max_frequency_for_policy () {
    local policy=${1}
    get_values_for_policy ${policy} "scaling_max_freq" "Current max frequency"
}

set_max_frequency_for_policy () {
    local policy=${1}
    local value=${2}
    set_value_for_policy ${policy} "scaling_min_freq" ${value} "min frequency"
    set_value_for_policy ${policy} "scaling_max_freq" ${value} "max frequency"
}

get_available_governors_for_policy () {
    local policy=${1}
    get_values_for_policy ${policy} "scaling_available_governors" "Available governors"
}

get_governor_for_policy () {
    local policy=${1}
    get_values_for_policy ${policy} "scaling_governor" "Current governor"
}

set_governor_for_policy () {
    local policy=${1}
    local value=${2}
    set_value_for_policy ${policy} "scaling_governor" ${value} "governor"
}

get_cpu_frequencies_and_governors () {
    local policies=${1}
    for i in ${policies}; do
        get_available_cpu_frequencies_for_policy ${i}
        get_max_frequency_for_policy ${i}
        get_available_governors_for_policy ${i}
        get_governor_for_policy ${i}
    done
}

set_cpu_frequencies_and_governors () {
    local policy_frequency_map=${1}
    local governor=${2}
    for policy_frequency in ${policy_frequency_map}; do
        # policy_frequency is a string of form "<policy>=<frequency>"
        # We split the string on "="
        local policy=${policy_frequency%%=*}
        local frequency=${policy_frequency#*=}
        set_max_frequency_for_policy ${policy} ${frequency}
        set_governor_for_policy ${policy} ${governor}
    done
}

workaround_adobe_acrobat_storage_permission () {
    do_command "adb shell cmd appops set --uid com.adobe.reader MANAGE_EXTERNAL_STORAGE allow"
}
