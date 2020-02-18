package thybulle.highlights;

import java.util.*;
import java.io.*;
import thybulle.misc.*;

//TODO: implement getVideo

/**Immutable class representing a play, with the ability to get a video of the play.
@author Owen Kulik
*/

public abstract class Play {
	private static final boolean CHECK_REP = true;

	private final PlayType pt;
	private final Player[] p;
	private final Timestamp t;
	private final Team te;

	//RI: No fields (except video) are null, p.length == pt.getNumberOfPlayers()
	//AF: Represents a play. The play was made by player p, is of type pt, at time t, and calling getVideo returns a video of the play.
	//Note that v has different functions depeding on what implementation is being used.

	/**Constructs a Play with the specified fields.
	@throws NullPointerException if any arguments are null.
	*/
	public Play(PlayType playType, Timestamp timestamp, Team team, Player... player){
		if(playType == null || player == null || timestamp == null || team == null){
			throw new NullPointerException("A field was null.");
		}
		if(player.length != playType.getNumberOfPlayers()){
			throw new IllegalArgumentException("Number of players did not match the number required for the play type.");
		}
		pt = playType;
		p = player;
		t = timestamp;
		te = team;
		checkRep();
	}

	//Checks this Play's rep invariant.
	private void checkRep(){
		if(!CHECK_REP){
			return;
		}
		if(p.length != pt.getNumberOfPlayers()){
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
		return pt;
	}

	/**Returns the players that are associated with this play.
	@return the players that are associated with this play.
	*/
	public List<Player> getPlayers(){
		return Collections.unmodifiableList(Arrays.asList(p));
	}

	/**Returns the time that this play occured at.
	@return the time that this play occured at.
	*/
	public Timestamp getTimestamp(){
		return t;
	}

	/**Returns the team that committed this play.
	@return the team that committed this play.
	*/
	public Team getTeam(){
		return te;
	}

	@Override
	/**Returns a hash code for this Play.
	@return a hash code for this Play.
	*/
	public int hashCode(){
		return pt.hashCode() + p.hashCode() + t.hashCode() + te.hashCode();
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
		return this.pt.equals(pl.pt) && this.p.equals(pl.p) && this.t.equals(pl.t) && this.te.equals(pl.te);
	}

	@Override
	/**Returns a String representation of this Play.
	@return a String representation of this Play.
	*/
	public String toString(){
		return pt.toString() + " by " + p.toString() + " of " + te.toString() + " at " + t.toString();
	}
}