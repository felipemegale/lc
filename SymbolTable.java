import java.util.HashMap;
import java.util.Map;

/**
 * Pontificia Universidade Catolica de Minas Gerais
 * Intituto de Ciencias Exatas e Informatica - Departamento de Ciencia da Computacao
 * Disciplina de Compiladores - L Language Compiler
 * @author: Alexandre Abreu, Felipe Megale, João Castro
 */

public class SymbolTable {
    public static HashMap<String, Symbol> symbolTable;
    public static byte lastByte;
    public static int idCount;

    public final byte CONST = 0x00000000;
    public final byte VAR = 0x00000001;
    public final byte INTEGER = 0x00000002;
    public final byte CHAR = 0x00000003;
    public final byte FOR = 0x00000004;
    public final byte IF = 0x00000005;
    public final byte ELSE = 0x00000006;
    public final byte END = 0x00000007;
    public final byte OR = 0x00000008;
    public final byte NOT = 0x00000009;
    public final byte EQUALS = 0x00000010;
    public final byte TO = 0x00000011;
    public final byte OPEN_PAR = 0x00000012;
    public final byte CLOSE_PAR = 0x00000013;
    public final byte LESS_THAN = 0x00000014;
    public final byte GREATER_THAN = 0x00000015;
    public final byte DIFFERENT = 0x00000016;
    public final byte GREATER_EQUALS = 0x00000017;
    public final byte LESS_EQUALS = 0x00000018;
    public final byte COMMA = 0x00000019;
    public final byte PLUS = 0x00000020;
    public final byte MINUS = 0x00000021;
    public final byte TIMES = 0x00000022;
    public final byte DIVIDED = 0x00000023;
    public final byte SEMI_COLON = 0x00000024;
    public final byte OPEN_CBRA = 0x00000025;
    public final byte CLOSE_CBRA = 0x00000026;
    public final byte THEN = 0x00000027;
    public final byte READLN = 0x00000028;
    public final byte STEP = 0x00000029;
    public final byte WRITE = 0x00000030;
    public final byte WRITELN = 0x00000031;
    public final byte MODULUS = 0x00000032;
    public final byte OPEN_SBRA = 0x00000033;
    public final byte CLOSE_SBRA = 0x00000034;
    public final byte DO = 0x00000035;
    public final byte EOF = 0x00000036;

    public SymbolTable() {
        symbolTable = new HashMap<>();

        symbolTable.put("const", new Symbol(CONST, "const"));
        symbolTable.put("var", new Symbol(VAR, "var"));
        symbolTable.put("integer", new Symbol(INTEGER, "integer"));
        symbolTable.put("char", new Symbol(CHAR, "char"));
        symbolTable.put("for", new Symbol(FOR, "for"));
        symbolTable.put("if", new Symbol(IF, "if"));
        symbolTable.put("else", new Symbol(ELSE, "else"));
        symbolTable.put("end", new Symbol(END, "end"));
        symbolTable.put("or", new Symbol(OR, "or"));
        symbolTable.put("not", new Symbol(NOT, "not"));
        symbolTable.put("=", new Symbol(EQUALS, "="));
        symbolTable.put("to", new Symbol(TO, "to"));
        symbolTable.put("(", new Symbol(OPEN_PAR, "("));
        symbolTable.put(")", new Symbol(CLOSE_PAR, ")"));
        symbolTable.put("<", new Symbol(LESS_THAN, "<"));
        symbolTable.put(">", new Symbol(GREATER_THAN, ">"));
        symbolTable.put("<>", new Symbol(DIFFERENT, "<>"));
        symbolTable.put(">=", new Symbol(GREATER_EQUALS, ">="));
        symbolTable.put("<=", new Symbol(LESS_EQUALS, "<="));
        symbolTable.put(",", new Symbol(COMMA, ","));
        symbolTable.put("+", new Symbol(PLUS, "+"));
        symbolTable.put("-", new Symbol(MINUS, "-"));
        symbolTable.put("*", new Symbol(TIMES, "*"));
        symbolTable.put("/", new Symbol(DIVIDED, "/"));
        symbolTable.put(";", new Symbol(SEMI_COLON, ";"));
        symbolTable.put("{", new Symbol(OPEN_CBRA, "{"));
        symbolTable.put("}", new Symbol(CLOSE_CBRA, "}"));
        symbolTable.put("then", new Symbol(THEN, "then"));
        symbolTable.put("readln", new Symbol(READLN, "readln"));
        symbolTable.put("step", new Symbol(STEP, "step"));
        symbolTable.put("write", new Symbol(WRITE, "write"));
        symbolTable.put("writeln", new Symbol(WRITELN, "writeln"));
        symbolTable.put("%", new Symbol(MODULUS, "%"));
        symbolTable.put("[", new Symbol(OPEN_SBRA, "["));
        symbolTable.put("]", new Symbol(CLOSE_SBRA, "]"));
        symbolTable.put("do", new Symbol(DO, "do"));
        symbolTable.put("EOF", new Symbol(EOF, "EOF"));
        lastByte = EOF;
    }

    public Symbol searchLexeme(String searchObject) {
        return symbolTable.get(searchObject);
    }

    public Symbol insertToken(String sValue) {
        Symbol s = new Symbol("id", sValue, ++lastByte);
        symbolTable.putIfAbsent(sValue, s);
        return s;
    }

    public void printTable() {
        for (Map.Entry<String, Symbol> entry : symbolTable.entrySet()) {
            System.out.println("Chave: " + entry.getKey() + " || Valor: " + entry.getValue());
        }
    }
}
