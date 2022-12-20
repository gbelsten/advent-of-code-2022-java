package gab.aoc.twentytwo;

import java.io.PrintStream;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import gab.aoc.util.InputFileException;
import gab.aoc.util.TaskException;

public class Day6 extends DayTask
{
  /**
   * Return the index at the end of the first unique substring in the given
   * line with the given substring length.
   */
  private static int detectFirstUniqueSubstring(
    final String line, final int substrLength)
  {
    final String marker = IntStream.rangeClosed(substrLength, line.length())
      .mapToObj( i -> line.substring(i - substrLength, i) )
      .filter(Day6::stringContainsUniqueChars)
      .findFirst()
      .orElseThrow( () ->
        new InputFileException("Failed to find unique substring") );

    return line.indexOf(marker) + substrLength;
  }

  /**
   * Return true if a given string only contains unique characters (that is,
   * that no character appears more than once).
   */
  private static boolean stringContainsUniqueChars(final String input)
  {
    final List<Integer> chars = input.chars()
      .boxed()
      .collect(Collectors.toList());

    final boolean anyDuplicates = chars.stream()
      .anyMatch( i -> (Collections.frequency(chars, i) > 1) );

    return !anyDuplicates;
  }

  @Override
  public void doTask(PrintStream output, boolean debug) throws TaskException
  {
    final List<String> inputLines = getFileLines();
    final String input = inputLines.get(0);

    final int markerLocation = detectFirstUniqueSubstring(input, 4);
    output.println("Part 1: " + markerLocation);

    final int messageLocation = detectFirstUniqueSubstring(input, 14);
    output.println("Part 2: " + messageLocation);
  }
}
