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
	
	private final URL internetLocation;
	private final File machineLocation;

	//RI: one field is null, the other is not.
	//AF: Represents a video, either on the internet or on the local machine.

	/**Constructs a video pointing to the given URL.
	 * @param file The URL where the video is located.
	 * @throws NullPointerException if file is null.
	 */
	public Video(URL file) {
		if(file == null) {
			throw new NullPointerException();
		}
		internetLocation = file;
		machineLocation = null;
		checkRep();
	}
	
	/**Constructs a video pointing to the given file.
	 * @param location The file where the video is located.
	 * @throws NullPointerException if location is null.
	 */
	public Video(File location) {
		if(location == null) {
			throw new NullPointerException();
		}
		internetLocation = null;
		machineLocation = location;
		checkRep();
	}

	//Checks Video's rep invariant.
	private void checkRep(){
		if(!CHECK_REP){
			return;
		}
		if(internetLocation == null && machineLocation == null){
			throw new IllegalStateException();
		}
		if(internetLocation != null && machineLocation != null){
			throw new IllegalStateException();
		}
	}

	/**Combines the given videos and saves them to location.
	@param location The location to save the video.
	@param garbageFolder a folder to use to save temporary files.
	@param videos The videos to combine.
	@throws NullPointerException if either argument is null.
	@throws IllegalArgumentException if videos.length == 0.
	@return a reference to the resulting video.
	*/
	public static Video combineVideos(File location, File garbageFolder, Video... videos) throws IOException {
		if(location == null || garbageFolder == null || videos == null){
			throw new NullPointerException();
		}
		if(videos.length == 0){
			throw new IllegalArgumentException("videos.length was 0.");
		}
		//StringBuilder sb = new StringBuilder("ffmpeg -y -i \"concat:");
		StringBuilder s = new StringBuilder("");
		for(int i = 0; i < videos.length; i++){
			if(videos[i] == null) {
				continue;
			}
			Video v = videos[i].save(new File(garbageFolder, i + ".mp4"));
			//sb.append(v.getLocation() + "|");
			s.append("file '" + v.getLocation() + "'\n");
		}
		/*sb.deleteCharAt(sb.length() - 1);
		sb.append("\"");
		sb.append(" -c copy " + location.toString() + "");*/

		File list = new File(garbageFolder, File.separator + "list.txt");
		BufferedWriter writer = new BufferedWriter(new FileWriter(list));
    	writer.write(s.toString());
    	writer.close();

    	Process concat = Runtime.getRuntime().exec("ffmpeg -y -f concat -safe 0 -i " + list.getAbsolutePath() + " -c copy " + location.getAbsolutePath());
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
	public Video save(File location) throws IOException {
		if(location == null){
			throw new NullPointerException();
		}
		InputStream stream;
		if(internetLocation != null){
			stream = internetLocation.openStream();
		} else {
			stream = new FileInputStream(machineLocation);
		}
		Files.copy(stream, location.toPath(), StandardCopyOption.REPLACE_EXISTING);
		return new Video(location);
	}

	/**Returns the location that this Video points to.
	@return the location that this Video points to.
	*/
	public String getLocation(){
		return (internetLocation != null ? internetLocation.toString() : machineLocation.toString());
	}

	@Override
	/**Returns a hash code for this Video.
	@return a hash code for this Video.
	*/
	public int hashCode(){
		return (internetLocation != null ? internetLocation.hashCode() : machineLocation.hashCode());
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
		return (internetLocation != null ? this.internetLocation.equals(v.internetLocation) : this.machineLocation.equals(v.machineLocation));
	}

	@Override
	/**Returns a String representation of this Video.
	@return a String representation of this Video.
	*/
	public String toString(){
		return "Video located at " + this.getLocation();
	}
}