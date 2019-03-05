import java.io.BufferedReader;
import java.io.FileReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;


class NickLexer {
  private BufferedReader current;
  private char[] array;
  private int lineNumber;

  public static enum TokenTypes {
    Symbol, Reservedw, Constant, Identifier;
  }
  public static class Token {
    public String Lexeme;
    public TokenTypes Type;
    public int line;
    public int column;

    public Token() {
      Lexeme = null;
      Type = null;
    }
  }

  public boolean init(String filename) {
    File newFile = new File(filename);
    if (newFile.exists() == false) {
      System.out.println("File " + filename + " does not exist.");
      return false;
    }
    try {
      current = new BufferedReader(new FileReader(filename));
    }
    catch (FileNotFoundException error) {
      System.out.println("Unable to open file " + filename);
      return false;
    }
    lineNumber = 1;
    return true;
  }

  public void readFile(int offset, int length) {
    try {
      current.read(array, offset, length);
    } catch (IOException error) {
      System.out.println("Failed to read!");
    }
//    lineNumber++;
  }

  public void closeFile() {
    try {
      current.close();
    }
    catch (IOException error) {
      System.out.println(error.getMessage());
    }
  }

  public Token NextToken()
  {
    char start;
    Token word = new Token();
    long skipped = 0;

    try { for( char i : array ) {
      while (Character.isWhitespace(i)) {
        skipped = skipped + current.skip(1); //skip the character of white space
      }
      start = i;
      if (Character.isLetter(start) == true) {
        word.Type = TokenTypes.Reservedw;
        System.out.println("This is Reservedw");
      }
    }
    } catch (IOException error) {
      System.out.println(error.getMessage());
    }
    return word;
  }

  public static void main(String[] args) {
    char[] myArray;
    myArray = new char[100];
    int i;

    System.out.println("Hello Java");
    NickLexer myLexer = new NickLexer();
    boolean judge = myLexer.init("samplecode.cs");
    System.out.println(judge);
    myLexer.readFile(0, 100);
    // myLexer.closeFile();
    Token token = myLexer.NextToken();
    System.out.println(token.Type);
    for(i = 0; i < 100; i++) {
      System.out.println(myArray[i]);
    }
  }
}
