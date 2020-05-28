package thybulle.highlights;

import java.io.PrintStream;
import java.util.logging.*;
import thybulle.misc.Logging;

/**Class to be used for logging in the Highlights package.
 * Outside classes can designate where they want logging output to be printed using the setOutput method.
 * @author kuliko
 *
 */

public class HighlightsLogger {
	static Logging logging = new Logging();

	private HighlightsLogger(){}

	/**Outputs information from Highlights to the given PrintStream.
	 * @param ps the PrintStream to use. If ps is null, disables logging.
	*/
	public static void setOutput(PrintStream ps){
		if(ps == null) {
			logging = new Logging();
		} else {
			logging = new Logging(ps);
		}
	}
}