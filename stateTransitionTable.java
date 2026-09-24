import java.io.*;
import java.nio.file.*;
import java.util.*;

class StateTranstionTable {

    // columns
    public static final int LETTER = 0;      // a-z, A-Z
    public static final int DIGIT = 1;       // 0-9
    public static final int DOT = 2;         // . (for floats)
    public static final int PLUS = 3;        // +
    public static final int MINUS = 4;       // -
    public static final int STAR = 5;        // *
    public static final int SLASH = 6;       // /
    public static final int EQUAL = 7;       // =
    public static final int LESS = 8;        // <
    public static final int GREATER = 9;     // >
    public static final int BANG = 10;       // !
    public static final int LPAREN = 11;     // (
    public static final int RPAREN = 12;     // ): right parenthesis
    public static final int E_COL = 13;      // e
    public static final int L_COL = 14;      // l
    public static final int S_COL = 15;      // s
    public static final int I_COL = 16;      // i
    public static final int F_COL = 17;      // f
    public static final int W_COL = 18;      // w
    public static final int H_COL = 19;      // h
    public static final int O_COL = 20;      // o
    public static final int R_COL = 21;      // r
    public static final int N_COL = 22;      // n
    public static final int COLON = 23;      // :
    public static final int COL_OTHER = 24;  // other characters
    public static final int NUM_COLS = 25;   // number of columns

    public static final int WHITESPACE = -2; // hard-coded

    // rows
    public static final int START = 0;
    public static final int INT_LIT = 1;
    public static final int INT_DOT = 2;
    public static final int FLOAT_LIT = 3;
    public static final int VAR_IDF = 4;
    public static final int OPEN_PAREN = 5;
    public static final int CLOSE_PAREN = 6;
    public static final int S_W = 7;
    public static final int S_WH = 8;
    public static final int S_WHI = 9;
    public static final int S_WHIL = 10;
    public static final int WHILE_KWD = 11;
    public static final int S_I = 12;
    public static final int IN_KWD = 13;
    public static final int IF_KWD = 14;
    public static final int S_F = 15;
    public static final int S_FO = 16;
    public static final int FOR_KWD = 17;
    public static final int S_E = 18;
    public static final int S_EL = 19;
    public static final int S_ELS = 20;
    public static final int S_ELI = 21;
    public static final int ELIF_KWD = 22;
    public static final int ELSE_KWD = 23;
    public static final int DIV_OP = 24;
    public static final int PLUS_OP = 25;
    public static final int SUB_OP = 26;
    public static final int ASSIGN_OP = 27;
    public static final int EQUAL_OP = 28;
    public static final int MULT_OP = 29;
    public static final int BANG_OP = 30;
    public static final int INEQUAL_OP = 31;
    public static final int GREATER_OP = 32;
    public static final int GREATER_EQUAL_OP = 33;
    public static final int LESSER_OP = 34;
    public static final int LESSER_EQUAL_OP = 35;
    public static final int COLON_SYM = 36;
    public static final int NUM_ROWS = 37;


    
};

    public static void main(String[] args) throws IOException {
 
        System.out.println("Lexical Analyzer, please enter your input file absolute path: ");
        BufferedReader stdin = new BufferedReader(new InputStreamReader(System.in));
        String inputFile = stdin.readLine();
 
        Path filePath = Paths.get(inputFile);
        String lines;
        try {
            if (!Files.exists(filePath)) {
                System.out.println("File does not exist.");
                return;
            }
            lines = Files.readString(filePath);
        } catch (Exception e) {
            System.out.println("Error checking file existence: " + e.getMessage());
            return;
        }
 
        int pos = 0;
        int row = START;
        StringBuilder lexeme = new StringBuilder();
 
        while (pos < lines.length()) {
            char c = lines.charAt(pos);
            int col = getColumn(c);
 
            if (col == WHITESPACE) {
                if (row != START) {
                    emitToken(row, lexeme);
                } // else: whitespace between tokens, nothing to flush
                row = START;
                lexeme.setLength(0);
                pos++;
                continue;
            }
 
            if (col == COL_OTHER) {
                System.out.println("Error: Invalid character '" + c + "' at position " + pos);
                return;
            }
 
            int next = STATE_TRANSITION_TABLE[row][col];
 
            if (next == -1) {
                // dead transition
                if (row != START) {
                    emitToken(row, lexeme);
                    row = START;
                    lexeme.setLength(0);
                    // do no advance pos
                    continue;
                } else {
                    System.out.println("Error: Invalid token at position " + pos + " ('" + c + "')");
                    return;
                }
            }
 
            row = next;
            lexeme.append(c);
            pos++;
        }
 
        // flush whatever token was still being built
        if (row != START) {
            emitToken(row, lexeme);
        }
    }
 
    private static void emitToken(int row, StringBuilder lexeme) {
        System.out.print(lexeme + "~" + getType(row) + "\t");
    }

    // helper function
    public static int getColumn(char c) {
        switch (c) {
            case 'e': return E_COL;
            case 'l': return L_COL;
            case 's': return S_COL;
            case 'i': return I_COL;
            case 'f': return F_COL;
            case 'w': return W_COL;
            case 'h': return H_COL;
            case 'o': return O_COL;
            case 'r': return R_COL;
            case 'n': return N_COL;
            case '_': return LETTER; // underscore is considered a letter for identifiers
        }

        if (Character.isLetter(c)) return LETTER;
        
        if (Character.isDigit(c)) return DIGIT;
        
        switch (c) {
            case '.': return DOT;
            case '+': return PLUS;
            case '-': return MINUS;
            case '*': return STAR;
            case '/': return SLASH;
            case '=': return EQUAL;
            case '<': return LESS;
            case '>': return GREATER;
            case '!': return BANG;
            case '(': return LPAREN;
            case ')': return RPAREN;
            case ':': return COLON;
            default:  return COL_OTHER;
        }
}
    
    public static String getType (int state) {
        switch (state) {
            case INT_LIT: return "CON";
            case FLOAT_LIT: return "CON";
            case VAR_IDF: return "VAR";
            case OPEN_PAREN: return "SYM";
            case CLOSE_PAREN: return "SYM";
            case WHILE_KWD: return "KWD";
            case IN_KWD: return "KWD";
            case IF_KWD: return "KWD";
            case FOR_KWD: return "KWD";
            case ELIF_KWD: return "KWD";
            case ELSE_KWD: return "KWD";
            case DIV_OP: return "OP";
            case PLUS_OP: return "OP";
            case SUB_OP: return "OP";
            case ASSIGN_OP: return "OP";
            case EQUAL_OP: return "OP";
            case MULT_OP: return "OP";
            case INEQUAL_OP: return "OP";
            case GREATER_OP: return "OP";
            case GREATER_EQUAL_OP: return "OP";
            case LESSER_OP: return "OP";
            case LESSER_EQUAL_OP: return "OP";
            default: return null;
        }
    }


}