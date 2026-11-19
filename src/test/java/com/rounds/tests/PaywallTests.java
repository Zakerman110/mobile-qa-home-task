package com.rounds.tests;

import com.rounds.fixtures.BaseTest;
import com.rounds.screens.MainScreen;
import com.rounds.screens.PaywallDialog;
import com.rounds.screens.PaywallDialog.Action;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.util.Arrays;

public class PaywallTests extends BaseTest {
    private static final String FIRST_ITEM = "Earth 1";
    private static final String LAST_ITEM = "Earth 11";

    @Test(groups = "smoke")
    public void launchShowsMainScreen() {
        new MainScreen(driver).awaitReady();
    }

    @Test(groups = "smoke")
    public void downloadOpensPaywallWithActions() {
        new MainScreen(driver).openPaywallForItem(FIRST_ITEM);
        PaywallDialog paywall = new PaywallDialog(driver);
        paywall.awaitOpen();
        paywall.awaitBillingExplanation();
        paywall.awaitActionsVisible();
    }

    @Test(groups = "smoke")
    public void lastEarthItemOpensPaywall() {
        new MainScreen(driver).openPaywallForItem(LAST_ITEM);
        new PaywallDialog(driver).awaitOpen();
    }

    @Test(groups = "smoke")
    public void notNowDismissesPaywallAndReturnsToMain() {
        MainScreen main = new MainScreen(driver);
        PaywallDialog paywall = new PaywallDialog(driver);
        main.openPaywallForItem(FIRST_ITEM);
        paywall.tap(Action.NOT_NOW);
        paywall.awaitClosed();
        main.awaitReady();
    }

    @Test(groups = "smoke")
    public void paywallCanBeReopenedAfterDismissal() {
        MainScreen main = new MainScreen(driver);
        PaywallDialog paywall = new PaywallDialog(driver);
        main.openPaywallForItem(FIRST_ITEM);
        paywall.tap(Action.NOT_NOW);
        paywall.awaitClosed();
        main.openPaywallForItem(FIRST_ITEM);
        paywall.awaitOpen();
    }

    @DataProvider(name = "billingActions")
    public Object[][] billingActions() {
        return new Object[][] {
            {Action.SUBSCRIBE, "Test paywall: subscribe (no-op)"},
            {Action.PURCHASE, "Test paywall: purchase (no-op)"},
            {Action.RESTORE_PURCHASES, "Test paywall: restore (no-op)"}
        };
    }

    @DataProvider(name = "billingActionNames")
    public Object[][] billingActionNames() {
        return Arrays.stream(billingActions())
            .map(testCase -> new Object[] {testCase[0]})
            .toArray(Object[][]::new);
    }

    @Test(groups = "smoke", dataProvider = "billingActionNames")
    public void disabledBillingActionDismissesPaywall(Action action) {
        MainScreen main = new MainScreen(driver);
        PaywallDialog paywall = new PaywallDialog(driver);
        main.openPaywallForItem(FIRST_ITEM);
        paywall.tap(action);
        paywall.awaitClosed();
        main.awaitReady();
    }

    @Test(groups = "toast", dataProvider = "billingActions")
    public void disabledBillingActionShowsNoOpFeedback(Action action, String expectedToast) {
        PaywallDialog paywall = new PaywallDialog(driver);
        new MainScreen(driver).openPaywallForItem(FIRST_ITEM);
        paywall.tap(action);
        paywall.awaitToast(expectedToast);
    }
}
