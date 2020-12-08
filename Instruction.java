package instruction;

import java.util.Objects;

public class Instruction {
    // 指令的种类 neg -> 0x34;
    private InstructionType instr;
    // 指令的顺序数
    private Integer instrId;

    public Instruction(InstructionType instr, Integer instrId) {
        this.instr = instr;
        this.instrId = instrId;
    }

    public InstructionType getInstr() {
        return instr;
    }

    public Integer getInstrId() {
        return instrId;
    }

    public void setInstr(InstructionType instr) {
        this.instr = instr;
    }

    public void setInstrId(Integer instrId) {
        this.instrId = instrId;
    }
}
