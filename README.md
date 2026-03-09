# FlowLang Lexical Analyzer

## Team Members
| Name | Roll Number |
|------|------------|
| Member 1 | 23i0721 |
| Member 2 | 23i0036 |

## Language: FlowLang
- **File Extension**: `.lang`
- **Case Sensitivity**: Keywords are case-sensitive (all lowercase). Identifiers must start with uppercase.

---

## Keywords
| Keyword | Meaning |
|---------|---------|
| `start` | Program entry point |
| `finish` | Program end |
| `loop` | Iteration construct |
| `condition` | Conditional branching (if) |
| `declare` | Variable declaration |
| `output` | Print to console |
| `input` | Read from console |
| `function` | Function definition |
| `return` | Return from function |
| `break` | Exit loop |
| `continue` | Skip to next iteration |
| `else` | Alternative branch |

## Identifier Rules
- **Pattern**: `[A-Z][a-z0-9_]{0,30}`
- Must start with an uppercase letter (A-Z)
- Followed by lowercase letters, digits, or underscores
- Maximum 31 characters total
- **Valid**: `Count`, `Variable_name`, `X`, `Total_sum_2024`
- **Invalid**: `count` (lowercase start), `2Count` (digit start), `myVariable` (lowercase start)

## Literal Formats

### Integer Literals
- **Pattern**: `[0-9]+`
- Examples: `42`, `0`, `100`, `999`

### Floating-Point Literals
- **Pattern**: `[0-9]+\.[0-9]{1,6}([eE][+-]?[0-9]+)?`
- Maximum 6 decimal digits
- Optional exponent with `e` or `E`
- Examples: `3.14`, `2.5`, `0.123456`, `1.5e10`, `2.0E-3`

### String Literals
- Enclosed in double quotes `"..."`
- Escape sequences: `\"`, `\\`, `\n`, `\t`, `\r`
- Examples: `"Hello"`, `"Line1\nLine2"`, `"C:\\path"`

### Character Literals
- Enclosed in single quotes `'...'`
- Single character or escape sequence
- Escape sequences: `\'`, `\\`, `\n`, `\t`, `\r`
- Examples: `'A'`, `'\n'`, `'\\'`

### Boolean Literals
- `true`, `false` (case-sensitive)

## Operators

| Category | Operators | Precedence |
|----------|-----------|------------|
| Exponentiation | `**` | Highest |
| Unary | `!`, `++`, `--` | High |
| Multiplicative | `*`, `/`, `%` | Medium-High |
| Additive | `+`, `-` | Medium |
| Relational | `<`, `>`, `<=`, `>=` | Medium-Low |
| Equality | `==`, `!=` | Low |
| Logical AND | `&&` | Lower |
| Logical OR | `\|\|` | Lowest |
| Assignment | `=`, `+=`, `-=`, `*=`, `/=` | Right-to-left |

## Comment Syntax
- **Single-line**: `## comment text`
- **Multi-line**: `#* comment text *#`

## Punctuators
`(`, `)`, `{`, `}`, `[`, `]`, `,`, `;`, `:`

---

## Sample Programs

### Program 1: Hello World
```
start
output("Hello, World!");
finish
```

### Program 2: Factorial
```
start

declare N = 5;
declare Result = 1;

loop (N > 1) {
    Result *= N;
    N--;
}

output("Factorial: ");
output(Result);

finish
```

### Program 3: Fibonacci
```
start

declare A = 0;
declare B = 1;
declare Count = 10;
declare Temp = 0;

output("Fibonacci: ");
loop (Count > 0) {
    output(A);
    output(" ");
    Temp = A + B;
    A = B;
    B = Temp;
    Count--;
}

finish
```

---

## Compilation & Execution Instructions

### Prerequisites
- Java Development Kit (JDK) 8 or later
- JFlex (optional, only needed to regenerate `Yylex.java`)

### Manual Scanner (VS Code / Eclipse Terminal)

```bash
# Navigate to source directory
cd 23i0721-23i0036-A/src

# Compile all Java files
javac TokenType.java Token.java SymbolTable.java ErrorHandler.java ManualScanner.java Main.java

# Run on a test file
java Main ../tests/test1.lang
java Main ../tests/test2.lang
java Main ../tests/test3.lang
java Main ../tests/test4.lang
java Main ../tests/test5.lang
```

### JFlex Scanner

```bash
# Step 1: Generate Yylex.java from Scanner.flex (requires JFlex)
# Download jflex-full-1.9.1.jar from https://jflex.de/
java -jar jflex-full-1.9.1.jar Scanner.flex

# Step 2: Compile
javac TokenType.java Token.java SymbolTable.java ErrorHandler.java Yylex.java JFlexScannerDriver.java

# Step 3: Run
java JFlexScannerDriver ../tests/test1.lang
```

### Eclipse IDE Setup
1. Create a new Java Project
2. Copy all `.java` files from `src/` into the `src` folder of the project
3. Right-click `Main.java` → Run As → Run Configurations
4. Set Program Arguments to the path of a test file (e.g., `tests/test1.lang`)
5. Click Run

### VS Code Setup
1. Open the `23i0721-23i0036-G-D` folder in VS Code
2. Install the "Extension Pack for Java" if not already installed
3. Open a terminal (`Ctrl+`` `)
4. Follow the command-line compilation steps above
