import java.io.*;
import java.util.*;

public class SymbolTable {
  private List<Symbol> table;
  private int staticCount;
  private int fieldCount;
  private int varCount;
  private int argumentCount;

  public enum SymbolTypes {
    Static, Field, Argument, Var;
  }
  public class Symbol {
    public SymbolTypes kind;
    public String label;
    public int offset;
  }

  public SymbolTable() {
    table = new ArrayList<Symbol>();
    staticCount = 0;
    fieldCount = 0;
    argumentCount = 0;
    varCount = 0;
  }

  public void addSymbol(String name, SymbolTypes kind) {
    Symbol newSymbol = new Symbol();
    newSymbol.label = name;
    newSymbol.kind = kind;
    if (kind == SymbolTypes.Static) newSymbol.offset = staticCount++;
    else if (kind == SymbolTypes.Field) newSymbol.offset = fieldCount++;
    else if (kind == SymbolTypes.Argument) newSymbol.offset = argumentCount++;
    else if (kind == SymbolTypes.Var) newSymbol.offset = varCount++;

    table.add(newSymbol);
  }

  public boolean findSymbol(String name) {
    Iterator<Symbol> i = table.iterator();
    while (i.hasNext()) {
      if (i.next().label.equals(name)) {
        return true;
      }
    }
    return false;
  }

  public SymbolTypes getSymbolType(String name) {
    Symbol s;
    Iterator<Symbol> i = table.iterator();
    while (i.hasNext()) {
      s = i.next();
      if (s.label.equals(name)) {
        return s.kind;
      }
    }
    return null;
  }

  public int getSymbolOffset(String variable) {
    Symbol s;
    Iterator<Symbol> i = table.iterator();
    while (i.hasNext()) {
      s = i.next();
      if (s.label.equals(variable)) {
        return s.offset;
      }
    }
    return -1;
  }

  public void printTable() {
    Symbol s;
    Iterator<Symbol> i = table.iterator();
    try {
      PrintWriter writer = new PrintWriter("symbolTable.txt", "UTF-8");
      while (i.hasNext()) {
        s = i.next();
        writer.println(s.label + ", " + s.offset + ", " + s.kind);
      }
      writer.close();
    } catch (FileNotFoundException err) {
      System.err.println(err);
    } catch (UnsupportedEncodingException err) {
      System.err.println(err);
    }
  }
}
