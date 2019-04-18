
package symbolstable;

import java.util.HashMap;
import java.util.Map;


class Simbolos {
    
    private String token;
    private String lexema;
    private String classe;
    private String tipo;
    private int tamanho;

    public Simbolos() {
    }

    public Simbolos(String token, String lexema, String classe, String tipo, int tamanho) {
        this.token = token.toLowerCase();
        this.lexema = lexema.toLowerCase();
        this.classe = classe.toLowerCase();
        this.tipo = tipo.toLowerCase();
        this.tamanho = tamanho;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getLexema() {
        return lexema;
    }

    public void setLexema(String lexema) {
        this.lexema = lexema;
    }

    public String getClasse() {
        return classe;
    }

    public void setClasse(String classe) {
        this.classe = classe;
    }

    public String getTipo() {
        return tipo;
    }

    public void setTipo(String tipo) {
        this.tipo = tipo;
    }

    public int getTamanho() {
        return tamanho;
    }

    public void setTamanho(int tamanho) {
        this.tamanho = tamanho;
    }

    @Override
    public String toString() {
        return "{ " + token + ", lexema = " + lexema + ", classe = " + classe + ", tipo = " + tipo + ", tamanho = " + tamanho + '}'+"\n";
    }
    
    
    
}


public class SymbolsTable{

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        
        Map<String,Simbolos> tabela = new HashMap<String,Simbolos>();
        
      Simbolos constante = new Simbolos("constante","const","Palavra Reservada", "String",0);
      tabela.put("const", constante);
      
      Simbolos variavel = new Simbolos("variavel","var","Palavra Reservada", "String",0);
      tabela.put("var", variavel);
      
      Simbolos inteiro = new Simbolos("inteiro","integer","Palavra Reservada", "String",0);
      tabela.put("integer", inteiro);
      
      Simbolos caracter = new Simbolos("caracter","char","Palavra Reservada", "String",0);
      tabela.put("char", caracter);
            
      Simbolos para = new Simbolos("para","for","Palavra Reservada", "String",0);
      tabela.put("for", para);
      
      Simbolos se = new Simbolos("se","if","Palavra Reservada","String",0);
      tabela.put("if", se);
      
      Simbolos senao = new Simbolos("senao","else","Palavra Reservada","String",0);
      tabela.put("else", senao);
      
      Simbolos e = new Simbolos("e","and","Palavra Reservada", "String",0);
      tabela.put("and", e);
      
      Simbolos ou = new Simbolos("ou","or","Palavra Reservada", "String",0);
      tabela.put("or", ou);
      
      Simbolos nao = new Simbolos("nao","not","Palavra Reservada","String",0);
      tabela.put("not", nao);
      
      Simbolos igual = new Simbolos("igual","=","Palavra Reservada","String",0);
      tabela.put("=", igual);
      
      Simbolos to = new Simbolos("to","to","Palavra Reservada","String",0);
      tabela.put("to", to);
      
      Simbolos abreParentese = new Simbolos("abreParentese","(","Palavra Reservada","String",0);
      tabela.put("(", abreParentese);
      
      Simbolos fechaParentese = new Simbolos("fechaParentese",")","Palavra Reservada","String",0);
      tabela.put(")", fechaParentese);
      
      Simbolos menor = new Simbolos("menor","<","Palavra Reservada","String",0);
      tabela.put("<", menor);
      
      Simbolos maior = new Simbolos("maior",">","Palavra Reservada","String",0);
      tabela.put(">", maior);
      
      Simbolos diferente = new Simbolos("<>","<>","Palavra Reservada","String",0);
      tabela.put("<>", diferente);
      
      Simbolos maiorIgual = new Simbolos("maiorIgual",">=","Palavra Reservada","String",0);
      tabela.put(">=", maiorIgual);
      
      Simbolos menorIgual = new Simbolos("menorIgual","<=","Palavra Reservada","String",0);
      tabela.put("<=", menorIgual);
      
      Simbolos virgula = new Simbolos("virgula",",","Palavra Reservada","String",0);
      tabela.put(",", virgula);
      
      Simbolos mais = new Simbolos("mais","+","Palavra Reservada","String",0);
      tabela.put("+", mais);
      
      Simbolos menos = new Simbolos("menos","-","Palavra Reservada","String",0);
      tabela.put("-", menos);
      
      Simbolos vezes = new Simbolos("vezes","*","Palavra Reservada","String",0);
      tabela.put("*", vezes);
      
      Simbolos dividido = new Simbolos("dividido","/","Palavra Reservada","String",0);
      tabela.put("/", dividido);
      
      Simbolos pontoVirgula = new Simbolos("pontoVirgula",";","Palavra Reservada","String",0);
      tabela.put(";", pontoVirgula);
      
      Simbolos abreChave = new Simbolos("abreChave","{","Palavra Reservada","String",0);
      tabela.put("{", abreChave);
      
      Simbolos fechaChave = new Simbolos("fechaChave","}","Palavra Reservada","String",0);
      tabela.put("}", fechaChave);
      
      Simbolos entao = new Simbolos("entao","then","Palavra Reservada","String",0);
      tabela.put("then", entao);
      
      Simbolos lerLinha = new Simbolos("lerLinha","readln","Palavra Reservada","String",0);
      tabela.put("readln", lerLinha);
      
      Simbolos passo = new Simbolos("passo","step","Palavra Reservada","String",0);
      tabela.put("step", passo);
      
      Simbolos escrever = new Simbolos("escrever","write","Palavra Reservada","String",0);
      tabela.put("write", escrever);
      
      Simbolos escreverLinha = new Simbolos("escreverLinha","writeln","Palavra Reservada","String",0);
      tabela.put("writeln", escreverLinha);
      
      Simbolos resto = new Simbolos("resto","%","Palavra Reservada","String",0);
      tabela.put("%", resto);
      
      Simbolos abreColchete = new Simbolos("abreColchete","[","Palavra Reservada","String",0);
      tabela.put("[", abreColchete);
      
      Simbolos fechaColchete = new Simbolos("fechaColchete","]","Palavra Reservada","String",0);
      tabela.put("]", fechaColchete);
      
      Simbolos faca = new Simbolos("faca","do","Palavra Reservada","String",0);
      tabela.put("do", faca);
      
      
      System.out.println(tabela.toString());
        System.out.println(tabela.size());
      
    }
    
}
