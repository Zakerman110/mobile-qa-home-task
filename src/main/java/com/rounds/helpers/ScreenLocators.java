package com.rounds.helpers;

import org.openqa.selenium.By;

public final class ScreenLocators {
    private static final String APP_RESOURCE_PREFIX = "com.zipo.interviewproject:id/";

    private ScreenLocators() {}

    public static By byAppId(String id) {
        return By.id(APP_RESOURCE_PREFIX + id);
    }
}
