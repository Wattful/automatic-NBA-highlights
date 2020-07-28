package thybulle.highlights;

/**Constraint representing this team's relative score, or this team's score minus the other team's score.
@author Owen Kulik
*/

public class RelativeScoreConstraint implements Constraint {
	private static final String SEPARATOR = "\\s*~\\s*";

	private final int firstLimit;
	private final int secondLimit;

	/**Constructs a RelativeScoreConstraint which is fulfilled if the relative score is exactly the given score.
	@param relativeScore The given score.
	*/
	public RelativeScoreConstraint(int relativeScore){
		this(relativeScore, relativeScore);
	}

	/**Constructs a RelativeScoreConstraint which is fulfilled if the score is above or below the given score, depending on the boolean parameter.<br>
	If the given boolean is true, it is fulfilled if the score is equal to or above the given score, otherwise equal to or below.
	@param relativeScore the given score
	@param up the given boolean
	*/
	public RelativeScoreConstraint(int relativeScore, boolean up){
		this(relativeScore, up ? Integer.MAX_VALUE : Integer.MIN_VALUE);
	}
	
	/**Constructs a RelativeScoreConstraint which is fulfilled if the score is between the given limits, inclusive.<br>
	It does not matter which limit is higher than the other limit.
	@param oneLimit the first limit.
	@param otherLimit the second limit.
	*/
	public RelativeScoreConstraint(int oneLimit, int otherLimit){
		this.firstLimit = oneLimit;
		this.secondLimit = otherLimit;
	}

	/**Returns this RelativeScoreConstraint's lower limit.
	@return this RelativeScoreConstraint's lower limit.
	*/
	public int getLowerLimit(){
		return this.firstLimit < this.secondLimit ? this.firstLimit : this.secondLimit;
	}


	/**Returns this RelativeScoreConstraint's upper limit.
	@return this RelativeScoreConstraint's upper limit.
	*/
	public int getUpperLimit(){
		return this.firstLimit > this.secondLimit ? this.firstLimit : this.secondLimit;
	}

	@Override
	/**Returns true if and only if the given Play's relative score is within this RelativeScoreConstraint's limits.
	@param p the Play to examine.
	@throws NullPointerException if p is null
	@return a boolean indicating whether the given Play's relative score is within this RelativeScoreConstraint's limits.
	*/
	public boolean satisfiedBy(Play p){
		int relativeScore = p.getScore().getRelativeScore();
		return (relativeScore <= this.firstLimit && relativeScore >= secondLimit) || (relativeScore >= this.firstLimit && relativeScore <= this.secondLimit);
	}

	/**Parses and returns a RelativeScoreConstraint from the given String.<br>
	The specification for this can be found in the config README file under the score constraint.
	@param constraint The string to parse from
	@throws NullPointerException if constraint is null
	@throws IllegalArgumentExcpetion if a RelativeScoreConstraint could not be parsed from the given String.
	@return a RelativeScoreConstraint parsed from the given String.
	*/
	public static RelativeScoreConstraint parse(String constraint){
		String[] split = constraint.split(SEPARATOR);
		if(split.length < 2){
			if(split[0].charAt(split[0].length() - 1) == '-'){
				String inTigers = split[0].substring(0, split[0].length() - 1);
				return new RelativeScoreConstraint(Integer.valueOf(inTigers), false);
			} else if(split[0].charAt(split[0].length() - 1) == '+'){
				String inTigers = split[0].substring(0, split[0].length() - 1);
				return new RelativeScoreConstraint(Integer.valueOf(inTigers), true);
			} else {
				return new RelativeScoreConstraint(Integer.valueOf(split[0]));
			}
		} else {
			return new RelativeScoreConstraint(Integer.valueOf(split[0]), Integer.valueOf(split[1]));
		}
	}

	@Override
	/**Returns a hash code for this RelativeScoreConstraint.
	*/
	public int hashCode(){
		return this.firstLimit + this.secondLimit;
	}

	@Override
	/**Returns a boolean indicating if the given object is equal to this RelativeScoreConstraint.<br>
	Will return true if and only if the given object is a RelativeScoreConstraint and has the same limits.
	@param o The object to compare to.
	@return a boolean indicating if the given object is equal to this RelativeScoreConstraint.
	*/
	public boolean equals(Object o){
		if(o == null){
			return false;
		}
		if(this == o){
			return true;
		}
		if(!(o instanceof RelativeScoreConstraint)){
			return false;
		}
		RelativeScoreConstraint rcs = (RelativeScoreConstraint)o;
		return this.firstLimit == rcs.firstLimit && this.secondLimit == rcs.secondLimit;
	}

	@Override
	/**Returns a String representation of this RelativeScoreConstraint.
	@return a String representation of this RelativeScoreConstraint.
	*/
	public String toString(){
		String baseString = "RelativeScoreConstraint: ";
		String endString;
		if(firstLimit == secondLimit){
			endString = "" + firstLimit;
		} else if(secondLimit == Integer.MAX_VALUE){
			endString = firstLimit + "+";
		} else if(secondLimit == Integer.MIN_VALUE){
			endString = firstLimit + "-";
		} else {
			endString = firstLimit + "~" + secondLimit;
		}
		return baseString + endString;
	}
}