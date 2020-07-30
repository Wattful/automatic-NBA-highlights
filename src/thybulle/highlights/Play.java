package thybulle.highlights;

import java.util.*;
import java.io.*;
import thybulle.misc.*;

//TODO:

/**Immutable class representing a play, with the ability to get a video of the play.
@author Owen Kulik
*/

public abstract class Play {
	private static final boolean CHECK_REP = true;

	private final PlayType pt;
	private final List<Player> p;
	private final Timestamp t;
	private final Team te;
	private final Score s;

	//RI: No fields (except video) are null, p.length == pt.getNumberOfPlayers()
	//AF: Represents a play. The play was made by player p, is of type pt, at time t, and calling getVideo returns a video of the play.
	//Note that v has different functions depeding on what implementation is being used.

	/**Constructs a Play with the specified fields.
	@throws NullPointerException if any arguments are null.
	*/
	protected Play(PlayType playType, Timestamp timestamp, Team team, Score score, Player... player){
		if(playType == null || player == null || timestamp == null || team == null || score == null){
			throw new NullPointerException("A field was null.");
		}
		if(player.length != playType.getNumberOfPlayers()){
			throw new IllegalArgumentException("Number of players did not match the number required for the play type.");
		}
		this.pt = playType;
		this.p = List.of(player);
		this.t = timestamp;
		this.te = team;
		this.s = score;
		checkRep();
	}

	//Checks this Play's rep invariant.
	private void checkRep(){
		if(!CHECK_REP){
			return;
		}
		if(p.size() != pt.getNumberOfPlayers()){
			throw new IllegalStateException();
		}
		if(pt == null || p == null || t == null || te == null){
			throw new IllegalStateException("A field was null.");
		}
	}

	/**Returns a video object containing footage of this play.
	@return a video object containing footage of this play. 
	*/
	public abstract Video getVideo() throws IOException;

	/**Returns the type of this play.
	@return the type of this play.
	*/
	public PlayType getType(){
		return this.pt;
	}

	/**Returns the players that are associated with this play.
	@return the players that are associated with this play.
	*/
	public List<Player> getPlayers(){
		return this.p;
	}

	/**Returns the time that this play occured at.
	@return the time that this play occured at.
	*/
	public Timestamp getTimestamp(){
		return this.t;
	}

	/**Returns the team that committed this play.
	@return the team that committed this play.
	*/
	public Team getTeam(){
		return this.te;
	}

	/**Returns the current score at the time of this play.
	@return the current score at the time of this play.
	*/
	public Score getScore(){
		return this.s;
	}

	@Override
	/**Returns a hash code for this Play.
	@return a hash code for this Play.
	*/
	public int hashCode(){
		return this.pt.hashCode() + this.p.hashCode() + this.t.hashCode() + this.te.hashCode() + this.s.hashCode();
	}

	@Override
	/**Returns a boolean indicating whether the Play is equal to the provided object. <br>
	The Plays are considered equal if all circumstances (play type, player, time remaining) are the same.<br>
	The video of the play is not taken into account.
	@param o The object to compare to.
	@return a boolean indicating whether the Play is equal to the provided object.
	*/
	public boolean equals(Object o){
		if(o == null){
			return false;
		}
		if(this == o){
			return true;
		}
		if(!(o instanceof Play)){
			return false;
		}
		Play pl = (Play)o;
		return this.pt.equals(pl.pt) && this.p.equals(pl.p) && this.t.equals(pl.t) && this.te.equals(pl.te) && this.s.equals(pl.s);
	}

	@Override
	/**Returns a String representation of this Play.
	@return a String representation of this Play.
	*/
	public String toString(){
		return this.pt.toString() + " by " + (this.p.size() > 0 ? (this.p.toString() + " of ") : "") + this.te.toString() + " at " + this.t.toString();
	}
}