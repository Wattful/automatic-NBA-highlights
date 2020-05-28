package thybulle.highlights;

import org.json.*;

import java.io.IOException;
import java.nio.file.*;

import org.openqa.selenium.chrome.*;
import org.openqa.selenium.firefox.*;
import org.openqa.selenium.edge.*;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.openqa.selenium.*;

/**Class which holds options used to construct a webdriver.<br>
Current options include which browser to use, whether to suppress output, whether to run in headless mode, and the path to the driver's executable file.<br>
This program currently supports three browsers: Mozilla Firefox, Google Chrome, and Microsoft Edge.<br>
Note that Edge supports neither headless mode nor output suppression.<br>
Browser settings can be saved in a JSON config file.<br>
This JSON file can have up to four keys, two of which are mandatory.<br>
<ul>
	<li>name - Mandatory key. Name of the browser to use. Must be either "firefox", "chrome", or "edge", case insensitive.</li>
	<li>headless - Boolean indicating whether the browser should be run in headless mode. Defaults to true.</li>
	<li>suppressOutput - Boolean indicating whether to suppress driver output. Defaults to true.</li>
	<li>executable - Mandatory key. Absolute path to driver executable, including the filename.</li>
</ul>
*/
public class Browser {
	private static final boolean CHECK_REP = true;

	static final String DEFAULT_CONFIG_PATH = "./browserConfig.json";

	private final String browserName;
	private final boolean headless;
	private final boolean suppressOutput;
	private final String executablePath;

	//RI: No fields are null, browserName is a supported browser.

	/**Constructs a Broswer with the given options.
	@throws NullPointerException if any parameter is null.
	@throws IllegalArgumentException if the browser name is unsupported.
	*/
	public Browser(String browserName, boolean headless, boolean suppressOutput, String executablePath){
		if(browserName == null || executablePath == null){
			throw new NullPointerException();
		}
		if(!(browserName.toLowerCase().equals("firefox") || browserName.toLowerCase().equals("chrome") || browserName.toLowerCase().equals("edge"))){
			throw new IllegalArgumentException("Unrecognized or unsupported browser: " + browserName);
		}
		this.browserName = browserName;
		this.headless = headless;
		this.suppressOutput = suppressOutput;
		this.executablePath = executablePath;
		checkRep();
	}

	//Checks this object's rep invariant.
	private void checkRep(){
		if(!CHECK_REP){
			return;
		}
		if(this.browserName == null || this.executablePath == null){
			throw new IllegalStateException();
		}
		if(!(this.browserName.toLowerCase().equals("firefox") || this.browserName.toLowerCase().equals("chrome") || this.browserName.toLowerCase().equals("edge"))){
			throw new IllegalStateException("Unrecognized or unsupported browser: " + browserName);
		}
	}

	/**Returns a newly constructed WebDriver object adhering to the options this Browser was constructed with.
	@return a newly constructed WebDriver object adhering to the options this Browser was constructed with.
	*/
	public WebDriver getDriver(){
		if(browserName.toLowerCase().equals("firefox")){
			System.setProperty("webdriver.gecko.driver", executablePath);
			if(suppressOutput){
				System.setProperty(FirefoxDriver.SystemProperty.DRIVER_USE_MARIONETTE,"true");
				System.setProperty(FirefoxDriver.SystemProperty.BROWSER_LOGFILE,"/dev/null");
			}
			FirefoxOptions options = new FirefoxOptions();
   			if(headless){
   				options.addArguments("--headless");
   			}
   			return new FirefoxDriver(options);
		} else if(browserName.toLowerCase().equals("chrome")){
			System.setProperty("webdriver.chrome.driver", executablePath);
			if(suppressOutput){
				System.setProperty("webdriver.chrome.args", "--disable-logging");
   				System.setProperty("webdriver.chrome.silentOutput", "true");
			}
			//DesiredCapabilities chrome = DesiredCapabilities.chrome();
			ChromeOptions options = new ChromeOptions();
			if(headless){
				options.addArguments("headless", "disable-gpu");
				//chrome.setCapability("goog:chromeOptions", options);
			}
			return new ChromeDriver(options);
		} else if(browserName.toLowerCase().equals("edge")){
			System.setProperty("webdriver.edge.driver", executablePath);
			return new EdgeDriver();
		} else {
			throw new AssertionError();
		}
	}

	/**Returns a Browser object constructed from the given JSON config file according to the specification above.
	@throws IOException if an IO error occurs.
	@throws NullPointerException if any parameter is null.
	@return a Browser object constructed from the given JSON config file according to the specification above.
	*/
	public static Browser fromConfigFile(String configPath) throws IOException {
		return fromJSONObject(new JSONObject(Files.readString(Path.of(configPath))));
	}

	/**Returns a Browser object constructed from the given JSON object according to the specification above.
	@throws NullPointerException if any parameter is null.
	@return a Browser object constructed from the given JSON object according to the specification above.
	*/
	public static Browser fromJSONObject(JSONObject input){
		String browserName = input.getString("name");
		boolean headless = input.optBoolean("headless", true);
		boolean suppressOutput = input.optBoolean("suppressOutput", true);
		String executablePath = input.getString("executable");
		return new Browser(browserName, headless, suppressOutput, executablePath);
	}
}