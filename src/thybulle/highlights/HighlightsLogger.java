package thybulle.highlights;

import java.util.logging.*;
import thybulle.misc.Logging;

public class HighlightsLogger {
	static final Logging logging = new Logging("thybulle.highlights");

	private HighlightsLogger(){}

	/**Adds the given handler to this logger.
	@param h The handler to add.
	*/
	public static void addHandler(Handler h){
		logging.addHandler(h);
	}
}