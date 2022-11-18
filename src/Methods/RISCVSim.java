package Methods;

import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.IOException;

/**
 * <h1>RISCVSim</h1>
 * RISC-V ISA simulator.
 * 
 * @author Arijus Grotuzas
 */
public class RISCVSim {

    static int pc;
    static int branchImmediate;
    static int[] reg = new int[32];
    static byte[] memory = new byte[1048576];
    private static boolean execute = true;
    private static boolean branch = false;


    /**
     * Executes an instruction from the subset of RISC-V ISA - RV32I
     *
     * @param instr A binary 32-bit instruction (Java int)
     * @param opcode A binary 7-bit opcode (Java int, leading bits must be 0)
     */
    public static void executeOP(int instr, int opcode){
        // Get most commonly used fields of instruction
        int rs1 = (instr >> 15) & 0x01f;
        int rs2 = (instr >> 20) & 0x01f;
        int rd = (instr >> 7) & 0x01f;
        int funct3 = (instr >> 12) & 0x7;

        switch (opcode) {
            case 0x37 -> { // LUI
                int uImm = (instr >> 12);
                reg[rd] = (uImm << 12);
            }
            case 0x17 -> {
                int uImm = (instr >> 12);
                reg[rd] = pc + (uImm << 12); // AUIPC
            }
            case 0x67 -> { // JALR - JAL
                reg[rd] = pc + 4;
                branchImmediate = (instr >> 20); // iImm
                branch = true;
            }
            case 0x6F -> {
                reg[rd] = pc + 4;
                branchImmediate = ((instr >> 11) & 0xFFF00000) + (instr & 0xFF000) + ((instr >> 9) & 0x800) + ((instr >> 20) & 0x7FE); // jImm
                branch = true;
            }
            case 0x63 -> { // BEQ - BGEU
                int bImm = ((instr >> 19) & 0xFFFFF000) + ((instr << 4) & 0x800) + ((instr >> 20) & 0x7E0) + ((instr >> 7) & 0x1e);
                switch (funct3){
                    case 0x0 -> {
                        branch = reg[rs1] == reg[rs2]; // BEQ
                        branchImmediate = bImm;
                    }
                    case 0x1 -> {
                        branch = reg[rs1] != reg[rs2]; // BNE
                        branchImmediate = bImm;
                    }
                    case 0x4 -> {
                        branch = reg[rs1] < reg[rs2]; // BLT
                        branchImmediate = bImm;
                    }
                    case 0x5 -> {
                        branch = reg[rs1] >= reg[rs2]; // BGE
                        branchImmediate = bImm;
                    }
                    case 0x6 -> {
                        branch = Integer.compareUnsigned(reg[rs1], reg[rs2]) < 0; // BLTU
                        branchImmediate = bImm;
                    }
                    case 0x7 -> {
                        branch = Integer.compareUnsigned(reg[rs1], reg[rs2]) > 0; // BGEU
                        branchImmediate = bImm;
                    }
                }
            }
            case 0x3 -> { // LB - LHU
                int iImm = (instr >> 20);
                switch (funct3){
                    case 0x0 -> reg[rd] = memory[reg[rs1] + iImm]; // LB
                    case 0x1 -> reg[rd] = (memory[reg[rs1] + iImm] & 0xFF) + (memory[reg[rs1] + iImm + 1] << 8); // LH
                    case 0x2 -> reg[rd] = (memory[reg[rs1] + iImm] & 0xFF) + ((memory[reg[rs1] + iImm + 1] & 0xFF) << 8) + ((memory[reg[rs1] + iImm + 2] & 0xFF) << 16) + (memory[reg[rs1] + iImm + 3] << 24); // LW
                    case 0x4 -> reg[rd] = memory[reg[rs1] + iImm] & 0xFF; // LBU
                    case 0x5 -> reg[rd] = (memory[reg[rs1] + iImm] & 0xFF) + ((memory[reg[rs1]+ iImm + 1] & 0xFF) << 8); // LHU
                }
            }
            case 0x23 -> { // SB - SW
                int sImm = ((instr >> 20) & 0xFFFFFFE0) + ((instr >> 7) & 0x1F);
                switch (funct3){
                    case 0x0 -> memory[reg[rs1] + sImm] = (byte) reg[rs2]; // SB
                    case 0x1 -> { // SH
                        memory[reg[rs1] + sImm] = (byte) reg[rs2];
                        memory[reg[rs1] + sImm + 1] = (byte) (reg[rs2] >> 8);
                    }
                    case 0x2 -> { // SW
                        memory[reg[rs1] + sImm] = (byte) reg[rs2];
                        memory[reg[rs1] + sImm + 1] = (byte) (reg[rs2] >> 8);
                        memory[reg[rs1] + sImm + 2] = (byte) (reg[rs2] >> 16);
                        memory[reg[rs1] + sImm + 3] = (byte) (reg[rs2] >> 24);
                    }
                }
            }
            case 0x13 -> { // ADDI - SRAI
                int iImm = (instr >> 20);
                switch (funct3) {
                    case 0x0 -> reg[rd] = reg[rs1] + iImm; // ADDI
                    case 0x2 -> reg[rd] = (reg[rs1] < iImm) ? 1:0; // SLTI
                    case 0x3 -> reg[rd] = ((reg[rs1] & 0x7fffffff) < (iImm & 0x7fffffff)) ? 1:0; // SLTIU
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
                int iImm = (instr >> 20);
                switch (funct3){
                    case 0x0 -> { // ADD - SUB
                        int funct7 = (iImm >> 5);
                        switch (funct7){
                            case 0x20 -> reg[rd] = reg[rs1] - reg[rs2]; // SUB
                            case 0x0 -> reg[rd] = reg[rs1] + reg[rs2]; // ADD
                        }
                    }
                    case 0x1 -> reg[rd] = reg[rs1] << reg[rs2]; // SLL
                    case 0x2 -> reg[rd] = (reg[rs1] < reg[rs2]) ? 1:0;// SLT
                    case 0x3 -> reg[rd] = ((reg[rs1] & 0x7fffffff) < (reg[rs2] & 0x7fffffff)) ? 1:0; // SLTU
                    case 0x4 -> reg[rd] = reg[rs1] ^ reg[rs2]; // XOR
                    case 0x5 -> {
                        int funct7 = (iImm >> 5);
                        switch (funct7) {
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
                    case 0x1 -> System.out.println(reg[10]); // print_int
                    case 0x2 -> System.out.println(Float.intBitsToFloat(reg[10])); // print_float
                    case 0xa -> { // exit
                        execute = false;
                        System.out.println("Program Exit");
                    }
                    case 0xb -> System.out.println((char)reg[10]); // print_char
                    case 0x22 -> System.out.println(Integer.toHexString(reg[10])); // print_hex
                    case 0x23 -> System.out.println(Integer.toBinaryString(reg[10])); // print_bin
                    case 0x24 -> System.out.println((reg[10] & 0x7fffffff)); // print_unsigned
                    default -> System.out.println("Unrecognized ECALL");
                }
            }
            default -> System.out.println("Unrecognized operation: " + opcode);
        }
    }

    /**
     * Returns an instruction from the memory given a byte address
     *
     * @param byteAddress A base address of the instruction (Java int)
     * @return A 32-bit instruction (Java int)
     */
    public static int getInstr(int byteAddress){
        return (memory[byteAddress] & 0xFF) + ((memory[byteAddress + 1] & 0xFF) << 8) + ((memory[byteAddress + 2] & 0xFF) << 16) + (memory[byteAddress + 3] << 24);
    }

    /**
     * Reads a binary file containing the machine code in bytes and loads it into memory
     *
     * @param filename String containing the name of the binary file (Java String)
     * @throws IOException Could not read a specified file
     */
    public static void readProgram(String filename) throws IOException {
        try (DataInputStream dis = new DataInputStream(new FileInputStream(filename))) {
            int numOfBytes = dis.available();
            while (dis.available() > 0) {
                byte value = dis.readByte();
                memory[numOfBytes - dis.available() - 1] = value;
            }
        }
    }

    public static void main(String[] args) throws IOException {

        if(args.length > 0){
            readProgram(args[0]);
        }
        else{
            readProgram("tests/task1/set.bin");
        }

        // For loop for executing the program
        do {
            branch = false;

            // Get all the fields from the instruction
            int instr = getInstr(pc);
            int opcode = instr & 0x7f;

            executeOP(instr, opcode);
            reg[0] = 0;

            // Branch Control
            if (!branch) pc += 4;
            else {
                switch (opcode){
                    case 0x67 -> {
                        int rs1 = (instr >> 15) & 0x01f;
                        pc = (reg[rs1] + branchImmediate) & 0xfffffffe; // JALR
                    }
                    case 0x6F, 0x63 -> pc = pc + branchImmediate; // JAL - B
                }
            }
        } while (pc < memory.length && execute);

        //Dump the values of the registers
        for (int j : reg) {
            System.out.print(Integer.toHexString(j) + " ");
        }
        System.out.println();
    }

}