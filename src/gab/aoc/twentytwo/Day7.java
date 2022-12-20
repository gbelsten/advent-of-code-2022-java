package gab.aoc.twentytwo;

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collector;
import java.util.stream.Collectors;

import gab.aoc.util.LogicException;

public class Day7 extends DayTask
{
  @Override
  public void doTask(final PrintStream output, final boolean debug)
  {
    final List<String> inputLines = getFileLines();

    final Collector<String, RootDirectory, RootDirectory> treeCollector =
      Collector.of(
        RootDirectory::newRoot,
        RootDirectory::processOutputLine,
        RootDirectory::combine,
        Collector.Characteristics.IDENTITY_FINISH
    );

    final RootDirectory tree = inputLines.subList(1, inputLines.size())
      .stream()
      .collect(treeCollector);

    final List<TreeItem> allItems = tree.flatten();

    final long sumOfAllDirectoriesUnder100k = allItems.stream()
      .filter(TreeItem::isDirectory)
      .mapToLong(TreeItem::getSize)
      .filter( size -> size < 100000L )
      .sum();

    output.println("Part 1: " + sumOfAllDirectoriesUnder100k);

    final long freeSpace = 70000000L - tree.getSize();
    final long spaceToFree = 30000000L - freeSpace;

    final long sizeOfItemToDelete = allItems.stream()
      .filter(TreeItem::isDirectory)
      .mapToLong(TreeItem::getSize)
      .sorted() // lowest to highest
      .filter( size -> size >= spaceToFree )
      .findFirst()
      .orElseThrow( () -> new LogicException("Failed to find for delete") );

    output.println("Part 2: " + sizeOfItemToDelete);
  }

  /**
   * A representation of an "item" in the input tree, which may be a plain
   * file with an individual size, or a directory containing further files
   * and/or directories.
   */
  public abstract static class TreeItem
  {
    private final String name;

    protected TreeItem(final String name)
    {
      this.name = name;
    }

    /**
     * @return Size of this item. For plain files, this is the size of this
     * individual item. For directories, this is the sum of the sizes of
     * everything that it contains.
     */
    public abstract long getSize();

    /**
     * @return true if this is a directory, false otherwise.
     */
    public abstract boolean isDirectory();

    /**
     * @return a flattened list containing this item plus, if this is a
     * directory, all directories and items nested under this directory.
     */
    public abstract List<TreeItem> flatten();

    /**
     * @return Name of this item
     */
    public final String getName() { return this.name; }

    /**
     * @return this item as a directory, or empty if this is not a directory.
     */
    public final Optional<Directory> getAsDirectory()
    {
      final Optional<Directory> output;

      if (this instanceof Directory)
      {
        output = Optional.of((Directory)this);
      }
      else
      {
        output = Optional.empty();
      }

      return output;
    }

    @Override
    public String toString()
    {
      final StringBuilder builder = new StringBuilder();
      builder.append(this.getName());
      builder.append(" [");
      builder.append(this.getSize());
      builder.append("]");
      return builder.toString();
    }
  }

  /**
   * A simple file.
   */
  public static class SimpleFile extends TreeItem
  {
    private final long size;

    /**
     * Constructor. Create a simple file of the given name and size.
     */
    public SimpleFile(final String name, final long size)
    {
      super(name);
      this.size = size;
    }

    @Override
    public final boolean isDirectory() { return false; }

    @Override
    public List<TreeItem> flatten() { return Arrays.asList(this); }

    @Override
    public long getSize() { return this.size; }
  }

  /**
   * A directory, possibly containing further items.
   */
  public static class Directory extends TreeItem
  {
    private final List<TreeItem> contents = new ArrayList<>();
    private final Optional<Directory> parent;

    /**
     * Constructor for directroy with no parent.
     */
    protected Directory(final String name)
    {
      super(name);
      this.parent = Optional.empty();
    }

    /**
     * Sub-directory constructor.
     */
    protected Directory(final String name, final Directory parent)
    {
      super(name);
      this.parent = Optional.of(parent);
    }

    /**
     * @return a new sub-directory with the given name
     */
    public Directory newSubDirectory(final String name)
    {
      return new Directory(name, this);
    }

    /**
     * @return the parent directory, or optional if none
     */
    public Optional<Directory> getParent() { return this.parent; }

    /**
     * @return a list of this directory's contents (may be empty)
     */
    public List<TreeItem> listContents() { return contents; }

    /**
     * Add item to this directory's contents
     */
    public void addItemToContents(final TreeItem item) { contents.add(item); }

    @Override
    public final boolean isDirectory() { return true; }

    @Override
    public long getSize()
    {
      final long sum = contents.stream()
        .map(TreeItem::getSize)
        .mapToLong(Long::valueOf)
        .sum();

      return sum;
    }

    @Override
    public List<TreeItem> flatten()
    {
      //-----------------------------------------------------------------------
      // This will recursively call through flatten until we've processed
      // every nested directory. We'll get a stream of lists, which we
      // combine.
      //-----------------------------------------------------------------------
      final List<TreeItem> items = this.contents.stream()
        .map(TreeItem::flatten)
        .flatMap(Collection::stream)
        .collect(Collectors.toList());

      //-----------------------------------------------------------------------
      // Make sure this directory is included!
      //-----------------------------------------------------------------------
      items.add(this);
      return items;
    }
  }

  /*
   * A root directory, with no parent. Includes logic required to be the
   * working object in the input collector, to build the file tree from the
   * task input.
   */
  public static class RootDirectory extends Directory
  {
    /**
     * @return a new root directory
     */
    public static RootDirectory newRoot() { return new RootDirectory(); }

    /**
     * The directory on which our collector is currently acting.
     */
    private Directory currentAccumulatorDir = this;

    /**
     * Create a new root directory.
     */
    private RootDirectory() { super("/"); }

    /**
     * @return The directory for the accumulator to act on
     */
    public Directory getCurrentAccumulatorDir()
    {
      return this.currentAccumulatorDir;
    }

    /**
     * Descend the accumulator directory into the directory with the given
     * name.
     */
    private void descend(final String name)
    {
      currentAccumulatorDir = currentAccumulatorDir.listContents().stream()
        .filter( dir -> dir.getName().equals(name) )
        .findFirst()
        .orElseThrow( () -> new LogicException("Bad dir name: " + name) )
        .getAsDirectory()
        .orElseThrow( () -> new LogicException("Not a directory: " + name) );
    }

    /**
     * Ascend the accumulator directory into its parent.
     */
    private void ascend()
    {
      currentAccumulatorDir = currentAccumulatorDir.getParent()
        .orElseThrow( () -> new LogicException("No parent directory") );
    }

    /**
     * Collector helper function. Process a line from the task input. This
     * will either be a command prompt (which will inform where we are in the
     * directory tree), or a file or directory to add to the tree.
     */
    public void processOutputLine(final String line)
    {
      final String[] tokens = line.split(" ");

      if (tokens[0].equals("$"))
      {
        processCommand(tokens);
      }
      else
      {
        processListOutput(tokens);
      }
    }

    /**
     * Process a command prompt line from the task input. If it's a 'cd'
     * command then we will ascend or descend the tree that we're processing,
     * as appropriate.
     */
    private void processCommand(final String[] tokens)
    {
      //-----------------------------------------------------------------------
      // We only care about 'cd', not about 'ls'.
      //-----------------------------------------------------------------------
      if (!tokens[1].equals("cd"))
      {
        return;
      }

      final String dirName = tokens[2];

      if (dirName.equals(".."))
      {
        this.ascend();
      }
      else
      {
        this.descend(dirName);
      }
    }

    /**
     * Process an output line, containing either a new file or a new
     * directory to add to the tree.
     */
    private void processListOutput(final String[] tokens)
    {
      final String name = tokens[1];
      final TreeItem newItem;

      if (tokens[0].equals("dir"))
      {
        newItem = currentAccumulatorDir.newSubDirectory(name);
      }
      else
      {
        final long size = Long.parseLong(tokens[0]);
        newItem = new SimpleFile(name, size);
      }

      currentAccumulatorDir.addItemToContents(newItem);
    }

    /**
     * Combiner for the collector. In theory, combines two RootDirectories.
     * In practice, should never be called as we don't run the stream in
     * parallel.
     */
    public RootDirectory combine(final RootDirectory dir)
    {
      dir.getAsDirectory()
        .orElseThrow( () -> new LogicException("Not a directory") )
        .listContents()
        .stream()
        .forEach(this::addItemToContents);

      return this;
    }
  }
}
