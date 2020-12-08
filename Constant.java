package Ident;

public class Constant {
    private String constName;
    private Integer constId;
    private Integer constLevel;
    public Constant(String constName, Integer constId, Integer constLevel){
        this.constName = constName;
        this.constId = constId;
        this.constLevel = constLevel;
    }

    public Integer getConstId() {
        return constId;
    }

    public Integer getConstLevel() {
        return constLevel;
    }

    public String getConstName() {
        return constName;
    }

    public void setConstId(Integer constId) {
        this.constId = constId;
    }

    public void setConstLevel(Integer constLevel) {
        this.constLevel = constLevel;
    }

    public void setConstName(String constName) {
        this.constName = constName;
    }
}
