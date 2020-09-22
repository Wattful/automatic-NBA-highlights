package thybulle.driver;

import java.util.*;
import java.io.*;
import thybulle.highlights.*;
import thybulle.misc.*;

/**Class representing a highlights video, with the ability to get a reference to the video.
@author Owen Kulik
*/

public class Highlights {
	private final List<Play> plays;
	private FileVideo video;

	/**Constructs a Highlights object from the given plays.
	 * @param p The plays to include.
	 * @throws NullPointerException if p is null.
	 */
	Highlights(List<? extends Play> p){
		plays = new ArrayList<Play>(p);
	}

	/**Saves this highlights video to the specified path.
	@param path The location to save the video.
	@throws NullPointerException if any parameters are null.
	@return a video for this Highlights object.
	*/
	public FileVideo saveVideo(File path) throws IOException {
		return saveVideo(path, new Logging());
	}

	/**This method is exactly equivalent to {@link #saveVideo(File) saveVideo} except relevant information is logged through output.
	@param path The location to save the video.
	@param output Logging object to output relevant information.
	@throws NullPointerException if any parameters are null.
	@return a video for this Highlights object.
	*/
	public FileVideo saveVideo(File path, Logging output) throws IOException {
		if(video != null){
			return video;
		}
		if(path == null){
			throw new NullPointerException();
		}
		Set<Video> v = new LinkedHashSet<Video>();
		output.info("Resolving " + plays.size() + (plays.size() == 1 ? " play video." : " play videos."));
		for(Play p : plays){
			v.add(p.getVideo());
		}
		output.info("Finished resolving play videos.");
		//@SuppressWarnings("unchecked")
		video = FileVideo.combineVideos(path, output, new ArrayList<Video>(v));
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