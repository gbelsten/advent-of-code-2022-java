package gab.aoc.twentytwo;

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class Day5 extends DayTask
{
  @Override
  public void doTask(final PrintStream output, final boolean debug)
  {
    final List<String> inputLines = getFileLines();
    final int breakIndex = inputLines.indexOf("");

    if (breakIndex == -1)
    {
      throw new InputFileException("Failed to break");
    }

    final List<String> stackLines = inputLines.subList(0, breakIndex);

    final List<Instruction> instructions =
      inputLines.subList(breakIndex + 1, inputLines.size()).stream()
      .map(Instruction::new)
      .collect(Collectors.toList());

    //-------------------------------------------------------------------------
    // Part 1: moving crates one at a time.
    //-------------------------------------------------------------------------
    final CargoLayout partOneCargo = CargoLayout.withSingleCrane(stackLines);
    instructions.stream().forEach(partOneCargo::applyInstruction);
    final String partOneAnswer = partOneCargo.getTopOfEachStack();
    output.println("Part 1: " + partOneAnswer);

    //-------------------------------------------------------------------------
    // Part 2: moving multiple crates at once.
    //-------------------------------------------------------------------------
    final CargoLayout partTwoCargo = CargoLayout.withMultiCrane(stackLines);
    instructions.stream().forEach(partTwoCargo::applyInstruction);
    final String partTwoAnswer = partTwoCargo.getTopOfEachStack();
    output.println("Part 2: " + partTwoAnswer);
  }

  /**
   * Helper enum for identifying whether we are doing part 1 (moving one crate
   * at a time) or part 2 (moving multiple crates at a time).
   */
  private enum CraneType { SINGLE, MULTI; }

  /**
   * Class representing the layout of the cargo, stored as lists of the
   * characters that represent each crate.
   */
  private static class CargoLayout
  {
    private final List<List<Character>> stacks = new ArrayList<>();
    private final CraneType craneType;

    /**
     * Layout for Part 1, where one crate is moved at a time.
     */
    public static CargoLayout withSingleCrane(final List<String> lines)
    {
      return new CargoLayout(lines, CraneType.SINGLE);
    }

    /**
     * Layout for Part 2, where crates are moved in bulk.
     */
    public static CargoLayout withMultiCrane(final List<String> lines)
    {
      return new CargoLayout(lines, CraneType.MULTI);
    }

    private CargoLayout(final List<String> lines, final CraneType craneType)
    {
      //-----------------------------------------------------------------------
      // We ignore the line with the stack indices for simplicity, as it is
      // always in sequence (albeit one-indexed instead of zero-indexed), so
      // we just store the stacks in a list.
      //-----------------------------------------------------------------------
      lines.subList(0, lines.size() - 1).stream().forEach(this::addStackLine);
      this.craneType = craneType;
    }

    /**
     * Process an input line and add it to the cargo stacks.
     */
    private void addStackLine(final String line)
    {
      //-----------------------------------------------------------------------
      // The character representing the crate - or the space if there is no
      // crate there - is in a repeated location. We can use division
      // remainder trickery to get the right characters from each line.
      //-----------------------------------------------------------------------
      final List<Character> crates = IntStream.range(0, line.length())
        .filter( i -> i % 4 == 1 )
        .mapToObj(line::charAt)
        .collect(Collectors.toList());

      //-----------------------------------------------------------------------
      // First time through, initialize the stacks. We'll always have the same
      // number of crates, including blank entries.
      //-----------------------------------------------------------------------
      if (stacks.isEmpty())
      {
        IntStream.range(0, crates.size())
          .forEach( i -> stacks.add(new ArrayList<>() ));
      }

      //-----------------------------------------------------------------------
      // We insert each crate at the start of the list, so each list
      // represents a cargo stack from bottom to top.
      //-----------------------------------------------------------------------
      IntStream.range(0, crates.size())
        .filter( i -> Character.isLetter(crates.get(i)) )
        .forEach( i -> stacks.get(i).add(0, crates.get(i)) );
    }

    /**
     * Moves a cargo slice between stacks, from the given stack index, to the
     * given stack index, moving the given number of crates at once.
     */
    private void moveSliceBetweenStacks(
        final int from, final int to, final int count)
    {
      final List<Character> fromStack = stacks.get(from);
      final List<Character> toStack = stacks.get(to);
      final int removeAtIndex = fromStack.size() - count;

      final List<Character> movedCrates = IntStream.range(0, count)
        .mapToObj( i -> fromStack.remove(removeAtIndex) )
        .collect(Collectors.toList());

      toStack.addAll(movedCrates);
    }

    /**
     * Apply the given instruction.
     */
    public void applyInstruction(final Instruction instruction)
    {
      final int from = instruction.fromStack();
      final int to = instruction.toStack();
      final int numberToMove = instruction.numberToMove();

      //-----------------------------------------------------------------------
      // For Part 1, one crate is moved at a time, repeated until we've moved
      // 'x' crates. For Part 2, 'x' crates are moved in one go.
      //-----------------------------------------------------------------------
      final boolean oneAtATime = (craneType == CraneType.SINGLE);
      final int applyTimes = oneAtATime ? numberToMove : 1;
      final int moveAtATime = oneAtATime ? 1 : numberToMove;

      IntStream.range(0, applyTimes)
        .forEach( i -> this.moveSliceBetweenStacks(from, to, moveAtATime) );
    }

    /**
     * Get a String containing the top label from each stack.
     */
    public String getTopOfEachStack()
    {
      return stacks.stream()
        .map( stack -> stack.get(stack.size() - 1) )
        .map(String::valueOf)
        .collect(Collectors.joining());
    }
  }

  /**
   * Helper class for parsing instruction lines.
   */
  private static class Instruction
  {
    private final int numberToMove;
    private final int fromStack;
    private final int toStack;

    /**
     * Takes a line of input from the instruction stack, and parses it into
     * an easier-to-handle format.
     */
    public Instruction(final String line)
    {
      //-----------------------------------------------------------------------
      // We expect all instructions to be in the format: "move <number> from
      // <number> to <number>". So, we can split on spaces to pick out the
      // right fields.
      //-----------------------------------------------------------------------
      final String[] parts = line.split(" ");

      if (parts.length != 6)
      {
        throw new InputFileException("Not an instruction line: " + line);
      }

      try
      {
        this.numberToMove = Integer.parseInt(parts[1]);

        //---------------------------------------------------------------------
        // The indices in the instructions are offset by one: they are
        // one-indexed rather than zero-indexed, whereas we're just using a
        // list internally for convenience.
        //---------------------------------------------------------------------
        this.fromStack = Integer.parseInt(parts[3]) - 1;
        this.toStack = Integer.parseInt(parts[5]) - 1;
      }
      catch (final NumberFormatException e)
      {
        throw new InputFileException("Bad format, instruction line: " + line);
      }
    }

    public int numberToMove() { return numberToMove; }
    public int fromStack() { return fromStack; }
    public int toStack() { return toStack; }
  }
}
