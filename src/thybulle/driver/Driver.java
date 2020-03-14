package thybulle.driver;

import java.util.*;
import java.util.List;
import java.util.logging.*;
import java.io.*;
import java.time.LocalDate;

import thybulle.highlights.*;
import thybulle.misc.*;

//TODO: Improve AdvancedStats page loading

public class Driver {
	static final Logging logging = new Logging(System.out);

	public static void main(String[] args) throws IOException {
		//Definitely pre-check these before running the entire program.
		String inputFile = args[0];
		String outputFile = args[1];
		String garbageLocation = args[2];
		
		HighlightsLogger.setOutput(System.out);
		
		HighlightsCompiler hc = Highlights.compiler();
		logging.info("Parsing input");
		Collection<Constraint> constraints = InputParsing.parseConstraints(inputFile);
		Game.Source source = InputParsing.parseSource(inputFile);
		Collection<Pair<LocalDate, LocalDate>> dataset = InputParsing.parseDataset(inputFile);
		Collection<Team> teams = InputParsing.parseTeams(inputFile);
		
		logging.info("Getting play-by-play data");
		List<GameInfo> information = new LinkedList<GameInfo>();
		for(Pair<LocalDate, LocalDate> p : dataset) {
			information.addAll(source.getTeamGameInformationBetweenDates(p.first(), p.second(), teams.toArray(new Team[0])));
		}
		List<Game> games = source.getGames(information);
		logging.info("Done getting play-by-play data. Found " + games.size() + " games.");
		hc.addGames(games.toArray(new Game[0]));
		hc.addConstraints(constraints.toArray(new Constraint[0]));
		logging.info("Getting all plays that satisfy constraints");
		Highlights h = hc.compile();
		logging.info("Found " + h.size() + " plays.");
		logging.info("Downloading and saving video");
		h.saveVideo(new File(outputFile), new File(garbageLocation));
		logging.info("Done!");
		source.exit();
		
		/*List<Game> g = Game.Source.NBA_ADVANCED_STATS.getGames(Game.Source.NBA_ADVANCED_STATS.getAllGameInfosOnDay(LocalDate.of(2020, 3, 10)));
		HighlightsCompiler hc = Highlights.compiler();
		hc.addGames(g.toArray(new Game[0]));
		hc.addConstraints(PlayType.AND_ONE_DUNK);
		hc.compile().saveVideo(new File("C:\\Users\\kuliko\\Desktop\\javier\\Basketball\\complete\\mar11andonedunks.mp4"), new File("C:\\Users\\kuliko\\Desktop\\javier\\Basketball\\garbage"));*/
	}
}