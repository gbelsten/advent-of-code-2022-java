package gab.aoc.twentytwo;

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collector;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import gab.aoc.util.InputFileException;
import gab.aoc.util.LogicException;

public class Day10 extends DayTask
{
  @Override
  public void doTask(PrintStream output, boolean debug)
  {
    final List<String> inputLines = getFileLines();

    final Collector<String, RegisterValues, RegisterValues> registerCollector =
      Collector.of(
        RegisterValues::new,
        RegisterValues::doInstruction,
        RegisterValues::combine
    );

    final RegisterValues registerValues = inputLines.stream()
      .collect(registerCollector);

    final int sumOfSixSignalStrengths =
      registerValues.getSignalStrengthAtCycles(20, 60, 100, 140, 180, 220);

    output.println("Sum of six signal strengths: " + sumOfSixSignalStrengths);

    final List<String> crtLines = registerValues.drawAsSprite();
    output.println("CRT representation:");
    crtLines.stream().forEach(output::println);
  }

  /**
   * Helper class. Holds the register values and includes functions for
   * accumulating input lines, and processing the results.
   */
  private static class RegisterValues extends ArrayList<Integer>
  {
    private static final int CRT_WIDTH = 40;
    private static final int CRT_HEIGHT = 6;

    private Integer currentValue = 1;

    public RegisterValues()
    {
      super();
      this.add(currentValue);
    }

    /**
     * Accumulator for input lines.
     */
    public void doInstruction(final String inputLine)
    {
      //-----------------------------------------------------------------------
      // Whether this is a noop or an addx, we start by repeating the current
      // register value representing one cycle.
      //-----------------------------------------------------------------------
      this.add(currentValue);

      if (!"noop".equals(inputLine))
      {
        final String[] tokens = inputLine.split(" ");

        if (tokens.length != 2 || !"addx".equals(tokens[0]))
        {
          throw new InputFileException("Bad input line: " + inputLine);
        }

        //---------------------------------------------------------------------
        // If this is an addx, then we alter the register value by the given
        // factor and then record the value, representing a second cycle.
        //---------------------------------------------------------------------
        final Integer valueChange = Integer.parseInt(tokens[1]);
        currentValue += valueChange;
        this.add(currentValue);
      }
    }

    /**
     * This should never be called, as we aren't parallelising.
     */
    public RegisterValues combine(final RegisterValues values)
    {
      this.addAll(values);
      return this;
    }

    /**
     * @return the sum of the signal strengths at the given cycles. The input
     * cycles are one-indexed.
     */
    public int getSignalStrengthAtCycles(final int... cycles)
    {
      //-----------------------------------------------------------------------
      // The cycle number is one-indexed - and we need to keep it that way, as
      // it's used as a multiplicative factor - but the values in our list are
      // zero-indexed, so we need to account for that.
      //-----------------------------------------------------------------------
      return IntStream.of(cycles)
        .map( i -> i * this.get(i - 1))
        .sum();
    }

    /**
     * @return a list of lines representing the sprite drawing given by
     * comparing the register values to the pixel index
     */
    public List<String> drawAsSprite()
    {
      if (this.size() < CRT_HEIGHT * CRT_WIDTH)
      {
        throw new LogicException("Insufficient register values");
      }

      final List<String> output = IntStream.range(0, CRT_HEIGHT)
        .mapToObj(this::getSpriteLine)
        .collect(Collectors.toList());

      return output;
    }

    /**
     * @return the sprite line for the given row index
     */
    private String getSpriteLine(final int lineIndex)
    {
      final int startIndex = lineIndex * CRT_WIDTH;
      final StringBuilder line = new StringBuilder();

      IntStream.range(startIndex, startIndex + CRT_WIDTH)
        .mapToObj(this::getPixel)
        .forEach(line::append);

      return line.toString();
    }

    /**
     * @return the character to draw at the given pixel index
     */
    private char getPixel(final int pixelIndex)
    {
      final int registerValue = this.get(pixelIndex);
      final int linePosition = pixelIndex % CRT_WIDTH;
      final int offset = registerValue - linePosition;
      final boolean pixelIsLit = (Math.abs(offset) <= 1);
      return pixelIsLit ? '#' : '.';
    }
  }
}
