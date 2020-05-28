package thybulle.highlights;

import java.util.*;
import thybulle.misc.*;

/**Enum representing possible play types.<br>
Has the capability of marking PlayTypes as "supertypes" of other play types. For example, dunk is a supertype of and one dunk.
@author Owen Kulik
*/

public enum PlayType implements Constraint {
	UNKNKOWN(0), 
	FIELD_GOAL_ATTEMPT(1), FIELD_GOAL_MISSED(1, FIELD_GOAL_ATTEMPT), FIELD_GOAL_MADE(1, FIELD_GOAL_ATTEMPT), AND_ONE(1, FIELD_GOAL_MADE), 
	DUNK_ATTEMPT(1, FIELD_GOAL_ATTEMPT), DUNK_MADE(1, FIELD_GOAL_MADE, DUNK_ATTEMPT), DUNK_MISSED(1, DUNK_ATTEMPT, FIELD_GOAL_MISSED), AND_ONE_DUNK(1, DUNK_MADE, AND_ONE), 
	THREE_POINTER_ATTEMPT(1, FIELD_GOAL_ATTEMPT), THREE_POINTER_MISSED(1, THREE_POINTER_ATTEMPT, FIELD_GOAL_MISSED), 
	THREE_POINTER_MADE(1, FIELD_GOAL_MADE, THREE_POINTER_ATTEMPT), AND_ONE_THREE_POINTER(1, THREE_POINTER_MADE, AND_ONE),
	FREE_THROW_ATTEMPT(1), FREE_THROW_MADE(1, FREE_THROW_ATTEMPT), FREE_THROW_MISSED(1, FREE_THROW_ATTEMPT),
	REBOUND(1), TEAM_REBOUND(0, REBOUND), 
	ASSIST(1), 
	STEAL(1), 
	BLOCK(1), 
	ALLEY_OOP(2), 
	TURNOVER(1), BASKET_INTERFERENCE(1, TURNOVER), TRAVELING(1, TURNOVER), 
	TEAM_TURNOVER(0, TURNOVER), SHOT_CLOCK_VIOLATION(0, TEAM_TURNOVER), EIGHT_SECOND_VIOLATION(0, TEAM_TURNOVER),
	FOUL(1), FLAGRANT_FOUL(1, FOUL), FLAGRANT_FOUL_1(1, FLAGRANT_FOUL), FLAGRANT_FOUL_2(1, FLAGRANT_FOUL), TECHNICAL_FOUL(1, FOUL), TEAM_TECHNICAL_FOUL(0, TECHNICAL_FOUL),
	DEFENSIVE_FOUL(1, FOUL), SHOOTING_FOUL(1, DEFENSIVE_FOUL), LOOSE_BALL_FOUL(1, FOUL), OFFENSIVE_FOUL(1, FOUL),
	VIOLATION(0), GOALTENDING(1, VIOLATION),
	JUMP_BALL(2), 
	SUBSTITUTION(2),
	TIMEOUT(0);

	//RI: numberOfPlayers >= 0, supertypes != null
	/*AF: Represents a type of play. numberOfPlayers is the number of players involved in this type of play. 
	supertype is all PlayTypes considered this PlayType's supertype - ie dunk is a supertype of and one dunk.*/

	private static final boolean CHECK_REP = true;

	private final int numberOfPlayers;
	private final PlayType[] supertypes;

	//Constructs a PlayType with this number of players and these subtypes.
	private PlayType(int np, PlayType... supert){
		if(np < 0){
			throw new IllegalArgumentException("Number of players was less than 0.");
		}
		if(supert == null){
			throw new NullPointerException();
		}
		numberOfPlayers = np;
		supertypes = supert;
		checkRep();
	}

	//Checks this object's rep invariant.
	private void checkRep(){
		if(!CHECK_REP){
			return;
		}
		if(numberOfPlayers < 0){
			throw new IllegalStateException();
		}
		if(supertypes == null){
			throw new IllegalStateException();
		}
	}

	/**Returns true if and only if pt is a supertype of this PlayType.
	@param playType The PlayType to test.
	@throws NullPointerException if pt is null.
	@return a boolean indicating whether pt is a supertype of this PlayType.
	*/
	public boolean hasSupertype(PlayType playType){
		if(playType == null){
			throw new NullPointerException();
		}
		if(this == playType){
			return true;
		}
		for(PlayType pt : supertypes){
			if(pt.hasSupertype(playType)){
				return true;
			}
		}
		return false;
	}

	/**Returns the number of players that are involved in plays of this PlayType.
	@return the number of players that are involved in plays of this PlayType.
	*/
	public int getNumberOfPlayers(){
		return numberOfPlayers;
	}

	/**Returns the PlayType corresponding to this String, or null if no such PlayType exists. <br>
	This is equivalent to calling PlayType.valueOf(), except this method is case insensitive, and underscores can be replace with spaces.<br>
	Other than case and underscores/spaces, the input string must match a PlayType constant exactly to be returned.
	@param input The String to parse.
	@throws NullPointerException if input is null.
	@return the PlayType corresponding to this String, or null if no such PlayType exists.
	*/
	public static PlayType parse(String input){
		try {
			return PlayType.valueOf(input.toUpperCase().replace(" ", "_"));
		} catch(IllegalArgumentException e){
			return null;
		}
	}

	@Override
	/**Returns true if p is of this PlayType, or of a supertype of this PlayType.
	@param p The play to examine.
	@throws NullPointerException if p is null.
	@return a boolean representing whether p is of this PlayType, or of a supertype of this PlayType.
	*/
	public boolean satisfiedBy(Play p){
		return p.getType().hasSupertype(this);
	}

	@Override
	/**Returns a String representation for this PlayType.
	@return a String representation for this PlayType.
	*/
	public String toString(){
		return this.name().toLowerCase().replace("_", " ");
	}
}