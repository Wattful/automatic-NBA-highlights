package thybulle.highlights;

import java.time.*;

import org.json.*;

/**Class representing all of the necessary information to identify an NBA game. <br>
Stores the date that a game occurred on, as well as the game's home and away teams. <br>
@author Owen Kulik
*/

public class GameInfo implements Comparable<GameInfo> {
	private final LocalDate date;
	private final Team away;
	private final Team home;

	/**Constructs a GameInfo with the given infomration.
	 * @param time The date the games was played on.
	 * @param awayTeam The game's away team.
	 * @param homeTeam The game's home team.
	 * @throws NullPointerException if any parameter is null.
	 * @throws IllegalArgumentException if awayTeam is the same as homeTeam.
	 */
	public GameInfo(LocalDate time, Team awayTeam, Team homeTeam){
		if(time == null || awayTeam == null || homeTeam == null){
			throw new NullPointerException();
		}
		if(awayTeam.equals(homeTeam)){
			throw new IllegalArgumentException("A team cannot play itself:" + awayTeam.toString() + " " + homeTeam.toString());
		}
		date = time;
		away = awayTeam;
		home = homeTeam;
	}

	/**Returns this GameInfo's home team.
	@return this GameInfo's home team.
	*/
	public Team homeTeam(){
		return home;
	}

	/**Returns this GameInfo's away team.
	@return this GameInfo's away team.
	*/
	public Team awayTeam(){
		return away;
	}

	/**Returns this GameInfo's date.
	@return this GameInfo's date.
	*/
	public LocalDate date(){
		return date;
	}

	/**Returns true if t is the home team or the away team in this GameInfo.
	@return true if t is the home team or the away team in this GameInfo, false otherwise.
	*/
	public boolean hasTeam(Team t){
		return home.equals(t) || away.equals(t);
	}

	/**Returns a JSONObject representation of this GameInfo.<br>
	The JSONObject will have three keys: "date", "away", and "home", 
	which point to string representations of the date, away team, and home team, respectively.
	@return a JSONObject representation of this GameInfo.
	*/
	public JSONObject toJSON(){
		JSONObject answer = new JSONObject();
		answer.put("date", this.date.toString());
		answer.put("away", this.away.toString());
		answer.put("home", this.home.toString());
		return answer;
	}

	/**Parses and returns a GameInfo from the given JSONObject according to the specification in the toJSON() method.
	@param input The JSONObject to parse.
	@throws NullPointerException if input is null.
	@throws JSONException if the JSONObject does not meet the specification.
	@return a GameInfo with the information in the given JSONObject.
	*/
	public static GameInfo fromJSON(JSONObject input){
		return new GameInfo(LocalDate.parse(input.getString("date")), Team.get(input.getString("away")), Team.get(input.getString("home")));
	}

	/**Parses and returns a GameInfo from a String.<br>
	The string must be in EXACTLY the following format: DATE - AWAY_TEAM at HOME_TEAM.<br>
	The date can be in any format which can be parsed by the LocalDate.parse() method.<br>
	Note that this format is the same as the one return by GameInfo.toString().
	@param input The String to parse.
	@throws NullPointerException if input is null.
	@throws IllegalArgumentException if input does not match the specified format.
	@return a GameInfo object parsed from the given string.
	*/
	public static GameInfo parse(String input){
		if(input.indexOf(" - ") == -1 || input.indexOf(" at ") == -1 || input.indexOf(" - ") > input.indexOf(" at ")){
			throw new IllegalArgumentException("Malformed GameInfo String: " + input);
		}
		String date = input.substring(0, input.indexOf(" - "));
		String away = input.substring(input.indexOf(" - ") + 3, input.indexOf(" at "));
		String home = input.substring(input.indexOf(" at ") + 4);
		return new GameInfo(LocalDate.parse(date), Team.get(away), Team.get(home));
	}

	@Override
	/**Compares this GameInfo to the specified GameInfo.<br>
	They are compared first by the date and time they occurred, then by the away team name, then by the home team name.
	@param other The Game to compare this to.
	@throws NullPointerException if other is null.
	@return a negative number if this Game is "less than" the other game, 0 if they are equal
	*/
	public int compareTo(GameInfo other){
		int dateCompare = this.date.compareTo(other.date);
		if(dateCompare != 0){
			return dateCompare;
		}
		int awayCompare = this.away.compareTo(other.away);
		if(awayCompare != 0){
			return awayCompare;
		}
		return this.home.compareTo(other.home);
	}

	@Override
	/**Returns a hash code for this GameInfo object.
	@return a hash code for this GameInfo object.
	*/
	public int hashCode(){
		return date.hashCode() + away.hashCode() + home.hashCode();
	}

	@Override
	/**Returns a boolean indicating whether this GameInfo is equal to the provided object.<br>
	They are considered equal if all fields are equal.
	@param o The object to compare to.
	@return a boolean indicating whether this GameInfo is equal to the provided object.
	*/
	public boolean equals(Object o){
		if(o == null){
			return false;
		}
		if(this == o){
			return true;
		}
		if(!(o instanceof GameInfo)){
			return false;
		}
		GameInfo gi = (GameInfo)o;
		return this.date.equals(gi.date) && this.away.equals(gi.away) && this.home.equals(gi.home);
	}

	@Override
	/**Returns a String representing this GameInfo object.
	@return a String representing this GameInfo object.
	*/
	public String toString(){
		return date.toString() + " - " + away.toString() + " at " + home.toString();
	}
}