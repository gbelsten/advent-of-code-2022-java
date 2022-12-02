package gab.aoc.twentytwo;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

public abstract class DayTask
{
  private static final String INPUTS_DIR = "inputs";

  protected final Path getFilePath()
  {
    final String dayName = this.getClass().getSimpleName().toLowerCase();
    final String filePathString = INPUTS_DIR + "/" + dayName + ".txt";
    final Path filePath = Paths.get(filePathString);
    return filePath;
  }

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

  public abstract void doTask(
    final PrintStream output, final boolean debug) throws TaskException;
}
