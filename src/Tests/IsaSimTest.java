package Tests;

import org.junit.Assert;
import org.junit.Test;
import Methods.*;

import java.io.IOException;
import java.util.Arrays;

public class IsaSimTest {

    @Test(expected = IOException.class)
    public void testReadProgramException() throws IOException {
        RISCVSim.readProgram("");
    }

    @Test
    public void testGetInstr() throws IOException {
        RISCVSim.readProgram("tests/task1/addlarge.bin");

        Assert.assertEquals(RISCVSim.getInstr(1), 0x13800005);
        Assert.assertEquals(RISCVSim.getInstr(5), -0x48FFEAFB);
        Assert.assertEquals(RISCVSim.getInstr(10), -0x7A6C8000);
        Assert.assertEquals(RISCVSim.getInstr(1000), 0x0);
    }

    @Test
    public void testExecuteInstr() throws IOException {
        RISCVSim.readProgram("tests/task1/addlarge.bin");

        int instr = RISCVSim.getInstr(0);
        int opcode = instr & 0x7f;
        RISCVSim.executeInstr(instr, opcode);
        Assert.assertEquals("[0, 0, 0, 0, 0, 0, 0, 0, 0, 0, -2147483648, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0]", Arrays.toString(RISCVSim.getReg()));

        instr = RISCVSim.getInstr(4);
        opcode = instr & 0x7f;
        RISCVSim.executeInstr(instr, opcode);
        Assert.assertEquals("[0, 0, 0, 0, 0, 0, 0, 0, 0, 0, -2147483647, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0]", Arrays.toString(RISCVSim.getReg()));

        instr = RISCVSim.getInstr(8);
        opcode = instr & 0x7f;
        RISCVSim.executeInstr(instr, opcode);
        Assert.assertEquals("[0, 0, 0, 0, 0, 0, 0, 0, 0, 0, -2147483647, -2147483648, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0]", Arrays.toString(RISCVSim.getReg()));
    }
}
