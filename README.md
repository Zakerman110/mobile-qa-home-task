# Rounds — Android UI automation

Java 17 + Appium 3.9.1 / UiAutomator2 8.6.0 + TestNG + Gradle.

Appium lets the suite drive the supplied APK through the Android UI without access to app source. The test plan was committed first and describes the coverage priorities.

## Prerequisites

- JDK 17 (`java -version`), Node.js 22.12+ in the 22.x line, npm 10+, Android SDK (`adb`), and a running Google Play image emulator (Android 15 API 35). The Gradle Wrapper installs Gradle 8.10.
- Appium installed with `npm install -g appium@3.9.1` and `appium driver install uiautomator2@8.6.0`.
- Emulator `Rounds_API_35` running and visible as `emulator-5554` in `adb devices`.
- Put the supplied APK at `app/app-debug.apk` or configure a local path or an HTTP(S) download URL reachable by Appium.

## Run locally

Start Appium in a **separate PowerShell**: `appium`

Then in project root run **one command**:

```powershell
.\gradlew.bat test
```

To specify another device or APK location:

```powershell
.\gradlew.bat "-DUDID=emulator-5554" "-DAPK_PATH=F:\path\to\app-debug.apk" test
```

The default run includes both smoke and toast tests. To run one group, use `.\gradlew.bat test "-PtestGroup=smoke"` or `.\gradlew.bat test "-PtestGroup=toast"`.

Find HTML test results in `build/reports/tests/test/index.html` and failure artifacts in `artifacts/<test-name>-<unique-id>/`. Parameterized billing tests include the action name in their artifact directory.

## Configuration

Settings use a nonblank uppercase environment variable first, then the uppercase `-D` property, then the lowercase key in `src/main/resources/config.properties`. For example, use the `APK_PATH` environment variable or `-DAPK_PATH` to override `apk_path`.

```powershell
.\gradlew.bat "-DAPK_PATH=https://example.com/path/app-debug.apk" test
```

## Coverage and architecture

- `src/main/java/com/rounds/screens` implements screen interactions and explicit waits.
- `src/main/java/com/rounds/core` handles configuration, Appium sessions, ADB logcat, screenshots, and crash/ANR checks.
- `src/testFixtures/java` contains the shared TestNG setup and teardown.
- `src/test/java` contains the test scenarios and their data providers.
- No fixed sleeps in test code. The fast splash transition is validated indirectly by successful main readiness.
- Smoke checks that all three billing actions close the paywall and return to the main screen. The toast group checks their exact no-op messages separately.
- Tests open the paywall from the `Earth 1` and `Earth 11` rows in the assignment APK, scrolling as needed.

## Diagnostics and crash policy

- Before every test, clear device logcat; after the test collect `main,system,crash` buffers. Fail on matching `FATAL EXCEPTION` or `ANR in <package>` and an unexpected app-not-foreground state. Save `logcat.txt` and `failure.txt` on failure. When a session is available, also attempt `screenshot.png` and `ui-hierarchy.xml`.
- The configured `app_package` identifies the assignment app before the session starts. Override it with `APP_PACKAGE` when using an APK with a different package.
- Detection is best-effort, log-based; log lines can be delayed or incomplete. ANR coverage benefits from an additional app responsiveness probe and `dumpsys activity` in a follow-up.
- Do not run tests in parallel against one emulator: logcat isolation assumes sequential execution.

## CI

`.github/workflows/android.yml` downloads the APK from the HTTPS URL in the `APK_PATH` repository secret, runs the smoke group on an emulator, and uploads reports and diagnostics. The Android job fails if the APK is missing. For pull requests from forks, the Android job is skipped and the workflow summary explains why.

The suite has run locally on the Google Play emulator. The GitHub-hosted run is still unverified and needs an accessible APK URL in `APK_PATH`.

## Optional AI in the workflow

`python scripts/ai_failure_summary.py artifacts` matches known failure patterns locally and sends only predefined diagnostic labels to OpenAI's Responses API when `OPENAI_API_KEY` is set. Raw log lines, stack traces, screenshots, and UI hierarchy are excluded from the request. When the API returns a summary, the script saves it as `ai-summary.txt` for the failed case. TestNG determines the test outcome. A repository secret with the same name enables this step in CI.

## Scaling to 20 apps / devices

Use a per-app module (APK, locators, flows, app package, expected paywall state) backed by shared driver/diagnostics modules. Run an API/OS/device matrix on parallel isolated runners, each with a unique ADB serial and artifact namespace. Add test tagging/smoke tiers, versioned test data and flaky-test observability before growing test counts.

## Deferred / exploratory

Screen layout on small devices, font scaling, rotation, Back/outside tap, network changes, and signed-in Play pricing. Signed-in device validation should inspect product offerings and error handling if applicable, without completing a real purchase.

