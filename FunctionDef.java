package Ident;

import instruction.Instruction;

import java.util.ArrayList;
import java.util.List;

public class FunctionDef {
    private Integer funcId;
    private String funcName;
    private Integer retSlots;
    private Integer paramSlots;
    private Integer locSlots;
    private ArrayList<Instruction> body;
    private Integer bodyCount;

    public FunctionDef(Integer funcId, String funcName, Integer retSlots, Integer paramSlots, Integer locSlots, ArrayList<Instruction> body) {
        this.funcId = funcId;
        this.funcName = funcName;
        this.retSlots = retSlots;
        this.paramSlots = paramSlots;
        this.locSlots = locSlots;
        this.body = body;
        this.bodyCount = body.size();
    }

    public ArrayList<Instruction> getBody() {
        return body;
    }

    public Integer getBodyCount() {
        return bodyCount;
    }

    public Integer getFuncId() {
        return funcId;
    }

    public Integer getLocSlots() {
        return locSlots;
    }

    public Integer getParamSlots() {
        return paramSlots;
    }

    public Integer getRetSlots() {
        return retSlots;
    }

    public String getFuncName() {
        return funcName;
    }
}
