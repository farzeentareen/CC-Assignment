/**
 * Enumeration of all token types recognized by the FlowLang lexical analyzer.
 */
public enum TokenType {
    // Keywords
    KEYWORD,

    // Identifiers
    IDENTIFIER,

    // Literals
    INTEGER_LITERAL,
    FLOAT_LITERAL,
    STRING_LITERAL,
    CHAR_LITERAL,
    BOOLEAN_LITERAL,

    // Operators
    ARITHMETIC_OP,
    RELATIONAL_OP,
    LOGICAL_OP,
    ASSIGNMENT_OP,
    INC_DEC_OP,

    // Punctuators
    PUNCTUATOR,

    // Comments
    SINGLE_LINE_COMMENT,
    MULTI_LINE_COMMENT,

    // Whitespace (tracked but not emitted as token)
    WHITESPACE,

    // Error token
    ERROR,

    // End of file
    EOF
}
