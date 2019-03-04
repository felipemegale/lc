package com.company.main;

import java.io.BufferedReader;
import java.io.InputStreamReader;

public class Main {

    public static void main(String[] args) {
        BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
        String data;
        SymbolTable st = new SymbolTable();
        LexicalAnalyzer la = new LexicalAnalyzer();

        System.out.println("Type STAHP to stahp");

        try {

            do {
                data = reader.readLine();

                Byte addr = st.searchLexeme(data);

                if (addr != null)
                    System.out.println(addr + " found!");
                else
                    System.out.println(addr + " found");

                if (la.isLexemeValid(data)) {
                    System.out.println("lexeme is valid");
                    System.out.println(st.insertToken(data));
                }
                else
                    System.out.println("lexeme is NOT valid");

                st.printTable();
            } while (!data.equals("STAHP"));

        } catch (java.io.IOException ioe) {
            ioe.printStackTrace();
        }
    }
}
