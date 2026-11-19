#!/usr/bin/env bash
set -euo pipefail

appium --address 127.0.0.1 --log appium-server.log &
appium_pid=$!
trap 'kill "$appium_pid" 2>/dev/null || true' EXIT

ready_deadline=$((SECONDS + 90))
until curl --fail --silent --max-time 2 http://127.0.0.1:4723/status |
    jq --exit-status '.value.ready == true' >/dev/null; do
    if ! kill -0 "$appium_pid" 2>/dev/null; then
        echo "::error::Appium exited during startup. See appium-server.log."
        exit 1
    fi
    if (( SECONDS >= ready_deadline )); then
        echo "::error::Appium did not report readiness within 90 seconds. See appium-server.log."
        exit 1
    fi
    sleep 2
done

bash gradlew test -PtestGroup=smoke --no-daemon
