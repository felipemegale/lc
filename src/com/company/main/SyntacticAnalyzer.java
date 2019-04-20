package com.company.main;

import java.util.Date;

public class SyntacticAnalyzer {
    String token;

    LexicalAnalyzer lexicalAnalyzer;
    Error errors;
    boolean logEnabled;
    public SyntacticAnalyzer(LexicalAnalyzer lexicalAnalyzer, Error errors){
        this.lexicalAnalyzer = lexicalAnalyzer;
        this.errors = errors;
        logEnabled = true;
    }

    // S -> { Declarações }* { Comando }* [EOF(?)]
    public void procedure_S(){
        while((token.equals("var") || token.equals("const")) && !errors.checkError()){ //TODO Ideia para parar de pesquisar..
            procedure_Statemants();
        }
        //token.matches("(id)|(for)|(if)|(;)|(readln)|(write | writeln)|(writeln)")
        while(token.equals("id") || token.equals("for") || token.equals("if") ||
                token.equals(";") || token.equals("readln") || token.equals("write") || token.equals("writeln")){
            procedure_Command();
        }
        matchToken("EOF");
    }

    /////////////////////////////////////////////////////Statemants\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\

    // Declarações -> “Var” {ListaId}+ | “const” id “=” [ - ] valor “;”
    public void procedure_Statemants(){
        log(">> SYN (Statemants)");
        if(token.equals("var")){
            matchToken("var");
            procedure__ListIDs();
            while(token.equals("integer") || token.equals("char")){
                procedure__ListIDs();
            }
        }else if(token.equals("const")){
            matchToken("const");
            matchToken("id");
            matchToken("=");
            if(token.equals("-")){
                matchToken("-");
                matchToken("constant");
                matchToken(";");
            }else{
                matchToken("constant");
                matchToken(";");
            }
        }
    }

    // ListaId -> (“integer” | “char” )  id [ “[” tam ”]”  |   “=” [ - ] valor ] { “,” ListaId }* ;
    public void procedure__ListIDs(){
        if(token.equals("integer")){
            matchToken("integer");
        }else{
            matchToken("char");
        }
        matchToken("id");
        procedure_ValueVector();
        while(token.equals(",")){
            matchToken(",");
            matchToken("id");
            procedure_ValueVector();
        }
        matchToken(";");
    }

    public void procedure_ValueVector() {
        if(token.equals("[")){
            matchToken("[");
            matchToken("constant");
            matchToken("]");
        }else if(token.equals("=")){
            matchToken("=");
            if(token.equals("-")){
                matchToken("-");
                matchToken("constant");
            }else{
                matchToken("constant");
            }
        }
    }


    ////////////////////////////////////////////////////// Commands \\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\

    // Comando → Atribuição | Repetição | Teste | “;” | Escrita | Leitura
    public void procedure_Command(){
        log(">> SYN (Command)");
        if(token.equals("id")) {
            procedure_Assigment();
        }
        else if(token.equals("for")) {
            procedure_Loop();
        }else if(token.equals("if")){
            procedure_Condition();
        }else if(token.equals(";")){
            matchToken(";");
        }else if(token.equals("write") || token.equals("writeln")){
            procedure_Write();
        }else if(token.equals("readln")){
            procedure_Read();
        }
    }
    // Atribuição → “id” [ “[” Expressão “]” ] “=” Expressão “;”
    public void procedure_Assigment(){
        matchToken("id");
        if(token.equals("[")){
            matchToken("[");
            procedure_Expression();
            matchToken("]");
        }
        matchToken("=");
        procedure_Expression();
        matchToken(";");
    }

    // Repetição → “for” “id” “=” Expressão “to” Expressão [ “step” “num” ] “do” ( “{“ Comando ”}“ | Comando )
    public void procedure_Loop(){
        matchToken("for");
        matchToken("id");
        matchToken("=");
        procedure_Expression();
        matchToken("to");
        procedure_Expression();
        if(token.equals("step")){
            matchToken("step");
            matchToken("num");
        }
        matchToken("do");
        if(token.equals("{")){
            matchToken("{");
            while(token.equals("id") || token.equals("for") || token.equals("if") ||
                    token.equals(";") || token.equals("readln") || token.equals("write") || token.equals("writeln")){
                procedure_Command();
            }
            matchToken("}");
        }else{
            procedure_Command();
        }
    }
    // “if” Expressão “then” ( Comando | “{“ Comando “}” ) [ “else” ( Comando | “{“ Comando “}” ) ]
    public void procedure_Condition(){
        matchToken("if");
        procedure_Expression();
        matchToken("then");
        if(token.equals("{")){
            matchToken("{");
            while(token.equals("id") || token.equals("for") || token.equals("if") ||
                    token.equals(";") || token.equals("readln") || token.equals("write") || token.equals("writeln")){
                procedure_Command();
            }
            matchToken("}");
        }else{
            procedure_Command();
        }
        if(token.equals("else")){
            matchToken("else");
            if(token.equals("{")){
                matchToken("{");
                while(token.equals("id") || token.equals("for") || token.equals("if") ||
                        token.equals(";") || token.equals("readln") || token.equals("write") || token.equals("writeln")){
                    procedure_Command();
                }
                matchToken("}");
            }else{
                procedure_Command();
            }
        }
    }
    // Escrita → “write” “(“ Expressões “)” “;” | “writeln” “(“ Expressões “)” “;”
    public void procedure_Write(){
        if(token.equals("write")){
            matchToken("write");
            matchToken("(");
            procedure_Expressions();
            matchToken(")");
            matchToken(";");
        }else{
            matchToken("writeln");
            matchToken("(");
            procedure_Expressions();
            matchToken(")");
            matchToken(";");
        }
    }

    // Leitura → “readln” “(“ “id” “)” “;”
    public void procedure_Read(){
        matchToken("readln");
        matchToken("(");
        matchToken("id");
        matchToken(")");
        matchToken(";");
    }


    //////////////////////////////////////////////////////////// Expressions \\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\
    // Expressões -> Expressão { “,” Expressões }*
    public void procedure_Expressions(){
        procedure_Expression();
        if(token.equals(",")){
            matchToken(",");
            procedure_Expressions();
        }
    }

    // Expressão -> ExpressãoS [ (“=” | “<>” | “<” | “>” | “<=” | “>=”) ] ExpressãoS
    public void procedure_Expression(){
        procedure_Expression_S();
        if(token.equals("=")){
            matchToken("=");
            procedure_Expression_S();
        }else if(token.equals("<>")){
            matchToken("<>");
            procedure_Expression_S();
        }else if(token.equals("<")){
            matchToken("<");
            procedure_Expression_S();
        }else if(token.equals(">")){
            matchToken(">");
            procedure_Expression_S();
        }else if(token.equals("<=")){
            matchToken("<=");
            procedure_Expression_S();
        }else if(token.equals(">=")){
            matchToken(">=");
            procedure_Expression_S();
        }
    }

    // ExpressãoS -> [“+” | “-”] Termo {(“+” | “-” | “or”) Termo}*
    public void procedure_Expression_S(){
        if(token.equals("+")){
            matchToken("+");
        }else if(token.equals("-")){
            matchToken("-");
        }
        procedure_Term();
        while(token.equals("+") ||  token.equals("-") || token.equals("or")){
            if(token.equals("+")){
                matchToken("+");
            }else if(token.equals("-")){
                matchToken("-");
            }else{
                matchToken("or");
            }
            procedure_Term();
        }
    }


    //Termo -> Fator { ( “*” | “/” | “%” | “and” ) Fator }*
    public void procedure_Term(){
        procedure_Factor();
        while(token.equals("*") || token.equals("/") || token.equals("%") || token.equals("and")){
            if(token.equals("*")){
                matchToken("*");
            }else if(token.equals("/")){
                matchToken("/");
            }else if(token.equals("%")){
                matchToken("%");
            }else{
                matchToken("and");
            }
            procedure_Factor();
        }
    }

    //    Fator -> “not” Fator | “(“ Expressão “)” | constante | id [ “[“ Expressão ”]” ]
    public void procedure_Factor(){
        if(token.equals("not")){
            matchToken("not");
            procedure_Factor();
        }else if(token.equals("(")){
            matchToken("(");
            procedure_Expression();
            matchToken(")");
        }else if(token.equals("constant")){
            matchToken("constant");
        }else{
            matchToken("id");
            if(token.equals("[")){
                matchToken("[");
                procedure_Expression();
                matchToken("]");
            }
        }
    }


    public void matchToken(String tok){
        log(this.token + " == " + tok);
        if(this.token.equals(tok)){
            this.token = this.lexicalAnalyzer.lexicalAnalysis().getToken();
            log("new token: " + this.token);
        } else {
            if(this.token.equals("EOF")){
                errors.setError(this.lexicalAnalyzer.getLinesRead(), "SYN_EOFNOTEXPECTED");
            }else{
                errors.setError(this.lexicalAnalyzer.getLinesRead(), "SYN_TOKENNOTEXPECTED:" + this.token);
            }
            this.token = null;
        }
    }



    /**
     * Debugging method used to log messages to standard output.
     * @param msg message
     */
    public void log(String msg){
        if(this.logEnabled){
            System.out.println(new Date().toString() + " >> SYN " + msg);
        }
    }





}
