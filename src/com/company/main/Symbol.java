package com.company.main;

public class Symbol {
    private String token;
    private String lexeme;
    private String _class;
    private String type;
    private int size;

    public Symbol(){}

    public Symbol(String token, String lexeme, String _class, String type, int size) {
        this.token = token;
        this.lexeme = lexeme;
        this._class = _class;
        this.type = type;
        this.size = size;
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
}
