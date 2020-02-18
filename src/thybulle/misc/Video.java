package thybulle.misc;

import java.io.*;
import java.util.*;
import java.nio.file.*;
import java.net.*;

//TODO: implement save, combineVideos.

/**Immutable class representing a video file.
@author Owen Kulik
*/

public class Video {
	private static final boolean CHECK_REP = true;

	private final String videoLocation;

	//RI: videoLocation != null.
	//AF: Represents a video. videoLocation is a URI pointing to the video.

	public Video(String uri){
		if(uri == null){
			throw new NullPointerException("URI was null.");
		}
		videoLocation = uri;
		checkRep();
	}

	//Checks Video's rep invariant.
	private void checkRep(){
		if(!CHECK_REP){
			return;
		}
		if(videoLocation == null){
			throw new IllegalStateException();
		}
	}

	/**Combines the given videos and saves them to location.
	@param location The location to save the video.
	@param videos The videos to combine.
	@throws NullPointerException if either argument is null.
	@throws IllegalArgumentException if videos.length == 0.
	@return a reference to the resulting video.
	*/
	public static Video combineVideos(String location, String garbageFolder, Video... videos) throws IOException {
		if(videos == null || location == null){
			throw new NullPointerException();
		}
		if(videos.length == 0){
			throw new IllegalArgumentException("videos.length was 0.");
		}
		List<Video> videoLocations = new LinkedList<Video>();
		for(int i = 0; i < videos.length; i++){
			videoLocations.add(videos[i].save(garbageFolder + File.separator + i + ".mp4"));
		}
		StringBuilder s = new StringBuilder("");
		for(int i = 0; i < videos.length; i++){
			s.append("file '" + videoLocations.get(i) + "'\n");
		}

		BufferedWriter writer = new BufferedWriter(new FileWriter(garbageFolder + File.separator + "list.txt"));
    	writer.write(s.toString());
    	writer.close();

    	Process concat = Runtime.getRuntime().exec("ffmpeg -y -f concat -safe 0 -i list.txt -c copy " + location);
    	BufferedReader stdInput = new BufferedReader(new InputStreamReader(concat.getErrorStream()));
		String t;
		while ((t = stdInput.readLine()) != null) {
			//System.out.println(t);
		}
		return new Video(location);
	}

	/**Saves a copy of this video to the given location and returns a Video pointing to that location.
	@param location The location to save the video to.
	@throws NullPointerException if location is null.
	@return a reference to a Video object pointing to the given location.
	*/
	public Video save(String location) throws IOException {
		if(location == null){
			throw new NullPointerException();
		}
		
		InputStream stream;
		String scheme;
		try{
			scheme = new URI(videoLocation).getScheme();
		} catch(URISyntaxException e){
			throw new IOException("Video location " + videoLocation + " was not a valid URI.", e);
		}
		if(scheme.equals("http") || scheme.equals("https")){
			stream = new URL(videoLocation).openStream();
		} else {
			stream = new FileInputStream(videoLocation);
		}
		try (InputStream in = stream) {
		    Files.copy(in, Path.of(new URI(location)), StandardCopyOption.REPLACE_EXISTING);
		} catch(URISyntaxException e){
			throw new IOException(location + " was not a valid URI.", e);
		}
		return new Video(location);
	}

	/**Returns the location that this Video points to.
	@return the location that this Video points to.
	*/
	public String getLocation(){
		return videoLocation;
	}

	@Override
	/**Returns a hash code for this Video.
	@return a hash code for this Video.
	*/
	public int hashCode(){
		return videoLocation.hashCode();
	}

	@Override
	/**Returns a boolean indicating whether the objects represent the same video. <br>
	This will return true if and only if the Video objects point to the same location.<br> 
	It does not take the content of the video into account.
	@param o The object to compare to.
	@return a boolean indicating whether the objects represent the same video.
	*/
	public boolean equals(Object o){
		if(o == null){
			return false;
		}
		if(this == o){
			return true;
		}
		if(!(o instanceof Video)){
			return false;
		}
		Video v = (Video)o;
		return this.videoLocation.equals(v.videoLocation);
	}

	@Override
	/**Returns a String representation of this Video.
	@return a String representation of this Video.
	*/
	public String toString(){
		return "Video located at " + videoLocation;
	}
}