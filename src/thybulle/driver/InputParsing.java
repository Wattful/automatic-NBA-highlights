package thybulle.driver;

import org.json.*;
import java.nio.file.*;
import java.util.*;
import java.io.*;
import java.time.*;
import java.time.format.*;
import java.util.regex.*;
import thybulle.highlights.*;
import thybulle.misc.*;

/**Class containing static methods which parse input.<br>
Input determines which games to use as an input dataset, as well as what constraints to apply to the dataset.<br>
This class parses this input and returns it to the caller.<br>
The finished product will include all plays in the dataset that satisfy the given constraints.<br>
Input is formatted as a JSON object.<br>
The JSON object should have four keys, each of which are mandatory:
<ul>
	<li>playsrc - Mandatory key. An integer representing which primary source to get play-by-play data from.
		Currently there are is only one source:
		<ul>
			<li>0 - NBA Advanced Stats - Get play-by-play data from NBA Advanced stats.</li>
		</ul>
	</li>
	<li>dataset - Mandatory key. An array of strings representing what set of games to get data from.<br>
		Each string in the array corresponds to a group of games to include in the final dataset.
		There are two ways to specify games: dates and seasons.
		<ul>
			<li>Dates - can be represented as either one date or two dates. <br>
			One date includes all games from that day, while two dates includes all games from between those dates, inclusive.<br>
			Dates are represented in MM/DD/YYYY format, with multiple dates separated by a dash.<br>
			For example, if one wants to include games only on January 7, 2020, the String would be {@code "01/07/2020"}.<br>
			If one wanted to include games from December 30, 2019 to January 6, 2020, the String would be {@code 12/30/2019-01/06/2020}.
			</li>
			<li>Seasons - includes all games in certain seasons.<br>
				A season is represented as two years separated by a dash, followed by multiple letters specifying which parts of the season to include.<br>
				The letters are as follows:
				<ul>
					<li>r - regular season</li>
					<li>p - playoffs, not including the NBA finals</li>
					<li>f - NBA finals</li>
					<li>e - preseason</li>
					<li>a - all-star game and the rising stars challenge
						(Note that the only current source - stats.nba.com, does NOT have video for all star games).</li>
				</ul>
				The order and case of the letters do not matter.<br>
				For example, {@code "2019-2020r"} Includes regular season games from the 2019-20 season.<br>
				{@code "2016-2017pef"} includes playoff and preseason games from the 2016-17 season.<br>
				If one wishes to include multiple consecutive seasons, they can put the first year as the first year of the starting season, 
				and the second year as the last year of the final season.<br>
				For example, {@code "2015-2019ra"} includes regular season and all-star games from the 2015-16 season to the 2018-19 season.
			</li>
		</ul>
	</li>
	<li>datasetteam - Mandatory key. An array of strings representing teams to limit the dataset to.<br>
		Each string represents a team. If specified, the dataset will be limited to games played by the listed teams.<br>
		Team names must be listed in full, case insensitive.<br>
		For example, {@code ["Milwaukee Bucks"]} will limit the dataset to games played by the Milwaukee Bucks,
		and {@code ["Philadelphia 76ers", "golden state warriors"]} will limit the dataset to games played by Philadelphia OR Golden State.<br>
		If this key is not included, games from all teams will be included in the dataset.<br>
		Note that for simplicity, all-star games are affected by this limit.
	</li>
	<li>constraints - Mandatory key. An array of constraints. Each constraint is represented as either a string or an object with a single key.<br>
		All constraints specified must be satisfied by a play to be included in the final video.<br>
		The constraints are as follows:
		<ul>
			<li>Player - Represented as a String, case-insensitive, formatted as {@code "Player: PLAYER_NAME"}. A play satisfies this constraint 
				if it involves this player. <br>
				For example, a play will satisfy {@code "Player: Giannis Antetokounmpo"} if it involves Giannis Antetokounmpo.
			</li>
			<li>Team - Represented as a String, case-insensitive, formatted as {@code "Team: TEAM_NAME"}. A play satisfies this constraint if it was committed by this team. 
				The string must be a team's official, full name (See {@link thybulle.highlights.Team} for a list of NBA teams and their official, full names.)<br>
				For example, a play will satisfy {@code "Team: Portland Trail Blazers"} if it was committed by the Portland Trail Blazers.
			</li>
			<li>Play type - Represented as a String, case-insensitive, formatted as {@code "Type: TYPE_NAME"}. A play satisfies this constraint if it is of this play type.
				(See {@link thybulle.highlights.PlayType} for more information as well as a list of defined play types.)<br>
				For example, a play will satisfy {@code "Type: Dunk"} if it is a dunk.
			</li>
			<li>Time of game - Represented as a String, case-insensitive, formatted as {@code "Time: TIME_OF_GAME"}. A play satisfies this constraints if it is within
				the specified time of game. Time of game can be specified in two ways: 
				<ul>
					<li>Quarter - The play occurs within the specified quarter or overtime period. Acceptable strings for this format include {@code "1st"}, 
						{@code "2nd"}, {@code "3rd"}, {@code "4th"}, {@code "ot"}, {@code "1ot"}, {@code "2ot"}, and so on.<br> 
						For example, a play will satisfy {@code "Time: 1st"} if it occurred in the first quarter.
						Note that "ot" specifies <i>any</i> overtime period, not just the first overtime. 
						If one only wants to include the first overtime, they should use "1ot".
					</li>
					<li>Specific time - The play occurs between the specified times in the game. A time is represented as a number of minutes and a number of seconds,
						followed by the quarter or overtime period. Note that the quarter name must be separated from the time by at least a space.
						The two times are separated by a dash.<br>
						For example, a play will satisfy {@code "Time: 03:00 4th-00:00 1ot"} if it occurs between three minutes left in the fourth and the end of  
						first ovetime, inclusive.
					</li>
				</ul>
			</li>
			<li>Not constraint - Represented as an object with a single key, {@code "NOT"} (case insensitive), corresponding to a value of a single constraint.<br>
				A play satisfies this constraint if it does not satisfy the constraint in the value.<br>
				For example, a play will satisfy {@code {"Not" : "Type: "Three-Pointer"}} if it is not a three pointer.
			</li>
			<li>And constraint - Represented as an object with a single key, {@code "AND"} (case insensitive), corresponding to a value of an array of constraints.<br>
				A play satisfies this constraint if it satisfies all of the constraints in the array.<br>
				For example, a play will satisfy {@code {"AND" : ["Time: 3rd", "Team: Minnesota Timeberwolves"]}} if it occurred in the third quarter and was committed
				by the Minnesota Timberwolves.
			</li>
			<li>
				Or constraint - Represented as an object with a single key, {@code "OR"} (case insensitive), corresponding to a value of an array of constraints.<br>
				A play satisfies this constraint if it satisfies any of the constraints in the array.<br>
				For example, a play will satisfy {@code {"or" : ["Player: Lebron James", "Player: Anthony Davis"]}} if it was committed 
				by either Lebron James or Anthony Davis.
			</li>
		</ul>
		These constraints can be nested within each other.<br>
		For an overall example, if the constraints key points to <br>
		{@code 
		["Player: James Harden", 
		{"OR": 
			["Type: Field Goal", 
			"Type: Assist",
			"Type: Rebound"
			]
		},
		{"NOT" : 
			{"AND":
				["Time: ot",
				"Type: Dunk"
				]
			}
		}
		]},<br>
		The video will include all of James Harden's field goals, assists, and rebounds, except for his overtime dunks.
	</li>
</ul>
<br>
*/

//TODO: Fill out season dates. 

public class InputParsing {
	private static final String separator = "\\s*\\-\\s*";

	private static final String timestampPattern = "\\d{1,2}:\\d\\d\\s+\\d\\w\\w";
	private static final String timeIntervalPattern = timestampPattern + separator + timestampPattern;

	private static final String datePattern = "\\d\\d/\\d\\d/\\d\\d\\d\\d";
	private static final String dateIntervalPattern = datePattern + separator + datePattern;
	private static final String yearPattern = "\\d\\d\\d\\d";
	private static final String yearIntervalPattern = yearPattern + separator + yearPattern;
	private static final String seasonFormatPattern = "[\\w&&[\\D]]+";
	private static final String seasonPattern = yearIntervalPattern + seasonFormatPattern;

	//Lists of dates of the start and end of each year's preseason, regular season, all-star weekend, and playoffs.
	//BaseYear is the first season with data, no earlier seasons can be used.
	private static final Year baseYear = Year.of(2012);
	private static final DateTimeFormatter dateFormat = DateTimeFormatter.ofPattern("MM/dd/yyyy");
	private static final List<Pair<LocalDate, LocalDate>> preseasonDates = List.of(
		new Pair<LocalDate, LocalDate>(LocalDate.of(2012, 10, 5), LocalDate.of(2012, 10, 26)),
		new Pair<LocalDate, LocalDate>(LocalDate.of(2013, 10, 5), LocalDate.of(2013, 10, 25)),
		new Pair<LocalDate, LocalDate>(LocalDate.of(2014, 10, 4), LocalDate.of(2014, 10, 24)),
		new Pair<LocalDate, LocalDate>(LocalDate.of(2015, 10, 2), LocalDate.of(2015, 10, 23)),
		new Pair<LocalDate, LocalDate>(LocalDate.of(2016, 10, 1), LocalDate.of(2016, 10, 21)),
		new Pair<LocalDate, LocalDate>(LocalDate.of(2017, 9, 30), LocalDate.of(2017, 10, 13)),
		new Pair<LocalDate, LocalDate>(LocalDate.of(2018, 9, 28), LocalDate.of(2018, 10, 12)),
		new Pair<LocalDate, LocalDate>(LocalDate.of(2019, 9, 30), LocalDate.of(2019, 10, 18))
	);
	private static final List<Pair<LocalDate, LocalDate>> regularSeasonDates = List.of(
		new Pair<LocalDate, LocalDate>(LocalDate.of(2012, 10, 30), LocalDate.of(2013, 4, 17)),
		new Pair<LocalDate, LocalDate>(LocalDate.of(2013, 10, 29), LocalDate.of(2014, 4, 16)),
		new Pair<LocalDate, LocalDate>(LocalDate.of(2014, 10, 28), LocalDate.of(2015, 4, 15)),
		new Pair<LocalDate, LocalDate>(LocalDate.of(2015, 10, 27), LocalDate.of(2016, 4, 13)),
		new Pair<LocalDate, LocalDate>(LocalDate.of(2016, 10, 25), LocalDate.of(2017, 4, 12)),
		new Pair<LocalDate, LocalDate>(LocalDate.of(2017, 10, 17), LocalDate.of(2018, 4, 11)),
		new Pair<LocalDate, LocalDate>(LocalDate.of(2018, 10, 16), LocalDate.of(2019, 4, 10)),
		new Pair<LocalDate, LocalDate>(LocalDate.of(2019, 10, 22), LocalDate.of(2020, 4, 15))
	);
	private static final List<Pair<LocalDate, LocalDate>> allStarWeekendDates = List.of(
		new Pair<LocalDate, LocalDate>(LocalDate.of(2013, 2, 15), LocalDate.of(2013, 2, 17)),
		new Pair<LocalDate, LocalDate>(LocalDate.of(2014, 2, 14), LocalDate.of(2014, 2, 16)),
		new Pair<LocalDate, LocalDate>(LocalDate.of(2015, 2, 13), LocalDate.of(2015, 2, 15)),
		new Pair<LocalDate, LocalDate>(LocalDate.of(2016, 2, 12), LocalDate.of(2016, 2, 14)),
		new Pair<LocalDate, LocalDate>(LocalDate.of(2017, 2, 17), LocalDate.of(2017, 2, 19)),
		new Pair<LocalDate, LocalDate>(LocalDate.of(2018, 2, 16), LocalDate.of(2018, 2, 18)),
		new Pair<LocalDate, LocalDate>(LocalDate.of(2019, 2, 15), LocalDate.of(2019, 2, 17)),
		new Pair<LocalDate, LocalDate>(LocalDate.of(2020, 2, 14), LocalDate.of(2020, 2, 16))
	);
	private static final List<Pair<LocalDate, LocalDate>> playoffsDates = List.of(
		new Pair<LocalDate, LocalDate>(LocalDate.of(2013, 4, 20), LocalDate.of(2013, 6, 3)),
		new Pair<LocalDate, LocalDate>(LocalDate.of(2014, 4, 19), LocalDate.of(2014, 5, 31)),
		new Pair<LocalDate, LocalDate>(LocalDate.of(2015, 4, 18), LocalDate.of(2015, 5, 27)),
		new Pair<LocalDate, LocalDate>(LocalDate.of(2016, 4, 16), LocalDate.of(2016, 5, 30)),
		new Pair<LocalDate, LocalDate>(LocalDate.of(2017, 4, 15), LocalDate.of(2017, 5, 25)),
		new Pair<LocalDate, LocalDate>(LocalDate.of(2018, 4, 14), LocalDate.of(2018, 5, 28)),
		new Pair<LocalDate, LocalDate>(LocalDate.of(2019, 4, 15), LocalDate.of(2019, 5, 25)),
		new Pair<LocalDate, LocalDate>(LocalDate.of(2020, 4, 18), LocalDate.of(2020, 6, 3))
	);
	private static final List<Pair<LocalDate, LocalDate>> finalsDates = List.of(
		new Pair<LocalDate, LocalDate>(LocalDate.of(2013, 6, 6), LocalDate.of(2013, 6, 20)),
		new Pair<LocalDate, LocalDate>(LocalDate.of(2014, 6, 5), LocalDate.of(2014, 6, 15)),
		new Pair<LocalDate, LocalDate>(LocalDate.of(2015, 6, 4), LocalDate.of(2015, 6, 16)),
		new Pair<LocalDate, LocalDate>(LocalDate.of(2016, 6, 2), LocalDate.of(2016, 6, 19)),
		new Pair<LocalDate, LocalDate>(LocalDate.of(2017, 6, 1), LocalDate.of(2017, 6, 12)),
		new Pair<LocalDate, LocalDate>(LocalDate.of(2018, 5, 31), LocalDate.of(2018, 6, 8)),
		new Pair<LocalDate, LocalDate>(LocalDate.of(2019, 5, 30), LocalDate.of(2019, 6, 15)),
		new Pair<LocalDate, LocalDate>(LocalDate.of(2020, 6, 4), LocalDate.of(2020, 6, 21))
	);
	
	private InputParsing(){}
	
	/**Parses the given file and returns the game Source specified by the playsrc key.
	 * @param inputFile Path to the input file
	 * @throws IOException If an IO error occurs.
	 * @throws NullPointerException if inputFile is null.
	 * @throws JSONException if the input file is not a JSON file, or if the JSON does not meet the standards described above.
	 * @return the game Source specified by the playsrc key.
	 */
	public static GameSource parseSource(String inputFile) throws IOException {
		return parseSource(new JSONObject(Files.readString(Path.of(inputFile))).getInt("playsrc"));
	}
	
	/**Returns the source corresponding to the given int according to the above specification.
	 * @param src int corresponding to a source.
	 * @throws JSONException if the input does not correspond to a source.
	 * @return The Game Source corresponding to the given int.
	 */
	public static GameSource parseSource(int src) throws IOException {
		if(src == 0){
			return AdvancedStats.open();
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
		return parseDataset(new JSONObject(Files.readString(Path.of(inputFile))).getJSONArray("dataset"));
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
		Collection<Pair<LocalDate, LocalDate>> dates = new LinkedList<Pair<LocalDate, LocalDate>>();
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
			return List.of(new Pair<LocalDate, LocalDate>(d, d));
		} else if(Pattern.matches(dateIntervalPattern, input)){
			String[] split = input.split(separator);
			LocalDate d1 = parseDate(split[0]);
			LocalDate d2 = parseDate(split[1]);
			if(d2.isBefore(d1)) {
				throw new JSONException("Second date cannot be before first date: " + input);
			}
			return List.of(new Pair<LocalDate, LocalDate>(d1, d2));
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
			Collection<Pair<LocalDate, LocalDate>> dates = new LinkedList<Pair<LocalDate, LocalDate>>();
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
		if(d.isBefore(preseasonDates.get(0).first())){
			throw new JSONException("Cannot parse dates before the 2012-13 season: " + input);
		}
		return d;
	}

	//Parses a collection of beginning and end dates from a Year (first year of the season), and info string according to the above specification.
	private static Collection<Pair<LocalDate, LocalDate>> parseSeason(Year year, String info){
		Collection<Pair<LocalDate, LocalDate>> dates = new LinkedList<Pair<LocalDate, LocalDate>>();
		boolean detected = false;
		int index = year.getValue() - baseYear.getValue();
		info = info.toLowerCase();
		if(info.contains("r")){
			detected = true;
			dates.add(new Pair<LocalDate, LocalDate>(regularSeasonDates.get(index).first(), allStarWeekendDates.get(index).first()));
			dates.add(new Pair<LocalDate, LocalDate>(allStarWeekendDates.get(index).second(), regularSeasonDates.get(index).second()));
		}
		if(info.contains("p")){
			detected = true;
			dates.add(playoffsDates.get(index));
		}
		if(info.contains("e")){
			detected = true;
			dates.add(preseasonDates.get(index));
		}
		if(info.contains("a")){
			detected = true;
			dates.add(allStarWeekendDates.get(index));
		}
		if(info.contains("f")){
			detected = true;
			dates.add(finalsDates.get(index));
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
		return parseTeams(new JSONObject(Files.readString(Path.of(inputFile))).getJSONArray("datasetteam"));
	}
	
	/**Returns a collection of team given a JSONArray according to the above specification.
	 * @param t The JSON array.
	 * @throws NullPointerException if t is null.
	 * @throws JSONException if the JSON array does not meet the specification.
	 * @return a collection of team given a JSONArray according to the above specification.
	 */
	public static Collection<Team> parseTeams(JSONArray t){
		if(t == null){
			return new LinkedList<Team>();
		}
		Collection<Team> teams = new LinkedList<Team>();
		for(Object o : t){
			if(!(o instanceof String)){
				throw new JSONException("Non-string in Team array: " + o.toString());
			}
			teams.add(parseTeam((String)o));
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
		return parseConstraints(new JSONObject(Files.readString(Path.of(inputFile))).getJSONArray("constraints"));
	}

	/**Parses the given JSON array and returns constraints according to the above specification.
	 * @param source The JSON array
	 * @throws NullPointerException if source is null.
	 * @throws JSONException if the JSON array does not meet the given specification.
	 * @return
	 */
	public static Collection<Constraint> parseConstraints(JSONArray source){
		Collection<Constraint> answer = new LinkedList<Constraint>();
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
			return new OrConstraint(parseConstraints(jo.getJSONArray(s)).toArray(new Constraint[0]));
		} else if(constraintType.equals("and")){
			return new AndConstraint(parseConstraints(jo.getJSONArray(s)).toArray(new Constraint[0]));
		} else if(constraintType.equals("not")){
			return new NotConstraint(parseConstraint(jo.get(s)));
		} else {
			throw new JSONException("Unknown constraint type: " + s);
		}
	}

	//Parses a constraint from a string.
	private static Constraint parseStringConstraint(String input){
		String simplifiedInput = input.trim().toLowerCase();
		String[] split = simplifiedInput.split("\\s+", 2);
		if(split.length < 2){
			throw new JSONException("Unknown constraint: " + input);
		}
		String constraintType = split[0];
		String constraint = split[1];
		if(constraintType.equals("player:")){
			return parsePlayer(constraint);
		} else if(constraintType.equals("team:")){
			return parseTeam(constraint);
		} else if(constraintType.equals("type:")){
			return parsePlayType(constraint);
		} else if(constraintType.equals("time:")){
			return parseTimeInterval(constraint);
		} else {
			throw new JSONException("Unknown constraint type: " + constraintType);
		}
	}

	//Parses a player from the given string.
	private static Player parsePlayer(String constraint){
		String[] playerNames = constraint.split("\\s+", 2);
		if(playerNames.length == 1){
			return Player.get(null, playerNames[0]);
		} else {
			return Player.get(playerNames[0], playerNames[1]);
		}
	}

	//Parses a team from the given string.
	private static Team parseTeam(String constraint){
		Team t = Team.getNBATeam(constraint.trim().toLowerCase());
		if(t == null){
			throw new JSONException("Unknown team: " + constraint);
		}
		return t;
	}

	//Parses a play type from the given string.
	private static PlayType parsePlayType(String constraint){
		PlayType p = PlayType.parse(constraint);
		if(p == null){
			throw new JSONException("Unknown play type: " + constraint);
		}
		return p;
	}

	//Parses a time interval from the given string.
	private static TimeInterval parseTimeInterval(String constraint){
		if(constraint.equals("ot")){
			//An assumption is made that there will be no overtime past the 11th overtime.
			return new TimeInterval(new Timestamp(5, 300), new Timestamp(15, 0));
		} else if(Pattern.matches(timeIntervalPattern, constraint)){
			String[] patternSplit = constraint.split(separator);
			String firstTimestamp = patternSplit[0];
			String secondTimestamp = patternSplit[1];
			return new TimeInterval(parseTimestamp(firstTimestamp), parseTimestamp(secondTimestamp));
		} else {
			int quarter;
			try {
				quarter = Timestamp.parseQuarter(constraint);
			} catch(IllegalArgumentException e) {
				throw new JSONException("Malformed quarter: " + constraint, e);
			}
			
			if(quarter > 4){
				return new TimeInterval(new Timestamp(quarter, 300), new Timestamp(quarter, 0));
			} else if(quarter > 0){
				return new TimeInterval(new Timestamp(quarter, 720), new Timestamp(quarter, 0));
			} else {
				throw new JSONException("Could not parse timestamp: " + constraint);
			}
		}
	}

	//Parses a timestamp from a string.
	private static Timestamp parseTimestamp(String constraint){
		try{
			return Timestamp.parse(constraint);
		} catch(IllegalArgumentException e){
			throw new JSONException("Malformed timestamp : " + constraint, e);
		}
	}
}