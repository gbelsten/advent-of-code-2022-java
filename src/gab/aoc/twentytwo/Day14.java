package gab.aoc.twentytwo;

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import gab.aoc.util.Coordinate;
import gab.aoc.util.InputFileException;
import gab.aoc.util.LogicException;

public class Day14 extends DayTask
{
  /**
   * Given a list of rocks, return the lowest x coordinate.
   */
  private static int getMinX(final List<Coordinate> rocks)
  {
    return rocks.stream()
      .mapToInt(Coordinate::x)
      .min()
      .orElseThrow( () -> new InputFileException("Failed to get min x") );
  }

  /**
   * Given a list of rocks, return the highest x coordinate.
   */
  private static int getMaxX(final List<Coordinate> rocks)
  {
    return rocks.stream()
      .mapToInt(Coordinate::x)
      .max()
      .orElseThrow( () -> new InputFileException("Failed to get max x") );
  }

  /**
   * Given a list of rocks, return the highest y coordinate.
   */
  private static int getMaxY(final List<Coordinate> rocks)
  {
    return rocks.stream()
      .mapToInt(Coordinate::y)
      .max()
      .orElseThrow( () -> new InputFileException("Failed to get max y") );
  }

  private static Coordinate coordinateFromInput(final String input)
  {
    final String[] tokens = input.split(",");

    if (tokens.length != 2)
    {
      throw new InputFileException("Bad coordinate string: " + input);
    }

    final int x = Integer.parseInt(tokens[0]);
    final int y = Integer.parseInt(tokens[1]);
    return new Coordinate(x, y);
  }

  /**
   * Visualise the grid of rocks and sand. Does not draw anything beyond the
   * leftmost and rightmost rock formations, but the floor is drawn (at 2
   * spaces below the bottom-most rock).
   * Returns a list of strings representing each line, for the caller to
   * print.
   */
  private static List<String> visualise(
      final List<Coordinate> rocks, final List<Coordinate> sand)
  {
    final int minX = getMinX(rocks);
    final int maxX = getMaxX(rocks);
    final int maxY = getMaxY(rocks);
    final int floor = maxY + 2;
    final List<String> printLines = new ArrayList<>();

    for (int y = 0; y <= floor; y++)
    {
      final StringBuilder line = new StringBuilder();

      for (int x = minX; x <= maxX; x++)
      {
        final Coordinate c = new Coordinate(x, y);

        if (c.y() == 0 && c.x() == 500)
        {
          //-------------------------------------------------------------------
          // Sand origin
          //-------------------------------------------------------------------
          line.append('+');
        }
        else if (rocks.contains(c) || y == floor)
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
    // Part 2 requires some optimisation. We add two "imaginary" walls, three
    // spaces left and right of our grid. This avoids having to iterate the
    // falling sand past these "walls". We can easily calculate the amount of
    // stacked sand either side afterwards - all we need to track is the
    // height of the pile next to the "walls", and track sand that falls "back
    // into the grid".
    //-------------------------------------------------------------------------
    final int leftWall = getMinX(allRocks) - 3;
    final int rightWall = getMaxX(allRocks) + 3;
    final int maxY = getMaxY(allRocks);
    final int floor = maxY + 2;
    final List<Coordinate> sandAtRest = new ArrayList<>();
    final Coordinate sandOrigin = new Coordinate(500, 0);
    Coordinate fallingSand = sandOrigin;

    //-------------------------------------------------------------------------
    // On each iteration, we drop the sand to the lowest possible height, and
    // then shift left or right if possible. If that sand cannot move any
    // further then we propagate a new unit from the origin.
    //
    // Iteration stops when sand has backed up to the origin.
    //-------------------------------------------------------------------------
    while (sandAtRest.isEmpty() ||
           !sandAtRest.get(sandAtRest.size() - 1).equals(sandOrigin))
    {
      final int currentX = fallingSand.x();
      final int currentY = fallingSand.y();
      final List<Coordinate> fullList = new ArrayList<>(allRocks);
      fullList.addAll(sandAtRest);

      final int depthOfObstacle = fullList.stream()
        .filter( c -> c.x() == currentX )
        .filter( c -> c.y() > currentY )
        .mapToInt(Coordinate::y)
        .min()
        .orElse(floor);

      //-----------------------------------------------------------------------
      // If the sand has any room to fall, propagate it down (so that it sits
      // above the obstacle).
      //-----------------------------------------------------------------------
      if (depthOfObstacle - 1 > currentY)
      {
        fallingSand = new Coordinate(currentX, depthOfObstacle - 1);
      }

      //-----------------------------------------------------------------------
      // Preferentially move diagonally left if possible, else diagonally
      // right if possible.
      //-----------------------------------------------------------------------
      final Coordinate leftShift =
        new Coordinate(fallingSand.x() - 1, fallingSand.y() + 1);

      final Coordinate rightShift =
        new Coordinate(fallingSand.x() + 1, fallingSand.y() + 1);

      final boolean moveLeft = (leftShift.x() > leftWall) &&
        (leftShift.y() < floor) &&
        fullList.stream().noneMatch( c -> c.equals(leftShift) );

      final boolean moveRight = !moveLeft && (rightShift.x() < rightWall) &&
        (leftShift.y() < floor) &&
        fullList.stream().noneMatch( c -> c.equals(rightShift) );

      if (moveLeft)
      {
        fallingSand = leftShift;
      }
      else if (moveRight)
      {
        fallingSand = rightShift;
      }
      else
      {
        //---------------------------------------------------------------------
        // Sand has come to rest. Add it to the list and reset.
        //---------------------------------------------------------------------
        sandAtRest.add(fallingSand);
        fallingSand = sandOrigin;
      }
    }

    output.println("\nAfter sand has fallen:");
    visualise(allRocks, sandAtRest).forEach(output::println);

    //-------------------------------------------------------------------------
    // The part 1 count "ends" with the first sand that leaves the "grid" -
    // that is, the first sand that hits the floor.
    //-------------------------------------------------------------------------
    final Coordinate firstSandOnFloor = sandAtRest.stream()
      .filter( c -> c.y() > maxY )
      .findFirst()
      .orElseThrow( () -> new LogicException("Unable to calculate part 1") );

    final int part1 = sandAtRest.indexOf(firstSandOnFloor);
    output.println("\nUnits of sand at rest (part 1): " + part1);

    //-------------------------------------------------------------------------
    // For the part 2 answer, we need to calculate the sand that would have
    // fallen to rest beyond the walls.
    //-------------------------------------------------------------------------
    final int leftSandPileHeight = floor - sandAtRest.stream()
      .filter( c -> c.x() == leftWall + 1 )
      .mapToInt(Coordinate::y)
      .min()
      .orElse(floor);

    final int leftSandTotal =
      IntStream.range(1, leftSandPileHeight).sum();

    final int rightSandPileHeight = floor - sandAtRest.stream()
      .filter( c -> c.x() == rightWall - 1 )
      .mapToInt(Coordinate::y)
      .min()
      .orElse(floor);

    final int rightSandTotal =
      IntStream.range(1, rightSandPileHeight).sum();

    final int part2 = sandAtRest.size() + leftSandTotal + rightSandTotal;
    output.println("Units of sand at rest (part 2): " + part2);
  }

  private static class RockStructure
  {
    public static RockStructure buildFromInputLine(final String input)
    {
      final String[] tokens = input.split(" -> ");
      return Stream.of(tokens).collect(
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
      final Coordinate endOfLine = coordinateFromInput(newCoordinate);

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
