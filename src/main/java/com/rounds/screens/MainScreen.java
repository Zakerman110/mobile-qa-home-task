package com.rounds.screens;

import com.rounds.helpers.ScrollUtil;
import com.rounds.helpers.ScrollUtil.Direction;
import io.appium.java_client.android.AndroidDriver;
import org.openqa.selenium.By;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

import java.util.Objects;
import java.util.function.BooleanSupplier;

import static com.rounds.helpers.ScreenLocators.byAppId;

public final class MainScreen extends BaseScreen {
    private static final By TITLE_BAR = byAppId("action_bar");
    private static final By SPLASH_TITLE = byAppId("splash_app_name");
    private static final By EARTH_LABEL = byAppId("my_adapter_item_tv");
    private static final By DOWNLOAD = byAppId("my_adapter_item_btn");
    private static final By EARTH_LIST = byAppId("list_rv");
    private static final By TITLE_TEXT = By.className("android.widget.TextView");
    private static final By EARTH_ROW = By.xpath("//android.widget.TextView[starts-with(@text, 'Earth ')]/..");

    public MainScreen(AndroidDriver driver) {
        super(driver);
    }
    public void awaitReady() {
        wait.until("Main screen must show Interview Project and an Earth row with an enabled DOWNLOAD button",
            this::isReady);
    }

    private boolean isReady(WebDriver driver) {
        return driver.findElements(SPLASH_TITLE).stream().noneMatch(WebElement::isDisplayed)
            && driver.findElements(TITLE_BAR).stream()
            .flatMap(bar -> bar.findElements(TITLE_TEXT).stream())
            .anyMatch(title -> title.isDisplayed() && "Interview Project".equals(title.getText()))
            && driver.findElements(EARTH_ROW).stream().anyMatch(row ->
                row.findElements(EARTH_LABEL).stream().anyMatch(WebElement::isDisplayed)
                    && downloadButton(row) != null);
    }

    public void openPaywallForItem(String name) {
        awaitReady();
        BooleanSupplier itemReady = () -> findDownloadButton(name) != null;
        if (!ScrollUtil.scrollUntil(driver, EARTH_LIST, Direction.DOWN, itemReady)
            && !ScrollUtil.scrollUntil(driver, EARTH_LIST, Direction.UP, itemReady)) {
            throw new NoSuchElementException("Could not reach enabled DOWNLOAD in row " + name);
        }
        WebElement button = wait.until("Expected enabled DOWNLOAD in row " + name,
            ignored -> findDownloadButton(name));
        button.click();
    }

    private WebElement findDownloadButton(String name) {
        return driver.findElements(EARTH_ROW).stream()
            .filter(row -> row.findElements(EARTH_LABEL).stream()
                .anyMatch(label -> label.isDisplayed() && name.equals(label.getText())))
            .map(this::downloadButton)
            .filter(Objects::nonNull)
            .findFirst().orElse(null);
    }

    private WebElement downloadButton(WebElement row) {
        return row.findElements(DOWNLOAD).stream()
            .filter(button -> button.isDisplayed() && button.isEnabled() && "DOWNLOAD".equals(button.getText()))
            .findFirst().orElse(null);
    }
}
