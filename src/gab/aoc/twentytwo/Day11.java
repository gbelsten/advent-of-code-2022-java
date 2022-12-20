package gab.aoc.twentytwo;

import java.io.PrintStream;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import gab.aoc.util.InputFileException;
import gab.aoc.util.LogicException;

public class Day11 extends DayTask
{
  private static final Pattern MONKEY_REGEX = Pattern.compile(
    "Monkey (?<monkeyIndex>\\d+):\n" +
    "  Starting items: (?<startingItems>.+)\n" +
    "  Operation: new = (?<operation>.+)\n" +
    "  Test: divisible by (?<testDivisor>\\d+)\n" +
    "    If true: throw to monkey (?<onTrue>\\d+)\n" +
    "    If false: throw to monkey (?<onFalse>\\d+)"
  );

  private static void doRound(final List<Monkey> monkeys)
  {
    monkeys.stream().forEach( m -> m.doRound(monkeys));
  }

  private static long getMonkeyBusiness(final List<Monkey> monkeys)
  {
    return monkeys.stream()
      .map(Monkey::totalInspections)
      .sorted(Collections.reverseOrder())
      .limit(2)
      .mapToLong(Long::valueOf)
      .reduce(1, Math::multiplyExact);
  }

  @Override
  public void doTask(PrintStream output, boolean debug)
  {
    final List<String> inputLines = getFileLines();
    final String fullInput = String.join("\n", inputLines);
    final String[] monkeyInputs = fullInput.split("\n\n");

    final List<Monkey> monkeys = Stream.of(monkeyInputs)
      .map(Monkey::new)
      .collect(Collectors.toList());

    final long commonFactor = monkeys.stream()
      .mapToLong(Monkey::testDivisor)
      .reduce(1, (a, b) -> a * b);

    monkeys.stream().forEach( m -> m.setCommonFactor(commonFactor) );
    IntStream.range(0, 20).forEach( i -> doRound(monkeys));
    final long monkeyBusiness = getMonkeyBusiness(monkeys);

    output.println(
      "Amount of monkey business after 20 rounds: " + monkeyBusiness);

    monkeys.stream().forEach(Monkey::reset);
    monkeys.stream().forEach( m -> m.setWorryFactor(1) );
    IntStream.range(0, 10000).forEach( i -> doRound(monkeys));
    final long extremeMonkeyBusiness = getMonkeyBusiness(monkeys);

    output.println(
      "Amount of monkey business after 10,000 rounds while very worried: " +
      extremeMonkeyBusiness);
  }

  private static class Monkey
  {
    private final List<BigInteger> currentItems = new ArrayList<>();

    private final int index;
    private final int[] startingItems;
    private final String operation;
    private final int testDivisor;
    private final int onTrue;
    private final int onFalse;

    private long totalInspections = 0;
    private BigInteger worryFactor = BigInteger.valueOf(3);
    private BigInteger commonFactor = BigInteger.ONE;

    private static BigInteger getOperand(
        final String token, final BigInteger oldWorry)
    {
      return "old".equals(token) ? oldWorry : new BigInteger(token);
    }

    public Monkey(final String input)
    {
      final Matcher matcher = MONKEY_REGEX.matcher(input);

      if (!matcher.matches())
      {
        throw new InputFileException("Failed to parse section:\n" + input);
      }

      this.index = Integer.parseInt(matcher.group("monkeyIndex"));
      this.operation = matcher.group("operation");
      this.testDivisor = Integer.parseInt(matcher.group("testDivisor"));
      this.onTrue = Integer.parseInt(matcher.group("onTrue"));
      this.onFalse = Integer.parseInt(matcher.group("onFalse"));

      final String startingItemsString = matcher.group("startingItems");
      this.startingItems = Stream.of(startingItemsString.split(", "))
        .mapToInt(Integer::parseInt)
        .toArray();

      this.reset();
    }

    public int index() { return this.index; }
    public long testDivisor() { return this.testDivisor; }
    public long totalInspections() { return this.totalInspections; }

    public void reset()
    {
      this.currentItems.clear();
      totalInspections = 0;

      IntStream.of(this.startingItems)
        .mapToObj(BigInteger::valueOf)
        .forEach(this.currentItems::add);
    }

    public void setWorryFactor(final int worryFactor)
    {
      this.worryFactor = BigInteger.valueOf(worryFactor);
    }

    public void setCommonFactor(final long commonFactor)
    {
      this.commonFactor = BigInteger.valueOf(commonFactor);
    }

    public void doRound(final List<Monkey> allMonkeys)
    {
      currentItems.stream()
        .map(this::inspect)
        .forEach( item -> this.throwItem(item, allMonkeys) );

      currentItems.clear();
    }

    public void throwItem(final BigInteger item, final List<Monkey> allMonkeys)
    {
      final BigInteger remainder =
        item.remainder(BigInteger.valueOf(this.testDivisor));

      final boolean testResult = remainder.equals(BigInteger.ZERO);
      final int throwTo = testResult ? onTrue : onFalse;

      allMonkeys.stream()
        .filter( m -> m.index() == throwTo )
        .findFirst()
        .orElseThrow( () -> new LogicException("No monkey with i: " + throwTo))
        .throwItemToThisMonkey(item);
    }

    public void throwItemToThisMonkey(final BigInteger item)
    {
      this.currentItems.add(item);
    }

    private BigInteger inspect(final BigInteger startingWorry)
    {
      this.totalInspections++;
      final String[] operationTokens = this.operation.split(" ");

      if (operationTokens.length != 3)
      {
        throw new InputFileException("Bad operation: " + this.operation);
      }

      final String operator = operationTokens[1];
      final BigInteger firstNum =
        getOperand(operationTokens[0], startingWorry);
      final BigInteger secondNum =
        getOperand(operationTokens[2], startingWorry);
      final BigInteger operationOutput;

      switch (operator)
      {
        case "+": operationOutput = firstNum.add(secondNum); break;
        case "-": operationOutput = firstNum.subtract(secondNum); break;
        case "*": operationOutput = firstNum.multiply(secondNum); break;
        case "/": operationOutput = firstNum.divide(secondNum); break;
        default: throw new InputFileException("Bad operator: " + operator);
      }

      final BigInteger afterWorryFactor =
        operationOutput.divide(this.worryFactor);

      return afterWorryFactor.remainder(this.commonFactor);
    }
  }
}
