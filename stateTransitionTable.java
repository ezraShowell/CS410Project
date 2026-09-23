import java.util.Scanner;
import java.io.File;
import java.io.FileNotFoundException;

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

    public static void main(String[] args) {

            // scanner logic\
                // follow fsm, (i.e if character is "i", check next character if it 
                // is "f" then it is a keyword, else it is an identifier)

            // remember to actually make the input file with valid syntax
            try(Scanner scanner = new Scanner(new File("input.txt"))){
                while(scanner.hasNextLine()){
                    String line = scanner.nextLine();
                }
            }
            catch (FileNotFoundException e) {
                e.printStackTrace();
            }
                
    }

    // helper function
    public int getColumn(char c) {
        if (Character.isLetter(c)) return LETTER;
        if (Character.isDigit(c)) return DIGIT;
        if (Character.isWhitespace(c)) return WS;
        
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