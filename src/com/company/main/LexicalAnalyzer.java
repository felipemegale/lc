package com.company.main;

/*
/\b((([A-Z])+)((\w|\.)*))|(((\.|_)+)(([A-Z])|\d)((\w|\.)*))/gi --> variable names
/0x([A-F]|[0-9]){2}\b/gi --> hexadecimal characters
 */

import java.io.*;
import java.util.Date;

public class LexicalAnalyzer {


//    private static final String VARIABLE_PATTERN = "\\b((([a-zA-Z])+)((([a-zA-Z])|\\.|_|[0-9])*))|(((\\.|_)+)(([a-zA-Z])|[0-9])((([a-zA-Z])|\\.|_|[0-9])*))";
//    private static final String HEXADECIMAL_PATTERN = "0x([A-F]|[0-9]){2}\\b";
    private static final String AVAILABLE_CHARACTERS = "[a-zA-z0-9\\s_\\.,;\\\"&\\*:\\(\\)\\[\\]{}+\\-\"\\'/%\\^@!?><=\"\\n\\r\\t]";
    private SymbolTable symbolTable;
    private boolean logEnabled;

    public long getRead() {
        return read;
    }

    private long read;
    private SyntacticAnalyzer syntacticAnalyzer;

    public LexicalAnalyzer() {
        this.symbolTable = new SymbolTable();
        this.logEnabled = true;
        this.read = 0;
        this.syntacticAnalyzer = new SyntacticAnalyzer();
    }

    /**
     * A Quick check if the read character belongs to the language alphabet
     * @param lexeme Read Character
     * @return true or false
     */
    public boolean isLexemeValid(String lexeme) {
        return lexeme.matches(AVAILABLE_CHARACTERS);
    }


    /**
     * Implementation of a Lexical Analyzer using a Finite State Machine to verify a language syntax.
     * @param sourceCode File containing the source code
     * @return a boolean result (err = false, ok = true)
     */
    public boolean lexicalAnalysis(File sourceCode){
        int initialState = 0;
        int finalState = 2;
        int currentState = initialState;
        int status;
        String c;
        String lexeme = "";
        Byte token = 0;

        try {
            BufferedReader src = new BufferedReader(new FileReader(sourceCode));
            src.skip(this.read);
            System.out.println("Skipping " + this.read + " chars");
            while(currentState != finalState){
                if((status = src.read()) != -1){
                    c = (((char) status) + "").toLowerCase();
                    read++;
                    log("Char Lido: " + c.toString());
                    log("Char Lido: " + status);
                    if(!isLexemeValid(c)){
                        log("Invalid character read");
                        return false; //Return to main function to a compile failed message be called.
                    }
                }else{
                    log("EOF");
                    return false;
                }
                // Implementation of a Finite State Machine using switch case tests
                //to implement the lexical analyzer.
                switch(currentState){
                    case 0:// Initial State (0)
                        if(c.matches("[a-z]")){ // Testing Letters to begin an Id and a Reserved Word
                            lexeme += c;
                            currentState = 1;
                        }else if(c.matches("\\.|_")){ // Testing . and _ to begin of an id
                            lexeme += c;
                            currentState = 3;
                        }else if(c.matches("[(|)|;|,|\\+|=|%|{|}|\\[|\\]]")){ // Testing single tokens
                            lexeme += c;
                            currentState = 2;
                            token = semanticAction(lexeme);
                        }else if(c.matches("[0-9]")){
                            lexeme += c;
                            currentState = 6;
                        }else if(c.matches("'")){
                            lexeme += c;
                            currentState = 4;
                        }else if(c.matches(">")){
                            lexeme += c;
                            currentState = 13;
                        }else if(c.matches("<")){
                            lexeme += c;
                            currentState = 14;
                        }else if(c.matches("/")){
                            lexeme += c;
                            currentState = 10;
                        } else if(c.matches("\\\"")){
                            lexeme += c;
                            currentState = 15;
                        } else if(!c.matches("[\\n\\r\\t\\s]")){
                            return false;
                        }
                        break;
                    case 1:
                        if(c.matches("([a-z])|\\.|_|([0-9])")){
                            lexeme += c;
                            currentState = 1;
                        }else{
                            currentState = 2;
                            token = semanticAction(lexeme);
                            read--; //returning last read char
                        }
                        break;
                    case 3:
                        if(c.matches("\\.|_")){
                            lexeme += c;
                            currentState = 3;
                        }else if(c.matches("[a-z]|[0-9]")){
                            lexeme += c;
                            currentState = 1;
                        }else {
                            return false;
                        }
                        break;
                    case 4:
                        if(c.matches("'")){
                            lexeme += c;
                            currentState = 2;
                        }else if(c.matches(AVAILABLE_CHARACTERS)) {
                            lexeme += c;
                            currentState = 5;
                        }else{
                            return false;
                        }
                        break;
                    case 5:
                        if(c.matches("'")){
                            lexeme += c;
                            currentState = 2;
                            token = semanticAction("const");
                        }else{
                            return false;
                        }
                        break;
                    case 6:
                        if(c.matches("x")){
                            lexeme += c;
                            currentState = 7;
                            log("TIPO = hexadecimal");
                        }else if(c.matches("[0-9]")){
                            lexeme += c;
                            currentState = 9;
                            log("TIPO = Integer");
                        }else{
                            currentState = 2;
                            read--;
                            token = semanticAction("const");
                        }
                        break;
                    case 7:
                        if(c.matches("[a-z]|[0-9]")){
                            lexeme += c;
                            currentState = 8;
                        }else{
                            return false;
                        }
                        break;
                    case 8:
                        if(c.matches("[a-z]|[0-9]")){
                            lexeme += c;
                            currentState = 2;
                            token = semanticAction("const");
                        }else{
                            return false;
                        }
                        break;
                    case 9:
                        if(c.matches("[0-9]")){
                            lexeme += c;
                            currentState = 9;
                        }else{
                            currentState = 2;
                            read--;
                            token = semanticAction("const");
                        }
                        break;
                    case 10:
                        if(c.matches("\\*")){
                            lexeme = "";
                            currentState = 11;
                        }else{
                            currentState = 2;
                            read--;
                            token = semanticAction(lexeme);
                        }
                        break;
                    case 11:
                        if(c.matches("\\*")){
                            currentState = 12;
                        }else{
                            currentState = 11;
                        }
                        break;
                    case 12:
                        if(c.matches("/")){
                            currentState = 0;
                        }else{
                            currentState = 11;
                        }
                        break;
                    case 13:
                        if(c.matches("=")){
                            lexeme += c;
                            token = semanticAction(lexeme);
                            currentState = 2;
                        }else{
                            token = semanticAction(lexeme);
                            read--;
                            currentState = 2;
                        }
                        break;
                    case 14:
                        if(c.matches("[=|>]")){
                            lexeme += c;
                            currentState = 2;
                            token = semanticAction(lexeme);
                        }else{
                            currentState = 2;
                            token = semanticAction(lexeme);
                            read--;
                        }
                        break;
                    case 15:
                        if(c.matches("\\\"")){
                            lexeme += c;
                            currentState = 2;
                            token = semanticAction("const");
                        }else if(c.matches(AVAILABLE_CHARACTERS)){
                            lexeme += c;
                            currentState = 15;
                        }else{
                            return false;
                        }
                        break;
                    default: return false;
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        log("Token Encontrado: " + token);
        return true;
    }


    /**
     * This Method is implemented to execute every semantic action the lexical analyzer requires,
     * later it can receive as a parameter an int to identify which semantic action should be done
     * and categorized with a switch case.
     * @param lexeme pattern
     * @return token
     */
    private Byte semanticAction(String lexeme){
        Byte tok, pointer;
        pointer = symbolTable.searchLexeme(lexeme);
        if(pointer == null){
            tok = symbolTable.insertToken(lexeme);
        }else{
            tok = pointer;
        }
        return tok;
    }

    /**
     * Debugging method used to log messages to standard output.
     * @param msg message
     */
    public void log(String msg){
        if(this.logEnabled){
            System.out.println(new Date().toString() + " >> " + msg);
        }
    }
}
