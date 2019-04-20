package com.company.main;

import java.io.File;

public class LC {

    public static void main(String[] args) {
        //TODO: Configurar IDEA para utilizar parâmetros.
        String sourceFileName = args[0];
        String outputFileName = args[1];
        //TODO: Validar nome de Arquivos.
        File sourceFile = new File(sourceFileName);
        Error errors = new Error();
        LexicalAnalyzer lexicalAnalyzer = new LexicalAnalyzer(sourceFile, errors);
        SyntacticAnalyzer syntacticAnalyzer = new SyntacticAnalyzer(lexicalAnalyzer, errors);
        syntacticAnalyzer.token = lexicalAnalyzer.lexicalAnalysis().getLexeme();
        syntacticAnalyzer.procedure_S();
        if(!syntacticAnalyzer.token.equals("EOF"))
            System.out.println("Erro!");
    }
}
