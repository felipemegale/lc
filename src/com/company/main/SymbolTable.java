package com.company.main;

import java.util.HashMap;
import java.util.Map;

public class SymbolTable {
    public static HashMap<Byte, String> symbolTable;
    public static HashMap<String, Byte> reverseSymbolTable; // reverse table is kept to facilitate search by value
    public static byte lastByte;

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

    public SymbolTable() {
        symbolTable = new HashMap<>();
        reverseSymbolTable = new HashMap<>();

        symbolTable.put(CONST, "const");
        symbolTable.put(VAR, "var");
        symbolTable.put(INTEGER, "integer");
        symbolTable.put(CHAR, "char");
        symbolTable.put(FOR, "for");
        symbolTable.put(IF, "if");
        symbolTable.put(ELSE, "else");
        symbolTable.put(END, "end");
        symbolTable.put(OR, "or");
        symbolTable.put(NOT, "not");
        symbolTable.put(EQUALS, "equals");
        symbolTable.put(TO, "to");
        symbolTable.put(OPEN_PAR, "(");
        symbolTable.put(CLOSE_PAR, ")");
        symbolTable.put(LESS_THAN, "<");
        symbolTable.put(GREATER_THAN, ">");
        symbolTable.put(DIFFERENT, "<>");
        symbolTable.put(GREATER_EQUALS, ">=");
        symbolTable.put(LESS_EQUALS, "<=");
        symbolTable.put(COMMA, ",");
        symbolTable.put(PLUS, "+");
        symbolTable.put(MINUS, "-");
        symbolTable.put(TIMES, "*");
        symbolTable.put(DIVIDED, "/");
        symbolTable.put(SEMI_COLON, ";");
        symbolTable.put(OPEN_CBRA, "{");
        symbolTable.put(CLOSE_CBRA, "}");
        symbolTable.put(THEN, "then");
        symbolTable.put(READLN, "readln");
        symbolTable.put(STEP, "step");
        symbolTable.put(WRITE, "write");
        symbolTable.put(WRITELN, "writeln");
        symbolTable.put(MODULUS, "%");
        symbolTable.put(OPEN_SBRA, "[");
        symbolTable.put(CLOSE_SBRA, "]");
        symbolTable.put(DO, "do");

        reverseSymbolTable.put("const", CONST);
        reverseSymbolTable.put("var", VAR);
        reverseSymbolTable.put("integer", INTEGER);
        reverseSymbolTable.put("char", CHAR);
        reverseSymbolTable.put("for", FOR);
        reverseSymbolTable.put("if", IF);
        reverseSymbolTable.put("else", ELSE);
        reverseSymbolTable.put("end", END);
        reverseSymbolTable.put("or", OR);
        reverseSymbolTable.put("not", NOT);
        reverseSymbolTable.put("equals", EQUALS);
        reverseSymbolTable.put("to", TO);
        reverseSymbolTable.put("(", OPEN_PAR);
        reverseSymbolTable.put(")", CLOSE_PAR);
        reverseSymbolTable.put("<", LESS_THAN);
        reverseSymbolTable.put(">", GREATER_THAN);
        reverseSymbolTable.put("<>", DIFFERENT);
        reverseSymbolTable.put(">=", GREATER_EQUALS);
        reverseSymbolTable.put("<=", LESS_EQUALS);
        reverseSymbolTable.put(",", COMMA);
        reverseSymbolTable.put("+", PLUS);
        reverseSymbolTable.put("-", MINUS);
        reverseSymbolTable.put("*", TIMES);
        reverseSymbolTable.put("/", DIVIDED);
        reverseSymbolTable.put(";", SEMI_COLON);
        reverseSymbolTable.put("{", OPEN_CBRA);
        reverseSymbolTable.put("}", CLOSE_CBRA);
        reverseSymbolTable.put("then", THEN);
        reverseSymbolTable.put("readln", READLN);
        reverseSymbolTable.put("step", STEP);
        reverseSymbolTable.put("write", WRITE);
        reverseSymbolTable.put("writeln", WRITELN);
        reverseSymbolTable.put("%", MODULUS);
        reverseSymbolTable.put("[", OPEN_SBRA);
        reverseSymbolTable.put("]", CLOSE_SBRA);
        reverseSymbolTable.put("do", DO);

        lastByte = DO;
    }

    public Byte searchLexeme(String searchObject) {
        Byte number = null;

        if (symbolTable.containsValue(searchObject))
            number = reverseSymbolTable.get(searchObject);

        return number;
    }

    public Byte insertToken(String sValue) {
        symbolTable.putIfAbsent(++lastByte, sValue);
        reverseSymbolTable.putIfAbsent(sValue, lastByte);

        return lastByte;
    }

    public void printTable() {
        for (Map.Entry<Byte, String> entry : symbolTable.entrySet()) {
            System.out.println("Chave: " + entry.getKey() + " || Valor: " + entry.getValue());
        }
    }
}
