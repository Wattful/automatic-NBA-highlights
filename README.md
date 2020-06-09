A tool which allows users to automatically edit together NBA highilight videos.

An example of a video created using this program can be found [here](https://youtu.be/udCukXcvWbo)

The user specifies the types of plays that they would like to see, as well as what games they would like to include plays from.

The program will then find all plays from the specified games which meet the given requirements, and edit a video of them together.

This is accomplished using a source: A method of retrieving play-by-play and video data. The details of sources are discussed below.

# Setup
Before running the program, you must do all of the following steps:

1. Clone this repository (obviously)
2. Install FFMpeg and add it to the PATH
2. Get required JAR files (explained in the Dependencies section below)
3. Install browser driver (explained in config folder README)
4. Specify settings in config files (explained in config folder README)

# Dependencies
The program has four dependencies:

[JSON-java](https://github.com/stleary/JSON-java)

[JSoup](https://github.com/jhy/jsoup)

[Selenium](https://github.com/SeleniumHQ/selenium)

[FFMpeg](https://github.com/ffmpeg/ffmpeg)

The following maven pom.xml file can be used to get all required JAR files:

```
<dependency>
    <groupId>org.json</groupId>
    <artifactId>json</artifactId>
    <version>20200518</version>
</dependency>
<dependency>
    <groupId>org.jsoup</groupId>
    <artifactId>jsoup</artifactId>
    <version>1.13.1</version>
</dependency>
<dependency>
    <groupId>org.seleniumhq.selenium</groupId>
    <artifactId>selenium-java</artifactId>
    <version>3.141.59</version>
</dependency>
```
Use maven or paste the XML into [JAR-download.com](https://jar-download.com/online-maven-download-tool.php) to get required JAR files.

Since FFMPeg is a command line tool as opposed to a Java library, it must be installed separately. See [here](https://www.ffmpeg.org/download.html).

# Input
The program takes input from the command line, a JSON input file, and several JSON config files.

## JSON Input
The main JSON input file is the program's primary way of taking input.

Through this file, the user specifies what plays they want to see, which games they would like to see plays from, and whay source they would like to use.

The specification for this input file can be found in the README for the config folder.

## Config Files
The program reads settings from several JSON config files.

Default versions and specifications for these files can be found in the config folder.

**Look at these config files before running the program.** Some settings do not have a valid default value and must be set manually before the program can be run.

## Command Line Input
The program takes two command line options, both of which are mandatory.

The first one is the path to the JSON input file, and the second one is the desired location of the finished video file.

# Build and Run
This project is written in Java, and as such it is compiled and run using the [`javac`](https://docs.oracle.com/en/java/javase/13/docs/specs/man/javac.html) and [`java`](https://docs.oracle.com/en/java/javase/13/docs/specs/man/java.html) commands, respectively (links contain more information about the commands).

To build, navigate to the source directory, then run:
```
javac -cp CLASSPATH thybulle/driver/*.java thybulle/highlights/*.java thybulle/misc/*.java
```

Replace CLASSPATH with a list of paths to all required JAR files (More info about the java classpath can be found [here](https://stackoverflow.com/questions/2396493/what-is-a-classpath-and-how-do-i-set-it?lq=1)).

To run, stay in the source directory, and run:
```
java -cp CLASSPATH thybulle.driver.Driver "INPUT.JSON_PATH" "OUTPUT_FILE_PATH"
```

Replacing INPUT.JSON_PATH with the path to the input.json file, and OUTPUT_FILE_PATH with the desired location to save the finished video file.

Make sure that the classpath is the same when compiling and running. Discrepancies between the compile-time and runtime classpaths can lead to mysterious runtime errors.

# Aborting During Runtime
There are two ways to abort the program during runtime:

Keyboard Interrupting will cause the program to exit immediately, without saving data or killing the browser drivers.

Inputting "quit" or "exit" will cause the program to attempt to save its data and exit browser drivers before shutting down. This may take several seconds.

# Sources
A source is a method of retrieving play-by-play and video data.

The user can decide which source they use to get data.

There is currently only one source: NBA Advanced Stats.

## NBA Advanced Stats
NBA Advanced Stats pulls its data from stats.nba.com, a source of official NBA data.

This source has several advantages. Notably, it has pre-editied, high quality clips of almost every play.

This allows the program to run relatively quickly. However, it also makes the clips themselves rigid; it is impossible to specify, for example, how long each clip should be.

Additionally, NBA Advanced Stats has one large flaw: It is impossible to tell the difference between players on the same team with the same last name.

There's not much that can be done about this without unacceptably increasing the program's runtime.

So if you want to watch highlights of Giannis, you'll have to sit through the highlights of Thanasis.

## Developing New Sources

The application design of the program makes it simple to develop new sources.

In order to do this, one must write a class which implements the GameSource interface.

The highlights/AdvancedStats.java class is a blueprint as to how a Source is implemented. 

## Data Storage
Sources which collect data from the internet can drastically reduce their runtime by storing data on the user's local machine.

NBA Advanced stats has the option to do this. A full season of play-by-play data takes up approximately 500 megabytes of data.
