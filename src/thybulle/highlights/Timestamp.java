package thybulle.highlights;

//TODO:

/**Immutable class representing a time in a game, including the quarter and the time remaining in the quarter.
@author Owen Kulik
*/

public class Timestamp implements Comparable<Timestamp> {
	private static final boolean CHECK_REP = true;

	private final int quarter;
	private final int timeRemaining;

	//RI: quarter > 0 and 0 <= timeRemaining <= 720 if quarter <= 4, otherwise 0 <= timeRemaining <= 300.
	/*AF: Represents a time in a game. 
	Quarter = 1-4 corresponds to the first through fourth quarters, higher values correspond to overtimes: 5 = ot, 6 = 2ot, so on.
	timeRemaining is the number of seconds remaining in the quarter.
	*/

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
		checkRep();
	}

	//Checks the Timestamp's rep invariant.
	private void checkRep(){
		if(!CHECK_REP){
			return;
		}
		if(quarter <= 0){
			throw new IllegalStateException("Quarter was non positive.");
		} else if(timeRemaining < 0){
			throw new IllegalStateException("Time remaining was negative.");
		} else if(quarter <= 4 && timeRemaining > 720){
			throw new IllegalStateException("There was greater than 12 minutes in the quarter.");
		} else if(quarter > 4 && timeRemaining > 300){
			throw new IllegalStateException("There was greater than 5 minutes in the overtime period.");
		}
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
		if(quarter == 1){
			ordinal = "1st";
		} else if(quarter == 2){
			ordinal = "2nd";
		} else if(quarter == 3){
			ordinal = "3rd";
		} else if(quarter == 4){
			ordinal = "4th";
		} else {
			ordinal = (quarter - 4) + "OT";
		}
		return ordinal + " " + (timeRemaining/60) + ":" + (String.format("%02d", timeRemaining%60));
	}
}