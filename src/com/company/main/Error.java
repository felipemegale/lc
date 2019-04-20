package com.company.main;

public class Error {
    private int lineError;
    private String messageError;
    private String _type;
    private boolean hasError;


    public Error(){
        hasError = false;
    }

    public void setError(int lineError, String _type){
        this.lineError = lineError;
        this._type = _type;
        setMessageError();
        throwError();
    }

    public void throwError(){
        hasError = true;
        System.out.println(this);
    }

    public boolean checkError(){
        return this.hasError;
    }

    public void setMessageError(){
        if(_type.equals("LEX_INVALIDCHAR"))
            messageError = "caractere invalido.";
        else if(_type.contains("LEX_LEXEMENOTFOUND:")){
            messageError = "lexema nao identificado [" + _type.split(":")[1] + "].";
        }else if(_type.contains("LEX_EOFNOTEXPECTED")){
            messageError = "fim do arquivo nao esperado.";
        }else if(_type.contains("SYN_TOKENNOTEXPECTED:")){
            messageError = "token nao esperado [" + _type.split(":")[1] + "].";
        }else if(_type.equals("SYN_EOFNOTEXPECTED")){
            messageError = "fim do arquivo nao esperado.";
        }
    }

    @Override
    public String toString() {
        return this.lineError + ":" + this.messageError;
    }
}
