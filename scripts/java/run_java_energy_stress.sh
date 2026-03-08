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

turn_off_display

# Set up device for benchmarking
$SCRIPTS_DIR/setup-device.sh ${orig_device}
sleep 5

set_airplane_mode "enable"

echo "===== Starting benchmark run on ${DEVICE_NAME} ====="

echo running -w "adb shell" runbms ~/git/evaluation/results/log ~/git/evaluation/configs/energy-stress/ix-energy-stress-gc-java-pixel7pro.yml -i 5 -p `date +%F`-${device}-stress-ix
running -w "adb shell" runbms ~/git/evaluation/results/log ~/git/evaluation/configs/energy-stress/ix-energy-stress-gc-java-pixel7pro.yml -i 5 -p `date +%F`-${device}-stress-ix

sleep 120

$SCRIPTS_DIR/setup-device.sh ${orig_device}

echo running -w "adb shell" runbms ~/git/evaluation/results/log ~/git/evaluation/configs/energy-stress/ss-energy-stress-gc-java-pixel7pro.yml -i 5 -p `date +%F`-${device}-stress-ix
running -w "adb shell" runbms ~/git/evaluation/results/log ~/git/evaluation/configs/energy-stress/ss-energy-stress-gc-java-pixel7pro.yml -i 5 -p `date +%F`-${device}-stress-ix

sleep 120

$SCRIPTS_DIR/setup-device.sh ${orig_device}

echo running -w "adb shell" runbms ~/git/evaluation/results/log ~/git/evaluation/configs/energy-stress/nogc-java-pixel7pro.yml -i 10 -p `date +%F`-${device}-nogc
running -w "adb shell" runbms ~/git/evaluation/results/log ~/git/evaluation/configs/energy-stress/nogc-java-pixel7pro.yml -i 10 -p `date +%F`-${device}-nogc

sleep 120

$SCRIPTS_DIR/setup-device.sh ${orig_device}

echo -e ${GREEN}running -w "adb shell" runbms ~/git/evaluation/results/log ~/git/evaluation/configs/energy-stress/stock-mmtk-gc-java-pixel7pro.yml -i 10 -s 1.0,1.125,1.25,1.5,2.0,2.5,4.0 -p `date +%F`-java-energy-hfac-${device}${NORM}
running -w "adb shell" runbms ~/git/evaluation/results/log ~/git/evaluation/configs/energy-stress/stock-mmtk-gc-java-pixel7pro.yml -i 10 -s 1.0,1.125,1.25,1.5,2.0,2.5,4.0 -p `date +%F`-java-energy-hfac-${device}

echo "===== Finished benchmark run on ${DEVICE_NAME} ====="
