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
    public static final int UNDERSCORE = 23; // _
    public static final int COL_OTHER = 24;  // other characters

    public static final int WHITESPACE = -2; // hard-coded

    // rows
    public static final int START = 0;
    public static final int INT_LIT = 1;
    public static final int FLOAT_LIT = 2;
    public static final int VAR_IDF = 3;
    public static final int OPEN_PAREN = 4;
    public static final int CLOSE_PAREN = 5;
    public static final int WHILE_KWD = 6;
    public static final int IN_KWD = 7;
    public static final int IF_KWD = 8;
    public static final int FOR_KWD = 9;
    public static final int ELIF_KWD = 10;
    public static final int ELSE_KWD = 11;
    public static final int DIV_OP = 12;
    public static final int PLUS_OP = 13;
    public static final int SUB_OP = 14;
    public static final int ASSIGN_OP = 15;
    public static final int EQUAL_OP = 16;
    public static final int MULT_OP = 17;
    public static final int INEQUAL_OP = 18;
    public static final int GREATER_OP = 19;
    public static final int GREATER_EQUAL_OP = 20;
    public static final int LESSER_OP = 21;
    public static final int LESSER_EQUAL_OP = 22;


    public static final int[][] STATE_TRANSITION_TABLE = {
    { VAR_IDF, INT_LIT, -1,        PLUS_OP, SUB_OP, MULT_OP, DIV_OP, ASSIGN_OP, LESSER_OP, GREATER_OP, BANG_STATE, OPEN_PAREN, CLOSE_PAREN, -1,    E_STATE, VAR_IDF, VAR_IDF, I_STATE, F_STATE, W_STATE, VAR_IDF, VAR_IDF, VAR_IDF, VAR_IDF, VAR_IDF }, // START
    { -1,      INT_LIT, DOT_STATE, -1,      -1,     -1,      -1,     -1,        -1,        -1,         -1,         -1,         -1,          -1,    -1,      -1,      -1,      -1,      -1,      -1,      -1,      -1,      -1,      -1,      -1      }, // INT_LIT
    // ...one row like this for every state, in the same order as your row constants
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
        int tokenStart = 0;
 
        while (pos < lines.length()) {
            char c = lines.charAt(pos);
            int col = getColumn(c);
 
            if (col == WHITESPACE) {
                if (row != START) {
                    emitToken(row, lexeme, tokenStart);
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
                    emitToken(row, lexeme, tokenStart);
                    row = START;
                    lexeme.setLength(0);
                    // do no advance pos, reprocess from START
                    continue;
                } else {
                    System.out.println("Error: Invalid token at position " + pos + " ('" + c + "')");
                    return;
                }
            }
 
            if (row == START) {
                tokenStart = pos;
            }
            row = next;
            lexeme.append(c);
            pos++;
        }
 
        // flush whatever token was still being built
        if (row != START) {
            emitToken(row, lexeme, tokenStart);
        }
    }
 
    private static void emitToken(int row, StringBuilder lexeme, int tokenStart) {
        String name = (row >= 0 && row < ROW_NAMES.length) ? ROW_NAMES[row] : ("STATE_" + row);
        System.out.println(name + "\t\"" + lexeme + "\"\tat position " + tokenStart);
    }

    // helper function
    public static int getColumn(char c) {
        if (Character.isLetter(c)) {
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
                default:  return LETTER; // any other letter
            }   
        }
        if (Character.isDigit(c)) return DIGIT;
        if (Character.isWhitespace(c)) return -2; // whitespace - skip
        
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
            default:  return COL_OTHER;
        }
}


}