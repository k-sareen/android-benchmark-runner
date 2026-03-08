#!/bin/sh

cd /dev/cpuset
procs=$(cat top-app/cgroup.procs)

for prc in ${procs}; do
    echo "Moving ${prc} to background"
    echo ${prc} > background/cgroup.procs
done

procs=$(cat foreground/cgroup.procs)
for prc in ${procs}; do
    echo "Moving ${prc} to background"
    echo ${prc} > background/cgroup.procs
done
