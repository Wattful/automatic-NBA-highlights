package thybulle.misc;

import java.util.logging.*;

public class Logging {
	private final Logger logging;

	/**Constructs a Logging with the given name.
	@param name The Logging's name.
	*/
	public Logging(String name){
		logging = Logger.getLogger(name);
	}

	/**Adds the given handler to this logger.
	@param h The handler to add.
	*/
	public void addHandler(Handler h){
		logging.addHandler(h);
	}

	/**Logs the given message with level info.
	@param message The message to log.
	*/
	public void info(String message){
		logging.log(Level.INFO, message);
	}

	/**Logs the given message with level warning.
	@param message The message to log.
	*/
	public void warning(String message){
		logging.log(Level.WARNING, message);
	}

	/**Logs the given message with level severe.
	@param message The message to log.
	*/
	public void error(String message){
		logging.log(Level.SEVERE, message);
	}
}