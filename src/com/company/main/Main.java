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
        //TODO: Esta chamada tem que ser feita recebendo como retorno o endereço do ultimo char lido, assim
        // quando voltar e não for EOF deve ser chamado de novo para continuar a leitura do arquivo para
        // o proximo teste léxico, quando voltar, provavelmente deve precisar chamar o analisador sintatico
        // logo em seguida.
        boolean result = lexicalAnalyzer.lexicalAnalysis(sourceFile);
        if(!result){
            System.out.println("Compilation Failed!");
        }
    }
}
