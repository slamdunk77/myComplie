package Ident;

import java.util.ArrayList;
import java.util.List;

public class Function {
    private String funcName;
    private String funcType;
    private Integer funcId;
    private ArrayList<Param> params;
    public Function(String funcName, String funcType, Integer funcId, ArrayList<Param> params){
        this.funcName = funcName;
        this.funcType = funcType;
        this.funcId = funcId;
        this.params = params;
    }

    public String getFuncType() {
        return funcType;
    }

    public ArrayList<Param> getParams() {
        return params;
    }

    public Integer getFuncId() {
        return funcId;
    }

    public String getFuncName() {
        return funcName;
    }

    public void setFuncId(Integer funcId) {
        this.funcId = funcId;
    }

    public void setFuncName(String funcName) {
        this.funcName = funcName;
    }
}
