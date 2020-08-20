package thybulle.highlights;

import java.util.regex.Pattern;
import java.util.*;
import thybulle.misc.*;

/**Immutable class representing an interval of time in a game, implemented as a beginning Timestamp and an end Timestamp.
@author Owen Kulik
*/

public class TimeInterval implements Constraint {
	private static final String separator = "\\s*\\-\\s*";
	private static final String timestampPattern = "\\d{1,2}:\\d\\d\\s+\\d\\w\\w";
	private static final String timeIntervalPattern = timestampPattern + separator + timestampPattern;

	private final Timestamp beginning;
	private final Timestamp end;

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

	/**Parses a TimeInterval from a String. The specification for the input can be found in the config README file under the time constraint.
	@param constraint The String to parse
	@throws NullPointerException if constraint is null
	@throws IllegalArgumentException if the given String could not be parsed
	@return the parsed TimeInterval.
	*/
	public static TimeInterval parse(String constraint){
		if(constraint.equals("ot")){
			//An assumption is made that there will be no overtime past the 11th overtime.
			return new TimeInterval(new Timestamp(5, 300), new Timestamp(15, 0));
		} else if(Pattern.matches(timeIntervalPattern, constraint)){
			String[] patternSplit = constraint.split(separator);
			String firstTimestamp = patternSplit[0];
			String secondTimestamp = patternSplit[1];
			return new TimeInterval(parseTimestamp(firstTimestamp), parseTimestamp(secondTimestamp));
		} else {
			int quarter;
			try {
				quarter = Timestamp.parseQuarter(constraint);
			} catch(IllegalArgumentException e) {
				throw new IllegalArgumentException("Malformed quarter: " + constraint, e);
			}
			
			if(quarter > 4){
				return new TimeInterval(new Timestamp(quarter, 300), new Timestamp(quarter, 0));
			} else if(quarter > 0){
				return new TimeInterval(new Timestamp(quarter, 720), new Timestamp(quarter, 0));
			} else {
				throw new IllegalArgumentException("Could not parse timestamp: " + constraint);
			}
		}
	}

	//Parses a timestamp from a string.
	private static Timestamp parseTimestamp(String constraint){
		try{
			return Timestamp.parse(constraint);
		} catch(IllegalArgumentException e){
			throw new IllegalArgumentException("Malformed timestamp : " + constraint, e);
		}
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