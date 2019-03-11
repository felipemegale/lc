package com.company.main;

/*
/\b((([A-Z])+)((\w|\.)*))|(((\.|_)+)(([A-Z])|\d)((\w|\.)*))/gi --> variable names
/0x([A-F]|[0-9]){2}\b/gi --> hexadecimal characters
 */

import java.io.*;
import java.util.Date;

public class LexicalAnalyzer {

    private static final String VARIABLE_PATTERN = "\\b((([a-zA-Z])+)((([a-zA-Z])|\\.|_|[0-9])*))|(((\\.|_)+)(([a-zA-Z])|[0-9])((([a-zA-Z])|\\.|_|[0-9])*))";
    private static final String HEXADECIMAL_PATTERN = "0x([A-F]|[0-9]){2}\\b";
    private static final String AVAILABLE_CHARACTERS = "[a-zA-z0-9\\s_\\.,;&:\\(\\)\\[\\]{}+\\-\"\\'/%\\^@!?><=\\n\\r\\t]";
    SymbolTable symbolTable;
    private boolean logEnabled;

    public LexicalAnalyzer() {
        symbolTable = new SymbolTable();
        logEnabled = true;
    }

    public boolean isLexemeValid(String lexeme) {
        return lexeme.matches(AVAILABLE_CHARACTERS);
    }

    public boolean lexicalAnalysis(File sourceCode){
        int initialState = 0;
        int finalState = 2;
        int currentState = initialState;
        int status;
        String c;
        String lexeme = "";
        Byte pointer, token;

        try {
            BufferedReader src = new BufferedReader(new FileReader(sourceCode));
            while(currentState != finalState){ //TODO, Ele está lendo até o estado final e parando de ler o arquivo. (FIX)
                if((status = src.read()) != -1){
                    c = ((char) status) + ""; //TODO, Está como String porq os lexemas já testam
                    log("Char Lido: " + c);
                    if(!isLexemeValid(c))
                        return false; //Return to main function to a compile failed message be called.
                }else{
                    return false; //TODO: EOF em C ?
                }
                switch(currentState){
                    case 0:// Initial State (0)
                        if(c.matches("[a-z]")){
                            lexeme += c;
                            currentState = 1;
                        }else if(c.matches("\\.|_")){
                            lexeme += c;
                            currentState = 3;
                        }else{
                            return false;
                        }
                        //TODO: Implementar Restante...
                        break;
                    case 1:
                        if(c.matches("([a-z])|\\.|_|([0-9])")){
                            lexeme += c;
                            currentState = 1;
                        }else{
                            currentState = 2;
                            pointer = symbolTable.searchLexeme(lexeme);
                            if(pointer == null){
                                token = symbolTable.insertToken(lexeme);
                            }else{
                                token = pointer;
                                //TODO: Devolver C ?
                            }
                        }
                        break;
                    case 3:
                        if(c.matches("\\.|_")){
                            lexeme += c;
                            currentState = 3;
                        }else if(c.matches("[a-z]|[0-9]")){
                            lexeme += c;
                            currentState = 1;
                        }else{ //TODO, Sempre Colocar else para casos de erro ?
                            return false;
                        }
                        break;
                    case 4:
                        break;
                    case 5:
                        break;
                    case 6:
                        break;
                    case 7:
                        break;
                    case 8:
                        break;
                    case 9:
                        break;
                    case 10:
                        break;
                    case 11:
                        break;
                    case 12:
                        break;
                    case 13:
                        break;
                    case 14:
                        break;
                    default: return false; // ERRO
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return true;
    }

    public void log(String msg){
        if(this.logEnabled){
            System.out.println(new Date().toString() + " >> " + msg);
        }
    }
}
