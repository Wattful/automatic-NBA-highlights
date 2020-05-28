package thybulle.misc;

import java.io.*;
import java.util.*;
import java.nio.file.*;

/**Immutable class representing a video on the user's computer.
In addition to implementing the Video interface, includes the option to delete the video, as well as get a File object pointing to the Video.
*/

public class FileVideo implements Video {
	private static final boolean CHECK_REP = true;

	private final File machineLocation;

	/**Constructs a FileVideo pointing to the given File.
	@param location The video.
	@throws NullPointerException if location is null.
	*/
	public FileVideo(File location){
		if(location == null){
			throw new NullPointerException();
		}
		this.machineLocation = location;
		checkRep();
	}

	/**Constructs a FileVideo pointing to the path in the given String.
	@param path Path to the video.
	@throws NullPointerException if path is null.
	*/
	public FileVideo(String path){
		if(path == null){
			throw new NullPointerException();
		}
		this.machineLocation = new File(path);
		checkRep();
	}

	private void checkRep(){
		if(!CHECK_REP){
			return;
		}
		if(this.machineLocation == null){
			throw new IllegalStateException();
		}
	}

	@Override
	/**Saves a copy of this video to the given location and returns a FileVideo pointing to that location.
	@param location The location to save the video to.
	@throws NullPointerException if location is null.
	@return a reference to a Video object pointing to the given location.
	*/
	public FileVideo save(File location) throws IOException {
		if(location == null){
			throw new NullPointerException();
		}
		InputStream stream = new FileInputStream(machineLocation);
		Files.copy(stream, location.toPath(), StandardCopyOption.REPLACE_EXISTING);
		return new FileVideo(location);
	}

	/**Deletes this video file.
	*/
	public void delete(){
		this.machineLocation.delete();
	}

	/**Returns a File object pointing to this video.
	@return a File object pointing to this video.
	*/
	public File getFileLocation(){
		return this.machineLocation;
	}

	@Override
	/**Returns a String representation of this video's location.
	@return a String representation of this video's location.
	*/
	public String getLocation(){
		return this.machineLocation.toString();
	}

	@Override
	/**Returns a hash code for this FileVideo.
	@return a hash code for this FileVideo.
	*/
	public int hashCode(){
		return machineLocation.hashCode();
	}

	@Override
	/**Returns a boolean indicating whether this FileVideo is equal to the given object.<br>
	They are considered equal if and only if o is a FileVideo in the same location as this FileVideo.
	@param o The object to compare to.
	@return a boolean indicating whether this FileVideo is equal to the given object.
	*/
	public boolean equals(Object o){
		if(o == null){
			return false;
		}
		if(this == o){
			return true;
		}
		if(!(o instanceof FileVideo)){
			return false;
		}
		FileVideo v = (FileVideo)o;
		return this.machineLocation.equals(v.machineLocation);
	}

	@Override
	/**Returns a String representation of this FileVideo.
	@return a String representation of this FIleVideo.
	*/
	public String toString(){
		return "Video located at " + this.getLocation();
	}
}