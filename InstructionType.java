package instruction;

public enum  InstructionType {
    push,
    popn,
    loca,
    arga,
    globa,
    load,
    store,
    stackalloc,
    add,
    sub,
    mul,
    div,
    not,
    cmp,
    neg,
    setLt,
    setGt,
    br,
    brTrue,
    call,
    ret,
    callname;

    public Integer getInstructionNum() {
        switch (this) {
            case push:
                return 0x01;
            case popn:
                return 0x03;
            case loca:
                return 0x0a;
            case arga:
                return 0x0b;
            case globa:
                return 0x0c;
            case load:
                return 0x13;
            case store:
                return 0x17;
            case stackalloc:
                return 0x1a;
            case add:
                return 0x20;
            case sub:
                return 0x21;
            case mul:
                return 0x22;
            case div:
                return 0x23;
            case not:
                return 0x2e;
            case cmp:
                return 0x30;
            case neg:
                return 0x34;
            case setLt:
                return 0x39;
            case setGt:
                return 0x3a;
            case br:
                return 0x41;
            case brTrue:
                return 0x43;
            case call:
                return 0x48;
            case ret:
                return 0x49;
            case callname:
                return 0x4a;
            default:
                return null;
        }
    }
}
