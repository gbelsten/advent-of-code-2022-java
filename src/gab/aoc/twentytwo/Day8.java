package gab.aoc.twentytwo;

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collector;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import gab.aoc.util.LogicException;

public class Day8 extends DayTask
{
  @Override
  public void doTask(final PrintStream output, final boolean debug)
  {
    final List<String> inputLines = getFileLines();

    final Collector<String, TreeCollector, List<Tree>> treeCollector =
      Collector.of(
        TreeCollector::new,
        TreeCollector::addNewRow,
        TreeCollector::combine,
        TreeCollector::getTrees
    );

    final List<Tree> trees = inputLines.stream().collect(treeCollector);
    trees.stream().forEach( tree -> tree.updateTreeVisibilityAndScore(trees) );

    final long numberOfVisibleTrees = trees.stream()
      .filter(Tree::isVisible)
      .count();

    output.println("Number of visible trees: " + numberOfVisibleTrees);

    final int highestScore = trees.stream()
      .mapToInt(Tree::score)
      .max()
      .orElseThrow( () -> new LogicException("Failed to get highest score"));

    output.println("Highest score: " + highestScore);
  }

  private static class Tree
  {
    private final int row;
    private final int column;
    private final int height;
    private final String stringDescription;
    private boolean visible = false;
    private int score = 1;

    private static List<Tree> filterList(
        final List<Tree> list, final Predicate<Tree> filter)
    {
      return list.stream().filter(filter).collect(Collectors.toList());
    }

    public Tree(final int row, final int column, final int height)
    {
      this.row = row;
      this.column = column;
      this.height = height;
      this.stringDescription = "x:" + row + ",y:" + column + ",h:" + height;
    }

    public int row() { return this.row; }
    public int column() { return this.column; }
    public int height() { return this.height; }
    public int score() { return this.score; }
    public boolean isVisible() { return this.visible; }

    public void updateTreeVisibilityAndScore(final List<Tree> allTrees)
    {
      final List<Tree> sameColumn = filterList(
        allTrees, tree -> tree.column() == this.column());

      final List<Tree> sameRow = filterList(
        allTrees, tree -> tree.row() == this.row());

      final List<Tree> above = filterList(
        sameColumn, tree -> tree.row() < this.row());

      final List<Tree> below = filterList(
        sameColumn, tree -> tree.row() > this.row());

      final List<Tree> left = filterList(
        sameRow, tree -> tree.column() < this.column());

      final List<Tree> right = filterList(
        sameRow, tree -> tree.column() > this.column());

      for (final List<Tree> list : Arrays.asList(above, below, left, right))
      {
        final List<Tree> equalOrTaller = list.stream()
          .filter( tree -> tree.height() >= this.height() )
          .collect(Collectors.toList());

        this.visible = this.visible || equalOrTaller.isEmpty();

        score *= equalOrTaller.stream()
          .mapToInt(this::getPositionDifference)
          .sorted()
          .findFirst()
          .orElse(list.size());
      }
    }

    @Override
    public final String toString() { return this.stringDescription; }

    private int getPositionDifference(final Tree tree)
    {
      return Math.abs(
        (tree.row() + tree.column()) - (this.row() + this.column()) );
    }
  }

  private static class TreeCollector
  {
    private final List<Tree> trees = new ArrayList<>();
    private int row = 0;

    public void addNewRow(final String line)
    {
      final String[] heightStrings = line.split("");

      final int[] heights = Stream.of(heightStrings)
        .mapToInt(Integer::parseInt)
        .toArray();

      IntStream.range(0, heights.length)
        .forEach( col -> trees.add(new Tree(this.row, col, heights[col])) );

      row++;
    }

    public TreeCollector combine(final TreeCollector secondCollector)
    {
      trees.addAll(secondCollector.getTrees());
      return this;
    }

    public List<Tree> getTrees() { return trees; }
  }
}
