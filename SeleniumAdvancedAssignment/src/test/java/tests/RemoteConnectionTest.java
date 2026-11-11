
package tests;

import org.testng.annotations.Test;
import org.testng.annotations.Parameters;

import base.BaseTest;

public class RemoteConnectionTest extends BaseTest {

    @Test
    @Parameters({"url"})
    public void verifyRemoteBrowser(String url) {

        driver.get(url);

        System.out.println("Page title: " + driver.getTitle());
        System.out.println("Current URL: " + driver.getCurrentUrl());
    }
}
