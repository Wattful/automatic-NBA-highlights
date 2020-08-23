package thybulle.driver;

import org.json.*;
import java.nio.file.*;
import java.util.*;
import java.io.*;
import java.time.*;
import java.time.format.*;
import java.util.regex.*;
import java.lang.reflect.*;
import thybulle.highlights.*;
import thybulle.misc.*;

/**Class containing static methods which parse input.<br>
Input determines which games to use as an input dataset, as well as what constraints to apply to the dataset.<br>
This class parses this input and returns it to the caller.
*/

public class InputParsing {
	private static final String separator = "\\s*\\-\\s*";
	private static final String datePattern = "\\d\\d/\\d\\d/\\d\\d\\d\\d";
	private static final String dateIntervalPattern = datePattern + separator + datePattern;
	private static final String yearPattern = "\\d\\d\\d\\d";
	private static final String yearIntervalPattern = yearPattern + separator + yearPattern;
	private static final String seasonFormatPattern = "[\\w&&[\\D]]+";
	private static final String seasonPattern = yearIntervalPattern + seasonFormatPattern;
	private static final DateTimeFormatter dateFormat = DateTimeFormatter.ofPattern("MM/dd/yyyy");

	//Lists of dates of the start and end of each year's preseason, regular season, all-star weekend, and playoffs.
	//BaseYear is the first season with data, no earlier seasons can be used.
	private static final Year baseYear = Year.of(2012);
	private static final List<List<Pair<LocalDate, LocalDate>>> preseasonDates = List.of(
		List.of(Pair.of(LocalDate.of(2012, 10, 5), LocalDate.of(2012, 10, 26))),
		List.of(Pair.of(LocalDate.of(2013, 10, 5), LocalDate.of(2013, 10, 25))),
		List.of(Pair.of(LocalDate.of(2014, 10, 4), LocalDate.of(2014, 10, 24))),
		List.of(Pair.of(LocalDate.of(2015, 10, 2), LocalDate.of(2015, 10, 23))),
		List.of(Pair.of(LocalDate.of(2016, 10, 1), LocalDate.of(2016, 10, 21))),
		List.of(Pair.of(LocalDate.of(2017, 9, 30), LocalDate.of(2017, 10, 13))),
		List.of(Pair.of(LocalDate.of(2018, 9, 28), LocalDate.of(2018, 10, 12))),
		List.of(Pair.of(LocalDate.of(2019, 9, 30), LocalDate.of(2019, 10, 18)), Pair.of(LocalDate.of(2020, 7, 22), LocalDate.of(2020, 7, 28)))
	);
	private static final List<List<Pair<LocalDate, LocalDate>>> regularSeasonDates = List.of(
		List.of(Pair.of(LocalDate.of(2012, 10, 30), LocalDate.of(2013, 2, 14)), Pair.of(LocalDate.of(2013, 2, 18), LocalDate.of(2013, 4, 17))),
		List.of(Pair.of(LocalDate.of(2013, 10, 29), LocalDate.of(2014, 2, 13)), Pair.of(LocalDate.of(2014, 2, 17), LocalDate.of(2014, 4, 16))),
		List.of(Pair.of(LocalDate.of(2014, 10, 28), LocalDate.of(2015, 2, 12)), Pair.of(LocalDate.of(2015, 2, 16), LocalDate.of(2015, 4, 15))),
		List.of(Pair.of(LocalDate.of(2015, 10, 27), LocalDate.of(2016, 2, 11)), Pair.of(LocalDate.of(2016, 2, 15), LocalDate.of(2016, 4, 13))),
		List.of(Pair.of(LocalDate.of(2016, 10, 25), LocalDate.of(2017, 2, 16)), Pair.of(LocalDate.of(2017, 2, 20), LocalDate.of(2017, 4, 12))),
		List.of(Pair.of(LocalDate.of(2017, 10, 17), LocalDate.of(2018, 2, 15)), Pair.of(LocalDate.of(2018, 2, 19), LocalDate.of(2018, 4, 11))),
		List.of(Pair.of(LocalDate.of(2018, 10, 16), LocalDate.of(2019, 2, 14)), Pair.of(LocalDate.of(2019, 2, 18), LocalDate.of(2019, 4, 10))),
		List.of(Pair.of(LocalDate.of(2019, 10, 22), LocalDate.of(2020, 2, 13)), Pair.of(LocalDate.of(2020, 2, 17), LocalDate.of(2020, 3, 11)), Pair.of(LocalDate.of(2020, 7, 30), LocalDate.of(2020, 8, 14)))
	);
	private static final List<List<Pair<LocalDate, LocalDate>>> allStarWeekendDates = List.of(
		List.of(Pair.of(LocalDate.of(2013, 2, 15), LocalDate.of(2013, 2, 17))),
		List.of(Pair.of(LocalDate.of(2014, 2, 14), LocalDate.of(2014, 2, 16))),
		List.of(Pair.of(LocalDate.of(2015, 2, 13), LocalDate.of(2015, 2, 15))),
		List.of(Pair.of(LocalDate.of(2016, 2, 12), LocalDate.of(2016, 2, 14))),
		List.of(Pair.of(LocalDate.of(2017, 2, 17), LocalDate.of(2017, 2, 19))),
		List.of(Pair.of(LocalDate.of(2018, 2, 16), LocalDate.of(2018, 2, 18))),
		List.of(Pair.of(LocalDate.of(2019, 2, 15), LocalDate.of(2019, 2, 17))),
		List.of(Pair.of(LocalDate.of(2020, 2, 14), LocalDate.of(2020, 2, 16)))
	);
	private static final List<List<Pair<LocalDate, LocalDate>>> playoffsDates = List.of(
		List.of(Pair.of(LocalDate.of(2013, 4, 20), LocalDate.of(2013, 6, 3))),
		List.of(Pair.of(LocalDate.of(2014, 4, 19), LocalDate.of(2014, 5, 31))),
		List.of(Pair.of(LocalDate.of(2015, 4, 18), LocalDate.of(2015, 5, 27))),
		List.of(Pair.of(LocalDate.of(2016, 4, 16), LocalDate.of(2016, 5, 30))),
		List.of(Pair.of(LocalDate.of(2017, 4, 15), LocalDate.of(2017, 5, 25))),
		List.of(Pair.of(LocalDate.of(2018, 4, 14), LocalDate.of(2018, 5, 28))),
		List.of(Pair.of(LocalDate.of(2019, 4, 15), LocalDate.of(2019, 5, 25))),
		List.of(Pair.of(LocalDate.of(2020, 8, 15), LocalDate.of(2020, 9, 28)))
	);
	private static final List<List<Pair<LocalDate, LocalDate>>> finalsDates = List.of(
		List.of(Pair.of(LocalDate.of(2013, 6, 6), LocalDate.of(2013, 6, 20))),
		List.of(Pair.of(LocalDate.of(2014, 6, 5), LocalDate.of(2014, 6, 15))),
		List.of(Pair.of(LocalDate.of(2015, 6, 4), LocalDate.of(2015, 6, 16))),
		List.of(Pair.of(LocalDate.of(2016, 6, 2), LocalDate.of(2016, 6, 19))),
		List.of(Pair.of(LocalDate.of(2017, 6, 1), LocalDate.of(2017, 6, 12))),
		List.of(Pair.of(LocalDate.of(2018, 5, 31), LocalDate.of(2018, 6, 8))),
		List.of(Pair.of(LocalDate.of(2019, 5, 30), LocalDate.of(2019, 6, 15))),
		List.of(Pair.of(LocalDate.of(2020, 9, 30), LocalDate.of(2020, 10, 13)))
	);
	private static final Map<String, List<List<Pair<LocalDate, LocalDate>>>> seasonCodeMappings = new HashMap<String, List<List<Pair<LocalDate, LocalDate>>>>();
	static{
		seasonCodeMappings.put("e", preseasonDates);
		seasonCodeMappings.put("r", regularSeasonDates);
		seasonCodeMappings.put("a", allStarWeekendDates);
		seasonCodeMappings.put("p", playoffsDates);
		seasonCodeMappings.put("f", finalsDates);
	}

	private static final Map<String, Class<? extends Constraint>> includedConstraints = new HashMap<String, Class<? extends Constraint>>();
	static{
		includedConstraints.put("player", thybulle.highlights.Player.class);
		includedConstraints.put("team", thybulle.highlights.Team.class);
		includedConstraints.put("type", thybulle.highlights.PlayType.class);
		includedConstraints.put("time", thybulle.highlights.TimeInterval.class);
		includedConstraints.put("score", thybulle.highlights.RelativeScoreConstraint.class);
	}
	
	private InputParsing(){}
	
	/**Parses the given file and returns the game Source specified by the playsrc key.
	 * @param inputFile Path to the input file
	 * @throws IOException If an IO error occurs.
	 * @throws NullPointerException if inputFile is null.
	 * @throws JSONException if the input file is not a JSON file, or if the JSON does not meet the standards described above.
	 * @return the game Source specified by the playsrc key.
	 */
	public static GameSource parseSource(String inputFile) throws IOException {
		return parseSource(new JSONObject(FileUtils.fileToString(inputFile)).getInt("playsrc"));
	}
	
	/**Returns the source corresponding to the given int according to the above specification.
	 * @param src int corresponding to a source.
	 * @throws JSONException if the input does not correspond to a source.
	 * @return The Game Source corresponding to the given int.
	 */
	public static GameSource parseSource(int src) throws IOException {
		if(src == 0){
			return AdvancedStats.open(new JSONObject(FileUtils.fileToString(AdvancedStats.DEFAULT_CONFIG_PATH)));
		} else {
			throw new JSONException("Unrecognized play-by-play source: " + src);
		}
	}

	/**Parses the given file and returns a collection of pairs of dates.<br>
	 * Each pair indicates that games between these dates, inclusive, should be included.
	@param inputFile Path to the input file.
	@throws NullPointerException if inputFile is null.
	@throws IOException if an IO error occurs.
	@throws JSONException if the input file is not a JSON file, or if the JSON does not meet the standards described above.
	@return a collection of pairs of dates indicating which games should be included.
	*/
	public static Collection<Pair<LocalDate, LocalDate>> parseDataset(String inputFile) throws IOException {
		return parseDataset(new JSONObject(FileUtils.fileToString(inputFile)).getJSONArray("dataset"));
	}
	
	/**Returns a collection of pairs of dates given a JSON array according to the above specification.
	 * @throws JSONException if the JSON array does not meet the standards described above.
	 * @param jo The JSON array.
	 * @return collection of pairs of dates given a JSON array according to the above specification.
	 */
	public static Collection<Pair<LocalDate, LocalDate>> parseDataset(JSONArray jo) {
		return parseDateArray(jo);
	}

	//Parses an array of dates into a collection of beginning and end dates.
	private static Collection<Pair<LocalDate, LocalDate>> parseDateArray(JSONArray times){
		Collection<Pair<LocalDate, LocalDate>> dates = new ArrayList<Pair<LocalDate, LocalDate>>();
		for(Object o : times){
			if(!(o instanceof String)){
				throw new JSONException("Non-string in Dataset array: " + o.toString());
			}
			dates.addAll(parseDates((String)o));
		}
		return dates;
	}

	//Parses a date string into one or more pairs of beginning and end dates.
	private static Collection<Pair<LocalDate, LocalDate>> parseDates(String input){
		if(Pattern.matches(datePattern, input)){
			LocalDate d = parseDate(input);
			return List.of(Pair.of(d, d));
		} else if(Pattern.matches(dateIntervalPattern, input)){
			String[] split = input.split(separator);
			LocalDate d1 = parseDate(split[0]);
			LocalDate d2 = parseDate(split[1]);
			if(d2.isBefore(d1)) {
				throw new JSONException("Second date cannot be before first date: " + input);
			}
			return List.of(Pair.of(d1, d2));
		} else if(Pattern.matches(seasonPattern, input)){
			String[] split = input.replaceAll(seasonFormatPattern, "").split(separator);
			Year beginning = Year.parse(split[0]);
			Year end = Year.parse(split[1]);
			if(beginning.isAfter(end)){
				throw new JSONException("End year cannot be before beginning year: " + input);
			}
			if(beginning.equals(end)){
				throw new JSONException("End year cannot be the same as beginning year: " + input);
			}
			if(beginning.isBefore(baseYear)){
				throw new JSONException("Cannot parse seasons before " + baseYear.toString() + ": " + input);
			}
			Collection<Pair<LocalDate, LocalDate>> dates = new ArrayList<Pair<LocalDate, LocalDate>>();
			for(int i = beginning.getValue(); i < end.getValue(); i++){
				dates.addAll(parseSeason(Year.of(i), input.replace(yearIntervalPattern, "")));
			}
			return dates;
		} else {
			throw new JSONException("Unrecognized time: " + input);
		}
	}

	//Parses a date from a string. Precondition is that pattern.matches(datePattern, input)
	private static LocalDate parseDate(String input){
		LocalDate d = LocalDate.parse(input, dateFormat);
		if(d.isBefore(preseasonDates.get(0).get(0).first())){
			throw new JSONException("Cannot parse dates before the 2012-13 season: " + input);
		}
		return d;
	}

	//Parses a collection of beginning and end dates from a Year (first year of the season), and info string according to the above specification.
	private static Collection<Pair<LocalDate, LocalDate>> parseSeason(Year year, String info){
		Collection<Pair<LocalDate, LocalDate>> dates = new ArrayList<Pair<LocalDate, LocalDate>>();
		boolean detected = false;
		int index = year.getValue() - baseYear.getValue();
		info = info.toLowerCase();
		for(Map.Entry<String, List<List<Pair<LocalDate, LocalDate>>>> entry : seasonCodeMappings.entrySet()){
			if(info.contains(entry.getKey())){
				detected = true;
				dates.addAll(entry.getValue().get(index));
			}
		}
		if(!detected){
			throw new JSONException("Improperly formatted season type string: " + info);
		}
		return dates;
	}
	
	/**Parses the given file and returns a collection of team according to the above specification.
	 * @param inputFile The input JSON file.
	 * @throws IOException if an IO error occurs.
	 * @throws JSONException if the given file is not a JSON file or is not fomatted as specified above.
	 * @return a collection of team according to the above specification.
	 */
	public static Collection<Team> parseTeams(String inputFile) throws IOException {
		return parseTeams(new JSONObject(FileUtils.fileToString(inputFile)).getJSONArray("datasetteam"));
	}
	
	/**Returns a collection of team given a JSONArray according to the above specification.
	 * @param t The JSON array.
	 * @throws NullPointerException if t is null.
	 * @throws JSONException if the JSON array does not meet the specification.
	 * @return a collection of team given a JSONArray according to the above specification.
	 */
	public static Collection<Team> parseTeams(JSONArray t){
		if(t == null){
			return new ArrayList<Team>();
		}
		Collection<Team> teams = new ArrayList<Team>();
		for(Object o : t){
			if(!(o instanceof String)){
				throw new JSONException("Non-string in Team array: " + o.toString());
			}
			try {
				teams.add(Team.parse((String)o));
			} catch(IllegalArgumentException e) {
				throw new JSONException(e);
			}
		}
		return teams;
	}

	/**Parses the given file according to the specification above, and returns a list of the described constraints.
	@param inputFile Path to the input file.
	@throws NullPointerException if inputFile is null.
	@throws IOException if an IO error occurs.
	@throws JSONException if the input file is not a JSON file, or if the JSON does not meet the standards described above.
	@return all of the described constraints.
	*/
	public static Collection<Constraint> parseConstraints(String inputFile) throws IOException {
		return parseConstraints(new JSONObject(FileUtils.fileToString(inputFile)).getJSONArray("constraints"));
	}

	/**Parses the given JSON array and returns constraints according to the above specification.
	 * @param source The JSON array
	 * @throws NullPointerException if source is null.
	 * @throws JSONException if the JSON array does not meet the given specification.
	 * @return
	 */
	public static Collection<Constraint> parseConstraints(JSONArray source){
		Collection<Constraint> answer = new ArrayList<Constraint>();
		for(Object o : source){
			answer.add(parseConstraint(o));
		}
		return answer;
	}

	//Parses a constraint from an object.
	private static Constraint parseConstraint(Object o){
		if(o instanceof String){
			return parseStringConstraint((String)o);
		} else if(o instanceof JSONObject){
			return parseJSONObjectConstraint((JSONObject)o);
		} else {
			throw new JSONException("Unknown constraint: " + o.toString());
		}
	}

	//Parses a constraint from a JSON object.
	private static Constraint parseJSONObjectConstraint(JSONObject jo){
		if(jo.length() != 1){
			throw new JSONException("Unknown constraint: " + jo.toString());
		}
		String s = jo.keys().next();
		String constraintType = s.trim().toLowerCase();
		if(constraintType.equals("or")){
			return new OrConstraint(parseConstraints(jo.getJSONArray(s)));
		} else if(constraintType.equals("and")){
			return new AndConstraint(parseConstraints(jo.getJSONArray(s)));
		} else if(constraintType.equals("not")){
			return new NotConstraint(parseConstraint(jo.get(s)));
		} else {
			throw new JSONException("Unknown constraint type: " + s);
		}
	}

	//Parses a constraint from a string.
	@SuppressWarnings("unchecked")
	private static Constraint parseStringConstraint(String input){
		String simplifiedInput = input.trim();
		String[] split = simplifiedInput.split(":\\s+", 2);
		String constraintType = split[0];
		String constraintInput = split.length < 2 ? null : split[1].toLowerCase();
		Class<? extends Constraint> argumentClass = includedConstraints.get(constraintType.toLowerCase());
		if(argumentClass == null){
			try{
				argumentClass = (Class<? extends Constraint>)Class.forName(constraintType);
			} catch(ClassNotFoundException e){
				throw new JSONException(e);
			} catch(ClassCastException e){
				throw new JSONException(argumentClass.toString() + " does not implement thybulle.highlights.Constraint.", e);
			}
		}
		if(argumentClass.isInterface()){
			throw new JSONException(argumentClass.toString() + " is an interface.");
		} else if(Modifier.isAbstract(argumentClass.getModifiers())){
			throw new JSONException(argumentClass.toString() + " is an abstract class.");
		} else if(!Constraint.class.isAssignableFrom(argumentClass)) {
			throw new JSONException(argumentClass.toString() + " does not implement thybulle.highlights.Constraint.");
		}
		Constructor<? extends Constraint> validConstructor;
		Method parseMethod;
		try{
			validConstructor = argumentClass.getConstructor(String.class);
		} catch(NoSuchMethodException e){
			validConstructor = null;
		}
		try{
			parseMethod = argumentClass.getMethod("parse", String.class);
			if(!Constraint.class.isAssignableFrom(parseMethod.getReturnType())){
				parseMethod = null;
			}
		} catch(NoSuchMethodException e){
			parseMethod = null;
		}

		try{
			if(parseMethod != null){
				Constraint c  = (Constraint)parseMethod.invoke(null, constraintInput);
				if(c == null){
					throw new JSONException(argumentClass.toString() + " parse method returned null for input " + constraintInput);
				}
				return c;
			} else if(validConstructor != null){
				return validConstructor.newInstance(constraintInput);
			} else {
				throw new JSONException(argumentClass.toString() + " did not define a constructor or parse method with a single String parameter.");
			}
		} catch(ReflectiveOperationException e){
			throw new JSONException(e);
		} catch(IllegalArgumentException e){
			throw new JSONException(input + " could not be parsed.", e);
		}
	}
}