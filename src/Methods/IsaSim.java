package Methods;

import java.awt.desktop.ScreenSleepEvent;
import java.io.*;
import java.nio.ByteBuffer;
import java.util.ArrayList;

public class IsaSim {

    static int pc;
    static int[] reg = new int[32];
    static final ArrayList<Integer> program = new ArrayList<>();
    private static boolean execute = true;
    private static boolean branch = false;

    /**
     *
     * @param opcode F
     * @param rd F
     * @param rs1 F
     * @param rs2 F
     * @param iImm F
     * @param uImm F
     * @param funct3 F
     */
    public static void executeOP(int opcode, int rd, int rs1, int rs2, int iImm, int uImm, int funct3){
        switch (opcode) {
            case 0x37 -> reg[rd] = (uImm << 12); // LUI
            case 0x17 -> reg[rd] = pc + (uImm << 12); // AUIPC
            case 0x63 -> { // BEQ - BGEU
                switch (funct3){
                    case 0x0 -> branch = reg[rs1] == reg[rs2]; // BEQ
                    case 0x1 -> branch = reg[rs1] != reg[rs2]; // BNE
                    case 0x4 -> branch = reg[rs1] < reg[rs2]; // BLT
                    case 0x5 -> branch = reg[rs1] >= reg[rs2]; // BGE
                    case 0x6 -> branch = Integer.compareUnsigned(reg[rs1], reg[rs2]) < 0; // BLTU
                    case 0x7 -> branch = Integer.compareUnsigned(reg[rs1], reg[rs2]) > 0; // BGEU
                }
            }
            case 0x13 -> { // ADDI - SRAI
                switch (funct3) {
                    case 0x0 -> reg[rd] = reg[rs1] + iImm; // ADDI
                    case 0x2 -> reg[rd] = (reg[rs1] < iImm) ? 1:0; // SLTI
                    case 0x3 -> reg[rd] = (Integer.compareUnsigned(reg[rs1], iImm) < 0) ? 1:0; // SLTIU
                    case 0x4 -> reg[rd] = reg[rs1] ^ iImm; // XORI
                    case 0x6 -> reg[rd] = reg[rs1] | iImm; // ORI
                    case 0x7 -> reg[rd] = reg[rs1] & iImm; // ANDI
                    case 0x1 -> reg[rd] = reg[rs1] << (iImm & 0x01f); // SLLI
                    case 0x5 -> { // SRLI - SRAI
                        switch ((iImm >> 5)) {
                            case 0x0 -> reg[rd] = reg[rs1] >>> (iImm & 0x01f); // SRLI
                            case 0x20 -> reg[rd] = reg[rs1] >> (iImm & 0x01f); // SRAI
                        }
                    }
                }
            }
            case 0x33 -> { // ADD - AND
                switch (funct3){
                    case 0x0 -> { // ADD - SUB
                        switch ((iImm >> 5)){
                            case 0x20 -> reg[rd] = reg[rs1] - reg[rs2]; // SUB
                            case 0x0 -> reg[rd] = reg[rs1] + reg[rs2]; // ADD
                        }
                    }
                    case 0x1 -> reg[rd] = reg[rs1] << reg[rs2]; // SLL
                    case 0x2 -> reg[rd] = (reg[rs1] < reg[rs2]) ? 1:0;// SLT
                    case 0x3 -> reg[rd] = (Integer.compareUnsigned(reg[rs1], reg[rs2]) < 0) ? 1:0; // SLTU
                    case 0x4 -> reg[rd] = reg[rs1] ^ reg[rs2]; // XOR
                    case 0x5 -> {
                        switch ((iImm >> 5)) {
                            case 0x0 -> reg[rd] = reg[rs1] >>> reg[rs2]; // SRL
                            case 0x20 -> reg[rd] = reg[rs1] >> reg[rs2]; // SRA
                        }
                    }
                    case 0x6 -> reg[rd] = reg[rs1] | reg[rs2]; // OR
                    case 0x7 -> reg[rd] = reg[rs1] & reg[rs2]; // AND
                }
            }
            case 0x73 -> { // ECALL
                switch (reg[17]){
                    case 0xa -> execute = false; // EXIT
                    default -> System.out.println("ECALL");
                }
            }
            default -> System.out.println("Unrecognized operation: " + opcode);
        }
    }

    /**
     *
     * @param filename String containing the name of the binary file
     * @throws IOException f
     */
    public static void readProgram(String filename) throws IOException {
        try (DataInputStream dis = new DataInputStream(new FileInputStream(filename))) {
            byte[] instr = new byte[4];
            int i = 0;
            while (dis.available() > 0) {
                byte value = dis.readByte();
                instr[3 - i] = value;
                if(i == 3){
                    ByteBuffer wrapped = ByteBuffer.wrap(instr);
                    int num = wrapped.getInt();
                    program.add(num);
                    i = 0;
                } else{
                    i++;
                }
            }
        }
    }

    public static void main(String[] args) throws IOException {

        if(args.length > 0){
            readProgram(args[0]);
        }
        else{
            readProgram("tests/task2/branchtrap.bin");
        }

        // For loop for executing the program
        do {
            branch = false;

            // Get all the fields from the instruction
            int instr = program.get(pc >> 2);
            int opcode = instr & 0x7f;
            int rd = (instr >> 7) & 0x01f;
            int rs1 = (instr >> 15) & 0x01f;
            int rs2 = (instr >> 20) & 0x01f;
            int funct3 = (instr >> 12) & 0x7;
            int iImm = (instr >> 20);
            int uImm = (instr >> 12);
            int bImm = ((instr >> 19) & 0xFFFFF000) + ((instr << 4) & 0x800) + ((instr >> 20) & 0x7E0) + ((instr >> 7) & 0x1e);
            
            executeOP(opcode, rd, rs1, rs2, iImm, uImm, funct3);

            // Branch Control
            if (!branch) pc += 4;
            else {
                pc = pc + bImm;
            }

            //Dump the values of the registers
            for (int j : reg) {
                System.out.print(j + " ");
            }
            System.out.println();
        } while ((pc >> 2) < program.size() && execute);

        System.out.println("Program exit");
    }

}