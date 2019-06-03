import java.io.*;
import java.util.Date;

/**
 * Pontificia Universidade Catolica de Minas Gerais Intituto de Ciencias Exatas
 * e Informatica - Departamento de Ciencia da Computacao Disciplina de
 * Compiladores - L Language Compiler
 * 
 * @author: Alexandre Abreu, Felipe Megale, João Castro
 */

public class LexicalAnalyzer {

    private static final String AVAILABLE_CHARACTERS = "[a-zA-z0-9\\s_\\.,;\\\"&\\*:\\(\\)\\[\\]{}+\\-\"\\'/%\\^@!?><=\"\\n\\r\\t]";
    public SymbolTable symbolTable;
    private boolean logEnabled;
    private File sourceCode;
    private Error errors;

    private long read;
    private int linesRead;

    public LexicalAnalyzer(File sourceCode) {
        this.symbolTable = new SymbolTable();
        this.sourceCode = sourceCode;
        this.logEnabled = false;
        this.read = 0;
        this.linesRead = 1;
    }

    /**
     * A Quick check if the read character belongs to the language alphabet
     * 
     * @param lexeme Read Character
     * @return true or false
     */
    public boolean isLexemeValid(String lexeme) {
        return lexeme.matches(AVAILABLE_CHARACTERS);
    }

    /**
     * Implementation of a Lexical Analyzer using a Finite State Machine to verify a
     * language syntax.
     * 
     * @return a boolean result (err = false, ok = true)
     */

    public Symbol lexicalAnalysis() {
        int initialState = 0;
        int finalState = 2;
        int currentState = initialState;
        int status;
        String c;
        String lexeme = "";
        Symbol token = null;

        try {
            BufferedReader src = new BufferedReader(new FileReader(this.sourceCode));
            src.skip(this.read);
            while (currentState != finalState) {
                if ((status = src.read()) != -1) {
                    c = (((char) status) + "").toLowerCase();
                    read++;
                    log("Char Lido: " + c.toString());
                    // log("Char Lido: " + status);
                    if (!isLexemeValid(c)) {
                        throw new Error(this.linesRead + ":caractere invalido.");
                    }
                } else {
                    if (currentState != 0) {
                        throw new Error(this.linesRead + ":fim de arquivo nao esperado.");
                    } else {
                        return semanticAction("EOF");
                    }
                }
                // Implementation of a Finite State Machine using switch case tests
                // to implement the lexical analyzer.
                switch (currentState) {
                case 0:// Initial State (0)
                    if (c.matches("[a-z]")) { // Testing Letters to begin an Id and a Reserved Word
                        lexeme += c;
                        currentState = 1;
                    } else if (c.matches("\\.|_")) { // Testing . and _ to begin of an id
                        lexeme += c;
                        currentState = 3;
                    } else if (c.matches("[(|)|;|,|\\+|=|%|{|}|\\[|\\]]")) { // Testing single tokens
                        lexeme += c;
                        currentState = 2;
                        token = semanticAction(lexeme);
                    } else if (c.matches("0")) {
                        lexeme += c;
                        currentState = 6;
                    } else if (c.matches("[1-9]")) {
                        log("entrei");
                        lexeme += c;
                        currentState = 9;
                    } else if (c.matches("'")) {
                        lexeme += c;
                        currentState = 4;
                    } else if (c.matches(">")) {
                        lexeme += c;
                        currentState = 13;
                    } else if (c.matches("<")) {
                        lexeme += c;
                        currentState = 14;
                    } else if (c.matches("/")) {
                        lexeme += c;
                        currentState = 10;
                    } else if (c.matches("\\\"")) {
                        lexeme += c;
                        currentState = 15;
                    } else if (c.matches("\\n")) {
                        linesRead++;
                    } else if (!c.matches("[\\r\\t\\s]")) {
                        throw new Error(this.linesRead + ":lexema nao identificado [" + c + "]");
                    }
                    break;
                case 1:
                    if (c.matches("([a-z])|\\.|_|([0-9])")) {
                        lexeme += c;
                        currentState = 1;
                    } else {
                        currentState = 2;
                        token = semanticAction(lexeme);
                        read--; // returning last read char
                    }
                    break;
                case 3:
                    if (c.matches("\\.|_")) {
                        lexeme += c;
                        currentState = 3;
                    } else if (c.matches("[a-z]|[0-9]")) {
                        lexeme += c;
                        currentState = 1;
                    } else {
                        throw new Error(this.linesRead + ":lexema nao identificado [" + (lexeme + c) + "]");
                    }
                    break;
                case 4:
                    if (c.matches("'")) {
                        lexeme += c;
                        currentState = 2;
                    } else if (c.matches(AVAILABLE_CHARACTERS)) {
                        lexeme += c;
                        currentState = 5;
                    } else {
                        throw new Error(this.linesRead + ":lexema nao identificado [" + (lexeme + c) + "]");
                    }
                    break;
                case 5:
                    if (c.matches("'")) {
                        lexeme += c;
                        currentState = 2;
                        token = semanticActionForConstants(lexeme, "constant");
                        token.setType("CHAR");
                    } else {
                        throw new Error(this.linesRead + ":lexema nao identificado [" + (lexeme + c) + "]");
                    }
                    break;
                case 6:
                    if (c.matches("x")) {
                        lexeme += c;
                        currentState = 7;
                    } else if (c.matches("[0-9]")) {
                        lexeme += c;
                        currentState = 9;
                    } else {
                        currentState = 2;
                        read--;
                        token = semanticActionForConstants(lexeme, "constant");
                        token.setType("INTEGER");
                    }
                    break;
                case 7:
                    if (c.matches("[0-9]|[a-f]")) {
                        lexeme += c;
                        currentState = 8;
                    } else {
                        throw new Error(this.linesRead + ":lexema nao identificado [" + (lexeme + c) + "]");
                    }
                    break;
                case 8:
                    if (c.matches("[0-9]|[a-f]")) {
                        lexeme += c;
                        currentState = 2;
                        token = semanticActionForConstants(lexeme, "constant");
                        token.setType("CHAR");
                    } else {
                        throw new Error(this.linesRead + ":lexema nao identificado [" + (lexeme + c) + "]");
                    }
                    break;
                case 9:
                    if (c.matches("[0-9]")) {
                        lexeme += c;
                        currentState = 9;
                    } else {
                        currentState = 2;
                        read--;
                        token = semanticActionForConstants(lexeme, "constant");
                        token.setType("INTEGER");
                    }
                    break;
                case 10:
                    if (c.matches("\\*")) {
                        lexeme = "";
                        currentState = 11;
                    } else {
                        currentState = 2;
                        read--;
                        token = semanticAction(lexeme);
                    }
                    break;
                case 11:
                    if (c.matches("\\*")) {
                        currentState = 12;
                    } else {
                        currentState = 11;
                    }
                    break;
                case 12:
                    if (c.matches("/")) {
                        currentState = 0;
                    } else {
                        currentState = 11;
                    }
                    break;
                case 13:
                    if (c.matches("=")) {
                        lexeme += c;
                        token = semanticAction(lexeme);
                        currentState = 2;
                    } else {
                        token = semanticAction(lexeme);
                        read--;
                        currentState = 2;
                    }
                    break;
                case 14:
                    if (c.matches("[=|>]")) {
                        lexeme += c;
                        currentState = 2;
                        token = semanticAction(lexeme);
                    } else {
                        currentState = 2;
                        token = semanticAction(lexeme);
                        read--;
                    }
                    break;
                case 15:
                    if (c.matches("\\\"")) {
                        lexeme += c;
                        currentState = 2;
                        token = semanticActionForConstants(lexeme, "constant");
                        token.setType("STRING");
                    } else if (c.matches(AVAILABLE_CHARACTERS)) {
                        lexeme += c;
                        currentState = 15;
                    } else {
                        throw new Error(this.linesRead + ":lexema nao identificado [" + (lexeme + c) + "]");
                    }
                    break;
                default:
                    return token;
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        if (token != null)
            log(linesRead + ":Token Lido: " + token.getLexeme());
        return token;
    }

    /**
     * This Method is implemented to execute every semantic action the lexical
     * analyzer requires, later it can receive as a parameter an int to identify
     * which semantic action should be done and categorized with a switch case.
     * 
     * @param lexeme pattern
     * @return token
     */
    private Symbol semanticAction(String lexeme) {
        Symbol tok, p;
        tok = symbolTable.searchLexeme(lexeme);
        if (tok == null) {
            p = symbolTable.insertToken(lexeme);
        } else {
            p = tok;
        }
        return p;
    }

    private Symbol semanticActionForConstants(String lexeme, String token) {
        return new Symbol(token, lexeme, null);
    }

    /**
     * Debugging method used to log messages to standard output.
     * 
     * @param msg message
     */
    public void log(String msg) {
        if (this.logEnabled) {
            System.out.println(new Date().toString() + " >> LEX " + msg);
        }
    }

    public long getRead() {
        return read;
    }

    public int getLinesRead() {
        return this.linesRead;
    }

}
