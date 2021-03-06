package com.paulhroth.selenium.tests;

import static org.junit.Assert.*;

import com.paulhroth.selenium.parser.Interpreter;
import com.paulhroth.selenium.parser.SaucePlatform;
import com.paulhroth.selenium.parser.StaXParser;
import com.saucelabs.common.SauceOnDemandAuthentication;
import com.saucelabs.common.SauceOnDemandSessionIdProvider;
import com.saucelabs.junit.SauceOnDemandTestWatcher;
import com.saucelabs.junit.Parallelized;
import com.saucelabs.saucerest.SauceREST;

import java.io.FileInputStream;
import java.net.URL;
import java.util.LinkedList;
import java.util.List;
import java.util.Properties;
import java.util.Random;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.Platform;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.remote.CapabilityType;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.openqa.selenium.support.ui.ExpectedCondition;
import org.openqa.selenium.support.ui.Select;
import org.openqa.selenium.support.ui.WebDriverWait;

@RunWith(Parallelized.class)
public class TestCNN {

    // setup strings: feel free to modify these as required
    private final String sauce_user = "polarqa";
    private final String sauce_access = "d609b648-22e3-44bb-a38e-c28931df837d";
    private final String baseUrl = Interpreter.getLink("src/main/config/baseurls.properties", "CNN");

    // these are required for the test to work
    private WebDriver driver;
    private SauceREST client;
    private String jobID;
    private boolean passed;
    private String browser;
    private String os;
    private String version;

    public TestCNN(String os, String version, String browser) {
        super();
        this.os = os;
        this.version = version;
        this.browser = browser;
    }

    @Parameterized.Parameters
    public static LinkedList<String[]> browsersStrings() throws Exception {
        StaXParser parser = new StaXParser();
        List<SaucePlatform> sauceplatforms = parser
                .readConfig("src/main/config/config.xml");
        LinkedList<String[]> browsers = Interpreter.interpret(sauceplatforms);
        return browsers;
    }

    @Before
    public void setUp() throws Exception {

        DesiredCapabilities capabilities = new DesiredCapabilities();
        capabilities.setCapability(CapabilityType.BROWSER_NAME, browser);
        capabilities.setCapability(CapabilityType.VERSION, version);
        capabilities.setCapability(CapabilityType.PLATFORM, os);
        client = new SauceREST(sauce_user, sauce_access);
        this.driver = new RemoteWebDriver(new URL("http://" + sauce_user + ":"
                + sauce_access + "@ondemand.saucelabs.com:80/wd/hub"),
                capabilities);
        jobID = ((RemoteWebDriver) driver).getSessionId().toString();
        passed = false;
    }

    @Test
    public void webDriver() throws Exception {
        // loads site
        driver.get(baseUrl);
        
        
        //injects native ads
        JavascriptExecutor js = (JavascriptExecutor) driver;
        js.executeScript("var d=document;var s=d.createElement('script');s.setAttribute('type','text/javascript');s.setAttribute('charset','UTF-8');s.setAttribute('src','http://localhost:9999/bookmarklet/demo.js');d.body.appendChild(s);");
        
        //scrolls down to make ads visible in screenshot later
        js.executeScript("window.scrollBy(0,450)");
        
        //asserts that the ads are displayed on the page
        synchronized (driver) { driver.wait(10000); }
        assertEquals("There should be 2 sponsored links on the page containing the link 'sponsored.html#!/'." , 2, driver.findElements(By.cssSelector("a[href*=\"sponsored.html#!/\"]")).size());   

        // passes the test
        passed = true;
    }

    @After
    public void tearDown() throws Exception {
        if (passed) {
            client.jobPassed(jobID);
        } else {
            client.jobFailed(jobID);
        }
        driver.quit();
    }
}
