package thybulle.highlights;

import java.util.*;
import thybulle.misc.*;

//TODO:

/**Immutable class representing a team. <br>
This class uses interning, and as such no constructors are public. <br>
If one wishes to "construct" a team, they should use the {@code getNBATeam(String)} method.<br>
This class has a hard-coded list of the names of all NBA teams, as well as all team names from the All-Star game and the Rising Stars challenge since 2012.<br>
Here is a list of all such team names:
<ul>
	<li>Current NBA Teams
		<ul>
			<li>Atlanta Hawks</li>
			<li>Boston Celtics</li>
			<li>Brooklyn Nets</li>
			<li>Charlotte Hornets</li>
			<li>Chicago Bulls</li>
			<li>Cleveland Cavaliers</li>
			<li>Dallas Mavericks</li>
			<li>Denver Nuggets</li>
			<li>Detroit Pistons</li>
			<li>Golden State Warriors</li>
			<li>Houston Rockets</li>
			<li>Indiana Pacers</li>
			<li>LA Clippers</li>
			<li>Los Angeles Lakers</li>
			<li>Memphis Grizzlies</li>
			<li>Miami Heat</li>
			<li>Milwaukee Bucks</li>
			<li>Minnesota Timberwolves</li>
			<li>New Orleans Pelicans</li>
			<li>New York Knicks</li>
			<li>Oklahoma City Thunder</li>
			<li>Orlando Magic</li>
			<li>Philadelphia 76ers</li>
			<li>Phoenix Suns</li>
			<li>Portland Trail Blazers</li>
			<li>Sacramento Kings</li>
			<li>San Antonio Spurs</li>
			<li>Toronto Raptors</li>
			<li>Utah Jazz</li>
			<li>Washington Wizards</li>
		</ul>
	</li>
	<li>Former NBA Teams (since 2012)
		<ul>
			<li>Charlotte Bobcats</li>
			<li>New Orleans Hornets</li>
		</ul>
	</li>
	<li>NBA All-Star Teams
		<ul>
			<li>East</li>
			<li>West</li>
			<li>Team LeBron</li>
			<li>Team Stephen</li>
			<li>Team Giannis</li>
		</ul>
	</li>
	<li>NBA Rising Stars Challenge Teams
		<ul>
			<li>Team Shaq</li>
			<li>Team Chuck</li>
			<li>Team Webber</li>
			<li>Team Hill</li>
			<li>World</li>
			<li>USA</li>
		</ul>
	</li>
</ul>
@author Owen Kulik
*/

public class Team implements Constraint, Comparable<Team> {
	private static final boolean CHECK_REP = true;

	private final String teamName;

	private static final Map<String, Team> interning = new HashMap<String, Team>();

	private static final Set<String> nbaTeams = Set.of("atlanta hawks", "boston celtics", "brooklyn nets", "charlotte hornets", "chicago bulls", "cleveland cavaliers",
			"dallas mavericks", "denver nuggets", "detroit pistons", "golden state warriors", "houston rockets", "indiana pacers", "la clippers", "los angeles lakers",
			"memphis grizzlies", "miami heat", "milwaukee bucks", "minnesota timberwolves", "new orleans pelicans", "new york knicks", "oklahoma city thunder",
			"orlando magic", "philadelphia 76ers", "phoenix suns", "portland trail blazers", "sacramento kings", "san antonio spurs", "toronto raptors", "utah jazz",
			"washington wizards", "charlotte bobcats", "new orleans hornets", "team lebron", "team giannis", "team stephen", "east", "west", "world", "usa", "team shaq", 
			"team chuck", "team webber", "team hill");

	//RI: No fields are null.
	//AF: Represents a team with name teamName.

	//Constructs a team with name name.
	private Team(String name){
		teamName = name;
		checkRep();
	}

	//Checks this object's rep invariant.
	private void checkRep(){
		if(!CHECK_REP){
			return;
		}
		if(teamName == null){
			throw new IllegalStateException("A field was null.");
		}
	}

	/**Returns the NBA team with the given name, or null if no such team exists.<br>
	A full list of NBA teams and their official names can be found in the description of this class.
	@param name The team name.
	@throws NullPointerException if name is null.
	@return the NBA team with the given name, or null if no such team exists.
	*/
	public static Team getNBATeam(String name){
		if(name == null){
			throw new NullPointerException();
		}
		name = name.toLowerCase();
		if(nbaTeams.contains(name)){
			return get(name);
		} else {
			return null;
		}
	}

	/**Returns a team with name name.<br>
	This method returns a canonical instance of a team. If a team with this name has not been created yet, it will be created and returned.<br>
	If one has been created, a reference to the already created team is returned.<br>
	This should be used as the "constructor" for Team.
	@param name the team's name.
	@throws NullPointerException if name is null.
	@return a canonical representation of a team.
	*/
	static Team get(String name){
		if(name == null){
			throw new NullPointerException();
		}
		name = name.toLowerCase();
		Team t = interning.get(name);
		if(t == null){
			Team answer = new Team(name);
			interning.put(name, answer);
			return answer;
		} else {
			return t;
		}
	}

	/**Returns this Team's name.
	@return this Team's name.
	*/
	public String name(){
		return this.teamName;
	}

	@Override
	/**Compares this Team to the specified team, using the teams' names.
	@param other The team to compare this to.
	@throws NullPointerException if other is null.
	@return a negative number if this is "less than" other, 0 if this is equal to other, and a positive number if this is "greater than" other.
	*/
	public int compareTo(Team other){
		if(this == other){
			return 0;
		}
		int nameCompare = this.name().compareTo(other.name());
		if(nameCompare != 0){
			return nameCompare;
		}
		//This should never happen.
		throw new AssertionError("Two different instances of the same team existed.");
	}

	@Override
	/**Returns true if and only if the play was committed by this team.
	@param p the play to consider.
	@return a boolean indicating whether the play was committed by this team.
	*/
	public boolean satisfiedBy(Play p){
		return this.equals(p.getTeam());
	}

	@Override
	/**Returns a String representing this team.
	@return a String representing this team.
	*/
	public String toString(){
		return this.name();
	}
}