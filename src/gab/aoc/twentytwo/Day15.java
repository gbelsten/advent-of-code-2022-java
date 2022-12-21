package gab.aoc.twentytwo;

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.IntStream;

import gab.aoc.util.Coordinate;
import gab.aoc.util.InputFileException;
import gab.aoc.util.LogicException;

public class Day15 extends DayTask
{


  @Override
  public void doTask(PrintStream output, boolean debug)
  {
    final List<String> inputLines = getFileLines();

    final TunnelLayout layout = inputLines.stream()
      .collect(
        TunnelLayout::new, TunnelLayout::processLine, TunnelLayout::combine);

    final int part1 = layout.getBeaconFreeSpacesForRow(2000000).size();
    output.println("Beacon free spaces at row: " + part1);

    final Coordinate distressBeacon = layout.findDistressBeacon();
    output.println("Found distress beacon at: " + distressBeacon);
    final long tuningFrequency = distressBeacon.x() * 4000000L + distressBeacon.y();
    output.println("Distress beacon tuning frequency: " + tuningFrequency);
  }

  private static class TunnelLayout
  {
    private static final String COORD_REGEX = "[xy]=(?<digit>\\-?\\d+)[:,]?";
    private static final Pattern COORD_PATTERN = Pattern.compile(COORD_REGEX);

    private static int parseCoordinateNumber(final String token)
    {
      final Matcher matcher = COORD_PATTERN.matcher(token);

      if (!matcher.matches())
      {
        throw new InputFileException("Bad coordinate token: " + token);
      }

      final String parsedNumber = matcher.group("digit");
      return Integer.parseInt(parsedNumber);
    }

    final Map<Coordinate, Integer> sensorBeaconDistances = new HashMap<>();
    final List<Coordinate> beacons = new ArrayList<>();

    public Map<Coordinate, Integer> getDistances()
    {
      return Collections.unmodifiableMap(this.sensorBeaconDistances);
    }

    public void processLine(final String input)
    {
      final String[] tokens = input.split(" ");

      if (tokens.length != 10)
      {
        throw new InputFileException("Bad input line: " + input);
      }

      //-----------------------------------------------------------------------
      // For each token containing a coordinate, we want to split out the
      // number, retaining any negative sign.
      //-----------------------------------------------------------------------
      final int xSensor = parseCoordinateNumber(tokens[2]);
      final int ySensor = parseCoordinateNumber(tokens[3]);
      final int xBeacon = parseCoordinateNumber(tokens[8]);
      final int yBeacon = parseCoordinateNumber(tokens[9]);

      final Coordinate sensorCoordinate = new Coordinate(xSensor, ySensor);
      final Coordinate beaconCoordinate = new Coordinate(xBeacon, yBeacon);

      final int distance =
        sensorCoordinate.manhattanDistanceTo(beaconCoordinate);

      this.sensorBeaconDistances.put(sensorCoordinate, distance);
      this.beacons.add(beaconCoordinate);
    }

    public TunnelLayout combine(final TunnelLayout layout)
    {
      this.sensorBeaconDistances.putAll(layout.getDistances());
      return this;
    }

    public Set<Coordinate> getBeaconFreeSpacesForRow(final int row)
    {
      final Set<Coordinate> beaconFreeSpaces = new HashSet<>();

      for (final Coordinate sensor : this.sensorBeaconDistances.keySet())
      {
        final int maxDistance = this.sensorBeaconDistances.get(sensor);
        final int rowDistance = Math.abs(row - sensor.y());

        if (rowDistance > maxDistance)
        {
          continue;
        }

        final int xMin = sensor.x() - (maxDistance - rowDistance);
        final int xMax = sensor.x() + (maxDistance - rowDistance);

        IntStream.rangeClosed(xMin, xMax)
          .mapToObj( x -> new Coordinate(x, row) )
          .filter( c -> !this.beacons.contains(c) )
          .forEach(beaconFreeSpaces::add);
      }

      return Collections.unmodifiableSet(beaconFreeSpaces);
    }

    public Coordinate findDistressBeacon()
    {
      return IntStream.rangeClosed(0, 4000000)
        .mapToObj(this::searchRowForDistressBeacon)
        .filter(Optional::isPresent)
        .map(Optional::get)
        .findFirst()
        .orElseThrow( () -> new LogicException("Failed to find beacon") );
    }

    public Optional<Coordinate> searchRowForDistressBeacon(final int row)
    {
      //-----------------------------------------------------------------------
      // Try to be clever about this. For each row, work out (and combine)
      // the ranges which are covered by each sensor. (This will include all
      // known beacons, so we can ignore those.
      //-----------------------------------------------------------------------
      List<Interval> intervalsOfEmptySpace = new ArrayList<>();

      for (final Coordinate sensor : this.sensorBeaconDistances.keySet())
      {
        final int maxDistance = this.sensorBeaconDistances.get(sensor);
        final int rowDistance = Math.abs(row - sensor.y());

        if (rowDistance > maxDistance)
        {
          continue;
        }

        final int xMin =
          Math.max(0, sensor.x() - (maxDistance - rowDistance));
        final int xMax =
          Math.min(4000000, sensor.x() + (maxDistance - rowDistance));

        Interval newInterval = new Interval(xMin, xMax);
        final List<Interval> updatedList = new ArrayList<>();

        for (final Interval interval : intervalsOfEmptySpace)
        {
          //-------------------------------------------------------------------
          // Merge in any overlapping intervals, otherwise copy them across to
          // the new list.
          //-------------------------------------------------------------------
          if (interval.overlaps(newInterval))
          {
            newInterval = interval.combine(newInterval);
          }
          else
          {
            updatedList.add(interval);
          }
        }

        updatedList.add(newInterval);
        intervalsOfEmptySpace = updatedList;
      }

      final List<Interval> finalIntervals = intervalsOfEmptySpace;

      if (finalIntervals.size() > 1 ||
          finalIntervals.get(0).start() > 0 ||
          finalIntervals.get(0).end() < 4000000)
      {
        //---------------------------------------------------------------------
        // We've found it! Work out where the beacon is.
        //---------------------------------------------------------------------
        return IntStream.range(0, 4000000)
          .filter( i -> !anyIntervalContains(finalIntervals, i) )
          .mapToObj( x -> new Coordinate(x, row) )
          .findFirst();
      }
      else
      {
        return Optional.empty();
      }
    }

    private boolean anyIntervalContains(
        final Collection<Interval> intervals, final int i)
    {
      return intervals.stream().anyMatch( interval -> interval.contains(i) );
    }
  }

  private static class Interval
  {
    private final int start;
    private final int end;

    public Interval(final int start, final int end)
    {
      this.start = start;
      this.end = end;
    }

    public int start() { return this.start; }
    public int end() { return this.end; }

    public boolean overlaps(final Interval interval)
    {
      return (this.contains(interval.start()) ||
              this.contains(interval.end()) ||
              interval.contains(this.start()) ||
              interval.contains(this.end()));
    }

    public Interval combine(final Interval interval)
    {
      final int newStart = Math.min(this.start(), interval.start());
      final int newEnd = Math.max(this.end(), interval.end());
      return new Interval(newStart, newEnd);
    }

    public boolean contains(final int i)
    {
      return (i >= this.start() && i <= this.end());
    }

    @Override
    public String toString()
    {
      return "start: " + this.start() + ", end: " + this.end();
    }
  }
}
