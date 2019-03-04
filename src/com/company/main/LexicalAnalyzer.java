package com.company.main;

/*
/\b((([A-Z])+)((\w|\.)*))|(((\.|_)+)(([A-Z])|\d)((\w|\.)*))/gi --> variable names
/0x([A-F]|[0-9]){2}\b/gi --> hexadecimal characters
 */

public class LexicalAnalyzer {

    private static final String VARIABLE_PATTERN = "\\b((([a-zA-Z])+)((([a-zA-Z])|\\.|_|[0-9])*))|(((\\.|_)+)(([a-zA-Z])|[0-9])((([a-zA-Z])|\\.|_|[0-9])*))";
    private static final String HEXADECIMAL_PATTERN = "0x([A-F]|[0-9]){2}\\b";
    private static final String AVAILABLE_CHARACTERS = "'[a-zA-z0-9\\s_\\.,;&:\\(\\)\\[\\]{}+\\-\"\\'/%\\^@!?><=\\n\\r\\t]'";

    public LexicalAnalyzer() {}

    public boolean isLexemeValid(String lexeme) {
        return lexeme.matches(VARIABLE_PATTERN);
    }
}
