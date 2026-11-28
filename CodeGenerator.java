import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.HashMap;
import java.util.Map;

public class CodeGenerator {
	
	static final int ADD = 2;
	static final int SUB = 3;
	static final int BLE = 10;
	static final int BGE = 11;
	static final int BNE = 12;
	static final int LW = 13;
	static final int SW = 14;
	static final int HLT = 15;
	
	static List<Integer> code = new ArrayList<>();
	static List<String> symbols = new ArrayList<>();
	static List<int[]> fixups = new ArrayList<>();

	
	public static void main(String[] args) {
		
		try {
		    generate(Path.of("resources/CGInput.txt"));
		    write(Path.of("resources/CGOutput.bin"));
		} catch (IOException e) {
		    e.printStackTrace();
		}
		
		// take atoms.txt
		// read each line, split it into op, left, right, result, comp, dest
		// switch (op)
		// ADD -> genAdd(left, right, result
		// Sub -> genSub()
		// ...
		//write to file
	}
	
	// bits             31-28   27-24   23-20   19-16    15-0 
	static int to32Bit(int op, int rt, int rx, int ry, int addr) {
		return (op << 28) + (rt << 24) + (rx << 20) + (ry << 16) + (addr % 0x10000);
	}
	
	static int symbolIndex(String name) {
		int index = symbols.indexOf(name);
		if (index == -1) {
			symbols.add(name);
			index = symbols.size() - 1;
			
		}
		return index;
	}
		
	static void emitMem(int op, int reg, String name) {
	    fixups.add(new int[]{code.size(), symbolIndex(name)});
	    code.add(to32Bit(op, reg, 0, 0, 0));
	}
	
	// atom: (ADD, left, right, result, , )
	static void add(String left, String right, String result) {
		emitMem(LW, 2, left);
		emitMem(LW, 3, right);
		code.add(to32Bit(ADD, 4, 2, 3, 0));
		emitMem(SW, 4, result);
	}
	
	static void sub(String left, String right, String result) {
	    emitMem(LW, 2, left);
	    emitMem(LW, 3, right);
	    code.add(to32Bit(SUB, 4, 2, 3, 0));   // ireg4 = ireg2 - ireg3
	    emitMem(SW, 4, result);
	}
	
	static void write(Path out) throws IOException {
		
		// every program starts with hlt
		code.add(to32Bit(HLT, 0, 0, 0, 0));

		for(Object[] f : labelFix){
			int codeIndex = (Integer) f[0];
			int target = labels.get((String) f[1]);
			code.set(codeIndex, code.get(codeIndex) + target);

		}
		
		int dataStart = code.size() * 4;
		
		for (int[] f : fixups) {
	       int codeIndex = f[0];
	       int symIndex = f[1];
	       int address = dataStart + symIndex * 4;
	       int patched = code.get(codeIndex) + address;
	       code.set(codeIndex, patched);
		}
		
		// 1 4 byte per instruction and per data slot
		ByteBuffer buf = ByteBuffer.allocate((code.size() + symbols.size()) * 4); // big-endian
		
		// code segment
	    for (int word : code) {
	    	buf.putInt(word);
	    }
	     // data segment
	    for (String s : symbols) {
	        if (isIntConst(s)) {
	            buf.putInt(Integer.parseInt(s));
	        }
	        else if (isFloatConst(s)) {
	            buf.putFloat(Float.parseFloat(s));
	        } 
	        else {
	            buf.putInt(0);   // variable or temp starts at 0
	        }
	    }
	    Files.write(out, buf.array());
	}
	
	//helper method for write
	private static boolean isIntConst(String s) {
		int start = 0;
		// permit negative
		if(s.charAt(0) == '-') {
			start = 1;
		}
		
		for(int i = start; i < s.length(); i++) {
			if(!Character.isDigit(s.charAt(i))){
				return false;
			}
		}
		
		return true;
	}
	
	// other helper method for write
	static boolean isFloatConst(String s) {
	    int dots = 0;
	    int start = 0;
	    if (s.charAt(0) == '-') start = 1;
	    if (start == s.length()) return false;
	    for (int i = start; i < s.length(); i++) {
	        char c = s.charAt(i);
	        if (c == '.') dots++;
	        else if (!Character.isDigit(c)) return false;
	    }
	    return dots == 1;
	}
	
	static void generate(Path atomFile) throws IOException {
		List<String> lines = Files.readAllLines(atomFile);
		
		for(String line : lines) {
			line = line.trim().substring(1, line.length() - 1);
			
			String[] parts = line.split(",",ADD -1);
			for(int i = 0; i < parts.length; i++) { // see my O(n^2) and weep
				parts[i] = parts[i].trim();
			}
			
			String op = parts[0];
	        String left = parts[1];
	        String right = parts[2];
	        String result = parts[3];
	        String comp = parts[4];
	        String dest = parts[5];

			switch (op) {
			case "ADD":
				add(left, right, result);
				break;
			
			case "SUB":
				sub(left, right, result);
				break;

			case "LBL":
				label(dest);
				break;

			case "TST":
				tst(left, right, comp, dest);
				break;

			case "HLT":
				hlt();
				break;
			
			}

		}
	}

	// MCIHAELS WORK DOWN HERE HOMIE

	static Map<String, Integer> labels = new HashMap<>();
	static List<Object[]> labelFix = new ArrayList<>();

	//Records where L1 is 
	static void label(String name){
		labels.put(name, code.size() * 4); // next byte address
	}

	static void emitBranch(int op, int rx, int ry, String label){
		labelFix.add(new Object[]{code.size(), label});
		code.add(to32Bit(op, 0, rx, ry, 0));
	}
	
	static void tst(String left, String right, String comp, String dest) {
		emitMem(LW, 2, left);
		emitMem(LW, 3, right);
		switch (Integer.parseInt(comp)) {
			// LOGAN CAN ADD HERE BEQ, BLT, BGT
			case 4: emitBranch(BLE, 2, 3, dest); // <=
			break;
			case 5: emitBranch(BGE, 2, 3, dest); // >=
			break;
			case 6: emitBranch(BNE,2, 3, dest); // !=
			break;
			default:
				break;
		}
	}
	static void hlt(){
		code.add(to32Bit(HLT,0,0,0,0));
	}

}

