
package tests;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Duration;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.apache.hc.client5.http.classic.methods.HttpGet;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClients;

import org.openqa.selenium.By;
import org.openqa.selenium.Dimension;
import org.openqa.selenium.StaleElementReferenceException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.remote.LocalFileDetector;
import org.openqa.selenium.remote.RemoteWebDriver;

import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.FluentWait;
import org.openqa.selenium.support.ui.WebDriverWait;

import org.testng.Assert;
import org.testng.asserts.SoftAssert;
import org.testng.annotations.Test;

import com.fasterxml.jackson.databind.ObjectMapper;

import base.BaseTest;

public class Scenario2Test extends BaseTest {

    // Common explicit wait.
    private WebDriverWait getWait() {

        return new WebDriverWait(
                driver,
                Duration.ofSeconds(30));
    }

    // =====================================================
    // PART 1: DRAG AND DROP SLIDERS
    // =====================================================

    @Test
    public void verifyDragAndDropSlider() {

        WebDriverWait wait = getWait();

        // STEP 1: Open Slider Demo.
        driver.get(
                "https://www.testmuai.com/selenium-playground/drag-drop-range-sliders-demo/");

        wait.until(
                ExpectedConditions.visibilityOfElementLocated(
                        By.cssSelector("input[type='range']")));

        System.out.println("Slider Demo opened.");

        // STEP 2: Find all range sliders.
        List<WebElement> sliders =
                driver.findElements(
                        By.cssSelector("input[type='range']"));

        Assert.assertTrue(
                sliders.size() > 0,
                "At least one slider should be available");

        // STEP 3: Find slider with default value 50.
        WebElement targetSlider = null;

        for (WebElement slider : sliders) {

            String value =
                    slider.getDomProperty("value");

            if ("50".equals(value)) {

                targetSlider = slider;
                break;
            }
        }

        Assert.assertNotNull(
                targetSlider,
                "Slider with default value 50 should be available");

        // Make a final reference for use inside lambda.
        final WebElement sliderToVerify = targetSlider;

        // STEP 4: Read slider minimum, maximum and current value.
        double minimum = Double.parseDouble(
                sliderToVerify.getAttribute("min"));

        double maximum = Double.parseDouble(
                sliderToVerify.getAttribute("max"));

        double currentValue = Double.parseDouble(
                sliderToVerify.getDomProperty("value"));

        double targetValue = 95;

        Assert.assertTrue(
                targetValue >= minimum && targetValue <= maximum,
                "Target value should be within slider range");

        // STEP 5: Calculate movement using runtime dimensions.
        Dimension sliderSize =
                sliderToVerify.getSize();

        int sliderWidth =
                sliderSize.getWidth();

        int currentOffset = (int) Math.round(
                ((currentValue - minimum) / (maximum - minimum))
                * sliderWidth);

        int targetOffset = (int) Math.round(
                ((targetValue - minimum) / (maximum - minimum))
                * sliderWidth);

        int movement =
                targetOffset - currentOffset;

        System.out.println(
                "Slider width: " + sliderWidth);

        System.out.println(
                "Current value: " + currentValue);

        System.out.println(
                "Calculated movement: " + movement);

        // STEP 6: Drag slider using Actions.
        new Actions(driver)
                .clickAndHold(sliderToVerify)
                .moveByOffset(movement, 0)
                .release()
                .perform();

        // STEP 7: Wait for slider value to become 95.
        wait.until(webDriver ->
                "95".equals(
                        sliderToVerify.getDomProperty("value")));

        String actualValue =
                sliderToVerify.getDomProperty("value");

        Assert.assertEquals(
                actualValue,
                "95",
                "Slider should move to 95");

        System.out.println(
                "Slider successfully moved to: " + actualValue);
    }

    // =====================================================
    // PART 2: PROGRESS BAR
    // =====================================================

    private boolean isDownloadComplete(WebDriver webDriver) {

        String bodyText =
                webDriver.findElement(By.tagName("body"))
                        .getText()
                        .toLowerCase();

        boolean percentageComplete =
                bodyText.contains("100%");

        List<WebElement> progressElements =
                webDriver.findElements(
                        By.cssSelector(
                                "[role='progressbar'], "
                                + ".progress-bar, "
                                + ".ui-progressbar-value, "
                                + "#progressbar"));

        for (WebElement progress : progressElements) {

            String ariaValue =
                    progress.getAttribute("aria-valuenow");

            String style =
                    progress.getAttribute("style");

            if ("100".equals(ariaValue)
                    || (style != null
                    && style.replace(" ", "")
                    .contains("width:100%"))) {

                percentageComplete = true;
            }
        }

        boolean completionMessage =
                bodyText.contains("download complete")
                || bodyText.contains("download completed");

        return percentageComplete && completionMessage;
    }

    @Test(dependsOnMethods = "verifyDragAndDropSlider")
    public void verifyProgressBar() {

        // STEP 1: Open Progress Bar Demo.
        driver.get(
                "https://www.testmuai.com/selenium-playground/jquery-download-progress-bar-demo/");

        System.out.println(
                "Download Progress-Bar Demo opened.");

        WebDriverWait wait = getWait();

        // STEP 2: Locate Start Download button.
        WebElement startButton =
                wait.until(
                        ExpectedConditions.elementToBeClickable(
                                By.xpath(
                                        "//button[contains(translate(normalize-space(.),"
                                        + "'ABCDEFGHIJKLMNOPQRSTUVWXYZ',"
                                        + "'abcdefghijklmnopqrstuvwxyz'),"
                                        + "'start download')]")));

        // STEP 3: Start download.
        startButton.click();

        System.out.println("Download started.");

        // STEP 4: Create FluentWait.
        FluentWait<WebDriver> fluentWait =
                new FluentWait<>(driver)
                        .withTimeout(Duration.ofSeconds(120))
                        .pollingEvery(Duration.ofMillis(500))
                        .ignoring(
                                StaleElementReferenceException.class);

        // STEP 5: Wait until progress reaches 100%.
        boolean completed =
                fluentWait.until(webDriver ->
                        isDownloadComplete(webDriver));

        // STEP 6: Verify completion.
        Assert.assertTrue(
                completed,
                "Download should reach 100% and show completion message");

        String finalPageText =
                driver.findElement(By.tagName("body")).getText();

        System.out.println(
                "Progress bar completion details: "
                + finalPageText);

        System.out.println(
                "Download completed successfully.");
    }

    // =====================================================
    // PART 3: TABLE SORTING
    // =====================================================

    @Test(dependsOnMethods = "verifyProgressBar")
    public void verifyTableSorting() {

        WebDriverWait wait = getWait();

        // STEP 1: Open Table Sort & Search Demo.
        driver.get(
                "https://www.testmuai.com/selenium-playground/table-sort-search-demo/");

        // STEP 2: Wait for table.
        WebElement table =
                wait.until(
                        ExpectedConditions.visibilityOfElementLocated(
                                By.id("example")));

        System.out.println(
                "Table Sorting and Searching Demo opened.");

        // STEP 3: Locate Age column header.
        WebElement ageHeader =
                table.findElement(
                        By.xpath(
                                ".//thead//th[normalize-space()='Age']"));

        // STEP 4: Click Age header.
        ageHeader.click();

        // STEP 5: Wait for visible rows.
        wait.until(webDriver -> {

            List<WebElement> rows =
                    webDriver.findElements(
                            By.cssSelector("#example tbody tr"));

            return !rows.isEmpty();
        });

        // STEP 6: Read visible rows.
        List<WebElement> rows =
                driver.findElements(
                        By.cssSelector("#example tbody tr"));

        List<Integer> ages =
                new ArrayList<>();

        for (WebElement row : rows) {

            List<WebElement> cells =
                    row.findElements(By.tagName("td"));

            if (cells.size() >= 4
                    && !cells.get(3).getText().trim().isEmpty()) {

                int age = Integer.parseInt(
                        cells.get(3).getText().trim());

                ages.add(age);

                System.out.println(
                        "Table row: " + row.getText());
            }
        }

        // STEP 7: Verify rows are available.
        Assert.assertTrue(
                ages.size() > 0,
                "Visible table rows should be available");

        // STEP 8: Verify ascending order.
        for (int i = 0; i < ages.size() - 1; i++) {

            Assert.assertTrue(
                    ages.get(i) <= ages.get(i + 1),
                    "Age column is not sorted in ascending order: "
                    + ages);
        }

        System.out.println(
                "Visible ages in sorted order: " + ages);

        System.out.println(
                "Table sorting verified successfully.");
    }

    // =====================================================
    // PART 4: FILE UPLOAD
    // =====================================================

    @Test(dependsOnMethods = "verifyTableSorting")
    public void verifyFileUpload() throws IOException {

        WebDriverWait wait = getWait();

        // STEP 1: Create test-data folder.
        Path uploadFolder =
                Paths.get(
                        System.getProperty("user.dir"),
                        "test-data");

        Files.createDirectories(uploadFolder);

        // STEP 2: Create test file.
        Path uploadFile =
                uploadFolder.resolve("upload-test.txt");

        Files.writeString(
                uploadFile,
                "This is a Selenium remote file upload test.");

        System.out.println(
                "Local test file created: "
                + uploadFile.toAbsolutePath());

        // STEP 3: Configure LocalFileDetector.
        RemoteWebDriver remoteDriver =
                (RemoteWebDriver) driver;

        remoteDriver.setFileDetector(
                new LocalFileDetector());

        // STEP 4: Open Upload File Demo.
        driver.get(
                "https://www.testmuai.com/selenium-playground/upload-file-demo/");

        System.out.println(
                "Upload File Demo opened.");

        // STEP 5: Locate file input.
        WebElement fileInput =
                wait.until(
                        ExpectedConditions.presenceOfElementLocated(
                                By.cssSelector("input[type='file']")));

        // STEP 6: Upload file.
        fileInput.sendKeys(
                uploadFile.toAbsolutePath().toString());

        System.out.println(
                "File sent to remote browser.");

        // STEP 7: Verify selected file name.
        wait.until(webDriver -> {

            String value =
                    fileInput.getAttribute("value");

            return value != null
                    && value.contains("upload-test.txt");
        });

        String uploadedFileName =
                fileInput.getAttribute("value");

        Assert.assertTrue(
                uploadedFileName.contains("upload-test.txt"),
                "Uploaded file name should be displayed");

        System.out.println(
                "File upload selection verified: "
                + uploadedFileName);

        System.out.println(
                "File upload test completed successfully.");
    }

    // =====================================================
    // PART 5: DYNAMIC DATA LOADING
    // =====================================================

    @Test(dependsOnMethods = "verifyFileUpload")
    public void verifyDynamicDataLoading() {

        WebDriverWait wait =
                new WebDriverWait(
                        driver,
                        Duration.ofSeconds(40));

        // STEP 1: Open Dynamic Data Loading Demo.
        driver.get(
                "https://www.testmuai.com/selenium-playground/dynamic-data-loading-demo/");

        System.out.println(
                "Dynamic Data Loading Demo opened.");

        // STEP 2: Locate Get Random User button.
        WebElement loadButton =
                wait.until(
                        ExpectedConditions.elementToBeClickable(
                                By.id("save")));

        // STEP 3: Click button.
        loadButton.click();

        System.out.println(
                "Requested random user data.");

        // STEP 4: Wait for dynamic content.
        wait.until(
                ExpectedConditions.textToBePresentInElementLocated(
                        By.id("loading"),
                        "First Name"));

        // STEP 5: Read loaded content.
        WebElement loadedData =
                wait.until(
                        ExpectedConditions.visibilityOfElementLocated(
                                By.id("loading")));

        String actualData =
                loadedData.getText();

        System.out.println(
                "Loaded dynamic data: " + actualData);

        // STEP 6: Verify content.
        Assert.assertFalse(
                actualData.trim().isEmpty(),
                "Dynamic data should not be empty");

        Assert.assertTrue(
                actualData.contains("First Name"),
                "Loaded content should contain First Name");

        System.out.println(
                "Dynamic data loaded and verified successfully.");
    }

    //=====================================================

    @Test(dependsOnMethods = "verifyDynamicDataLoading")
    public void verifyHomepageLinkAudit() throws Exception {

        WebDriverWait wait = getWait();

        // STEP 1: Open Selenium Playground homepage.
        driver.get(
                "https://www.testmuai.com/selenium-playground/");

        wait.until(
                ExpectedConditions.presenceOfAllElementsLocatedBy(
                        By.tagName("a")));

        System.out.println(
                "Selenium Playground homepage opened for link audit.");

        // STEP 2: Collect homepage demo links.
        List<WebElement> links =
                driver.findElements(By.tagName("a"));

        Map<String, String> demoLinks =
                new LinkedHashMap<>();

        for (WebElement link : links) {

            String href =
                    link.getAttribute("href");

            String linkText =
                    link.getText().trim();

            if (href != null
                    && href.contains("/selenium-playground/")
                    && !href.equals(
                            "https://www.testmuai.com/selenium-playground/")
                    && !href.contains("#")) {

                demoLinks.putIfAbsent(
                        href,
                        linkText.isEmpty() ? href : linkText);
            }
        }

        Assert.assertTrue(
                demoLinks.size() > 0,
                "Homepage demo links should be collected");

        System.out.println(
                "Total unique demo links collected: "
                + demoLinks.size());

        // STEP 3: Prepare report collection.
        List<Map<String, Object>> report =
                new ArrayList<>();

        SoftAssert softAssert =
                new SoftAssert();

        // STEP 4: Check HTTP status using Apache HttpClient.
        try (CloseableHttpClient httpClient =
                HttpClients.createDefault()) {

            for (Map.Entry<String, String> entry
                    : demoLinks.entrySet()) {

                String url = entry.getKey();
                String linkName = entry.getValue();

                int statusCode = 0;
                String errorMessage = "";

                try {

                    HttpGet request =
                            new HttpGet(url);

                    statusCode =
                            httpClient.execute(
                                    request,
                                    response -> response.getCode());

                } catch (Exception e) {

                    errorMessage = e.getMessage();

                    System.out.println(
                            "Link request error: "
                            + url + " : " + errorMessage);
                }

                // STEP 5: Store audit result.
                Map<String, Object> result =
                        new LinkedHashMap<>();

                result.put("linkName", linkName);
                result.put("url", url);
                result.put("statusCode", statusCode);

                result.put(
                        "result",
                        statusCode >= 200 && statusCode < 400
                                ? "PASS"
                                : "FAIL");

                result.put("error", errorMessage);

                report.add(result);

                System.out.println(
                        linkName + " | "
                        + url + " | HTTP "
                        + statusCode);

                // STEP 6: Soft assertion.
                softAssert.assertTrue(
                        statusCode >= 200 && statusCode < 400,
                        "Broken or unsuccessful link: "
                        + url + " HTTP status: "
                        + statusCode);
            }
        }

        // STEP 7: Create reports folder.
        Path reportFolder =
                Paths.get(
                        System.getProperty("user.dir"),
                        "reports");

        Files.createDirectories(reportFolder);

        // STEP 8: Generate JSON report.
        Path jsonFile =
                reportFolder.resolve("link-audit.json");

        ObjectMapper mapper =
                new ObjectMapper();

        mapper.writerWithDefaultPrettyPrinter()
                .writeValue(jsonFile.toFile(), report);

        System.out.println(
                "JSON report saved: "
                + jsonFile.toAbsolutePath());

        // STEP 9: Generate CSV report.
        Path csvFile =
                reportFolder.resolve("link-audit.csv");

        StringBuilder csv =
                new StringBuilder();

        csv.append(
                "Link Name,URL,Status Code,Result,Error\n");

        for (Map<String, Object> result : report) {

            csv.append(csvValue(result.get("linkName")))
                    .append(",");

            csv.append(csvValue(result.get("url")))
                    .append(",");

            csv.append(result.get("statusCode"))
                    .append(",");

            csv.append(csvValue(result.get("result")))
                    .append(",");

            csv.append(csvValue(result.get("error")))
                    .append("\n");
        }

        Files.writeString(csvFile, csv.toString());

        System.out.println(
                "CSV report saved: "
                + csvFile.toAbsolutePath());

        // STEP 10: Print audit summary.
        long passedLinks =
                report.stream()
                        .filter(r ->
                                "PASS".equals(r.get("result")))
                        .count();

        long failedLinks =
                report.size() - passedLinks;

        System.out.println(
                "Total links checked: " + report.size());

        System.out.println(
                "Passed links: " + passedLinks);

        System.out.println(
                "Failed links: " + failedLinks);

        // STEP 11: Verify link audit results.
        softAssert.assertAll();

        System.out.println(
                "Homepage link audit completed successfully.");
    }

    // CSV escaping helper.
    private String csvValue(Object value) {

        String text =
                value == null ? "" : value.toString();

        return "\""
                + text.replace("\"", "\"\"")
                + "\"";
    }
}
