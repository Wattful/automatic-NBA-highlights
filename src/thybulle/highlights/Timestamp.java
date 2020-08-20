package thybulle.highlights;

import java.util.*;

/**Immutable class representing a time in a game, including the quarter and the time remaining in the quarter.
@author Owen Kulik
*/

public class Timestamp implements Comparable<Timestamp> {
	private static final List<String> quarterNames = List.of("1st", "2nd", "3rd", "4th");

	private final int quarter;
	private final int timeRemaining;

	/**Constructs a new Timestamp with the specified quarter and time remaining.<br>
	quar = 1-4 corresponds to the first through fourth quarters, higher values correspond to overtimes: 5 = ot, 6 = 2ot, so on.<br>
	time is the time remaining in the quarter in seconds.<br>
	As such, if quar = 1-4, time cannot be greater than 720 (12 minutes), and if quar > 4, time cannot be greater than 300 (5 minutes)<br>
	A violation of these requirements will result in an IllegalArgumentException.
	@throws IllegalArgumentException if the requirements are violated.
	*/
	public Timestamp(int quar, int time){
		if(quar <= 0){
			throw new IllegalArgumentException("Quarter was non positive.");
		} else if(time < 0){
			throw new IllegalArgumentException("Time remaining was negative.");
		} else if(quar <= 4 && time > 720){
			throw new IllegalArgumentException("There was greater than 12 minutes in the quarter.");
		} else if(quar > 4 && time > 300){
			throw new IllegalArgumentException("There was greater than 5 minutes in the overtime period.");
		}
		quarter = quar;
		timeRemaining = time;
	}

	/**Returns this timestamp's quarter.
	@return this timestamp's quarter. <br>
	Quarter = 1-4 corresponds to the first through fourth quarters, higher values correspond to overtimes: 5 = ot, 6 = 2ot, so on.
	*/
	public int getQuarter(){
		return quarter;
	}

	/**Returns the time remaining in this quarter, in seconds.
	@return the time remaining in this quarter, in seconds.
	*/
	public int getTimeRemaining(){
		return timeRemaining;
	}

	/**Parses and returns a Timestamp from the given String.<br>
	The String MUST conform to the following format: MINUTES:SECONDS QUARTER.<br>
	Minutes and seconds can be any valid integer. 
	Valid quarter strings are discussed in the  description of the parseQuarter method.
	@param input The String to parse.
	@throws NullPointerException if input is null.
	@throws NumberFormatException if the minutes or seconds are formatted incorrectly.
	@throws IllegalArgumentException if the time is negative, or there is too much time for an NBA quarter.
	@return a Timestamp parsed from the given String.
	*/
	public static Timestamp parse(String input){
		String[] timestampSplit = input.split("\\s+");
		String timestamp = timestampSplit[0];
		String quarterString = timestampSplit[1];
		String[] timeSplit = timestamp.split(":");
		String minutesString = timeSplit[0];
		String secondsString = timeSplit[1];
		int minutes = Integer.parseInt(minutesString);
		int seconds = Integer.parseInt(secondsString);
		int quarter = parseQuarter(quarterString);
		//System.out.println(input);
		//System.out.println(quarterString);
		//System.out.println(quarter);
		return new Timestamp(quarter, (minutes * 60) + seconds);
	}

	/**Parses and returns an int representing a quarter from the given String.<br>
	1-4 represent the first through fourth quarters, with 5 representing the first overtime, 6 representing secodn overtime, and so on.<br>
	"1st", "2nd", "3rd", and "4th" are interpreted as the four quarters.<br>
	"1ot", "2ot", and so on are interpreted as overtime periods. Simply "ot" is NOT accepted.<br>
	Input for this method is case insensitive.
	@param input The String to parse.
	@throws NullPointerExcpeption if input is null.
	@throws IllegalArgumentException if input does not represent a quarter according to the above specification.
	@return an int representing a quarter.
	*/
	public static int parseQuarter(String input){
		try{
			return Integer.parseInt(input);
		} catch(NumberFormatException e){}
		if(input.substring(input.length() - 2).toLowerCase().equals("ot")){
			int overtime; 
			try{
				overtime = Integer.parseInt(input.substring(0, input.length() - 2));
			} catch(NumberFormatException e){
				throw new IllegalArgumentException("Unknown overtime period: " + input);
			}
			if(overtime <= 0){
				throw new IllegalArgumentException("Overtime period was below 1.");
			}
			return overtime + 4;
		} else if(quarterNames.contains(input)){
			return quarterNames.indexOf(input) + 1;
		} else {
			throw new IllegalArgumentException("Malformed quarter: " + input);
		}
	}

	@Override
	/**Compares the time remaining in the timestamp objects.  <br>
	The timestamp with more time remaining in the game is considered to be smaller.
	@return an int comparing the timestamp objects, according to the Comparable specification.
	*/
	public int compareTo(Timestamp other){
		if(this.quarter != other.quarter){
			return Integer.compare(this.quarter, other.quarter);
		}
		return -Integer.compare(this.timeRemaining, other.timeRemaining);
	}

	@Override
	/**Returns a hash code for this Timestamp.
	@return a hash code for this Timestamp.
	*/
	public int hashCode(){
		return quarter + timeRemaining;
	}

	@Override
	/**Returns a boolean indicating whether this Timestamp is equal to o.<br>
	The timestamp is considered equal to another if they are in teh same quarter and have the same time remaining.
	@param o The object to compare to.
	@return a boolean indicating whether this Timestamp is equal to o.
	*/
	public boolean equals(Object o){
		if(o == null){
			return false;
		}
		if(this == o){
			return true;
		}
		if(!(o instanceof Timestamp)){
			return false;
		}
		Timestamp t = (Timestamp)o;
		return this.quarter == t.quarter && this.timeRemaining == t.timeRemaining;
	}

	@Override
	/**Returns a String representing this Timestamp.
	@return a String representing this Timestamp.
	*/
	public String toString(){
		String ordinal;
		if(quarter < 5){
			ordinal = quarterNames.get(quarter - 1);
		} else {
			ordinal = (quarter - 4) + "OT";
		}
		return (timeRemaining/60) + ":" + (String.format("%02d", timeRemaining%60)) + " " + ordinal;
	}
}