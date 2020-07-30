package thybulle.highlights;

/**Immutable class representing a Game's score at a moment in time.<br>
This class, instead of taking the perspective of a home and away team, takes the perspective of "this team" and "the other team".<br>
This is so that in Play objects the team that committed the play will receive a copy of the score from its perspective.
@author Owen Kulik
*/

public class Score {
	private final int thisTeamsPoints;
	private final int otherTeamsPoints;

	/**Constructs a Score with the given values.
	@param thisTeamsPoints "this team's" score.
	@param otherTeamsPoints "the other team's" score.
	@throws IllegalArgumentException if either argument is negative.
	*/
	public Score(int thisTeamsPoints, int otherTeamsPoints){
		if(thisTeamsPoints < 0 || otherTeamsPoints < 0){
			throw new IllegalArgumentException("A point value was negative.");
		}
		this.thisTeamsPoints = thisTeamsPoints;
		this.otherTeamsPoints = otherTeamsPoints;
	}

	/**Returns "this team's" point value.
	@return "this team's" point value.
	*/
	public int getThisTeamsPoints(){
		return this.thisTeamsPoints;
	}

	/**Returns "the other team's" point value.
	@return "the other team's" point value.
	*/
	public int getOtherTeamsPoints(){
		return this.otherTeamsPoints;
	}

	/**Returns "this team's" relative score.
	@return "this team's" score minus "the other team's" score.
	*/
	public int getRelativeScore(){
		return this.thisTeamsPoints - this.otherTeamsPoints;
	}

	/**Returns a Score reflecting this Score from the other team's perspective.
	@return a Score reflecting this Score from the other team's perspective.
	*/
	public Score reverseScore(){
		return new Score(this.otherTeamsPoints, this.thisTeamsPoints);
	}

	/**Returns a Score with the given value added to this team's point value.
	@throws IllegalArgumentException if the value would result in a team having negative points.
	@return a Score with the given value added to this team's point value.
	*/
	public Score addToThisTeamsPoints(int points){
		return new Score(this.thisTeamsPoints + points, this.otherTeamsPoints);
	}

	/**Returns a Score with the given value added to the other team's point value.
	@throws IllegalArgumentException if the value would result in a team having negative points.
	@return a Score with the given value added to the other team's point value.
	*/
	public Score addToOtherTeamsPoints(int points){
		return new Score(this.thisTeamsPoints, this.otherTeamsPoints + points);
	}

	/**Parses and returns a Score from the given String.<br>
	The String must consist of two integers separated by "to", with any amount of whitespace before and after the separator.<br>
	@param input the String input
	@throws NullPointerException if input is null.
	@throws IllegalArgumentException if the input could not be parsed.
	@return the parsed Score.
	*/
	public static Score parse(String input){
		String separator = "\\s*to\\s*";
		String[] split = input.split(separator, 2);
		if(split.length < 2){
			throw new IllegalArgumentException("Could not parse score: " + input);
		}
		int ourScore = Integer.valueOf(split[0]);
		int theirScore = Integer.valueOf(split[1]);
		return new Score(ourScore, theirScore);
	}

	@Override
	/**Returns a hash code for this Score.
	@return a hash code for this Score.
	*/
	public int hashCode(){
		return this.thisTeamsPoints + this.otherTeamsPoints;
	}

	@Override
	/**Returns a boolean indicating if the given object is equal to this Score.<br>
	Will return true if and only if the given object is a Score and has the same point values.
	@param o The object to compare to.
	@return a boolean indicating if the given object is equal to this Score.
	*/
	public boolean equals(Object o){
		if(o == null){
			return false;
		}
		if(this == o){
			return true;
		}
		if(!(o instanceof Score)){
			return false;
		}
		Score s = (Score)o;
		return this.thisTeamsPoints == s.thisTeamsPoints && this.otherTeamsPoints == s.otherTeamsPoints;
	}

	@Override
	/**Returns a String representing this Score object.<br>
	This toString method is compatible with this class' parse method.
	In other words, for any Score s, s.equals(Score.parse(s.toString())) will be true.
	@return a String representing this Score object.
	*/
	public String toString(){
		return this.thisTeamsPoints + " to " + this.otherTeamsPoints;
	}
}