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
    private File sourceCode;
    private Error errors;

    private long read;
    private int linesRead;

    public LexicalAnalyzer(File sourceCode, Error errors) {
        this.symbolTable = new SymbolTable();
        this.sourceCode = sourceCode;
        this.logEnabled = true;
        this.read = 0;
        this.linesRead = 0;
        this.errors = errors;
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
     * @return a boolean result (err = false, ok = true)
     */

    public Symbol lexicalAnalysis(){
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
            System.out.println("Skipping " + this.read + " chars");
            while(currentState != finalState){
                if((status = src.read()) != -1){
                    c = (((char) status) + "").toLowerCase();
                    read++;
                    log("Char Lido: " + c.toString());
//                    log("Char Lido: " + status);
                    if(!isLexemeValid(c)){
                        errors.setError(linesRead, "LEX_INVALIDCHAR");
                        return null; //Return to main function to a compile failed message be called.
                    }
                }else{
                    if(currentState != 0){
                        errors.setError(linesRead, "LEX_EOFNOTEXPECTED");
                        return null;
                    }else{
                        return semanticAction("EOF");
                    }
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
                        }else if(c.matches("0")){
                            lexeme += c;
                            currentState = 6;
                        }else if(c.matches("[1-9]")){
                            log("entrei");
                            lexeme += c;
                            currentState = 9;
                        } else if(c.matches("'")){
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
                        } else if(c.matches("\\n")){
                            linesRead++;
                        }else if(!c.matches("[\\r\\t\\s]")){
                            errors.setError(this.linesRead, "LEX_LEXEMENOTFOUND:" + c);
                            return null;
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
                        }else{
                            errors.setError(this.linesRead, "LEX_LEXEMENOTFOUND:" + lexeme);
                            return null;
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
                            errors.setError(this.linesRead, "LEX_LEXEMENOTFOUND:" + lexeme);
                            return null;
                        }
                        break;
                    case 5:
                        if(c.matches("'")){
                            lexeme += c;
                            currentState = 2;
                            token = semanticAction("constant");
                            token.setType("char");
                        }else{
                            errors.setError(this.linesRead, "LEX_LEXEMENOTFOUND:" + lexeme);
                            return null;
                        }
                        break;
                    case 6:
                        if(c.matches("x")){
                            lexeme += c;
                            currentState = 7;
                        }else if(c.matches("[0-9]")){
                            lexeme += c;
                            currentState = 9;
                        }else{
                            currentState = 2;
                            read--;
                            token = semanticAction("constant");
                            token.setType("INTEGER");
                        }
                        break;
                    case 7:
                        if(c.matches("[0-9]|[a-f]")){
                            lexeme += c;
                            currentState = 8;
                        }else{
                            errors.setError(this.linesRead, "LEX_LEXEMENOTFOUND:" + lexeme);
                            return null;
                        }
                        break;
                    case 8:
                        if(c.matches("[0-9]|[a-f]")){
                            lexeme += c;
                            currentState = 2;
                            token = semanticAction("constant");
                            token.setType("HEXADECIMAL");
                        }else{
                            errors.setError(this.linesRead, "LEX_LEXEMENOTFOUND:" + lexeme);
                            return null;
                        }
                        break;
                    case 9:
                        if(c.matches("[0-9]")){
                            lexeme += c;
                            currentState = 9;
                        }else{
                            currentState = 2;
                            read--;
                            token = semanticAction("constant"); //TODO Setar tipo
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
                            token = semanticAction("constant"); //TODO Setar Tipos
                        }else if(c.matches(AVAILABLE_CHARACTERS)){
                            lexeme += c;
                            currentState = 15;
                        }else{
                            errors.setError(this.linesRead, "LEX_LEXEMENOTFOUND:" + lexeme);
                            return null;
                        }
                        break;
                    default: return token;
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        if(token != null)
            log(linesRead + ":Token Lido: " + token.getLexeme());
        return token;
    }


    /**
     * This Method is implemented to execute every semantic action the lexical analyzer requires,
     * later it can receive as a parameter an int to identify which semantic action should be done
     * and categorized with a switch case.
     * @param lexeme pattern
     * @return token
     */
    private Symbol semanticAction(String lexeme){
        Symbol tok, p;
        if(lexeme.equals("constant")){
            p = new Symbol(null, "constant");
        }else{
            tok = symbolTable.searchLexeme(lexeme);
            if(tok == null){
                p = symbolTable.insertToken(lexeme);
            }else{
                p = tok;
            }
        }
        return p;
    }

    /**
     * Debugging method used to log messages to standard output.
     * @param msg message
     */
    public void log(String msg){
        if(this.logEnabled){
            System.out.println(new Date().toString() + " >> LEX " + msg);
        }
    }

    public long getRead() {
        return read;
    }

    public int getLinesRead(){
        return this.linesRead;
    }

}
