package com.company.main;

public class Symbol {

    private Byte token;
    private String lexeme;
    private String _class;
    private String type;
    private int size;

    public Symbol(){}

    public Symbol(Byte token, String lexeme) {
        this.token = token;
        this.lexeme = lexeme;
        this._class = "";
        this.type = "";
        this.size = 0;
    }

    public Byte getToken() {
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

    public void setToken(Byte token) {
        this.token = token;
    }

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
