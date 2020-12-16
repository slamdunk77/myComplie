public class Break {
    private Instruction instruction;
    private int glo_addr;
    private int while_level;

    public Break(Instruction instruction, int glo_addr, int while_level){
        this.glo_addr = glo_addr;
        this.instruction = instruction;
        this.while_level = while_level;
    }
    public Instruction getInstruction() {
        return instruction;
    }

    public int getGlo_addr() {
        return glo_addr;
    }

    public int getWhile_level() {
        return while_level;
    }

    public void setGlo_addr(int glo_addr) {
        this.glo_addr = glo_addr;
    }

    public void setInstruction(Instruction instruction) {
        this.instruction = instruction;
    }

    public void setWhile_level(int while_level) {
        this.while_level = while_level;
    }
}
