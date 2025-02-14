import java.io.File;

/**
 * Pontificia Universidade Catolica de Minas Gerais
 * Intituto de Ciencias Exatas e Informatica - Departamento de Ciencia da Computacao
 * Disciplina de Compiladores - L Language Compiler
 * @author: Alexandre Abreu, Felipe Megale, João Castro
 */

public class LC {
    public static void main(String[] args) {
        String sourceFileName, outputFileName;
        if(args.length == 2){
            sourceFileName = args[0];
            outputFileName = args[1];
            if(sourceFileName.endsWith(".l") && outputFileName.endsWith(".asm")){
                File sourceFile = new File(sourceFileName);
                LexicalAnalyzer lexicalAnalyzer = new LexicalAnalyzer(sourceFile);
                SyntacticAnalyzer syntacticAnalyzer = new SyntacticAnalyzer(lexicalAnalyzer);
                syntacticAnalyzer.token = lexicalAnalyzer.lexicalAnalysis().getLexeme();
                syntacticAnalyzer.procedure_S();
            }else{
                throw new Error("parametros invalidos.");
            }
        }else{
            throw new Error("numero de parametros invalido.");
        }
    }
}

