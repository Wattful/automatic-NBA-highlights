package thybulle.misc;

import java.nio.file.*;
import java.io.*;

/**Static class with util methods for dealing with the filesystem.
*/

public class FileUtils {
	private FileUtils(){}

	/**Reads all bytes in the given file and returns them as a String.
	@param path path to file to read
	@throws NullPointerException if path is null
	@throws IOException if an IO error occurs
	@return all bytes in the given file as a String.
	*/
	public static String fileToString(String path) throws IOException {
		return Files.readString(Path.of(path));
	}

	/**Writes the given String to the given path.
	@param path path to write to
	@param data tat to save
	@throws NullPointerException if path is null
	@throws IOException if an IO error occurs
	*/
	public static void write(String path, String data) throws IOException {
		try (PrintStream out = new PrintStream(new FileOutputStream(path))) {
    		out.print(data);
		}
	}
}