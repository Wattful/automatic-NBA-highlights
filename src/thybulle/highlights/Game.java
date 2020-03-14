package thybulle.highlights;

import java.util.*;
import java.time.*;
import java.time.temporal.*;
import java.io.*;
import thybulle.misc.*;

//TODO:

/**Immutable class representing an NBA game.<br>
This class contains play-by-play data for a game, as well as information about when the game was played, the final score, and the teams playing.<br>
Game acts as the main way for the core package to interact with the datacollection package.<br>
When a client class provides the date, time, and teams of a game
the Game class will use the datacollection package to get play-by-play data for that game and construct a Game class, 
or return a reference to such a Game class if it already exists.<br> 
In order to construct a game, one must use the Game.Source enum.<br>
@author Owen Kulik
*/

public class Game {
	private static final boolean CHECK_REP = true;

	private final LocalDate date;

	private final Team awayTeam;
	private final Team homeTeam;

	private final List<Play> data = new LinkedList<Play>();

	private static final Map<GameInfo, Game> interning = new HashMap<GameInfo, Game>();

	/*RI: no fields are null, all plays in data were committed by either awayTeam or homeTeam, !awayTeam.equals(homeTeam), 
	awayScore != homeScore, awayWon == awayScore > homeScore
	*/
	/*AF: Represents a game. date is the date and time of the game, awayTeam and homeTeam are the participating teams,
	awayScore and homeScore are each team's scores, awayWon is whether the away team won, data is each play in the game, 
	sorted by when they occurred in the game.
	*/

	//Constructs a game from the given data. This constructor should only be called from the Source.getGame method.
	private Game(GameInfo gi, Collection<? extends Play> plays){
		if(plays == null || gi == null){
			throw new NullPointerException();
		}
		if(plays.contains(null)){
			throw new NullPointerException();
		}
		data.addAll(plays);
		date = gi.date();
		awayTeam = gi.awayTeam();
		homeTeam = gi.homeTeam();
		checkRep();
	}

	//Checks this object's rep invariant.
	private void checkRep(){
		if(!CHECK_REP){
			return;
		}
		if(data == null || date == null || awayTeam == null || homeTeam == null){
			throw new IllegalStateException();
		}
		if(awayTeam.equals(homeTeam)){
			throw new IllegalStateException();
		}
		if(data.contains(null)){
			throw new IllegalStateException();
		}
		for(Play p : data){
			if(!p.getTeam().equals(awayTeam) && !p.getTeam().equals(homeTeam)){
				throw new IllegalStateException();
			}
		}
	}

	/**Returns an unmodifiable list of plays with all plays in this Game that meet the given constraints, in order of when they occurred in the game.<br>
	In order for a play to be returned, it must satisfy all given constraints.
	@param constraints All constraints.
	@throws NullPointerException if constraints is null, or any value in constraints is null.
	@return a list of all plays which meet the given constraints.
	*/
	public List<Play> getAllPlaysThatSatisfy(Constraint... constraints){
		return List.copyOf(this.constrain(new AndConstraint(constraints)));
	}

	//Returns a list of all plays in this game that meet the constraint.
	private List<Play> constrain(Constraint constraint){
		List<Play> answer = new LinkedList<Play>();
		for(Play p : data){
			if(constraint.satisfiedBy(p)){
				answer.add(p);
			}
		}
		return answer;
	}

	/**Returns the away team for this Game.
	@return the away team for this Game.
	*/
	public Team awayTeam(){
		return awayTeam;
	}

	/**Returns the home team for this Game.
	@return the home team for this Game.
	*/
	public Team homeTeam(){
		return homeTeam;
	}

	/**Returns an unmodifiable list of all plays in this game, ordered by the time they occurred.
	@return an unmodifiable list of all plays in this game, ordered by the time they occurred.
	*/
	public List<Play> getAllPlays(){
		return List.copyOf(data);
	}

	/**Returns the date this game was played.
	@return the date this game was played.
	*/
	public LocalDate getDate(){
		return date;
	}

	@Override
	/**Returns a String representation of this Game.
	@return a String representation of this Game.
	*/
	public String toString(){
		return date.toString() + " - " + awayTeam.toString() + " at " + homeTeam.toString();
	}

	/**Enum representing potential sources of game play-by-play data.<br>
	Each value is a different website from which play-by-play data can be downloaded and parsed.
	*/
	public static enum Source {
		/**stats.nba.com
		*/
		NBA_ADVANCED_STATS(AdvancedStats.open());

		private final GameSource source;

		private Source(GameSource s){
			source = s;
		}

		/**Returns a Game with play-by-play data from a game which occurred at 
		the specified date and time between the specified teams, or null if no such Game exists.<br>
		An assumption is made that no two teams will play each other twice on the same day.<br>
		This method returns a canonical instance of a Game.<br>
		If the requested Game has not been created yet, it will be created and returned.<br>
		If it has been created, a reference to the already created Game is returned.<br>
		This should be used as the "constructor" for Game.
		@param time The date of the game.
		@param away The away team.
		@param home The home team.
		@throws IllegalArgumentException if away.equals(home) (a team cannot play itself).
		@throws NullPointerException if any parameter is null.
		@return a Game with play-by-play data from a game which occurred at 
		the specified date and time between the specified teams, or null if no such Game exists.
		*/
		public Game getGame(GameInfo gi) throws IOException {
			if(interning.containsKey(gi)){
				return interning.get(gi);
			}
			Collection<? extends Play> plays = this.downloadGame(gi);
			if(plays == null) {
				return null;
			}
			Game g = new Game(gi, plays);
			interning.put(gi, g);
			return g;
		}

		//"Downloads" and returns a game corresponding to the given GameInfo.
		private Collection<? extends Play> downloadGame(GameInfo gi) throws IOException {
			return source.getPlayByPlay(gi);
		}

		/**Returns a list of games corresponding to the provided GameInfos.<br>
		The order of the list corresponds to the iteration order of the provided Collection.
		@param gi The GameInfos.
		@return a list of games corresponding to the provided GameInfos.
		*/
		public List<Game> getGames(Collection<GameInfo> gis) throws IOException {
			List<Game> answer = new LinkedList<Game>();
			for(GameInfo gi : gis){
				answer.add(getGame(gi));
			}
			return answer;
		}

		/**Returns information for all NBA games played on the given day.
		@param date The day to get NBA games from.
		@throws NullPointerException if any paramter is null.
		@return information for all NBA games played on the given day.
		*/
		public List<GameInfo> getGameInfomationOnDay(LocalDate ld) throws IOException {
			return source.getGameInformationOnDay(ld);
		}

		/**Returns information for all NBA games played between the given dates, inclusive.
		@param beginning The beginning date.
		@param end The end date.
		@throws NullPointerException if any paramter is null.
		@throws IllegalArgumentException if end is before beginning.
		@return information for all NBA games played between the given dates, inclusive.
		*/
		public List<GameInfo> getGameInformationBetweenDates(LocalDate beginning, LocalDate end) throws IOException {
			if(beginning.isAfter(end)){
				throw new IllegalArgumentException("Beginning date was after end date.");
			}
			List<GameInfo> answer = new LinkedList<GameInfo>();
			for(int i = 0; i < beginning.until(end, ChronoUnit.DAYS); i++){
				LocalDate d = beginning.plusDays(i);
				answer.addAll(getGameInfomationOnDay(d));
			}
			return answer;
		}

		/**Returns information for games played by the given teams on date.<br>
		 * If no teams are specified, returns information for all games on the date.
		@param date The date to look for a game.
		@param team The team to get the game of.
		@throws NullPointerException if any paramter is null.
		@return information for games played by the given teams on date
		*/
		public List<GameInfo> getTeamGameInfomationOnDay(LocalDate date, Team... teams) throws IOException {
			if(teams.length == 0){
				return getGameInfomationOnDay(date);
			}
			List<GameInfo> answer = new LinkedList<GameInfo>();
			for(GameInfo g : source.getGameInformationOnDay(date)){
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
		@return a list of all GameInfos played by the given team between the given dates, inclusive.	
		*/
		public List<GameInfo> getTeamGameInformationBetweenDates(LocalDate beginning, LocalDate end, Team... teams) throws IOException {
			if(teams.length == 0){
				return getGameInformationBetweenDates(beginning, end);
			}
			if(beginning.isAfter(end)){
				throw new IllegalArgumentException("Beginning date was after end date.");
			}
			List<GameInfo> answer = new LinkedList<GameInfo>();
			for(int i = 0; i < beginning.until(end, ChronoUnit.DAYS); i++){
				LocalDate d = beginning.plusDays(i);
				List<GameInfo> g = getTeamGameInfomationOnDay(d, teams);
				answer.addAll(g);
			}
			return answer;
		}
		
		/**Closes all resources associated with this source.<br>
		 * Once this method is called, other methods associated with this Game Source can no longer be called.
		 */
		public void exit() {
			source.exit();
		}
	}
}