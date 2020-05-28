package thybulle.misc;

import java.io.*;
import java.util.*;
import java.nio.file.*;
import java.net.*;

//TODO: implement save, combineVideos.

/**Interface representing a video file.<br>
Implementing classes must be able to save the file to the user's computer.
@author Owen Kulik
*/

public interface Video {
	/**Saves a copy of this video to the given location and returns a FileVideo pointing to that location.
	@param location The location to save the video to.
	@throws NullPointerException if location is null.
	@return a reference to a Video object pointing to the given location.
	*/
	FileVideo save(File location) throws IOException;

	/**Returns a String representation of the location that this Video points to.
	@return the location that this Video points to.
	*/
	String getLocation();

	/**Combines the given videos, saves them to location, and returns a reference to the newly saved video.
	@param location The location to save the video.
	@param videos The videos to combine.
	@throws NullPointerException if any argument is null.
	@throws IllegalArgumentException if videos.length == 0.
	@return a reference to the resulting video.
	*/
	public static Video combineVideos(File location, Video... videos) throws IOException {
		return combineVideos(location, new Logging(), videos);
	}

	/**This method is identical to the {@link #combineVideos(File, Video...) combineVideos} method,
	with the exception that useful information will be logged to output.<br>
	This is useful for combineVideo calls with several videos, as these may take a very long time.
	@param location The location to save the video.
	@param output A logging object to output to.
	@param videos The videos to combine.
	@throws NullPointerException if any argument is null.
	@throws IllegalArgumentException if videos.length == 0.
	@return a reference to the resulting video.
	*/
	public static Video combineVideos(File location, Logging output, Video... videos) throws IOException {
		if(location == null || videos == null){
			throw new NullPointerException();
		}
		if(videos.length == 0){
			throw new IllegalArgumentException("videos.length was 0.");
		}
		StringBuilder s = new StringBuilder("");
		output.info("Saving " + videos.length + (videos.length == 1 ? " video." : " videos."));
		for(int i = 0; i < videos.length; i++){
			if(videos[i] == null) {
				continue;
			}
			File f = File.createTempFile(String.format("%03d", i), ".mp4");
			f.deleteOnExit();
			FileVideo v = videos[i].save(f);
			output.info("Saved video " + (i + 1) + ".");
			//sb.append(v.getLocation() + "|");
			s.append("file '" + v.getFileLocation() + "'\n");
		}
		output.info("Finished saving videos.");
		File list = File.createTempFile("list", ".txt");
		list.deleteOnExit();

		BufferedWriter writer = new BufferedWriter(new FileWriter(list));
    	writer.write(s.toString());
    	writer.close();

    	output.info("Concatenating videos.");
    	Process concat = Runtime.getRuntime().exec("ffmpeg -y -f concat -safe 0 -i " + list.getAbsolutePath() + " -c copy " + location.getAbsolutePath());
    	BufferedReader stdInput = new BufferedReader(new InputStreamReader(concat.getErrorStream()));
		String t;
		while ((t = stdInput.readLine()) != null) {
			//System.out.println(t);
		}
		output.info("Finished concatenating.");
		return new FileVideo(location);
	}
}