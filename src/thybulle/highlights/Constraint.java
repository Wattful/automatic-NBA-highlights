package thybulle.highlights;

/**Interface defining a constraint.<br>
A constraint is an ADT that can determine whether a play satisfies it.
@author Owen Kulik
*/

public interface Constraint {
	/**Returns true if and only if the play satisfies this class' constraint.
	@param p the Play to be examined.
	@return a boolean indicating whether the play satisfies this class' constraint.
	*/
	boolean satisfiedBy(Play p);
}