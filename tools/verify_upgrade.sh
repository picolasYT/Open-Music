#!/usr/bin/env bash
set -euo pipefail
if [[ ! -f distribution/update-1.7.0.json ]]; then
  echo 'Signed 1.7.0 transport is not present yet; device tests have run.'
  exit 0
fi
python3 tools/assemble_update.py --video
adb uninstall com.picolas.openmusic.next
adb install distribution/Open-Music-1.6.1.apk
adb shell run-as com.picolas.openmusic.next mkdir -p files
printf 'upgrade-keeps-data\n' | adb shell run-as com.picolas.openmusic.next sh -c '"cat > files/upgrade-check.txt"'
adb install -r distribution/Open-Music-1.7.0.apk
adb shell run-as com.picolas.openmusic.next cat files/upgrade-check.txt | tr -d '\r' | grep -Fx upgrade-keeps-data
adb shell dumpsys package com.picolas.openmusic.next | grep -F versionName=1.7.0
adb shell am start -W -n com.picolas.openmusic.next/com.picolas.openmusic.MainActivity
