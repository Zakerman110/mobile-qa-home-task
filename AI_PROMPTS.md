# AI prompts used

## Failed test run analysis

The optional failure-triage step matches failure patterns locally and appends predefined diagnostic labels to this prompt:

> Summarize what these Android UI test signals suggest in <=100 words. State uncertainty and suggest one diagnostic next step. Logcat may contain events from other apps. Do not decide pass/fail.

TestNG and the device checks determine the test outcome. The summary gives the reviewer a starting point for investigating the failure.

## Review prompts

- Review the Appium locators on the main screen. Which resource IDs can keep the checks stable, and when is a selector scoped to one row useful?
- Review APK configuration for local file paths and download URLs. Can both use the same configuration key and override order?
