import java.util.Scanner;
import java.io.File;
import java.io.FileNotFoundException;

class StateTranstionTable {

    // columns
    public static final int START = 0;
    public static final int LETTER = 1;      // a-z, A-Z
    public static final int DIGIT = 2;       // 0-9
    public static final int DOT = 3;         // . (for floats)
    public static final int PLUS = 4;        // +
    public static final int MINUS = 5;       // -
    public static final int STAR = 6;        // *
    public static final int SLASH = 7;       // /
    public static final int EQUAL = 8;       // =
    public static final int LESS = 9;        // <
    public static final int GREATER = 10;    // >
    public static final int BANG = 11;       // !
    public static final int LPAREN = 12;      // (       
    public static final int RPAREN = 13;     // ): right parenthesis
    public static final int E_COL = 14;
    public static final int L_COL = 15;
    public static final int S_COL = 16;
    public static final int I_COL = 17;
    public static final int F_COL = 18;
    public static final int W_COL = 19;
    public static final int H_COL = 20;
    public static final int O_COL = 21;
    public static final int R_COL = 22;
    public static final int N_COL = 23;

    // rows
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
        {0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21, 22, 23, 24},
        {4, 5, 6},
        {7, 8, 9}
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