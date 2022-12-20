package gab.aoc.util;

public class InputFileException extends RuntimeException
{
  public InputFileException(final String message)
  {
    super(message);
  }

  public InputFileException(final String message, final Throwable cause)
  {
    super(message, cause);
  }
}
