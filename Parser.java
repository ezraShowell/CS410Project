import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;

class Parser {

    static ArrayList<String> lexemes = new ArrayList<>();
    static ArrayList<String> tokens = new ArrayList<>();
    static ArrayList<String> atoms = new ArrayList<>();
    static int idx = 0;
    static String next = "";
    static String currentLexeme = "";

    static int tempCount = 0;
    static int labelCount = 0;

    public static void main(String[] args) throws IOException {
        System.err.println("Parser, please enter your scanner output file absolute path: ");
        BufferedReader stdin = new BufferedReader(new InputStreamReader(System.in));
        String line = stdin.readLine();
        if (line == null || line.trim().isEmpty()) {
            System.err.println("No input file provided.");
            return;
        }

        Path filePath = Paths.get(line.trim());
        if (!Files.exists(filePath)) {
            System.err.println("File does not exist.");
            return;
        }

        String content = Files.readString(filePath);
        loadScannerTokens(content);

        next = next_token();
        parse_PROGRAM();

        if (next.equals("EOF")) {
            for (String atom : atoms) {
                System.out.println(atom);
            }
            System.err.println("Accepted: Valid program.");
        } else {
            reject("Syntax Error: unexpected token " + next);
        }
    }

    // Reads scanner output lines formatted as: lexeme~TYPE
    public static void loadScannerTokens(String content) {
        String[] parts = content.trim().split("\\s+");
        for (String part : parts) {
            if (part.isEmpty() || !part.contains("~")) continue;
            String[] split = part.split("~");
            String lex = split[0];
            String type = split.length > 1 ? split[1] : "";

            switch (lex) {
                case "(":     type = "open_paren";  break;
                case ")":     type = "close_paren"; break;
                case ":":     type = "colon_sym";   break;
                case "range": type = "range_kwd";   break;
                case "or":    type = "or_kwd";      break;
                case "and":   type = "and_kwd";     break;
                case "in":    type = "in_kwd";      break;
                default:
                    switch (type) {
                        case "_INKWD":  type = "in_kwd"; break;
                        case "CON":     type = lex.contains(".") ? "float_lit" : "int_lit"; break;
                        case "VAR":     type = "var_idf"; break;
                        case "SUB_OP":  type = "minus_op"; break;
                        case "MULT_OP": type = "multi_op"; break;
                        default:        type = type.toLowerCase(); break;
                    }
                    break;
            }

            lexemes.add(lex);
            tokens.add(type);
        }
    }

    public static String next_token() {
        if (idx < tokens.size()) {
            currentLexeme = lexemes.get(idx);
            return tokens.get(idx++);
        }
        currentLexeme = "EOF";
        return "EOF";
    }

    public static String match(String expected_token) {
        if (next.equals(expected_token)) {
            String matchedLex = currentLexeme;
            next = next_token();
            return matchedLex;
        } else {
            reject("Syntax Error: Expected " + expected_token + " but found " + next);
            return "";
        }
    }

    public static void reject(String msg) {
        System.err.println(msg);
        System.exit(1);
    }

    public static String newTemp() {
        return "T" + (++tempCount);
    }

    public static String newLabel() {
        return "L" + (++labelCount);
    }

    public static void emit(String op, String left, String right, String res, String comp, String dest) {
        atoms.add("(" + op + ", " + left + ", " + right + ", " + res + ", " + comp + ", " + dest + ")");
    }

    // Flips comparison code (1..6) so TST jumps when condition is FALSE
    public static int invertComp(int c) {
        switch (c) {
            case 1: return 6; // == -> !=
            case 2: return 5; // <  -> >=
            case 3: return 4; // >  -> <=
            case 4: return 3; // <= -> >
            case 5: return 2; // >= -> <
            case 6: return 1; // != -> ==
            default: return c;
        }
    }

    // RECURSIVE DESCENT FUNCTIONS

    public static void parse_PROGRAM() {
        while (isStmtStart(next)) {
            parse_STMT();
        }
        if (!next.equals("EOF")) {
            reject("Syntax Error in PROGRAM: unexpected token " + next);
        }
    }

    public static void parse_STMT() {
        switch (next) {
            case "var_idf":   
            {
                parse_VAR_ASSIGN_STMT(); 
                break;
            }
            case "if_kwd":    
            {
                parse_IF_STMT();         
                break;
            }
            case "while_kwd": 
            {
                parse_WHILE_STMT();      
                break;
            }
            case "for_kwd":   
            {
                parse_FOR_STMT();        
                break;
            }
            default:          
                reject("Syntax Error: expected statement, found " + next);
        }
    }


    public static void parse_VAR_ASSIGN_STMT() {
        String id = match("var_idf");
        match("assign_op");
        int atomsBefore = atoms.size();
        String val = parse_EXPRESSION("STMT");

        // If EXPRESSION emitted an arithmetic/negation atom into a fresh temp, write directly to 'id'
        if (atoms.size() > atomsBefore && val.equals("T" + tempCount)) {
            String last = atoms.get(atoms.size() - 1);
            String target = ", " + val + ", , )";
            if (last.endsWith(target)) {
                atoms.set(atoms.size() - 1, last.substring(0, last.length() - target.length()) + ", " + id + ", , )");
                tempCount--;
                return;
            }
        }
        emit("MOV", val, "", id, "", "");
    }


    public static void parse_IF_STMT() {
        match("if_kwd");
        String falseLbl = newLabel();

        parse_BOOL_EXPRESSION(falseLbl, false);
        match("colon_sym");
        parse_STMT();

        if (next.equals("elif_kwd") || next.equals("else_kwd")) {
            String endLbl = newLabel();
            emit("JMP", "", "", "", "", endLbl);
            emit("LBL", "", "", "", "", falseLbl);

            parse_ELIF_STMT(endLbl);
            parse_ELSE_STMT();

            emit("LBL", "", "", "", "", endLbl);
        } else {
            // Simple if without elif/else: validate FOLLOW set and emit only falseLbl
            if (!isStmtFollow(next)) {
                reject("Syntax Error after if statement: unexpected token " + next);
            }
            emit("LBL", "", "", "", "", falseLbl);
        }
    }

    public static void parse_ELIF_STMT(String endLbl) {
        if (next.equals("elif_kwd")) {
            match("elif_kwd");
            String nextFalse = newLabel();

            parse_BOOL_EXPRESSION(nextFalse, false);
            match("colon_sym");
            parse_STMT();

            if (next.equals("elif_kwd") || next.equals("else_kwd")) {
                emit("JMP", "", "", "", "", endLbl);
                emit("LBL", "", "", "", "", nextFalse);
                parse_ELIF_STMT(endLbl);
            } else {
                if (!isStmtFollow(next)) {
                    reject("Syntax Error in elif statement: unexpected token " + next);
                }
                emit("LBL", "", "", "", "", nextFalse);
            }
        } else if (!next.equals("else_kwd") && !isStmtFollow(next)) {
            reject("Syntax Error in elif statement: unexpected token " + next);
        }
    }

    public static void parse_ELSE_STMT() {
        if (next.equals("else_kwd")) {
            match("else_kwd");
            match("colon_sym");
            parse_STMT();
        } else if (!isStmtFollow(next)) {
            reject("Syntax Error in else statement: unexpected token " + next);
        }
    }

    public static void parse_WHILE_STMT() {
        match("while_kwd");
        String startLbl = newLabel();
        String endLbl = newLabel();

        emit("LBL", "", "", "", "", startLbl);
        parse_BOOL_EXPRESSION(endLbl, false);
        match("colon_sym");
        parse_STMT();

        emit("JMP", "", "", "", "", startLbl);
        emit("LBL", "", "", "", "", endLbl);
    }

    public static void parse_FOR_STMT() {
        match("for_kwd");
        String id = match("var_idf");
        match("in_kwd");
        String limit = parse_RANGE();
        match("colon_sym");

        String startLbl = newLabel();
        String endLbl = newLabel();

        emit("MOV", "0", "", id, "", "");
        emit("LBL", "", "", "", "", startLbl);
        emit("TST", id, limit, "", "5", endLbl); // jump out when id >= limit

        parse_STMT();

        emit("ADD", id, "1", id, "", "");
        emit("JMP", "", "", "", "", startLbl);
        emit("LBL", "", "", "", "", endLbl);
    }

    public static String parse_RANGE() {
        match("range_kwd");
        match("open_paren");
        String limit = parse_EXPRESSION("PAREN");
        match("close_paren");
        return limit;
    }

    // Precedence: 'and' (BOOL_TERM) binds tighter than 'or' (BOOL_EXPRESSION_END)
    public static void parse_BOOL_EXPRESSION(String falseLbl, boolean inBoolParens) {
        String[] firstTerm = parse_BOOL_TERM(inBoolParens);
        parse_BOOL_EXPRESSION_END(firstTerm, null, falseLbl, inBoolParens);
    }

    public static void parse_BOOL_EXPRESSION_END(String[] prevTerm, String trueLbl, String falseLbl, boolean inBoolParens) {
        if (next.equals("or_kwd")) {
            if (trueLbl == null) {
                trueLbl = newLabel(); // Allocated lazily only when 'or' is present
            }
            if (prevTerm[0].equals("GROUP")) {
                // Previous term was a parenthesized boolean group; if it didn't jump to prevTerm[3], it succeeded
                emit("JMP", "", "", "", "", trueLbl);
            } else {
                emit("TST", prevTerm[0], prevTerm[2], "", prevTerm[1], trueLbl);
            }
            if (prevTerm[3] != null) {
                emit("LBL", "", "", "", "", prevTerm[3]);
            }

            match("or_kwd");
            String[] nextTerm = parse_BOOL_TERM(inBoolParens);
            parse_BOOL_EXPRESSION_END(nextTerm, trueLbl, falseLbl, inBoolParens);
        } else if ((!inBoolParens && next.equals("colon_sym")) || (inBoolParens && next.equals("close_paren"))) {
            if (!prevTerm[0].equals("GROUP")) {
                int inv = invertComp(Integer.parseInt(prevTerm[1]));
                emit("TST", prevTerm[0], prevTerm[2], "", String.valueOf(inv), falseLbl);
            }
            if (prevTerm[3] != null) {
                // Redirect any earlier failed AND-conditions in this final term directly to falseLbl
                for (int i = 0; i < atoms.size(); i++) {
                    String a = atoms.get(i);
                    if (a.endsWith(", " + prevTerm[3] + ")")) {
                        atoms.set(i, a.substring(0, a.length() - (prevTerm[3].length() + 1)) + falseLbl + ")");
                    }
                }
            }
            if (trueLbl != null) {
                emit("LBL", "", "", "", "", trueLbl);
            }
        } else {
            reject("Syntax Error in boolean expression: unexpected token " + next);
        }
    }

    public static String[] parse_BOOL_TERM(boolean inBoolParens) {
        String[] factor = parse_BOOL_FACTOR(inBoolParens);
        return parse_BOOL_TERM_END(factor, null, inBoolParens);
    }

    public static String[] parse_BOOL_TERM_END(String[] prevFactor, String termFailLbl, boolean inBoolParens) {
        if (next.equals("and_kwd")) {
            if (termFailLbl == null) {
                termFailLbl = newLabel();
            }
            if (prevFactor[0].equals("GROUP")) {
                // Redirect group failure label to termFailLbl
                for (int i = 0; i < atoms.size(); i++) {
                    String a = atoms.get(i);
                    if (a.endsWith(", " + prevFactor[3] + ")")) {
                        atoms.set(i, a.substring(0, a.length() - (prevFactor[3].length() + 1)) + termFailLbl + ")");
                    }
                }
            } else {
                int inv = invertComp(Integer.parseInt(prevFactor[1]));
                emit("TST", prevFactor[0], prevFactor[2], "", String.valueOf(inv), termFailLbl);
            }

            match("and_kwd");
            String[] nextFactor = parse_BOOL_FACTOR(inBoolParens);
            return parse_BOOL_TERM_END(nextFactor, termFailLbl, inBoolParens);
        } else if (next.equals("or_kwd") || (!inBoolParens && next.equals("colon_sym")) || (inBoolParens && next.equals("close_paren"))) {
            if (prevFactor[0].equals("GROUP") && termFailLbl != null) {
                for (int i = 0; i < atoms.size(); i++) {
                    String a = atoms.get(i);
                    if (a.endsWith(", " + prevFactor[3] + ")")) {
                        atoms.set(i, a.substring(0, a.length() - (prevFactor[3].length() + 1)) + termFailLbl + ")");
                    }
                }
                return new String[]{"GROUP", "0", "", termFailLbl};
            }
            return new String[]{prevFactor[0], prevFactor[1], prevFactor[2],
                    prevFactor[0].equals("GROUP") ? prevFactor[3] : termFailLbl};
        } else {
            reject("Syntax Error in boolean term: unexpected token " + next);
            return new String[]{"", "1", "", null};
        }
    }

    public static String[] parse_BOOL_FACTOR(boolean inBoolParens) {
        if (next.equals("open_paren") && isParenthesizedBoolean()) {
            match("open_paren");
            String groupFailLbl = newLabel();
            parse_BOOL_EXPRESSION(groupFailLbl, true);
            match("close_paren");
            return new String[]{"GROUP", "0", "", groupFailLbl};
        }
        return parse_CONDITION(inBoolParens);
    }

    // Checks if '(' starts a parenthesized boolean condition (e.g. 'if (a < b or c < d):')
    // rather than an arithmetic expression (e.g. 'if (a + b) < c:')
    public static boolean isParenthesizedBoolean() {
        int depth = 0;
        for (int i = idx - 1; i < tokens.size(); i++) {
            String t = tokens.get(i);
            if (t.equals("open_paren")) depth++;
            else if (t.equals("close_paren")) {
                depth--;
                if (depth == 0) return false;
            } else if (depth == 1 && (isCompOp(t) || t.equals("or_kwd") || t.equals("and_kwd"))) {
                return true;
            }
        }
        return false;
    }

    public static String[] parse_CONDITION(boolean inBoolParens) {
        String left = parse_EXPRESSION("COND_LEFT");
        int op = parse_CONDITION_OP();
        String right = parse_EXPRESSION(inBoolParens ? "COND_RIGHT_PAREN" : "COND_RIGHT");
        return new String[]{left, String.valueOf(op), right};
    }

    public static int parse_CONDITION_OP() {
        switch (next) {
            case "equal_op":         
            {
                match("equal_op");         
                return 1;
            }
            case "lesser_op":        
            {
                match("lesser_op"); 
                return 2;
            }
            case "greater_op":       
            {
                match("greater_op");       
                return 3;
            }
            case "lesser_equal_op":  
            {
                match("lesser_equal_op");
                return 4;
            }
            case "greater_equal_op": 
            {
                match("greater_equal_op"); 
                return 5;
            }
            case "inequal_op":       
            {
                match("inequal_op");       
                return 6;
            }
            default:
                reject("Syntax Error: expected comparison operator, found " + next);
                return -1;
        }
    }

    public static String parse_EXPRESSION(String ctx) {
        String left = parse_TERM(ctx);
        return parse_EXPRESSION_END(left, ctx);
    }

    public static String parse_EXPRESSION_END(String left, String ctx) {
        switch (next) {
            case "plus_op": 
            {
                match("plus_op");
                String right = parse_TERM(ctx);
                String temp = newTemp();
                emit("ADD", left, right, temp, "", "");
                return parse_EXPRESSION_END(temp, ctx);
            }
            case "minus_op": 
            {
                match("minus_op");
                String right = parse_TERM(ctx);
                String temp = newTemp();
                emit("SUB", left, right, temp, "", "");
                return parse_EXPRESSION_END(temp, ctx);
            }
            default:
                if (isValidExprFollow(next, ctx)) {
                    return left; // epsilon
                }
                reject("Syntax Error in end of expression: unexpected token " + next);
                return "";
        }
    }

    public static String parse_TERM(String ctx) {
        String left = parse_VALUE(ctx);
        return parse_TERM_END(left, ctx);
    }

    public static String parse_TERM_END(String left, String ctx) {
        switch (next) {
            case "multi_op": 
            {
                match("multi_op");
                String right = parse_VALUE(ctx);
                String temp = newTemp();
                emit("MUL", left, right, temp, "", "");
                return parse_TERM_END(temp, ctx);
            }
            case "div_op": 
            {
                match("div_op");
                String right = parse_VALUE(ctx);
                String temp = newTemp();
                emit("DIV", left, right, temp, "", "");
                return parse_TERM_END(temp, ctx);
            }
            default:
                if (next.equals("plus_op") || next.equals("minus_op") || isValidExprFollow(next, ctx)) {
                    return left; // epsilon
                }
                reject("Syntax Error in end of term: unexpected token " + next);
                return "";
        }
    }

    public static String parse_VALUE(String ctx) {
        switch (next) {
            case "open_paren": 
            {
                match("open_paren");
                String val = parse_EXPRESSION("PAREN");
                match("close_paren");
                return val;
            }
            case "minus_op": 
            {
                match("minus_op");
                String val = parse_VALUE(ctx);
                String temp = newTemp();
                emit("NEG", val, "", temp, "", "");
                return temp;
            }
            case "var_idf":  
            {
               return match("var_idf");
            }
            case "int_lit":   
            {
                return match("int_lit");
            }
            case "float_lit": 
            {
                return match("float_lit");
            }
            default:
                reject("Syntax Error: expected value or parenthesis, found " + next);
                return "";
        }
    }

    public static boolean isStmtStart(String tok) {
        return tok.equals("var_idf") || tok.equals("if_kwd") || tok.equals("while_kwd") || tok.equals("for_kwd");
    }

    public static boolean isStmtFollow(String tok) {
        return isStmtStart(tok) || tok.equals("elif_kwd") || tok.equals("else_kwd") || tok.equals("EOF");
    }

    public static boolean isCompOp(String tok) {
        return tok.equals("equal_op") || tok.equals("inequal_op") || tok.equals("greater_op")
                || tok.equals("lesser_op") || tok.equals("greater_equal_op") || tok.equals("lesser_equal_op");
    }

    public static boolean isValidExprFollow(String tok, String ctx) {
        return switch (ctx) {
            case "PAREN" -> tok.equals("close_paren");
            case "COND_LEFT" -> isCompOp(tok);
            case "COND_RIGHT" -> tok.equals("or_kwd") || tok.equals("and_kwd") || tok.equals("colon_sym");
            case "COND_RIGHT_PAREN" -> tok.equals("or_kwd") || tok.equals("and_kwd") || tok.equals("close_paren");
            case "STMT" -> isStmtFollow(tok);
            default -> false;
        };
    }
}