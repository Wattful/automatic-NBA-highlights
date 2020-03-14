package thybulle.highlights;

import static thybulle.highlights.HighlightsLogger.*;

import thybulle.misc.*;
import java.util.*;
import java.util.regex.*;
import java.time.*;
import java.io.*;
import java.net.URL;

import org.jsoup.*;
import org.jsoup.nodes.*;
import org.jsoup.select.*;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
//import org.openqa.selenium.firefox.FirefoxDriver;
//import org.openqa.selenium.phantomjs.PhantomJSDriver;
import org.openqa.selenium.support.ui.WebDriverWait;

import com.google.common.base.Predicate;

/**Class which parses play-by-play data from stats.nba.com.
*/

//One problem with current implementation: 
//no way to distinguish between players on the same team with the same last name.

class AdvancedStats implements GameSource {
	private static final long DEFAULT_TIMEOUT = 4000;
	private static final long VIDEO_TIMEOUT = 10000;
	
	private static final AdvancedStats singleton = new AdvancedStats();
	
	private WebDriver driver;
	
	private static final Map<GameInfo, String> links = new HashMap<GameInfo, String>();
	private static final Map<Pair<String, String>, PlayType> playTypeParsing = new LinkedHashMap<Pair<String, String>, PlayType>();
	private static final Map<String, Team> teamAbbreviations = new HashMap<String, Team>();
	
	private static final String playerRegexNoGroup = "(?!MISS).+?";
	private static final String playerRegex = "(" + playerRegexNoGroup + ")";
	private static final String distanceRegex = "(?:\\d{1,2}' )?";
	private static final String shotModifiers = "(?:Jump|Alley Oop|Reverse|Finger Roll|Running|Layup|Tip|Putback|Turnaround|Bank|Hook|Step Back|Floating|Pullup|Pull-Up|Cutting|Fadeaway|Driving|Tip|Jumper|Shot| )*";
	private static final String turnoverModifiers = "(?:Lost Ball|Bad Pass|Offensive Foul|Basket Interference|Step Out of Bounds|Out of Bounds Lost Ball|Traveling|Out of Bounds \\- Bad Pass Turnover|3 Second Violation|Palming|Backcourt|Double Dribble|Discontinue Dribble|Inbound|No| )*";
	
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

	private static final String alleyOopRegex = playerRegex + " " + distanceRegex + " Alley Oop .* \\(\\d+ PTS\\) \\(" + playerRegex + " \\d+ AST\\)";

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
		File f = new File("C:\\Libraries\\ChromeDriverOld\\chromedriver.exe");
		System.setProperty("webdriver.chrome.driver", f.getAbsolutePath());
	}

	private static <F, S> Pair<F, S> p(F first, S second){
		return new Pair<F, S>(first, second);
	}

	private AdvancedStats(){}

	/**Returns an AdvancedStats instance.
	@return an AdvancedStats instance.
	*/
	static AdvancedStats open(){
		return new AdvancedStats();
	}
	
	private void setup() {
		if(driver == null) {
			driver = new ChromeDriver();
		}
	}

	/**Returns all plays in this game, in order of when they occurred, or an empty list if the given GameInfo does not represent a game that occurred.
	 *@param gi Information about the game to get
	 *@throws IOException if an IO error occurs.
	 *@throws NullPointerException if gi is null.
	 *@return a list of all plays in this game.
	 */
	public List<? extends Play> getPlayByPlay(GameInfo gi) throws IOException {
		setup();
		String url = getLink(gi);
		logging.info("=========================================================================================================================");
		logging.info("Getting play-by-play data for " + gi.toString());
		if(url == null){
			return new LinkedList<Play>();
		}
		logging.info("Parsing players");
		Element boxScoreBody = renderPage(url, DEFAULT_TIMEOUT).body();
		Elements players = boxScoreBody.getElementsByClass("nba-stat-table__overlay");
		if(players.isEmpty()) {
			logging.warning("Could not get play-by-play for " + gi.toString());
			return null;
		}
		LinkedHashSet<Player> awayPlayers = getPlayers(players.get(0).getElementsByClass("player"));
		LinkedHashSet<Player> homePlayers = getPlayers(players.get(1).getElementsByClass("player"));

		logging.info("Getting play-by-play data");
		String playByPlayLink = url + "/playbyplay";
		Element playByPlayBody = renderPage(playByPlayLink, DEFAULT_TIMEOUT).body();
		Elements test = playByPlayBody.getElementsByClass("boxscore-pbp__inner");
		if(test.isEmpty()) {
			logging.warning("Could not get play-by-play for " + gi.toString());
			return null;
		}
		Element table = test.get(0).getElementsByAttributeValue("ng-if", "!boxscore.isLive").get(0);
		//Yes, this is disgusting. Yes, it is necessary.
		//Each pair represents all plays committed by each team at a certain time.
		//The first value is the away team's plays, the second value is the home team's plays.
		//This is used to classify plays which involve both teams - for example, an and one is a field goal by one team combined with a shooting foul by the other.
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

		logging.info("Parsing play-by-play data");
		List<AdvancedStatsPlay> plays = new LinkedList<AdvancedStatsPlay>();
		for(Timestamp t : rawPlays.keySet()){
			Pair<List<UnparsedPlay>, List<UnparsedPlay>> unparsedPlayGroup = rawPlays.get(t);
			
			Collection<AdvancedStatsPlay> pls = parsePlays(unparsedPlayGroup, awayPlayers, homePlayers, gi.awayTeam(), t);
			Collection<AdvancedStatsPlay> plsr = parsePlays(unparsedPlayGroup.reversePair(), homePlayers, awayPlayers, gi.homeTeam(), t);
			if(pls.isEmpty() && plsr.isEmpty()) {
				if(!unparsedPlayGroup.first().isEmpty()) {
					System.err.println("No match found for " + unparsedPlayGroup.first().get(0).rawPlay);
				} else if(!unparsedPlayGroup.second().isEmpty()){
					System.err.println("No match found for " + unparsedPlayGroup.second().get(0).rawPlay);
				}
			}
			
			plays.addAll(pls);
			plays.addAll(plsr);
		}
		logging.info("Finished. Found " + plays.size() + " plays.");
		return plays;
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

	//Parses all plays contained in unparsedPlayGroup
	private Collection<AdvancedStatsPlay> parsePlays(Pair<List<UnparsedPlay>, List<UnparsedPlay>> unparsedPlayGroup, 
				Collection<? extends Player> firstPlayers, Collection<? extends Player> secondPlayers, Team team, Timestamp timestamp){
		
		Collection<AdvancedStatsPlay> newPlays = new LinkedList<AdvancedStatsPlay>();
		for(int i = 0; i < unparsedPlayGroup.first().size(); i++){
			
			Collection<AdvancedStatsPlay> newPlaysForThisUnparsedPlay = new LinkedList<AdvancedStatsPlay>();
			for(Pair<String, String> parsingPair : playTypeParsing.keySet()){
				PlayType playType = playTypeParsing.get(parsingPair);
				for(Play p : newPlaysForThisUnparsedPlay){
					if(p.getType().hasSupertype(playType)){
						continue;
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
				//System.out.println(unparsedPlayGroup.first().get(i).rawPlay);
				//System.out.println(playType);
				//System.out.println(playerLastNames);
				for(int j = 0; j < playerLastNames.size(); j++){
					players[j] = guessPlayer(playerLastNames.get(j), firstPlayers, secondPlayers);
				}
				
				AdvancedStatsPlay asp = new AdvancedStatsPlay(unparsedPlayGroup.first().get(i).playLink, playType, timestamp, team, players);
				newPlays.add(asp);
				newPlaysForThisUnparsedPlay.add(asp);
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
		System.err.println("Player not found: " + lastName);
		return Player.get("", "");
		//throw new AssertionError();
	}
	
	/**Returns information for all games played on the given day.
	 * @param ld the date.
	 * @throws IOExcpetion if an IO error occurs.
	 * @throws NullPointerException if ld is null.
	 * @return information for all games played on the given day.
	 */
	public List<GameInfo> getGameInformationOnDay(LocalDate ld) throws IOException {
		setup();
		logging.info("Getting game information for " + ld.toString());
		String url = "https://stats.nba.com/help/videostatus/#!/" + String.format("%02d", ld.getMonthValue()) + "/" + String.format("%02d", ld.getDayOfMonth()) + "/" + ld.getYear();
		Document dayPage = renderPage(url, DEFAULT_TIMEOUT);
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
				Element gamePageBody = renderPage(link, DEFAULT_TIMEOUT).body();
				Elements teams = gamePageBody.getElementsByClass("game-summary-team__name");
				if(teams == null) {
					logging.warning("Could not get game information for a game on " + ld.toString());
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
	
	/**Closes the resources associated with AdvancedStats.
	 * Once this method is called, no other AdvancedStats methods can be called.
	 */
	public void exit() {
		driver.close();
	}
	
	private synchronized Document renderPage(String filePath, long timeout) {
        driver.get(filePath);
        Predicate<WebDriver> func = webDriver -> ((JavascriptExecutor) webDriver).executeScript("return document.readyState").equals("complete");
        //Predicate<WebDriver> func = webDriver -> false;
        /*Predicate<WebDriver> func = d -> {
        	try {
        		d.findElement(By.tagName("data-ng-repeat"));
        		return true;
        	} catch(NoSuchElementException e) {
        		return false;
        	}
        };*/
        //try {
        new WebDriverWait(driver, timeout/1000).until(func);
        try {
        	Thread.sleep(timeout);
        } catch(InterruptedException e) {
        	throw new AssertionError();
        }
        //} catch(TimeoutException e) {}
        return Jsoup.parse(driver.getPageSource());
    }

	private static class AdvancedStatsPlay extends Play {
		private final String playLink;
		private Video v;

		private AdvancedStatsPlay(String link, PlayType playType, Timestamp timestamp, Team team, Player... player){
			super(playType, timestamp, team, player);
			playLink = link;
		}

		//RI: true
		//AF: Same as superclass. v is video of entire play.

		public Video getVideo() throws IOException {
			if(playLink == null){
				return null;
			}
			if(v != null){
				return v;
			}
			singleton.setup();
			Element playBody = singleton.renderPage(this.playLink, VIDEO_TIMEOUT).body();
			try {
				v = new Video(new URL(playBody.getElementById("stats-videojs-player_html5_api").attr("src")));
				return v;
			} catch(NullPointerException e) {
				logging.warning("Could not get video for " + this.toString());
				return null;
			}
			
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
		
		public String toString() {
			return rawPlay;
		}
	}
}