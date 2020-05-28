package thybulle.highlights;

import java.time.*;
import java.time.temporal.*;
import java.util.*;
import java.io.*;

/**Interface representing a source of play-by-play data.<br>
The source must be able to perform two operations.<br>
It must be able to get the information of all games that occurred on a particular day 
and it must be able to return play-by-play data for a game given a GameInfo object.<br>
Additionally, several default methods which call getGameInformationOnDay and getGame in various ways are included.
*/

public interface GameSource {
	/**Returns a list of GameInfos representing all NBA games played on the given date.
	@param date The date to get GameInfos for.
	@throws NullPointerExcpetion if date is null.
	@throws IOException if an IO error occurs.
	@return a list of GameInfos representing all NBA games played on the given date.
	*/
	List<GameInfo> getGameInformationOnDay(LocalDate date) throws IOException;

	/**Returns a Game with play-by-play data for the given GameInfo, or null if the GameInfo does not represent a valid game.
	@param gi GameInfo representing the game to get plays of.
	@throws NullPointerException if gi is null.
	@throws IOException if an IO error occurs.
	@return a Game with play-by-play data for the given GameInfo.
	*/
	Game getGame(GameInfo gi) throws IOException;
	
	/**Closes all resources associated with this GameSource.<br>
	 * Once this method is called on an instance of GameSource, other method calls may or may not function correctly.
	 */
	void exit() throws IOException;

	/**Returns a list of games corresponding to the provided GameInfos.<br>
	The order of the list corresponds to the iteration order of the provided Collection.
	@throws NullPointerException if any paramter is null.
	@throws IOException if an IO error occurs.
	@param gi The GameInfos.
	@return a list of games corresponding to the provided GameInfos.
	*/
	default List<Game> getGames(Collection<GameInfo> gis) throws IOException {
		List<Game> answer = new LinkedList<Game>();
		for(GameInfo gi : gis){
			answer.add(getGame(gi));
		}
		return answer;
	}

	/**Returns information for all NBA games played between the given dates, inclusive.
	@param beginning The beginning date.
	@param end The end date.
	@throws NullPointerException if any paramter is null.
	@throws IllegalArgumentException if end is before beginning.
	@throws IOException if an IO error occurs.
	@return information for all NBA games played between the given dates, inclusive.
	*/
	default List<GameInfo> getGameInformationBetweenDates(LocalDate beginning, LocalDate end) throws IOException {
		if(beginning.isAfter(end)){
			throw new IllegalArgumentException("Beginning date was after end date.");
		}
		if(beginning.equals(end)){
			return this.getGameInformationOnDay(beginning);
		}
		List<GameInfo> answer = new LinkedList<GameInfo>();
		for(int i = 0; i <= beginning.until(end, ChronoUnit.DAYS); i++){
			LocalDate d = beginning.plusDays(i);
			answer.addAll(this.getGameInformationOnDay(d));
		}
		return answer;
	}

	/**Returns information for games played by the given teams on date.<br>
	 * If no teams are specified, returns information for all games on the date.
	@param date The date to look for a game.
	@param team The team to get the game of.
	@throws NullPointerException if any paramter is null.
	@throws IOException if an IO error occurs.
	@return information for games played by the given teams on date
	*/
	default List<GameInfo> getTeamGameInformationOnDay(LocalDate date, Team... teams) throws IOException {
		if(teams.length == 0){
			return this.getGameInformationOnDay(date);
		}
		List<GameInfo> answer = new LinkedList<GameInfo>();
		for(GameInfo g : this.getGameInformationOnDay(date)){
			for(Team team : teams){
				if(g.hasTeam(team)){
					answer.add(g);
				}
			}
		}
		return answer;
	}

	/**Returns information for all games played by the given teams between the given dates, inclusive.<br>
	 * If no teams are specified, returns information for all games between the dates.
	@param beginning The beginning date.
	@param end The end date.
	@param teams The teams to get games of.
	@throws NullPointerException if any paramter is null.
	@throws IllegalArgumentException if end is before beginning.
	@throws IOException if an IO error occurs.
	@return a list of all GameInfos played by the given team between the given dates, inclusive.	
	*/
	default List<GameInfo> getTeamGameInformationBetweenDates(LocalDate beginning, LocalDate end, Team... teams) throws IOException {
		if(teams.length == 0){
			return this.getGameInformationBetweenDates(beginning, end);
		}
		if(beginning.isAfter(end)){
			throw new IllegalArgumentException("Beginning date was after end date.");
		}
		if(beginning.equals(end)){
			return this.getTeamGameInformationOnDay(beginning, teams);
		}
		List<GameInfo> answer = new LinkedList<GameInfo>();
		for(int i = 0; i <= beginning.until(end, ChronoUnit.DAYS); i++){
			LocalDate d = beginning.plusDays(i);
			List<GameInfo> g = this.getTeamGameInformationOnDay(d, teams);
			answer.addAll(g);
		}
		return answer;
	}
}