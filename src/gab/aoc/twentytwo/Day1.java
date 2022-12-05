package gab.aoc.twentytwo;

import java.io.PrintStream;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Day 1's task. A simple one - we just group the list of calories for each
 * elf and use stream logic to compute the results.
 */
class Day1 extends DayTask
{
  /**
   * Takes a comma-separated list of numbers and returns their sum.
   */
  private static int sumCalorieList(final String commaSeparatedList)
  {
    final String[] items = commaSeparatedList.split(",");
    final int sum = Stream.of(items).mapToInt(Integer::parseInt).sum();
    return sum;
  }

  @Override
  public void doTask(final PrintStream output, final boolean debug)
  {
    //-------------------------------------------------------------------------
    // By joining on comma, we can identify the file's empty lines by then
    // splitting on double commas. The result, a list of comma-separated
    // calorie counts per elf, is convenient for further processing.
    //-------------------------------------------------------------------------
    final List<String> inputLines = getFileLines();
    final String[] listForEachElf = String.join(",", inputLines).split(",,");

    final List<Integer> countForEachElf = Stream.of(listForEachElf)
      .map(Day1::sumCalorieList)
      .collect(Collectors.toList());

    //-------------------------------------------------------------------------
    // This sorts into highest-to-lowest order, which trivialises finding
    // the highest and summing the top three.
    //-------------------------------------------------------------------------
    Collections.sort(countForEachElf, Collections.reverseOrder());

    output.println("The highest calorie count for any elf is: " +
      countForEachElf.get(0));

    final int sumOfTopThree = countForEachElf.stream()
      .limit(3)
      .mapToInt(Integer::valueOf)
      .sum();

    output.println("The sum of the top 3 is: " + sumOfTopThree);
  }
}
