package gab.aoc.twentytwo;

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Stream;

/**
 * Day 1's task. A simple one - we just group the list of calories for each
 * elf and use stream logic to compute the results.
 */
class Day1 extends DayTask
{
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
    final List<Integer> countForEachElf = new ArrayList<>();

    for (final String commaSeparatedCalories : listForEachElf)
    {
      //-----------------------------------------------------------------------
      // Processing using IntStream makes it easy to sum each list -
      // marginally less verbose than using a for loop. (Getting rid of the
      // outer for loop would require some, IMO, ugly stream logic.)
      //-----------------------------------------------------------------------
      final String[] calorieArray = commaSeparatedCalories.split(",");
      final int total = Stream.of(calorieArray)
        .mapToInt(Integer::parseInt)
        .sum();
      countForEachElf.add(total);
    }

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
