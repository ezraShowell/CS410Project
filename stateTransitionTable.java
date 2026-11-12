import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

class StateTransitionTable {

    // columns/transitions
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
    
    // total number of column constants
    public static final int NUM_COLS = 25;   // number of columns

    public static final int WHITESPACE = -2; // arbitrary

    // rows/states
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
    public static final int S_O = 37;
    public static final int OR_KWD = 38;
    
    // total number of row constants
    public static final int NUM_ROWS = 39;

    public static final int[][] STATE_TRANSITION_TABLE = {
        //{LETTER, DIGIT, DOT, PLUS, MINUS, STAR, SLASH, EQUAL, LESS, GREATER, BANG, LPAREN, RPAREN, E_COL, L_COL, S_COL, I_COL, F_COL, W_COL, H_COL, O_COL, R_COL, N_COL, COLON, COL_OTHER},
    	{VAR_IDF, INT_LIT, -1, PLUS_OP, SUB_OP, MULT_OP, DIV_OP, ASSIGN_OP, LESSER_OP, GREATER_OP, BANG_OP, OPEN_PAREN, CLOSE_PAREN, S_E, VAR_IDF, VAR_IDF, S_I, S_F, S_W, VAR_IDF, S_O, VAR_IDF, VAR_IDF, COLON_SYM, -1}, // START
        {-1, INT_LIT, INT_DOT, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1}, // INT_LIT
        {-1, FLOAT_LIT, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1}, // INT_DOT
        {-1, FLOAT_LIT, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1}, // FLOAT_LIT
        {VAR_IDF, VAR_IDF, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, VAR_IDF, VAR_IDF, VAR_IDF, VAR_IDF, VAR_IDF, VAR_IDF, VAR_IDF, VAR_IDF, VAR_IDF, VAR_IDF, -1, -1}, // VAR_IDF
        {-1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1}, // OPEN_PAREN
        {-1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1}, // CLOSE_PAREN
        {VAR_IDF, VAR_IDF, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, VAR_IDF, VAR_IDF, VAR_IDF, VAR_IDF, VAR_IDF, VAR_IDF, S_WH, VAR_IDF, VAR_IDF, VAR_IDF, -1, -1}, // S_W
        {VAR_IDF, VAR_IDF, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, VAR_IDF, VAR_IDF, VAR_IDF, S_WHI, VAR_IDF, VAR_IDF, VAR_IDF, VAR_IDF, VAR_IDF, VAR_IDF, -1, -1}, // S_WH
        {VAR_IDF, VAR_IDF, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, VAR_IDF, S_WHIL, VAR_IDF, VAR_IDF, VAR_IDF, VAR_IDF, VAR_IDF, VAR_IDF, VAR_IDF, VAR_IDF, -1, -1}, // S_WHI
        {VAR_IDF, VAR_IDF, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, WHILE_KWD, VAR_IDF, VAR_IDF, VAR_IDF, VAR_IDF, VAR_IDF, VAR_IDF, VAR_IDF, VAR_IDF, VAR_IDF, -1, -1}, // S_WHIL
        {VAR_IDF, VAR_IDF, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, VAR_IDF, VAR_IDF, VAR_IDF, VAR_IDF, VAR_IDF, VAR_IDF, VAR_IDF, VAR_IDF, VAR_IDF, VAR_IDF, -1, -1}, // WHILE_KWD
        {VAR_IDF, VAR_IDF, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, VAR_IDF, VAR_IDF, VAR_IDF, VAR_IDF, IF_KWD, VAR_IDF, VAR_IDF, VAR_IDF, VAR_IDF, IN_KWD, -1, -1}, // S_I
        {VAR_IDF, VAR_IDF, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, VAR_IDF, VAR_IDF, VAR_IDF, VAR_IDF, VAR_IDF, VAR_IDF, VAR_IDF, VAR_IDF, VAR_IDF, VAR_IDF, -1, -1}, // IN_KWD
        {VAR_IDF, VAR_IDF, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, VAR_IDF, VAR_IDF, VAR_IDF, VAR_IDF, VAR_IDF, VAR_IDF, VAR_IDF, VAR_IDF, VAR_IDF, VAR_IDF, -1, -1}, // IF_KWD
        {VAR_IDF, VAR_IDF, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, VAR_IDF, VAR_IDF, VAR_IDF, VAR_IDF, VAR_IDF, VAR_IDF, VAR_IDF, S_FO, VAR_IDF, VAR_IDF, -1, -1}, // S_F
        {VAR_IDF, VAR_IDF, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, VAR_IDF, VAR_IDF, VAR_IDF, VAR_IDF, VAR_IDF, VAR_IDF, VAR_IDF, VAR_IDF, FOR_KWD, VAR_IDF, -1, -1}, // S_FO
        {VAR_IDF, VAR_IDF, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, VAR_IDF, VAR_IDF, VAR_IDF, VAR_IDF, VAR_IDF, VAR_IDF, VAR_IDF, VAR_IDF, VAR_IDF, VAR_IDF, -1, -1}, // FOR_KWD
        {VAR_IDF, VAR_IDF, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, VAR_IDF, S_EL, VAR_IDF, VAR_IDF, VAR_IDF, VAR_IDF, VAR_IDF, VAR_IDF, VAR_IDF, VAR_IDF, -1, -1}, // S_E
        {VAR_IDF, VAR_IDF, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, VAR_IDF, VAR_IDF, S_ELS, S_ELI, VAR_IDF, VAR_IDF, VAR_IDF, VAR_IDF, VAR_IDF, VAR_IDF, -1, -1}, // S_EL
        {VAR_IDF, VAR_IDF, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, ELSE_KWD, VAR_IDF, VAR_IDF, VAR_IDF, VAR_IDF, VAR_IDF, VAR_IDF, VAR_IDF, VAR_IDF, VAR_IDF, -1, -1}, // S_ELS
        {VAR_IDF, VAR_IDF, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, VAR_IDF, VAR_IDF, VAR_IDF, VAR_IDF, ELIF_KWD, VAR_IDF, VAR_IDF, VAR_IDF, VAR_IDF, VAR_IDF, -1, -1}, // S_ELI
        {VAR_IDF, VAR_IDF, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, VAR_IDF, VAR_IDF, VAR_IDF, VAR_IDF, VAR_IDF, VAR_IDF, VAR_IDF, VAR_IDF, VAR_IDF, VAR_IDF, -1, -1}, // ELIF_KWD
        {VAR_IDF, VAR_IDF, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, VAR_IDF, VAR_IDF, VAR_IDF, VAR_IDF, VAR_IDF, VAR_IDF, VAR_IDF, VAR_IDF, VAR_IDF, VAR_IDF, -1, -1}, // ELSE_KWD
        {-1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1}, // DIV_OP
        {-1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1}, // PLUS_OP
        {-1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1}, // SUB_OP
        {-1, -1, -1, -1, -1, -1, -1, EQUAL_OP, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1}, // ASSIGN_OP
        {-1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1}, // EQUAL_OP
        {-1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1}, // MULT_OP
        {-1, -1, -1, -1, -1, -1, -1, INEQUAL_OP, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1}, // BANG_OP
        {-1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1}, // INEQUAL_OP
        {-1, -1, -1, -1, -1, -1, -1, GREATER_EQUAL_OP, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1}, // GREATER_OP
        {-1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1}, // GREATER_EQUAL_OP
        {-1, -1, -1, -1, -1, -1, -1, LESSER_EQUAL_OP, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1}, // LESSER_OP
        {-1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1}, // LESSER_EQUAL_OP
        {-1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1}, // COLON_SYM
        {VAR_IDF, VAR_IDF, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, VAR_IDF, VAR_IDF, VAR_IDF, VAR_IDF, VAR_IDF, VAR_IDF, VAR_IDF, VAR_IDF, OR_KWD, VAR_IDF, -1, -1}, // S_O
        {VAR_IDF, VAR_IDF, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, VAR_IDF, VAR_IDF, VAR_IDF, VAR_IDF, VAR_IDF, VAR_IDF, VAR_IDF, VAR_IDF, VAR_IDF, VAR_IDF, -1, -1}, // OR_KWD
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
 
        int pos = 0; // 0
        int row = START; // 0 
        StringBuilder lexeme = new StringBuilder();
 
        while (pos < lines.length()) {
            char c = lines.charAt(pos);
            int col = getColumn(c);
 
            if (col == WHITESPACE) {
                if (row != START) { // "if a token is not in progress"
                    emitToken(row, lexeme);
                } 
                // else: whitespace between tokens, nothing to flush
                row = START;
                lexeme.setLength(0);
                pos++;
                continue;
            }

            // if it didn't match anything, our parser can't read it
            if (col == COL_OTHER) {
                System.out.println("Error: Invalid character '" + c + "' at position " + pos);
                return;
            }
 
            int next = STATE_TRANSITION_TABLE[row][col];
 
            if (next == -1) {
                // dead transition
                if (row != START) { // "if a token is not in progress"
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
 //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    private static void emitToken(int row, StringBuilder lexeme) {
        System.out.println(lexeme + "~" + getType(row) + "\t");
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

        if (Character.isLetter(c)) {
			return LETTER;
		}
        
        if (Character.isDigit(c)) {
			return DIGIT;
		}
        
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
            case ' ': case '\t': case '\n': case '\r': return WHITESPACE;
            default:  return COL_OTHER;
        }
}
    
    public static String getType (int state) {
    	// handle when a var_idf is part of a keyword ex: wh, els
		if (state == S_W || state == S_WH || state == S_WHI || state == S_WHIL || state == S_I || state == S_F || state == S_FO || state == S_E || state == S_EL || state == S_ELS || state == S_ELI || state == S_O) {
			return "VAR";
		}
        switch (state) {
            case INT_LIT: return "CON";
            case FLOAT_LIT: return "CON";
            case VAR_IDF: return "VAR";
            case OPEN_PAREN: return "SYM";
            case CLOSE_PAREN: return "SYM";
            case WHILE_KWD: return "WHILE_KWD";
            case IN_KWD: return "_INKWD";
            case IF_KWD: return "IF_KWD";
            case FOR_KWD: return "FOR_KWD";
            case ELIF_KWD: return "ELIF_KWD";
            case ELSE_KWD: return "ELSE_KWD";
            case DIV_OP: return "DIV_OP";
            case PLUS_OP: return "PLUS_OP";
            case SUB_OP: return "SUB_OP";
            case ASSIGN_OP: return "ASSIGN_OP";
            case EQUAL_OP: return "EQUAL_OP";
            case MULT_OP: return "MULT_OP";
            case INEQUAL_OP: return "INEQUAL_OP";
            case GREATER_OP: return "GREATER_OP";
            case GREATER_EQUAL_OP: return "GREATER_EQUAL_OP";
            case LESSER_OP: return "LESSER_OP";
            case LESSER_EQUAL_OP: return "LESSER_EQUAL_OP";
            default: return null;
        }
    }
    
    


}

