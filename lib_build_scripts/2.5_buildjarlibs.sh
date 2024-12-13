#!/bin/bash
# stop if has err
set -e
# download src and create necessary dirs by `downloadsrc.sh`
export build_root=~/puppylibsbuild
export build_out=$build_root/out
export build_src=$build_root/src
mkdir -p $build_out
mkdir -p $build_src

echo "no jar need build for now"


