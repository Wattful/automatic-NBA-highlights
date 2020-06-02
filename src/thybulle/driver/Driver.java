package thybulle.driver;

import java.util.*;
import java.util.List;
import java.util.logging.*;
import java.io.*;
import java.time.LocalDate;

import thybulle.highlights.*;
import thybulle.misc.*;

//TODO: Test new classes, write readme
//To test: Browser
//Refactoring: unify varargs/lists, include Player parse method, include utility methods in TimeInterval, Figure out how to parse broken games, change browser name to enum

public class Driver {
	static final Logging logging = new Logging(System.out);

	public static void main(String[] args) throws IOException {
		String inputFile = args[0];
		File outputFile = new File(args[1]);
		
		HighlightsLogger.setOutput(System.out);
		
		HighlightsCompiler hc = Highlights.compiler();
		logging.info("Parsing input");
		Collection<Constraint> constraints = InputParsing.parseConstraints(inputFile);
		GameSource source = InputParsing.parseSource(inputFile);
		Collection<Pair<LocalDate, LocalDate>> dataset = InputParsing.parseDataset(inputFile);
		Collection<Team> teams = InputParsing.parseTeams(inputFile);

		Runtime.getRuntime().addShutdownHook(
			new Thread(){
				public void run(){
					logging.info("Cleaning up resources");
					try{
						source.exit();
					} catch(IOException e){
						logging.error("Unable to shutdown properly.");
					}
					
				}
			}
		);
		
		logging.info("Getting game information");
		List<GameInfo> information = new LinkedList<GameInfo>();
		for(Pair<LocalDate, LocalDate> p : dataset) {
			information.addAll(source.getTeamGameInformationBetweenDates(p.first(), p.second(), teams.toArray(new Team[0])));
		}
		logging.info("Getting play-by-play data");
		List<Game> games = source.getGames(information);
		logging.info("Done getting play-by-play data. Found " + games.size() + (games.size() == 1 ? " game." : " games."));
		hc.addGames(games.toArray(new Game[0]));
		hc.addConstraints(constraints.toArray(new Constraint[0]));
		logging.info("Finding all plays that satisfy constraints");
		Highlights h = hc.compile();
		logging.info("Found " + h.size() + (h.size() == 1 ? " play." : " plays."));
		h.saveVideo(outputFile, logging);
		logging.info("Done!");
		System.exit(0);
	}
}