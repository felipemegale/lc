package com.company.main;

import java.io.File;

public class Main {

    public static void main(String[] args) {
        //TODO: Configurar IDEA para utilizar parâmetros.
        String sourceFileName = args[0];
        String outputFileName = args[1];
        //TODO: Validar nome de Arquivos.
        File sourceFile = new File(sourceFileName);
        LexicalAnalyzer lexicalAnalyzer = new LexicalAnalyzer();
        boolean result = lexicalAnalyzer.lexicalAnalysis(sourceFile);
        if(!result){
            System.out.println("Compilation Failed!");
        }
    }
}
