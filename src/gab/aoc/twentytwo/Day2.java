package gab.aoc.twentytwo;

import java.io.PrintStream;
import java.util.Arrays;
import java.util.Collections;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Stream;

/**
 * A perhaps verbose solution which aims to be readable and avoid
 * duplication rather than solving the task in as few lines as possible.
 */
public class Day2 extends DayTask
{
  /**
   * Validate that the input line contains two tokens, and split that into
   * a two-element list.
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
    return mine.playAgainst(opponent);
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
      case LOSE: mine = opponent.getLosingPlay(); break;
      case DRAW: mine = opponent; break;
      case WIN: mine = opponent.getWinningPlay(); break;
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
   * Enum representing the possible plays and their base scores.
   */
  private enum Play
  {
    ROCK(1, "A", "X"),
    PAPER(2, "B", "Y"),
    SCISSORS(3, "C", "Z"),
    ;

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

    /**
     * The base score granted for using this play.
     */
    final int baseScore;

    /**
     * The labels that represent this play.
     */
    final List<String> labels;

    /**
     * Private enum constructor
     */
    private Play(final int baseScore, final String... labels)
    {
      this.baseScore = baseScore;
      this.labels = Arrays.asList(labels);
    }

    /**
     * Play this action against an opponent's action, and return the
     * resulting total score.
     */
    public int playAgainst(final Play opponent)
    {
      final Outcome outcome = Outcome.getOutcome(this, opponent);
      return this.baseScore + outcome.getScore();
    }

    /**
     * Get the action that will win against this play.
     */
    public Play getWinningPlay()
    {
      return findPlayForOutcome(Outcome.LOSE);
    }

    /**
     * Get the action that will lose against this play.
     */
    public Play getLosingPlay()
    {
      return findPlayForOutcome(Outcome.WIN);
    }

    /**
     * Find the play that will produce the desired outcome.
     */
    private Play findPlayForOutcome(final Outcome outcome)
    {
      final Map<Play, Outcome> outcomes = Outcome.getOutcomesFor(this);
      return outcomes.keySet().stream()
        .filter( play -> outcomes.get(play).equals(outcome) )
        .findFirst()
        .orElseThrow( () -> new LogicException("Bad outcome" ) );
    }
  }

  /**
   * Outcomes of a match, and the score from each match.
   */
  private enum Outcome
  {
    LOSE(0),
    DRAW(3),
    WIN(6),
    ;

    /**
     * Mapping plays by the player and plays by the opponent to the outcome.
     */
    private static final Map<Play, Map<Play, Outcome>> outcomes;

    static
    {
      final Map<Play, Outcome> rockOutcomes = new EnumMap<>(Play.class);
      rockOutcomes.put(Play.ROCK, DRAW);
      rockOutcomes.put(Play.PAPER, LOSE);
      rockOutcomes.put(Play.SCISSORS, WIN);

      final Map<Play, Outcome> paperOutcomes = new EnumMap<>(Play.class);
      paperOutcomes.put(Play.ROCK, WIN);
      paperOutcomes.put(Play.PAPER, DRAW);
      paperOutcomes.put(Play.SCISSORS, LOSE);

      final Map<Play, Outcome> scissorsOutcomes = new EnumMap<>(Play.class);
      scissorsOutcomes.put(Play.ROCK, LOSE);
      scissorsOutcomes.put(Play.PAPER, WIN);
      scissorsOutcomes.put(Play.SCISSORS, DRAW);

      final Map<Play, Map<Play, Outcome>> fullMap = new EnumMap<>(Play.class);
      fullMap.put(Play.ROCK, Collections.unmodifiableMap(rockOutcomes));
      fullMap.put(Play.PAPER, Collections.unmodifiableMap(paperOutcomes));
      fullMap.put(
        Play.SCISSORS, Collections.unmodifiableMap(scissorsOutcomes));

      outcomes = Collections.unmodifiableMap(fullMap);
    }

    /**
     * @return the outcome mapping for a given play.
     */
    public static Map<Play, Outcome> getOutcomesFor(final Play play)
    {
      return outcomes.get(play);
    }

    /**
     * @return the outcome of a match of our play against an opponent's play.
     */
    public static Outcome getOutcome(final Play ours, final Play opponent)
    {
      return outcomes.get(ours).get(opponent);
    }

    /**
     * Score given for this outcome.
     */
    private final int score;

    /**
     * Private enum constructor
     */
    private Outcome(final int score)
    {
      this.score = score;
    }

    /**
     * @return the score for this outcome
     */
    public int getScore()
    {
      return this.score;
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

    /**
     * The label representing this strategy.
     */
    final String label;

    private Strategy(final String label)
    {
      this.label = label;
    }
  }
}
