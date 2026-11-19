# Rounds Mobile QA Automation — Test Plan

## 1. Goal and scope

Validate that the Android app launches reliably, presents its main content, and handles the **Download → Premium paywall** flow without crashes, hangs, or unexpected navigation. Prioritize observable user behavior over test count. The assignment is timeboxed to 7 hours.

**Observed during exploration:** The main screen is titled `Interview Project` and lists `Earth` items with `DOWNLOAD` buttons (11 items visible on the tested emulator). Tapping Download opens a `Premium` dialog with `SUBSCRIBE`, `PURCHASE`, `RESTORE PURCHASES`, and `NOT NOW`. The dialog states that billing and downloads are disabled. Each of the first three actions closes the dialog and displays a short no-op message; `NOT NOW` simply dismisses it. The exact message text has only been captured for `PURCHASE`: `Test paywall: purchase (no-op)`.

## 2. Risk-based priorities

| Priority | Risk / failure mode | User impact | Coverage decision |
| --- | --- | --- | --- |
| P0 | App fails to launch, crashes, or becomes unresponsive | App unusable | Automate launch and monitor crash/ANR throughout each test |
| P0 | Main content or Download action is unavailable | User cannot enter the primary flow | Automate main-screen readiness and Download → paywall |
| P0 | Paywall fails to open, is missing primary actions, or cannot be dismissed | User cannot proceed or leave the flow | Automate paywall visibility, controls, and dismissal |
| P1 | Subscribe, Purchase, or Restore behaves incorrectly in the disabled-billing build | Confusing behavior or broken navigation | Automate each action's resulting dismissal; validate short feedback where deterministic |
| P1 | Paywall cannot be reopened after dismissal | Primary interaction breaks after first use | Automate repeated open/close cycle |
| P1 | Download only works for a particular list item | Some content cannot enter the flow | Exercise representative first and last visible items |
| P2 | Layout is clipped on smaller screens or under different font sizes | Controls become inaccessible | Manual/exploratory; add device matrix later |
| P2 | System Back, rotation, interruptions, and network changes cause UI issues | Edge-case navigation or stability issues | Manual/exploratory within timebox |

## 3. Automated scenarios

1. **Launch and main readiness:** start/restart the app; assert the `Interview Project` title and at least one visible Earth item with a Download action; fail on app crash/ANR.
2. **Open paywall:** tap Download; assert `Premium`, the disabled-billing explanation, and all four actions are visible.
3. **Dismiss with Not Now:** tap `NOT NOW`; assert paywall disappears and the main screen remains usable.
4. **Disabled-billing actions:** independently open the paywall and tap `SUBSCRIBE`, `PURCHASE`, and `RESTORE PURCHASES`; assert the dialog closes and the app stays on the main screen. Where accessible reliably, verify the corresponding no-op feedback without fixed sleeps.
5. **Reopen paywall:** dismiss it, then tap Download again; assert the dialog opens normally.
6. **Representative item coverage:** open the paywall using Download for the first and last visible Earth items, without duplicating identical cases for all 11 entries.

Use explicit condition-based waits and isolated tests. Do not assume all devices display all 11 entries without scrolling; scroll only when necessary to reach the target item.

## 4. Manual and exploratory scope

- Observe the splash-to-main transition and check for flicker or blank screens; avoid a timing-sensitive assertion that requires catching a transient splash screen.
- Check system Back and outside-dialog tap behavior, and document actual behavior before asserting it as a requirement.
- Check a smaller screen, larger font/display size, and rotation for clipping, inaccessible buttons, or lost state.
- Explore offline/poor network behavior if time allows.
- On a signed-in Google Play device, examine any additional paywall states/pricing if they exist; **no real purchase is required**.
- Confirm the exact user-facing feedback for each disabled-billing action; flag inconsistent or missing feedback only if it contradicts the app's stated behavior.

## 5. Test environment and approach

- **Baseline:** Android 15 / API 35, Pixel 8 emulator, **Google Play** system image, Windows 10 locally; equivalent Play-services-capable emulator in CI.
- **Tooling:** Java 17, Appium 2 with UiAutomator2, TestNG, and Gradle. Choose Appium for portability across emulator/device configurations and alignment with existing Java mobile automation experience.
- **Structure:** screen objects for Main and Paywall; driver/session setup separate from tests; reusable waits and failure diagnostics; configuration for device and APK paths.
- **Isolation:** reset navigation to a known main-screen state before each scenario; avoid relying on the outcome of prior tests.
- **Diagnostics:** on every test failure, capture a screenshot, device logcat, and test/exception metadata. Detect application crash and ANR independently of UI assertion failures; fail the affected test.
- **CI:** GitHub Actions runs the suite on an Android emulator and uploads failure artifacts even when tests fail.
- **AI in workflow:** optionally summarize captured failure artifacts into a short diagnostic note. AI must not decide pass/fail; deterministic assertions and Android diagnostics remain the source of truth. Document limitations, data handling, and fallback when AI is unavailable.

## 6. Coverage / exit criteria

Coverage is sufficient for this timebox when:

- The launch → main → Download → paywall → dismiss → reopen journey is automated.
- Each of the four paywall buttons has a verified outcome (automated when stable; otherwise documented exploratory result with rationale).
- Representative Earth items are exercised, without assuming that identical buttons require 11 separate tests.
- No known unresolved P0 failure remains; any P1/P2 issues are documented with reproduction steps, expected/actual behavior, and severity.
- Failure screenshots and per-test logcat are generated; crash/ANR checks are present.
- A one-command local run and a GitHub Actions emulator workflow exist; any CI limitations are explicitly recorded.

If time runs out, prioritize stable critical-path checks, diagnostics, and clear documentation over increasing test count. Record unimplemented checks and why they were deferred.

## 7. Assumptions and open questions

- The test build explicitly disables billing/downloads; no-op purchase actions are expected, not defects by themselves.
- Google Play account sign-in is not assumed in CI; missing prices or a limited/error paywall state may be expected according to assignment instructions.
- No functional requirement has been supplied for preserving scroll position, Back behavior, or outside-tap dismissal; explore before asserting.
- A blank logcat file observed during initial manual collection is a tooling/collection question, **not** evidence of an app defect; verify capture before relying on it.

