package thybulle.highlights;

import java.time.*;

/**Class representing all of the necessary information to identify an NBA game. <br>
Stores the date that a game occurred on, as well as the game's home and away team. <br>
@author Owen Kulik
*/

public class GameInfo implements Comparable<GameInfo> {
	private static final boolean CHECK_REP = true;

	private final LocalDate date;
	private final Team away;
	private final Team home;

	//RI: No fields are null, !away.equals(home).
	//AF: Represents a game data "skeleton" - all data necessary to identify a game. date is the game's date and time, away is the away team, home is the home team.

	//Constructs a GameInfo with the given data.
	GameInfo(LocalDate time, Team awayTeam, Team homeTeam){
		if(time == null || awayTeam == null || homeTeam == null){
			throw new NullPointerException();
		}
		if(awayTeam.equals(homeTeam)){
			throw new IllegalArgumentException("A team cannot play itself.");
		}
		date = time;
		away = awayTeam;
		home = homeTeam;
		checkRep();
	}

	//Checks this object's rep invariant.
	private void checkRep(){
		if(!CHECK_REP){
			return;
		}
		if(date == null || away == null || home == null){
			throw new IllegalStateException();
		}
		if(away.equals(home)){
			throw new IllegalStateException();
		}
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