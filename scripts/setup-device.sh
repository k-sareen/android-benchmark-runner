#!/bin/bash

source $HOME/git/evaluation/scripts/common.sh

adb_root
disable_selinux

disable_bg_dexopt_jobs
suppress_ui_automator_logs

governor="performance"
if [[ "${device}" == "pixel7pro" ]]; then
    policies="0 4 6"
    cpus="0 1 2 3 4 5 6 7"
    mem_frequency="3172000"

    # get_cpu_frequencies_and_governors "${policies}"
    set_cpu_frequencies_and_governors "0=1401000 4=1999000 6=2401000" ${governor}

    # pixel7pro_get_memory_frequencies_and_governors "${cpus}"
    pixel7pro_set_memory_frequencies_and_governors "${cpus}" ${mem_frequency} ${governor}

    pixel7pro_set_odpm_sampling_rate
    pixel7pro_isolate_cores
elif [[ "${device}" == "pixel6pro" ]]; then
    policies="0 4 6"
    cpus="0 1 2 3 4 5 6 7"
    mem_frequency="3172000"

    # get_cpu_frequencies_and_governors "${policies}"
    set_cpu_frequencies_and_governors "0=1401000 4=1999000 6=2401000" ${governor}

    # pixel6pro_get_memory_frequencies_and_governors "${cpus}"
    pixel6pro_set_memory_frequencies_and_governors "${cpus}" ${mem_frequency} ${governor}

    pixel6pro_set_odpm_sampling_rate
    pixel6pro_isolate_cores
elif [[ "${device}" == "pixel4a" ]]; then
    policies="0 6 7"
    cpus="0 6 7"
    l3_frequency="1516800000"
    llcc_frequency="16265"
    ddr_frequency="7980"

    # get_cpu_frequencies_and_governors "${policies}"
    set_cpu_frequencies_and_governors "0=1516800 6=1900800 7=2188800" ${governor}

    # pixel4a_get_memory_frequencies_and_governors "${cpus}"
    pixel4a_set_memory_frequencies_and_governors "${cpus}" ${l3_frequency} ${llcc_frequency} ${ddr_frequency} ${governor}

    pixel4a_isolate_cores
fi
