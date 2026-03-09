import java.io.*;
import java.util.*;

/**
 * Driver program for the JFlex-generated Lexical Analyzer.
 * Usage: java JFlexScannerDriver <source-file.lang>
 */
public class JFlexScannerDriver {

    public static void main(String[] args) {
        if (args.length < 1) {
            System.out.println("Usage: java JFlexScannerDriver <source-file>");
            System.out.println("Example: java JFlexScannerDriver ../tests/test1.lang");
            System.exit(1);
        }

        String filename = args[0];

        try {
            System.out.println("========================================");
            System.out.println("  FlowLang JFlex Lexical Analyzer");
            System.out.println("========================================");
            System.out.println("Scanning file: " + filename);
            System.out.println();

            FileReader reader = new FileReader(filename);
            Yylex scanner = new Yylex(reader);

            // Scan all tokens
            Token token;
            while ((token = scanner.yylex()) != null) {
                // token already added to tokenList in makeToken()
            }

            // Print all tokens
            System.out.println("========================================");
            System.out.println("          TOKEN LIST");
            System.out.println("========================================");
            List<Token> tokenList = scanner.getTokenList();
            for (Token t : tokenList) {
                if (t.getType() != TokenType.WHITESPACE) {
                    System.out.println(t);
                }
            }

            // Print statistics
            Map<TokenType, Integer> counts = scanner.getTokenCounts();
            int totalTokens = tokenList.size();

            System.out.println("\n========================================");
            System.out.println("          SCANNER STATISTICS");
            System.out.println("========================================");
            System.out.println("Total tokens:      " + totalTokens);
            System.out.println("Comments removed:  " + scanner.getCommentsRemoved());
            System.out.println("\nToken counts by type:");
            System.out.println("----------------------------------------");
            for (Map.Entry<TokenType, Integer> entry : counts.entrySet()) {
                if (entry.getValue() > 0) {
                    System.out.printf("  %-25s %d%n", entry.getKey(), entry.getValue());
                }
            }
            System.out.println("========================================");

            // Print symbol table
            scanner.getSymbolTable().printTable();

            // Print errors
            scanner.getErrorHandler().printErrors();

            reader.close();

        } catch (FileNotFoundException e) {
            System.err.println("Error: File not found: " + filename);
            System.exit(1);
        } catch (IOException e) {
            System.err.println("Error reading file: " + e.getMessage());
            System.exit(1);
        }
    }
}
