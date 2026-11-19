package com.rounds.helpers;

import org.openqa.selenium.By;
import org.openqa.selenium.StaleElementReferenceException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;
import java.util.function.Function;

public final class WaitUtil {
    private final WebDriver driver;
    private final WebDriverWait wait;

    public WaitUtil(WebDriver driver, int timeoutSeconds) {
        this.driver = driver;
        this.wait = newWait(Duration.ofSeconds(timeoutSeconds), Duration.ofMillis(500));
    }

    public void untilVisible(By locator) {
        wait.until(ExpectedConditions.visibilityOfElementLocated(locator));
    }

    public void untilHidden(By locator) {
        wait.until(driver -> driver.findElements(locator).stream().noneMatch(WebElement::isDisplayed));
    }

    public void untilPresent(By locator, Duration timeout, Duration pollingInterval) {
        newWait(timeout, pollingInterval).until(ExpectedConditions.presenceOfElementLocated(locator));
    }

    public WebElement untilClickable(By locator) {
        return wait.until(ExpectedConditions.elementToBeClickable(locator));
    }

    public <T> T until(String message, Function<WebDriver, T> condition) {
        try {
            return wait.withMessage(message).until(condition);
        } finally {
            wait.withMessage(() -> null);
        }
    }

    private WebDriverWait newWait(Duration timeout, Duration pollingInterval) {
        var wait = new WebDriverWait(driver, timeout, pollingInterval);
        wait.ignoring(StaleElementReferenceException.class);
        return wait;
    }
}
