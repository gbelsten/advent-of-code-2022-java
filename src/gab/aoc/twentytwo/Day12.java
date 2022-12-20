package gab.aoc.twentytwo;

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import gab.aoc.util.InputFileException;
import gab.aoc.util.LogicException;

public class Day12 extends DayTask
{
  private static List<Tile> getTilesForLine(final int row, final String line)
  {
    final String[] tileStrings = line.split("");
    return IntStream.range(0, tileStrings.length)
      .mapToObj( i -> new Tile(i, row, tileStrings[i]))
      .collect(Collectors.toList());
  }

  private static Map<Tile, Route> getRoutesFromPoint(final Tile start)
  {
    final Map<Tile, Route> routeMap = new HashMap<>();
    final List<Route> routesToIterate = new ArrayList<>();
    routesToIterate.add(new Route(start));

    while (!routesToIterate.isEmpty())
    {
      final List<Route> iteratedRoutes = routesToIterate.stream()
        .map(Route::iterate)
        .collect(ArrayList::new, ArrayList::addAll, ArrayList::addAll);

      routesToIterate.clear();

      for (final Route route : iteratedRoutes)
      {
        final Tile routeHead = route.getCurrentHead();
        final Route currentShortest = routeMap.get(routeHead);

        if (currentShortest == null || route.steps() < currentShortest.steps())
        {
          routeMap.put(routeHead, route);
          routesToIterate.add(route);
        }
      }
    }

    return routeMap;
  }

  @Override
  public void doTask(PrintStream output, boolean debug)
  {
    final List<String> inputLines = getFileLines();

    final List<Tile> allTiles = IntStream.range(0, inputLines.size())
      .mapToObj( i -> getTilesForLine(i, inputLines.get(i)) )
      .collect(ArrayList::new, ArrayList::addAll, ArrayList::addAll);

    allTiles.forEach( tile -> tile.calculateConnections(allTiles) );

    final Tile startingTile = allTiles.stream()
      .filter(Tile::isStart)
      .findFirst()
      .orElseThrow( () -> new InputFileException("Failed to find start") );

    final Tile endingTile = allTiles.stream()
      .filter(Tile::isEnd)
      .findFirst()
      .orElseThrow( () -> new InputFileException("Failed to find end") );

    final Map<Tile, Route> routesFromStart = getRoutesFromPoint(startingTile);
    final Route shortestRouteToTop = routesFromStart.get(endingTile);
    output.println("Steps to reach top: " + shortestRouteToTop.steps());

    final Route shortestScenicRoute = allTiles.stream()
      .filter( t -> (t.height() == 0) )
      .map(Day12::getRoutesFromPoint)
      .map( m -> m.get(endingTile) )
      .filter(Objects::nonNull)
      .min( (r1, r2) -> (r1.steps() - r2.steps()) )
      .orElseThrow( () -> new LogicException("Failed to find scenic route") );

    output.println("Steps for scenic route: " + shortestScenicRoute.steps());
  }

  private static class Tile
  {
    private final int xPos;
    private final int yPos;
    private final int height;
    private final boolean start;
    private final boolean end;
    private final List<Tile> connectedTiles = new ArrayList<>();

    private static int charToHeight(final String elevation)
    {
      final char elevationChar = elevation.charAt(0);
      return elevationChar - 97;
    }

    public Tile(final int x, final int y, final String elevation)
    {
      this.xPos = x;
      this.yPos = y;

      if ("S".equals(elevation))
      {
        start = true;
        end = false;
        height = charToHeight("a");
      }
      else if ("E".equals(elevation))
      {
        start = false;
        end = true;
        height = charToHeight("z");
      }
      else
      {
        start = false;
        end = false;
        height = charToHeight(elevation);
      }
    }

    public void calculateConnections(final List<Tile> allTiles)
    {
      allTiles.stream()
        .filter(this::isAdjacent)
        .filter( t -> (t.height() - this.height() <= 1) )
        .forEach(this.connectedTiles::add);
    }

    public int x() { return this.xPos; }
    public int y() { return this.yPos; }
    public int height() { return this.height; }
    public boolean isStart() { return this.start; }
    public boolean isEnd() { return this.end; }
    public List<Tile> getConnections() { return this.connectedTiles; }

    @Override
    public String toString()
    {
      return
        "(x: " + this.x() + ", y: " + this.y() + ", h: " + this.height() + ")";
    }

    private boolean isAdjacent(final Tile otherTile)
    {
      final int xDif = Math.abs(otherTile.x() - this.x());
      final int yDif = Math.abs(otherTile.y() - this.y());
      return (xDif + yDif == 1);
    }
  }

  private static class Route
  {
    private final List<Tile> path;
    private boolean complete = false;

    public Route(final Tile startingTile)
    {
      this.path = new ArrayList<>();
      this.path.add(startingTile);
    }

    private Route(final Route existingRoute, final Tile nextStep)
    {
      this.path = new ArrayList<>(existingRoute.getPath());
      this.path.add(nextStep);
    }

    public List<Route> iterate()
    {
      if (this.complete)
      {
        return new ArrayList<>();
      }

      final Tile currentEndpoint = this.path.get(this.path.size() - 1);
      final List<Route> nextSteps = currentEndpoint.getConnections().stream()
        .filter( t -> (!path.contains(t)) )
        .map( t -> new Route(this, t) )
        .collect(Collectors.toList());

      if (nextSteps.isEmpty())
      {
        this.complete = true;
      }

      return nextSteps;
    }

    public List<Tile> getPath()
    {
      return Collections.unmodifiableList(this.path);
    }

    public Tile getCurrentHead()
    {
      return this.path.get(this.path.size() - 1);
    }

    public int steps() { return this.path.size() - 1; }

    @Override
    public String toString()
    {
      return String.join(", ", this.path.stream().map(Tile::toString).collect(Collectors.toList()));
    }
  }
}
