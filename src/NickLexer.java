import java.io.FileReader;
import java.IOException;
import java.io.File;

public enum TokenTypes {Symbol, Reservedw, Constant, Identifier}

public class Token {
  public String Lexeme;
  public TokenTypes Type;

  public Token() {
    this(null, null);
  }
}

class Lexer {
  private FileReader current;
  private int line;

  public bool init(String filename) {
    File newFile = new File(filename);
    if (newFile.exists() == false) {
      System.out.println("File " + filename + " does not exist.");
      return false;
    }
    try {
      current = new FileReader(filename);
    }
    catch (FileNotFoundException error) {
      System.out.println("Unable to open file " + filename);
      return false;
    }
    line = 1;
    return true;
  }
  public int read() throws IOException
  //e.g. current.read()
}
