package com.rounds.core;

import io.appium.java_client.android.AndroidDriver;
import org.openqa.selenium.OutputType;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.regex.Pattern;

public final class Diagnostics {
    private Diagnostics() {}

    public static Path save(String testName, AndroidDriver driver, String log, Throwable failure) {
        try {
            Path root = Files.createDirectories(Config.artifactRoot());
            String prefix = testName.replaceAll("[^a-zA-Z0-9._-]", "_") + "-";
            Path folder = Files.createTempDirectory(root, prefix);
            Files.writeString(folder.resolve("logcat.txt"), log, StandardCharsets.UTF_8);
            var trace = new StringWriter();
            if (failure == null) {
                trace.write("No exception provided");
            } else {
                failure.printStackTrace(new PrintWriter(trace));
            }
            Files.writeString(folder.resolve("failure.txt"), "Test: " + testName + "\n" + trace);
            if (driver != null) {
                try { Files.write(folder.resolve("screenshot.png"), driver.getScreenshotAs(OutputType.BYTES)); }
                catch (Exception screenshotError) {
                    Files.writeString(folder.resolve("screenshot-error.txt"), screenshotError.toString());
                }
                try {
                    Files.writeString(folder.resolve("ui-hierarchy.xml"), driver.getPageSource());
                } catch (Exception pageSourceError) {
                    Files.writeString(folder.resolve("ui-hierarchy-error.txt"), pageSourceError.toString());
                }
            }
            System.err.println("Failure artifacts: " + folder);
            return folder;
        } catch (Exception e) {
            System.err.println("Failed to save diagnostics: " + e);
            return null;
        }
    }

    public static String findAppCrashOrAnr(String logs, String appPackage) {
        if (appPackage == null || appPackage.isBlank()) return "";
        // Filter by app package; other Android processes can crash independently.
        String escaped = Pattern.quote(appPackage);
        if (Pattern.compile("(?m)ANR in " + escaped + "(?:\\s|$)").matcher(logs).find()) return "ANR detected for " + appPackage;
        // AndroidRuntime crash entries contain Process: <package> in the same exception block.
        if (Pattern.compile("(?s)FATAL EXCEPTION.{0,900}?Process: " + escaped + "(?:,|\\s|$)").matcher(logs).find())
            return "FATAL EXCEPTION detected for " + appPackage;
        return "";
    }
}
