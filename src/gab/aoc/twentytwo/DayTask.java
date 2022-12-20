package gab.aoc.twentytwo;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import gab.aoc.util.InputFileException;
import gab.aoc.util.TaskException;

/**
 * Superclass for each day's task, with getters for the input file. Each
 * day needs to derive this class and implement 'doTask'.
 */
public abstract class DayTask
{
  private static final String INPUTS_DIR = "inputs";

  /**
   * Get a Path representing this day's input file, which is expected to have
   * been (manually) saved/downloaded to the inputs directory as e.g.
   * "day1.txt", "day2.txt" etc.
   */
  protected final Path getFilePath()
  {
    final String dayName = this.getClass().getSimpleName().toLowerCase();
    final String filePathString = INPUTS_DIR + "/" + dayName + ".txt";
    final Path filePath = Paths.get(filePathString);
    return filePath;
  }

  /**
   * Get a List of Strings representing the lines of the input file.
   * Excludes the end-of-line characters.
   */
  protected final List<String> getFileLines()
  {
    try
    {
      final Path filePath = getFilePath();
      final List<String> fileLines = Files.readAllLines(filePath);
      return fileLines;
    }
    catch (final FileNotFoundException e)
    {
      throw new InputFileException("Input file not found", e);
    }
    catch (final IOException e)
    {
      throw new InputFileException("Could not read input file", e);
    }
  }

  /**
   * Run the day's task, outputting the result to the given PrintStream.
   * Which will probably always be stdout, but it gives me flexibility to
   * send it somewhere else in the future.
   *
   * In principle, 'debug' should turn on debug logging, but I haven't made
   * use of that. Any proper debug logging in the future should go to a trace
   * file, which will require a function signature update.
   */
  public abstract void doTask(
    final PrintStream output, final boolean debug) throws TaskException;
}
