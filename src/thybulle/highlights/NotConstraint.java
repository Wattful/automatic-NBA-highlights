package thybulle.highlights;

//TODO:

/**Immutable constraint representing the opposite of a given constraint.<br>
For example, if a NotConstraint is constructed with constraint A,
then a play will satisfy this NotConstraint if and only if it does not satisfy A.
@author Owen Kulik
*/

public class NotConstraint implements Constraint {
	private static final boolean CHECK_REP = true;

	private final Constraint constraint;

	//RI: constraint != null
	//AF: represents the opposite of a given constraint. constraint is the given constraint.

	/**Constructs a NotConstraint with the given constraint.
	@param c The constraint.
	@throws NullPointerException if c is null.
	*/
	public NotConstraint(Constraint c){
		if(c == null){
			throw new NullPointerException();
		}
		constraint = c;
		checkRep();
	}

	//Checks this object's rep invariant.
	private void checkRep(){
		if(!CHECK_REP){
			return;
		}
		if(constraint == null){
			throw new IllegalStateException();
		}
	}

	/**Returns the constraint contained in this NotConstraint.
	@return the constraint contained in this NotConstraint.
	*/
	public Constraint getConstraint(){
		return constraint;
	}

	@Override
	/**Returns true if the given play does not satisfy the constraint.
	@param p The play to examine.
	@return a boolean indicating if the given play does not satisfy the constraint.
	*/
	public boolean satisfiedBy(Play p){
		return !constraint.satisfiedBy(p);
	}

	@Override
	/**Returns a hash code for this object.
	@return a hash code for this object.
	*/
	public int hashCode(){
		return -constraint.hashCode();
	}

	@Override
	/**Returns a boolean indicating whether these two objects are equal.<br>
	They are considered equal if o is a NotConstraint containing the same constraint.
	@param o The object to compare to.
	@return a boolean indicating whether these two objects are equal.
	*/
	public boolean equals(Object o){
		if(o == null){
			return false;
		}
		if(this == o){
			return true;
		}
		if(!(o instanceof NotConstraint)){
			return false;
		}
		NotConstraint nc = (NotConstraint)o;
		return this.constraint.equals(nc.constraint);
	}

	@Override
	/**Returns a String representation of this NotConstraint.
	@return a String representation of this NotConstraint.
	*/
	public String toString(){
		return "( NOT " + constraint.toString() + " )";
	}

}