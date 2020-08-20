package thybulle.driver;

import java.util.*;
import java.util.List;
import java.util.logging.*;
import java.io.*;
import java.time.LocalDate;

import thybulle.highlights.*;
import thybulle.misc.*;

//Refactoring: Remove (most) varargs methods, Figure out how to parse broken games, change browser name to enum, fix and commit tests, fix 2019-20 season dates

public class Driver {
	static final Logging logging = new Logging(System.out);

	public static void main(String[] args) throws IOException {
		checkFFMpeg();
		String inputFile = args[0];
		File outputFile = new File(args[1]);
		
		HighlightsLogger.setOutput(System.out);
		
		HighlightsCompiler hc = Highlights.compiler();
		logging.info("Parsing input");
		Collection<Constraint> constraints = InputParsing.parseConstraints(inputFile);
		GameSource source = InputParsing.parseSource(inputFile);
		Collection<Pair<LocalDate, LocalDate>> dataset = InputParsing.parseDataset(inputFile);
		Collection<Team> teams = InputParsing.parseTeams(inputFile);

		new Thread(() -> {
			Scanner keyboard = new Scanner(System.in);
			while(keyboard.hasNextLine()){
				String input = keyboard.nextLine();
				if(input.toLowerCase().equals("quit") || input.toLowerCase().equals("exit")){
					logging.info("Cleaning up resources");
					try{
						source.close();
						System.exit(0);
					} catch(IOException e){
						throw new UncheckedIOException(e);
					}
				}
			}
		}).start();

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
		if(h.size() == 0){
			logging.error("No plays were found. Exiting.");
			source.close();
			System.exit(0);
		}
		h.saveVideo(outputFile, logging);
		logging.info("Cleaning up resources.");
		source.close();
		logging.info("Done!");
		System.exit(0);
	}

	private static void checkFFMpeg(){
		try{
			Runtime.getRuntime().exec("ffmpeg");
		} catch(IOException e){
			throw new IllegalStateException("ffmpeg is not installed or has not been added to the PATH.", e);
		}
	}
}