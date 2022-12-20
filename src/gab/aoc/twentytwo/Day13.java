package gab.aoc.twentytwo;

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import gab.aoc.util.InputFileException;

public class Day13 extends DayTask
{
  private static <T> void addItemToPairList(
      final List<Pair<T>> list, final T item)
  {
    final Pair<T> lastPair =
      (list.isEmpty()) ? null : list.get(list.size() - 1);

    if (lastPair != null && lastPair.getSecond() == null)
    {
      lastPair.setSecond(item);
    }
    else
    {
      final Pair<T> newPair = new Pair<>();
      newPair.setFirst(item);
      list.add(newPair);
    }
  }

  private static Optional<Boolean> packetsInRightOrder(
      final Pair<Packet> packetPair)
  {
    final List<Object> firstContents = packetPair.getFirst().getContents();
    final List<Object> secondContents = packetPair.getSecond().getContents();
    Optional<Boolean> result = compareLists(firstContents, secondContents);
    return result;
  }

  private static int packetComparator(
      final Packet firstPacket, final Packet secondPacket)
  {
    final Pair<Packet> pair = new Pair<>();
    pair.setFirst(firstPacket);
    pair.setSecond(secondPacket);
    final Optional<Boolean> result = packetsInRightOrder(pair);

    if (result.isPresent())
    {
      return result.get().booleanValue() ? -1 : 1;
    }
    else
    {
      return 0;
    }
  }

  private static Optional<Boolean> compareLists(
      final List<Object> first, final List<Object> second)
  {
    final int length = Math.max(first.size(), second.size());

    for (int i = 0; i < length; i++)
    {
      if (i >= first.size())
      {
        return Optional.of(Boolean.TRUE);
      }
      else if (i >= second.size())
      {
        return Optional.of(Boolean.FALSE);
      }
      else
      {
        Object firstElement = first.get(i);
        Object secondElement = second.get(i);

        final boolean firstIsList = (firstElement instanceof List<?>);
        final boolean secondIsList = (secondElement instanceof List<?>);

        if (firstIsList || secondIsList)
        {
          if (!firstIsList)
          {
            final List<Object> newList = new ArrayList<>();
            newList.add(firstElement);
            firstElement = newList;
          }
          else if (!secondIsList)
          {
            final List<Object> newList = new ArrayList<>();
            newList.add(secondElement);
            secondElement = newList;
          }

          final Optional<Boolean> result = compareLists(
            (List<Object>)firstElement, (List<Object>)secondElement);

          if (result.isPresent())
          {
            return result;
          }
        }
        else
        {
          Integer firstNumber = (Integer)firstElement;
          Integer secondNumber = (Integer)secondElement;

          if (firstNumber < secondNumber)
          {
            return Optional.of(Boolean.TRUE);
          }
          else if (firstNumber > secondNumber)
          {
            return Optional.of(Boolean.FALSE);
          }
        }
      }
    }

    return Optional.empty();
  }

  @Override
  public void doTask(PrintStream output, boolean debug)
  {
    final List<String> inputLines = getFileLines();

    final List<Packet> allPackets = inputLines.stream()
      .filter( str -> !str.isEmpty() )
      .map(Packet::new)
      .collect(Collectors.toList());

    final List<Pair<Packet>> pairsOfPackets = allPackets.stream()
      .collect(ArrayList::new, Day13::addItemToPairList, ArrayList::addAll);

    final Map<Integer, Boolean> packetInOrder = new HashMap<>();

    IntStream.range(0, pairsOfPackets.size())
      .forEach( i -> packetInOrder.put(i+1,
        packetsInRightOrder(pairsOfPackets.get(i)).orElse(true)) );

    final int sumOfIndicesInRightOrder = packetInOrder.keySet().stream()
      .filter(packetInOrder::get)
      .mapToInt(Integer::valueOf)
      .sum();

    output.println(
      "Sum of indices in right order: " + sumOfIndicesInRightOrder);

    final Packet firstDivider = new Packet("[[2]]");
    final Packet secondDivider = new Packet("[[6]]");
    allPackets.add(firstDivider);
    allPackets.add(secondDivider);

    final List<Packet> sortedPacketList = allPackets.stream()
      .sorted(Day13::packetComparator)
      .collect(Collectors.toList());

    final int firstDividerIndex = sortedPacketList.indexOf(firstDivider) + 1;
    final int secondDividerIndex = sortedPacketList.indexOf(secondDivider) + 1;
    output.println("Decoder key: " + firstDividerIndex * secondDividerIndex);
  }

  private static class Packet
  {
    private final List<Object> contents;
    private int inputScanIndex = 0;

    public Packet(final String line)
    {
      //-----------------------------------------------------------------------
      // Strip off the opening and closing braces, so we don't end up with an
      // extra nested list.
      //-----------------------------------------------------------------------
      if (line.charAt(0) != '[' || line.charAt(line.length() - 1) != ']')
      {
        throw new InputFileException("Bad line: " + line);
      }

      final String subLine = line.substring(1, line.length() - 1);
      this.contents = buildContents(subLine);
    }

    private List<Object> buildContents(final String input)
    {
      final List<Object> workingList = new ArrayList<>();
      StringBuilder intElementBuilder = new StringBuilder();

      while (inputScanIndex < input.length())
      {
        final char c = input.charAt(inputScanIndex++);

        if (c == ']')
        {
          break;
        }
        else if (c == '[')
        {
          final Object nestedList = buildContents(input);
          workingList.add(nestedList);
        }
        else if (Character.isDigit(c))
        {
          intElementBuilder.append(c);

          if (inputScanIndex >= input.length() ||
              !Character.isDigit(input.charAt(inputScanIndex)))
          {
            Integer i = Integer.parseInt(intElementBuilder.toString());
            workingList.add(i);
            intElementBuilder = new StringBuilder();
          }
        }
      }

      return workingList;
    }

    public List<Object> getContents()
    {
      return Collections.unmodifiableList(this.contents);
    }
  }

  private static class Pair<T>
  {
    private T first = null;
    private T second = null;

    public Pair() {}

    public void setFirst(final T first) { this.first = first; }
    public void setSecond(final T second) { this.second = second; }

    public T getFirst() { return this.first; }
    public T getSecond() { return this.second; }
  }
}
