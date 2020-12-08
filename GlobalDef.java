package Ident;

public class GlobalDef {
    private Integer isConst;
    // valueItem的长度
    private Integer valueCount;
    // 具体的数组的值
    private String valueItems;
    public GlobalDef(Integer isConst, Integer valueCount, String valueItems){
        this.isConst = isConst;
        this.valueCount = valueCount;
        this.valueItems = valueItems;
    }

    public Integer getIsConst() {
        return isConst;
    }

    public Integer getValueCount() {
        return valueCount;
    }

    public String getValueItems() {
        return valueItems;
    }

    public void setIsConst(Integer isConst) {
        this.isConst = isConst;
    }

    public void setValueCount(Integer valueCount) {
        this.valueCount = valueCount;
    }

    public void setValueItems(String valueItems) {
        this.valueItems = valueItems;
    }
}
