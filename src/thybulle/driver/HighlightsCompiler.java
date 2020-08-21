package thybulle.driver;

import java.util.*;
import java.io.*;
import thybulle.highlights.*;
import thybulle.misc.*;

/**Class which compiles highlights.<br>
It takes as input games to get highlights from and contraints to apply to those games.<br>
From these inputs, it compiles a Highlights object with a Video of all plays in the source games that meet the given constraints.
@author Owen Kulik
*/

public class HighlightsCompiler {
	//All fields cannot be null, initialized in constructor. Size > 1 indicates OR, any can match.
	//Represents all games to get highlights from. Must have size > 0 in order to compile.
	private final List<Game> sourceGames;
	private final Collection<Constraint> constraintSet;

	/**Constructs a HighlightsCompiler without any constraints or source games.
	 */
	HighlightsCompiler(){
		sourceGames = new ArrayList<Game>();
		constraintSet = new HashSet<Constraint>();
	}

	/**Adds the specified games to the compiler. Nulls are ignored.
	@param g The games to get highlights from.
	@return this, for method call chaining.
	*/
	public HighlightsCompiler addGames(Collection<? extends Game> g){
		List<Game> games = new ArrayList<Game>(g);
		games.removeAll(Collections.singleton(null));
		sourceGames.addAll(games);
		return this;
	}

	/**Adds the specified constraints to this HighlightsCompiler. Nulls are ignored.
	@param c The constraints to add.
	@return this, for method call chaining.
	*/
	public HighlightsCompiler addConstraints(Collection<? extends Constraint> c){
		List<Constraint> constraints = new ArrayList<Constraint>(c);
		constraints.removeAll(Collections.singleton(null));
		constraintSet.addAll(constraints);
		return this;
	}

	/**Compiles and returns a Highlights object.<br>
	This highlights object contains all plays in this HighlightCompiler's games that satisfy the provided constraints.
	@return a Highlights object containing all plays in this HighlightCompiler's games that satisfy the provided constraints.
	*/
	public Highlights compile(){
		if(sourceGames.isEmpty()){
			throw new IllegalStateException("No source games were added.");
		}
		if(constraintSet.isEmpty()){
			throw new IllegalStateException("No constraints were specified.");
		}
		List<Play> plays = new ArrayList<Play>();
		for(Game g : sourceGames){
			plays.addAll(g.getAllPlaysThatSatisfy(constraintSet));
		}
		return new Highlights(plays);
	}
}