import io.github.bonigarcia.wdm.WebDriverManager;
import java.util.Optional;
import javax.swing.text.html.Option;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.devtools.Command;
import org.openqa.selenium.devtools.Console;
import org.openqa.selenium.devtools.Console.ConsoleMessage;
import org.openqa.selenium.devtools.DevTools;
import org.openqa.selenium.devtools.network.Network;
import org.openqa.selenium.devtools.security.Security;

public class ChromeDevToolTest {

  private static ChromeDriver chromeDriver;
  private static DevTools chromeDevTools;

  @BeforeClass
  public static void setupDriverAndTools() {
    WebDriverManager.chromedriver().setup();
    chromeDriver = new ChromeDriver();
    chromeDriver.get("https://www.google.com");
    chromeDevTools = chromeDriver.getDevTools();
    chromeDevTools.createSession();
  }

  @Test
  public void clearBrowserCaches() {
    chromeDevTools.send(Network.clearBrowserCookies());
    Assert.assertTrue(chromeDriver.manage().getCookies().isEmpty());
  }

  @Test
  public void loadInsecureWebsite() {
    chromeDevTools.send(Security.enable());
    chromeDevTools.send(Security.setIgnoreCertificateErrors(true));
    chromeDriver.get("https://expired.badssl.com/");
    Assert.assertTrue(chromeDriver.getPageSource().contains("true"));
  }

  @Test
  public void verifyConsoleMessageAdded() {
    String consoleMessage = "Hello";
    chromeDevTools.send(Console.enable());
    chromeDevTools.addListener(Console.messageAdded(), consoleMessageFromDevTools ->
        Assert.assertEquals(true, consoleMessageFromDevTools.getText().contains(consoleMessage)));
    chromeDriver.get("https://www.google.com");
    chromeDriver.executeScript("console.log('" + consoleMessage + "')");
  }

  @Test
  public void interceptRequestAndContinue() {
    chromeDevTools.send(Network.enable(Optional.empty(), Optional.empty(), Optional.empty()));
    chromeDevTools.addListener(Network.requestIntercepted(),
        requestIntercepted -> chromeDevTools.send(Network.continueInterceptedRequest(requestIntercepted.getInterceptionId(),
            Optional.empty(), Optional.empty(),
            Optional.empty(), Optional.empty(),
            Optional.empty(), Optional.empty(),
            Optional.empty())));
  }

  @AfterClass
  public static void driverQuit() {
    chromeDriver.quit();
  }
}
