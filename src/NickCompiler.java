/* NickCompiler class that analyses the jack source file and finds syntactic errors.
 * Code generation and symbol table are partially functioning.
 */
import java.io.*;
import java.util.*;

class NickCompiler {
  private static NickLexer lexer;
  private SymbolTable[] tables;
  private int currentTable;
  private String[] vmCode;
  private int commandPointer;
  private int maxIndex = 10000;

  public NickCompiler() {
    tables = new SymbolTable[2];
    tables[0] = new SymbolTable();
    tables[1] = new SymbolTable();
    currentTable = 0;
    vmCode = new String[maxIndex];
    commandPointer = 0;
  }

  //initialise the file
  public void init(String filename) {
    lexer = new NickLexer();
    if (!lexer.init(filename)) {
      System.out.println("Parser cannot be initialised");
      System.exit(1);
    }
    System.out.println("Initialised successfully");
    lexer.readFile();
    lexer.closeFile();
  }

  //print error message properly
  public void error(NickLexer.Token token, String message) {
    System.out.println("Error, line " + token.line + " close to " + "\"" + token.Lexeme + "\", " + message);
    System.exit(1);
  }

  //print OK for correct syntaxes
  public void success(NickLexer.Token token) {
    System.out.println(token.Lexeme + ": OK");
  }

  /* This is the whole jack program layout, i.e. syntax of jack class.
   * It calls memberDeclar() method when it is a correct place for declaring the components of class.
   * It prints out symbol table and code generation in text file, created after execution of compiler.
   */
  public void jackProg() {
    NickLexer.Token word = lexer.GetNextToken();
    if (word.Lexeme.toString().equals("class")) {
      success(word);
    } else {
      // System.out.println(word.Lexeme);
      error(word, "\'class\' is expected at this position");
    }
    word = lexer.GetNextToken();
    if (word.Type == NickLexer.TokenTypes.Identifier) {
      success(word);
    } else error(word, "an identifier is expected at this position");
    word = lexer.GetNextToken();
    if (word.Lexeme.toString().equals("{")) {
      success(word);
      word = lexer.PeekNextToken();
      while (!word.Lexeme.toString().equals("}")) {
        memberDeclar();
        word = lexer.PeekNextToken();
      }
      word = lexer.GetNextToken();
      if (word.Lexeme.toString().equals("}")) {
        success(word);
      } else error(word, "\'}\' is expected at this position");
    } else error(word, "\'{\' is expected at this position");

    tables[0].printTable();


    try {
      PrintWriter writer = new PrintWriter("vmcode.txt", "UTF-8");
      for (int i = 0; i < maxIndex; i++) {
        writer.println(vmCode[i]);
      }
      writer.close();
    } catch (FileNotFoundException err) {
      System.err.println(err);
    } catch (UnsupportedEncodingException err) {
      System.err.println(err);
    }
  }
  //method to add string element in an array that stores VM code generated so far.
  public void writeVmCommand(String vmCommand) {
    vmCode[commandPointer++] = vmCommand;
  }

  public void memberDeclar() {
    NickLexer.Token word = lexer.PeekNextToken();

    if (word.Lexeme.toString().equals("static") || word.Lexeme.toString().equals("field")) {
      classVarDeclar();
    }
    else if (word.Lexeme.toString().equals("constructor") || word.Lexeme.toString().equals("function") || word.Lexeme.toString().equals("method")) {
      subroutineDeclar();
    }
    else {
      error(word, "known keyword is expected at this position");
    }
  }

  public void classVarDeclar() {
    NickLexer.Token word = lexer.GetNextToken();
    NickLexer.Token tmp = word;
    if (word.Lexeme.toString().equals("static") || word.Lexeme.toString().equals("field")) {
      success(word);
      type();
    } else error(word, "\'static\' or \'field\' is expected at this position");
    word = lexer.GetNextToken();
    if (word.Type == NickLexer.TokenTypes.Identifier) {
      success(word);
      if (tmp.Lexeme.toString().equals("static")) tables[currentTable].addSymbol(word.Lexeme.toString(), SymbolTable.SymbolTypes.Static);
      else if (tmp.Lexeme.toString().equals("field")) tables[currentTable].addSymbol(word.Lexeme.toString(), SymbolTable.SymbolTypes.Field);
    } else error(word, "an identifier is expected at this position");
    word = lexer.PeekNextToken();
    while (word.Lexeme.toString().equals(",")) {
      success(word);
      word = lexer.GetNextToken();
      word = lexer.GetNextToken();
      if (word.Type == NickLexer.TokenTypes.Identifier) {
        success(word);
        if (tmp.Lexeme.toString().equals("static")) tables[currentTable].addSymbol(word.Lexeme.toString(), SymbolTable.SymbolTypes.Static);
        else if (tmp.Lexeme.toString().equals("field")) tables[currentTable].addSymbol(word.Lexeme.toString(), SymbolTable.SymbolTypes.Field);
      } else error(word, "an identifier is expected at this position");
      word = lexer.PeekNextToken();
    }
    word = lexer.GetNextToken();
    if (word.Lexeme.toString().equals(";")) {
      success(word);
    } else error(word, "\';\' is expected at this position");
  }

  public boolean type() {
    NickLexer.Token word = lexer.GetNextToken();
    if (word.Lexeme.toString().equals("int")) {
      success(word);
      return true;
    }
    else if (word.Lexeme.toString().equals("char")) {
      success(word);
      return true;
    }
    else if (word.Lexeme.toString().equals("boolean")) {
      success(word);
      return true;
    }
    else if (word.Type == NickLexer.TokenTypes.Identifier) {
      success(word);
      return true;
    }
    else {
      error(word, "variable type keyword is expected at this position");
      return false;
    }
  }

  public void subroutineDeclar() {
    NickLexer.Token word = lexer.GetNextToken();
    NickLexer.Token tmp = null;
    int numOfVars = 0;

    if (word.Lexeme.toString().equals("constructor") || word.Lexeme.toString().equals("function") || word.Lexeme.toString().equals("method")) {
      success(word);
    } else error(word, "subroutine type keyword is expected at this position");
    word = lexer.GetNextToken();
    if (word.Lexeme.toString().equals("void")) {
      success(word);
    }
    else if (word.Lexeme.toString().equals("int") || word.Lexeme.toString().equals("char")
    || word.Lexeme.toString().equals("boolean") || word.Type == NickLexer.TokenTypes.Identifier) {
      success(word);
    }
    else {
      error(word, "\'void\' or subroutine type keyword is expected at this position");
    }
    word = lexer.GetNextToken();
    if (word.Type == NickLexer.TokenTypes.Identifier) {
      success(word);
      tmp = word;
    } else error(word, "an identifier is expected at this position");
    word = lexer.GetNextToken();
    if (word.Lexeme.toString().equals("(")) {
      success(word);
      paramList();
    } else error(word, "\'(\' is expected at this position");
    word = lexer.GetNextToken();
    if (word.Lexeme.toString().equals(")")) {
      success(word);
      numOfVars = subroutineBody();
    } else error(word, "\')\' is expected at this position");
    writeVmCommand("function " + tmp.Lexeme.toString() + " " + numOfVars);
  }

  public void paramList() {
    NickLexer.Token word = lexer.PeekNextToken();

    if (word.Lexeme.toString().equals("int") || word.Lexeme.toString().equals("char") || word.Lexeme.toString().equals("boolean") || word.Type == NickLexer.TokenTypes.Identifier) {
      word = lexer.GetNextToken();
      success(word);
      word = lexer.GetNextToken();
      if (word.Type == NickLexer.TokenTypes.Identifier) {
        success(word);
        tables[currentTable].addSymbol(word.Lexeme.toString(), SymbolTable.SymbolTypes.Argument);
      } else error(word, "an identifier is expected at this position");
      word = lexer.PeekNextToken();
      while (word.Lexeme.toString().equals(",")) {
        word = lexer.GetNextToken();
        success(word);
        type();
        word = lexer.GetNextToken();
        if (word.Type == NickLexer.TokenTypes.Identifier) {
          success(word);
          tables[currentTable].addSymbol(word.Lexeme.toString(), SymbolTable.SymbolTypes.Argument);
        } else error(word, "an identifier is expected at this position");
        word = lexer.PeekNextToken();
      }
    } else success(word);
  }

  public int subroutineBody() {
    NickLexer.Token word = lexer.GetNextToken();
    int count = 0;

    if (word.Lexeme.toString().equals("{")) {
      success(word);
      word = lexer.PeekNextToken();
      while (!word.Lexeme.toString().equals("}")) {
        count = statement();
        word = lexer.PeekNextToken();
      }
      word = lexer.GetNextToken();
      if (word.Lexeme.toString().equals("}")) {
        success(word);
      } else error(word, "\'}\' is expected at this position");
    } else error(word, "\'{\' is expected at this position");
    return count;
  }

  public int statement() {
    NickLexer.Token word = lexer.PeekNextToken();
    int count = 0;

    switch (word.Lexeme.toString()) {
      case "var":
        count = varDeclarStatement();
        break;
      case "let":
        letStatement();
        break;
      case "if":
        ifStatement();
        break;
      case "while":
        whileStatement();
        writeVmCommand("label end");
        break;
      case "do":
        doStatement();
        break;
      case "return":
        returnStatement();
        break;
      default:
        error(word, "statement name keyword is expected at this position");
    }
    return count;
  }

  public int varDeclarStatement() {
    NickLexer.Token word = lexer.GetNextToken();
    int count = 0;
    if (word.Lexeme.toString().equals("var")) {
      success(word);
      type();
    } else error(word, "\'var\' is expected at this position");
    word = lexer.GetNextToken();
    if (word.Type == NickLexer.TokenTypes.Identifier) {
      success(word);
      tables[currentTable].addSymbol(word.Lexeme.toString(), SymbolTable.SymbolTypes.Var);
      count++;
    } else error(word, "an identifier is expected at this position");
    word = lexer.PeekNextToken();
    while (word.Lexeme.toString().equals(",")) {
      success(word);
      word = lexer.GetNextToken();
      word = lexer.GetNextToken();
      if (word.Type == NickLexer.TokenTypes.Identifier) {
        success(word);
        tables[currentTable].addSymbol(word.Lexeme.toString(), SymbolTable.SymbolTypes.Var);
        count++;
      } else error(word, "an identifier is expected at this position");
      word = lexer.PeekNextToken();
    }
    word = lexer.GetNextToken();
    if (word.Lexeme.toString().equals(";")) {
      success(word);
    } else error(word, "\';\' is expected at this position");
    return count;
  }

  public void letStatement() {
    NickLexer.Token word = lexer.GetNextToken();
    SymbolTable.SymbolTypes sKind = null;
    int offset = -1;

    if (word.Lexeme.toString().equals("let")) {
      success(word);
    } else error(word, "\'let\' is expected at this position");
    word = lexer.GetNextToken();
    if (word.Type == NickLexer.TokenTypes.Identifier) {
      success(word);
      sKind = tables[currentTable].getSymbolType(word.Lexeme.toString());
      offset = tables[currentTable].getSymbolOffset(word.Lexeme.toString());
    } else error(word, "an identifier is expected at this position");
    word = lexer.PeekNextToken();
    if (word.Lexeme.toString().equals("[")) {
      success(word);
      word = lexer.GetNextToken();
      expression();
      word = lexer.GetNextToken();
      if (!word.Lexeme.toString().equals("]")) error(word, "\']\' is expected at this position");
    }
    word = lexer.GetNextToken();
    if (word.Lexeme.toString().equals("=")) {
      success(word);
      expression();
      writeVmCommand("pop " + sKind + " " + offset);
      word = lexer.GetNextToken();
      if (word.Lexeme.toString().equals(";")) {
        success(word);
      } else error(word, "\';\' is expected at this position");
    } else error(word, "\'=\' is expected at this position");
  }

  public void ifStatement() {
    NickLexer.Token word = lexer.GetNextToken();
    String label = "";

    if (word.Lexeme.toString().equals("if")) {
      success(word);
    } else error(word, "\'if\' is expected at this position");
    word = lexer.GetNextToken();
    if (word.Lexeme.toString().equals("(")) {
      success(word);
      expression();
      writeVmCommand("if-goto " + label);
      word = lexer.GetNextToken();
      if (!word.Lexeme.toString().equals(")")) error(word, "\')\' is expected at this position");
    } else error(word, "\'(\' is expected at this position");
    word = lexer.GetNextToken();
    if (word.Lexeme.toString().equals("{")) {
      success(word);
      word = lexer.PeekNextToken();
      while (!word.Lexeme.toString().equals("}")) {
        statement();
        word = lexer.PeekNextToken();
      }
      word = lexer.GetNextToken();
      if (!word.Lexeme.toString().equals("}")) error(word, "\'}\' is expected at this position");
    } else error(word, "\'{\' is expected at this position");
    word = lexer.PeekNextToken();
    if (word.Lexeme.toString().equals("else")) {
      word = lexer.GetNextToken();
      success(word);
      writeVmCommand("label else");
      word = lexer.GetNextToken();
      if (word.Lexeme.toString().equals("{")) {
        success(word);
        word = lexer.PeekNextToken();
        while (!word.Lexeme.toString().equals("}")) {
          statement();
          word = lexer.PeekNextToken();
        }
        word = lexer.GetNextToken();
        if (!word.Lexeme.toString().equals("}")) error(word, "\'}\' is expected at this position");
      } else error(word, "\'{\' is expected at this position");
    }
  }

  public void whileStatement() {
    NickLexer.Token word = lexer.GetNextToken();
    String label = "";

    if (word.Lexeme.toString().equals("while")) {
      success(word);
    } else error(word, "\'while\' is expected at this position");
    word = lexer.GetNextToken();
    if (word.Lexeme.toString().equals("(")) {
      success(word);
      expression();
      writeVmCommand("label loop");
      writeVmCommand("if-goto " + label);
      word = lexer.GetNextToken();
      if (!word.Lexeme.toString().equals(")")) error(word, "\')\' is expected at this position");
    } else error(word, "\'(\' is expected at this position");
    word = lexer.GetNextToken();
    if (word.Lexeme.toString().equals("{")) {
      success(word);
      word = lexer.PeekNextToken();
      while (!word.Lexeme.toString().equals("}")) {
        statement();
        writeVmCommand("goto loop");
        word = lexer.PeekNextToken();
      }
      word = lexer.GetNextToken();
      if (!word.Lexeme.toString().equals("}")) error(word, "\'}\' is expected at this position");
    } else error(word, "\'{\' is expected at this position");
  }

  public void doStatement() {
    NickLexer.Token word = lexer.GetNextToken();
    if (word.Lexeme.toString().equals("do")) {
      success(word);
      subroutineCall();
      writeVmCommand("call do 1");
    } else error(word, "\'do\' is expected at this position");
    word = lexer.GetNextToken();
    if (word.Lexeme.toString().equals(";")) {
      success(word);
    } else error(word, "\';\' is expected at this position");
  }

  public void subroutineCall() {
    NickLexer.Token word = lexer.GetNextToken();
    SymbolTable.SymbolTypes sKind = null;
    int offset = -1;
    if (word.Type == NickLexer.TokenTypes.Identifier) {
      success(word);
      sKind = tables[currentTable].getSymbolType(word.Lexeme.toString());
      offset = tables[currentTable].getSymbolOffset(word.Lexeme.toString());
    } else error(word, "an identifier is expected at this position");
    word = lexer.PeekNextToken();
    if (word.Lexeme.toString().equals(".")) {
      word = lexer.GetNextToken();
      word = lexer.GetNextToken();
      if (word.Type == NickLexer.TokenTypes.Identifier) {
        success(word);
        sKind = tables[currentTable].getSymbolType(word.Lexeme.toString());
        offset = tables[currentTable].getSymbolOffset(word.Lexeme.toString());
      } else error(word, "an identifier is expected at this position");
    }
    writeVmCommand("push " + sKind + " " + offset);
    word = lexer.GetNextToken();
    if (word.Lexeme.toString().equals("(")) {
      success(word);
      expressionList();
      word = lexer.GetNextToken();
      if (!word.Lexeme.toString().equals(")")) error(word, "\')\' is expected at this position");
    } else error(word, "\'(\' is expected at this position");
  }

  public void expressionList() {
    NickLexer.Token word = lexer.PeekNextToken();
    String l = word.Lexeme.toString();
    if (l.equals("-") || l.equals("~")) {
      word = lexer.GetNextToken();
      expression();
      word = lexer.PeekNextToken();
      while (word.Lexeme.toString().equals(",")) {
        // success(word);
        word = lexer.GetNextToken();
        expression();
        word = lexer.PeekNextToken();
      }
    }
    else if (word.Type == NickLexer.TokenTypes.IntegerConstant || word.Type == NickLexer.TokenTypes.Identifier
    || l.equals("(") || word.Type == NickLexer.TokenTypes.StringConstant || l.equals("true")
    || l.equals("false") || l.equals("null") || l.equals("this")) { //operand() does the same thing as expression()... that is why the line two lines below is commented out
      expression();
      word = lexer.PeekNextToken();
      while (word.Lexeme.toString().equals(",")) {
        // success(word);
        word = lexer.GetNextToken();
        expression();
        word = lexer.PeekNextToken();
      }
    }
    else {
      success(word);
    }
  }

  public void returnStatement() {
    NickLexer.Token word = lexer.GetNextToken();

    if (word.Lexeme.toString().equals("return")) {
      success(word);
    } else error(word, "\'return\' is expected at this position");
    word = lexer.PeekNextToken();
    String l = word.Lexeme.toString();
    if (l.equals("-") || l.equals("~") || word.Type == NickLexer.TokenTypes.IntegerConstant
    || word.Type == NickLexer.TokenTypes.Identifier || l.equals("(") || word.Type == NickLexer.TokenTypes.StringConstant
    || l.equals("true") || l.equals("false") || l.equals("null") || l.equals("this")) {
      expression();
      writeVmCommand("return");
    }
    word = lexer.GetNextToken();
    if (word.Lexeme.toString().equals(";")) {
      success(word);
    } else error(word, "\';\' is expected at this position");
  }

  public boolean expression() {
    boolean result = false;
    NickLexer.Token word = lexer.PeekNextToken();
    if (relationalExpression()) {
      System.out.println("out from relationalExpression");
      result = true;
    }
    word = lexer.PeekNextToken();
    while (word.Lexeme.toString().equals("&") || word.Lexeme.toString().equals("|")) {
      word = lexer.GetNextToken();
      relationalExpression();
      if (word.Lexeme.toString().equals("&")) writeVmCommand("and");
      else writeVmCommand("or");
      word = lexer.PeekNextToken();
    }
    return result;
  }

  public boolean relationalExpression() {
    boolean result = false;
    if (arithmeticExpression()) {
      result = true;
    }
    NickLexer.Token word = lexer.PeekNextToken();
    while (word.Lexeme.toString().equals("=") || word.Lexeme.toString().equals(">") || word.Lexeme.toString().equals("<")) {
      word = lexer.GetNextToken();
      arithmeticExpression();
      if (word.Lexeme.toString().equals("=")) writeVmCommand("eq");
      else if (word.Lexeme.toString().equals(">")) writeVmCommand("gt");
      else writeVmCommand("lt");
      word = lexer.PeekNextToken();
    }
    return result;
  }

  public boolean arithmeticExpression() {
    boolean result = false;
    if (term()) {
      result = true;
    }
    NickLexer.Token word = lexer.PeekNextToken();
    while (word.Lexeme.toString().equals("+") || word.Lexeme.toString().equals("-")) {
      word = lexer.GetNextToken();
      term();
      if (word.Lexeme.toString().equals("+")) writeVmCommand("add");
      else writeVmCommand("sub");
      word = lexer.PeekNextToken();
    }
    return result;
  }

  public boolean term() {
    boolean result = false;
    if (factor()) {
      result = true;
    }
    NickLexer.Token word = lexer.PeekNextToken();
    while (word.Lexeme.toString().equals("*") || word.Lexeme.toString().equals("/")) {
      word = lexer.GetNextToken();
      success(word);
      factor();
      if (word.Lexeme.toString().equals("*")) writeVmCommand("call Math.multiply 2");
      else writeVmCommand("call Math.divide 2");
      word = lexer.PeekNextToken();
    }
    return result;
  }

  public boolean factor() {
    NickLexer.Token word = lexer.PeekNextToken();
    NickLexer.Token tmp = word;

    boolean result = false;
    if (word.Lexeme.toString().equals("-") || word.Lexeme.toString().equals("~")) {
      word = lexer.GetNextToken();
      success(word);
      result = true;
    }
    if (operand()) {
      result = true;
    }
    if (tmp.Lexeme.toString().equals("-") || tmp.Lexeme.toString().equals("~")) writeVmCommand("not");
    return result;
  }

  public boolean operand() {
    NickLexer.Token word = lexer.GetNextToken();
    boolean result = false;

    if (word.Type == NickLexer.TokenTypes.IntegerConstant) {
      success(word);
      result = true;
      writeVmCommand("push constant " + word.Lexeme.toString());
    }
    else if (word.Type == NickLexer.TokenTypes.Identifier) {
      success(word);
      result = true;
      SymbolTable.SymbolTypes sKind = tables[currentTable].getSymbolType(word.Lexeme.toString());
      int offset = tables[currentTable].getSymbolOffset(word.Lexeme.toString());
      word = lexer.PeekNextToken();
      if (word.Lexeme.toString().equals(".")) {
        word = lexer.GetNextToken();
        word = lexer.GetNextToken();
        if (word.Type == NickLexer.TokenTypes.Identifier) {
          success(word);
          sKind = tables[currentTable].getSymbolType(word.Lexeme.toString());
          offset = tables[currentTable].getSymbolOffset(word.Lexeme.toString());
        } else error(word, "an identifier is expected at this position");
      }
      word = lexer.PeekNextToken();
      if (word.Lexeme.toString().equals("[")) {
        word = lexer.GetNextToken();
        expression();
        word = lexer.GetNextToken();
        if (!word.Lexeme.toString().equals("]")) error(word, "\']\' is expected at this position");
      }
      else if (word.Lexeme.toString().equals("(")) {
        word = lexer.GetNextToken();
        expressionList();
        word = lexer.GetNextToken();
        if (!word.Lexeme.toString().equals(")")) error(word, "\')\' is expected at this position");
      }
      writeVmCommand("push " + sKind + " " + offset);
    }
    else if (word.Lexeme.toString().equals("(")) {
      success(word);
      result = true;
      expression();
      word = lexer.GetNextToken();
      if (!word.Lexeme.toString().equals(")")) error(word, "\')\' is expected at this position");
    }
    else if (word.Type == NickLexer.TokenTypes.StringConstant) {
      success(word);
      result = true;
    }
    else if (word.Lexeme.toString().equals("true")) {
      success(word);
      result = true;
    }
    else if (word.Lexeme.toString().equals("false")) {
      success(word);
      result = true;
    }
    else if (word.Lexeme.toString().equals("null")) {
      success(word);
      result = true;
    }
    else if (word.Lexeme.toString().equals("this")) {
      success(word);
      result = true;
    }
    else {
      error(word, "operand keyword is expected at this position");
      result = false;
    }
    return result;
  }

  public static void main(String[] args) {
    NickCompiler parser = new NickCompiler();
    int i;

    //the first string after the executable's name (NickCompiler) should be .jack file name.
    for (i=0; i<args.length; i++) {
      parser.init(args[i]);
    }
    parser.jackProg();
  }
}
