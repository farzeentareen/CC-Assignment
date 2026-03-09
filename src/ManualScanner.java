import java.io.*;
import java.util.*;

/**
 * Manual DFA-based Lexical Analyzer for FlowLang.
 * Reads source code character-by-character and produces tokens
 * using longest-match principle with correct priority ordering.
 */
public class ManualScanner {

    private String source;
    private int pos;
    private int line;
    private int column;
    private List<Token> tokens;
    private ErrorHandler errorHandler;
    private SymbolTable symbolTable;

    // Statistics
    private int totalTokens;
    private Map<TokenType, Integer> tokenCounts;
    private int linesProcessed;
    private int commentsRemoved;

    // Keywords set
    private static final Set<String> KEYWORDS = new HashSet<>(Arrays.asList(
            "start", "finish", "loop", "condition", "declare", "output",
            "input", "function", "return", "break", "continue", "else"));

    // Boolean literals
    private static final Set<String> BOOLEANS = new HashSet<>(Arrays.asList(
            "true", "false"));

    public ManualScanner(String source) {
        this.source = source;
        this.pos = 0;
        this.line = 1;
        this.column = 1;
        this.tokens = new ArrayList<>();
        this.errorHandler = new ErrorHandler();
        this.symbolTable = new SymbolTable();
        this.totalTokens = 0;
        this.tokenCounts = new LinkedHashMap<>();
        for (TokenType t : TokenType.values()) {
            tokenCounts.put(t, 0);
        }
        this.linesProcessed = 1;
        this.commentsRemoved = 0;
    }

    /**
     * Reads a source file and creates a ManualScanner.
     */
    public static ManualScanner fromFile(String filename) throws IOException {
        StringBuilder sb = new StringBuilder();
        BufferedReader reader = new BufferedReader(new FileReader(filename));
        int ch;
        while ((ch = reader.read()) != -1) {
            sb.append((char) ch);
        }
        reader.close();
        return new ManualScanner(sb.toString());
    }

    // ============================================================
    // Helper methods
    // ============================================================

    private char peek() {
        if (pos >= source.length())
            return '\0';
        return source.charAt(pos);
    }

    private char peekAhead(int offset) {
        int idx = pos + offset;
        if (idx >= source.length())
            return '\0';
        return source.charAt(idx);
    }

    private char advance() {
        char ch = source.charAt(pos);
        pos++;
        if (ch == '\n') {
            line++;
            column = 1;
        } else {
            column++;
        }
        return ch;
    }

    private boolean isAtEnd() {
        return pos >= source.length();
    }

    private void addToken(TokenType type, String lexeme, int startLine, int startCol) {
        Token token = new Token(type, lexeme, startLine, startCol);
        tokens.add(token);
        totalTokens++;
        tokenCounts.put(type, tokenCounts.get(type) + 1);
    }

    // ============================================================
    // Main scanning loop
    // ============================================================

    /**
     * Scans the entire source and returns the list of tokens.
     */
    public List<Token> scanTokens() {
        while (!isAtEnd()) {
            scanToken();
        }

        // Count lines processed
        linesProcessed = line;
        if (source.length() > 0 && source.charAt(source.length() - 1) != '\n') {
            // last line doesn't end with newline, already counted
        }

        addToken(TokenType.EOF, "", line, column);
        return tokens;
    }

    /**
     * Scans a single token, following the priority order:
     * 1. Multi-line comments
     * 2. Single-line comments
     * 3. Multi-character operators
     * 4. Keywords (handled after scanning word)
     * 5. Boolean literals (handled after scanning word)
     * 6. Identifiers (handled after scanning word)
     * 7. Floating-point literals
     * 8. Integer literals
     * 9. String/character literals
     * 10. Single-character operators
     * 11. Punctuators
     * 12. Whitespace
     */
    private void scanToken() {
        int startLine = line;
        int startCol = column;
        char c = peek();

        // 1 & 2. Comments (start with #)
        if (c == '#') {
            if (peekAhead(1) == '*') {
                scanMultiLineComment(startLine, startCol);
                return;
            } else if (peekAhead(1) == '#') {
                scanSingleLineComment(startLine, startCol);
                return;
            }
            // Single '#' is not valid — fall through to error
        }

        // 3. Multi-character operators (check before single-char)
        if (tryMultiCharOperator(startLine, startCol)) {
            return;
        }

        // 4, 5, 6. Words: keywords, booleans, identifiers
        if (Character.isLetter(c) || c == '_') {
            scanWord(startLine, startCol);
            return;
        }

        // 7 & 8. Numeric literals (integer and float)
        if (Character.isDigit(c)) {
            scanNumber(startLine, startCol);
            return;
        }

        // Handle +/- that could be part of a number — but since we already
        // checked multi-char operators (++, +=, --, -=), and single +/-
        // are operators, we treat standalone +/- as operators.

        // 9. String literals
        if (c == '"') {
            scanStringLiteral(startLine, startCol);
            return;
        }

        // 9. Character literals
        if (c == '\'') {
            scanCharLiteral(startLine, startCol);
            return;
        }

        // 10. Single-character operators (already handled partially in multi-char)
        if (trySingleCharOperator(startLine, startCol)) {
            return;
        }

        // 11. Punctuators
        if (isPunctuator(c)) {
            advance();
            addToken(TokenType.PUNCTUATOR, String.valueOf(c), startLine, startCol);
            return;
        }

        // 12. Whitespace
        if (Character.isWhitespace(c)) {
            scanWhitespace();
            return;
        }

        // Error: invalid character
        advance();
        errorHandler.reportInvalidCharacter(startLine, startCol, String.valueOf(c));
        addToken(TokenType.ERROR, String.valueOf(c), startLine, startCol);
    }

    // ============================================================
    // Comment scanning
    // ============================================================

    private void scanMultiLineComment(int startLine, int startCol) {
        StringBuilder sb = new StringBuilder();
        sb.append(advance()); // #
        sb.append(advance()); // *

        boolean closed = false;
        while (!isAtEnd()) {
            if (peek() == '*' && peekAhead(1) == '#') {
                sb.append(advance()); // *
                sb.append(advance()); // #
                closed = true;
                break;
            }
            sb.append(advance());
        }

        if (!closed) {
            errorHandler.reportUnclosedComment(startLine, startCol, sb.toString());
            addToken(TokenType.ERROR, sb.toString(), startLine, startCol);
        } else {
            addToken(TokenType.MULTI_LINE_COMMENT, sb.toString(), startLine, startCol);
            commentsRemoved++;
        }
    }

    private void scanSingleLineComment(int startLine, int startCol) {
        StringBuilder sb = new StringBuilder();
        sb.append(advance()); // #
        sb.append(advance()); // #

        while (!isAtEnd() && peek() != '\n') {
            sb.append(advance());
        }

        addToken(TokenType.SINGLE_LINE_COMMENT, sb.toString(), startLine, startCol);
        commentsRemoved++;
    }

    // ============================================================
    // Operator scanning
    // ============================================================

    private boolean tryMultiCharOperator(int startLine, int startCol) {
        char c = peek();
        char next = peekAhead(1);

        // Two-character operators
        String twoChar = "" + c + next;

        switch (twoChar) {
            case "**":
                advance();
                advance();
                addToken(TokenType.ARITHMETIC_OP, "**", startLine, startCol);
                return true;
            case "==":
            case "!=":
            case "<=":
            case ">=":
                advance();
                advance();
                addToken(TokenType.RELATIONAL_OP, twoChar, startLine, startCol);
                return true;
            case "&&":
            case "||":
                advance();
                advance();
                addToken(TokenType.LOGICAL_OP, twoChar, startLine, startCol);
                return true;
            case "++":
            case "--":
                advance();
                advance();
                addToken(TokenType.INC_DEC_OP, twoChar, startLine, startCol);
                return true;
            case "+=":
            case "-=":
            case "*=":
            case "/=":
                advance();
                advance();
                addToken(TokenType.ASSIGNMENT_OP, twoChar, startLine, startCol);
                return true;
        }

        return false;
    }

    private boolean trySingleCharOperator(int startLine, int startCol) {
        char c = peek();

        switch (c) {
            case '+':
            case '-':
            case '*':
            case '/':
            case '%':
                advance();
                addToken(TokenType.ARITHMETIC_OP, String.valueOf(c), startLine, startCol);
                return true;
            case '<':
            case '>':
                advance();
                addToken(TokenType.RELATIONAL_OP, String.valueOf(c), startLine, startCol);
                return true;
            case '!':
                advance();
                addToken(TokenType.LOGICAL_OP, "!", startLine, startCol);
                return true;
            case '=':
                advance();
                addToken(TokenType.ASSIGNMENT_OP, "=", startLine, startCol);
                return true;
        }

        return false;
    }

    // ============================================================
    // Word scanning (keywords, booleans, identifiers)
    // ============================================================

    private void scanWord(int startLine, int startCol) {
        StringBuilder sb = new StringBuilder();
        char first = peek();

        // Check if it starts with uppercase (potential identifier)
        boolean startsWithUpper = Character.isUpperCase(first);
        // Check if it starts with lowercase (potential keyword/boolean)
        boolean startsWithLower = Character.isLowerCase(first);

        sb.append(advance());

        if (startsWithUpper) {
            // Identifier: [A-Z][a-z0-9_]{0,30}
            while (!isAtEnd() && sb.length() < 31) {
                char ch = peek();
                if (Character.isLowerCase(ch) || Character.isDigit(ch) || ch == '_') {
                    sb.append(advance());
                } else {
                    break;
                }
            }

            String word = sb.toString();

            // Check if the identifier exceeds max length by peeking further
            if (!isAtEnd()) {
                char ch = peek();
                if (Character.isLowerCase(ch) || Character.isDigit(ch) || ch == '_') {
                    // Still more valid chars — identifier too long
                    while (!isAtEnd()) {
                        ch = peek();
                        if (Character.isLowerCase(ch) || Character.isDigit(ch) || ch == '_') {
                            sb.append(advance());
                        } else {
                            break;
                        }
                    }
                    word = sb.toString();
                    errorHandler.reportInvalidIdentifier(startLine, startCol, word,
                            "Identifier exceeds maximum length of 31 characters (length: " + word.length() + ")");
                    addToken(TokenType.ERROR, word, startLine, startCol);
                    return;
                }
            }

            addToken(TokenType.IDENTIFIER, word, startLine, startCol);
            symbolTable.addIdentifier(word, startLine, startCol);

        } else if (startsWithLower) {
            // Could be keyword, boolean, or invalid identifier
            while (!isAtEnd()) {
                char ch = peek();
                if (Character.isLetter(ch) || Character.isDigit(ch) || ch == '_') {
                    sb.append(advance());
                } else {
                    break;
                }
            }

            String word = sb.toString();

            if (KEYWORDS.contains(word)) {
                addToken(TokenType.KEYWORD, word, startLine, startCol);
            } else if (BOOLEANS.contains(word)) {
                addToken(TokenType.BOOLEAN_LITERAL, word, startLine, startCol);
            } else {
                // Lowercase start — invalid identifier
                errorHandler.reportInvalidIdentifier(startLine, startCol, word,
                        "Identifier must start with an uppercase letter (A-Z)");
                addToken(TokenType.ERROR, word, startLine, startCol);
            }

        } else {
            // Starts with underscore — invalid identifier start
            while (!isAtEnd()) {
                char ch = peek();
                if (Character.isLetter(ch) || Character.isDigit(ch) || ch == '_') {
                    sb.append(advance());
                } else {
                    break;
                }
            }
            String word = sb.toString();
            errorHandler.reportInvalidIdentifier(startLine, startCol, word,
                    "Identifier must start with an uppercase letter (A-Z)");
            addToken(TokenType.ERROR, word, startLine, startCol);
        }
    }

    // ============================================================
    // Number scanning (integer and float)
    // ============================================================

    private void scanNumber(int startLine, int startCol) {
        StringBuilder sb = new StringBuilder();

        // Integer part
        while (!isAtEnd() && Character.isDigit(peek())) {
            sb.append(advance());
        }

        // Check for float
        if (!isAtEnd() && peek() == '.' && Character.isDigit(peekAhead(1))) {
            sb.append(advance()); // consume '.'

            int decimalDigits = 0;
            while (!isAtEnd() && Character.isDigit(peek())) {
                sb.append(advance());
                decimalDigits++;
            }

            if (decimalDigits > 6) {
                errorHandler.reportMalformedLiteral(startLine, startCol, sb.toString(),
                        "Floating-point literal has more than 6 decimal digits");
                addToken(TokenType.ERROR, sb.toString(), startLine, startCol);
                return;
            }

            // Check for exponent
            if (!isAtEnd() && (peek() == 'e' || peek() == 'E')) {
                sb.append(advance()); // consume 'e' or 'E'

                if (!isAtEnd() && (peek() == '+' || peek() == '-')) {
                    sb.append(advance()); // consume sign
                }

                if (isAtEnd() || !Character.isDigit(peek())) {
                    errorHandler.reportMalformedLiteral(startLine, startCol, sb.toString(),
                            "Floating-point literal has incomplete exponent");
                    addToken(TokenType.ERROR, sb.toString(), startLine, startCol);
                    return;
                }

                while (!isAtEnd() && Character.isDigit(peek())) {
                    sb.append(advance());
                }
            }

            addToken(TokenType.FLOAT_LITERAL, sb.toString(), startLine, startCol);
        } else if (!isAtEnd() && peek() == '.') {
            // e.g., "3." with no digit after dot
            sb.append(advance());
            errorHandler.reportMalformedLiteral(startLine, startCol, sb.toString(),
                    "Floating-point literal must have digits after decimal point");
            addToken(TokenType.ERROR, sb.toString(), startLine, startCol);
        } else {
            addToken(TokenType.INTEGER_LITERAL, sb.toString(), startLine, startCol);
        }
    }

    // ============================================================
    // String literal scanning
    // ============================================================

    private void scanStringLiteral(int startLine, int startCol) {
        StringBuilder sb = new StringBuilder();
        sb.append(advance()); // opening "

        boolean closed = false;
        while (!isAtEnd()) {
            char c = peek();

            if (c == '\n') {
                // Unterminated string (newline inside string)
                break;
            }

            if (c == '\\') {
                sb.append(advance()); // backslash
                if (!isAtEnd()) {
                    char escaped = peek();
                    if (escaped == '"' || escaped == '\\' || escaped == 'n' ||
                            escaped == 't' || escaped == 'r') {
                        sb.append(advance());
                    } else {
                        // Invalid escape sequence
                        sb.append(advance());
                        errorHandler.reportInvalidEscape(startLine, startCol,
                                "\\" + escaped, "Invalid escape sequence '\\" + escaped + "'");
                    }
                }
                continue;
            }

            if (c == '"') {
                sb.append(advance()); // closing "
                closed = true;
                break;
            }

            sb.append(advance());
        }

        if (!closed) {
            errorHandler.reportUnterminatedString(startLine, startCol, sb.toString());
            addToken(TokenType.ERROR, sb.toString(), startLine, startCol);
        } else {
            addToken(TokenType.STRING_LITERAL, sb.toString(), startLine, startCol);
        }
    }

    // ============================================================
    // Character literal scanning
    // ============================================================

    private void scanCharLiteral(int startLine, int startCol) {
        StringBuilder sb = new StringBuilder();
        sb.append(advance()); // opening '

        boolean closed = false;

        if (!isAtEnd() && peek() != '\n') {
            char c = peek();

            if (c == '\\') {
                sb.append(advance()); // backslash
                if (!isAtEnd() && peek() != '\n') {
                    char escaped = peek();
                    if (escaped == '\'' || escaped == '\\' || escaped == 'n' ||
                            escaped == 't' || escaped == 'r') {
                        sb.append(advance());
                    } else {
                        sb.append(advance());
                        errorHandler.reportInvalidEscape(startLine, startCol,
                                "\\" + escaped, "Invalid escape sequence '\\" + escaped + "'");
                    }
                }
            } else if (c == '\'') {
                // Empty char literal ''
                sb.append(advance());
                errorHandler.reportMalformedLiteral(startLine, startCol, sb.toString(),
                        "Empty character literal");
                addToken(TokenType.ERROR, sb.toString(), startLine, startCol);
                return;
            } else {
                sb.append(advance());
            }

            // Expect closing quote
            if (!isAtEnd() && peek() == '\'') {
                sb.append(advance());
                closed = true;
            }
        }

        if (!closed) {
            errorHandler.reportUnterminatedChar(startLine, startCol, sb.toString());
            addToken(TokenType.ERROR, sb.toString(), startLine, startCol);
        } else {
            addToken(TokenType.CHAR_LITERAL, sb.toString(), startLine, startCol);
        }
    }

    // ============================================================
    // Whitespace scanning
    // ============================================================

    private void scanWhitespace() {
        while (!isAtEnd() && Character.isWhitespace(peek())) {
            advance();
        }
        // Whitespace is skipped but line/column tracking happens in advance()
    }

    // ============================================================
    // Punctuator check
    // ============================================================

    private boolean isPunctuator(char c) {
        return c == '(' || c == ')' || c == '{' || c == '}' ||
                c == '[' || c == ']' || c == ',' || c == ';' || c == ':';
    }

    // ============================================================
    // Accessors
    // ============================================================

    public List<Token> getTokens() {
        return tokens;
    }

    public ErrorHandler getErrorHandler() {
        return errorHandler;
    }

    public SymbolTable getSymbolTable() {
        return symbolTable;
    }

    public int getTotalTokens() {
        return totalTokens;
    }

    public Map<TokenType, Integer> getTokenCounts() {
        return tokenCounts;
    }

    public int getLinesProcessed() {
        return linesProcessed;
    }

    public int getCommentsRemoved() {
        return commentsRemoved;
    }

    // ============================================================
    // Statistics printing
    // ============================================================

    public void printStatistics() {
        System.out.println("\n========================================");
        System.out.println("          SCANNER STATISTICS");
        System.out.println("========================================");
        System.out.println("Total tokens:      " + totalTokens);
        System.out.println("Lines processed:   " + linesProcessed);
        System.out.println("Comments removed:  " + commentsRemoved);
        System.out.println("\nToken counts by type:");
        System.out.println("----------------------------------------");
        for (Map.Entry<TokenType, Integer> entry : tokenCounts.entrySet()) {
            if (entry.getValue() > 0) {
                System.out.printf("  %-25s %d%n", entry.getKey(), entry.getValue());
            }
        }
        System.out.println("========================================");
    }
}
