package gab.aoc.twentytwo;

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import gab.aoc.util.InputFileException;
import gab.aoc.util.LogicException;

public class Day16 extends DayTask
{

  @Override
  public void doTask(PrintStream output, boolean debug)
  {
    final List<String> inputLines = getFileLines();
    final TunnelMap tunnelMap = TunnelMap.build(inputLines);
    final Node startingNode = tunnelMap.getNode("AA");
    List<Route> routes = new ArrayList<>();
    routes.add(new Route(startingNode));

    for (int i = 1; i < 31; i++)
    {
      output.println("Iteration " + i);
      List<Route> updatedRoutes = new ArrayList<>();

      routes.stream()
        .map( route -> route.getNextRoutes(tunnelMap) )
        .forEach(updatedRoutes::addAll);

      routes = updatedRoutes;
    }

    final int highestPossiblePressureRelease = routes.stream()
      .mapToInt(Route::pressureReleased)
      .max()
      .orElseThrow( () -> new LogicException("Failed to get pressure") );

    output.println(
      "Highest possible pressure release: " + highestPossiblePressureRelease);
  }

  private static class TunnelMap
  {
    private static final String FLOW_REGEX = "rate=(?<flow>\\d+);";
    private static final Pattern FLOW_PATTERN = Pattern.compile(FLOW_REGEX);

    public static TunnelMap build(final List<String> input)
    {
      final TunnelMap map = new TunnelMap();
      input.stream().forEach(map::processLine);
      return map;
    }

    private final List<Node> nodes = new ArrayList<>();

    public Node getNode(final String label)
    {
      return this.nodes.stream()
        .filter( node -> node.label().equals(label) )
        .findFirst()
        .orElseThrow( () -> new LogicException("No node " + label) );
    }

    private void processLine(final String line)
    {
      final String[] tokens = line.split(" ");

      if (tokens.length < 10)
      {
        throw new InputFileException("Bad input line: " + line);
      }

      final Matcher flowMatcher = FLOW_PATTERN.matcher(tokens[4]);

      if (!flowMatcher.matches())
      {
        throw new InputFileException("Failed to get flow: " + line);
      }

      final String nodeLabel = tokens[1];
      final int flowRate = Integer.parseInt(flowMatcher.group("flow"));

      final List<String> connections = IntStream.range(9, tokens.length)
        .mapToObj( i -> tokens[i] )
        .map( label -> label.replace(",", "") )
        .collect(Collectors.toList());

      final Node newNode = new Node(nodeLabel, flowRate, connections);
      this.nodes.add(newNode);
    }
  }

  private static class Node
  {
    private final String label;
    private final int flowRate;
    private final List<String> connections;

    public Node(
        final String label, final int flowRate, final List<String> connections)
    {
      this.label = label;
      this.flowRate = flowRate;
      this.connections = connections;
    }

    public String label() { return this.label; }
    public int flowRate() { return this.flowRate; }

    public List<String> connections()
    {
      return Collections.unmodifiableList(this.connections);
    }
  }

  private static class Route
  {
    final List<Node> nodeRoute = new ArrayList<>();
    final List<Node> valvesOpened = new ArrayList<>();
    private int length = 0;
    private int pressureReleased = 0;

    public static Route startingAt(final Node startingNode)
    {
      return new Route(startingNode);
    }

    private Route(final Node startingNode)
    {
      this.nodeRoute.add(startingNode);
    }

    private Route(final Route original)
    {
      this.nodeRoute.addAll(original.nodeRoute);
      this.valvesOpened.addAll(original.valvesOpened);
      this.length = original.length;
      this.pressureReleased = original.pressureReleased;
    }

    public int routeLength() { return this.length; }
    public int pressureReleased() { return this.pressureReleased; }
    public Node currentNode() { return nodeRoute.get(nodeRoute.size() - 1); }

    public List<Route> getNextRoutes(final TunnelMap tunnelMap)
    {
      final List<Route> nextRoutes = new ArrayList<>();
      final Node currentNode = this.currentNode();

      if (!valvesOpened.contains(currentNode) && currentNode.flowRate() > 0)
      {
        final Route openValveRoute = new Route(this);
        openValveRoute.openCurrentValve();
        nextRoutes.add(openValveRoute);
      }

      for (final String nodeLabel : currentNode.connections())
      {
        final Node nextNode = tunnelMap.getNode(nodeLabel);
        final Route nextStep = new Route(this);
        nextStep.moveTo(nextNode);
        nextRoutes.add(nextStep);
      }

      nextRoutes.stream().forEach(Route::updateLengthAndPressure);
      return nextRoutes;
    }

    private void openCurrentValve()
    {
      this.valvesOpened.add(this.currentNode());
    }

    private void moveTo(final Node destinationNode)
    {
      this.nodeRoute.add(destinationNode);
    }

    private void updateLengthAndPressure()
    {
      length++;
      valvesOpened.stream()
        .forEach( node -> this.pressureReleased += node.flowRate() );
    }
  }
}
