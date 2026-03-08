#!/bin/sh

adb shell cp /system/bin/simpleperf $ART_TEST_CHROOT/system/bin/
adb shell cp /system/lib64/libprotobuf-cpp-lite.so $ART_TEST_CHROOT/system/lib64/
adb shell cp /system/lib64/libziparchive.so $ART_TEST_CHROOT/system/lib64/
adb shell cp /system/lib64/libevent.so $ART_TEST_CHROOT/system/lib64/
adb shell cp /system/lib64/libLLVM_android.so $ART_TEST_CHROOT/system/lib64/
