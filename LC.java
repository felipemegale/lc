import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

/**
 * Pontificia Universidade Catolica de Minas Gerais Intituto de Ciencias Exatas
 * e Informatica - Departamento de Ciencia da Computacao Disciplina de
 * Compiladores - L Language Compiler
 * 
 * @author: Alexandre Abreu, Felipe Megale, João Castro
 */

public class LC {
    public static void main(String[] args) {
        String sourceFileName, outputFileName;
        FileWriter codeWriter;
        if(args.length == 2){
            sourceFileName = args[0];
            outputFileName = args[1];
            try {
                codeWriter = new FileWriter(outputFileName);
                if(sourceFileName.endsWith(".l") && outputFileName.endsWith(".asm")){
                    File sourceFile = new File(sourceFileName);
                    LexicalAnalyzer lexicalAnalyzer = new LexicalAnalyzer(sourceFile);
                    SyntacticAnalyzer syntacticAnalyzer = new SyntacticAnalyzer(lexicalAnalyzer, codeWriter);
                    syntacticAnalyzer.token = lexicalAnalyzer.lexicalAnalysis().getLexeme();
                    syntacticAnalyzer.procedure_S();
                }else{
                    codeWriter.close();
                    throw new Error("parametros invalidos.");
                }
            } catch (IOException ioe) {
                throw new Error("problema de arquivo");
            }
            }
        } catch (Error e) {
            System.out.println(e.getMessage());
        }
    }
}
