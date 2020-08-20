package thybulle.highlights;

import java.util.*;
import java.time.*;
import java.time.temporal.*;
import java.io.*;
import thybulle.misc.*;

/**Immutable class representing an NBA game.<br>
This class contains play-by-play data for a game, as well as information about when the game was played, the final score, and the teams playing.<br>
Game acts as the main way for the core package to interact with the datacollection package.<br>
When a client class provides the date, time, and teams of a game
the Game class will use the datacollection package to get play-by-play data for that game and construct a Game class, 
or return a reference to such a Game class if it already exists.<br> 
In order to get a Game object, one must use a class which implements the GameSource interface.<br>
@author Owen Kulik
*/

public class Game {
	private final LocalDate date;

	private final Team awayTeam;
	private final Team homeTeam;

	private final List<Play> data = new LinkedList<Play>();

	//Constructs a game from the given data. This constructor should only be called from the Source.getGame method.
	Game(GameInfo gi, Collection<? extends Play> plays){
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
	/**Returns a hash code for this Game object.
	@return a hash code for this game object.
	*/
	public int hashCode(){
		return (date.hashCode() + awayTeam.hashCode() + homeTeam.hashCode()) >> 12;
	}

	@Override
	/**Returns a boolean indicating whether this game is equivalent to the given object.<br>
	They will be considered equivalent if o is a Game and they refer to the same Game.<br>
	The two objects do not necessarily need to have the exact same plays to be equal.
	@param o The object to compare to.
	@return a boolean indicating whether this game is equivalent to the given object.
	*/
	public boolean equals(Object o){
		if(o == null){
			return false;
		}
		if(this == o){
			return true;
		}
		if(!(o instanceof Game)){
			return false;
		}
		Game g = (Game)o;
		return this.date.equals(g.date) && this.awayTeam.equals(g.awayTeam) && this.homeTeam.equals(g.homeTeam);
	}

	@Override
	/**Returns a String representation of this Game.
	@return a String representation of this Game.
	*/
	public String toString(){
		return date.toString() + " - " + awayTeam.toString() + " at " + homeTeam.toString();
	}
}