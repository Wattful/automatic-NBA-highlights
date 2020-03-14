package thybulle.highlights;

import java.util.*;
import thybulle.misc.*;

//TODO:

/**Immutable class representing a player. <br>
This class uses interning, and as such no constructors are public. <br>
If one wishes to "construct" a player, they should use the {@code get(String, String)} method.
@author Owen Kulik
*/

public class Player implements Constraint, Comparable<Player> {
	private static final boolean CHECK_REP = true;

	private final String firstName;
	private final String lastName;

	private static final Map<Pair<String, String>, Player> interning = new HashMap<Pair<String, String>, Player>(); //please hire me for an internship.

	//RI: lastName is not null.
	//AF: Represents an immutable player with first name firstName and last name lastName. If a player has one name (ex Nene), firstName is null. 

	/*Constructs a player with first name first and last name last. If a player only has one name (ex Nene), first should be null.
	*/
	private Player(String first, String last){
		if(last == null){
			throw new NullPointerException("Last name was null.");
		}
		firstName = first;
		lastName = last;
		checkRep();
	}

	//Checks the rep invariant of this Player
	private void checkRep(){
		if(!CHECK_REP){
			return;
		}
		if(lastName == null){
			throw new IllegalStateException("Last name was null.");
		}
	}

	/**Returns a player with first name first and last name last. If a player only has one name (ex Nene), first should be null.<br>
	This method returns a canonical instance of a player.<br>
	If a player with this name has not been created yet, it will be created and returned.<br>
	If one has been created, a reference to the already created player is returned.<br>
	This should be used as the "constructor" for Player.
	@param first the player's first name.
	@param last the player's last name. Cannot be null.
	@throws NullPointerException if last is null.
	@return a canonical representation of a player.
	*/
	public static Player get(String first, String last){
		if(last == null){
			throw new NullPointerException();
		}
		first = first != null ? first.toLowerCase() : null;
		last = last.toLowerCase();
		Player p = interning.get(new Pair<String, String>(first, last));
		if(p == null){
			Player answer = new Player(first, last);
			interning.put(new Pair<String, String>(first, last), answer);
			return answer;
		} else {
			return p;
		}
	}

	@Override
	/**Returns true if and only if the specified play involves this player.
	@param p The play to consider.
	@return a boolean indicating whether the specified play involves this player.
	*/
	public boolean satisfiedBy(Play p){
		return p.getPlayers().contains(this);
	}

	/**Returns this player's first name.<br>
	If the player does not have a first name, an empty string is returned.
	@return this player's first name.
	*/
	public String firstName(){
		return firstName != null ? firstName : "";
	}

	/**Returns this player's last name.
	@return this player's first name.
	*/
	public String lastName(){
		return lastName;
	}

	/**Returns a String with a player's first initial and last name.<br>
	For example, a player object representing Matisse Thybulle would return "M. Thybulle".
	@return a String with a player's first initial and last name.
	*/
	public String initialedName(){
		if(firstName == null){
			return lastName;
		}
		return firstName.substring(0, 1) + ". " + lastName;
	}

	/**Returns this Player's full name.<br>
	For example, a player object representing Matisse Thybulle would return "Matisse Thybulle".
	@return this Player's full name.
	*/
	public String fullName(){
		if(firstName == null){
			return lastName;
		}
		return firstName + " " + lastName;
	}

	@Override
	/**Compares this Player to the specified player, using the players' full names.
	@param other The team to compare this to.
	@throws NullPointerException if other is null.
	@return a negative number if this is "less than" other, 0 if this is equal to other, and a positive number if this is "greater than" other.
	*/
	public int compareTo(Player other){
		if(this == other){
			return 0;
		}
		int nameCompare = this.fullName().compareTo(other.fullName());
		if(nameCompare != 0){
			return nameCompare;
		}
		//This should never happen.
		throw new AssertionError("Two different instances of the same player existed.");
	}

	@Override
	/**Returns a String representing this Player object.<br>
	Calling this method is exactly equivalent to calling fullName().
	@return a String representing this Player object.
	*/
	public String toString(){
		return this.fullName();
	}
}