package gab.aoc.twentytwo;

public class LogicException extends RuntimeException
{
  public LogicException(final String message)
  {
    super(message);
  }

  public LogicException(final String message, final Throwable cause)
  {
    super(message, cause);
  }
}
