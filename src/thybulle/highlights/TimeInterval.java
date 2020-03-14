package thybulle.highlights;

import java.util.*;
import thybulle.misc.*;

//TODO:

/**Immutable class representing an interval of time in a game, implemented as a beginning Timestamp and an end Timestamp.
@author Owen Kulik
*/

public class TimeInterval implements Constraint {
	private static final boolean CHECK_REP = true;

	private final Timestamp beginning;
	private final Timestamp end;

	//RI: no fields are null, beginning.compareTo(end) <= 0
	//AF: Represents an interval of gametime beginning at beginning and ending at end.

	/**Constructs a TimeInterval beginning at first and ending at second.
	@param first The beginning of the TimeInterval.
	@param second The end of the TimeInterval.
	@throws IllegalArgumentException if first.compareTo(second) > 0
	@throws NullPointerException if either argument is null.
	*/
	public TimeInterval(Timestamp first, Timestamp second){
		if(first == null || second == null){
			throw new NullPointerException("An argument was null.");
		}
		if(first.compareTo(second) > 0){
			throw new IllegalArgumentException("First was after second.");
		}
		beginning = first;
		end = second;
		checkRep();
	}

	//Checks this object's rep invariant.
	private void checkRep(){
		if(!CHECK_REP){
			return;
		}
		if(beginning == null || end == null){
			throw new IllegalStateException("An field was null.");
		}
		if(beginning.compareTo(end) > 0){
			throw new IllegalStateException("Beginning was after end.");
		}
	}

	/**Returns the beginning of the TimeInterval.
	@return the beginning of the TimeInterval.
	*/
	public Timestamp getBeginning(){
		return beginning;
	}

	/**Returns the end of the TimeInterval.
	@return the end of the TimeInterval.
	*/
	public Timestamp getEnd(){
		return end;
	}

	/**Returns true if and only if the given timestap is within this TimeInterval.
	@param t The timestamp to consider.
	@return a boolean indicating whether the given timestap is within this TimeInterval.
	*/
	public boolean contains(Timestamp t){
		return beginning.compareTo(t) <= 0 && end.compareTo(t) >= 0;
	}

	@Override
	/**Returns true if and only if the play was occured in this TimeInterval.
	@param p The play to examine.
	@returns a boolean indicating whether the play was occured in this TimeInterval.
	*/
	public boolean satisfiedBy(Play p){
		return this.contains(p.getTimestamp());
	}

	@Override
	/**Returns a hash code for this TimeInterval.
	@return a hash code for this TimeInterval.
	*/
	public int hashCode(){
		return beginning.hashCode() + end.hashCode();
	}

	@Override
	/**Returns a boolean indicating whether these objects are equal.<br>
	They are considered equal if and only if they start and end at the send time.
	@param o The object to compare to.
	@return a boolean indicating whether these objects are equal.
	*/
	public boolean equals(Object o){
		if(o == null){
			return false;
		}
		if(this == o){
			return true;
		}
		if(!(o instanceof TimeInterval)){
			return false;
		}
		TimeInterval t = (TimeInterval)o;
		return this.beginning.equals(t.beginning) && this.end.equals(t.end);
	}

	@Override
	/**Returns a String that represents this TimeInterval.
	@return a String that represents this TimeInterval.
	*/
	public String toString(){
		return beginning.toString() + " - " + end.toString();
	}
}