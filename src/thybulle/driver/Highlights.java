package thybulle.driver;

import java.util.*;
import java.io.*;
import thybulle.highlights.*;
import thybulle.misc.*;

//TODO:

/**Class representing a highlights video, with the ability to get a reference to the video.
@author Owen Kulik
*/

public class Highlights {
	private static final boolean CHECK_REP = true;

	private final List<Play> plays;
	private Video video;

	//RI: plays is not null.
	//AF: Represents a Highlights video. plays is all the plays in this video, in order.

	/**Constructs a Highlights object from the given plays.
	 * @param p The plays to include.
	 * @throws NullPointerException if p is null.
	 */
	Highlights(List<? extends Play> p){
		plays = new LinkedList<Play>(p);
		checkRep();
	}

	//Checks this object's rep invariant.
	private void checkRep(){
		if(!CHECK_REP){
			return;
		}
		if(plays == null){
			throw new IllegalStateException();
		}
	}

	/**Saves this highlights video to the specified path.<br>
	Since this method downloads videos from the internet before combining them, it must save the videos intermediately.<br>
	Therefore, you must provide a location to temporarily store these videos.<br>
	@param path The location to save the video.
	@param garbageLocation The location to save temporary videos.
	@throws NullPointerException if any parameters are null.
	@return a video for this Highlights object.
	*/
	public Video saveVideo(File path, File garbageLocation) throws IOException {
		if(video != null){
			return video;
		}
		if(path == null || garbageLocation == null){
			throw new NullPointerException();
		}
		List<Video> v = new LinkedList<Video>();
		for(Play p : plays){
			v.add(p.getVideo());
		}
		//@SuppressWarnings("unchecked")
		video = Video.combineVideos(path, garbageLocation, v.toArray(new Video[0]));
		return video;
	}

	/**Returns a new HighlightsCompiler.
	@return a new HighlightsCompiler.
	*/
	public static HighlightsCompiler compiler(){
		return new HighlightsCompiler();
	}
	
	/**Returns the number of plays in this highlights object.
	 * @return the number of plays in this highlights object.
	 */
	public int size() {
		return plays.size();
	}

	/**Returns a hash code for this Highlights object.
	@return a hash code for this Highlights object.
	*/
	public int hashCode(){
		return plays.hashCode();
	}

	/**Returns a boolean indicating whether this Highlights object is equivalent to the given object.<br>
	They are considered equal if o is a Highlights object with the same plays in the same order as this Highlights object.
	@param o The object to compare to.
	@return a boolean indicating whether this Highlights object is equivalent to the given object.
	*/
	public boolean equals(Object o){
		if(o == null){
			return false;
		}
		if(this == o){
			return true;
		}
		if(!(o instanceof Highlights)){
			return false;
		}
		Highlights h = (Highlights)o;
		return this.plays.equals(h.plays);
	}
}