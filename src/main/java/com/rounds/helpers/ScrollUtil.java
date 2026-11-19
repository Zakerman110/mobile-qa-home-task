package com.rounds.helpers;

import io.appium.java_client.android.AndroidDriver;
import io.appium.java_client.remote.AppiumWebElement;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

import java.util.Locale;
import java.util.Map;
import java.util.function.BooleanSupplier;

public final class ScrollUtil {
    public enum Direction {
        UP,
        DOWN
    }

    private static final int MAX_SCROLLS = 20;

    private ScrollUtil() {}

    public static boolean scrollUntil(
        AndroidDriver driver, By container, Direction direction, BooleanSupplier targetReached
    ) {
        for (int scrolls = 0; scrolls < MAX_SCROLLS; scrolls++) {
            if (targetReached.getAsBoolean()) {
                return true;
            }
            if (!scroll(driver, driver.findElement(container), direction)) {
                break;
            }
        }
        return targetReached.getAsBoolean();
    }

    private static boolean scroll(AndroidDriver driver, WebElement container, Direction direction) {
        var element = (AppiumWebElement) container;
        return Boolean.TRUE.equals(driver.executeScript("mobile: scrollGesture", Map.of(
            "elementId", element.getId(),
            "direction", direction.name().toLowerCase(Locale.ROOT),
            "percent", 0.75
        )));
    }
}
