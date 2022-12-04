package gab.aoc.twentytwo;

import java.io.PrintStream;
import java.util.List;

public class Day3 extends DayTask
{
  /**
   * Takes a line of the task input, splits it in half, and finds the common
   * character present in both parts. Assumes that there is only one common
   * character. Output is the integer decimal code point of the character.
   */
  private static int getSharedItem(final String line)
  {
    //-------------------------------------------------------------------------
    // We expect that there is only one character common to both halves, but
    // that character can occur multiple times in either half, so we can't use
    // the naive solution of looking for a char that's present twice; we
    // actually have to split the string and find a match.
    //-------------------------------------------------------------------------
    final int splitPoint = line.length() / 2;
    final String firstPart = line.substring(0, splitPoint);
    final String secondPart = line.substring(splitPoint, line.length());

    final int outputCode = firstPart.codePoints()
      .filter( codePoint -> secondPart.indexOf(codePoint) >= 0 )
      .findFirst()
      .orElseThrow( () -> new LogicException("Bad line: " + line) );

    return outputCode;
  }

  /**
   * Given the input item code, get the "priority" for the item, as specified
   * by the task (where 'a-z' is 1-26, 'A-Z' is 27-52).
   */
  private static int getPriorityForItem(final int itemCode)
  {
    //-------------------------------------------------------------------------
    // We're abusing the ASCII table here. 'a-z' is represented by decimal 97
    // onwards, so an offset of 96 gets us the right range. 'A-Z' is
    // represented by decimal 65 onwards, so an offset of 38 starts us at 27.
    //-------------------------------------------------------------------------
    final int offset = (Character.isUpperCase(itemCode)) ? 38 : 96;
    return itemCode - offset;
  }

  @Override
  public void doTask(final PrintStream output, final boolean debug)
  {
    final List<String> inputLines = getFileLines();
    final int sumOfPriorities = inputLines.stream()
      .map(Day3::getSharedItem)
      .mapToInt(Day3::getPriorityForItem)
      .sum();

    output.println("Result: " + sumOfPriorities);
  }
}
