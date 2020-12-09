

public class Variable {
    private String varName;
    private Integer varId;
    private Integer varLevel;
    public Variable(String varName, Integer varId, Integer varLevel){
        this.varName = varName;
        this.varId = varId;
        this.varLevel = varLevel;
    }

    public Integer getVarId() {
        return varId;
    }

    public Integer getVarLevel() {
        return varLevel;
    }

    public String getVarName() {
        return varName;
    }

    public void setVarId(Integer varId) {
        this.varId = varId;
    }

    public void setVarLevel(Integer varLevel) {
        this.varLevel = varLevel;
    }

    public void setVarName(String varName) {
        this.varName = varName;
    }
}
