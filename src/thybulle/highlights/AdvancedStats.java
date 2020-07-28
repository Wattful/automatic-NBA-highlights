package thybulle.highlights;

import static thybulle.highlights.HighlightsLogger.*;

import thybulle.misc.*;
import java.util.*;
import java.util.regex.*;
import java.time.*;
import java.io.*;
import java.net.URL;
import java.time.Duration;
import java.nio.file.*;
import java.net.MalformedURLException;

import org.jsoup.*;
import org.jsoup.nodes.*;
import org.jsoup.select.*;
import org.openqa.selenium.support.ui.*;
import org.openqa.selenium.*;
import org.openqa.selenium.remote.*;
import com.google.common.base.Function;
import com.google.common.base.Predicate;

import org.json.*;

/**Class which parses play-by-play data from stats.nba.com.<br>
stats.nba.com is an excellent source for play-by-play data.<br>
It includes pre-edited videos of almost all plays, with the notable exception of most dead-ball turnovers.<br>
Its only major weakness is that it is impossible to tell the difference between players on the same team with the same last name.<br>
Any plays committed by either player are attributed to the one which shows up first in the box score.<br>
This source has the ability to save its data locally on the user's machine.<br>
Doing so may reduce the overall runtime of the program from up to several hours down to a few minutes.<br>
The options to read and write data can be specified in the JSON file "./advancedstatsconfig.json".
*/

public class AdvancedStats implements GameSource {
	private WebDriver driver;
	private final boolean read;
	private final boolean write;
	private JSONObject data;
	private final String writeLocation;

	private static final Browser browser;

	private static final String DEFAULT_DATA_LOCATION = "./advancedstatsdata.json";
	private static final String DEFAULT_CONFIG_PATH = "../config/advancedstatsconfig.json";

	private static final long DEFAULT_TIMEOUT = 10000;
	private static final long VIDEO_TIMEOUT = 60000;
	private static final long MINIMUM_TIMEOUT = 1000;
	private static final int MAX_RETRIES = 3;
	
	private static final Map<GameInfo, Game> interning = new HashMap<GameInfo, Game>();
	private static final Map<GameInfo, String> links = new HashMap<GameInfo, String>();
	private static final Map<Pair<String, String>, PlayType> playTypeParsing = new LinkedHashMap<Pair<String, String>, PlayType>();
	private static final Map<String, Team> teamAbbreviations = new HashMap<String, Team>();
	
	private static final String playerRegexNoGroup = "(?!MISS).+?";
	private static final String playerRegex = "(" + playerRegexNoGroup + ")";
	private static final String distanceRegex = "(?:\\d{1,2}' )?";
	private static final String shotModifiers = "(?:Jump|Alley Oop|Reverse|Finger Roll|Running|Layup|Tip|Putback|Turnaround|Bank|Hook|Step Back|Floating|Pullup|Pull-Up|Cutting|Fadeaway|Driving|Tip|Jumper|Shot| )*";
	private static final String turnoverModifiers = "(?:Lost Ball|Bad Pass|Offensive Foul|Basket Interference|5 Second Violation|Lane Violation|Step Out of Bounds|Out of Bounds Lost Ball|Traveling|Out of Bounds \\- Bad Pass Turnover|3 Second Violation|Palming|Backcourt|Double Dribble|Discontinue Dribble|Kicked Ball Violation|Inbound|No| )*";
	
	private static final String dunkMadeRegex = playerRegex + " " + distanceRegex + shotModifiers + "Dunk (?:Shot )?\\(\\d+ PTS\\).*";
	private static final String missedDunkRegex = "MISS " + playerRegex + " " + distanceRegex + shotModifiers + "Dunk(?: Shot)?";
	private static final String threePointerMadeRegex = playerRegex + " " + distanceRegex + "3PT" + shotModifiers + " \\(\\d+ PTS\\).*";
	private static final String missedThreePointerRegex = "MISS " + playerRegex + " " + distanceRegex + "3PT" + shotModifiers;
	private static final String fieldGoalMadeRegex = playerRegex + " " + distanceRegex + shotModifiers + " \\(\\d+ PTS\\).*";
	private static final String missedFieldGoalRegex = "MISS " + playerRegex + " " + distanceRegex + shotModifiers;

	private static final String missedFreeThrowRegex = "MISS " + playerRegex + " Free Throw.*";
	private static final String madeFreeThrowRegex = playerRegex + " Free Throw.*";

	private static final String teamReboundRegex = ".* Rebound";
	private static final String reboundRegex = playerRegex + " REBOUND.*";

	private static final String assistRegex = ".*\\(" + playerRegex + " \\d+ AST\\)";

	private static final String stealRegex = playerRegex + " STEAL \\(\\d+ STL\\)";

	private static final String blockRegex = playerRegex + " BLOCK \\(\\d+ BLK\\)";

	private static final String alleyOopRegex = playerRegex + " " + distanceRegex + shotModifiers + "Alley Oop .* \\(\\d+ PTS\\) \\(" + playerRegex + " \\d+ AST\\)";

	private static final String teamTechnicalRegex = ".* T\\.Foul \\(Def. 3 Sec .*\\).*";
	private static final String flagrantFoul1Regex = playerRegex + " FLAGRANT\\.FOUL\\.TYPE1.*";
	private static final String flagrantFoul2Regex = playerRegex + " FLAGRANT\\.FOUL\\.TYPE2.*";
	private static final String shootingFoulRegex = playerRegex + " S\\.FOUL.*";
	private static final String defensiveFoulRegex = playerRegex + " P\\.FOUL.*";
	private static final String looseBallFoulRegex = playerRegex + " L\\.B\\.FOUL.*";
	private static final String technicalFoulRegex = playerRegex + " T\\.FOUL.*";
	private static final String offensiveFoulRegex = playerRegex + " OFF\\.Foul.*";
	private static final String chargeRegex = playerRegex + " Offensive Charge Foul.*";
	private static final String intentionalFoulRegex = playerRegex + " Personal Take Foul.*";
	private static final String clearPathFoulRegex = playerRegex + " C\\.P\\.FOUL.*";
	private static final String awayFromThePlayFoulRegex = playerRegex + " AWAY\\.FROM\\.PLAY\\.FOUL.*";
	
	private static final String shootingFoulRegexNoGroup = playerRegexNoGroup + " S\\.FOUL.*";
	
	private static final String eightSecondViolationRegex = ".* Turnover: 8 Second Violation \\(T#\\d+\\)";
	private static final String shotClockViolationRegex = ".* Turnover: Shot Clock \\(T#\\d+\\)";
	private static final String teamTurnoverRegex = ".* Turnover: .* \\(T#\\d+\\)";
	private static final String travelingRegex = playerRegex + " Traveling Turnover \\(P\\d+\\.T\\d+\\)";
	private static final String basketInterferenceRegex = playerRegex + " Offensive Goaltending Turnover \\(P\\d+\\.T\\d+\\)";
	
	private static final String jumpBallRegex = "Jump Ball(?:\\(CC\\)| )*" + playerRegex + " vs\\. " + playerRegex + ":.*";
	
	private static final String turnoverRegex = playerRegex + turnoverModifiers + " Turnover \\(P\\d+\\.T\\d+\\)";

	private static final String goaltendingRegex = playerRegex + " Violation:Defensive Goaltending.*";
	private static final String violationRegex = ".* Violation:.*";

	private static final String subRegex = "SUB: " + playerRegex + " FOR " + playerRegex;

	private static final String timeoutRegex = ".* Timeout: .*";
	
	static {
		teamAbbreviations.put("ATL", Team.getNBATeam("Atlanta Hawks"));
		teamAbbreviations.put("BOS", Team.getNBATeam("Boston Celtics"));
		teamAbbreviations.put("BKN", Team.getNBATeam("Brooklyn Nets"));
		teamAbbreviations.put("CHA", Team.getNBATeam("Charlotte Hornets"));
		teamAbbreviations.put("CLE", Team.getNBATeam("Cleveland Cavaliers"));
		teamAbbreviations.put("CHI", Team.getNBATeam("Chicago Bulls"));
		teamAbbreviations.put("DAL", Team.getNBATeam("Dallas Mavericks"));
		teamAbbreviations.put("DEN", Team.getNBATeam("Denver Nuggets"));
		teamAbbreviations.put("DET", Team.getNBATeam("Detroit Pistons"));
		teamAbbreviations.put("GSW", Team.getNBATeam("Golden State Warriors"));
		teamAbbreviations.put("HOU", Team.getNBATeam("Houston Rockets"));
		teamAbbreviations.put("IND", Team.getNBATeam("Indiana Pacers"));
		teamAbbreviations.put("LAC", Team.getNBATeam("LA Clippers"));
		teamAbbreviations.put("LAL", Team.getNBATeam("Los Angeles Lakers"));
		teamAbbreviations.put("MEM", Team.getNBATeam("Memphis Grizzlies"));
		teamAbbreviations.put("MIA", Team.getNBATeam("Miami Heat"));
		teamAbbreviations.put("MIL", Team.getNBATeam("Milwaukee Bucks"));
		teamAbbreviations.put("MIN", Team.getNBATeam("Minnesota Timberwolves"));
		teamAbbreviations.put("NOP", Team.getNBATeam("New Orleans Pelicans"));
		teamAbbreviations.put("NYK", Team.getNBATeam("New York Knicks"));
		teamAbbreviations.put("OKC", Team.getNBATeam("Oklahoma City Thunder"));
		teamAbbreviations.put("ORL", Team.getNBATeam("Orlando Magic"));
		teamAbbreviations.put("PHI", Team.getNBATeam("Philadelphia 76ers"));
		teamAbbreviations.put("PHX", Team.getNBATeam("Phoenix Suns"));
		teamAbbreviations.put("POR", Team.getNBATeam("Portland Trail Blazers"));
		teamAbbreviations.put("SAC", Team.getNBATeam("Sacramento Kings"));
		teamAbbreviations.put("SAS", Team.getNBATeam("San Antonio Spurs"));
		teamAbbreviations.put("TOR", Team.getNBATeam("Toronto Raptors"));
		teamAbbreviations.put("UTA", Team.getNBATeam("Utah Jazz"));
		teamAbbreviations.put("WAS", Team.getNBATeam("Washington Wizards"));
	}

	static {
		playTypeParsing.put(p(missedDunkRegex, null), PlayType.DUNK_MISSED);
		playTypeParsing.put(p(missedThreePointerRegex, null), PlayType.THREE_POINTER_MISSED);
		playTypeParsing.put(p(missedFieldGoalRegex, null), PlayType.FIELD_GOAL_MISSED);
		
		playTypeParsing.put(p(dunkMadeRegex, shootingFoulRegexNoGroup), PlayType.AND_ONE_DUNK);
		playTypeParsing.put(p(dunkMadeRegex, null), PlayType.DUNK_MADE);
		playTypeParsing.put(p(threePointerMadeRegex, shootingFoulRegexNoGroup), PlayType.AND_ONE_THREE_POINTER);
		playTypeParsing.put(p(threePointerMadeRegex, null), PlayType.THREE_POINTER_MADE);
		playTypeParsing.put(p(fieldGoalMadeRegex, shootingFoulRegexNoGroup), PlayType.AND_ONE);
		playTypeParsing.put(p(fieldGoalMadeRegex, null), PlayType.FIELD_GOAL_MADE);

		playTypeParsing.put(p(missedFreeThrowRegex, null), PlayType.FREE_THROW_MISSED);
		playTypeParsing.put(p(madeFreeThrowRegex, null), PlayType.FREE_THROW_MADE);
		
		playTypeParsing.put(p(teamReboundRegex, null), PlayType.TEAM_REBOUND);
		playTypeParsing.put(p(reboundRegex, null), PlayType.REBOUND);

		playTypeParsing.put(p(assistRegex, null), PlayType.ASSIST);

		playTypeParsing.put(p(stealRegex, null), PlayType.STEAL);

		playTypeParsing.put(p(blockRegex, null), PlayType.BLOCK);

		playTypeParsing.put(p(alleyOopRegex, null), PlayType.ALLEY_OOP);

		playTypeParsing.put(p(teamTechnicalRegex, null), PlayType.TEAM_TECHNICAL_FOUL);
		playTypeParsing.put(p(flagrantFoul1Regex, null), PlayType.FLAGRANT_FOUL_1);
		playTypeParsing.put(p(flagrantFoul2Regex, null), PlayType.FLAGRANT_FOUL_2);
		playTypeParsing.put(p(shootingFoulRegex, null), PlayType.SHOOTING_FOUL);
		playTypeParsing.put(p(defensiveFoulRegex, null), PlayType.DEFENSIVE_FOUL);
		playTypeParsing.put(p(looseBallFoulRegex, null), PlayType.LOOSE_BALL_FOUL);
		playTypeParsing.put(p(technicalFoulRegex, null), PlayType.TECHNICAL_FOUL);
		playTypeParsing.put(p(offensiveFoulRegex, null), PlayType.OFFENSIVE_FOUL);
		playTypeParsing.put(p(chargeRegex, null), PlayType.OFFENSIVE_FOUL);
		playTypeParsing.put(p(intentionalFoulRegex, null), PlayType.DEFENSIVE_FOUL);
		playTypeParsing.put(p(clearPathFoulRegex, null), PlayType.DEFENSIVE_FOUL);
		playTypeParsing.put(p(awayFromThePlayFoulRegex, null), PlayType.DEFENSIVE_FOUL);

		playTypeParsing.put(p(eightSecondViolationRegex, null), PlayType.EIGHT_SECOND_VIOLATION);
		playTypeParsing.put(p(shotClockViolationRegex, null), PlayType.SHOT_CLOCK_VIOLATION);
		playTypeParsing.put(p(teamTurnoverRegex, null), PlayType.TEAM_TURNOVER);
		playTypeParsing.put(p(travelingRegex, null), PlayType.TRAVELING);
		playTypeParsing.put(p(basketInterferenceRegex, null), PlayType.BASKET_INTERFERENCE);
		playTypeParsing.put(p(turnoverRegex, null), PlayType.TURNOVER);
		
		playTypeParsing.put(p(jumpBallRegex, null), PlayType.JUMP_BALL);
		
		playTypeParsing.put(p(goaltendingRegex, null), PlayType.GOALTENDING);
		playTypeParsing.put(p(violationRegex, null), PlayType.VIOLATION);
		
		playTypeParsing.put(p(subRegex, null), PlayType.SUBSTITUTION);

		playTypeParsing.put(p(timeoutRegex, null), PlayType.TIMEOUT);
	}

	static {
		try{
			browser = Browser.fromConfigFile(Browser.DEFAULT_CONFIG_PATH);
		} catch(IOException e){
			throw new java.io.UncheckedIOException(e);
		}
	}

	private static <F, S> Pair<F, S> p(F first, S second){
		return new Pair<F, S>(first, second);
	}

	private AdvancedStats(boolean read, boolean write, String readPath, String writePath) {
		this.read = read;
		this.write = write;
		this.writeLocation = writePath;
		File f = new File(readPath);
		if(this.read){
			if(!f.exists()){
				logging.warning("No data file exists at " + readPath + ". Running without pre-read data.");
				this.data = new JSONObject();
			} else {
				try{
					this.data =  new JSONObject(Files.readString(Path.of(readPath)));
				} catch(IOException | JSONException e){
					logging.error("Advanced stats data file at "  + readPath + " was improperly formatted. Running without pre-read data.");
					logging.error(e.getMessage());
					this.data = new JSONObject();
				}
			}
		} else {
			this.data = new JSONObject();
		}
	}

	/**Returns an AdvancedStats instance using the specified booleans to determine whether to read or write local data, and using "./advancedstatsdata.json" at the data location.
	@param read Whether to read local data.
	@param write Whether to write local data.
	@throws IOException if an IO error occurs.
	@return an AdvancedStats instance.
	*/
	public static AdvancedStats open(boolean read, boolean write) {
		return new AdvancedStats(read, write, DEFAULT_DATA_LOCATION, DEFAULT_DATA_LOCATION);
	}

	/**Returns an AdvancedStats instance using the specified booleans to determine whether to read or write local data, and using the given path as the data file location.
	@param read Whether to read local data.
	@param write Whether to write local data.
	@param readLocation Path to the Advanced Stats data file. Can be null if read == false.
	@param writeLocation Path to save any collected data to. Can be null if write == false.
	@throws IOException if an IO error occurs.
	@return an AdvancedStats instance.
	*/
	public static AdvancedStats open(boolean read, boolean write, String readLocation, String writeLocation) {
		return new AdvancedStats(read, write, readLocation, writeLocation);
	}

	/**Returns an AdvancedStats instance using the ./advancedstatsconfig.json config file to determine whether to read or write local data.
	@return an AdvancedStats instance.
	*/
	public static AdvancedStats open() throws IOException {
		JSONObject obj = new JSONObject(Files.readString(Path.of(DEFAULT_CONFIG_PATH)));
		return new AdvancedStats(obj.getBoolean("read"), obj.getBoolean("write"), obj.optString("readLocation", DEFAULT_DATA_LOCATION), obj.optString("writeLocation", DEFAULT_DATA_LOCATION));
	}
	
	private void setup(){
		if(driver == null){
			driver = browser.getDriver();
		}
	}

	private void reset() throws IOException {
		this.close();
		this.setup();
	}

	/**Returns a Game object with play-by-play data for the given GameInfo, 
	or null if the GameInfo does not exist, or if the play-by-play data could not be obtained.
	@param gi The Game Information.
	@throws IOException if an IO error occurs.
	@throws NullPointerException if gi is null.
	@return a Game object with play-by-play data for the given GameInfo
	*/
	public Game getGame(GameInfo gi) throws IOException {
		try{
			synchronized(this){
				return getGameInternal(gi);
			}
		} catch(AdvancedStatsControlFlowException e){
			logging.error("Could not get play-by-play data for " + gi.toString());
			return null;
		}
	}

	private Game getGameInternal(GameInfo gi) throws IOException {
		logging.info("====================================================================================");
		logging.info("Getting play-by-play data for " + gi.toString());
		if(interning.containsKey(gi)){
			logging.info("Found cached play-by-play data.");
			return interning.get(gi);
		} else if(this.read && this.data.has(gi.toString())){
			logging.info("Found stored play-by-play data.");
			Game g = new Game(gi, JSONArrayToPlays(this.data.getJSONArray(gi.toString())));
			interning.put(gi, g);
			return g;
		}
		logging.info("Using browser to get play-by-play data.");
		String url = getLink(gi);
		if(url == null){
			throw new AdvancedStatsControlFlowException("Could not find a game corresponding to the given GameInformation");
		}
		logging.info("Parsing players");
		Element boxScoreBody = renderPage(url, DEFAULT_TIMEOUT, MINIMUM_TIMEOUT, ExpectedConditions.visibilityOfElementLocated(By.className("nba-stat-table"))).body();
		Elements players = boxScoreBody.getElementsByClass("nba-stat-table__overlay");
		if(players.isEmpty()) {
			throw new AdvancedStatsControlFlowException("Could not parse players");
		}
		LinkedHashSet<Player> awayPlayers = getPlayers(players.get(0).getElementsByClass("player"));
		LinkedHashSet<Player> homePlayers = getPlayers(players.get(1).getElementsByClass("player"));

		logging.info("Getting play-by-play data");
		String playByPlayLink = url + "/playbyplay";
		Element playByPlayBody = renderPage(playByPlayLink, DEFAULT_TIMEOUT, MINIMUM_TIMEOUT, ExpectedConditions.visibilityOfElementLocated(By.className("boxscore-pbp__inner"))).body();
		Element table = getTable(playByPlayBody);
		//Yes, this is disgusting. Yes, it is necessary.
		//Each pair represents all plays committed by each team at a certain time.
		//The first value is the away team's plays, the second value is the home team's plays.
		//This is used to classify plays which involve both teams - for example, an and one is a field goal by one team combined with a shooting foul by the other.
		SortedMap<Timestamp, Pair<List<UnparsedPlay>, List<UnparsedPlay>>> rawPlays = getRawPlays(table);
		
		logging.info("Parsing play-by-play data");
		List<AdvancedStatsPlay> plays = parseAllPlays(rawPlays, awayPlayers, homePlayers, gi.awayTeam(), gi.homeTeam());
		
		logging.info("Finished. Found " + plays.size() + (plays.size() == 1 ? "play." : " plays."));
		Game result = new Game(gi, plays);
		interning.put(gi, result);
		if(this.write){
			this.data.put(gi.toString(), playsToJSONArray(plays));
		}
		return result;
	}

	private Element getTable(Element playByPlayBody){
		Elements test = playByPlayBody.getElementsByClass("boxscore-pbp__inner");
		if(test.isEmpty()) {
			throw new AdvancedStatsControlFlowException("Could not get play-by-play");
		}
		Elements test2 = test.get(0).getElementsByAttributeValue("ng-if", "!boxscore.isLive");
		if(test2.isEmpty()) {
			throw new AdvancedStatsControlFlowException("Could not get play-by-play");
		}
		return test2.get(0);
	}

	private SortedMap<Timestamp, Pair<List<UnparsedPlay>, List<UnparsedPlay>>> getRawPlays(Element table){
		SortedMap<Timestamp, Pair<List<UnparsedPlay>, List<UnparsedPlay>>> rawPlays = new TreeMap<Timestamp, Pair<List<UnparsedPlay>, List<UnparsedPlay>>>();
		int currentQuarter = 0;
		for(Element e = table.child(0); e != null; e = e.nextElementSibling()){
			if(e.text().startsWith("Start of")){
				currentQuarter++;
				continue;
			}
			if(e.getElementsByClass("play team htm").get(0).text().startsWith("Go to")) {
				continue;
			}
			String awayPlay = e.getElementsByClass("play team vtm").get(0).text();
			String time = e.getElementsByClass("time").get(0).text();
			String homePlay = e.getElementsByClass("play team htm").get(0).text();
			String[] timeSplit = time.split(":");
			String minutesString = timeSplit[0];
			String secondsString = timeSplit[1];
			int minutes = Integer.parseInt(minutesString);
			int seconds = Integer.parseInt(secondsString);
			Timestamp timestamp = new Timestamp(currentQuarter, (minutes * 60) + seconds);
			Pair<List<UnparsedPlay>, List<UnparsedPlay>> playsAtTime = rawPlays.getOrDefault(timestamp, 
					new Pair<List<UnparsedPlay>, List<UnparsedPlay>>(new LinkedList<UnparsedPlay>(), new LinkedList<UnparsedPlay>()));
			if(!awayPlay.equals("")){
				playsAtTime.first().add(new UnparsedPlay(awayPlay, getPlayLink(e.getElementsByClass("play team vtm").get(0))));
			}
			if(!homePlay.equals("")){
				playsAtTime.second().add(new UnparsedPlay(homePlay, getPlayLink(e.getElementsByClass("play team htm").get(0))));
			}
			rawPlays.put(timestamp, playsAtTime);
		}
		return rawPlays;
	}

	private Score addToScore(PlayType pt, Score score){
		if(pt.hasSupertype(PlayType.THREE_POINTER_MADE)){
			return score.addToThisTeamsPoints(3);
		} else if(pt.hasSupertype(PlayType.FIELD_GOAL_MADE)){
			return score.addToThisTeamsPoints(2);
		} else if(pt.hasSupertype(PlayType.FREE_THROW_MADE)){
			return score.addToThisTeamsPoints(2);
		} else {
			return score;
		}
	}

	private List<AdvancedStatsPlay> JSONArrayToPlays(JSONArray input){
		List<AdvancedStatsPlay> plays = new LinkedList<AdvancedStatsPlay>();
		for(Object o : input){
			if(!(o instanceof JSONObject)){
				throw new JSONException("Invalid data json object.");
			}
			JSONObject jo = (JSONObject)o;
			plays.add(AdvancedStatsPlay.fromJSON(this, jo));
		}
		return plays;
	}

	private JSONArray playsToJSONArray(List<AdvancedStatsPlay> input){
		JSONArray answer = new JSONArray();
		for(AdvancedStatsPlay asp : input){
			answer.put(asp.toJSON());
		}
		return answer;
	}

	private LinkedHashSet<Player> getPlayers(Elements players){
		LinkedHashSet<Player> answer = new LinkedHashSet<Player>();
		for(int i = 0; i < players.size(); i++){
			Element e = players.get(i);
			String text = e.text();
			if(text.equals("Player") || text.equals("Totals:")) {
				continue;
			}
			if(i < 6){
				text = text.substring(0, text.length() - 2);
			}
			//System.out.println(text);
			boolean singleName = !text.contains(" ");
			String[] split = text.split(" ", 2);
			//System.out.println(Arrays.asList(split));
			answer.add(singleName ? Player.get(null, split[0]) : Player.get(split[0], split[1]));
		}
		return answer;
	}

	private String getPlayLink(Element play){
		String playLink;
		try {
			playLink = "https://stats.nba.com" + play.getElementsByAttribute("href").get(0).attr("href");
		} catch(IndexOutOfBoundsException e){
			playLink = null;
		}
		return playLink;
	}

	private List<AdvancedStatsPlay> parseAllPlays(SortedMap<Timestamp, Pair<List<UnparsedPlay>, List<UnparsedPlay>>> rawPlays,
		Collection<? extends Player> awayPlayers, Collection<? extends Player> homePlayers, Team awayTeam, Team homeTeam){
		List<AdvancedStatsPlay> plays = new LinkedList<AdvancedStatsPlay>();
		Score score = new Score(0, 0);
		for(Timestamp t : rawPlays.keySet()){
			Pair<List<UnparsedPlay>, List<UnparsedPlay>> unparsedPlayGroup = rawPlays.get(t);
			
			Collection<AdvancedStatsPlay> pls = parsePlays(unparsedPlayGroup, awayPlayers, homePlayers, awayTeam, t, score);
			Collection<AdvancedStatsPlay> plsr = parsePlays(unparsedPlayGroup.reversePair(), homePlayers, awayPlayers, homeTeam, t, score.reverseScore());
			/*if(pls.size() + plsr.size() < unparsedPlayGroup.first().size() + unparsedPlayGroup.second().size()){
				logging.warning("Match not found for a play in: " + unparsedPlayGroup);
			}*/

			for(AdvancedStatsPlay p : pls){
				score = addToScore(p.getType(), score);
			}
			for(AdvancedStatsPlay p : plsr){
				score = addToScore(p.getType(), score.reverseScore()).reverseScore();
			}
			
			plays.addAll(pls);
			plays.addAll(plsr);
		}
		return plays;
	}

	//Parses all plays contained in unparsedPlayGroup
	private Collection<AdvancedStatsPlay> parsePlays(Pair<List<UnparsedPlay>, List<UnparsedPlay>> unparsedPlayGroup, 
				Collection<? extends Player> firstPlayers, Collection<? extends Player> secondPlayers, Team team, Timestamp timestamp, Score score){
		
		Collection<AdvancedStatsPlay> newPlays = new LinkedList<AdvancedStatsPlay>();
		for(int i = 0; i < unparsedPlayGroup.first().size(); i++){
			
			Collection<AdvancedStatsPlay> newPlaysForThisUnparsedPlay = new LinkedList<AdvancedStatsPlay>();
			eachPlayType: for(Pair<String, String> parsingPair : playTypeParsing.keySet()){
				PlayType playType = playTypeParsing.get(parsingPair);
				for(Play p : newPlaysForThisUnparsedPlay){
					if(p.getType().hasSupertype(playType)){
						continue eachPlayType;
					}
				}

				if(i + 1 > unparsedPlayGroup.second().size() && parsingPair.second() != null){
					continue;
				}
				List<String> playerLastNames = new LinkedList<String>();
				Matcher matcher = Pattern.compile(parsingPair.first()).matcher(unparsedPlayGroup.first().get(i).rawPlay);
				if(!matcher.matches()){
					continue;
				}
				playerLastNames.addAll(getAllGroups(matcher));
				if(parsingPair.second() != null){
					Matcher matcher2 = Pattern.compile(parsingPair.second()).matcher(unparsedPlayGroup.second().get(i).rawPlay);
					if(!matcher2.matches()){
						continue;
					}
					playerLastNames.addAll(getAllGroups(matcher2));
				}
				Player[] players = new Player[playerLastNames.size()];
				//logging.info(unparsedPlayGroup.first().get(i).rawPlay);
				//logging.info(playType.toString());
				//logging.info(playerLastNames.toString());
				for(int j = 0; j < playerLastNames.size(); j++){
					players[j] = guessPlayer(playerLastNames.get(j), firstPlayers, secondPlayers);
				}
				
				AdvancedStatsPlay asp = new AdvancedStatsPlay(this, unparsedPlayGroup.first().get(i).playLink, playType, timestamp, team, score, players);
				newPlays.add(asp);
				newPlaysForThisUnparsedPlay.add(asp);
			}
			if(newPlaysForThisUnparsedPlay.size() == 0){
				logging.warning("No match found for " + unparsedPlayGroup.first().get(i));
			}
		}
		return newPlays;
	}

	private Collection<String> getAllGroups(Matcher m){
		Collection<String> answer = new LinkedList<String>();
		for(int i = 1; i <= m.groupCount(); i++){
			answer.add(m.group(i));
		}
		return answer;
	}

	//Preliminary method for guessing a player based on their last name. A more robust method of accomplishing this should be devised, as this has problems.
	private Player guessPlayer(String lastName, Collection<? extends Player> firstPlayers, Collection<? extends Player> secondPlayers){
		lastName = lastName.toLowerCase();
		//System.out.println(lastName);
		for(Player p : firstPlayers){
			if(p.lastName().equals(lastName) || p.initialedName().contentEquals(lastName)){
				return p;
			}
		}
		for(Player p : secondPlayers){
			if(p.lastName().equals(lastName) || p.initialedName().contentEquals(lastName)){
				return p;
			}
		}
		logging.warning("Player not found: " + lastName);
		return Player.get("", "");
		//throw new AssertionError();
	}

	/**Returns information for all games played on the given day, or null if an error occurs.
	 * @param ld the date.
	 * @throws IOExcpetion if an IO error occurs.
	 * @throws NullPointerException if ld is null.
	 * @return information for all games played on the given day.
	 */
	public List<GameInfo> getGameInformationOnDay(LocalDate ld) throws IOException {
		try {
			synchronized(this){
				return getGameInformationOnDayInternal(ld);
			}
		} catch(AdvancedStatsControlFlowException e){
			logging.error("Could not get game information for " + ld.toString());
			return null;
		}
	}
	
	private List<GameInfo> getGameInformationOnDayInternal(LocalDate ld) throws IOException {
		logging.info("====================================================================================");
		logging.info("Getting game information for " + ld.toString());
		if(this.read && this.data.has(ld.toString())){
			logging.info("Found stored game information.");
			return JSONArrayToGameInfos(this.data.getJSONArray(ld.toString()));
		}
		logging.info("Using browser to get game information.");
		String url = "https://stats.nba.com/help/videostatus/#!/" + String.format("%02d", ld.getMonthValue()) + "/" + String.format("%02d", ld.getDayOfMonth()) + "/" + ld.getYear();
		Document dayPage = renderPage(url, DEFAULT_TIMEOUT, MINIMUM_TIMEOUT, ExpectedConditions.visibilityOfElementLocated(By.className("stats-video-status-page")));
		Element dayPageBody = dayPage.body();
		Elements games = dayPageBody.getElementsByAttribute("data-ng-repeat");
		List<GameInfo> answer = new LinkedList<GameInfo>();
		for(Element e : games){
			Elements check = e.getElementsByClass("has-video");
			if(check.size() == 0) {
				continue;
			}
			String[] teamAbbrs = e.getElementsByClass("text").get(0).text().split(" @ ");
			Team homeTeam;
			Team awayTeam;
			
			boolean hasVideo = check.get(0).text().equals("Video Available");
			String link = "https://stats.nba.com" + e.getElementsByClass("has-boxscore").get(0).getElementsByAttribute("ng-href").get(0).attr("href");
			if(teamAbbreviations.containsKey(teamAbbrs[0]) && teamAbbreviations.containsKey(teamAbbrs[1])) {
				awayTeam = teamAbbreviations.get(teamAbbrs[0]);
				homeTeam = teamAbbreviations.get(teamAbbrs[1]);
			} else {
				Element gamePageBody = renderPage(link, DEFAULT_TIMEOUT, MINIMUM_TIMEOUT, ExpectedConditions.visibilityOfElementLocated(By.className("game-summary-team__name"))).body();
				Elements teams = gamePageBody.getElementsByClass("game-summary-team__name");
				if(teams == null) {
					logging.error("Could not get game information for a game on " + ld.toString());
					continue;
				}
				awayTeam = Team.get(teams.get(0).text());
				homeTeam = Team.get(teams.get(1).text());
			}
			
			GameInfo gi = new GameInfo(ld, awayTeam, homeTeam);
			answer.add(gi);
			if(hasVideo){
				storeLink(gi, link);
			} else {
				logging.warning("No video available for " + gi.toString());
				storeLink(gi, null);
			}
		}
		if(this.write){
			data.put(ld.toString(), gameInfosToJSONArray(answer));
		}
		logging.info("Got game information for " + ld.toString() + ". Found " + answer.size() + (answer.size() == 1 ? " game." : " games."));
		return answer;
	}

	private List<GameInfo> JSONArrayToGameInfos(JSONArray input){
		List<GameInfo> answer = new LinkedList<GameInfo>();
		for(Object o : input){
			if(!(o instanceof JSONObject)){
				throw new JSONException("Invalid stored data.");
			}
			JSONObject jo = (JSONObject)o;
			GameInfo gi = GameInfo.fromJSON(jo);
			answer.add(gi);
			storeLink(gi, jo.getString("gamelink"));
		}
		return answer;
	}

	private JSONArray gameInfosToJSONArray(List<GameInfo> input) throws IOException {
		JSONArray answer = new JSONArray();
		for(GameInfo gi : input){
			JSONObject jo = gi.toJSON();
			jo.put("gamelink", getLink(gi));
			answer.put(jo);
		}
		return answer;
	}

	private void storeLink(GameInfo gi, String link){
		links.put(gi, link);
	}

	//Get a link to the game page from a GameInfo object.
	private String getLink(GameInfo gi) throws IOException {
		if(links.containsKey(gi)){
			return links.get(gi);
		}
		this.getGameInformationOnDay(gi.date());
		if(links.containsKey(gi)){
			return links.get(gi);
		}
		logging.warning("No game found for " + gi.toString());
		return null;
	}

	//If in write mode, flushes the current JSON data
	private void flush() throws IOException {
		if(this.write){
			try (PrintStream out = new PrintStream(new FileOutputStream(this.writeLocation))) {
    			out.print(this.data.toString());
			}
		}
	}
	
	/**Closes the resources associated with AdvancedStats, and flushes cached data to the local machine if write mode is active.
	 * Once this method is called, no other AdvancedStats methods can be called.
	 @throws IOException if an IO error occurs.
	 */
	public void close() throws IOException {
		this.flush();
		if(this.driver != null){
			try{
				this.driver.quit();
			} catch(UnreachableBrowserException e){
				logging.error("Failed to exit browser. You may have to manually kill the browser processes after the program has finished.");
			}
			this.driver = null;
		}
	}

    private synchronized Document renderPage(String filePath, long timeout, long minTimeout, com.google.common.base.Function<WebDriver, ?> func) throws IOException {
        return renderPage(filePath, timeout, minTimeout, func, 0);
    }

    private synchronized Document renderPage(String filePath, long timeout, long minTimeout, com.google.common.base.Function<WebDriver, ?> func, int retry) throws IOException {
		if(retry >= MAX_RETRIES){
			throw new AdvancedStatsControlFlowException();
		}
		setup();
		try{
			driver.get(filePath);
		} catch(UnreachableBrowserException e){
			this.reset();
		}
        try {
        	new WebDriverWait(driver, timeout/*Duration.ofMillis(timeout)*/).until(func);
        } catch(TimeoutException e){
        	this.reset();
        	return renderPage(filePath, timeout, minTimeout, func, retry + 1);
        }
        try {
        	Thread.sleep(minTimeout);
        } catch(InterruptedException e) {
        	Thread.currentThread().interrupt();
        }
        return Jsoup.parse(driver.getPageSource());
	}

	private static class AdvancedStatsPlay extends Play {
		private final String playLink;
		private final AdvancedStats source;
		private Video v;
		private String videoLink;

		private AdvancedStatsPlay(AdvancedStats stats, String link, PlayType playType, Timestamp timestamp, Team team, Score score, Player... player){
			super(playType, timestamp, team, score, player);
			this.source = stats;
			this.playLink = link;
			this.videoLink = videoLink;
		}

		//RI: true
		//AF: Same as superclass. v is video of entire play.

		/**Returns a Video depicting this play, or null if an error occurs.
		@throws IOException if an IO error occurs.
		@return a Video depicting this play.
		*/
		public Video getVideo() throws IOException {
			try{
				return getVideoInternal();
			} catch(AdvancedStatsControlFlowException e){
				logging.error("Could not resolve video for " + this.toString());
				return null;
			}
		}

		private Video getVideoInternal() throws IOException {
			logging.info("====================================================================================");
			logging.info("Resolving video for " + this.toString());
			if(playLink == null){
				logging.warning("No video exists for " + this.toString());
				return null;
			}
			if(v != null){
				logging.info("Found cached video.");
				return v;
			}
			if(this.source.read && this.source.data.has(this.playLink)){
				logging.info("Found stored video location.");
				v = new InternetVideo(this.source.data.getString(this.playLink));
				return v;
			}
			logging.info("Using browser to resolve video.");
			Element playBody = source.renderPage(this.playLink, VIDEO_TIMEOUT, MINIMUM_TIMEOUT, ExpectedConditions.visibilityOfElementLocated(By.id("stats-videojs-player_html5_api"))).body();
			try {
				this.videoLink = playBody.getElementById("stats-videojs-player_html5_api").attr("src");
				v = new InternetVideo(this.videoLink);
				if(this.source.write){
					this.source.data.put(this.playLink, this.videoLink);
				}
				logging.info("Finished resolving this video.");
				return v;
			} catch(NullPointerException e) {
				throw new AdvancedStatsControlFlowException();
			}
		}

		private JSONObject toJSON(){
			JSONObject answer = new JSONObject();
			answer.put("playlink", this.playLink == null ? JSONObject.NULL : this.playLink);
			answer.put("type", this.getType().toString());
			answer.put("time", this.getTimestamp().toString());
			answer.put("team", this.getTeam().toString());
			answer.put("score", this.getScore().toString());
			JSONArray players = new JSONArray();
			for(Player p : this.getPlayers()){
				players.put(p.toString());
			}
			answer.put("players", players);
			return answer;
		}

		private static AdvancedStatsPlay fromJSON(AdvancedStats stats, JSONObject input){
			String playLink = input.get("playlink") == JSONObject.NULL ? null : input.getString("playlink");
			PlayType type = PlayType.parse(input.getString("type"));
			Timestamp timestamp = Timestamp.parse(input.getString("time"));
			Team team = Team.get(input.getString("team"));
			Score score = Score.parse(input.getString("score"));
			JSONArray arr = input.getJSONArray("players");
			Player[] players = new Player[arr.length()];
			for(int i = 0; i < arr.length(); i++){
				players[i] = Player.parse(arr.getString(i));
			}
			return new AdvancedStatsPlay(stats, playLink, type, timestamp, team, score, players);
		}
	}

	//Class used to hold a String representing an unparsed play, as well as a video link to the play.
	private static class UnparsedPlay {
		private final String rawPlay;
		private final String playLink;

		private UnparsedPlay(String s, String v){
			rawPlay = s;
			playLink = v;
		}
		
		@Override
		public String toString() {
			return rawPlay;
		}
	}

	//Exception used to control flow in AdvancedStats methods.
	private static class AdvancedStatsControlFlowException extends RuntimeException {
		private AdvancedStatsControlFlowException(){
			super();
		}

		private AdvancedStatsControlFlowException(String message){
			super(message);
		}

		private AdvancedStatsControlFlowException(String message, Throwable cause){
			super(message, cause);
		}
	}
}