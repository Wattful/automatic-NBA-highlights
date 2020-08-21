package thybulle.highlights;

import java.util.*;

/**Constraint representing a group of constraints, evaluated conjunctively.<br>
For example, if a ConjunctiveConstraint is constructed with constraints A, B, and C, 
then a play must satisfy A, B, and C to satisfy this constraint.
@author Owen Kulik
*/

public class AndConstraint implements Constraint {
	private final Set<Constraint> constraints;

	/**Constructs a ConjunctiveConstraint consisting of the given constraints.
	@param c The constraints to use.
	@throws NullPointerException if c is null, or any parameter in c is null.
	@throws IllegalArgumentException if c is empty
	*/
	public AndConstraint(Collection<? extends Constraint> c){
		if(c.isEmpty()){
			throw new IllegalArgumentException("No constraints were provided.");
		}
		constraints = Set.copyOf(c);
	}

	/**Returns the number of constraints in this ConjunctiveConstraint.<br>
	@return the number of constraints in this ConjunctiveConstraint.
	*/
	public int numberOfConstraints(){
		return constraints.size();
	}

	/**Returns an unmodifiable set containing all constraints in this ConjunctiveConstraint.
	@return an unmodifiable set containing all constraints in this ConjunctiveConstraint.
	*/
	public Set<Constraint> getConstraints(){
		return constraints;
	}

	@Override
	/**Returns true if the given play satisfies all constraints in this ConjunctiveConstraint.
	@param p the play to examine
	@return a boolean indicating whether the given play satisfies a constraint in this ConjunctiveConstraint.
	*/
	public boolean satisfiedBy(Play p){
		for(Constraint c : constraints){
			if(!c.satisfiedBy(p)){
				return false;
			}
		}
		return true;
	}

	@Override
	/**Returns a hash code for this ConjunctiveConstraint.
	@return a hash code for this ConjunctiveConstraint.
	*/
	public int hashCode(){
		int answer = 0;
		for(Constraint c : constraints){
			answer += c.hashCode();
		}
		return answer;
	}

	@Override
	/**Returns a boolean indicating whether these objects are equal.<br>
	They are considered equal if the other object is a ConjunctiveConstraint and they have the same constraints.
	@param o The object to compare.
	@return a boolean indicating whether these objects are equal.
	*/
	public boolean equals(Object o){
		if(o == null){
			return false;
		}
		if(this == o){
			return true;
		}
		if(!(o instanceof AndConstraint)){
			return false;
		}
		AndConstraint dc = (AndConstraint)o;
		return this.constraints.equals(dc.constraints);
	}

	@Override
	/**Returns a String representing this ConjunctiveConstraint.
	@return a String representing this ConjunctiveConstraint.
	*/
	public String toString(){
		StringBuilder sb = new StringBuilder("(");
		for(Constraint c : constraints){
			sb.append(c.toString() + " AND ");
		}
		return sb.substring(0, sb.length() - 5) + ")";
	}
}