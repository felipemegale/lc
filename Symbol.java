/**
 * Pontificia Universidade Catolica de Minas Gerais
 * Intituto de Ciencias Exatas e Informatica - Departamento de Ciencia da Computacao
 * Disciplina de Compiladores - L Language Compiler
 * @author: Alexandre Abreu, Felipe Megale, João Castro
 */

public class Symbol {

    private String token;
    private String lexeme;
    private String _class;
    private String type;
    private Byte _addr;
    private int size;

    public Symbol(){}

    public Symbol(String token, String lexeme, Byte _addr) {
        this.token = token;
        this.lexeme = lexeme;
        this._class = "";
        this.type = "";
        this.size = 0;
        this._addr = _addr;
    }

    public Symbol(Byte _addr, String lexeme) {
        this.token = this.lexeme = lexeme;
        this._class = "";
        this.type = "";
        this.size = 0;
        this._addr = _addr;
    }

    public String getToken() {
        return token;
    }

    public String getLexeme() {
        return lexeme;
    }

    public String get_Class() {
        return _class;
    }

    public String getType() {
        return type;
    }

    public int getSize() {
        return size;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public Byte getAddr(){ return this._addr; }

    public void setAddr(Byte _addr){this._addr = _addr;}

    public void setLexeme(String lexeme) {
        this.lexeme = lexeme;
    }

    public void set_Class(String _class) {
        this._class = _class;
    }

    public void setType(String type) {
        this.type = type;
    }

    public void setSize(int size) {
        this.size = size;
    }

    @Override
    public String toString() {
        return "Symbol{" +
                "token=" + token +
                ", lexeme='" + lexeme + '\'' +
                '}';
    }
}
