
package tests;

import java.net.URL;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.openqa.selenium.Alert;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.Keys;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.SearchContext;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.support.locators.RelativeLocator;
import org.openqa.selenium.support.ui.ExpectedCondition;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import org.testng.Assert;
import org.testng.annotations.Test;

import base.BaseTest;

public class Scenario1Test extends BaseTest {

    // Find a link using Selenium 4 Relative Locators.
    private WebElement findRelativeLink(
            WebDriver driver,
            WebElement anchor,
            String linkText) {

        List<By> relativeLocators = new ArrayList<>();

        relativeLocators.add(
                RelativeLocator.with(By.tagName("a")).above(anchor));

        relativeLocators.add(
                RelativeLocator.with(By.tagName("a")).below(anchor));

        relativeLocators.add(
                RelativeLocator.with(By.tagName("a")).toLeftOf(anchor));

        relativeLocators.add(
                RelativeLocator.with(By.tagName("a")).toRightOf(anchor));

        relativeLocators.add(
                RelativeLocator.with(By.tagName("a")).near(anchor));

        for (By locator : relativeLocators) {

            List<WebElement> elements =
                    driver.findElements(locator);

            for (WebElement element : elements) {

                if (element.getText().trim().equals(linkText)) {

                    System.out.println(
                            "Found using Selenium 4 Relative Locator: "
                            + linkText);

                    return element;
                }
            }
        }

        System.out.println(
                "Relative locator did not find the link. "
                + "Trying exact link text.");

        return driver.findElement(By.linkText(linkText));
    }

    // Common method to wait for document loading.
    private void waitForPageLoad(WebDriverWait wait) {

        wait.until(new ExpectedCondition<Boolean>() {

            @Override
            public Boolean apply(WebDriver webDriver) {

                JavascriptExecutor js =
                        (JavascriptExecutor) webDriver;

                return "complete".equals(
                        js.executeScript(
                                "return document.readyState"));
            }

            @Override
            public String toString() {
                return "Document readyState to become complete";
            }
        });
    }

    // SCENARIO 1 - PART 1: Browser Window Handling
    @Test
    public void verifyBrowserWindows() throws Exception {

        WebDriverWait wait =
                new WebDriverWait(driver, Duration.ofSeconds(20));

        // STEP 1: Open Selenium Playground.
        driver.get(
                "https://www.testmuai.com/selenium-playground/");

        waitForPageLoad(wait);

        System.out.println("Selenium Playground opened.");

        // STEP 2: Store parent window handle.
        String parentWindow = driver.getWindowHandle();

        // STEP 3: Find the anchor link.
        WebElement tablePagination =
                driver.findElement(
                        By.linkText("Table Pagination"));

        // STEP 4: Find Table Sort & Search using Relative Locators.
        WebElement tableSortSearch =
                findRelativeLink(
                        driver,
                        tablePagination,
                        "Table Sort & Search");

        String targetURL =
                tableSortSearch.getAttribute("href");

        String expectedPath =
                new URL(targetURL).getPath();

        System.out.println(
                "Target link URL: " + targetURL);

        // STEP 5: Detect remote operating system.
        JavascriptExecutor js =
                (JavascriptExecutor) driver;

        String remoteOS =
                (String) js.executeScript(
                        "return navigator.platform;");

        System.out.println(
                "Remote operating system: " + remoteOS);

        Keys modifier =
                remoteOS.toLowerCase().contains("mac")
                ? Keys.COMMAND
                : Keys.CONTROL;

        // STEP 6: Open link in a new tab.
        new Actions(driver)
                .keyDown(modifier)
                .click(tableSortSearch)
                .keyUp(modifier)
                .perform();

        // STEP 7: Wait for exactly two windows.
        wait.until(
                ExpectedConditions.numberOfWindowsToBe(2));

        Set<String> windowHandles =
                driver.getWindowHandles();

        List<String> handlesList =
                new ArrayList<>(windowHandles);

        System.out.println(
                "Window handles: " + handlesList);

        Assert.assertEquals(
                handlesList.size(),
                2,
                "Exactly two browser windows should be open");

        // STEP 8: Switch to new tab by comparing handles.
        boolean switchedToNewTab = false;

        for (String handle : handlesList) {

            if (!handle.equals(parentWindow)) {

                driver.switchTo().window(handle);
                switchedToNewTab = true;
                break;
            }
        }

        Assert.assertTrue(
                switchedToNewTab,
                "New browser tab should be available");

        // STEP 9: Verify new tab URL.
        wait.until(
                ExpectedConditions.urlContains(expectedPath));

        Assert.assertTrue(
                driver.getCurrentUrl().contains(expectedPath),
                "Table Sort & Search page should open in new tab");

        System.out.println(
                "New tab URL: " + driver.getCurrentUrl());

        // STEP 10: Close new tab.
        driver.close();

        // STEP 11: Switch back to parent window.
        driver.switchTo().window(parentWindow);

        wait.until(
                ExpectedConditions.numberOfWindowsToBe(1));

        Assert.assertEquals(
                driver.getWindowHandles().size(),
                1,
                "Only parent browser window should remain");

        System.out.println(
                "Parent URL: " + driver.getCurrentUrl());

        System.out.println(
                "Window handling completed successfully.");
    }

    // SCENARIO 1 - PART 2: JavaScript Alerts
    @Test(dependsOnMethods = "verifyBrowserWindows")
    public void verifyJavaScriptAlerts() {

        WebDriverWait wait =
                new WebDriverWait(driver, Duration.ofSeconds(20));

        // STEP 1: Open JavaScript Alert demo.
        driver.get(
                "https://www.testmuai.com/selenium-playground/javascript-alert-box-demo/");

        waitForPageLoad(wait);

        System.out.println("JavaScript Alert Demo opened.");

        // STEP 2: Locate Confirm Box button.
        WebElement confirmButton =
                wait.until(
                        ExpectedConditions.elementToBeClickable(
                                By.xpath(
                                    "(//button[normalize-space(.)='Click Me'])[2]"
                                )
                        )
                );

        confirmButton.click();

        // STEP 3: Wait for Confirm Alert.
        Alert confirmAlert =
                wait.until(
                        ExpectedConditions.alertIsPresent());

        String confirmMessage =
                confirmAlert.getText();

        System.out.println(
                "Confirm Alert Message: " + confirmMessage);

        // STEP 4: Dismiss Confirm Alert.
        confirmAlert.dismiss();

        // STEP 5: Verify Cancel result.
        WebElement confirmResult =
                wait.until(
                        ExpectedConditions.visibilityOfElementLocated(
                                By.id("confirm-demo")));

        String actualConfirmResult =
                confirmResult.getText();

        System.out.println(
                "Confirm Result: " + actualConfirmResult);

        Assert.assertTrue(
                actualConfirmResult.toLowerCase().contains("cancel"),
                "Confirm alert should display Cancel result");

        // STEP 6: Locate Prompt Box button.
        WebElement promptButton =
                wait.until(
                        ExpectedConditions.elementToBeClickable(
                                By.xpath(
                                    "(//button[normalize-space(.)='Click Me'])[3]"
                                )
                        )
                );

        promptButton.click();

        // STEP 7: Wait for Prompt Alert.
        Alert promptAlert =
                wait.until(
                        ExpectedConditions.alertIsPresent());

        String promptMessage =
                promptAlert.getText();

        System.out.println(
                "Prompt Alert Message: " + promptMessage);

        // STEP 8: Enter name.
        String enteredName = "Khushi";

        promptAlert.sendKeys(enteredName);

        // STEP 9: Accept Prompt Alert.
        promptAlert.accept();

        // STEP 10: Verify entered name.
        WebElement promptResult =
                wait.until(
                        ExpectedConditions.visibilityOfElementLocated(
                                By.id("prompt-demo")));

        String actualPromptResult =
                promptResult.getText();

        System.out.println(
                "Prompt Result: " + actualPromptResult);

        Assert.assertTrue(
                actualPromptResult.contains(enteredName),
                "Prompt should display the entered name");

        System.out.println(
                "JavaScript Alerts handled successfully.");
    }

    // SCENARIO 1 - PART 3: Iframe Demo
    @Test(dependsOnMethods = "verifyJavaScriptAlerts")
    public void verifyIframeDemo() {

        WebDriverWait wait =
                new WebDriverWait(driver, Duration.ofSeconds(25));

        // STEP 1: Open Iframe Demo.
        driver.get(
                "https://www.testmuai.com/selenium-playground/iframe-demo/");

        waitForPageLoad(wait);

        System.out.println("Iframe Demo opened.");

        // STEP 2: Switch to iframe.
        wait.until(
                ExpectedConditions.frameToBeAvailableAndSwitchToIt(
                        By.id("iFrame1")));

        System.out.println(
                "Successfully switched to iframe.");

        // STEP 3: Locate editable area inside iframe.
        WebElement editor =
                wait.until(
                        ExpectedConditions.visibilityOfElementLocated(
                                By.cssSelector("[contenteditable='true']")));

        // STEP 4: Enter text inside iframe.
        String iframeText =
                "Testing iframe using Selenium WebDriver";

        editor.click();
        editor.sendKeys(Keys.COMMAND, "a");
        editor.sendKeys(iframeText);

        System.out.println(
                "Text entered inside iframe: " + editor.getText());

        // STEP 5: Verify text inside iframe.
        String actualIframeText =
                editor.getText();

        Assert.assertTrue(
                actualIframeText.contains(iframeText),
                "Text should be entered inside iframe");

        // STEP 6: Return to main page.
        driver.switchTo().defaultContent();

        System.out.println(
                "Switched back to main page.");

        // STEP 7: Verify an element outside iframe.
        WebElement mainHeading =
                wait.until(
                        ExpectedConditions.visibilityOfElementLocated(
                                By.xpath(
                                    "//h1[contains(normalize-space(.),'Simple iframe')]"
                                )
                        )
                );

        Assert.assertTrue(
                mainHeading.isDisplayed(),
                "Main page heading should be accessible outside iframe");

        System.out.println(
                "Main page heading: " + mainHeading.getText());

        System.out.println(
                "Iframe handling completed successfully.");
    }

    @Test(dependsOnMethods = "verifyIframeDemo")
    public void verifyShadowDOM() throws Exception {

        WebDriverWait wait =
                new WebDriverWait(driver, Duration.ofSeconds(25));

        driver.get(
                "https://www.testmuai.com/selenium-playground/shadow-dom/");

        waitForPageLoad(wait);

        System.out.println("Shadow DOM Demo opened.");

        WebElement shadowHost =
                wait.until(
                        ExpectedConditions.visibilityOfElementLocated(
                                By.cssSelector("shadow-signup-form")));

        SearchContext shadowRoot =
                shadowHost.getShadowRoot();

        System.out.println(
                "Successfully accessed Shadow Root.");

        WebElement usernameField =
                shadowRoot.findElement(
                        By.cssSelector("input[name='username']"));

        WebElement emailField =
                shadowRoot.findElement(
                        By.cssSelector("input[name='email']"));

        WebElement passwordField =
                shadowRoot.findElement(
                        By.cssSelector("input[name='password']"));

        WebElement confirmPasswordField =
                shadowRoot.findElement(
                        By.cssSelector("input[name='confirm_password']"));

        String username = "Khushi";
        String email = "khushi@example.com";
        String password = "Test@12345";
        String confirmPassword = "Test@12345";

        usernameField.sendKeys(username);
        emailField.sendKeys(email);
        passwordField.sendKeys(password);
        confirmPasswordField.sendKeys(confirmPassword);

        System.out.println(
                "Data entered into Shadow DOM fields.");

        String actualUsername =
                usernameField.getDomProperty("value");

        String actualEmail =
                emailField.getDomProperty("value");

        String actualPassword =
                passwordField.getDomProperty("value");

        String actualConfirmPassword =
                confirmPasswordField.getDomProperty("value");

        Assert.assertEquals(
                actualUsername,
                username,
                "Username value does not match");

        Assert.assertEquals(
                actualEmail,
                email,
                "Email value does not match");

        Assert.assertEquals(
                actualPassword,
                password,
                "Password value does not match");

        Assert.assertEquals(
                actualConfirmPassword,
                confirmPassword,
                "Confirm password value does not match");

        System.out.println(
                "Username verified: " + actualUsername);

        System.out.println(
                "Email verified: " + actualEmail);

        System.out.println(
                "Password field value verified.");

        System.out.println(
                "Confirm password field value verified.");

        File screenshot =
                shadowHost.getScreenshotAs(OutputType.FILE);

        Path screenshotFolder =
                Paths.get(
                        System.getProperty("user.dir"),
                        "screenshots");

        Files.createDirectories(screenshotFolder);

        Path screenshotPath =
                screenshotFolder.resolve("shadow-dom.png");

        Files.copy(
                screenshot.toPath(),
                screenshotPath,
                java.nio.file.StandardCopyOption.REPLACE_EXISTING);

        System.out.println(
                "Shadow DOM screenshot saved at: "
                + screenshotPath.toAbsolutePath());

        System.out.println(
                "Shadow DOM handling completed successfully.");
    }
}
