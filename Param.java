

public class Param {
    private String paramType;
    private String paramName;

    public Param(String paramType, String paramName) {
        this.paramType = paramType;
        this.paramName = paramName;
    }

    public String getParamName() {
        return paramName;
    }

    public String getParamType() {
        return paramType;
    }
}
