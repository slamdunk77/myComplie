public class Continue {
    private Instruction instruction;
    private int addr;
    private int while_level;

    public Continue(Instruction instruction, int addr, int while_level){
        this.addr = addr;
        this.instruction = instruction;
        this.while_level = while_level;
    }
    public Instruction getInstruction() {
        return instruction;
    }

    public int getAddr() {
        return addr;
    }

    public int getWhile_level() {
        return while_level;
    }

    public void setAddr(int addr) {
        this.addr = addr;
    }

    public void setInstruction(Instruction instruction) {
        this.instruction = instruction;
    }

    public void setWhile_level(int while_level) {
        this.while_level = while_level;
    }
}
