#!/usr/bin/env python3

import argparse
import glob
import os
from pathlib import Path
import re

parser = argparse.ArgumentParser()
parser.add_argument("log_dir", type=Path)

p = parser.parse_args()

assert(p.log_dir.exists())

cc_pattern = "*.art-cc-*"
# ss_pattern = "*.art-ss-*"
tmp_pattern = "*.log.gz.tmp"

cc_files = glob.glob(cc_pattern, root_dir=p.log_dir)
for f in reversed(cc_files):
    splitted = f.split(".")
    hfac = int(splitted[1])
    actual_hfac = 2 * hfac
    heap = int(splitted[2])
    actual_heap = 2 * heap

    splitted[1] = str(actual_hfac)
    splitted[2] = str(actual_heap)
    new_name = ".".join(splitted) + ".tmp"

    print("Renaming {} -> {}".format(f, new_name))
    os.rename(p.log_dir / f, p.log_dir / new_name)

# ss_files = glob.glob(ss_pattern, root_dir=p.log_dir)
# for f in reversed(ss_files):
#     splitted = f.split(".")
#     hfac = int(splitted[1])
#     actual_hfac = 2 * hfac
#     heap = int(splitted[2])
#     actual_heap = 2 * heap
#
#     splitted[1] = str(actual_hfac)
#     splitted[2] = str(actual_heap)
#     new_name = ".".join(splitted) + ".tmp"
#
#     print("Renaming {} -> {}".format(f, new_name))
#     os.rename(p.log_dir / f, p.log_dir / new_name)

print()
print("Moving files to actual locations now")
print()

tmp_files = glob.glob(tmp_pattern, root_dir=p.log_dir)
for f in reversed(tmp_files):
    print("Renaming {} -> {}".format(f, f[:-4]))
    os.rename(p.log_dir / f, p.log_dir / f[:-4])

