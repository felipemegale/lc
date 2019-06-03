import java.util.Date;

/**
 * Pontificia Universidade Catolica de Minas Gerais Intituto de Ciencias Exatas
 * e Informatica - Departamento de Ciencia da Computacao Disciplina de
 * Compiladores - L Language Compiler
 * 
 * @author: Alexandre Abreu, Felipe Megale, João Castro
 */

public class SyntacticAnalyzer {
    String token;
    Symbol lexicalRegister;

    LexicalAnalyzer lexicalAnalyzer;
    boolean logEnabled;

    public SyntacticAnalyzer(LexicalAnalyzer lexicalAnalyzer) {
        this.lexicalAnalyzer = lexicalAnalyzer;
        logEnabled = true;
    }

    // S -> { Declarações }* { Comando }* [EOF(?)]
    public void procedure_S() {
        while (token.equals("var") || token.equals("const")) {
            procedure_Statemants();
        }
        // token.matches("(id)|(for)|(if)|(;)|(readln)|(write | writeln)|(writeln)")
        while (token.equals("id") || token.equals("for") || token.equals("if") || token.equals(";")
                || token.equals("readln") || token.equals("write") || token.equals("writeln")) {
            procedure_Command();
        }
        matchToken("EOF");
    }

    ///////////////////////////////////////////////////// Statements \\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\

    /**
     * Procedure for Statements grammar
     * Declarações -> "Var""" {ListaId}+ | "const" id "=" [ - ] valor ";"
     * With Semantic Actions
     * Declarações -> "var" {ListaId}+ | "const" "id"<U1> "="<C1>[<C2> "-" ] "valor" <T5><G>";"
     */
    public void procedure_Statemants() {
        boolean cond = false;
        Symbol id;
        log(">> SYN (Statemants)");
        if (token.equals("var")) {
            matchToken("var");
            Symbol listIds = new Symbol(null, "listIds");
            procedure__ListIDs(listIds);
            while (token.equals("integer") || token.equals("char")) {
                Symbol listIds1 = new Symbol(null, "listIds");
                procedure__ListIDs(listIds1);
            }
        } else if (token.equals("const")) {
            matchToken("const");
            id = this.lexicalRegister;
            if ((id.get_Class()).equals("")) {
                id.set_Class("CLASSE-CONST");
                id = updateLexicalRegister(id);
            } else {
                throw new Error(
                        this.lexicalAnalyzer.getLinesRead() + ":identificador ja declarado [" + id.getLexeme() + "].");
            }
            matchToken("id");
            matchToken("=");
            cond = false;
            if (token.equals("-")) {
                cond = true;
                matchToken("-");
                // Valor
                if (cond) { // T5
                    if (!this.lexicalRegister.getType().equals("INTEGER")) {
                        throw new Error(this.lexicalAnalyzer.getLinesRead() + ":tipos incompatíveis.");
                    } else {
                        id.setType("INTEGER");
                        id = updateLexicalRegister(id);
                    }
                }
                matchToken("constant");
                matchToken(";");
            } else {
                if (this.lexicalRegister.getType().equals("STRING")) {
                    throw new Error(this.lexicalAnalyzer.getLinesRead() + ":tipos incompatíveis.");
                } else {
                    id.setType(this.lexicalRegister.getType());
                    id = updateLexicalRegister(id);
                }
                matchToken("constant");
                matchToken(";");
            }
            log("Teste semantico: Id " + id);
        }
    }

    /**
     * Procedure for ListIds grammar
     * ListaId -> ("integer" | "char" ) id [ "[" tam "]" | "=" [ - ] valor ] { "," id  [ValVet] }*;
     * With Semantic Actions
     * ListaId -> <C1>("integer" <C2>| "char" )  "id"<U1><T1> <C1>[<C2>ValVet <T4>] { "," "id" <U1><T1> <C1>[<C2> ValVet <T4>] }* ;
     * @param listIds
     */
    public void procedure__ListIDs(Symbol listIds) {
        boolean cond = false;
        Symbol id, id1;
        if (token.equals("integer")) {
            cond = true;
            matchToken("integer");
        } else {
            matchToken("char");
        }
        id = this.lexicalRegister;
        if ((id.get_Class()).equals("")) {// <U1> <T1>
            id.set_Class("CLASSE-VAR");
            if (cond) id.setType("INTEGER"); else id.setType("CHAR");
            id = updateLexicalRegister(id);
        } else {
            throw new Error(
                    this.lexicalAnalyzer.getLinesRead() + ":identificador ja declarado [" + id.getLexeme() + "].");
        }
        matchToken("id");
        procedure_ValueVector();
        while (token.equals(",")) {
            matchToken(",");
            id1 = this.lexicalRegister;
            if ((id1.get_Class()).equals("")) {
                id1.set_Class("CLASSE-VAR");
                if (cond) id1.setType("INTEGER"); else id1.setType("CHAR");
                id1 = updateLexicalRegister(id1);
            } else {
                throw new Error(
                        this.lexicalAnalyzer.getLinesRead() + ":identificador ja declarado [" + id1.getLexeme() + "].");
            }
            matchToken("id");
            procedure_ValueVector();
            log("Teste semantico: Var ID1 " + id1);
        }
        matchToken(";");
        log("Teste semantico: Var ID " + id);
    }

    /**
     * Procedure for ValVet grammar
     * ValVet -> "[" "tam" "]"  |   "=" [ "-" ] "valor"
     * With Semantic Actions
     * ValVet -> "[" "tam" <T2> "]"  |   "=" <C1>[<C2> "-" ] "valor"<T3>
     */
    public void procedure_ValueVector() {
        boolean cond = false;
        if (token.equals("[")) {
            matchToken("[");
            matchToken("constant");
            matchToken("]");
        } else if (token.equals("=")) {
            matchToken("=");
            if (token.equals("-")) {
                matchToken("-");
                matchToken("constant");
            } else {
                matchToken("constant");
            }
        }
    }

    ////////////////////////////////////////////////////// Commands
    ////////////////////////////////////////////////////// \\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\

    /**
     * Procedure for Comando grammar
     * Comando -> Atribuição | Repetição | Teste | ";" | Escrita |  Leitura
     * With Semantic Actions
     * Comando -> Atribuição | Repetição | Teste | ";" | Escrita |  Leitura
     */
    public void procedure_Command() {
        log(">> SYN (Command)");
        if (token.equals("id")) {
            procedure_Assigment();
        } else if (token.equals("for")) {
            procedure_Loop();
        } else if (token.equals("if")) {
            procedure_Condition();
        } else if (token.equals(";")) {
            matchToken(";");
        } else if (token.equals("write") || token.equals("writeln")) {
            procedure_Write();
        } else if (token.equals("readln")) {
            procedure_Read();
        }
    }

    /**
     * Procedure for Atribuicao grammar
     * Comando -> Atribuição | Repetição | Teste | ";" | Escrita |  Leitura
     * With Semantic Actions
     * Comando -> Atribuição | Repetição | Teste | ";" | Escrita |  Leitura
     */
    public void procedure_Assigment() {
        matchToken("id");
        if (token.equals("[")) {
            matchToken("[");
            procedure_Expression();
            matchToken("]");
        }
        matchToken("=");
        procedure_Expression();
        matchToken(";");
    }

    /**
     * Procedure for Repeticao grammar
     * Repetição -> "for" "id" "=" Expressão "to" Expressão [ "step" "num" ] "do" ( "{" Comando "}" | Comando )
     * With Semantic Actions
     * Repetição ->  "for" "id"<U2> "=" Expressão <T17> "to" Expressão <C1>[<C2> "step" "num" <T18>] "do"
     */
    public void procedure_Loop() {
        matchToken("for");
        matchToken("id");
        matchToken("=");
        procedure_Expression();
        matchToken("to");
        procedure_Expression();
        if (token.equals("step")) {
            matchToken("step");
            matchToken("num");
        }
        matchToken("do");
        if (token.equals("{")) {
            matchToken("{");
            while (token.equals("id") || token.equals("for") || token.equals("if") || token.equals(";")
                    || token.equals("readln") || token.equals("write") || token.equals("writeln")) {
                procedure_Command();
            }
            matchToken("}");
        } else {
            procedure_Command();
        }
    }

    /**
     * Procedure for Teste grammar
     * Teste -> "if" Expressão "then" ( Comando | "{" { Comando }* "}" ) [ "else" ( Comando |  "{" { Comando }* "}" ) ]
     * With Semantic Actions
     * Teste -> "if" Expressão <T16> "then" ( Comando | "{" { Comando }* "}" ) [ "else" ( Comando |  "{" { Comando }* "}" ) ]
     */
    public void procedure_Condition() {
        matchToken("if");
        procedure_Expression();
        matchToken("then");
        if (token.equals("{")) {
            matchToken("{");
            while (token.equals("id") || token.equals("for") || token.equals("if") || token.equals(";")
                    || token.equals("readln") || token.equals("write") || token.equals("writeln")) {
                procedure_Command();
            }
            matchToken("}");
        } else {
            procedure_Command();
        }
        if (token.equals("else")) {
            matchToken("else");
            if (token.equals("{")) {
                matchToken("{");
                while (token.equals("id") || token.equals("for") || token.equals("if") || token.equals(";")
                        || token.equals("readln") || token.equals("write") || token.equals("writeln")) {
                    procedure_Command();
                }
                matchToken("}");
            } else {
                procedure_Command();
            }
        }
    }

    // Escrita -> "write" "(" Expressões ")" ";" | "writeln" "(" Expressões ")" ";"
    /**
     * Procedure for Escrita grammar
     * Escrita -> "write" "(" Expressões ")" ";" | "writeln" "(" Expressões ")" ";"
     * With Semantic Actions
     * Escrita -> "write" "(" Expressões ")" ";" | "writeln" "(" Expressões ")" ";"
     */
    public void procedure_Write() {
        if (token.equals("write")) {
            matchToken("write");
            matchToken("(");
            procedure_Expressions();
            matchToken(")");
            matchToken(";");
        } else {
            matchToken("writeln");
            matchToken("(");
            procedure_Expressions();
            matchToken(")");
            matchToken(";");
        }
    }

    /**
     * Procedure for Leitura grammar
     * Leitura -> "readln" "(" "id" ")" ";"
     * With Semantic Actions
     * Leitura -> "readln" "(" "id"<U2> ")" ";"
     */
    public void procedure_Read() {
        matchToken("readln");
        matchToken("(");
        matchToken("id");
        matchToken(")");
        matchToken(";");
    }

    //////////////////////////////////////////////////////////// Expressions
    //////////////////////////////////////////////////////////// \\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\
    
    /**
     * Procedure for Expressoes grammar
     * Expressões -> Expressão { "," Expressões }*
     * With Semantic Actions
     * Expressões -> Expressão { "," Expressões }*
     */
    public void procedure_Expressions() {
        procedure_Expression();
        if (token.equals(",")) {
            matchToken(",");
            procedure_Expressions();
        }
    }

    /**
     * Procedure for Expressao grammar
     * Expressão -> ExpressãoS [ ("=" | "<>" | "<" | ">" | "<=" | ">=") ExpressãoS ] 
     * With Semantic Actions
     * Expressão -> ExpressãoS<T9> [ ("=" <C4>| "<>"<C5> | "<" <C6>| ">"<C7> | "<=" <C8>| ">=") ExpressãoS¹ <T15>] 
     */
    public void procedure_Expression() {
        procedure_Expression_S();
        if (token.equals("=")) {
            matchToken("=");
            procedure_Expression_S();
        } else if (token.equals("<>")) {
            matchToken("<>");
            procedure_Expression_S();
        } else if (token.equals("<")) {
            matchToken("<");
            procedure_Expression_S();
        } else if (token.equals(">")) {
            matchToken(">");
            procedure_Expression_S();
        } else if (token.equals("<=")) {
            matchToken("<=");
            procedure_Expression_S();
        } else if (token.equals(">=")) {
            matchToken(">=");
            procedure_Expression_S();
        }
    }

    /**
     * Procedure for ExpressaoS grammar
     * ExpressãoS -> ["+" | "-"] Termo {("+" | "-" | "or") Termo}*
     * With Semantic Actions
     * ExpressãoS -> [<C9>"+" |<C10> "-"] Termo <T13> {("+"<C9> | "-"<C10> | "or") Termo¹ <T14>}*
     */
    public void procedure_Expression_S() {
        if (token.equals("+")) {
            matchToken("+");
        } else if (token.equals("-")) {
            matchToken("-");
        }
        procedure_Term();
        while (token.equals("+") || token.equals("-") || token.equals("or")) {
            if (token.equals("+")) {
                matchToken("+");
            } else if (token.equals("-")) {
                matchToken("-");
            } else {
                matchToken("or");
            }
            procedure_Term();
        }
    }

    /**
     * Procedure for Termo grammar
     * Termo -> Fator { ( "*" | "/" | "%" | "and" ) Fator }*
     * With Semantic Actions
     * Termo -> Fator<T11> { ( "*"<C11> | "/" <C12>| "%" <C13>| "and" ) Fator¹<T12> }*
     */
    public void procedure_Term() {
        procedure_Factor();
        while (token.equals("*") || token.equals("/") || token.equals("%") || token.equals("and")) {
            if (token.equals("*")) {
                matchToken("*");
            } else if (token.equals("/")) {
                matchToken("/");
            } else if (token.equals("%")) {
                matchToken("%");
            } else {
                matchToken("and");
            }
            procedure_Factor();
        }
    }

    /**
     * Procedure for Fator grammar
     * Fator -> "not" Fator | "(" Expressão ")" | constante | id [ "[" Expressão "]" ]
     * With Semantic Actions
     * Fator -> "not" Fator¹ <T10> | "(" Expressão <T8> ")" | "valor"<T7> | id <U2> <C1>[<C2> "[" Expressão "]" ]<T6>
     */
    public void procedure_Factor() {
        boolean cond = false;
        if (token.equals("not")) {
            matchToken("not");
            procedure_Factor();
        } else if (token.equals("(")) {
            matchToken("(");
            procedure_Expression();
            matchToken(")");
        } else if (token.equals("constant")) {
            matchToken("constant");
        } else {
            matchToken("id");
            if (token.equals("[")) {
                matchToken("[");
                procedure_Expression();
                matchToken("]");
            }
        }
    }

    public void matchToken(String tok) {
        log(this.token + " == " + tok);
        if (this.token.equals(tok)) {
            this.lexicalRegister = this.lexicalAnalyzer.lexicalAnalysis();
            this.token = this.lexicalRegister.getToken();
            log("new token: " + this.token);
        } else {
            if (this.token.equals("EOF")) {
                throw new Error(this.lexicalAnalyzer.getLinesRead() + ":fim de arquivo nao esperado.");
            } else {
                throw new Error(this.lexicalAnalyzer.getLinesRead() + ":token nao esperado [" + (this.token) + "]");
            }
        }
    }

    public Symbol updateLexicalRegister(Symbol register) {
        Symbol updatedLexicalRegister;
        updatedLexicalRegister = this.lexicalAnalyzer.symbolTable.updateLexicalRegister(register);
        if (updatedLexicalRegister == null) {
            log(" ERRO INTERNO. updateLexicalRegister " + register);
        }
        return updatedLexicalRegister;
    }

    /**
     * Debugging method used to log messages to standard output.
     * 
     * @param msg message
     */
    public void log(String msg) {
        if (this.logEnabled) {
            System.out.println(new Date().toString() + " >> SYN " + msg);
        }
    }

}
