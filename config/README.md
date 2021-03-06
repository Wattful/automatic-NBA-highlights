The program reads settings and input from various JSON files.

Default versions of these files are located in the [config/default](/default) folder.

After looking at the config files and editing the settings, copy them from the config/default folder to the config folder, which is where the program looks for config files.

Note that the locations and names for the config files (NOT the input.json file) are hardcoded, so changing their locations or names will lead to a runtime error. (I know, I know, bad application design).

Here are the specifications for the config and input files:

# advancedstatsconfig.json
advancedstatsconfig.json contains options for the NBA Advanced Stats source.

There are four keys, two of which are mandatory.

* read - boolean. Mandatory key. If true, Advanced Stats will read locally stored play-by-play data. If false, will always use browser to get data.
* write - boolean. Mandatory key. If true, Advanced Stats will locally store all data read during runtime. If false, will not store data.
* readLocation - String. Optional. Specifies the location of an Advanced Stats JSON data file to read from. If not specified, defaults to "./advancedstatsdata.json". Ignored if read is false.
* writeLocation - String. Optional. Specifies where to write Advanced Stats data. If not specified, defaults to "./advancedstatsdata.json". Ignored if write is false.

# browserconfig.json
Sources use a web browser to collect data from the internet.

browserconfig.json contains options regarding which browser to use and how to use it.

The program supports three browsers: Mozilla Firefox, Google Chrome, and Microsoft Edge.

In order for the program to use a browser, you must install that browser's driver.

A driver is an executable which allows programs to automate browser operation.

Drivers for each browser can be found here: [Firefox](https://github.com/mozilla/geckodriver/releases), [Chrome](https://chromedriver.chromium.org/), [Edge](https://developer.microsoft.com/en-us/microsoft-edge/tools/webdriver/)

browserconfig.json contains four keys, each of which is mandatory.

* name - String indicating which browser to use. Must be "firefox", "chrome", or "edge", case insensitive.
* headless - boolean. If true, the browser is run in headless mode, meaning that it is not drawn on screen. If false, draws the browser on screen.
* suppressOutput - boolean. If true, suppresses the driver's output. If false, forwards the driver's output to System.out.
* executable - String. Absolute path of the browser driver executable, including the file name. This variable MUST be set for the program to run.

Note that Microsoft Edge supports neither headless mode nor suppressing of output.

# input.json
input.json contains the user's input for the program.

It contains four keys, each of which are mandatory.

* playsrc - Integer. Indicates which Source to use in getting play-by-play and video data. Currently there is only one source, NBA Advanced Stats, which corresponds to a value of 0.
* dataset - Array of Strings. Represents which games to get plays from. Each String corresponds to a group of games to include in the dataset. There are two ways to specify games: Dates and Seasons.
	* Dates - can be represented as either one date or two dates.
			One date includes all games from that day, while two dates includes all games from between those dates, inclusive.
			Dates are represented in MM/DD/YYYY format, with multiple dates separated by a dash.
			For example, if one wants to include games only on January 7, 2020, the String would be `"01/07/2020"`.
			If one wanted to include games from December 30, 2019 to January 6, 2020, the String would be `12/30/2019-01/06/2020`.
	* Seasons - includes all games in certain seasons.
			A season is represented as two years separated by a dash, followed by multiple letters specifying which parts of the season to include.
			The letters are as follows:
				* r - regular season
				* p - playoffs, not including the NBA finals
				* f - NBA finals
				* e - preseason
				* a - all-star game and the rising stars challenge
					(Note that the only current source does NOT have video for all star games).
			The order and case of the letters do not matter.
			For example, `"2019-2020r"` Includes regular season games from the 2019-20 season.
			`"2016-2017pef"` includes playoff, finals and preseason games from the 2016-17 season.
			If one wishes to include multiple consecutive seasons, they can put the first year as the first year of the starting season, 
			and the second year as the last year of the final season.
				For example, `"2015-2019ra"` includes regular season and all-star games from the 2015-16 season to the 2018-19 season.
* datasetteam - Array of Strings. Represents teams to limit the dataset to.
		Each string represents a team. If specified, the dataset will be limited to games played by the listed teams.
		Team names must be listed in full, case insensitive (A list of NBA team's full, official names can be found in thybulle.highlights.Team). 
		For example, `["Milwaukee Bucks"]` will limit the dataset to games played by the Milwaukee Bucks,
		and `["Philadelphia 76ers", "golden state warriors"]` will limit the dataset to games played by Philadelphia OR Golden State.
		If the array is empty, games from ALL teams will be included in the dataset.
		Note that for simplicity, all-star games are affected by this limit.
* constraints - An array of constraints. Each constraint is represented as either a String or a JSON object with a single key.
		All plays that satisfy all of the specified constraints will be included in the final video. All constraints are case-insensitive.
		Constraints can be divided into two groups: single constraints and compound constraints. Single constraints consist of a String, and are satisfied if some property of the play matches the constraint. Compound constraints consist of a JSON object with a single key pointing to other constraints, and are satisfied if some combination of the constraints it is pointing to are or are not satisfied. First, we'll look at single constraints.
	* Player - Formatted as `"Player: PLAYER_NAME"`. A play satisfies this constraint 
	if it involves this player. 
	For example, a play will satisfy `"Player: Giannis Antetokounmpo"` if it involves Giannis Antetokounmpo.
	* Team - Formatted as `"Team: TEAM_NAME"`. A play satisfies this constraint if it was committed by this team. 
	The string must be a team's official, full name (See [thybulle.highlights.Team](../src/thybulle/highlights/Team.java) for a list of NBA teams and their official, full names.)
	For example, a play will satisfy `"Team: Portland Trail Blazers"` if it was committed by the Portland Trail Blazers.
	* Play type - Formatted as `"Type: TYPE_NAME"`. A play satisfies this constraint if it is of this play type.
	All play types are defined as enum constants in [thybulle.highlights.PlayType](../src/thybulle/highlights/PlayType.java). The given type must match a play type defined in that class exactly (case-insensitive), with the exception that underscores can be replaced with spaces.
	For example, the strings `"Type: AND_ONE_DUNK"`, `"Type: AND ONE DUNK"`, `"Type: and_one_dunk"`, and `"Type: AND ONE_DuNk"` all correspond to the PlayType.AND_ONE_DUNK enum constant.
	* Time of game - Formatted as `"Time: TIME_OF_GAME"`. A play satisfies this constraint if it is within
	the specified time of game. Time of game can be specified in two ways: 
		* Quarter - The play occurs within the specified quarter or overtime period. Acceptable strings for this format include `"1st"`, 
			`"2nd"`, `"3rd"`, `"4th"`, `"ot"`, `"1ot"`, `"2ot"`, and so on.
			For example, a play will satisfy `"Time: 1st"` if it occurred in the first quarter.
			Note that `"ot"` specifies *any* overtime period, not just the first overtime. 
			If one only wants to include the first overtime, they should use `"1ot"`.
		* Specific time - The play occurs between the specified times in the game. A time is represented as a number of minutes and a number of seconds,
			followed by the quarter or overtime period. Note that the quarter name must be separated from the time by at least a space.
			The two times are separated by a dash.
			For example, a play will satisfy `"Time: 03:00 4th-00:00 1ot"` if it occurs between three minutes left in the fourth and the end of  
			first ovetime, inclusive.
	* Score - Formatted as `"Score: SCORE"`. The score constraint specifies whether the team committing the play is winning or losing, and by how much. 
	This constraint considers a team's "relative score" - how much they are winning or losing by - as opposed to the game's absolute score.
	A team's relative score is equal to their score minus their opponent's score. In other words, if a team is winning, their relative score will be how much they are winning by, if a team is losing, their relative score will be -1 times how much they are losing by, and if the game is tied, the relative score for both teams is 0. This means that at any point in time, the sum of both team's relative scores is 0.
	Note that when looking at made field goals, this constraint considers the team's relative score **before** the made field goal.
	There are three possible ways to specify a score constraint:
		* Exact score: consists of a single integer. Matches plays for which the committing team has this exact relative score. For example, `"Score: 1"` will match plays in which the committing team is winning by one.
		* Below or above exact score: consists of a single integer followed by a `+` or `-`. Matches plays for which the committing team has a relative score below or above the given number, inclusive. For example, `"Score: -1+"` will match plays where the committing team is losing by one, the game is tied, or the committing team is winning.
		* Range: consists of two integers separated by a `~` (a tilde is used to avoid confusion with negative numbers). Matches plays for which the committing team has a relative score within the given range, inclusive. It does not matter whether the first or second integer is larger. For example, `"Score: -3~3"` will include plays which happen when the score is within three, and `"Score: -3~-6"` will include plays in which the committing team is losing by 3 to 6 points.
	* Custom Constraint - Users can define their own custom single constraints. In order to do so, they must define a class which does the following:
		1. Implements the [thybulle.highlights.Constraint](../src/thybulle/highlights/Constraint.java) interface. This interface defines a single method, satisfiedBy, which takes in a [thybulle.highlights.Play](../src/thybulle/highlights/Play.java) object and returns a boolean indicating whether the given play satisfies the constraint. See the [thybulle.highlights.Play](../src/thybulle/highlights/Play.java) object for more information on its public interface.
		2. Defines either:
			* A public constructor which takes a String as its only argument.
			* A public static method called `parse` which takes a String as its only argument and returns an instance of itself.
			This method or constructor should throw IllegalArgumentException or return null if the String input could not be parsed correctly.
		Custom constraints are formatted as `"CLASS_NAME[: INPUT]`. `CLASS_NAME` is the fully qualified Java class name of the custom constraint (note that a fully qualified class name **IS** case sensitive). `INPUT` is the String input which will be passed to that class' constructor or parse method. Whitespace at the beginning and end of the input will be removed, and the input will be converted to lowercase. If `: INPUT` is omitted, the String passed to the constructor or parse method will be null. If the class defines both a String constructor and a parse method, the parse method will be called. If the given class does not exist or does not meet the required qualifications, an exception will be thrown and the program will terminate.

		Next, we'll look at the compound constraints. Note that unlike single constraints, there is no way to define a custom compound constraint.
	* Not - Single key is `"NOT"`, corresponding to a value of a single constraint.
	A play satisfies this constraint if it does not satisfy the constraint in the value.
	For example, a play will satisfy `{"Not" : "Type: "Three Pointer Attempt"}` if it is not a three point attempt.
	* And - Single key is `"AND"`, corresponding to a value of an array of constraints.
	A play satisfies this constraint if it satisfies all of the constraints in the array.
	For example, a play will satisfy `{"AND" : ["Time: 3rd", "Team: Minnesota Timeberwolves"]}}` if it occurred in the third quarter and was committed
	by the Minnesota Timberwolves.
	* Or - Single key is `"OR"`, (case insensitive), corresponding to a value of an array of constraints.
	A play satisfies this constraint if it satisfies any of the constraints in the array.
	For example, a play will satisfy `{"or" : ["Player: Lebron James", "Player: Anthony Davis"]}` if it was committed 
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
