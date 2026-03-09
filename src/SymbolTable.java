import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Symbol table that stores identifier information including name,
 * type, first occurrence, and frequency.
 */
public class SymbolTable {

    /**
     * Represents a single entry in the symbol table.
     */
    public static class Entry {
        private String name;
        private String type; // e.g., "IDENTIFIER"
        private int firstLine;
        private int firstColumn;
        private int frequency;

        public Entry(String name, String type, int firstLine, int firstColumn) {
            this.name = name;
            this.type = type;
            this.firstLine = firstLine;
            this.firstColumn = firstColumn;
            this.frequency = 1;
        }

        public String getName() {
            return name;
        }

        public String getType() {
            return type;
        }

        public int getFirstLine() {
            return firstLine;
        }

        public int getFirstColumn() {
            return firstColumn;
        }

        public int getFrequency() {
            return frequency;
        }

        public void incrementFrequency() {
            frequency++;
        }

        @Override
        public String toString() {
            return String.format("%-25s %-15s %-15s %d",
                    name, type, firstLine + ":" + firstColumn, frequency);
        }
    }

    private LinkedHashMap<String, Entry> table;

    public SymbolTable() {
        table = new LinkedHashMap<>();
    }

    /**
     * Adds an identifier to the symbol table. If it already exists,
     * increments its frequency.
     */
    public void addIdentifier(String name, int line, int column) {
        if (table.containsKey(name)) {
            table.get(name).incrementFrequency();
        } else {
            table.put(name, new Entry(name, "IDENTIFIER", line, column));
        }
    }

    /**
     * Retrieves an entry by name.
     */
    public Entry getEntry(String name) {
        return table.get(name);
    }

    /**
     * Returns all entries in insertion order.
     */
    public Map<String, Entry> getEntries() {
        return table;
    }

    /**
     * Returns the number of unique identifiers.
     */
    public int size() {
        return table.size();
    }

    /**
     * Prints the symbol table in a formatted manner.
     */
    public void printTable() {
        System.out.println("\n========================================");
        System.out.println("          SYMBOL TABLE");
        System.out.println("========================================");
        System.out.printf("%-25s %-15s %-15s %s%n", "Name", "Type", "First Occur.", "Frequency");
        System.out.println("----------------------------------------");
        for (Entry entry : table.values()) {
            System.out.println(entry);
        }
        System.out.println("========================================");
        System.out.println("Total unique identifiers: " + table.size());
    }
}
