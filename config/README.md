The program reads settings and input from various JSON files.

Default versions of these files are located in this folder, and specifications for these files follow.

Note that the locations and names for the config files (NOT the input.json file) are hardcoded, so changing their locations or names will lead to a runtime error. (I know, I know, bad application design).

#advancedstatsconfig.json
advancedstatsconfig.json contains options for the NBA Advanced Stats source.

There are two keys, both of which are mandatory.

* read - boolean. If true, Advanced Stats will read locally stored play-by-play data. If false, will always use browser to get data.
* write - boolean. If true, Advanced Stats will locally store all data read during runtime. If false, will not store data.

#browserconfig.json
Sources use a web browser to collect data from the internet.

browserconfig.json contains options regarding which browser to use and how to use it.

The program supports three browsers: Mozilla Firefox, Google Chrome, and Microsoft Edge.

In order for the program to use a browser, you must install that browser's driver.

A driver is an executable which allows programs to automate browser operation.

Drivers for each browser can be found here: [Firefox](), [Chrome](), [Edge]()

browserconfig.json contains four keys, each of which is mandatory.

* name - String indicating which browser to use. Must be "firefox", "chrome", or "edge", case insensitive.
* headless - boolean. If true, the browser is run in headless mode, meaning that it is not drawn on screen. If false, draws the browser on screen.
* suppressOutput - boolean. If true, suppresses the driver's output. If false, forwards the driver's output to System.out.
* executable - String. Absolute path of the browser driver executable, including the file name. This variable MUST be set for the program to run.

Note that Microsoft Edge supports neither headless mode nor suppressing of output.

#input.json
input.json contains the user's input for the program.

It contains four keys, each of which are mandatory.

* playsrc - Integer. Indicates which Source to use in getting play-by-play and video data. Currently there is only one source, NBA Advanced Stats, which corresponds to a value of 0.
* dataset - Array of Strings. Represents which games to get plays from. Each String corresponds to a group of games to include in the dataset. There are two ways to specify games: Dates and Seasons.
	** Dates - can be represented as either one date or two dates.
			One date includes all games from that day, while two dates includes all games from between those dates, inclusive.
			Dates are represented in MM/DD/YYYY format, with multiple dates separated by a dash.
			For example, if one wants to include games only on January 7, 2020, the String would be `"01/07/2020"`.
			If one wanted to include games from December 30, 2019 to January 6, 2020, the String would be `12/30/2019-01/06/2020`.
	** Seasons - includes all games in certain seasons.<br>
			A season is represented as two years separated by a dash, followed by multiple letters specifying which parts of the season to include.
			The letters are as follows:
				*** r - regular season
				*** p - playoffs, not including the NBA finals
				*** f - NBA finals
				*** e - preseason
				*** a - all-star game and the rising stars challenge
					(Note that the only current source does NOT have video for all star games).
			The order and case of the letters do not matter.
			For example, {@code "2019-2020r"} Includes regular season games from the 2019-20 season.
			{@code "2016-2017pef"} includes playoff, finals and preseason games from the 2016-17 season.
			If one wishes to include multiple consecutive seasons, they can put the first year as the first year of the starting season, 
			and the second year as the last year of the final season.
				For example, {@code "2015-2019ra"} includes regular season and all-star games from the 2015-16 season to the 2018-19 season.
* datasetteam - Array of Strings. Represents teams to limit the dataset to.
		Each string represents a team. If specified, the dataset will be limited to games played by the listed teams.
		Team names must be listed in full, case insensitive (A list of NBA team's full, official names can be found in the highlights/Team.java class). 
		For example, `["Milwaukee Bucks"]` will limit the dataset to games played by the Milwaukee Bucks,
		and `["Philadelphia 76ers", "golden state warriors"]` will limit the dataset to games played by Philadelphia OR Golden State.
		If the array is empty, games from ALL teams will be included in the dataset.
		Note that for simplicity, all-star games are affected by this limit.
* constraints - An array of constraints. Each constraint is represented as either a String or a JSON object with a single key.
		All plays that satisfy all of the specified constraints will be included in the final video.
		The constraints are as follows:
		** Player - Represented as a String, case-insensitive, formatted as `"Player: PLAYER_NAME"`. A play satisfies this constraint 
			if it involves this player. 
			For example, a play will satisfy `"Player: Giannis Antetokounmpo"` if it involves Giannis Antetokounmpo.
		** Team - Represented as a String, case-insensitive, formatted as {`"Team: TEAM_NAME"`. A play satisfies this constraint if it was committed by this team. 
			The string must be a team's official, full name (See `highlights/Team.java` for a list of NBA teams and their official, full names.)
			For example, a play will satisfy {@code "Team: Portland Trail Blazers"} if it was committed by the Portland Trail Blazers.
		** Play type - Represented as a String, case-insensitive, formatted as {@code "Type: TYPE_NAME"}. A play satisfies this constraint if it is of this play type.
			All play types are defined as enum constants in highlights/PlayType.java. The given type must match a play type defined in that class exactly (case insensitive), with the exception that underscores can be replaced with spaces.
			For example, the strings `"Type: AND_ONE_DUNK"`, `"Type: AND ONE DUNK"`, `"Type: and_one_dunk"`, and `"Type: AND ONE_DuNk"` all correspond to the PlayType.AND_ONE_DUNK enum constant.
		** Time of game - Represented as a String, case-insensitive, formatted as {@code "Time: TIME_OF_GAME"}. A play satisfies this constraint if it is within
			the specified time of game. Time of game can be specified in two ways: 
				*** Quarter - The play occurs within the specified quarter or overtime period. Acceptable strings for this format include {@code "1st"}, 
					{@code "2nd"}, {@code "3rd"}, {@code "4th"}, {@code "ot"}, {@code "1ot"}, {@code "2ot"}, and so on.
					For example, a play will satisfy {@code "Time: 1st"} if it occurred in the first quarter.
					Note that "ot" specifies *any* overtime period, not just the first overtime. 
					If one only wants to include the first overtime, they should use "1ot".

				*** Specific time - The play occurs between the specified times in the game. A time is represented as a number of minutes and a number of seconds,
					followed by the quarter or overtime period. Note that the quarter name must be separated from the time by at least a space.
					The two times are separated by a dash.
					For example, a play will satisfy {@code "Time: 03:00 4th-00:00 1ot"} if it occurs between three minutes left in the fourth and the end of  
					first ovetime, inclusive.
		** Not constraint - Represented as an object with a single key, `"NOT"` (case insensitive), corresponding to a value of a single constraint.
			A play satisfies this constraint if it does not satisfy the constraint in the value.
			For example, a play will satisfy `{"Not" : "Type: "Three Pointer Attempt"}` if it is not a three point attempt.
		** And constraint - Represented as an object with a single key, `"AND"` (case insensitive), corresponding to a value of an array of constraints.
			A play satisfies this constraint if it satisfies all of the constraints in the array.
			For example, a play will satisfy `{"AND" : ["Time: 3rd", "Team: Minnesota Timeberwolves"]}}` if it occurred in the third quarter and was committed
			by the Minnesota Timberwolves.
		** Or constraint - Represented as an object with a single key, `"OR"` (case insensitive), corresponding to a value of an array of constraints.
			A play satisfies this constraint if it satisfies any of the constraints in the array.
			For example, a play will satisfy {@code {"or" : ["Player: Lebron James", "Player: Anthony Davis"]}} if it was committed 
			by either Lebron James or Anthony Davis.
		These constraints can be nested within each other.
		For an overall example, if the constraints key points to
		```
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
		]}```,
		The video will include all of James Harden's field goals, assists, and rebounds, except for his overtime dunks.

The included example version of input.json will include all of Ben Simmons' dunks and steals from the 2019-2020 regular season.