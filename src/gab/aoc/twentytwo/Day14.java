package gab.aoc.twentytwo;

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

public class Day14 extends DayTask
{
  private static int getMinX(final List<Coordinate> rocks)
  {
    return rocks.stream()
      .mapToInt(Coordinate::x)
      .min()
      .orElseThrow( () -> new InputFileException("Failed to get min x") );
  }

  private static int getMaxX(final List<Coordinate> rocks)
  {
    return rocks.stream()
      .mapToInt(Coordinate::x)
      .max()
      .orElseThrow( () -> new InputFileException("Failed to get max x") );
  }

  private static int getMaxY(final List<Coordinate> rocks)
  {
    return rocks.stream()
      .mapToInt(Coordinate::y)
      .max()
      .orElseThrow( () -> new InputFileException("Failed to get max y") );
  }

  private static List<String> visualise(
      final List<Coordinate> rocks, final List<Coordinate> sand)
  {
    final int minX = getMinX(rocks);
    final int maxX = getMaxX(rocks);
    final int maxY = getMaxY(rocks);
    final List<String> printLines = new ArrayList<>();

    for (int y = 0; y <= maxY; y++)
    {
      final StringBuilder line = new StringBuilder();

      for (int x = minX; x <= maxX; x++)
      {
        final Coordinate c = new Coordinate(x, y);

        if (c.y() == 0 && c.x() == 500)
        {
          line.append('+');
        }
        else if (rocks.contains(c))
        {
          line.append('#');
        }
        else if (sand.contains(c))
        {
          line.append('o');
        }
        else
        {
          line.append('.');
        }
      }

      printLines.add(line.toString());
    }

    return printLines;
  }

  @Override
  public void doTask(PrintStream output, boolean debug)
  {
    final List<String> inputLines = getFileLines();

    final List<RockStructure> structures = inputLines.stream()
      .map(RockStructure::buildFromInputLine)
      .collect(Collectors.toList());

    final List<Coordinate> allRocks = structures.stream()
      .flatMap( s -> s.getRocks().stream() )
      .collect(Collectors.toList());

    //-------------------------------------------------------------------------
    // Visualise the rock layout before starting, to ensure it looks right.
    //-------------------------------------------------------------------------
    output.println("Rock layout before adding sand:");
    visualise(allRocks, Arrays.asList()).forEach(output::println);
  }

  private static class Coordinate
  {
    private final int x;
    private final int y;

    public Coordinate(final int x, final int y)
    {
      this.x = x;
      this.y = y;
    }

    public Coordinate(final String str)
    {
      final String[] tokens = str.split(",");

      if (tokens.length != 2)
      {
        throw new InputFileException("Bad coordinate string: " + str);
      }

      this.x = Integer.parseInt(tokens[0]);
      this.y = Integer.parseInt(tokens[1]);
    }

    public int x() { return this.x; }
    public int y() { return this.y; }

    @Override
    public String toString() { return this.x() + "," + this.y(); }

    @Override
    public boolean equals(final Object o)
    {
      if (o instanceof Coordinate)
      {
        final Coordinate c = (Coordinate)o;
        return (this.x() == c.x() && this.y() == c.y());
      }
      else
      {
        return false;
      }
    }

    @Override
    public int hashCode()
    {
      return Arrays.hashCode(new int[] { this.x(), this.y()});
    }
  }

  private static class RockStructure
  {
    public static RockStructure buildFromInputLine(final String input)
    {
      final String[] tokens = input.split(" -> ");
      return Stream.of(tokens)
        .collect(
        RockStructure::new, RockStructure::addLineTo, RockStructure::combine);
    }

    private final List<Coordinate> rocks = new ArrayList<>();
    private Coordinate lastAdded = null;

    public List<Coordinate> getRocks()
    {
      return Collections.unmodifiableList(this.rocks);
    }

    public void addLineTo(final String newCoordinate)
    {
      final Coordinate endOfLine = new Coordinate(newCoordinate);

      if (this.lastAdded == null)
      {
        rocks.add(endOfLine);
        this.lastAdded = endOfLine;
        return;
      }

      final Coordinate startOfLine = this.lastAdded;
      final int xDif = endOfLine.x() - startOfLine.x();
      final int yDif = endOfLine.y() - startOfLine.y();

      //-----------------------------------------------------------------------
      // Lines can only be horizontal or vertical.
      //-----------------------------------------------------------------------
      if (xDif != 0 && yDif != 0)
      {
        throw new InputFileException(
          "Line not straight: " + startOfLine + " to " + endOfLine);
      }

      final boolean vertical = (yDif != 0);
      final int length = Math.abs(xDif + yDif);
      final int start = (vertical) ? Math.min(startOfLine.y(), endOfLine.y())
                                   : Math.min(startOfLine.x(), endOfLine.x());

      final List<Coordinate> newRocks = IntStream.rangeClosed(start, start + length)
        .mapToObj( i -> (vertical) ? new Coordinate(endOfLine.x(), i)
                                   : new Coordinate(i, endOfLine.y()) )
        .filter( r -> !this.rocks.contains(r) )
        .collect(Collectors.toList());

      this.rocks.addAll(newRocks);
      this.lastAdded = endOfLine;
    }

    public RockStructure combine(final RockStructure structure)
    {
      this.rocks.addAll(structure.getRocks());
      return this;
    }
  }
}
