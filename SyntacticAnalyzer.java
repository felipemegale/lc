import java.util.Date;
import java.io.FileWriter;
import java.io.IOException;

/**
 * Pontificia Universidade Catolica de Minas Gerais Intituto de Ciencias Exatas
 * e Informatica - Departamento de Ciencia da Computacao Disciplina de
 * Compiladores - L Language Compiler
 * 
 * @author: Alexandre Abreu, Felipe Megale, João Castro
 */

public class SyntacticAnalyzer {
    Error error;
    String token;
    Symbol lexicalRegister;

    LexicalAnalyzer lexicalAnalyzer;
    boolean logEnabled;

    FileWriter codeWriter;

    public SyntacticAnalyzer(LexicalAnalyzer lexicalAnalyzer, FileWriter codeWriter) {
        this.lexicalAnalyzer = lexicalAnalyzer;
        this.codeWriter = codeWriter;
        logEnabled = true;
    }

    // S -> { Declarações }* { Comando }* [EOF(?)]
    public void procedure_S() {
        try {
            codeWriter.write("sseg segment stack         ; inicio seg pilha\n");
            codeWriter.write("byte 4000h DUP(?)          ; dimensiona pilha\n");
            codeWriter.write("sseg ends                  ; fim seg pilha\n\n");
            while (token.equals("var") || token.equals("const")) {
                procedure_Statemants();
            }
            codeWriter.write("dseg ends                  ; fim seg dados\n\n");
            codeWriter.write("cseg segment public        ; inicio seg codigo\n");
            codeWriter.write("assume CS:cseg, DS:dseg\n");
            codeWriter.write("strt:                      ; inicio do programa\n");
            codeWriter.write("mov ax, dseg\n");
            codeWriter.write("mov ds, ax\n");
            // token.matches("(id)|(for)|(if)|(;)|(readln)|(write | writeln)|(writeln)")
            while (token.equals("id") || token.equals("for") || token.equals("if") || token.equals(";")
                    || token.equals("readln") || token.equals("write") || token.equals("writeln")) {
                procedure_Command();
            }
            matchToken("EOF");
            codeWriter.write("\nmov ah, 4Ch\n");
            codeWriter.write("int 21h\n");
            codeWriter.write("cseg ends                  ; fim seg codigo\n");
            codeWriter.write("end strt                   ; fim programa");
            codeWriter.close();
        } catch (IOException ioe) {
            throw new Error("problema com o arquivo de saida");
        }
    }

    ///////////////////////////////////////////////////// Statements
    ///////////////////////////////////////////////////// \\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\

    /**
     * Procedure for Statements grammar Declarações -> "Var""" {ListaId}+ | "const"
     * id "=" [ - ] valor ";" With Semantic Actions Declarações -> "var" {ListaId}+
     * | "const" "id"<U1> "="<C1>[<C2> "-" ] "valor" <T5><G>";"
     */
    public void procedure_Statemants() {
        try {
            codeWriter.write("dseg segment public        ; inicio seg dados\n");
            codeWriter.write("byte 4000h DUP(?)          ; temporarios\n");
            boolean cond = false;
            Symbol id, value;
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
                matchToken("id");
                semanticActionU1(id); // Acao Semantica Unicidade 1
                matchToken("=");
                if (token.equals("-")) {
                    cond = true;
                    matchToken("-");
                    value = this.lexicalRegister;
                    matchToken("constant");
                    semanticActionT5(cond, id, value); // Acao Semantica Tipo 5 p/ Cond = true
                    matchToken(";");
                } else {
                    value = this.lexicalRegister;
                    matchToken("constant");
                    semanticActionT5(cond, id, value); // Acao Semantica Tipo 5 p/ Cond = false
                    matchToken(";");
                }
            }
        } catch (IOException ioe) {
            throw new Error("problema com o arquivo de saida");
        }
    }

    /**
     * Procedure for ListIds grammar ListaId -> ("integer" | "char" ) id [ValVet] {
     * "," id [ValVet] }*; With Semantic Actions ListaId -> <C1>("integer" <C2>|
     * "char" ) "id"<U1><T1> <C1>[<C2>ValVet <T4>] { "," "id" <U1><T1> <C1>[<C2>
     * ValVet <T4>] }* ;
     * 
     * @param listIds
     */
    public void procedure__ListIDs(Symbol listIds) {
        boolean cond = false;
        boolean condInt = false;
        Symbol id, id1;
        if (token.equals("integer")) {
            condInt = true;
            matchToken("integer");
        } else {
            matchToken("char");
        }
        id = this.lexicalRegister;
        matchToken("id");
        semanticActionU1(id); // Acao Semantica de Unicidade 1
        semanticActionT1(condInt, id); // Acao Semantica de Tipos 1
        cond = false;
        if (token.equals("[") || token.equals("=")) {
            cond = true;
            Symbol valueVector = new Symbol(null, "valueVector");
            procedure_ValueVector(valueVector);
            semanticActionT4(cond, id, valueVector);
        }
        while (token.equals(",")) {
            matchToken(",");
            id1 = this.lexicalRegister;
            matchToken("id");
            semanticActionU1(id1);
            semanticActionT1(condInt, id1);
            cond = false;
            if (token.equals("[") || token.equals("=")) {
                cond = true;
                Symbol valueVector1 = new Symbol(null, "valueVector1");
                procedure_ValueVector(valueVector1);
                semanticActionT4(cond, id, valueVector1);
            }
        }
        matchToken(";");
    }

    /**
     * Procedure for ValVet grammar ValVet -> "[" "tam" "]" | "=" [ "-" ] "valor"
     * With Semantic Actions ValVet -> "[" "tam" <T2> "]" | "=" <C1>[<C2> "-" ]
     * "valor"<T3>
     */
    public void procedure_ValueVector(Symbol valueVector) {
        boolean cond = false;
        Symbol value;
        if (token.equals("[")) {
            matchToken("[");
            value = this.lexicalRegister;
            matchToken("constant");
            semanticActionT2(value, valueVector);
            matchToken("]");
        } else if (token.equals("=")) {
            matchToken("=");
            if (token.equals("-")) {
                cond = true;
                matchToken("-");
                value = this.lexicalRegister;
                matchToken("constant");
            } else {
                value = this.lexicalRegister;
                matchToken("constant");
            }
            semanticActionT3(cond, value, valueVector);
        }
    }

    ////////////////////////////////////////////////////// Commands
    ////////////////////////////////////////////////////// \\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\

    /**
     * Procedure for Comando grammar Comando -> Atribuição | Repetição | Teste | ";"
     * | Escrita | Leitura With Semantic Actions Comando -> Atribuição | Repetição |
     * Teste | ";" | Escrita | Leitura
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
     * Procedure for Atribuicao grammar Comando -> Atribuição | Repetição | Teste |
     * ";" | Escrita | Leitura With Semantic Actions Comando -> Atribuição |
     * Repetição | Teste | ";" | Escrita | Leitura
     */
    public void procedure_Assigment() {
        Symbol id;
        boolean cond = false;
        id = this.lexicalRegister;
        if (id.get_Class().equals("")) {
            error = new Error(
                    this.lexicalAnalyzer.getLinesRead() + ":identificador não declarado [" + id.getLexeme() + "].");
        } else if (id.get_Class().equals("CLASSE-CONST")) {
            error = new Error(this.lexicalAnalyzer.getLinesRead() + ":classe de identificador incompatível ["
                    + id.getLexeme() + "].");
        }
        matchToken("id");
        if (token.equals("[")) {
            cond = true;
            matchToken("[");
            Symbol exp = new Symbol(null, "exp");
            procedure_Expression(exp);
            if (!exp.getType().equals("INTEGER") || id.getSize() > 0) {
                error = new Error(this.lexicalAnalyzer.getLinesRead() + ":tipos incompatíveis.");
            }
            matchToken("]");
        }
        matchToken("=");
        Symbol exp1 = new Symbol(null, "exp1");
        procedure_Expression(exp1);
        if (!exp1.getType().equals(id.getType())) {
            error = new Error(this.lexicalAnalyzer.getLinesRead() + ":tipos incompatíveis.");
        } else if (id.getType().equals("INTEGER") && id.getSize() > 0) {
            error = new Error(this.lexicalAnalyzer.getLinesRead() + ":tipos incompatíveis.");
        }
        matchToken(";");
    }

    /**
     * Procedure for Repeticao grammar Repetição -> "for" "id" "=" Expressão "to"
     * Expressão [ "step" "num" ] "do" ( "{" Comando "}" | Comando ) With Semantic
     * Actions Repetição -> "for" "id"<U2> "=" Expressão <T17> "to" Expressão
     * <C1>[<C2> "step" "num" <T18>] "do"
     */
    public void procedure_Loop() {
        boolean cond = false;
        Symbol id;
        matchToken("for");
        id = this.lexicalRegister;
        if (id.get_Class().equals("")) {
            error = new Error(
                    this.lexicalAnalyzer.getLinesRead() + ":identificador não declarado [" + id.getLexeme() + "].");
        } else if (id.get_Class().equals("CLASSE-CONST")) {
            error = new Error(this.lexicalAnalyzer.getLinesRead() + ":classe de identificador incompatível ["
                    + id.getLexeme() + "].");
        }
        matchToken("id");
        matchToken("=");
        Symbol exp = new Symbol(null, "exp");
        procedure_Expression(exp);
        if (!exp.getType().equals("INTEGER") || !id.getType().equals("INTEGER") || id.getSize() > 0) {
            error = new Error(this.lexicalAnalyzer.getLinesRead() + ":tipos incompatíveis.");
        }
        matchToken("to");
        Symbol exp1 = new Symbol(null, "exp");
        procedure_Expression(exp1);
        if (token.equals("step")) {
            cond = true;
            matchToken("step");
            if (!this.lexicalRegister.getType().equals("INTEGER")) {
                error = new Error(this.lexicalAnalyzer.getLinesRead() + ":tipos incompatíveis.");
            }
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
     * Procedure for Teste grammar Teste -> "if" Expressão "then" ( Comando | "{" {
     * Comando }* "}" ) [ "else" ( Comando | "{" { Comando }* "}" ) ] With Semantic
     * Actions Teste -> "if" Expressão <T16> "then" ( Comando | "{" { Comando }* "}"
     * ) [ "else" ( Comando | "{" { Comando }* "}" ) ]
     */
    public void procedure_Condition() {
        matchToken("if");
        Symbol exp = new Symbol(null, "exp");
        procedure_Expression(exp);
        if (!exp.getType().equals("LOGICAL")) {
            error = new Error(this.lexicalAnalyzer.getLinesRead() + ":tipos incompatíveis.");
        }
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
     * Procedure for Escrita grammar Escrita -> "write" "(" Expressões ")" ";" |
     * "writeln" "(" Expressões ")" ";" With Semantic Actions Escrita -> "write" "("
     * Expressões ")" ";" | "writeln" "(" Expressões ")" ";"
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
     * Procedure for Leitura grammar Leitura -> "readln" "(" "id" ")" ";" With
     * Semantic Actions Leitura -> "readln" "(" "id"<U2> ")" ";"
     */
    public void procedure_Read() {
        Symbol id;
        matchToken("readln");
        matchToken("(");
        id = this.lexicalRegister;
        if (id.get_Class().equals("")) {
            error = new Error(
                    this.lexicalAnalyzer.getLinesRead() + ":identificador não declarado [" + id.getLexeme() + "].");
        } else if (id.get_Class().equals("CLASSE-CONST")) {
            error = new Error(this.lexicalAnalyzer.getLinesRead() + ":classe de identificador incompatível ["
                    + id.getLexeme() + "].");
        }
        matchToken("id");
        matchToken(")");
        matchToken(";");
    }

    //////////////////////////////////////////////////////////// Expressions
    //////////////////////////////////////////////////////////// \\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\

    /**
     * Procedure for Expressoes grammar Expressões -> Expressão { "," Expressões }*
     * With Semantic Actions Expressões -> Expressão { "," Expressões }*
     */
    public void procedure_Expressions() {
        Symbol exp = new Symbol(null, "exp");
        procedure_Expression(exp);
        if (token.equals(",")) {
            matchToken(",");
            procedure_Expressions();
        }
    }

    /**
     * Procedure for Expressao grammar Expressão -> ExpressãoS [ ("=" | "<>" | "<" |
     * ">" | "<=" | ">=") ExpressãoS ] With Semantic Actions Expressão ->
     * ExpressãoS<T9> [ ("=" <C4>| "<>"<C5> | "<" <C6>| ">"<C7> | "<=" <C8>| ">=")
     * ExpressãoS¹ <T15>]
     */
    public void procedure_Expression(Symbol exp) {
        boolean condEquals = false, condDiff = false, condLess = false, condGreater = false, condLessEquals = false;
        Symbol expS = new Symbol(null, "expS");
        procedure_Expression_S(expS);
        exp.setType(expS.getType());
        if (token.equals("=")) {
            condEquals = true;
            condLess = condGreater = condLessEquals = condDiff = false;
            matchToken("=");
            Symbol expS1 = new Symbol(null, "expS1");
            procedure_Expression_S(expS1);
            if (!exp.getType().equals(expS1.getType())) {
                error = new Error(this.lexicalAnalyzer.getLinesRead() + ":tipos incompatíveis.");
            } else {
                exp.setType("LOGICAL");
            }
        } else if (token.equals("<>")) {
            condDiff = true;
            condLess = condGreater = condLessEquals = condEquals = false;
            matchToken("<>");
            Symbol expS1 = new Symbol(null, "expS1");
            procedure_Expression_S(expS1);
            if (!exp.getType().equals(expS1.getType())) {
                error = new Error(this.lexicalAnalyzer.getLinesRead() + ":tipos incompatíveis.");
            } else {
                exp.setType("LOGICAL");
            }
        } else if (token.equals("<")) {
            condLess = true;
            condEquals = condGreater = condLessEquals = condDiff = false;
            matchToken("<");
            Symbol expS1 = new Symbol(null, "expS1");
            procedure_Expression_S(expS1);
            if (!exp.getType().equals(expS1.getType())) {
                error = new Error(this.lexicalAnalyzer.getLinesRead() + ":tipos incompatíveis.");
            } else {
                exp.setType("LOGICAL");
            }
        } else if (token.equals(">")) {
            condGreater = true;
            condLess = condEquals = condLessEquals = condDiff = false;
            matchToken(">");
            Symbol expS1 = new Symbol(null, "expS1");
            procedure_Expression_S(expS1);
            log(" Tipos : " + exp);
            log(" Tipos : " + expS1);
            if (!exp.getType().equals(expS1.getType())) {
                error = new Error(this.lexicalAnalyzer.getLinesRead() + ":tipos incompatíveis.");
            } else {
                exp.setType("LOGICAL");
            }
        } else if (token.equals("<=")) {
            condLessEquals = true;
            condLess = condGreater = condEquals = condDiff = false;
            matchToken("<=");
            Symbol expS1 = new Symbol(null, "expS1");
            procedure_Expression_S(expS1);
            if (!exp.getType().equals(expS1.getType()))
                error = new Error(this.lexicalAnalyzer.getLinesRead() + ":tipos incompatíveis.");
        } else if (token.equals(">=")) {
            condLess = condGreater = condLessEquals = condDiff = condEquals = false;
            matchToken(">=");
            Symbol expS1 = new Symbol(null, "expS1");
            procedure_Expression_S(expS1);
            if (!exp.getType().equals(expS1.getType())) {
                error = new Error(this.lexicalAnalyzer.getLinesRead() + ":tipos incompatíveis.");
            } else {
                exp.setType("LOGICAL");
            }
        }
    }

    /**
     * Procedure for ExpressaoS grammar ExpressãoS -> ["+" | "-"] Termo {("+" | "-"
     * | "or") Termo}* With Semantic Actions ExpressãoS -> [<C9>"+" |<C10> "-"]
     * Termo <T13> {("+"<C9> | "-"<C10> | "or") Termo¹ <T14>}*
     */
    public void procedure_Expression_S(Symbol expS) {
        boolean condSoma = false, condSub = false;
        if (token.equals("+")) {
            condSoma = true;
            matchToken("+");
        } else if (token.equals("-")) {
            condSub = true;
            condSoma = false;
            matchToken("-");
        }
        Symbol term = new Symbol(null, "term");
        procedure_Term(term);
        if (condSoma) {
            if (!term.getType().equals("INTEGER")) {
                error = new Error(this.lexicalAnalyzer.getLinesRead() + ":tipos incompatíveis.");
            } else if (term.getSize() > 0) {
                error = new Error(this.lexicalAnalyzer.getLinesRead() + ":tipos incompatíveis.");
            } else {
                expS.setSize(term.getSize());
                expS.setType("INTEGER");
            }
        } else if (condSub) {
            if (!term.getType().equals("INTEGER")) {
                error = new Error(this.lexicalAnalyzer.getLinesRead() + ":tipos incompatíveis.");
            } else if (term.getSize() > 0) {
                error = new Error(this.lexicalAnalyzer.getLinesRead() + ":tipos incompatíveis.");
            } else {
                expS.setSize(term.getSize());
                expS.setType("INTEGER");
            }
        } else {
            expS.setSize(term.getSize());
            expS.setType(term.getType());
        }
        while (token.equals("+") || token.equals("-") || token.equals("or")) {
            if (token.equals("+")) {
                condSoma = true;
                condSub = false;
                matchToken("+");
            } else if (token.equals("-")) {
                condSoma = false;
                condSub = true;
                matchToken("-");
            } else {
                matchToken("or");
            }
            Symbol term1 = new Symbol(null, "term1");
            procedure_Term(term1);
            if (condSoma) {
                if (!term1.getType().equals("INTEGER")) {
                    error = new Error(this.lexicalAnalyzer.getLinesRead() + ":tipos incompatíveis.");
                } else if (term1.getSize() > 0) {
                    error = new Error(this.lexicalAnalyzer.getLinesRead() + ":tipos incompatíveis.");
                } else {
                    expS.setSize(term1.getSize());
                    expS.setType("INTEGER");
                }
            } else if (condSub) {
                if (!term1.getType().equals("INTEGER")) {
                    error = new Error(this.lexicalAnalyzer.getLinesRead() + ":tipos incompatíveis.");
                } else if (term1.getSize() > 0) {
                    error = new Error(this.lexicalAnalyzer.getLinesRead() + ":tipos incompatíveis.");
                } else {
                    expS.setSize(term1.getSize());
                    expS.setType("INTEGER");
                }
            } else {
                if (!term1.getType().equals("LOGICAL") || !expS.getType().equals("LOGICAL")) {
                    error = new Error(this.lexicalAnalyzer.getLinesRead() + ":tipos incompatíveis.");
                }
            }
        }
    }

    /**
     * Procedure for Termo grammar Termo -> Fator { ( "*" | "/" | "%" | "and" )
     * Fator }* With Semantic Actions Termo -> Fator<T11> { ( "*"<C11> | "/" <C12>|
     * "%" <C13>| "and" ) Fator¹<T12> }*
     */
    public void procedure_Term(Symbol term) {
        boolean condMult = false, condDiv = false, condMod = false;
        Symbol factor = new Symbol(null, "factor");
        procedure_Factor(factor);
        term.setType(factor.getType());
        while (token.equals("*") || token.equals("/") || token.equals("%") || token.equals("and")) {
            if (token.equals("*")) {
                condMult = true;
                condDiv = false;
                condMod = false;
                matchToken("*");
            } else if (token.equals("/")) {
                condMult = false;
                condDiv = true;
                condMod = false;
                matchToken("/");
            } else if (token.equals("%")) {
                condMult = false;
                condDiv = false;
                condMod = true;
                matchToken("%");
            } else {
                matchToken("and");
            }
            Symbol factor1 = new Symbol(null, "factor1");
            procedure_Factor(factor1);
            if (condMult) {
                if (!term.getType().equals("INTEGER") || !factor1.getType().equals("INTEGER")) {
                    error = new Error(this.lexicalAnalyzer.getLinesRead() + ":tipos incompatíveis.");
                } else if (term.getSize() > 0 || factor1.getSize() > 0) {
                    error = new Error(this.lexicalAnalyzer.getLinesRead() + ":tipos incompatíveis.");
                } else {
                    term.setType("INTEGER");
                }
            } else if (condDiv) {
                if (!term.getType().equals("INTEGER") || !factor1.getType().equals("INTEGER")) {
                    error = new Error(this.lexicalAnalyzer.getLinesRead() + ":tipos incompatíveis.");
                } else if (term.getSize() > 0 || factor1.getSize() > 0) {
                    error = new Error(this.lexicalAnalyzer.getLinesRead() + ":tipos incompatíveis.");
                } else {
                    term.setType("INTEGER");
                }
            } else if (condMod) {
                if (!term.getType().equals("INTEGER") || !factor1.getType().equals("INTEGER")) {
                    error = new Error(this.lexicalAnalyzer.getLinesRead() + ":tipos incompatíveis.");
                } else if (term.getSize() > 0 || factor1.getSize() > 0) {
                    error = new Error(this.lexicalAnalyzer.getLinesRead() + ":tipos incompatíveis.");
                } else {
                    term.setType("INTEGER");
                }
            } else {
                if (!term.getType().equals("LOGICAL") || !factor1.getType().equals("LOGICAL")) {
                    error = new Error(this.lexicalAnalyzer.getLinesRead() + ":tipos incompatíveis.");
                }
            }
        }
    }

    /**
     * Procedure for Fator grammar Fator -> "not" Fator | "(" Expressão ")" |
     * constante | id [ "[" Expressão "]" ] With Semantic Actions Fator -> "not"
     * Fator¹ <T10> | "(" Expressão <T8> ")" | "valor"<T7> | id <U3> <C1>[<C2> "["
     * Expressão "]" ]<T6>
     */
    public void procedure_Factor(Symbol factor) {
        boolean cond = false;
        Symbol id;
        if (token.equals("not")) {
            matchToken("not");
            Symbol factor1 = new Symbol(null, "factor1");
            procedure_Factor(factor1);
            if (!factor1.getType().equals("LOGICAL")) {
                error = new Error(this.lexicalAnalyzer.getLinesRead() + ":tipos incompatíveis.");
            }
        } else if (token.equals("(")) {
            matchToken("(");
            Symbol exp = new Symbol(null, "exp");
            procedure_Expression(exp);
            factor.setType(exp.getType());
            matchToken(")");
        } else if (token.equals("constant")) {
            factor.setType(this.lexicalRegister.getType());
            matchToken("constant");
        } else {
            id = this.lexicalRegister;
            if (id.get_Class().equals("")) {
                error = new Error(
                        this.lexicalAnalyzer.getLinesRead() + ":identificador não declarado [" + id.getLexeme() + "].");
            }
            matchToken("id");
            if (token.equals("[")) {
                cond = true;
                matchToken("[");
                Symbol exp1 = new Symbol(null, "exp1");
                procedure_Expression(exp1);
                if (!exp1.getType().equals("INTEGER")) {
                    error = new Error(this.lexicalAnalyzer.getLinesRead() + ":tipos incompatíveis.");
                } else {
                    factor.setType(id.getType());
                }
                matchToken("]");
            }
            if (cond == false) {
                factor.setType(id.getType());
                factor.setSize(id.getSize());
            }
        }
    }

    ////////////////////////////////////////// Semantic Actions
    ////////////////////////////////////////// ////////////////////////////////////////////////////////////////////////////
    /**
     * if id.classe != vazio, ERRO
     */
    public void semanticActionU1(Symbol id) {
        if (!(id.get_Class()).equals("")) {
            throw new Error(
                    this.lexicalAnalyzer.getLinesRead() + ":identificador ja declarado [" + id.getLexeme() + "].");
        }
    }

    /////////////////////// UNICIDADE
    /**
     * if id.classe = vazio, ERRO else if id.classe = classe-const, ERRO
     */
    public void semanticActionU2() {
    }

    /**
     * if id.classe = vazio, ERRO
     */
    public void semanticActionU3() {
    }

    /////////////////// TIPOS
    /**
     * if (cond){ id.tipo = inteiro } else { id.tipo = caractere } id.classe =
     * "CLASSE-VAR"
     */
    public void semanticActionT1(boolean condition, Symbol id) {
        if (condition) {
            id.setType("INTEGER");
        } else {
            id.setType("CHAR");
        }
        id = updateLexicalRegister(id);
        log("Semantic Action T1 -> condition: " + condition + " id: " + id);
    }

    /**
     * if(isNumero(num) == false){ ERRO }else{ valueVector.tam = num.lex }
     */
    public void semanticActionT2(Symbol value, Symbol valueVector) {
        if (!value.getType().equals("INTEGER")) {
            throwError(8, "");
        } else {
            valueVector.setSize(Integer.parseInt(value.getLexeme()));
            valueVector.setType("INTEGER");
        }
        log("Semantic Action T2 -> value: " + value + " valueVector: " + valueVector);
    }

    /**
     * if(cond){ if(isNumero(valor.lex) == false){ ERRO }else{ valueVector.tipo =
     * inteiro; } }else{ if(isNumero(valor.lex) == false){ valueVector.tipo =
     * caractere; }else{ valueVector.tipo = inteiro; } }
     */
    public void semanticActionT3(boolean condition, Symbol value, Symbol valueVector) {
        if (condition) {
            if (!value.getType().equals("INTEGER")) {
                throwError(8, "");
            } else {
                valueVector.setType("INTEGER");
            }
        } else {
            valueVector.setType(value.getType());
        }
        log("Semantic Action T3 -> condition: " + condition + " value: " + value + " valueVector: " + valueVector);
    }

    /**
     * if (valueVector.tam > 0){ if(id.tipo == inteiro){ if(valueVector.tam > 2000){
     * ERRO }else if(valueVector.tipo != id.tipo){ ERRO }else{ id.tam =
     * valueVector.tam } } else{ if(valueVector.tam > 4000){ ERRO }else
     * if(valueVector.tipo != id.tipo){ ERRO }else{ id.tam = valueVector.tam } }
     * }else if(valueVector.tipo != id.tipo){ ERRO }
     */
    public void semanticActionT4(boolean condition, Symbol id, Symbol valueVector) {
        if (valueVector.getSize() > 0) {
            if (id.getType().equals("INTEGER")) {
                if (valueVector.getSize() > 2000) {
                    throwError(9, "");
                } else if (!valueVector.getType().equals(id.getType())) {
                    throwError(8, "");
                } else {
                    id.setSize(valueVector.getSize());
                    id = updateLexicalRegister(id);
                }
            } else {
                if (valueVector.getSize() > 4000) {
                    throwError(9, "");
                } else if (!valueVector.getType().equals(id.getType())) {
                    throwError(8, "");
                } else {
                    id.setSize(valueVector.getSize());
                    id = updateLexicalRegister(id);
                }
            }
        } else if (!valueVector.getType().equals(id.getType())) {
            throwError(8, "");
        }
        log("Semantic Action T4 -> condition: " + condition + " id: " + id + " valueVector: " + valueVector);
    }

    /**
     * if (cond) { if (!isNumerico(valor.lex)) { ERRO } else { id.tipo = inteiro } }
     * else { if (isNumerico(valor.lex)) { id.tipo = inteiro } else { id.tipo =
     * caractere } } id.classe = "classe-const"
     */
    public void semanticActionT5(boolean condition, Symbol id, Symbol value) {
        if (condition) {
            if (!value.getType().equals("INTEGER")) {
                throw new Error(this.lexicalAnalyzer.getLinesRead() + ":tipos incompatíveis.");
            } else {
                id.setType("INTEGER");
                id.setType("CLASSE-CONST");
                id = updateLexicalRegister(id);
            }
        } else {
            if (value.getType().equals("STRING")) {
                throw new Error(this.lexicalAnalyzer.getLinesRead() + ":tipos incompatíveis.");
            } else {
                id.setType(value.getType());
                id.setType("CLASSE-CONST");
                id = updateLexicalRegister(id);
            }
        }
        log("Semantic Action T5 -> condition: " + condition + " id: " + id + " value: " + value);
    }

    public void matchToken(String tok) {
        log(this.token + " == " + tok);
        if (this.token.equals(tok)) {
            this.lexicalRegister = this.lexicalAnalyzer.lexicalAnalysis();
            this.token = this.lexicalRegister.getToken();
            log("new token: " + this.token);
        } else {
            if (this.token.equals("EOF")) {
                throwError(2, "");
            } else {
                throwError(3, this.token);
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

    public void throwError(int errorCode, String lex) {
        switch (errorCode) {
        case 0:
            throw new Error(this.lexicalAnalyzer.getLinesRead() + ":caractere invalido.");
        case 1:
            throw new Error(this.lexicalAnalyzer.getLinesRead() + ":lexema nao identificado [" + lex + "].");
        case 2:
            throw new Error(this.lexicalAnalyzer.getLinesRead() + ":fim de arquivo nao esperado.");
        case 3:
            throw new Error(this.lexicalAnalyzer.getLinesRead() + ":token nao esperado [" + lex + "].");
        case 4:
            throw new Error(this.lexicalAnalyzer.getLinesRead() + ":fim de arquivo não esperado.");
        case 5:
            throw new Error(this.lexicalAnalyzer.getLinesRead() + ":identificador nao declarado [" + lex + "].");
        case 6:
            throw new Error(this.lexicalAnalyzer.getLinesRead() + ":identificador ja declarado [" + lex + "].");
        case 7:
            throw new Error(
                    this.lexicalAnalyzer.getLinesRead() + ":classe de identificador incompatível [" + lex + "].");
        case 8:
            throw new Error(this.lexicalAnalyzer.getLinesRead() + ":tipos incompatíveis.");
        case 9:
            throw new Error(this.lexicalAnalyzer.getLinesRead() + ":tamanho do vetor excede o máximo permitido.");
        }
    }

}
