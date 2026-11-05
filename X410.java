import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Random;

public class X410 {
	private static final int MEMORY_LIMIT = 0x10000;
	private static final int CYCLE_LIMIT = 60;

	private byte[] mem;
	private ByteBuffer membuf;

	private int TS, PC, IR, SF;
	private int[] ireg;
	private float[] fpreg;

	private Random RNG;

	public static void main(String[] args) throws IOException {
		Path in = args.length >= 1
			? Paths.get(args[0])
			: Paths.get("resources", "fpsample.bin");

		Path out = args.length >= 2
			? Paths.get(args[1])
			: Paths.get("resources", "~dump.bin");

		X410 vm = new X410();
		vm.load(in);
		vm.execute();
		vm.dump(out);
	}

	public void load(Path program) throws IOException {
		if (Files.size(program) > MEMORY_LIMIT)
			throw new IOException();

		mem = Files.readAllBytes(program);
		membuf = ByteBuffer.wrap(mem).order(ByteOrder.BIG_ENDIAN);

		System.out.println("Loaded: " + program);
	}

	public void dump(Path program) throws IOException {
		Files.write(program, mem);

		System.out.println("Dumped: " + program);
	}

	public void execute() {
		System.out.println("Executing...");

		TS = PC = IR = SF = 0;
		ireg = new int[8];
		fpreg = new float[8];

		RNG = new Random();

		cycle:
		for (TS = 0; TS < CYCLE_LIMIT; TS++) {
			ireg[0] = (int) 0;
			ireg[1] = (int) 1;
			fpreg[0] = (float) RNG.nextFloat();
			fpreg[1] = (float) RNG.nextGaussian();

			IR = membuf.getInt(PC);
			PC += 4;

			final int
				opcode = (IR & 0xF0000000) >>> 28,
				rt     = (IR & 0x0F000000) >>> 24,
				rx     = (IR & 0x00F00000) >>> 20,
				ry     = (IR & 0x000F0000) >>> 16,
				addr   = (IR & 0x0000FFFF) >>> 0;

			switch (opcode) {
				case NOP -> {}
				case CLR -> {
					if (rt <= 7)
						ireg[rt] = 0;
					else if (rt >= 8)
						fpreg[rt-8] = 0.0f;
				}
				case ADD -> {
					if (rt <= 7)
						ireg[rt] = ireg[rx] + ireg[ry];
					else if (rt >= 8)
						fpreg[rt-8] = fpreg[rx-8] + fpreg[ry-8];
				}
				case SUB -> {
					if (rt <= 7)
						ireg[rt] = ireg[rx] - ireg[ry];
					else if (rt >= 8)
						fpreg[rt-8] = fpreg[rx-8] - fpreg[ry-8];
				}
				case MUL -> {
					if (rt <= 7)
						ireg[rt] = ireg[rx] * ireg[ry];
					else if (rt >= 8)
						fpreg[rt-8] = fpreg[rx-8] * fpreg[ry-8];
				}
				case DIV -> {
					if (rt <= 7)
						ireg[rt] = ireg[rx] / ireg[ry];
					else if (rt >= 8)
						fpreg[rt-8] = fpreg[rx-8] / fpreg[ry-8];
				}
				case BAL -> {
					SF = 1;
				}
				case BEQ -> {
					if (rt <= 7)
						SF = (ireg[rx] == ireg[ry]) ? 1 : 0;
					else if (rt >= 8)
						SF = (fpreg[rx-8] == fpreg[ry-8]) ? 1 : 0;
				}
				case BLT -> {
					if (rt <= 7)
						SF = (ireg[rx] < ireg[ry]) ? 1 : 0;
					else if (rt >= 8)
						SF = (fpreg[rx-8] < fpreg[ry-8]) ? 1 : 0;
				}
				case BGT -> {
					if (rt <= 7)
						SF = (ireg[rx] > ireg[ry]) ? 1 : 0;
					else if (rt >= 8)
						SF = (fpreg[rx-8] > fpreg[ry-8]) ? 1 : 0;
				}
				case BLE -> {
					if (rt <= 7)
						SF = (ireg[rx] <= ireg[ry]) ? 1 : 0;
					else if (rt >= 8)
						SF = (fpreg[rx-8] <= fpreg[ry-8]) ? 1 : 0;
				}
				case BGE -> {
					if (rt <= 7)
						SF = (ireg[rx] >= ireg[ry]) ? 1 : 0;
					else if (rt >= 8)
						SF = (fpreg[rx-8] >= fpreg[ry-8]) ? 1 : 0;
				}
				case BNE -> {
					if (rt <= 7)
						SF = (ireg[rx] != ireg[ry]) ? 1 : 0;
					else if (rt >= 8)
						SF = (fpreg[rx-8] != fpreg[ry-8]) ? 1 : 0;
				}
				case LW -> {
					if (rt <= 7)
						ireg[rt] = membuf.getInt(addr);
					else if (rt >= 8)
						fpreg[rt-8] = membuf.getFloat(addr);
				}
				case SW -> {
					if (rt <= 7)
						membuf.putInt(addr, ireg[rt]);
					else if (rt >= 8)
						membuf.putFloat(addr, fpreg[rt-8]);
				}
				case HLT -> {
					break cycle;
				}
			}

			if (opcode >= BAL && opcode <= BNE && SF == 1)
				PC = addr;

			if (rt <= 7) {
				fpreg[6] = (float) ireg[6];
				fpreg[7] = (float) ireg[7];
			}
			else if (rt >= 8) {
				ireg[6] = (int) fpreg[6];
				ireg[7] = (int) fpreg[7];
			}
		}

		System.out.println("...Terminating");
	}

	private static final int
		NOP = 0,
		CLR = 1,
		ADD = 2,
		SUB = 3,
		MUL = 4,
		DIV = 5,
		BAL = 6,
		BEQ = 7,
		BLT = 8,
		BGT = 9,
		BLE = 10,
		BGE = 11,
		BNE = 12,
		LW = 13,
		SW = 14,
		HLT = 15;
}
