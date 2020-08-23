package thybulle.highlights;

import org.json.*;

import java.io.IOException;
import java.nio.file.*;

import org.openqa.selenium.chrome.*;
import org.openqa.selenium.firefox.*;
import org.openqa.selenium.edge.*;
import org.openqa.selenium.remote.DesiredCapabilities;

import thybulle.misc.InternetVideo;

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
	static final String DEFAULT_CONFIG_PATH = "../config/browserconfig.json";

	private final String browserName;
	private final boolean headless;
	private final boolean suppressOutput;
	private final String executablePath;

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
		this.browserName = browserName.toLowerCase();
		this.headless = browserName.toLowerCase().equals("edge") ? false : headless;
		this.suppressOutput = browserName.toLowerCase().equals("edge") ? false : suppressOutput;
		this.executablePath = executablePath;
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
	
	/**Returns this browser's brand name.
	 * @return this browser's brand name.
	 */
	public String browserName() {
		return this.browserName;
	}
	
	/**Returns whether this Browser is in headless mode.
	 * @return whether this Browser is in headless mode.
	 */
	public boolean headless() {
		return this.headless;
	}
	
	/**Returns whether this Browser will suppress its outputs.
	 * @return whether this Browser will suppress its outputs.
	 */
	public boolean suppressOutput() {
		return this.suppressOutput;
	}
	
	/**Returns the path to the driver executable.
	 * @return the path to the driver executable.
	 */
	public String executablePath() {
		return this.executablePath;
	}

	/**Returns a Browser object constructed from the given JSON object according to the specification above.
	@throws NullPointerException if any parameter is null.
	@throws JSONException if any required keys are missing.
	@return a Browser object constructed from the given JSON object according to the specification above.
	*/
	public static Browser fromJSONObject(JSONObject input){
		String browserName = input.getString("name");
		boolean headless = input.optBoolean("headless", true);
		boolean suppressOutput = input.optBoolean("suppressOutput", true);
		String executablePath = input.getString("executable");
		return new Browser(browserName, headless, suppressOutput, executablePath);
	}
	
	/**Returns a hash code for this Browser.
	 * @return a hash code for this Browser.
	 */
	public int hashCode() {
		return this.browserName.hashCode() + (this.headless ? 1 : 0) + (this.suppressOutput ? 1 : 0) + this.executablePath.hashCode();
	}
	
	/**Returns a boolean indicating whether this Browser is equal to the provided object.<br>
	 * They are considered equal if and only if the given object is a Browser and all of its fields are equal.
	 * @param o The object to compare to
	 * @return a boolean indicating whether this Browser is equal to the provided object.
	 */
	public boolean equals(Object o) {
		if(o == null){
			return false;
		}
		if(this == o){
			return true;
		}
		if(!(o instanceof Browser)){
			return false;
		}
		Browser b = (Browser)o;
		return this.browserName.equals(b.browserName()) && this.headless == b.headless && this.suppressOutput == b.suppressOutput && this.executablePath.equals(b.executablePath);
	}
	
	/**Returns a String representation of this Browser.
	 * @return a String representation of this Browser.
	 */
	public String toString() {
		return "Browser: " + this.browserName();
	}
}