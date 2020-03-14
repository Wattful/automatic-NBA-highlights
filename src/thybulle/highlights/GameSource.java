package thybulle.highlights;

import java.time.*;
import java.util.*;
import java.io.*;

/**Interface representing a source of play-by-play data.<br>
The source must be able to perform two operations.<br>
It must be able to get the information of all games that occurred on a particular day 
and it must be able to return play-by-play data for a game given a GameInfo object.
*/

interface GameSource {
	/**Returns a list of GameInfos representing all NBA games played on the given date.
	@param date The date to get GameInfos for.
	@throws NullPointerExcpetion if date is null
	@return a list of GameInfos representing all NBA games played on the given date.
	*/
	List<GameInfo> getGameInformationOnDay(LocalDate date) throws IOException;

	/**Returns a collection of all plays occuring in the game represented by the given GameInfo, or an empty collection if no video info is available for the game.<br>
	The iteration order of the returned collection will return the plays in the order of when they occurred in the game.
	@param gi GameInfo representing the game to get plays of.
	@throws NullPointerException if gi is null.
	@return a collection of all plays occuring in the game represented by the given GameInfo.
	*/
	Collection<? extends Play> getPlayByPlay(GameInfo gi) throws IOException;
	
	/**Closes all resources associated with this GameSource.<br>
	 * Once this method is called on an instance of GameSource, no other methods can be called on the same instance.
	 */
	void exit();
}