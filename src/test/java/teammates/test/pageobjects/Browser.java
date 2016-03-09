package teammates.test.pageobjects;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Stack;

import org.openqa.selenium.WebDriver;

import com.thoughtworks.selenium.webdriven.WebDriverBackedSelenium;

import org.openqa.selenium.chrome.ChromeDriverService;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.htmlunit.HtmlUnitDriver;
import org.openqa.selenium.ie.InternetExplorerDriver;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.openqa.selenium.remote.RemoteWebDriver;

import teammates.test.driver.TestProperties;

import com.gargoylesoftware.htmlunit.BrowserVersion;
import com.thoughtworks.selenium.DefaultSelenium;

/**
 * A programmatic interface to the Browser used to test the app.
 */
@SuppressWarnings("deprecation")
public class Browser {
    
    protected ChromeDriverService chromeService = null;
    
    
    /**
     * The {@link WebDriver} object that drives the Browser instance.
     */
    public WebDriver driver;

    /**
     * A wrapper around the {@code driver} that represents a slightly
     * higher-level programmatic interface to the Browser instance.
     */
    public DefaultSelenium selenium = null;
    
    /**
     * Indicated to the {@link BrowserPool} that this object is currently being
     * used and not ready to be reused by another test.
     */
    public boolean isInUse;
    
    public boolean isAdminLoggedIn;
    
    /**
     * Keeps track of multiple windows opened by the {@link WebDriver}.
     */
    private final Stack<String> windowHandles = new Stack<String>();
    
    public Browser() {
        this.driver = createWebDriver();
        this.driver.manage().window().maximize();
        this.selenium = new WebDriverBackedSelenium(this.driver, TestProperties.inst().TEAMMATES_URL);
        isInUse = false; 
        isAdminLoggedIn = false;
    }
    
    /**
     * Switches to new browser window for browsing.
     */
    public void switchToNewWindow() {
        String curWin = driver.getWindowHandle();
        for (String handle : driver.getWindowHandles()) {
            if (!handle.equals(curWin) && !windowHandles.contains(curWin)) {
                windowHandles.push(curWin);
                driver.switchTo().window(handle);
                break;
            }
        }
    }
    
    /**
     * Closes the current browser window and switches back to the last window
     * used previously.
     */
    public void closeCurrentWindowAndSwitchToParentWindow() {
        driver.close();
        driver.switchTo().window(windowHandles.pop());
    }
    
    private WebDriver createWebDriver() {
        System.out.print("Initializing Selenium: ");

        if (TestProperties.inst().BROWSER.equals("htmlunit")) {
            System.out.println("Using HTMLUnit.");

            HtmlUnitDriver htmlUnitDriver = new HtmlUnitDriver(BrowserVersion.FIREFOX_38);
            htmlUnitDriver.setJavascriptEnabled(true);
            return htmlUnitDriver;

        } else if (TestProperties.inst().BROWSER.equals("firefox")) {
            // String url = "http://ashrayjain:65868d03-87eb-4385-936a-1624d088fd15@ondemand.saucelabs.com:80/wd/hub";
            // 
            // DesiredCapabilities caps = DesiredCapabilities.firefox();
            // caps.setCapability("platform", "Windows 10");
            // caps.setCapability("version", "38.0.5");
            // // String hub_url = String.format("%s:%s@localhost:4445",
            // //                                ,
            // //                                );
            // try {
            //     return new RemoteWebDriver(new URL(url), caps);
            // } catch (MalformedURLException e) {
            //     return null;
            // }
            System.out.println("Using Firefox.");
            String firefoxPath = TestProperties.inst().FIREFOX_PATH;
            if(!firefoxPath.isEmpty()){
                System.out.println("Custom path: " + firefoxPath);
                System.setProperty("webdriver.firefox.bin",firefoxPath);
            }
            return new FirefoxDriver();

        } else if (TestProperties.inst().BROWSER.equals("chrome")) {

            System.out.println("Using Chrome.");

            // We use the technique given in
            // http://code.google.com/p/selenium/wiki/ChromeDriver
            ChromeDriverService service = startChromeDriverService();
            return (new RemoteWebDriver(service.getUrl(), DesiredCapabilities.chrome()));

        } else if (TestProperties.inst().BROWSER.equals("iexplore")) {
            System.out.println("Using IE.");
            File file = new File(TestProperties.getIEDriverPath());
            System.setProperty("webdriver.ie.driver", file.getAbsolutePath());

            return new InternetExplorerDriver();
        }else {
            System.out.println("Using " + TestProperties.inst().BROWSER 
                    + " is not supported!");
            return null;
        }

    }
    
    private ChromeDriverService startChromeDriverService() {
        ChromeDriverService chromeService = new ChromeDriverService.Builder()
                .usingDriverExecutable(
                        new File(TestProperties.getChromeDriverPath()))
                .usingAnyFreePort().build();
        try {
            chromeService.start();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return chromeService;
    }
}
