package com.rounds.core;

import io.appium.java_client.android.AndroidDriver;
import io.appium.java_client.android.options.UiAutomator2Options;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;

public final class AppSession {
    private AppSession() {}

    public static AndroidDriver start() throws Exception {
        var options = new UiAutomator2Options()
            .setDeviceName(Config.deviceName())
            .setUdid(Config.udid())
            .setApp(resolveApk(Config.apkLocation()))
            .setAutoGrantPermissions(true)
            .setNewCommandTimeout(Duration.ofSeconds(90));
        options.setCapability("appium:appWaitActivity", "*");
        return new AndroidDriver(Config.appiumUri().toURL(), options);
    }

    private static String resolveApk(String location) {
        if (location.matches("(?is)^https?://.*")) {
            try {
                URI uri = URI.create(location);
                if (uri.getHost() != null) {
                    return uri.toASCIIString();
                }
            } catch (IllegalArgumentException ignored) {
                // Avoid including credentials or signed query parameters in this error.
            }
            throw new IllegalArgumentException("Invalid HTTP(S) APK URL");
        }

        Path apk = Path.of(location).toAbsolutePath().normalize();
        if (!Files.isRegularFile(apk)) {
            throw new IllegalStateException("APK missing: " + apk);
        }
        return apk.toString();
    }
}
