/**
 * Represents a single token produced by the lexical analyzer.
 * Compatible with both ManualScanner and JFlex-generated Yylex scanner.
 */
public class Token {
    private TokenType type;
    private String lexeme;
    private int line;
    private int column;

    public Token(TokenType type, String lexeme, int line, int column) {
        this.type = type;
        this.lexeme = lexeme;
        this.line = line;
        this.column = column;
    }

    public TokenType getType() {
        return type;
    }

    public String getLexeme() {
        return lexeme;
    }

    public int getLine() {
        return line;
    }

    public int getColumn() {
        return column;
    }

    /**
     * Returns the formatted token string as required:
     * <TYPE, "lexeme", Line: N, Col: M>
     */
    @Override
    public String toString() {
        String displayLexeme = lexeme.replace("\\", "\\\\")
                                     .replace("\n", "\\n")
                                     .replace("\t", "\\t")
                                     .replace("\r", "\\r");
        return "<" + type + ", \"" + displayLexeme + "\", Line: " + line + ", Col: " + column + ">";
    }
}
