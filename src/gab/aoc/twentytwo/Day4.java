package gab.aoc.twentytwo;

import java.io.PrintStream;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

public class Day4 extends DayTask
{
  private static List<List<Integer>> getPairing(final String line)
  {
    final String[] ranges = line.split(",");
    final List<List<Integer>> pairings = Stream.of(ranges)
      .map(Day4::getRange)
      .collect(Collectors.toList());

    return pairings;
  }

  private static List<Integer> getRange(final String rangeString)
  {
    final String[] bounds = rangeString.split("-");
    final int start = Integer.parseInt(bounds[0]);
    final int end = Integer.parseInt(bounds[1]);
    final List<Integer> range = IntStream.rangeClosed(start, end)
      .boxed()
      .collect(Collectors.toList());

    return range;
  }

  private static boolean checkForSubset(final List<List<Integer>> pairing)
  {
    final List<Integer> first = pairing.get(0);
    final List<Integer> second = pairing.get(1);
    final boolean subsetExists =
      (Collections.indexOfSubList(first, second) >= 0) ||
      (Collections.indexOfSubList(second, first) >= 0);

    return subsetExists;
  }

  @Override
  public void doTask(final PrintStream output, final boolean debug)
  {
    final List<String> inputLines = getFileLines();
    final int subsetPairingsCount = inputLines.stream()
      .map(Day4::getPairing)
      .filter(Day4::checkForSubset)
      .toArray()
      .length;

    output.println("Number of pairings with subset: " + subsetPairingsCount);
  }

}
