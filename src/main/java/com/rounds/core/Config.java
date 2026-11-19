package com.rounds.core;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.net.URI;
import java.nio.file.Path;
import java.util.Locale;
import java.util.Properties;

public final class Config {
    private static final Properties DEFAULTS = loadDefaults();

    private Config() {}

    public static URI appiumUri() {
        return URI.create(value("appium_url"));
    }

    public static String deviceName() {
        return value("device_name");
    }

    public static String udid() {
        return value("udid");
    }

    public static String apkLocation() {
        return value("apk_path");
    }

    public static String packageName() {
        return value("app_package");
    }

    public static Path artifactRoot() {
        return Path.of(value("artifacts_dir")).toAbsolutePath();
    }

    private static String value(String key) {
        String uppercaseKey = key.toUpperCase(Locale.ROOT);
        String environmentValue = System.getenv(uppercaseKey);
        if (environmentValue != null && !environmentValue.isBlank()) {
            return environmentValue;
        }
        return System.getProperty(uppercaseKey, DEFAULTS.getProperty(key));
    }

    private static Properties loadDefaults() {
        try (var input = Config.class.getResourceAsStream("/config.properties")) {
            if (input == null) {
                throw new IllegalStateException("Missing config.properties on the classpath");
            }
            var properties = new Properties();
            properties.load(input);
            return properties;
        } catch (IOException e) {
            throw new UncheckedIOException("Could not read config.properties", e);
        }
    }
}
