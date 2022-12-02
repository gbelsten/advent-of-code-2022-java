package gab.aoc.twentytwo;

import java.io.PrintStream;

class RunTask
{
  public static void main(final String[] args) throws TaskException
  {
    if (args == null || args.length < 1 || args.length > 2)
    {
      printUsage(System.err);
      throw new IllegalArgumentException("Bad arguments passed");
    }

    if (args.length == 1 &&
        (args[0].equals("-h") || args[0].equals("--help")))
    {
      printUsage(System.out);
      System.exit(0);
    }

    final int taskDay = Integer.parseInt(args[0]);
    final boolean includeDebug;

    if (args.length == 2)
    {
      final String debugArg = args[1];
      includeDebug = (debugArg.equals("-d") || debugArg.equals("--debug"));
    }
    else
    {
      includeDebug = false;
    }

    try
    {
      final Class taskClass = Class.forName("gab.aoc.twentytwo.Day" + taskDay);
      final DayTask task = (DayTask)taskClass.newInstance();
      task.doTask(System.out, includeDebug);
      System.exit(0);
    }
    catch (final Exception e)
    {
      throw new TaskException("Failed to run task", e);
    }
  }

  private static void printUsage(final PrintStream output)
  {
    output.println("RunTask numberofday [-d|--debug]");
  }
}
