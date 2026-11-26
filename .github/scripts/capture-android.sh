#!/usr/bin/env bash
# Installs the debug APK on the running emulator and opens each README scene (`--es scene <name>`).
# Fails if HireReel is not in the foreground, so a launcher screenshot is never committed.
set -euo pipefail

APP_ID="com.gayatripatel.hirereel.kmp"
APK="$1"
OUT="docs/screenshots/android"
mkdir -p "$OUT"

adb wait-for-device
until [ "$(adb shell getprop sys.boot_completed | tr -d '\r')" = "1" ]; do sleep 2; done
sleep 30   # a cold-booted emulator is slow for the first minute

adb install -r "$APK"
adb shell settings put global sysui_demo_allowed 1
adb shell am broadcast -a com.android.systemui.demo -e command enter >/dev/null
adb shell am broadcast -a com.android.systemui.demo -e command clock -e hhmm 0930 >/dev/null
adb shell am broadcast -a com.android.systemui.demo -e command notifications -e visible false >/dev/null

foreground() { adb shell dumpsys activity activities | grep -E "topResumedActivity|mResumedActivity" | grep -q "$APP_ID"; }

# A for loop, not `while read`: adb and simctl would swallow the list from stdin.
for scene in $(cat .github/scripts/scenes.txt); do
  adb shell am force-stop "$APP_ID"
  adb shell am broadcast -a android.intent.action.CLOSE_SYSTEM_DIALOGS >/dev/null 2>&1 || true
  adb shell am start -W -n "$APP_ID/com.gayatripatel.hirereel.MainActivity" --es scene "$scene" >/dev/null
  for _ in $(seq 1 30); do foreground && break; sleep 2; done
  sleep 4
  if ! foreground; then
    echo "::error::HireReel is not in the foreground; refusing to save $scene"
    adb logcat -d -s AndroidRuntime:E | tail -60 || true
    exit 1
  fi
  adb exec-out screencap -p > "$OUT/$scene.png"
  echo "Saved $OUT/$scene.png"
done
ls -la "$OUT"
