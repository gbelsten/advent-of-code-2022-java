package gab.aoc.twentytwo;

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.BinaryOperator;
import java.util.function.Supplier;
import java.util.function.UnaryOperator;
import java.util.stream.Collector;

public class Day3 extends DayTask
{
  private static final int GROUP_SIZE = 3;

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

  /**
   * Given a group of elves (i.e. a list of 3 inputs), find the character that
   * is present in all three, returned as its decimal code point.
   */
  private static int getGroupCommonItem(final ElfGroup group)
  {
    final String first = group.get(0);
    final String second = group.get(1);
    final String third = group.get(2);

    final int commonItemCode = first.codePoints()
      .filter( codePoint -> second.indexOf(codePoint) >= 0 )
      .filter( codePoint -> third.indexOf(codePoint) >= 0 )
      .findFirst()
      .orElseThrow( () -> new LogicException("Bad group: " + group) );

    return commonItemCode;
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

    final int sumOfBadgePriorities = inputLines.stream()
      .collect(new ElfGroupCollector())
      .stream()
      .map(Day3::getGroupCommonItem)
      .mapToInt(Day3::getPriorityForItem)
      .sum();

    output.println("Result for part 2: " + sumOfBadgePriorities);
  }

  /**
   * Effectively a typedef. Simplifies the collector that we set up below.
   * Groups will always be of size 3.
   */
  private static class ElfGroup extends ArrayList<String>
  {
    public ElfGroup()
    {
      super(GROUP_SIZE);
    }
  }

  /**
   * Another typdef to simplify the collector parameters, which would
   * otherwise be littered with <List<List<String>>>.
   */
  private static class ElfGroupList extends ArrayList<ElfGroup> {}

  /**
   * Custom collector. Takes the list of input lines (as strings) and
   * partitions it into a list of 'elf groups' (i.e. a list of lists of 3
   * entries each).
   */
  private static class ElfGroupCollector
      implements Collector<String, ElfGroupList, ElfGroupList>
  {
    private ElfGroup batch = new ElfGroup();

    private final BiConsumer<ElfGroupList, String> accumulator =
        (collectorOutput, lineToAdd) ->
    {
      batch.add(lineToAdd);

      if (batch.size() == GROUP_SIZE)
      {
        collectorOutput.add(batch);
        batch = new ElfGroup();
      }
    };

    private final BinaryOperator<ElfGroupList> combiner =
        (first, second) ->
    {
      ElfGroupList combined = new ElfGroupList();
      combined.addAll(first);
      combined.addAll(second);
      return combined;
    };

    private final UnaryOperator<ElfGroupList> finisher =
        output ->
    {
      if (!batch.isEmpty())
      {
        output.add(batch);
        batch = new ElfGroup();
      }

      return output;
    };

    @Override
    public Supplier<ElfGroupList> supplier()
    {
      return ElfGroupList::new;
    }

    @Override
    public BiConsumer<ElfGroupList, String> accumulator()
    {
      return accumulator;
    }

    @Override
    public BinaryOperator<ElfGroupList> combiner()
    {
      return combiner;
    }

    @Override
    public UnaryOperator<ElfGroupList> finisher()
    {
      return finisher;
    }

    @Override
    public Set<Characteristics> characteristics()
    {
      return new LinkedHashSet<>();
    }
  }
}
