package thybulle.driver;

import java.util.*;
import java.util.logging.*;
import java.io.*;
import thybulle.highlights.*;
import thybulle.misc.*;

public class Driver {
	static final Logging logging = new Logging("thybulle.driver");

	public static void main(String[] args) throws IOException {
		//Definitely pre-check these before running the entire program.
		String inputFile = args[0];
		String outputFile = args[1];
		String garbageLocation = args[2];
		logging.addHandler(new ConsoleHandler());
		HighlightsLogger.addHandler(new ConsoleHandler());
		HighlightsCompiler hc = Highlights.compiler();
		logging.info("Parsing constraints");
		Collection<Constraint> constraints = InputParsing.parseConstraints(inputFile);
		logging.info("Parsing dataset");
		Pair<Game.Source, List<GameInfo>> dataset = InputParsing.parseDataset(inputFile);
		logging.info("Getting play-by-play data");
		List<Game> games = dataset.first().getGames(dataset.second());
		logging.info("Done getting play-by-play data");
		hc.addGames(games.toArray(new Game[0]));
		hc.addConstraints(constraints.toArray(new Constraint[0]));
		logging.info("Getting all plays that satisfy the constraints");
		Highlights h = hc.compile();
		logging.info("Saving video");
		h.saveVideo(outputFile, garbageLocation);
		logging.info("Done!");
	}
}