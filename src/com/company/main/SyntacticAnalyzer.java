package com.company.main;

public class SyntacticAnalyzer {
    String token;
    LexicalAnalyzer lexicalAnalyzer;
    public SyntacticAnalyzer(LexicalAnalyzer lexicalAnalyzer) {
        this.lexicalAnalyzer = lexicalAnalyzer;
    }

    // S -> { Declarações }* { Comando }* [EOF(?)]
    public void procedure_S(){
        procedure_Statemants();
        procedure_Commands();
        if(token.equals("EOF")){
           matchToken(token);
        }
    }


    // Declarações -> “Var” Tipo ListaId “;” | “const” id “=” [ - ] valor “;”
    public void procedure_Statemants(){
        if(token.equals("var")){
            matchToken("var");
            procedure_Type();
            procedure__ListIDs();
            if(token.equals(";")){
                matchToken(";");
            }
        }else if(token.equals("const")){
            matchToken("const");
            if(token.equals("id")){
                matchToken("id");
                if(token.equals("=")){
                    matchToken("=");
                    if(token.equals("-")){
                        matchToken("-");
                        if(token.equals("const")){
                            matchToken("const");
                            if(token.equals(";")){
                                matchToken(";");
                            }
                        }
                    }else{
                        if(token.equals("const")){
                            matchToken("const");
                            if(token.equals(";")){
                                matchToken(";");
                            }
                        }
                    }
                }
            }
        }
    }


    public void procedure_Commands(){}


    public void procedure_Type(){
        if(token.equals("integer")){
            matchToken("integer");
        }else if(token.equals("char")){
            matchToken("char");
        }
    }


    // ListaId -> id ( [ “[” tam ”]” { “,” ListaId }* ] |  [ “=” [ - ] valor ] ) { “,” ListaId }*
    public void procedure__ListIDs(){
        if(token.equals("id")){
            matchToken("id");
            if(token.equals("[")){
                matchToken("[");
                if(token.equals("const")){
                    matchToken("const");
                    if(token.equals("]")){
                        if(token.equals(",")){
                            matchToken(",");
                            procedure__ListIDs();
                        }
                    }
                }
            }else if(token.equals("=")){
                matchToken("=");
                if(token.equals("-")){
                    matchToken("-");
                    if(token.equals("const")){
                        matchToken("const");
                    }
                }else if(token.equals("const")){
                    matchToken("const");
                }
            }
            if(token.equals(",")){
                matchToken(",");
                procedure__ListIDs();
            }
        }
    }

    public void procedure_Assignment(){}
    public void procedure_Loops(){}
    public void procedure_Conditioning(){}
    public void procedure_Null(){}
    public void procedure_Read(){}
    public void procedure_Write(){}

    // Expressões -> Expressão { “,” [ Expressões ] }*
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
            }else if(token.equals("or")){
                matchToken("or");
            }
            procedure_Term();
        }
    }


    //Termo -> Fator { ( “*” | “/” | “%” | “and” ) Fator }*
    public void procedure_Term(){
        procedure_Factor();
        if(token.equals("*")){
            matchToken("*");
            procedure_Factor();
        }else if(token.equals("/")){
            matchToken("/");
            procedure_Factor();
        }else if(token.equals("%")){
            matchToken("%");
            procedure_Factor();
        }else if(token.equals("and")){
            matchToken("and");
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
            matchToken(")"); // TODO, Revisar nos Outros métodos
        }else if(token.equals("const")){
            matchToken("const");
        }else{
            matchToken("id"); // TODO, Revisar nos Outros métodos
            if(token.equals("[")){
                matchToken("[");
                procedure_Expression();
                matchToken("]");
            }
        }
    }


    public void matchToken(String tok){
        if(this.token.equals(tok))
           token = this.lexicalAnalyzer.lexicalAnalysis();
        else
            token = "";
    }





}
