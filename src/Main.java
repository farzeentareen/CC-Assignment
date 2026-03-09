import java.io.*;
import java.util.*;

/**
 * Driver program for the Manual Lexical Analyzer.
 * Usage: java Main <source-file.lang>
 */
public class Main {

    public static void main(String[] args) {
        if (args.length < 1) {
            System.out.println("Usage: java Main <source-file>");
            System.out.println("Example: java Main ../tests/test1.lang");
            System.exit(1);
        }

        String filename = args[0];

        try {
            System.out.println("========================================");
            System.out.println("  FlowLang Manual Lexical Analyzer");
            System.out.println("========================================");
            System.out.println("Scanning file: " + filename);
            System.out.println();

            ManualScanner scanner = ManualScanner.fromFile(filename);
            List<Token> tokens = scanner.scanTokens();

            // Print all tokens
            System.out.println("========================================");
            System.out.println("          TOKEN LIST");
            System.out.println("========================================");
            for (Token token : tokens) {
                if (token.getType() != TokenType.WHITESPACE) {
                    System.out.println(token);
                }
            }

            // Print statistics
            scanner.printStatistics();

            // Print symbol table
            scanner.getSymbolTable().printTable();

            // Print errors
            scanner.getErrorHandler().printErrors();

        } catch (FileNotFoundException e) {
            System.err.println("Error: File not found: " + filename);
            System.exit(1);
        } catch (IOException e) {
            System.err.println("Error reading file: " + e.getMessage());
            System.exit(1);
        }
    }
}
