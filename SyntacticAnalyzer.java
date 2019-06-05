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
    int nextAvailableMemoryPosition = 4000; // endereco "base" da memoria de dados
    int label = 0; // contador de rotulo
    int temporary = 0; // contador de temporario

    public SyntacticAnalyzer(LexicalAnalyzer lexicalAnalyzer, FileWriter codeWriter) {
        this.lexicalAnalyzer = lexicalAnalyzer;
        this.codeWriter = codeWriter;
        logEnabled = true;
    }

    // S -> { Declarações }* { Comando }* [EOF(?)]
    public void procedure_S() {
        try {
            // segmento de pilha inicio
            codeWriter.write("sseg segment stack         ; inicio seg pilha\n");
            codeWriter.write("byte 4000h DUP(?)          ; dimensiona pilha\n");
            codeWriter.write("sseg ends                  ; fim seg pilha\n\n");
            // segmento de pilha fim

            // segmento de dados inicio
            codeWriter.write("dseg segment public        ; inicio seg dados\n");
            codeWriter.write("byte 4000h DUP(?)          ; temporarios\n");
            while (token.equals("var") || token.equals("const")) {
                procedure_Statemants();
            }
            codeWriter.write("dseg ends                  ; fim seg dados\n\n");
            // segmento de dados fim

            // segmento de codigo inicio
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
            // segmento de codigo fim
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
                codeGenerationT2(id, value);
                log("id after codegeneration: " + id);
            }
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
        Symbol valueVector = null;
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
            valueVector = new Symbol(null, "valueVector");
            procedure_ValueVector(valueVector);
            semanticActionT4(cond, id, valueVector);
        }
        codeGenerationT1(id, valueVector, cond);

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
                semanticActionT4(cond, id1, valueVector1);
            }
            codeGenerationT1(id1, valueVector, cond);
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
     * Procedure for Atribuicao grammar Atribuição -> "id" [ "[" Expressão "]" ] "="
     * Expressão ";" With Semantic Actions Atribuição -> "id" <C1>[<C2> "["
     * Expressão <T19>"]" ] <U2>"=" Expressão <T20>";"
     */
    public void procedure_Assigment() {
        Symbol id, exp = null;
        boolean cond = false;
        id = this.lexicalRegister;
        matchToken("id");
        semanticActionU2(id);
        if (token.equals("[")) {
            cond = true;
            matchToken("[");
            exp = new Symbol(null, "exp");
            temporary = 0;
            procedure_Expression(exp);
            semanticActionT19(cond, id, exp);
            matchToken("]");
        }
        matchToken("=");
        Symbol exp1 = new Symbol(null, "exp1");
        temporary = 0;
        procedure_Expression(exp1);
        semanticActionT20(cond, id, exp1);
        // codeGenerationT13(cond, id, exp, exp1);
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
        Symbol id, value;
        matchToken("for");
        id = this.lexicalRegister;
        matchToken("id");
        semanticActionU2(id);
        matchToken("=");
        Symbol exp = new Symbol(null, "exp");
        temporary = 0;
        procedure_Expression(exp);
        semanticActionT17(id, exp);
        matchToken("to");
        Symbol exp1 = new Symbol(null, "exp");
        temporary = 0;
        procedure_Expression(exp1);
        if (token.equals("step")) {
            cond = true;
            matchToken("step");
            value = this.lexicalRegister;
            matchToken("num");
            semanticActionT18(value);
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
        String rotFalso = newLabel();
        String rotFim = newLabel();
        temporary = 0;
        procedure_Expression(exp);
        semanticActionT16(exp);
        codeGenerationT14(rotFalso, rotFim, exp);
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
            codeGenerationT15(rotFalso, rotFim);
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
            codeGenerationT16(rotFim);
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
        matchToken("id");
        semanticActionU2(id);
        matchToken(")");
        matchToken(";");
        codeGenerationT17(id);
    }

    //////////////////////////////////////////////////////////// Expressions
    //////////////////////////////////////////////////////////// \\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\

    /**
     * Procedure for Expressoes grammar Expressões -> Expressão { "," Expressões }*
     * With Semantic Actions Expressões -> Expressão { "," Expressões }*
     */
    public void procedure_Expressions() {
        Symbol exp = new Symbol(null, "exp");
        temporary = 0;
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
        semanticActionT9(exp, expS);
        codeGenerationT11(exp, expS);
        if (token.equals("=") || token.equals("<>") || token.equals("<") || token.equals(">") || token.equals("<=")
                || token.equals(">=")) {
            Symbol expS1 = new Symbol(null, "expS1");
            if (token.equals("=")) {
                condEquals = true;
                condLess = condGreater = condLessEquals = condDiff = false;
                matchToken("=");
                procedure_Expression_S(expS1);
            } else if (token.equals("<>")) {
                condDiff = true;
                condLess = condGreater = condLessEquals = condEquals = false;
                matchToken("<>");
                procedure_Expression_S(expS1);
            } else if (token.equals("<")) {
                condLess = true;
                condEquals = condGreater = condLessEquals = condDiff = false;
                matchToken("<");
                procedure_Expression_S(expS1);
            } else if (token.equals(">")) {
                condGreater = true;
                condLess = condEquals = condLessEquals = condDiff = false;
                matchToken(">");
                procedure_Expression_S(expS1);
            } else if (token.equals("<=")) {
                condLessEquals = true;
                condLess = condGreater = condEquals = condDiff = false;
                matchToken("<=");
                procedure_Expression_S(expS1);
            } else if (token.equals(">=")) {
                condLess = condGreater = condLessEquals = condDiff = condEquals = false;
                matchToken(">=");
                procedure_Expression_S(expS1);
            }
            semanticActionT15(condEquals, condDiff, condLess, condGreater, condLessEquals, exp, expS);
            codeGenerationT12(condEquals, condDiff, condLess, condGreater, condLessEquals, exp, expS);
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
        semanticActionT13(condSoma, condSub, expS, term);
        codeGenerationT9(condSoma, condSub, expS, term);
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
            semanticActionT14(condSoma, condSub, term1, expS);
            codeGenerationT10(condSoma, condSub, term1, expS);
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
        semanticActionT11(term, factor);
        codeGenerationT7(term, factor);
        while (token.equals("*") || token.equals("/") || token.equals("%") || token.equals("and")) {
            if (token.equals("*")) {
                condMult = true;
                condDiv = condMod = false;
                matchToken("*");
            } else if (token.equals("/")) {
                condDiv = true;
                condMult = condMod = false;
                matchToken("/");
            } else if (token.equals("%")) {
                condMod = true;
                condMult = condDiv = false;
                matchToken("%");
            } else {
                matchToken("and");
            }
            Symbol factor1 = new Symbol(null, "factor1");
            procedure_Factor(factor1);
            semanticActionT12(condMult, condDiv, condMod, term, factor1);
            codeGenerationT8(condMult, condDiv, condMod, term, factor1);
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
        Symbol id, value, exp1 = null;
        if (token.equals("not")) {
            matchToken("not");
            Symbol factor1 = new Symbol(null, "factor1");
            procedure_Factor(factor1);
            semanticActionT10(factor1);
            codeGenerationT5(factor, factor1);
        } else if (token.equals("(")) {
            matchToken("(");
            Symbol exp = new Symbol(null, "exp");
            procedure_Expression(exp);
            semanticActionT8(factor, exp);
            codeGenerationT4(factor, exp);
            matchToken(")");
        } else if (token.equals("constant")) {
            value = this.lexicalRegister;
            log("value: " + value);
            matchToken("constant");
            semanticActionT7(factor, value);
            codeGenerationT3(factor, value);
        } else {
            id = this.lexicalRegister;
            matchToken("id");
            semanticActionU3(id);
            if (token.equals("[")) {
                cond = true;
                matchToken("[");
                exp1 = new Symbol(null, "exp1");
                procedure_Expression(exp1);
                matchToken("]");
            }
            semanticActionT6(cond, factor, id, exp1);
            codeGenerationT6(cond, factor, id, exp1);
        }
    }

    ////////////////////////////////////////// Semantic Actions
    ////////////////////////////////////////// ////////////////////////////////////////////////////////////////////////////
    /**
     * if id.classe != vazio, ERRO
     */
    public void semanticActionU1(Symbol id) {
        if (!(id.get_Class()).equals("")) {
            throwError(6, id.getLexeme());
        }
    }

    /////////////////////// UNICIDADE
    /**
     * if(id.classe = vazio){ ERRO }else if(id.classe == "CLASSE-CONST"){ ERRO }
     */
    public void semanticActionU2(Symbol id) {
        if (id.get_Class().equals("")) {
            throwError(5, id.getLexeme());
        } else if (id.get_Class().equals("CLASSE-CONST")) {
            throwError(7, id.getLexeme());
        }
    }

    /**
     * if id.classe = vazio, ERRO
     */
    public void semanticActionU3(Symbol id) {
        if (id.get_Class().equals("")) {
            throwError(5, id.getLexeme());
        }
    }

    /////////////////// TIPOS
    /**
     * if (cond){ id.tipo = inteiro } else { id.tipo = caractere } id.classe =
     * "CLASSE-VAR"
     */
    public void semanticActionT1(boolean condition, Symbol id) {
        if (condition) {
            id.setType("INTEGER");
            id.set_Class("CLASSE-VAR");
        } else {
            id.setType("CHAR");
            id.set_Class("CLASSE-VAR");
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
        }
        log("Semantic Action T2 -> value: " + value + " valueVector: " + valueVector);
    }

    /**
     * if(cond){ if(isNumero(valor.le x) == false){ ERRO }else{ valueVector.tipo =
     * inteiro; } }else{ if(isNumero(valor.lex) == false){ valueVector.tipo =
     * caractere; }else{ valueVector.tipo = inteiro; } }
     */
    public void semanticActionT3(boolean condition, Symbol value, Symbol valueVector) {
        if (condition) { // se for atribuicao
            if (!value.getType().equals("INTEGER")) {
                throwError(8, "");
            } else {
                valueVector.setType("INTEGER");
                valueVector.setLexeme(value.getLexeme());
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
                } else {
                    id.setSize(valueVector.getSize());
                    id = updateLexicalRegister(id);
                }
            } else {
                if (valueVector.getSize() > 4000) {
                    throwError(9, "");
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
                throwError(8, "");
            } else {
                id.setType("INTEGER");
                id.set_Class("CLASSE-CONST");
                id = updateLexicalRegister(id);
            }
        } else {
            if (value.getType().equals("STRING")) {
                throwError(8, "");
            } else {
                id.setType(value.getType());
                id.set_Class("CLASSE-CONST");
                id = updateLexicalRegister(id);
            }
        }
        log("Semantic Action T5 -> condition: " + condition + " id: " + id + " value: " + value);
    }

    /**
     * if(cond){ if(Expressão.tipo != inteiro){ ERRO }else{ Fator.tipo = id.tipo
     * Fator.tam = 0 } }else{ Fator.tipo = id.tipo Fator.tam = id.tam }
     */
    public void semanticActionT6(boolean condition, Symbol factor, Symbol id, Symbol exp) {
        if (condition) {
            if (!exp.getType().equals("INTEGER")) {
                throwError(8, "");
            } else {
                factor.setType(id.getType());
                factor.setSize(0);
            }
        } else {
            factor.setType(id.getType());
            factor.setSize(id.getSize());
        }
    }

    /**
     * Fator.tipo = getTipo(valor.lex)
     */
    public void semanticActionT7(Symbol factor, Symbol value) {
        // log("699: " + factor);
        factor.setType(value.getType());
        factor.setLexeme(value.getLexeme());
        // log("702: " + factor);
    }

    /**
     * Fator.tipo = Expressão.tipo
     */
    public void semanticActionT8(Symbol factor, Symbol exp) {
        // log("709: " + factor);
        factor.setType(exp.getType());
        factor.setLexeme(exp.getLexeme());
        // log("712: " + factor);
    }

    /**
     * Expressão.tipo = ExpressãoS.tipo
     */
    public void semanticActionT9(Symbol exp, Symbol expS) {
        // log("719: " + exp);
        exp.setType(expS.getType());
        exp.setLexeme(expS.getLexeme());
        // log("722: " + exp);
    }

    /**
     * if (Fator1.tipo != logico) { ERRO }
     */
    public void semanticActionT10(Symbol factor) {
        if (!factor.getType().equals("LOGICAL")) {
            throwError(8, "");
        }
    }

    /**
     * Termo.tipo = Fator.tipo
     */
    public void semanticActionT11(Symbol term, Symbol factor) {
        // log("738:" + term);
        term.setType(factor.getType());
        term.setLexeme(factor.getLexeme());
        // log("741:" + term);
    }

    /**
     * if (condMult){ if(Termo.tipo != inteiro || Fator¹.tipo != inteiro){ ERRO
     * }else if(Termo.tam > 0 || Fator¹.tam > 0){ ERRO } else { Termo.tipo = inteiro
     * } }else if(condDiv){ if(Termo.tipo != inteiro || Fator¹.tipo != inteiro){
     * ERRO }else if(Termo.tam > 0 || Fator¹.tam > 0){ ERRO } else { Termo.tipo =
     * inteiro } }else if(condMod){ if(Termo.tipo != inteiro || Fator¹.tipo !=
     * inteiro){ ERRO }else if(Termo.tam > 0 || Fator¹.tam > 0){ ERRO } else {
     * Termo.tipo = inteiro } }else{ if (Termo.tipo != logico || Fator¹.tipo !=
     * logico){ ERRO } }
     */
    public void semanticActionT12(boolean condMult, boolean condDiv, boolean condMod, Symbol term, Symbol factor) {
        if (condMult) {
            if (!term.getType().equals("INTEGER") || factor.getType().equals("INTEGER")) {
                throwError(8, "");
            } else if (term.getSize() > 0 || factor.getSize() > 0) {
                throwError(8, "");
            }
        } else if (condDiv) {
            if (!term.getType().equals("INTEGER") || factor.getType().equals("INTEGER")) {
                throwError(8, "");
            } else if (term.getSize() > 0 || factor.getSize() > 0) {
                throwError(8, "");
            }
        } else if (condMod) {
            if (!term.getType().equals("INTEGER") || factor.getType().equals("INTEGER")) {
                throwError(8, "");
            } else if (term.getSize() > 0 || factor.getSize() > 0) {
                throwError(8, "");
            }
        } else if (!term.getType().equals("LOGICAL") || !factor.getType().equals("LOGICAL")) {
            throwError(8, "");
        }
    }

    /**
     * 
     * if(condSoma || condSub){ if(termo.tipo != inteiro){ ERRO }else if(termo.tam >
     * 0){ ERRO }else{ ExpressãoS.tam = Termo.tam ExpressãoS.tipo = inteiro } }else{
     * ExpressãoS.tam = termo.tam ExpressaoS.tipo = termo.tipo }
     */
    public void semanticActionT13(boolean condSoma, boolean condSub, Symbol expS, Symbol term) {
        if (condSoma) {
            if (!term.getType().equals("INTEGER")) {
                throwError(8, "");
            } else if (term.getSize() > 0) {
                throwError(8, "");
            } else {
                expS.setType("INTEGER");
                expS.setLexeme(term.getLexeme());
            }
        } else if (condSub) {
            if (!term.getType().equals("INTEGER")) {
                throwError(8, "");
            } else if (term.getSize() > 0) {
                throwError(8, "");
            } else {
                expS.setType("INTEGER");
                expS.setLexeme(term.getLexeme());
            }
        } else {
            expS.setSize(term.getSize());
            expS.setType(term.getType());
            expS.setLexeme(term.getLexeme());
        }
    }

    /**
     * if(condSoma){ if(term.tipo != inteiro){ ERRO }else if(term.tam > 0){ ERRO
     * }else{ ExpressaoS.tipo = inteiro } }else if(condSub){ if(term.tipo !=
     * inteiro){ ERRO }else if(term.tam > 0){ ERRO }else{ ExpressaoS.tipo = inteiro
     * } }else if(term.tipo != logico || ExpressaoS.tipo != logico){ ERRO }
     */
    public void semanticActionT14(boolean condSoma, boolean condSub, Symbol term, Symbol expS) {
        if (condSoma) {
            if (!term.getType().equals("INTEGER")) {
                throwError(8, "");
            } else if (term.getSize() > 0) {
                throwError(8, "");
            } else {
                expS.setType("INTEGER");
            }
        } else if (condSub) {
            if (!term.getType().equals("INTEGER")) {
                throwError(8, "");
            } else if (term.getSize() > 0) {
                throwError(8, "");
            } else {
                expS.setType("INTEGER");
            }
        } else if (!term.getType().equals("LOGICAL")) {
            throwError(8, "");
        }
    }

    /**
     * 
     * if (condEquals){ if(expS.tipo != exp.tipo){ ERRO }else if(expS.tipo ==
     * inteiro){ if(expS.tam > 0 || exp.tam > 0){ ERRO } } }else if(condDiff){
     * if(expS.tipo == inteiro && exp.tipo == inteiro){ if(expS.tam > 0 || exp.tam >
     * 0){ ERRO } }else{ ERRO } }else if(condLess){ if(expS.tipo == inteiro &&
     * exp.tipo == inteiro){ if(expS.tam > 0 || exp.tam > 0){ ERRO } }else{ ERRO }
     * }else if(condGreater){ if(expS.tipo == inteiro && exp.tipo == inteiro){
     * if(expS.tam > 0 || exp.tam > 0){ ERRO } }else{ ERRO } }else
     * if(condLessEquals){ if(expS.tipo == inteiro && exp.tipo == inteiro){
     * if(expS.tam > 0 || exp.tam > 0){ ERRO } }else{ ERRO } }else{ if(expS.tipo ==
     * inteiro && exp.tipo == inteiro){ if(expS.tam > 0 || exp.tam > 0){ ERRO }
     * }else{ ERRO } } exp.tipo = logico
     */
    public void semanticActionT15(boolean condEquals, boolean condDiff, boolean condLess, boolean condGreater,
            boolean condLessEquals, Symbol exp, Symbol expS) {
        if (condEquals) {
            if (!(expS.getType().equals("CHAR") && expS.getSize() > 0) && (exp.getType().equals("STRING"))) {
                throwError(8, "");
            } else if (!expS.getType().equals(exp.getType())) {
                throwError(8, "");
            } else if (expS.getType().equals("INTEGER")) {
                if (expS.getSize() > 0 || exp.getSize() > 0) {
                    throwError(8, "");
                }
            }
        } else if (condDiff) {
            if (expS.getType().equals("INTEGER") && exp.getType().equals("INTEGER")) {
                if (expS.getSize() > 0 || exp.getSize() > 0) {
                    throwError(8, "");
                }
            } else {
                throwError(8, "");
            }
        } else if (condLess) {
            if (expS.getType().equals("INTEGER") && exp.getType().equals("INTEGER")) {
                if (expS.getSize() > 0 || exp.getSize() > 0) {
                    throwError(8, "");
                }
            } else {
                throwError(8, "");
            }
        } else if (condGreater) {
            if (expS.getType().equals("INTEGER") && exp.getType().equals("INTEGER")) {
                if (expS.getSize() > 0 || exp.getSize() > 0) {
                    throwError(8, "");
                }
            } else {
                throwError(8, "");
            }
        } else if (condLessEquals) {
            if (expS.getType().equals("INTEGER") && exp.getType().equals("INTEGER")) {
                if (expS.getSize() > 0 || exp.getSize() > 0) {
                    throwError(8, "");
                }
            } else {
                throwError(8, "");
            }
        } else {
            if (expS.getType().equals("INTEGER") && exp.getType().equals("INTEGER")) {
                if (expS.getSize() > 0 || exp.getSize() > 0) {
                    throwError(8, "");
                }
            } else {
                throwError(8, "");
            }
        }
        exp.setType("LOGICAL");
    }

    /**
     * if(exp.tipo != logico) { ERRO }
     */
    public void semanticActionT16(Symbol exp) {
        if (!exp.getType().equals("LOGICAL")) {
            throwError(8, "");
        }
    }

    /**
     * if(id.tipo != inteiro || exp.tipo != inteiro){ ERRO }else if(id.tam > 0){
     * ERRO }
     */
    public void semanticActionT17(Symbol id, Symbol exp) {
        if (!id.getType().equals("INTEGER") || !exp.getType().equals("INTEGER")) {
            throwError(8, "");
        } else if (id.getSize() > 0) {
            throwError(8, "");
        }
    }

    /**
     * if(valor.tipo != inteiro) { ERRO }
     */
    public void semanticActionT18(Symbol value) {
        if (!value.getType().equals("INTEGER")) {
            throwError(8, "");
        }
    }

    /**
     * if(cond){ if(exp.tipo != inteiro){ ERRO }else if (exp.tam > 0){ ERRO }else if
     * (id.tam = 0){ ERRO } }
     */
    public void semanticActionT19(boolean cond, Symbol id, Symbol exp) {
        if (cond) {
            if (!exp.getType().equals("INTEGER")) {
                throwError(8, "");
            } else if (exp.getSize() > 0) {
                throwError(8, "");
            } else if (id.getSize() == 0) {
                throwError(8, "");
            }
        }
    }

    /**
     * if(exp.tipo != id.tipo){ ERRO }else if(exp.tipo == inteiro && id.tipo ==
     * inteiro){ if(exp.tam > 0){ ERRO }else if(id.tam > 0 && cond == false){ ERRO }
     * }else{ if(exp.tam + 1 > id.tam){ ERRO } }
     */
    public void semanticActionT20(boolean cond, Symbol id, Symbol exp) {
        log("id: " + id);
        log("exp: " + exp);
        if (!exp.getType().equals(id.getType())) {
            if (!(id.getType().equals("CHAR") && id.getSize() > 0) && (exp.getType().equals("STRING")))
                throwError(8, "");
        } else if (exp.getType().equals("INTEGER") && id.getType().equals("INTEGER")) {
            if (exp.getSize() > 0) {
                throwError(8, "");
            } else if (id.getSize() > 0 && cond == false) {
                throwError(8, "");
            }
        } else {
            if (exp.getSize() + 1 > id.getSize()) {
                throwError(9, "");
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
     * pega o valor atual dos temporarios, incrementa em 1 ou 2, conforme tipo e
     * retorna
     */
    public byte newTemp(String allocType) {
        byte returnValue = (byte) temporary;

        if (allocType.equals("CHAR")) {
            temporary += 1;
        } else if (allocType.equals("INTEGER") || allocType.equals("LOGICAL")) {
            temporary += 2;
        }

        return returnValue;
    }

    /**
     * cria novos rotulos
     */
    public String newLabel() {
        label++;
        return "Rot" + label;
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

    public void writeCode(String code) {
        try {
            codeWriter.write(code);
        } catch (IOException ioe) {
            throw new Error("problema com o arquivo de saida");
        }
    }

    /**
     * se tipo do id = inteiro, escreve sword senao, escreve char. os tamanhos serao
     * definidos na geracao T4
     */
    public void codeGenerationT1(Symbol id, Symbol valueVector, boolean cond) {
        String code = "";
        if (cond) { // se entrou em valueVector
            if (valueVector.getSize() > 0) { // se e' um vetor
                if (id.getType().equals("INTEGER")) {
                    code = "sword " + Integer.toString(valueVector.getSize() * 2) + " DUP(?)         ; "
                            + nextAvailableMemoryPosition + "\n";
                    id.setAddr((byte) nextAvailableMemoryPosition);
                    nextAvailableMemoryPosition += 2 * valueVector.getSize();
                    log("!!! NEXT AVAILABLE MEMORY POSITION !!!" + nextAvailableMemoryPosition);
                } else if (id.getType().equals("CHAR")) {
                    code = "byte " + Integer.toString(valueVector.getSize()) + "h DUP(?)         ; "
                            + nextAvailableMemoryPosition + "\n";
                    id.setAddr((byte) nextAvailableMemoryPosition);
                    nextAvailableMemoryPosition += valueVector.getSize();
                    log("!!! NEXT AVAILABLE MEMORY POSITION !!!" + nextAvailableMemoryPosition);
                }
            } else { // se e' uma atribuicao
                if (id.getType().equals("INTEGER")) {
                    code = "sword " + valueVector.getLexeme() + "         ; " + nextAvailableMemoryPosition + "\n";
                    id.setAddr((byte) nextAvailableMemoryPosition);
                    nextAvailableMemoryPosition += 2;
                    log("!!! NEXT AVAILABLE MEMORY POSITION !!!" + nextAvailableMemoryPosition);
                } else if (id.getType().equals("CHAR")) {
                    code = "sword " + valueVector.getLexeme() + "         ; " + nextAvailableMemoryPosition + "\n";
                    id.setAddr((byte) nextAvailableMemoryPosition);
                    nextAvailableMemoryPosition++;
                    log("!!! NEXT AVAILABLE MEMORY POSITION !!!" + nextAvailableMemoryPosition);
                }
            }
        } else { // se nao for vetor nem atribuicao, ou seja, uma declaracao simples
            if (id.getType().equals("INTEGER")) {
                code = "sword ?          ; endereco atual: " + nextAvailableMemoryPosition + "\n";
                id.setAddr((byte) nextAvailableMemoryPosition);
                nextAvailableMemoryPosition += 2;
                log("!!! NEXT AVAILABLE MEMORY POSITION !!!" + nextAvailableMemoryPosition);
            } else if (id.getType().equals("CHAR")) {
                code = "byte ?         ; endereco atual: " + nextAvailableMemoryPosition + "\n";
                id.setAddr((byte) nextAvailableMemoryPosition);
                nextAvailableMemoryPosition++;
                log("!!! NEXT AVAILABLE MEMORY POSITION !!!" + nextAvailableMemoryPosition);
            }
        }
        writeCode(code);
    }

    /**
     * geracao de declaracao de constantes
     */
    public void codeGenerationT2(Symbol id, Symbol value) {
        String code = "";
        if (id.getSize() == 0) {
            if (id.getType().equals("INTEGER")) {
                code = "sword " + value.getLexeme() + "         ; endereco atual: " + nextAvailableMemoryPosition
                        + "\n";
                id.setAddr((byte) nextAvailableMemoryPosition);
                nextAvailableMemoryPosition += 2;
                log("!!! NEXT AVAILABLE MEMORY POSITION !!!" + nextAvailableMemoryPosition);
            } else if (id.getType().equals("CHAR")) {
                code = "byte " + value.getLexeme() + "         ; endereco atual: " + nextAvailableMemoryPosition + "\n";
                id.setAddr((byte) nextAvailableMemoryPosition);
                nextAvailableMemoryPosition++;
                log("!!! NEXT AVAILABLE MEMORY POSITION !!!" + nextAvailableMemoryPosition);
            }
        }
        writeCode(code);
    }

    /**
     * geracao de codigo para atribuicoes F -> const
     */
    public void codeGenerationT3(Symbol factor, Symbol value) {
        String code = "";

        // se value e' string
        if (value.getType().equals("STRING")) {
            String stringValue = value.getLexeme().substring(0, value.getLexeme().length() - 1) + "$\"";
            // log("1127: " + stringValue);
            code = "dseg segment public\t; Inicio segmento de dados\n" + "byte " + stringValue
                    + "\t; Armazenando valor String\n" + "dseg ENDS\t; Final segmento de dados\n";

            // F.end := contador de dados
            factor.setAddr((byte) nextAvailableMemoryPosition);
            // atualizar contador de dados
            nextAvailableMemoryPosition += stringValue.length() - 2;
        } else {
            factor.setAddr(newTemp(value.getType()));

            if (value.getType().equals("INTEGER")) {
                code = "mov ax, " + value.getLexeme() + " \t; Armazenando valor inteiro em ax\n" + "mov DS:["
                        + String.valueOf(factor.getAddr() & 0xFFF) + "h], ax\t; Salvando em Fator.end\n";
            } else if (value.getType().equals("CHAR")) {
                code = "mov al, " + value.getLexeme().replaceAll("\'", "") + "\t; Armazenando valor char em ax\n"
                        + "mov DS:[" + String.valueOf(factor.getAddr() & 0xFFF) + "h], al\t; Salvando em Fator.end\n";
            }
        }

        writeCode(code);
    }

    /**
     * F -> "(" Exp ")"
     */
    public void codeGenerationT4(Symbol factor, Symbol exp) {
        factor.setAddr(exp.getAddr());
    }

    /**
     * F -> not F1
     */
    public void codeGenerationT5(Symbol factor, Symbol factor1) {
        String code;
        factor.setAddr(newTemp(factor1.getType()));

        code = "mov dx, DS:[" + String.valueOf(factor1.getAddr() & 0xFFF) + "h] \t; Carregando valor em Fator1.end\n"
                + "neg dx\t; fazendo not Fator\n" + "add dx, 1\t; complemento de 1\n" + "mov DS:["
                + String.valueOf(factor.getAddr() & 0xFFF) + "h], dx\t; Carregando valor em Fator.end\n";

        writeCode(code);
    }

    /**
     * F -> id [ "["Exp"]" ]
     */
    public void codeGenerationT6(boolean condition, Symbol factor, Symbol id, Symbol exp1) {
        String code = "";
        if (condition) {
            factor.setAddr(newTemp(id.getType()));

            if (id.getType().equals("INTEGER")) {
                code = "mov BX, DS:[" + String.valueOf(exp1.getAddr() & 0xFFF)
                        + "h]\t; Obtendo valor de Exp1.end(Posicao)\n" + "add BX, BX\t; Inteiro (2Bytes)!\n"
                        + "add BX, " + String.valueOf(id.getAddr() & 0xFFF) + "h\t; Somando Posicao + id.end(inicial)\n"
                        + "mov AX, DS:[BX]\t; Carregando valor na pos correta\n" + "mov DS:["
                        + String.valueOf(factor.getAddr() & 0xFFF) + "h], AX\t; Armazenando em Fator.end\n";
            } else if (id.getType().equals("CHAR")) {
                code = "mov BX, DS:[" + String.valueOf(exp1.getAddr() & 0xFFF)
                        + "h]\t; Obtendo valor de Exp1.end(Posicao)\n" + "add BX, "
                        + String.valueOf(id.getAddr() & 0xFFF) + "h\t; Somando Posicao + id.end(inicial)\n"
                        + "mov AX, DS:[BX]\t; Carregando valor na pos correta\n" + "mov DS:["
                        + String.valueOf(factor.getAddr() & 0xFFF) + "h], AX\t; Armazenando em Fator.end\n";
            }
        } else {
            factor.setAddr(id.getAddr());
        }
        writeCode(code);
    }

    /**
     * Termo -> Fator
     */
    public void codeGenerationT7(Symbol term, Symbol factor) {
        term.setAddr(factor.getAddr());
    }

    /**
     * Termo -> Fator1
     */
    public void codeGenerationT8(boolean condMult, boolean condDiv, boolean condMod, Symbol term, Symbol factor1) {
        String code = "";
        String label1, label2 = "";

        code = "mov ax, DS:[" + String.valueOf(term.getAddr() & 0xFFF) + "h]\t; Carregar valor de Termo.end\n"
                + "mov bx, DS:[" + String.valueOf(factor1.getAddr() & 0xFFF) + "h]\t; Carregar valor de Fator1.end\n";

        if (condMult) { // multiplicacao
            code += "imul bx\t; ax <- ax * bx (Termo * Fator1)\n";
            term.setAddr(newTemp(factor1.getType()));

            code += "mov DS:[" + String.valueOf(term.getAddr() & 0xFFF)
                    + "h], ax\t; Armazenando resultado em Termo.end\n";
        } else if (condDiv) { // divisao
            code += "idiv bx\t; ax(quoc) <- ax / bx\n";
            term.setAddr(newTemp(factor1.getType()));

            code += "mov DS:[" + String.valueOf(term.getAddr() & 0xFFF)
                    + "h], ax\t; Armazenando resultado em Termo.end\n";
        } else if (condMod) { // resto
            code += "idiv bx\t; dx(resto) <- ax / bx\n" + "mov ax, dx\t; movendo o resto da divisao para ax\n";
            term.setAddr(newTemp(factor1.getType()));

            code += "mov DS:[" + String.valueOf(term.getAddr() & 0xFFF)
                    + "h], ax\t; Armazenando resultado em Termo.end\n";
        } else { // E logico
            label1 = newLabel();
            label2 = newLabel();
            code += "imul bx\t; Operacao AND (*)\n" + "cmp ax, 0\t; Verificando se AND = 0 (Falso)\n" + "jne " + label1
                    + "\t; Jump para " + label1 + " caso AND = 1\n" + "mov ax, 0\t; Salva resultado como Falso (0) \n"
                    + "jmp " + label2 + "\t; Jump para " + label2 + " (Final)\n" + label1 + ":\n"
                    + "mov ax, 1\t; Salva resultado como Verdadeiro (1) \n" + "jmp " + label2 + "\t; Jump para "
                    + label2 + " (Final)\n";
            term.setAddr(newTemp(factor1.getType()));

            code += label2 + ":\nmov DS:[" + String.valueOf(term.getAddr() & 0xFFF)
                    + "h], ax\t; Armazenando resultado em Termo.end\n";
        }

        writeCode(code);
    }

    /**
     * ExpS -> [+ | -] T1
     */
    public void codeGenerationT9(boolean condSoma, boolean condSub, Symbol expS, Symbol term) {
        String code = "";

        if (condSub) { // necessidade de negar term
            expS.setAddr(newTemp(term.getType()));

            code = "mov dx, DS:[" + String.valueOf(term.getAddr() & 0xFFF) + "h]\t; Carregando valor de Termo.end\n"
                    + "neg dx\t; Negando Termo (-) Termo\n" + "add dx, 1\t; Complemento de 1\n" + "mov DS:["
                    + String.valueOf(expS.getAddr() & 0xFFF) + "h], dx\t; Armazenando resultado em ExpS.end\n";
        } else {
            expS.setAddr(term.getAddr());
        }

        writeCode(code);
    }

    /**
     * ExpS -> (+ | - | OR) T2
     */
    public void codeGenerationT10(boolean condSoma, boolean condSub, Symbol term1, Symbol expS) {
        String code = "";
        String label1, label2 = "";

        code = "mov ax, DS:[" + String.valueOf(expS.getAddr() & 0xFFF) + "h]\t; ax <- Carregando valor de expS.end\n"
                + "mov bx, DS:[" + String.valueOf(term1.getAddr() & 0xFFF)
                + "h]\t; bx <- Carregando valor de Term1.end\n";

        if (condSoma) { // soma
            code += "add ax, bx\t; Operacao Soma (ax <- ax + bx)\n";
            expS.setAddr(newTemp(term1.getType()));

            code += "mov DS:[" + String.valueOf(expS.getAddr() & 0xFFF)
                    + "h], ax\t; Armazenando resultado em ExpS.end\n";
        } else if (condSub) { // subtracao
            code += "sub ax, bx\t; Operacao Subtracao (ax <- ax - bx)\n";
            expS.setAddr(newTemp(term1.getType()));

            code += "mov DS:[" + String.valueOf(expS.getAddr() & 0xFFF)
                    + "h], ax\t; Armazenando resultado em ExpS.end\n";
        } else { // OU logico
            label1 = newLabel();
            label2 = newLabel();
            code += "add ax, bx\t; Operacao OU Logico\n\n" + "cmp ax, 0\t; Comparando se OU = 0 (Falso)\n" + "jne "
                    + label1 + "\t; Jump para " + label1 + " caso OR = 1(verdadeiro)\n"
                    + "mov ax, 0\t; Armazena resultado como Falso (0)\n" + "jmp " + label2 + "\t; Jump para " + label2
                    + " (Fim)\n" + label1 + ":\n" + "mov ax, 1\t; Armazena resultado como Verdadeiro (1)\n" + "jmp "
                    + label2 + "\t; Jump para " + label2 + " (Fim)\n";
            expS.setAddr(newTemp(term1.getType()));

            code += label2 + ":\nmov DS:[" + String.valueOf(expS.getAddr() & 0xFFF)
                    + "h], ax\t; Armazenando resultado em ExpS.end\n";
        }

        writeCode(code);
    }

    /**
     * Exp -> ExpS
     */
    public void codeGenerationT11(Symbol exp, Symbol expS) {
        exp.setAddr(expS.getAddr());
    }

    /**
     * Exp -> (= | <> | > | < | <= | >= ) ExpS1
     */
    public void codeGenerationT12(boolean condEquals, boolean condDiff, boolean condLess, boolean condGreater,
            boolean condLessEquals, Symbol exp, Symbol expS) {
        String code = "";
        String rotTrue = newLabel();
        String rotEnd = newLabel();

        code = "mov ax, DS:[" + String.valueOf(exp.getAddr() & 0xFFF) + "h]\t; (ax) <- Carrega valor de exp.end\n"
                + "mov bx, DS:[" + String.valueOf(expS.getAddr() & 0xFFF) + "h]\t; (bx) <- Carrega valor de expS.end\n"
                + "mov ah, 0\t; Convertendo AX para inteiro (ah = 0)\n"
                + "mov bh, 0\t; Convertendo BX para inteiro (bh = 0)\n" + "cmp ax, bx\t; Comparando ax com bx\n";

        if (condEquals) {
            code += "je " + rotTrue + "\t; Pula para + " + rotTrue + " caso ax = bx\n"
                    + "mov ax, 0\t; armazena como FALSO\n" + "jmp " + rotEnd + "\t; Pula para + " + rotEnd + " (fim)\n";
        } else if (condDiff) {
            code += "jne " + rotTrue + "\t; Pula para + " + rotTrue + " caso ax <> bx\n"
                    + "mov ax, 0\t; armazena como FALSO\n" + "jmp " + rotEnd + "\t; Pula para + " + rotEnd + " (fim)\n";
        } else if (condLess) {
            code += "jl " + rotTrue + "\t; Pula para + " + rotTrue + " caso ax < bx\n"
                    + "mov ax, 0\t; armazena como FALSO\n" + "jmp " + rotEnd + "\t; Pula para + " + rotEnd + " (fim)\n";
        } else if (condGreater) {
            code += "jg " + rotTrue + "\t; Pula para + " + rotTrue + " caso ax > bx\n"
                    + "mov ax, 0\t; armazena como FALSO\n" + "jmp " + rotEnd + "\t; Pula para + " + rotEnd + " (fim)\n";
        } else if (condLessEquals) {
            code += "jle " + rotTrue + "\t; Pula para + " + rotTrue + " caso ax <= bx\n"
                    + "mov ax, 0\t; armazena como FALSO\n" + "jmp " + rotEnd + "\t; Pula para + " + rotEnd + " (fim)\n";
        } else { // se for maior ou igual
            code += "jge " + rotTrue + "\t; Pula para + " + rotTrue + " caso ax => bx\n"
                    + "mov ax, 0\t; armazena como FALSO\n" + "jmp " + rotEnd + "\t; Pula para + " + rotEnd + " (fim)\n";
        }

        code += rotTrue + ":\n mov ax, 1\t; armazena como VERDADEIRO\n" + rotEnd + ":\n";

        exp.setAddr(newTemp("INTEGER"));

        code += "mov DS:[" + String.valueOf(exp.getAddr() & 0xFFF) + "h], ax\t; Armazenando resultado em Exp.end\n";

        writeCode(code);
    }

    /**
     * Atribuicao
     */
    public void codeGenerationT13(boolean condition, Symbol id, Symbol exp, Symbol exp1) {
        String code = "";

        if (condition) {

        } else {
            if (exp.getType().equals("STRING")) {

            } else {
                code = "mov ax, DS:[" + String.valueOf(exp.getAddr() & 0xFFF) + "h]\n";
            }
        }
        /*
         * { carregar conteúdo de Exp.end } { armazenar o resultado em id.end } Obs:
         * strings devem ser movidas caractere a caractere
         */

        writeCode(code);
    }

    /**
     * if Exp { carregar conteúdo de Exp.end } { se exp é falsa, desvia para
     * RotFalso }
     */
    public void codeGenerationT14(String rotFalso, String rotFim, Symbol exp) {
        String code = "";

        code += "mov ax, DS:[" + String.valueOf(exp.getAddr() & 0xFFF) + "h]\t; Carrega valor de Exp.end\n"
                + "cmp ax, 0 \t; Verificando se Exp e' falso\n" + "je " + rotFalso + "\n";

        writeCode(code);
    }

    /**
     * else { desvia para RotFim } { RotFalso: }}
     */
    public void codeGenerationT15(String rotFalso, String rotFim) {
        String code = "";
        code += "jmp " + rotFim + "\t; Pulando para o final\n" + rotFalso + ":\n";
        writeCode(code);
    }

    /**
     * RotFim:
     */
    public void codeGenerationT16(String rotFim){
        String code = "";
        code += rotFim + ":\n";
        writeCode(code);
    }

    /**
     * Readln(x)
     */
    public void codeGenerationT17(Symbol id){
        String code = "";
        Symbol buffer = new Symbol(null, "");
        String size = "0FFh";
        if((id.getSize() <= 255)){
            size = String.valueOf(id.getSize() & 0xFFF) + "h";
        }
        buffer.setAddr(newTemp(id.getType()));
        code += "mov dx, " + String.valueOf(buffer.getAddr() & 0xFFF) + "h \t; Obtendo endereco de buffer\n" +
        "mov al, " + size + "\t; tamanho do vetor ou 255\n" + 
        "mov DS:[" + String.valueOf(buffer.getAddr() & 0xFFF) + "h], al\t; Armazenando tamanho\n" +
        "mov ah, 0Ah\n" +
        "int 21h\t; interrupcao para leitura\n" + 
        "mov ah, 02h\t; quebra de linha\n" +
        "mov dl, 0Dh\n " + 
        "int 21h\n" + 
        "mov DL, 0Ah\n" + 
        "int 21h\n";

            code += "mov di, [" + String.valueOf((buffer.getAddr()+2) & 0xFFF) + "h]\t; posicao do string\n" +
            "mov ax, 0\n" +
            "mov cx, 10\n" +
            "mov dx, 1\n" + 
            "mov bh, 0\n" +
            "mov bl, DS:[di]\n" +
            "cmp bx, 2Dh\n" +
            "jne r0\n" + 
            "mov dx, -1\n" + 
            "add di, 1\n" +
            "mov bl, ds:[di]\n" +
            "r0:\n" +
            "push dx\n" +
            "mov dx, 0\n" +
            "r1: \n" + 
            "cmp bx, 0dh\n"+
            "je r2\n" +
            "imul cx\n" + 
            "add bx, -48\n" +
            "add ax, bx\n" + 
            "add di, 1\n" +
            "mov bh, 0\n" + 
            "mov bl, ds:[di]\n" +
            "jmp r1\n"+
            "r2:\n"+
            "pop cx\n"+
            "imul cx\n";
        writeCode(code);
    }
}
