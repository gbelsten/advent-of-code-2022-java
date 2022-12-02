package gab.aoc.twentytwo;

public class TaskException extends Exception
{
  public TaskException(final String message)
  {
    super(message);
  }

  public TaskException(final String message, final Throwable cause)
  {
    super(message, cause);
  }
}
