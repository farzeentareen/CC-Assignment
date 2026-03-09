import java.util.ArrayList;
import java.util.List;

/**
 * Handles lexical error detection, reporting, and recovery.
 * Collects all errors encountered during scanning.
 */
public class ErrorHandler {

    /**
     * Represents a single lexical error.
     */
    public static class LexicalError {
        private String errorType;
        private int line;
        private int column;
        private String lexeme;
        private String reason;

        public LexicalError(String errorType, int line, int column, String lexeme, String reason) {
            this.errorType = errorType;
            this.line = line;
            this.column = column;
            this.lexeme = lexeme;
            this.reason = reason;
        }

        public String getErrorType() {
            return errorType;
        }

        public int getLine() {
            return line;
        }

        public int getColumn() {
            return column;
        }

        public String getLexeme() {
            return lexeme;
        }

        public String getReason() {
            return reason;
        }

        @Override
        public String toString() {
            return "[ERROR] Type: " + errorType +
                    ", Line: " + line +
                    ", Col: " + column +
                    ", Lexeme: \"" + lexeme +
                    "\", Reason: " + reason;
        }
    }

    private List<LexicalError> errors;

    public ErrorHandler() {
        errors = new ArrayList<>();
    }

    /**
     * Reports an invalid character error.
     */
    public void reportInvalidCharacter(int line, int col, String lexeme) {
        errors.add(new LexicalError("INVALID_CHARACTER", line, col, lexeme,
                "Character '" + lexeme + "' is not recognized in FlowLang"));
    }

    /**
     * Reports a malformed literal error (e.g., multiple decimals, too many decimal
     * digits).
     */
    public void reportMalformedLiteral(int line, int col, String lexeme, String reason) {
        errors.add(new LexicalError("MALFORMED_LITERAL", line, col, lexeme, reason));
    }

    /**
     * Reports an unterminated string literal.
     */
    public void reportUnterminatedString(int line, int col, String lexeme) {
        errors.add(new LexicalError("UNTERMINATED_STRING", line, col, lexeme,
                "String literal is not properly closed"));
    }

    /**
     * Reports an unterminated character literal.
     */
    public void reportUnterminatedChar(int line, int col, String lexeme) {
        errors.add(new LexicalError("UNTERMINATED_CHAR", line, col, lexeme,
                "Character literal is not properly closed"));
    }

    /**
     * Reports an invalid identifier.
     */
    public void reportInvalidIdentifier(int line, int col, String lexeme, String reason) {
        errors.add(new LexicalError("INVALID_IDENTIFIER", line, col, lexeme, reason));
    }

    /**
     * Reports an unclosed multi-line comment.
     */
    public void reportUnclosedComment(int line, int col, String lexeme) {
        errors.add(new LexicalError("UNCLOSED_COMMENT", line, col, lexeme,
                "Multi-line comment is not properly closed with *#"));
    }

    /**
     * Reports an invalid escape sequence in string/char literal.
     */
    public void reportInvalidEscape(int line, int col, String lexeme, String reason) {
        errors.add(new LexicalError("INVALID_ESCAPE", line, col, lexeme, reason));
    }

    /**
     * Returns true if any errors have been recorded.
     */
    public boolean hasErrors() {
        return !errors.isEmpty();
    }

    /**
     * Returns the list of all errors.
     */
    public List<LexicalError> getErrors() {
        return errors;
    }

    /**
     * Returns the total number of errors.
     */
    public int getErrorCount() {
        return errors.size();
    }

    /**
     * Prints all collected errors.
     */
    public void printErrors() {
        System.out.println("\n========================================");
        System.out.println("          LEXICAL ERRORS");
        System.out.println("========================================");
        if (errors.isEmpty()) {
            System.out.println("No lexical errors found.");
        } else {
            for (LexicalError error : errors) {
                System.out.println(error);
            }
            System.out.println("----------------------------------------");
            System.out.println("Total errors: " + errors.size());
        }
        System.out.println("========================================");
    }
}
