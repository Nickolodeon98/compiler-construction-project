/* NickLexer class that reads jack source file and identifies each tokens as
 * one of several types: symbol, reserved word, constant and identifier.
 */
/*TODO:One particular type of error to watch for is the unexpected end of
file while scanning a string literal, or a multi-line comment.
*/

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.List;
import java.util.Arrays;
import java.util.ArrayList;
import java.lang.*;

class NickLexer {
  private BufferedReader current;
  private char curElement;
  private char preElement;
  private int pointer;
  private int prePointer;
  private int wordPointer;
  private int lineNumber;
  private char[] array = new char[10000];
  private int length = 10000;
  private String filePath = "Set 1/List/";

  public static enum TokenTypes {
    Symbol, Reservedw, Constant, StringConstant, IntegerConstant, Identifier, EOF;
  }
  // class Token which has a number of state variables that define a token.
  public static class Token {
    public StringBuilder Lexeme = new StringBuilder();
    public TokenTypes Type;
    public int line; // ??
    public int column; // ??
  }

  // init method which creates a scanner to read a file with a given filename
  public boolean init(String filename) {
    File newFile = new File(filePath + filename);
    if (newFile.exists() == false) {
      System.out.println("File " + filename + " does not exist.");
      return false;
    }
    try {
      current = new BufferedReader(new FileReader(filePath + filename));
    }
    catch (FileNotFoundException error) {
      System.out.println("Unable to open file " + filename);
      return false;
    }
    lineNumber = 1;
    return true;
  }

  // readFile method which reads a file into a character array
  public void readFile() {
    try {
      current.read(array, 0, length);
    } catch (IOException error) {
      System.out.println("Failed to read!");
    } catch (NullPointerException error) {
      System.out.println("File is not found.");
    }
    // TODO: increment the line number every time a line of file is processed.
  }
  // closeFile method which closes the file after finish reading
  public void closeFile() {
    try {
      current.close();
    } catch (IOException error) {
      System.out.println(error.getMessage());
    } catch (NullPointerException error) {
      System.out.println("File is not found.");
    }
  }

  /*
   * GetNextToken method which reads the next token then figures out the type of that token.
   * The whitespaces and comments are skipped. The token read is consumed and marked as processed.
   */
  public Token GetNextToken() {
    Token word = new Token();
    int i;
    int n;
    int z;
    int count = 0;
    char start = 0;
    char element;
    boolean EOF;

    word.line = lineNumber;
    for (z=0; z< length; z++) {
      if(array[z] != '\u0000') count++;
    }

    // when GetNextToken() function is called
    if (curElement == 0) {
      preElement = curElement;
      curElement = array[0];//peeknexttoken array(lexeme.tostring.charat(0))
      prePointer = pointer;
      pointer = 0;
      if (prePointer > pointer) {
        curElement = preElement; //getnexttoken
        pointer = prePointer;
        EOF = true;
        word.Type = TokenTypes.EOF;
        return word;
      }
      EOF = false;
    } else {
      EOF = false;
    }

    // if the end of file has not been reached yet
    if (!EOF) {
      for (n = pointer; n < array.length; n++) {
        element = array[n]; // assign the element at the position of element currently being processed to 'element'
        pointer = n;
        if (Character.isWhitespace(element) || element == '\n') { // skip white spaces
          while (Character.isWhitespace(element) || element == '\n') {
            // System.out.println("whitespace");
            if (element == '\n') {
              lineNumber++;
              word.line = lineNumber;
            }
            pointer = pointer + 1;
            element = array[pointer];
            curElement = element;
            // System.out.println("pointer is " + pointer);
          }
        }
        //skip comments
        //TODO: identify when comments stop -- when curElement is \n and the element two steps forward is not '/'
        if ((element == '/' && array[pointer + 1] == '/') || (element == '/' && array[pointer + 1] == '*')) {
          pointer = pointer + 1;
          element = array[pointer];
          curElement = element;
          while (element != '\n' || array[pointer + 1] == '/') {
            pointer = pointer + 1;
            element = array[pointer];
            curElement = element;
            if (element == '\n') {
              lineNumber++;
              word.line = lineNumber;
              if ((pointer + 1) == count) {
                EOF = true;
                System.exit(1);
              }
            }
            //TODO: get the line in string array to find first character of that string
            int j = pointer+1;
            String line = " ";
            while(array[j] != '\n' && j != count) {
              if (line.equals(" ")) line = Character.toString(array[j]);
              else if (!Character.isWhitespace(array[j])) {
                line = line + Character.toString(array[j]);
              }
              j++;
            }
            if (element == '\n' && (array[pointer + 1] == '\n' ||
            (Character.isWhitespace(array[pointer + 1]) && (array[pointer + 3] == '/' ||
            array[pointer + 3] == '*' || array[pointer + 2] == '*' || line.charAt(0) == '/') || line.charAt(0) == '*'))) {
              if (array[pointer + 1] == '\n') {
                lineNumber++;
                System.out.println(lineNumber);
                word.line = lineNumber;
              }
              pointer = pointer + 1;
              element = array[pointer];
              curElement = element;
              while (array[pointer + 1] == '\n' || Character.isWhitespace(array[pointer + 1])) {
                pointer = pointer + 1;
                if (pointer == array.length) {
                  EOF = true;
                }
                element = array[pointer];
                curElement = element;
                if (element == '\n') {
                  lineNumber++;
                  word.line = lineNumber;
                }
              }
            }
          }
          if (element == '\n' && array[pointer + 1] != '/') {
            pointer = pointer + 1;
            element = array[pointer];
            curElement = element;
          }
        }

        if (element == '\n' || Character.isWhitespace(element)) {
          while (element == '\n' || Character.isWhitespace(element)) {
            if (element == '\n') {
              lineNumber++;
              word.line = lineNumber;
            }
            pointer = pointer + 1;
            element = array[pointer];
            curElement = element;
          }
        }

        start = curElement;
        // System.out.println(array[pointer + 3]);
        if (start == 0) {
          EOF = true;
          word.Type = TokenTypes.EOF;
          return word;
        } // EOF
        if ((start == '/' && array[pointer + 1] == '/') || (start == '/' && array[pointer + 1] == '*')) {
          pointer = pointer + 1;
          start = array[pointer];
          curElement = start;
          while (start != '\n' || array[pointer + 1] == '/') {
            pointer = pointer + 1;
            if (pointer == array.length) {
              EOF = true;
            }
            start = array[pointer];
            curElement = start;
            // System.out.println("comment after starting");
            // System.out.println("pointer is " + pointer);
            if (start == '\n') {
              lineNumber++;
              word.line = lineNumber;
              if ((pointer + 1) == count) {
                EOF = true;
                System.exit(1);
              }
            }
            int k = pointer+1;
            String secondLine = " ";
            while(array[k] != '\n') {
              if (secondLine.equals(" ")) secondLine = Character.toString(array[k]);
              else if (!Character.isWhitespace(array[k])) {
                secondLine = secondLine + Character.toString(array[k]);
                // System.out.println(line.charAt(0));
              }
              k++;
            }
            if (start == '\n' && (array[pointer + 1] == '\n' ||
            (Character.isWhitespace(array[pointer + 1]) && (array[pointer + 3] == '/' ||
            array[pointer + 3] == '*' || array[pointer + 2] == '*' || secondLine.charAt(0) == '/' || secondLine.charAt(0) == '*') ))) {
              if (array[pointer + 1] == '\n') {
                lineNumber++;
                word.line = lineNumber;
              }
              pointer = pointer + 1;
              start = array[pointer];
              curElement = start;
              while (array[pointer + 1] == '\n' || Character.isWhitespace(array[pointer + 1])) {
                pointer = pointer + 1;
                start = array[pointer];
                curElement = start;
                if (start == '\n') {
                  lineNumber++;
                  word.line = lineNumber;
                }
              }
            }
          }
          if (start == '\n' && array[pointer + 1] != '/') {
            pointer = pointer + 1;
            start = array[pointer];
            curElement = start;
          }
        }
        if (start == '\n' || Character.isWhitespace(start)) {
          while (start == '\n' || Character.isWhitespace(start)) {
            if (start == '\n') {
              lineNumber++;
              word.line = lineNumber;
            }
            pointer = pointer + 1;
            start = array[pointer];
            curElement = start;
          }
        }
        if (Character.isLetter(start) || start == '_') { // Reserved word & Identifier
          while (Character.isLetter(start) || Character.isDigit(start) || start == '_') {
            word.Lexeme.append(start);
            pointer = pointer + 1;
            start = array[pointer];
          }
          word.Type = TokenTypes.Identifier;
          curElement = start;
          if ("null".equals(word.Lexeme.toString()) || "true".equals(word.Lexeme.toString()) || "false".equals(word.Lexeme.toString())) {
            word.Type = TokenTypes.Constant;
          }

          return word;
        }
        else if (Character.isDigit(start)) { // IntegerConstant
          while(Character.isDigit(start)) {
            word.Lexeme.append(start);
            pointer = pointer + 1;
            start = array[pointer];
          }
          word.Type = TokenTypes.IntegerConstant;
          curElement = start;

          return word;
        }
        else if (start == '\"') {
          word.Lexeme.append(start);
          pointer = pointer + 1;
          start = array[pointer];
          while (start != '\"' && start != '\n') {
            word.Lexeme.append(start);
            pointer = pointer + 1;
            if (pointer == count) {
              EOF = true;
              System.exit(1);
            }
            start = array[pointer];
          }
          if (start == '\"') {
            word.Lexeme.append(start);
            pointer = pointer + 1;
            start = array[pointer];
          }
          word.Type = TokenTypes.StringConstant;
          curElement = start;

          return word;
        }
        else { // Symbol
          word.Lexeme.append(start);
          pointer = pointer + 1;
          word.Type = TokenTypes.Symbol;
          start = array[pointer];
          curElement = start;
          //
          return word;
        }
      }
    }
    return word;
  }

  /*
   * PeekNextToken method which reads the next token then figures out the type of that token.
   * THe whitespaces and comments are skipped. The token read is not consumed but just remains
   * the same as not proceesed yet.
   */
  public Token PeekNextToken() {
    Token word = new Token();
    int i;
    int n;
    int z;
    int count = 0;
    char start = 0;
    char element;
    boolean EOF;

    word.line = lineNumber;
    for (z=0; z< length; z++) {
      if(array[z] != '\u0000') count++;
    }
    // when NextToken() function is called
    if (curElement == 0) {
      preElement = curElement;
      curElement = array[0];//peeknexttoken array(lexeme.tostring.charat(0))
      prePointer = pointer;
      pointer = 0;
      if (prePointer > pointer) {
        curElement = preElement; //getnexttoken
        pointer = prePointer;
        EOF = true;
        word.Type = TokenTypes.EOF;
        return word;
      }
      EOF = false;
    } else {
      EOF = false;
    }

    //
    if (!EOF) {
      for (n = pointer; n < array.length; n++) {
        element = array[n]; // assign the element at the position of element currently being processed to 'element'
        pointer = n;
        if (Character.isWhitespace(element) || element == '\n') { // skip white spaces
          while (Character.isWhitespace(element) || element == '\n') {
            // System.out.println("whitespace");
            if (element == '\n') {
              lineNumber++;
              word.line = lineNumber;
            }
            pointer = pointer + 1;
            element = array[pointer];
            curElement = element;
            // System.out.println("pointer is " + pointer);
          }
        }
        //skip comments
        if ((element == '/' && array[pointer + 1] == '/') || (element == '/' && array[pointer + 1] == '*')) {
          pointer = pointer + 1;
          element = array[pointer];
          curElement = element;
          while (element != '\n' || array[pointer + 1] == '/') { // opposite case: element == '\n' && array[pointer + 1] != '/'
            // System.out.println("comment");
            pointer = pointer + 1;
            element = array[pointer];
            curElement = element;
            if (element == '\n') {
              lineNumber++;
              word.line = lineNumber;
              if ((pointer + 1) == count) {
                EOF = true;
                System.exit(1);
              }
            }
            // System.out.println("pointer is " + pointer);
            int l = pointer+1;
            String thirdLine = " ";
            while(array[l] != '\n') {
              if (thirdLine.equals(" ")) thirdLine = Character.toString(array[l]);
              else if (!Character.isWhitespace(array[l])) {
                thirdLine = thirdLine + Character.toString(array[l]);
                // System.out.println(line.charAt(0));
              }
              l++;
            }
            if (element == '\n' && (array[pointer + 1] == '\n' ||
            (Character.isWhitespace(array[pointer + 1]) && (array[pointer + 3] == '/' ||
            array[pointer + 3] == '*' || array[pointer + 2] == '*' || thirdLine.charAt(0) == '/') || thirdLine.charAt(0) == '*'))) {
              if (array[pointer + 1] == '\n') {
                lineNumber++;
                word.line = lineNumber;
              }
              pointer = pointer + 1;
              element = array[pointer];
              curElement = element;
              while (array[pointer + 1] == '\n' || Character.isWhitespace(array[pointer + 1])) {
                pointer = pointer + 1;
                element = array[pointer];
                curElement = element;
                if (element == '\n') {
                  lineNumber++;
                  word.line = lineNumber;
                }
              }
            }
          }
          if (element == '\n' && array[pointer + 1] != '/') {
            pointer = pointer + 1;
            element = array[pointer];
            curElement = element;
          }
          // pointer = pointer + 1;
          // curElement = array[pointer];
        }

        if (element == '\n' || Character.isWhitespace(element)) {
          while (element == '\n' || Character.isWhitespace(element)) {
            // System.out.println("newline character");
            if (element == '\n') {
              lineNumber++;
              word.line = lineNumber;
            }
            pointer = pointer + 1;
            element = array[pointer];
            curElement = element;
          }
        }

        start = curElement;
        if (start == 0) {
          EOF = true;
          word.Type = TokenTypes.EOF;
          return word; // EOF
        }
        if ((start == '/' && array[pointer + 1] == '/') || (start == '/' && array[pointer + 1] == '*')) {
          pointer = pointer + 1;
          start = array[pointer];
          curElement = start;
          while (start != '\n' || array[pointer + 1] == '/') {
            // System.out.println("comment after starting");
            pointer = pointer + 1;
            start = array[pointer];
            // System.out.println("pointer is " + pointer);
            curElement = start;
            if (start == '\n') {
              lineNumber++;
              word.line = lineNumber;
              if ((pointer + 1) == count) {
                EOF = true;
                System.exit(1);
              }
            }
            int m = pointer+1;
            String fourthLine = " ";
            while(array[m] != '\n') {
              if (fourthLine.equals(" ")) fourthLine = Character.toString(array[m]);
              else if (!Character.isWhitespace(array[m])) {
                fourthLine = fourthLine + Character.toString(array[m]);
                // System.out.println(line.charAt(0));
              }
              m++;
            }
            if (start == '\n' && (array[pointer + 1] == '\n' ||
            (Character.isWhitespace(array[pointer + 1]) && (array[pointer + 3] == '/' ||
            array[pointer + 3] == '*' || array[pointer + 2] == '*' || fourthLine.charAt(0) == '/') || fourthLine.charAt(0) == '*'))) {
              if (array[pointer + 1] == '\n') {
                lineNumber++;
                word.line = lineNumber;
              }
              pointer = pointer + 1;
              start = array[pointer];
              curElement = start;
              while (array[pointer + 1] == '\n' || Character.isWhitespace(array[pointer + 1])) {
                pointer = pointer + 1;
                start = array[pointer];
                curElement = start;
                if (start == '\n') {
                  lineNumber++;
                  word.line = lineNumber;
                }
              }
            }
          }
          if (start == '\n' && array[pointer + 1] != '/') {
            pointer = pointer + 1;
            start = array[pointer];
            curElement = start;
          }
        }
        if (start == '\n' || Character.isWhitespace(start)) {
          while (start == '\n' || Character.isWhitespace(start)) {
            // System.out.println("newline character");
            if (start == '\n') {
              lineNumber++;
              word.line = lineNumber;
            }
            pointer = pointer + 1;
            start = array[pointer];
            curElement = start;
          }
        }
        if (Character.isLetter(start) || start == '_') { // Reserved word & Identifier
          wordPointer = pointer;
          while (Character.isLetter(start) || Character.isDigit(start) || start == '_') {
            word.Lexeme.append(start);
            pointer = pointer + 1;
            start = array[pointer];
            // System.out.println("pointer is " + pointer);
          }
          word.Type = TokenTypes.Identifier;
          if ("null".equals(word.Lexeme.toString()) || "true".equals(word.Lexeme.toString()) || "false".equals(word.Lexeme.toString())) {
            word.Type = TokenTypes.Constant;
            //
          }

          pointer = wordPointer;
          return word;
        }
        else if (Character.isDigit(start)) { // IntegerConstant
          wordPointer = pointer;
          while(Character.isDigit(start)) {
            word.Lexeme.append(start);
            pointer = pointer + 1;
            start = array[pointer];
          }
          word.Type = TokenTypes.IntegerConstant;

          pointer = wordPointer;
          return word;
        }
        else if (start == '\"') {
          wordPointer = pointer;
          word.Lexeme.append(start);
          pointer = pointer + 1;
          start = array[pointer];
          while (start != '\"' && start != '\n') {
            word.Lexeme.append(start);
            pointer = pointer + 1;
            if (pointer == count) {
              EOF = true;
              System.exit(1);
            }
            start = array[pointer];
            // System.out.println("pointer is " + pointer);
          }
          if (start == '\"') {
            word.Lexeme.append(start);
            pointer = pointer + 1;
            start = array[pointer];
          }
          word.Type = TokenTypes.StringConstant;

          pointer = wordPointer;
          return word;
        }
        else {
          word.Lexeme.append(start);
          word.Type = TokenTypes.Symbol;
          return word;
        }
      }
    }
    return word;
  }


  public static void main(String[] args) {
    int i;
    int j;
    String repeatNo;
    Token myToken;
    NickLexer myLexer = new NickLexer();

    for (i=0; i<args.length; i++) {
      if (myLexer.init(args[i])) {
        myLexer.readFile();
        myLexer.closeFile();

        repeatNo = args[1]; //find how many times user wants to repeat using GetNextToken() using the command line interface
        for(j = 0; j<Integer.parseInt(repeatNo); j++) {
          myToken = myLexer.GetNextToken();
        }
        myToken = myLexer.GetNextToken();
      }

      else System.exit(0);
    }
  }
}
