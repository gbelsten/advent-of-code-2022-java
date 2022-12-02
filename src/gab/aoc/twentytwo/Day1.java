package gab.aoc.twentytwo;

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Stream;

class Day1 extends DayTask
{
  @Override
  public void doTask(final PrintStream output, final boolean debug)
  {
    //-------------------------------------------------------------------------
    // By joining on comma, and splitting on double-comma, we get a sequence
    // of comma-separated lists representing each elf.
    //-------------------------------------------------------------------------
    final List<String> inputLines = getFileLines();
    final String[] listForEachElf = String.join(",", inputLines).split(",,");
    final List<Integer> countForEachElf = new ArrayList<>();

    for (final String commaSeparatedCalories : listForEachElf)
    {
      final String[] calorieArray = commaSeparatedCalories.split(",");
      final int total = Stream.of(calorieArray)
        .mapToInt(Integer::parseInt)
        .sum();
      countForEachElf.add(total);
    }

    //-------------------------------------------------------------------------
    // Highest-to-lowest sort.
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
