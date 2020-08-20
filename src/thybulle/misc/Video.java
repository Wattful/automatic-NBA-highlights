package thybulle.misc;

import java.io.*;
import java.util.*;
import java.nio.file.*;
import java.net.*;

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
}