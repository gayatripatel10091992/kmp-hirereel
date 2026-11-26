#!/usr/bin/env bash
# Installs the simulator build and launches it once per README scene (`-scene <name>`), saving a simulator screenshot.
# Fails if the app is not running, so a home-screen screenshot is never committed.
set -euo pipefail

APP_ID="com.gayatripatel.hirereel.kmp"
APP="$1"
OUT="docs/screenshots/ios"
mkdir -p "$OUT"

# Newest iPhone Pro simulator on this Xcode.
DEVICE=$(xcrun simctl list devices available -j | python3 -c '
import json, sys, re
best = None
for runtime, items in json.load(sys.stdin)["devices"].items():
    if "iOS" not in runtime:
        continue
    version = tuple(int(x) for x in re.findall(r"\d+", runtime.split("iOS")[-1]))
    for d in items:
        if re.fullmatch(r"iPhone \d+ Pro", d["name"]):
            key = (version, d["name"])
            if best is None or key > best[0]:
                best = (key, d["udid"], d["name"] + " / " + runtime)
print(best[1]); print("Simulator: " + best[2], file=sys.stderr)
')
xcrun simctl boot "$DEVICE" || true
xcrun simctl bootstatus "$DEVICE" -b
xcrun simctl status_bar "$DEVICE" override --time "9:41" --batteryState charged --batteryLevel 100 --wifiBars 3 --cellularBars 4
xcrun simctl install "$DEVICE" "$APP"
xcrun simctl launch "$DEVICE" "$APP_ID" >/dev/null
sleep 20   # first launch on a fresh simulator is slow
xcrun simctl terminate "$DEVICE" "$APP_ID" || true

running() { xcrun simctl spawn "$DEVICE" launchctl list | grep -q "UIKitApplication:$APP_ID"; }

# A for loop, not `while read`: adb and simctl would swallow the list from stdin.
for scene in $(cat .github/scripts/scenes.txt); do
  # Status bar text follows the system appearance, so match it to the scene.
  case "$scene" in *dark*|*interview*) xcrun simctl ui "$DEVICE" appearance dark ;; *) xcrun simctl ui "$DEVICE" appearance light ;; esac
  xcrun simctl terminate "$DEVICE" "$APP_ID" 2>/dev/null || true
  sleep 1
  xcrun simctl launch "$DEVICE" "$APP_ID" -scene "$scene" >/dev/null
  sleep 5
  if ! running; then
    echo "::error::HireReel is not running for $scene; refusing to save it"
    exit 1
  fi
  xcrun simctl io "$DEVICE" screenshot "$OUT/$scene.png" >/dev/null
  echo "Saved $OUT/$scene.png"
done
ls -la "$OUT"
