/* JFlex specification for FlowLang Lexical Analyzer */

%%

%public
%class Yylex
%unicode
%line
%column
%type Token

%{
    /* User code - helper methods */
    private SymbolTable symbolTable = new SymbolTable();
    private ErrorHandler errorHandler = new ErrorHandler();
    private int commentsRemoved = 0;
    private java.util.List<Token> tokenList = new java.util.ArrayList<>();
    private java.util.Map<TokenType, Integer> tokenCounts = new java.util.LinkedHashMap<>();

    {
        for (TokenType t : TokenType.values()) {
            tokenCounts.put(t, 0);
        }
    }

    private Token makeToken(TokenType type, String lexeme) {
        Token t = new Token(type, lexeme, yyline + 1, yycolumn + 1);
        tokenList.add(t);
        tokenCounts.put(type, tokenCounts.getOrDefault(type, 0) + 1);
        return t;
    }

    public SymbolTable getSymbolTable() { return symbolTable; }
    public ErrorHandler getErrorHandler() { return errorHandler; }
    public int getCommentsRemoved() { return commentsRemoved; }
    public java.util.List<Token> getTokenList() { return tokenList; }
    public java.util.Map<TokenType, Integer> getTokenCounts() { return tokenCounts; }

    /* StringBuilder for string/char literal building */
    private StringBuilder stringBuffer = new StringBuilder();
    private int stringStartLine;
    private int stringStartCol;
%}

/* Macros */
DIGIT       = [0-9]
UPPER       = [A-Z]
LOWER       = [a-z]
WHITESPACE  = [ \t\r\n]+

/* States for string and char literal parsing */
%xstate STRING_STATE
%xstate CHAR_STATE

%%

/* ============================================ */
/*  YYINITIAL state - main scanning rules       */
/* ============================================ */

<YYINITIAL> {

    /* 1. Multi-line comments */
    "#*" ([^*] | "*"+ [^*#])* "*"+ "#"
        { makeToken(TokenType.MULTI_LINE_COMMENT, yytext()); commentsRemoved++; }

    "#*" ([^*] | "*"+ [^*#])* \** 
        { errorHandler.reportUnclosedComment(yyline + 1, yycolumn + 1, yytext());
          makeToken(TokenType.ERROR, yytext()); }

    /* 2. Single-line comments */
    "##" [^\n]*
        { makeToken(TokenType.SINGLE_LINE_COMMENT, yytext()); commentsRemoved++; }

    /* 3. Multi-character operators */
    "**"    { makeToken(TokenType.ARITHMETIC_OP, yytext()); }
    "=="    { makeToken(TokenType.RELATIONAL_OP, yytext()); }
    "!="    { makeToken(TokenType.RELATIONAL_OP, yytext()); }
    "<="    { makeToken(TokenType.RELATIONAL_OP, yytext()); }
    ">="    { makeToken(TokenType.RELATIONAL_OP, yytext()); }
    "&&"    { makeToken(TokenType.LOGICAL_OP, yytext()); }
    "||"    { makeToken(TokenType.LOGICAL_OP, yytext()); }
    "++"    { makeToken(TokenType.INC_DEC_OP, yytext()); }
    "--"    { makeToken(TokenType.INC_DEC_OP, yytext()); }
    "+="    { makeToken(TokenType.ASSIGNMENT_OP, yytext()); }
    "-="    { makeToken(TokenType.ASSIGNMENT_OP, yytext()); }
    "*="    { makeToken(TokenType.ASSIGNMENT_OP, yytext()); }
    "/="    { makeToken(TokenType.ASSIGNMENT_OP, yytext()); }

    /* 4. Keywords (must match before identifiers) */
    "start"     { makeToken(TokenType.KEYWORD, yytext()); }
    "finish"    { makeToken(TokenType.KEYWORD, yytext()); }
    "loop"      { makeToken(TokenType.KEYWORD, yytext()); }
    "condition" { makeToken(TokenType.KEYWORD, yytext()); }
    "declare"   { makeToken(TokenType.KEYWORD, yytext()); }
    "output"    { makeToken(TokenType.KEYWORD, yytext()); }
    "input"     { makeToken(TokenType.KEYWORD, yytext()); }
    "function"  { makeToken(TokenType.KEYWORD, yytext()); }
    "return"    { makeToken(TokenType.KEYWORD, yytext()); }
    "break"     { makeToken(TokenType.KEYWORD, yytext()); }
    "continue"  { makeToken(TokenType.KEYWORD, yytext()); }
    "else"      { makeToken(TokenType.KEYWORD, yytext()); }

    /* 5. Boolean literals */
    "true"      { makeToken(TokenType.BOOLEAN_LITERAL, yytext()); }
    "false"     { makeToken(TokenType.BOOLEAN_LITERAL, yytext()); }

    /* 6. Identifiers */
    {UPPER}({LOWER}|{DIGIT}|"_"){0,30}
        { makeToken(TokenType.IDENTIFIER, yytext());
          symbolTable.addIdentifier(yytext(), yyline + 1, yycolumn + 1); }

    /* Invalid identifiers (lowercase or underscore start followed by alphanum) */
    {LOWER} ({LOWER} | {UPPER} | {DIGIT} | "_")*
        { errorHandler.reportInvalidIdentifier(yyline + 1, yycolumn + 1, yytext(),
            "Identifier must start with an uppercase letter (A-Z)");
          makeToken(TokenType.ERROR, yytext()); }

    "_" ({LOWER} | {UPPER} | {DIGIT} | "_")*
        { errorHandler.reportInvalidIdentifier(yyline + 1, yycolumn + 1, yytext(),
            "Identifier must start with an uppercase letter (A-Z)");
          makeToken(TokenType.ERROR, yytext()); }

    /* 7. Floating-point literals */
    {DIGIT}+"."{DIGIT}{1,6}([eE][+-]?{DIGIT}+)?
        { makeToken(TokenType.FLOAT_LITERAL, yytext()); }

    /* Malformed float: too many decimal digits */
    {DIGIT}+"."{DIGIT}{6}{DIGIT}+
        { errorHandler.reportMalformedLiteral(yyline + 1, yycolumn + 1, yytext(),
            "Floating-point literal has more than 6 decimal digits");
          makeToken(TokenType.ERROR, yytext()); }

    /* Malformed float: no digits after dot */
    {DIGIT}+"."
        { errorHandler.reportMalformedLiteral(yyline + 1, yycolumn + 1, yytext(),
            "Floating-point literal must have digits after decimal point");
          makeToken(TokenType.ERROR, yytext()); }

    /* 8. Integer literals */
    {DIGIT}+
        { makeToken(TokenType.INTEGER_LITERAL, yytext()); }

    /* 9. String literals */
    \"  { stringBuffer.setLength(0);
          stringBuffer.append('"');
          stringStartLine = yyline + 1;
          stringStartCol = yycolumn + 1;
          yybegin(STRING_STATE); }

    /* 9. Character literals */
    \'  { stringBuffer.setLength(0);
          stringBuffer.append('\'');
          stringStartLine = yyline + 1;
          stringStartCol = yycolumn + 1;
          yybegin(CHAR_STATE); }

    /* 10. Single-character operators */
    "+"     { makeToken(TokenType.ARITHMETIC_OP, yytext()); }
    "-"     { makeToken(TokenType.ARITHMETIC_OP, yytext()); }
    "*"     { makeToken(TokenType.ARITHMETIC_OP, yytext()); }
    "/"     { makeToken(TokenType.ARITHMETIC_OP, yytext()); }
    "%"     { makeToken(TokenType.ARITHMETIC_OP, yytext()); }
    "<"     { makeToken(TokenType.RELATIONAL_OP, yytext()); }
    ">"     { makeToken(TokenType.RELATIONAL_OP, yytext()); }
    "!"     { makeToken(TokenType.LOGICAL_OP, yytext()); }
    "="     { makeToken(TokenType.ASSIGNMENT_OP, yytext()); }

    /* 11. Punctuators */
    "("     { makeToken(TokenType.PUNCTUATOR, yytext()); }
    ")"     { makeToken(TokenType.PUNCTUATOR, yytext()); }
    "{"     { makeToken(TokenType.PUNCTUATOR, yytext()); }
    "}"     { makeToken(TokenType.PUNCTUATOR, yytext()); }
    "["     { makeToken(TokenType.PUNCTUATOR, yytext()); }
    "]"     { makeToken(TokenType.PUNCTUATOR, yytext()); }
    ","     { makeToken(TokenType.PUNCTUATOR, yytext()); }
    ";"     { makeToken(TokenType.PUNCTUATOR, yytext()); }
    ":"     { makeToken(TokenType.PUNCTUATOR, yytext()); }

    /* 12. Whitespace - skip but track */
    {WHITESPACE}    { /* skip */ }

    /* Error: invalid character */
    .   { errorHandler.reportInvalidCharacter(yyline + 1, yycolumn + 1, yytext());
          makeToken(TokenType.ERROR, yytext()); }
}

/* ============================================ */
/*  STRING_STATE - scanning string literals     */
/* ============================================ */

<STRING_STATE> {
    \"  { stringBuffer.append('"');
          makeToken(TokenType.STRING_LITERAL, stringBuffer.toString());
          yybegin(YYINITIAL); }

    \\\"    { stringBuffer.append("\\\""); }
    \\\\    { stringBuffer.append("\\\\"); }
    \\n     { stringBuffer.append("\\n"); }
    \\t     { stringBuffer.append("\\t"); }
    \\r     { stringBuffer.append("\\r"); }

    \\.     { stringBuffer.append(yytext());
              errorHandler.reportInvalidEscape(stringStartLine, stringStartCol,
                  yytext(), "Invalid escape sequence '" + yytext() + "'"); }

    \n      { errorHandler.reportUnterminatedString(stringStartLine, stringStartCol,
                  stringBuffer.toString());
              makeToken(TokenType.ERROR, stringBuffer.toString());
              yybegin(YYINITIAL); }

    <<EOF>> { errorHandler.reportUnterminatedString(stringStartLine, stringStartCol,
                  stringBuffer.toString());
              makeToken(TokenType.ERROR, stringBuffer.toString());
              yybegin(YYINITIAL);
              return makeToken(TokenType.EOF, ""); }

    [^\"\\\n]+  { stringBuffer.append(yytext()); }
}

/* ============================================ */
/*  CHAR_STATE - scanning character literals    */
/* ============================================ */

<CHAR_STATE> {
    \\[\'\\ntr] \'
        { stringBuffer.append(yytext());
          makeToken(TokenType.CHAR_LITERAL, stringBuffer.toString());
          yybegin(YYINITIAL); }

    [^\'\\\n] \'
        { stringBuffer.append(yytext());
          makeToken(TokenType.CHAR_LITERAL, stringBuffer.toString());
          yybegin(YYINITIAL); }

    \'  { stringBuffer.append('\'');
          errorHandler.reportMalformedLiteral(stringStartLine, stringStartCol,
              stringBuffer.toString(), "Empty character literal");
          makeToken(TokenType.ERROR, stringBuffer.toString());
          yybegin(YYINITIAL); }

    \\.     { stringBuffer.append(yytext());
              errorHandler.reportInvalidEscape(stringStartLine, stringStartCol,
                  yytext(), "Invalid escape sequence '" + yytext() + "'");
              /* try to consume closing quote */
              }

    \n      { errorHandler.reportUnterminatedChar(stringStartLine, stringStartCol,
                  stringBuffer.toString());
              makeToken(TokenType.ERROR, stringBuffer.toString());
              yybegin(YYINITIAL); }

    <<EOF>> { errorHandler.reportUnterminatedChar(stringStartLine, stringStartCol,
                  stringBuffer.toString());
              makeToken(TokenType.ERROR, stringBuffer.toString());
              yybegin(YYINITIAL);
              return makeToken(TokenType.EOF, ""); }

    .       { stringBuffer.append(yytext()); }
}
