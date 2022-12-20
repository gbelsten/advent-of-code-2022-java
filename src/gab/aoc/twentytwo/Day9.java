package gab.aoc.twentytwo;

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import gab.aoc.util.InputFileException;
import gab.aoc.util.LogicException;

public class Day9 extends DayTask
{
  /**
   * Translate a line of input (e.g. "U 5") into a sequence of directions
   * (in this example, a list with 5 entries of Direction.UP).
   */
  private static List<Direction> getDirectionsFromLine(final String line)
  {
    final String[] tokens = line.split(" ");

    if (tokens.length != 2)
    {
      throw new InputFileException("Bad line: " + line);
    }

    final Direction dir = Direction.map(tokens[0]);
    final int count = Integer.parseInt(tokens[1]);
    final List<Direction> output = new ArrayList<>();

    IntStream.range(0, count).forEach( i -> output.add(dir) );
    return output;
  }

  @Override
  public void doTask(PrintStream output, boolean debug)
  {
    final List<String> inputLines = getFileLines();

    final List<Direction> flattenedDirectionList = inputLines.stream()
      .map(Day9::getDirectionsFromLine)
      .collect(ArrayList::new, ArrayList::addAll, ArrayList::addAll);

    final Rope rope = new Rope(2);
    flattenedDirectionList.stream().forEach(rope::moveHead);

    final long uniqueTailPositions = rope.getTailHistory().stream()
      .distinct()
      .count();

    output.println("Length 2 rope, unique tail pos: " + uniqueTailPositions);

    final Rope longRope = new Rope(10);
    flattenedDirectionList.stream().forEach(longRope::moveHead);

    final long uniqueLongTailPos = longRope.getTailHistory().stream()
      .distinct()
      .count();

    output.println("Length 10 rope, unique tail pos: " + uniqueLongTailPos);
  }

  /**
   * Enum representation of the 4 movement inputs.
   */
  private enum Direction
  {
    UP("U"),
    DOWN("D"),
    LEFT("L"),
    RIGHT("R"),
    ;

    public static Direction map(final String token)
    {
      return Stream.of(Direction.values())
        .filter( dir -> dir.token().equals(token) )
        .findFirst()
        .orElseThrow( () -> new InputFileException("Bad token: " + token) );
    }

    private final String token;

    private Direction(final String token)
    {
      this.token = token;
    }

    public String token() { return this.token; }
  }

  /**
   * Simple class representing an x,y position on the grid.
   */
  private static class Position
  {
    private final int x;
    private final int y;

    public Position(final int x, final int y)
    {
      this.x = x;
      this.y = y;
    }

    public int x() { return this.x; }
    public int y() { return this.y; }

    @Override
    public boolean equals(final Object obj)
    {
      if (obj == null)
      {
        return false;
      }

      if (!(obj instanceof Position))
      {
        return false;
      }

      final Position pos = (Position)obj;
      return ((this.x() == pos.x()) && (this.y() == pos.y()));
    }

    @Override
    public int hashCode()
    {
      return Arrays.hashCode(new int[] { this.x, this.y });
    }
  }

  /**
   * Class representing one of the rope segments. Encapsulates its position,
   * the segment that it follows (unless it's the head), and its position
   * history.
   */
  private static class RopeSegment
  {
    private Position position = new Position(0, 0);
    private List<Position> history = new ArrayList<>();
    private RopeSegment following = null;

    public RopeSegment()
    {
      this.history.add(this.position);
    }

    /**
     * Set the segment that this segment follows. Not applicable for the
     * head.
     */
    public void setFollowing(final RopeSegment following)
    {
      this.following = following;
    }

    /**
     * Get this segment's position.
     */
    public Position position() { return this.position; }

    /**
     * Update this segment's position.
     */
    public void update(final Position newPosition)
    {
      this.position = newPosition;
      this.history.add(newPosition);
    }

    /**
     * Update this segment's position based on the segment that it follows.
     * Not valid for the head (i.e. the leading segment).
     */
    public void updateFromFollowing()
    {
      if (this.following == null)
      {
        throw new LogicException("Called on head");
      }

      //-----------------------------------------------------------------------
      // We move if we're no longer adjacent to the segment that we're
      // following. Diagonally "connected" counts as adjacent.
      //-----------------------------------------------------------------------
      final int xOffset = this.following.position().x() - this.position().x();
      final int yOffset = this.following.position().y() - this.position().y();

      if (Math.abs(xOffset) > 1 || Math.abs(yOffset) > 1)
      {
        //---------------------------------------------------------------------
        // We're no longer next to the segment that we're following. We need
        // to move. If we're in line with the segment, we'll move straight,
        // otherwise we'll make a diagonal jump to be in line with it.
        //
        // Assuming that we're never out of sync by >2 in any direction, we
        // can calculate the right move by simply taking the signum of the
        // offsets - so each axis moves +1, 0, or -1 as appropriate.
        //---------------------------------------------------------------------
        final int xNew = this.position().x() + Integer.signum(xOffset);
        final int yNew = this.position().y() + Integer.signum(yOffset);
        this.update(new Position(xNew, yNew));
      }
    }

    /**
     * @return this segment's movement history, as an unmodifiable list
     */
    public List<Position> getHistory()
    {
      return Collections.unmodifiableList(this.history);
    }
  }

  /**
   * Class representing a rope with multiple segments.
   */
  private static class Rope
  {
    private final List<RopeSegment> segments = new ArrayList<>();

    /**
     * New rope containing a given number of segments (must be at least 2).
     */
    public Rope(final int length)
    {
      if (length < 2)
      {
        throw new LogicException("Bad rope length: " + length);
      }

      IntStream.range(0, length).forEach(this::addSegment);
    }

    /**
     * Constructor helper function. Add a new segment, with the given index.
     */
    private void addSegment(final int index)
    {
      final RopeSegment newSegment = new RopeSegment();
      segments.add(newSegment);

      if (index > 0)
      {
        final RopeSegment priorSegment = this.segments.get(index - 1);
        newSegment.setFollowing(priorSegment);
      }
    }

    /**
     * @return this rope's head segment
     */
    private RopeSegment head() { return this.segments.get(0); }

    /**
     * Move this rope's head in the given direction. The other segments will
     * be automatically updated as appropriate.
     */
    public void moveHead(final Direction dir)
    {
      int x = this.head().position().x();
      int y = this.head().position().y();

      switch (dir)
      {
        case UP: y++; break;
        case DOWN: y--; break;
        case LEFT: x--; break;
        case RIGHT: x++; break;
      }

      final Position updatedHeadPosition = new Position(x, y);
      this.head().update(updatedHeadPosition);

      this.segments.subList(1, this.segments.size()).stream()
        .forEach(RopeSegment::updateFromFollowing);
    }

    /**
     * @return the position history of this rope's tail segment
     */
    public List<Position> getTailHistory()
    {
      return this.segments.get(this.segments.size() - 1).getHistory();
    }
  }
}
