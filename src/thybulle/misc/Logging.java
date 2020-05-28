package thybulle.misc;

import java.io.PrintStream;

/**Class representing a simple logging interface.
 * Messages are output to a PrintStream which is set at creation time.
 * @author Owen Kulik
 */

public class Logging {
	private final PrintStream logging;
	
	/**Constructs a Logging without an output. Calls to this Logging will result in nothing.
	 */
	public Logging() {
		logging = null;
	}
	
	/**Constructs a Logging with the given PrintStream.
	@param ps the PrintStream to use.
	*/
	public Logging(PrintStream ps){
		logging = ps;
	}
	
	//Outputs the given message.
	private void log(String message) {
		if(logging != null) {
			logging.println(message);
		}
	}
	
	/**Logs the given message with level info.
	@param message The message to log.
	*/
	public void info(String message){
		log("INFO: " + message);
	}

	/**Logs the given message with level warning.
	@param message The message to log.
	*/
	public void warning(String message){
		log("WARNING: " +  message);
	}

	/**Logs the given message with level error.
	@param message The message to log.
	*/
	public void error(String message){
		log("ERROR: " + message);
	}
}