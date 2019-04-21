package com.company.main;

public class Error extends RuntimeException{

    public Error(String err){
        super(err);
        super.setStackTrace(new StackTraceElement[0]);
    }
}
