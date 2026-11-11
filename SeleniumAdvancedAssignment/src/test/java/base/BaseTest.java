
package base;

import java.net.URL;
import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

import org.openqa.selenium.MutableCapabilities;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.edge.EdgeOptions;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.openqa.selenium.JavascriptExecutor;

import org.testng.ITestResult;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Parameters;
import org.testng.annotations.Optional;

public class BaseTest {

	public WebDriver driver;

	@BeforeMethod
	@Parameters({ "browser", "platform", "browserVersion", "testName" })
	public void setUp(@Optional("chrome") String browser, @Optional("Windows 11") String platform,
			@Optional("latest") String browserVersion,
			@Optional("Scenario 2 - Selenium Advanced Assignment") String testName) throws Exception {

		// STEP 1: Read TestMu credentials.
		String username = System.getenv("LT_USERNAME");
		String accessKey = System.getenv("LT_ACCESS_KEY");

		if (username == null || accessKey == null || username.isBlank() || accessKey.isBlank()) {

			throw new IllegalStateException("TestMu AI credentials are not configured. "
					+ "Please check LT_USERNAME and LT_ACCESS_KEY " + "in Eclipse Run Configurations.");
		}

		// STEP 2: Configure browser capabilities.
		MutableCapabilities capabilities;

		if (browser.equalsIgnoreCase("chrome")) {

			capabilities = new ChromeOptions();

		} else if (browser.equalsIgnoreCase("edge")) {

			capabilities = new EdgeOptions();

		} else {

			throw new IllegalArgumentException("Unsupported browser: " + browser);
		}

		// STEP 3: Set browser and platform details.
		capabilities.setCapability("browserName", browser);

		capabilities.setCapability("browserVersion", browserVersion);

		capabilities.setCapability("platformName", platform);

		// STEP 4: Configure TestMu options.
		Map<String, Object> ltOptions = new HashMap<>();

		ltOptions.put("username", username);
		ltOptions.put("accessKey", accessKey);

		ltOptions.put("build", "Selenium Advanced Assignment");

		ltOptions.put("name", testName);

		ltOptions.put("project", "Selenium Advanced");

		ltOptions.put("w3c", true);

		capabilities.setCapability("LT:Options", ltOptions);

		// STEP 5: Connect to TestMu remote grid.
		URL gridURL = new URL("https://hub.lambdatest.com/wd/hub");

		driver = new RemoteWebDriver(gridURL, capabilities);

		// STEP 6: Configure browser timeout.
		driver.manage().timeouts().pageLoadTimeout(Duration.ofSeconds(60));

		System.out.println("Remote browser started: " + browser);

		System.out.println("Platform: " + platform);

		System.out.println("Browser version: " + browserVersion);

		System.out.println("Test name: " + testName);
	}

	// =====================================================
	// AFTER METHOD: UPDATE TESTMU SESSION STATUS
	// =====================================================

	@AfterMethod(alwaysRun = true)
	public void tearDown(ITestResult result) {

		if (driver != null) {

			try {

				JavascriptExecutor js = (JavascriptExecutor) driver;

				if (result.isSuccess()) {

					js.executeScript("lambda-status=passed");

					System.out.println("TestMu session status: PASSED");

				} else {

					js.executeScript("lambda-status=failed");

					System.out.println("TestMu session status: FAILED");
				}

			} catch (Exception e) {

				System.out.println("Could not update TestMu session status: " + e.getMessage());

			} finally {

				driver.quit();

				System.out.println("Remote browser closed.");
			}
		}
	}
}
