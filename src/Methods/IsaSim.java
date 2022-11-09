package Methods;

import java.io.*;
import java.nio.ByteBuffer;
import java.util.ArrayList;

public class IsaSim {

    static int pc;
    static int[] reg = new int[32];
    static final ArrayList<Integer> program = new ArrayList<>();

    // Here the first program hard coded as an array
    static int progr[] = {
            // As minimal RISC-V assembler example
            0x00200093, // addi x1 x0 2
            0x00300113, // addi x2 x0 3
            0x002081b3, // add x3 x1 x2
    };

    public static void executeOP(int opcode, int rd, int rs1, int rs2, int iImm, int uImm, int funct3){
        switch (opcode) {
            case 0x37 -> reg[rd] = (uImm << 12); // LUI
            case 0x13 -> { // ADDI - SRAI
                switch (funct3) {
                    case 0x0 -> reg[rd] = reg[rs1] + iImm; // ADDI
                    case 0x2 -> { // SLTI
                        if (reg[rs1] < iImm){
                            reg[rd] = 0x1;
                        }
                        else {
                            reg[rd] = 0x0;
                        }
                    }
                    case 0x3 -> reg[rd] = reg[rs1]; // SLTIU *
                    case 0x4 -> reg[rd] = reg[rs1] ^ iImm; // XORI
                    case 0x6 -> reg[rd] = reg[rs1] | iImm; // ORI
                    case 0x7 -> reg[rd] = reg[rs1] & iImm; // ANDI
                    case 0x1 -> reg[rd] = reg[rs1] + iImm; // SLLI
                    case 0x5 -> { // SRLI - SRAI
                        int upperImm = iImm >> 5;
                        int lowerImm = iImm & 0x01f;
                        switch (upperImm) {
                            case 0x20 -> reg[rd] = reg[rs1] >> lowerImm; // SRLI
                            case 0x0 -> reg[rd] = reg[rs1] >> lowerImm; // SRAI *
                        }
                    }
                }
            }
            case 0x33 -> reg[rd] = reg[rs1] + reg[rs2]; // ADD
            case 0x73 -> System.out.println("Ecall"); // ECALL
            default -> System.out.println("Unrecognized operation: " + opcode);
        }
    }

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
            readProgram("tests/addlarge.bin");
        }

        pc = 0;

        // For loop for executing the program
        do {
            // Get all the fields from the instruction
            int instr = program.get(pc >> 2);
            int opcode = instr & 0x7f;
            int rd = (instr >> 7) & 0x01f;
            int rs1 = (instr >> 15) & 0x01f;
            int rs2 = (instr >> 20) & 0x01f;
            int funct3 = (instr >> 12) & 0x7;
            int iImm = (instr >> 20);
            int uImm = (instr >> 12);

            System.out.println("Operation invoked: " + opcode);
            executeOP(opcode, rd, rs1, rs2, iImm, uImm, funct3);

            // Increment the PC
            pc += 4;

            //Dump the values of the registers
            for (int j : reg) {
                System.out.print(j + " ");
            }
            System.out.println();
        } while ((pc >> 2) < program.size());

        System.out.println("Program exit");
    }

}