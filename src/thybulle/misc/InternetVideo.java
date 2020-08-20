package thybulle.misc;

import java.net.*;
import java.io.*;
import java.util.*;
import java.nio.file.*;

/**Immutable class representing a video file on the internet.
Includes the ability to get a URL object pointing to the video.
*/

public class InternetVideo implements Video {
	private final URL internetLocation;

	/**Constructs an InternetVideo pointing to the given URL.
	@param location The video URL.
	@throws NullPointerException if location is null.
	*/
	public InternetVideo(URL location){
		if(location == null){
			throw new NullPointerException();
		}
		this.internetLocation = location;
	}

	/**Constructs an InternetVideo pointing to the path in the given String.
	@param path URL to the video.
	@throws NullPointerException if url is null.
	*/
	public InternetVideo(String url){
		if(url == null){
			throw new NullPointerException();
		}
		try{
			this.internetLocation = new URL(url);
		} catch(MalformedURLException e){
			throw new IllegalArgumentException("Malformed URL.", e);
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
		InputStream stream = internetLocation.openStream();
		Files.copy(stream, location.toPath(), StandardCopyOption.REPLACE_EXISTING);
		return new FileVideo(location);
	}

	/**Returns a URL object pointing to this video.
	@return a URL object pointing to this video.
	*/
	public URL getInternetLocation(){
		return this.internetLocation;
	}

	@Override
	/**Returns a String representation of this video's location.
	@return a String representation of this video's location.
	*/
	public String getLocation(){
		return this.internetLocation.toString();
	}

	@Override
	/**Returns a hash code for this InternetVideo.
	@return a hash code for this InternetVideo.
	*/
	public int hashCode(){
		return internetLocation.hashCode();
	}

	@Override
	/**Returns a boolean indicating whether this InternetVideo is equal to the given object.<br>
	They are considered equal if and only if o is an InternetVideo with the same URL as this InternetVideo.
	@param o The object to compare to.
	@return a boolean indicating whether this InternetVideo is equal to the given object.
	*/
	public boolean equals(Object o){
		if(o == null){
			return false;
		}
		if(this == o){
			return true;
		}
		if(!(o instanceof InternetVideo)){
			return false;
		}
		InternetVideo v = (InternetVideo)o;
		return this.internetLocation.equals(v.internetLocation);
	}

	@Override
	/**Returns a String representation of this InternetVideo.
	@return a String representation of this InternetVideo.
	*/
	public String toString(){
		return "Video located at " + this.getLocation();
	}
}