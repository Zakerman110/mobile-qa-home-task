package com.rounds.screens;

import com.rounds.helpers.WaitUtil;
import io.appium.java_client.android.AndroidDriver;

abstract class BaseScreen {
    protected final AndroidDriver driver;
    protected final WaitUtil wait;

    BaseScreen(AndroidDriver driver) {
        this.driver = driver;
        this.wait = new WaitUtil(driver, 15);
    }
}
