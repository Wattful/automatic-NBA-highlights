package thybulle.highlights;

import java.util.*;
import java.io.*;
import thybulle.misc.*;

/**Immutable class representing a play, with the ability to get a video of the play.
@author Owen Kulik
*/

public abstract class Play {
	private static final boolean CHECK_REP = true;

	private final PlayType playType;
	private final List<Player> players;
	private final Timestamp timestamp;
	private final Team team;
	private final Score score;

	/**Constructs a Play with the specified fields.
	@throws NullPointerException if any arguments are null.
	*/
	protected Play(PlayType playType, Timestamp timestamp, Team team, Score score, Collection<? extends Player> player){
		if(playType == null || player == null || timestamp == null || team == null || score == null){
			throw new NullPointerException("A field was null.");
		}
		if(player.size() != playType.getNumberOfPlayers()){
			throw new IllegalArgumentException("Number of players did not match the number required for the play type.");
		}
		this.playType = playType;
		this.players = List.copyOf(player);
		this.timestamp = timestamp;
		this.team = team;
		this.score = score;
	}

	/**Returns a video object containing footage of this play.
	@return a video object containing footage of this play. 
	*/
	public abstract Video getVideo() throws IOException;

	/**Returns the type of this play.
	@return the type of this play.
	*/
	public PlayType getType(){
		return this.playType;
	}

	/**Returns the players that are associated with this play.
	@return the players that are associated with this play.
	*/
	public List<Player> getPlayers(){
		return this.players;
	}

	/**Returns the time that this play occured at.
	@return the time that this play occured at.
	*/
	public Timestamp getTimestamp(){
		return this.timestamp;
	}

	/**Returns the team that committed this play.
	@return the team that committed this play.
	*/
	public Team getTeam(){
		return this.team;
	}

	/**Returns the current score at the time of this play.
	@return the current score at the time of this play.
	*/
	public Score getScore(){
		return this.score;
	}

	@Override
	/**Returns a hash code for this Play.
	@return a hash code for this Play.
	*/
	public int hashCode(){
		return this.playType.hashCode() + this.players.hashCode() + this.timestamp.hashCode() + this.team.hashCode() + this.score.hashCode();
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
		return this.playType.equals(pl.playType) && this.players.equals(pl.players) && this.timestamp.equals(pl.timestamp) && this.team.equals(pl.team) && this.score.equals(pl.score);
	}

	@Override
	/**Returns a String representation of this Play.
	@return a String representation of this Play.
	*/
	public String toString(){
		return this.playType.toString() + " by " + (this.players.size() > 0 ? (this.players.toString() + " of ") : "") + this.team.toString() + " at " + this.timestamp.toString();
	}
}