package gab.aoc.twentytwo;

import java.io.PrintStream;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Stream;

public class Day2 extends DayTask
{
  /**
   * Split line into a list of two labels.
   */
  private static List<String> splitInputLine(final String line)
  {
    final String[] labels = line.split(" ");

    if (labels.length != 2)
    {
      throw new InputFileException("Bad input line: " + line);
    }

    return Arrays.asList(labels);
  }

  /**
   * Get the round score for the given input line, assuming that both labels
   * represent plays (i.e. rock/paper/scissors).
   */
  private static int scoreRound(final String line)
  {
    final List<String> labels = splitInputLine(line);
    final Play opponent = Play.get(labels.get(0));
    final Play mine = Play.get(labels.get(1));
    return scoreRound(opponent, mine);
  }

  /**
   * Get the round score for the given plays.
   */
  private static int scoreRound(
    final Play opponent, final Play mine)
  {
    return mine.baseScore() + mine.playAgainst(opponent);
  }

  /**
   * Get the round score for the given input line, assuming that the first
   * label represents the opponent's play and the second label represents our
   * required strategy (win/draw/lose).
   */
  private static int scoreStrategy(final String line)
  {
    final List<String> labels = splitInputLine(line);
    final Play opponent = Play.get(labels.get(0));
    final Strategy strategy = Strategy.get(labels.get(1));
    final Play mine;

    switch (strategy)
    {
      case LOSE: mine = opponent.getPlayThatThisBeats(); break;
      case DRAW: mine = opponent; break;
      case WIN: mine = opponent.getPlayThatBeatsThis(); break;
      default: throw new LogicException("Bad strategy");
    }

    final int score = scoreRound(opponent, mine);
    return score;
  }

  @Override
  public void doTask(final PrintStream output, final boolean debug)
    throws TaskException
  {
    final List<String> inputSequence = getFileLines();
    final int scoreSum = inputSequence.stream()
      .map(Day2::scoreRound)
      .mapToInt(Integer::valueOf)
      .sum();

    output.println("Part 1: Score sum: " + scoreSum);

    final int strategyScore = inputSequence.stream()
      .map(Day2::scoreStrategy)
      .mapToInt(Integer::valueOf)
      .sum();

    output.println("Part 2: Score sum: " + strategyScore);
  }

  /**
   * Enum representing the possible plays and the labels that represent them.
   */
  private enum Play
  {
    ROCK(1, "A", "X"),
    PAPER(2, "B", "Y"),
    SCISSORS(3, "C", "Z"),
    ;

    private static final Map<Play, Play> playBeatsPlay;
    private static final Map<Play, Play> playBeatenByPlay;

    static
    {
      final Map<Play, Play> beats = new HashMap<>();
      beats.put(ROCK, SCISSORS);
      beats.put(PAPER, ROCK);
      beats.put(SCISSORS, PAPER);
      playBeatsPlay = Collections.unmodifiableMap(beats);

      final Map<Play, Play> beatenBy = new HashMap<>();
      beatenBy.put(ROCK, PAPER);
      beatenBy.put(PAPER, SCISSORS);
      beatenBy.put(SCISSORS, ROCK);
      playBeatenByPlay = Collections.unmodifiableMap(beatenBy);
    }

    /**
     * Get the Play representing the given input label.
     */
    public static Play get(final String input)
    {
      final Play opponent = Stream.of(Play.values())
        .filter( play -> play.labels.contains(input) )
        .findFirst()
        .orElseThrow(
          () -> new InputFileException("Bad input label: " + input) );

      return opponent;
    }

    final int baseScore;
    final List<String> labels;

    private Play(final int baseScore, final String... labels)
    {
      this.baseScore = baseScore;
      this.labels = Arrays.asList(labels);
    }

    /**
     * Get the base score for using this play, before scoring on
     * loss/draw/win.
     */
    public int baseScore()
    {
      return this.baseScore;
    }

    /**
     * Play this action against an opponent's action, and return the
     * resulting score (not including the base score).
     */
    public int playAgainst(final Play opponent)
    {
      final int score;

      if (Objects.equals(this, opponent.getPlayThatThisBeats()))
      {
        score = 0;
      }
      else if (Objects.equals(this, opponent))
      {
        score = 3;
      }
      else if (Objects.equals(this, opponent.getPlayThatBeatsThis()))
      {
        score = 6;
      }
      else
      {
        throw new LogicException("Logic error");
      }

      return score;
    }

    /**
     * Get the action that beats this one.
     */
    public Play getPlayThatBeatsThis()
    {
      return playBeatenByPlay.get(this);
    }

    /**
     * Get the action that this beats.
     */
    public Play getPlayThatThisBeats()
    {
      return playBeatsPlay.get(this);
    }
  }

  /**
   * Enum representing the lose/draw/win strategies and the labels that
   * represent them.
   */
  private enum Strategy
  {
    LOSE("X"),
    DRAW("Y"),
    WIN("Z"),
    ;

    /**
     * Get the strategy that represents the given input label.
     */
    public static Strategy get(final String input)
    {
      final Strategy result = Stream.of(Strategy.values())
        .filter( strategy -> Objects.equals(input, strategy.label) )
        .findFirst()
        .orElseThrow(
          () -> new InputFileException("Bad input label: " + input) );

      return result;
    }

    final String label;

    private Strategy(final String label)
    {
      this.label = label;
    }
  }
}
