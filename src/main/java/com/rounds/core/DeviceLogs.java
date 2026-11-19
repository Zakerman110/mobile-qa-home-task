
package com.rounds.core;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

public final class DeviceLogs {

    private DeviceLogs() {}

    public static String adb(String... args) {
        Path outputFile = null;
        Process process = null;

        try {
            List<String> command = new ArrayList<>(
                    List.of("adb", "-s", Config.udid())
            );
            command.addAll(List.of(args));

            outputFile = Files.createTempFile("rounds-adb-", ".log");

            process = new ProcessBuilder(command)
                    .redirectErrorStream(true)
                    .redirectOutput(outputFile.toFile())
                    .start();

            boolean completed = process.waitFor(30, TimeUnit.SECONDS);

            if (!completed) {
                process.destroyForcibly();
                process.waitFor(5, TimeUnit.SECONDS);
                throw new IllegalStateException("ADB timeout: " + command);
            }

            String output = Files.readString(outputFile, StandardCharsets.UTF_8);

            if (process.exitValue() != 0) {
                throw new IllegalStateException(
                        "ADB failed: " + command + "\n" + output
                );
            }

            return output;

        } catch (IOException e) {
            throw new IllegalStateException("ADB execution failed", e);
        } catch (InterruptedException e) {
            if (process != null) process.destroyForcibly();
            Thread.currentThread().interrupt();
            throw new IllegalStateException("ADB interrupted", e);
        } finally {
            if (outputFile != null) {
                try {
                    Files.deleteIfExists(outputFile);
                } catch (IOException ignored) {}
            }
        }
    }

    public static void clear() {
        adb("logcat", "-b", "main,system,crash", "-c");
    }

    public static String dump() {
        return adb(
                "logcat",
                "-d",
                "-b", "main,system,crash",
                "-v", "threadtime"
        );
    }
}
