package com.rounds.fixtures;

import com.rounds.core.AppSession;
import com.rounds.core.Config;
import com.rounds.core.DeviceLogs;
import com.rounds.core.Diagnostics;
import io.appium.java_client.android.AndroidDriver;
import io.appium.java_client.appmanagement.ApplicationState;
import io.qameta.allure.Allure;
import org.testng.IHookCallBack;
import org.testng.IHookable;
import org.testng.ITestResult;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;

import java.lang.reflect.InvocationTargetException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.stream.Collectors;

public abstract class BaseTest implements IHookable {
    protected AndroidDriver driver;
    private String appPackage;
    private Exception setupFailure;

    @BeforeMethod(alwaysRun = true)
    public void setUp() throws Exception {
        try {
            appPackage = Config.packageName();
            DeviceLogs.clear();
            driver = AppSession.start();
            if (appPackage.isBlank()) {
                appPackage = driver.getCurrentPackage();
            }
        } catch (Exception e) {
            setupFailure = e;
            throw e;
        }
    }

    @Override
    public final void run(IHookCallBack callback, ITestResult result) {
        callback.runTestMethod(result);
        Throwable original = result.getThrowable();
        if (original instanceof InvocationTargetException invocation && invocation.getCause() != null) {
            original = invocation.getCause();
        }
        Throwable failure = checkDeviceAndSave(result, original);
        if (original == null && failure != null) {
            throw new AssertionError("Device diagnostics failed: " + failure.getMessage(), failure);
        }
    }

    private Throwable checkDeviceAndSave(ITestResult result, Throwable failure) {
        String logs;
        try {
            logs = DeviceLogs.dump();
        } catch (Exception e) {
            logs = "LOGCAT COLLECTION FAILED: " + e;
            failure = addFailure(failure, e);
        }

        String deviceIssue = Diagnostics.findAppCrashOrAnr(logs, appPackage);
        if (!deviceIssue.isBlank()) {
            failure = addFailure(failure, new AssertionError(deviceIssue));
        }
        if (driver != null && appPackage != null && !appPackage.isBlank()) {
            try {
                ApplicationState state = driver.queryAppState(appPackage);
                if (state != ApplicationState.RUNNING_IN_FOREGROUND) {
                    failure = addFailure(failure, new AssertionError("App not foreground at test end: " + state));
                }
            } catch (Exception e) {
                failure = addFailure(failure, new IllegalStateException("Unable to query app state", e));
            }
        }
        if (failure != null) {
            String action = Arrays.stream(result.getParameters())
                .filter(Enum.class::isInstance)
                .map(parameter -> ((Enum<?>) parameter).name())
                .collect(Collectors.joining("-"));
            String testName = result.getName() + (action.isEmpty() ? "" : "-" + action);
            Path folder = Diagnostics.save(testName, driver, logs, failure);
            if (folder != null && Allure.getLifecycle().getCurrentTestCase().isPresent()) {
                attachToAllure(folder.resolve("screenshot.png"), "image/png", ".png");
                attachToAllure(folder.resolve("logcat.txt"), "text/plain", ".txt");
                attachToAllure(folder.resolve("ui-hierarchy.xml"), "application/xml", ".xml");
            }
        }
        return failure;
    }

    private static void attachToAllure(Path file, String mediaType, String extension) {
        if (!Files.isRegularFile(file)) {
            return;
        }
        try (var content = Files.newInputStream(file)) {
            Allure.addAttachment(file.getFileName().toString(), mediaType, content, extension);
        } catch (Exception e) {
            System.err.println("Unable to attach " + file.getFileName() + " to Allure: " + e);
        }
    }

    @AfterMethod(alwaysRun = true)
    public void tearDown(ITestResult result) {
        try {
            if (setupFailure != null) {
                checkDeviceAndSave(result, setupFailure);
            }
        } finally {
            try {
                if (driver != null) {
                    driver.quit();
                }
            } catch (Exception e) {
                System.err.println("Appium session cleanup failed: " + e);
            } finally {
                driver = null;
                appPackage = null;
                setupFailure = null;
            }
        }
    }

    private static Throwable addFailure(Throwable original, Throwable additional) {
        if (original == null) {
            return additional;
        }
        original.addSuppressed(additional);
        return original;
    }
}
