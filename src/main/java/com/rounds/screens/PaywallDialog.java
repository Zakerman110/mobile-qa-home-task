package com.rounds.screens;

import io.appium.java_client.AppiumBy;
import io.appium.java_client.android.AndroidDriver;
import org.openqa.selenium.By;
import java.time.Duration;

import static com.rounds.helpers.ScreenLocators.byAppId;

public final class PaywallDialog extends BaseScreen {
    public enum Action {
        SUBSCRIBE("paywall_btn_subscribe"),
        PURCHASE("paywall_btn_purchase"),
        RESTORE_PURCHASES("paywall_btn_restore"),
        NOT_NOW("paywall_btn_not_now");

        private final By locator;

        Action(String id) {
            this.locator = byAppId(id);
        }
    }

    private static By text(String s) { return AppiumBy.androidUIAutomator("new UiSelector().text(\"" + s + "\")"); }
    private static final By PREMIUM = text("Premium");
    private static final By BILLING_EXPLANATION = text(
        "Test paywall only. Billing is disabled; buttons show a short message and do not start a download.");
    public PaywallDialog(AndroidDriver driver) {
        super(driver);
    }
    public void awaitOpen() {
        wait.untilVisible(PREMIUM);
    }

    public void awaitBillingExplanation() {
        wait.untilVisible(BILLING_EXPLANATION);
    }

    public void awaitActionsVisible() {
        for (Action action : Action.values()) {
            wait.untilVisible(action.locator);
        }
    }

    public void tap(Action action) {
        awaitOpen();
        wait.untilClickable(action.locator).click();
    }

    public void awaitToast(String expectedText) {
        By toast = By.xpath("//android.widget.Toast[@text=\"" + expectedText + "\"]");
        // The toast can expire before a separate request for its text returns.
        wait.untilPresent(toast, Duration.ofSeconds(4), Duration.ofMillis(100));
    }

    public void awaitClosed() {
        wait.untilHidden(PREMIUM);
    }
}
