"""Optional AI triage: never decides whether a test passes.
Only predefined diagnostic labels are submitted.
Requires OPENAI_API_KEY and outbound access. Failure artifacts remain local otherwise.
"""
import json
import os
from pathlib import Path
import re
import sys
from urllib.request import Request, urlopen

LOGCAT_SIGNALS = {
    "Android runtime logged a fatal exception": r"\bAndroidRuntime\b[^\n]*\bFATAL EXCEPTION\b",
    "Logcat contains an ANR": r"\bANR in\b",
}
FAILURE_SIGNALS = {
    "Log collection failed": r"\bLOGCAT COLLECTION FAILED\b|\bADB (?:execution failed|failed|timeout|interrupted)\b",
    "Test diagnostics detected an app crash": r"\bFATAL EXCEPTION detected for\b",
    "Test diagnostics detected an app ANR": r"\bANR detected for\b",
    "App was not foreground at test end": r"\bApp not foreground at test end\b",
    "App state could not be queried": r"\bUnable to query app state\b",
    "An Appium session could not be created": r"\bSessionNotCreatedException\b",
    "An element wait or command timed out": r"\bTimeoutException\b",
    "An element could not be found": r"\bNoSuchElementException\b",
    "An element became stale": r"\bStaleElementReferenceException\b",
    "A test assertion failed": r"\bAssertionError\b",
}


def matching_signals(content, patterns):
    return [label for label, pattern in patterns.items() if re.search(pattern, content)]

def main():
    root = Path(sys.argv[1] if len(sys.argv) > 1 else "artifacts")
    key = os.getenv("OPENAI_API_KEY")
    if not key:
        print("Skipping optional AI summary: OPENAI_API_KEY not set")
        return
    for path in sorted(root.glob("*/logcat.txt")):
        summary = path.parent / "ai-summary.txt"
        if summary.is_file():
            continue
        try:
            logs = path.read_text(encoding="utf-8", errors="replace")
            signals = matching_signals(logs, LOGCAT_SIGNALS)
            failure = path.parent / "failure.txt"
            if failure.is_file():
                report = failure.read_text(encoding="utf-8", errors="replace")
                signals.extend(matching_signals(report, FAILURE_SIGNALS))
            if not signals:
                print(f"No recognized diagnostic signals for {path.parent}")
                continue
            prompt = ("Summarize what these Android UI test signals suggest in <=100 words. "
                      "State uncertainty and suggest one diagnostic next step. "
                      "Logcat may contain events from other apps. Do not decide pass/fail.\n"
                      + "\n".join(signals))
            body = {"model": os.getenv("AI_MODEL", "gpt-4.1-mini"), "input": prompt, "max_output_tokens": 180}
            request = Request(
                "https://api.openai.com/v1/responses",
                data=json.dumps(body).encode(),
                headers={"Authorization": "Bearer " + key, "Content-Type": "application/json"},
            )
            with urlopen(request, timeout=30) as response:
                data = json.load(response)
            text = "\n".join(part.get("text", "") for item in data.get("output", []) for part in item.get("content", []) if part.get("type") == "output_text")
            if text.strip():
                summary.write_text(text, encoding="utf-8")
                print(f"AI summary: {summary}")
            else:
                print(f"AI returned no summary for {path.parent}")
        except Exception as exc:
            print(f"AI unavailable for {path.parent}: {exc}")

if __name__ == "__main__":
    main()
