package thybulle.highlights;

import static thybulle.highlights.HighlightsLogger.*;

import thybulle.misc.*;
import java.util.*;
import java.util.regex.*;
import java.time.*;
import java.io.*;
import org.jsoup.*;
import org.jsoup.nodes.*;
import org.jsoup.select.*;

/**Class which parses play-by-play data from stats.nba.com.
(Discuss features and limitations of this parser.)
*/

//One problem with current implementation: 
//no way to distinguish between players on the same team with the same last name.

class AdvancedStats implements GameSource {
	private static final AdvancedStats singleton = new AdvancedStats();

	private static final Map<GameInfo, String> links = new HashMap<GameInfo, String>();

	private static final Map<Pair<String, String>, PlayType> playTypeParsing = new LinkedHashMap<Pair<String, String>, PlayType>();

	private static final String playerRegex = "(.*)";
	private static final String distanceRegex = "\\d{1, 2}'";

	private static final String dunkMadeRegex = playerRegex + " " + distanceRegex + " .* Dunk \\(\\d+ PTS\\).*";
	private static final String missedDunkRegex = "MISS " + playerRegex + " " + distanceRegex + " .* Dunk";
	private static final String threePointerMadeRegex = playerRegex + " " + distanceRegex + " .* 3PT .* \\(\\d+ PTS\\).*";
	private static final String missedThreePointerRegex = "MISS " + playerRegex + " " + distanceRegex + " .* 3PT .*";
	private static final String fieldGoalMadeRegex = playerRegex + " " + distanceRegex + " .* \\(\\d+ PTS\\).*";
	private static final String missedFieldGoalRegex = "MISS " + playerRegex + " " + distanceRegex + " .*";

	private static final String missedFreeThrowRegex = "MISS " + playerRegex + " Free Throw.*";
	private static final String madeFreeThrowRegex = playerRegex + " Free Throw.*";

	private static final String teamReboundRegex = ".* rebound";
	private static final String reboundRegex = playerRegex + " REBOUND.*";

	private static final String assistRegex = ".*\\(" + playerRegex + "\\d+ AST\\)";

	private static final String stealRegex = playerRegex + " STEAL \\(\\d+ STL\\)";

	private static final String blockRegex = playerRegex + " BLOCK \\(\\d+ BLK\\)";

	private static final String alleyOopRegex = playerRegex + " " + distanceRegex + " Alley Oop .* \\(\\d+ PTS\\) \\(" + playerRegex + " \\d+ AST\\)";

	private static final String teamTechnicalRegex = ".* T\\.Foul \\(Def. 3 Sec .*\\).*";
	private static final String flagrantFoul1Regex = playerRegex + " FLAGRANT\\.FOUL\\.TYPE1.*";
	private static final String flagrantFoul2Regex = playerRegex + " FLAGRANT\\.FOUL\\.TYPE2.*";
	private static final String shootingFoulRegex = playerRegex + " S\\.FOUL.*";
	private static final String defensiveFoulRegex = playerRegex + " P\\.FOUL.*";
	private static final String looseBallFoulRegex = playerRegex + " L\\.B\\.FOUL.*";
	private static final String offensiveFoulRegex = playerRegex + " OFF\\.FOUL.*";
	private static final String technicalFoulRegex = playerRegex + " T\\.FOUL.*";

	private static final String jumpBallRegex = "Jump Ball " + playerRegex + " vs. " + playerRegex + ":.*";

	private static final String eightSecondViolationRegex = ".* Turnover: 8 Second Violation \\(T#\\d+\\)";
	private static final String shotClockViolationRegex = ".* Turnover: Shot Clock \\(T#d+\\)";
	private static final String teamTurnoverRegex = ".* Turnover: .* \\(T#d+\\)";
	private static final String travelingRegex = playerRegex + " Traveling Turnover \\(P\\d+\\.T\\d+\\)";
	private static final String basketInterferenceRegex = playerRegex + " Offensive Goaltending Turnover \\(P\\d+\\.T\\d+\\)";
	private static final String turnoverRegex = playerRegex + " .* Turnover \\(P\\d+\\.T\\d+\\)";

	private static final String goaltendingRegex = playerRegex + " Violation:Defensive Goaltending.*";

	private static final String subRegex = "SUB: " + playerRegex + " FOR " + playerRegex;

	private static final String timeoutRegex = ".* Timeout: .*";
	

	static {
		playTypeParsing.put(p(dunkMadeRegex, shootingFoulRegex), PlayType.AND_ONE_DUNK);
		playTypeParsing.put(p(dunkMadeRegex, null), PlayType.DUNK);
		playTypeParsing.put(p(threePointerMadeRegex, shootingFoulRegex), PlayType.AND_ONE_THREE_POINTER);
		playTypeParsing.put(p(threePointerMadeRegex, null), PlayType.THREE_POINTER);
		playTypeParsing.put(p(fieldGoalMadeRegex, shootingFoulRegex), PlayType.AND_ONE);
		playTypeParsing.put(p(fieldGoalMadeRegex, null), PlayType.FIELD_GOAL);

		playTypeParsing.put(p(madeFreeThrowRegex, null), PlayType.FREE_THROW_MADE);
		playTypeParsing.put(p(missedFreeThrowRegex, null), PlayType.FREE_THROW_MISSED);

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
		playTypeParsing.put(p(offensiveFoulRegex, null), PlayType.OFFENSIVE_FOUL);

		playTypeParsing.put(p(jumpBallRegex, null), PlayType.JUMP_BALL);

		playTypeParsing.put(p(eightSecondViolationRegex, null), PlayType.EIGHT_SECOND_VIOLATION);
		playTypeParsing.put(p(shotClockViolationRegex, null), PlayType.SHOT_CLOCK_VIOLATION);
		playTypeParsing.put(p(teamTurnoverRegex, null), PlayType.TEAM_TURNOVER);
		playTypeParsing.put(p(travelingRegex, null), PlayType.TRAVELING);
		playTypeParsing.put(p(basketInterferenceRegex, null), PlayType.BASKET_INTERFERENCE);
		playTypeParsing.put(p(turnoverRegex, null), PlayType.TURNOVER);

		playTypeParsing.put(p(goaltendingRegex, null), PlayType.GOALTENDING);

		playTypeParsing.put(p(subRegex, null), PlayType.SUBSTITUTION);

		playTypeParsing.put(p(timeoutRegex, null), PlayType.TIMEOUT);
	}

	private static <F, S> Pair<F, S> p(F first, S second){
		return new Pair<F, S>(first, second);
	}

	private AdvancedStats(){}

	/**Returns an AdvancedStats instance.
	@return an AdvancedStats instance.
	*/
	static AdvancedStats open(){
		return singleton;
	}

	public Collection<? extends Play> getPlayByPlay(GameInfo gi) throws IOException {
		logging.info("========================================================================");
		logging.info("Getting play-by-play data for " + gi.toString());

		logging.info("Parsing players");
		String url = getLink(gi);
		if(gi == null){
			return new LinkedList<Play>();
		}
		Element boxScoreBody = Jsoup.connect(url).get().body();
		Elements players = boxScoreBody.getElementsByClass("player");
		LinkedHashSet<Player> awayPlayers = getPlayers(boxScoreBody.getElementsByClass("nba-stat-table").get(0).getElementsByClass("player"));
		LinkedHashSet<Player> homePlayers = getPlayers(boxScoreBody.getElementsByClass("nba-stat-table").get(1).getElementsByClass("player"));

		logging.info("Getting play-by-play data");
		String playByPlayLink = url + "/playbyplay";
		Element playByPlayBody = Jsoup.connect(playByPlayLink).get().body();
		Element table = playByPlayBody.getElementsByClass("boxscore-pbp__inner").get(0).getElementsByAttributeValue("ng-if", "!boxscore.isLive").get(0);
		//Yes, this is disgusting. Yes, it is necessary.
		//Each pair represents all plays committed by each team at a certain time.
		//The first value is the away team's plays, the second value is the home team's plays.
		//This is used to classify plays which involve both teams - for example, an and one is a field goal by one team combined with a shooting foul by the other.
		SortedMap<Timestamp, Pair<List<UnparsedPlay>, List<UnparsedPlay>>> rawPlays = new TreeMap<Timestamp, Pair<List<UnparsedPlay>, List<UnparsedPlay>>>();
		int currentQuarter = 1;
		for(Element e = table.child(0); e != null; e = e.nextElementSibling()){
			if(e.text().startsWith("Start of")){
				currentQuarter++;
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
			Timestamp timestamp = new Timestamp((minutes * 60) + seconds, currentQuarter);
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
		SortedSet<AdvancedStatsPlay> plays = new TreeSet<AdvancedStatsPlay>();
		for(Timestamp t : rawPlays.keySet()){
			Pair<List<UnparsedPlay>, List<UnparsedPlay>> unparsedPlayGroup = rawPlays.get(t);
			plays.addAll(parsePlays(unparsedPlayGroup, awayPlayers, homePlayers, gi.awayTeam(), t));
			plays.addAll(parsePlays(unparsedPlayGroup.reversePair(), homePlayers, awayPlayers, gi.homeTeam(), t));
		}
		logging.info("Finished. Found " + plays.size() + " plays.");
		return plays;
	}

	private LinkedHashSet<Player> getPlayers(Elements players){
		LinkedHashSet<Player> answer = new LinkedHashSet<Player>();
		for(int i = 0; i < players.size(); i++){
			Element e = players.get(0);
			String text = e.text();
			if(i < 5){
				text = text.substring(text.length() - 1);
			}
			boolean singleName = text.contains(" ");
			String[] split = text.split(" ", 2);
			answer.add(singleName ? Player.get(null, split[0]) : Player.get(split[0], split[1]));
		}
		return answer;
	}

	private String getPlayLink(Element play){
		String playLink;
		try {
			playLink = play.getElementsByAttribute("href").get(0).attr("href");
		} catch(NullPointerException e){
			playLink = null;
		}
		return playLink;
	}

	//Parses all plays contained in unparsedPlayGroup
	private Collection<AdvancedStatsPlay> parsePlays(Pair<List<UnparsedPlay>, List<UnparsedPlay>> unparsedPlayGroup, 
			Collection<? extends Player> firstPlayers, Collection<? extends Player> secondPlayers, Team team, Timestamp timestamp){
		Collection<AdvancedStatsPlay> newPlays = new LinkedList<AdvancedStatsPlay>();
		eachPlayType: for(Pair<String, String> parsingPair : playTypeParsing.keySet()){
			PlayType playType = playTypeParsing.get(parsingPair);
			for(Play p : newPlays){
				if(p.getType().hasSupertype(playType)){
					continue eachPlayType;
				}
			}

			eachUnparsedPlay: for(int i = 0; i < unparsedPlayGroup.first().size(); i++){

				if(i + 1 > unparsedPlayGroup.first().size() || i + (parsingPair.second() == null ? 0 : 1) > unparsedPlayGroup.second().size()){
					continue;
				}
				List<String> playerLastNames = new LinkedList<String>();
				Matcher matcher = Pattern.compile(parsingPair.first()).matcher(unparsedPlayGroup.first().get(i).rawPlay);
				if(!matcher.matches()){
					continue eachUnparsedPlay;
				}
				playerLastNames.addAll(getAllGroups(matcher));
				if(parsingPair.second() != null){
					Matcher matcher2 = Pattern.compile(parsingPair.second()).matcher(unparsedPlayGroup.second().get(i).rawPlay);
					if(!matcher2.matches()){
						continue eachUnparsedPlay;
					}
					playerLastNames.addAll(getAllGroups(matcher2));
				}
				Player[] players = new Player[playerLastNames.size()];
				for(int j = 0; j < playerLastNames.size(); j++){
					players[j] = guessPlayer(playerLastNames.get(j), firstPlayers, secondPlayers);
				}

				newPlays.add(new AdvancedStatsPlay(unparsedPlayGroup.first().get(i).playLink, playType, timestamp, team, players));
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

	//Preliminary method for guessing a player based on their last name. A more fobust method of accomplishing this should be devised, as this has problems.
	private Player guessPlayer(String lastName, Collection<? extends Player> firstPlayers, Collection<? extends Player> secondPlayers){
		for(Player p : firstPlayers){
			if(p.lastName().equals(lastName)){
				return p;
			}
		}
		for(Player p : secondPlayers){
			if(p.lastName().equals(lastName)){
				return p;
			}
		}
		throw new AssertionError();
	}

	public List<GameInfo> getAllGameInfosOnDay(LocalDate ld) throws IOException {
		logging.info("Getting game information for " + ld.toString());
		String url = "https://stats.nba.com/help/videostatus/#!/" + String.format("%02d", ld.getMonthValue()) + "/" + String.format("%02d", ld.getDayOfMonth()) + "/" + ld.getYear();
		Document dayPage = Jsoup.connect(url).get();
		Element dayPageBody = dayPage.body();
		Elements games = dayPageBody.getElementsByAttributeValue("data-ng-repeat", "(i, row) in page track by row.$hash");
		List<GameInfo> answer = new LinkedList<GameInfo>();
		for(Element e : games){
			boolean hasVideo = e.getElementsByClass("has-video").get(0).text().equals("Video Available");
			String link = e.getElementsByClass("has-boxscore").get(0).getElementsByAttribute("ng-href").get(0).attr("href");
			Element gamePageBody = Jsoup.connect(link).get().body();
			Elements teams = gamePageBody.getElementsByClass("game-summary-team__name");
			String awayTeam = teams.get(0).text();
			String homeTeam = teams.get(1).text();
			GameInfo gi = new GameInfo(ld, Team.get(awayTeam), Team.get(homeTeam));
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

		LocalDate ld = gi.date();
		String url = "https://stats.nba.com/help/videostatus/#!/" + String.format("%02d", ld.getMonthValue()) + "/" + String.format("%02d", ld.getDayOfMonth()) + "/" + ld.getYear();
		Document dayPage = Jsoup.connect(url).get();
		Element dayPageBody = dayPage.body();
		Elements games = dayPageBody.getElementsByAttributeValue("data-ng-repeat", "(i, row) in page track by row.$hash");
		for(Element e : games){
			boolean hasVideo = e.getElementsByClass("has-video").get(0).text().equals("Video Available");
			String link = e.getElementsByClass("has-boxscore").get(0).getElementsByAttribute("ng-href").get(0).attr("href");
			Element gamePageBody = Jsoup.connect(link).get().body();
			Elements teams = gamePageBody.getElementsByClass("game-summary-team__name");
			String awayTeam = teams.get(0).text();
			String homeTeam = teams.get(1).text();
			if(Team.get(awayTeam).equals(gi.awayTeam()) && Team.get(homeTeam).equals(gi.homeTeam())){
				if(hasVideo){
					storeLink(gi, link);
					return link;
				} else {
					logging.warning("No video available for " + gi.toString());
					storeLink(gi, null);
					return null;
				}
			}
		}
		logging.warning("No game found for " + gi.toString());
		return null;
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
			Element playBody = Jsoup.connect(this.playLink).get().body();
			return new Video(playBody.getElementById("stats-videojs-player_html5_api").attr("src"));
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
	}
}