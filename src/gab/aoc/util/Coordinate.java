package gab.aoc.util;

import java.util.Arrays;

public class Coordinate
{
  private final int x;
  private final int y;

  public Coordinate(final int x, final int y)
  {
    this.x = x;
    this.y = y;
  }

  public int x() { return this.x; }
  public int y() { return this.y; }

  public int manhattanDistanceTo(final Coordinate c)
  {
    return Math.abs(this.x() - c.x()) + Math.abs(this.y() - c.y());
  }

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
