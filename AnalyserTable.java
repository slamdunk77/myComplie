package analyser;

import Ident.*;
import instruction.Instruction;
import instruction.InstructionType;
import tokenizer.TokenType;

import java.util.ArrayList;
import java.util.List;

// 用于存储有关语义分析的表
public class AnalyserTable {
    // 变量表
    private static ArrayList<Variable> varList;
    //常量
    private static ArrayList<Constant> constList;
    //函数
    private static ArrayList<Function> functionList;
    // 指令集列表
    private static ArrayList<Instruction> instructionList;
    // 记录当前函数参数
    private static ArrayList<Param> paramList;
    //库函数列表
    private static ArrayList<LibraryFunction> libraryFuncList;
    // 全局变量输出表
    private static ArrayList<GlobalDef> globalDefList;
    // 函数输出表
    private static ArrayList<FunctionDef> functionDefList;


    public static ArrayList<LibraryFunction> getLibraryFuncList() {
        return libraryFuncList;
    }

    public static ArrayList<Param> getParamList() {
        return paramList;
    }

    public static void setParamList(ArrayList<Param> paramList) {
        AnalyserTable.paramList = paramList;
    }

    public static ArrayList<Constant> getConstList() {
        return constList;
    }

    public static ArrayList<Function> getFunctionList() {
        return functionList;
    }

    public static ArrayList<Instruction> getInstructionList() {
        return instructionList;
    }

    public static ArrayList<Variable> getVarList() {
        return varList;
    }

    public static ArrayList<FunctionDef> getFunctionDefList() {
        return functionDefList;
    }

    public static ArrayList<GlobalDef> getGlobalDefList() {
        return globalDefList;
    }

    public static void setConstList(ArrayList<Constant> constList) {
        AnalyserTable.constList = constList;
    }

    public static void setFunctionList(ArrayList<Function> functionList) {
        AnalyserTable.functionList = functionList;
    }

    public static void setInstructionList(ArrayList<Instruction> instructionList) {
        AnalyserTable.instructionList = instructionList;
    }

    public static void setVarList(ArrayList<Variable> varList) {
        AnalyserTable.varList = varList;
    }

    public static void initAnalyserTable(){
        varList = new ArrayList<>();
        constList = new ArrayList<>();
        functionList = new ArrayList<>();
        instructionList = new ArrayList<>();
        globalDefList = new ArrayList<>();
        functionDefList = new ArrayList<>();
        libraryFuncList = new ArrayList<>();
    }
    // 查找对应的函数名
    public static String findFunc(String funcName){
        for(int i = 0;i < functionList.size();i++){
            if(funcName.equals(functionList.get(i).getFuncName())){
                return funcName;
            }
        }
        return null;
    }
    // 函数是否有返回
    public static boolean isFuncReturn(String funcName){
        for(int i = 0;i < functionList.size();i++){
            if(funcName.equals(functionList.get(i).getFuncName())){
                if(functionList.get(i).getFuncType().equals("int"))
                    return true;
            }
        }
        return false;
    }
    // 是否是合法变量名(定义语句中)
    public static boolean isValidVarName(String varName, Integer level){
        if(isLibraryFuncName(varName)) return false;
        for(int i=0;i < functionList.size();i++){
            if(functionList.get(i).getFuncName().equals(varName) && level == 1){
                return false;
            }
        }
        for(int i=0;i < constList.size();i++){
            if(constList.get(i).getConstName().equals(varName) && level == constList.get(i).getConstLevel()){
                return false;
            }
        }
        for(int i=0;i < varList.size();i++){
            if(varList.get(i).getVarName().equals(varName) && level == varList.get(i).getVarLevel()){
                return false;
            }
        }
        return true;
    }
    // 是否是库函数名
    public static boolean isLibraryFuncName(String varName){
        if (varName.equals("getint") || varName.equals("getdouble") || varName.equals("getchar") ||
                varName.equals("putint") || varName.equals("putdouble") || varName.equals("putchar") ||
                varName.equals("putstr") || varName.equals("putln"))
            return true;
        return false;
    }
    // 处理表达式中栈中的运算符生成的指令
    public static void operationInstruction(TokenType tokenType) {
        Instruction instruction;
        switch (tokenType) {
            case LT:
                instruction = new Instruction(InstructionType.cmp, null);
                AnalyserTable.getInstructionList().add(instruction);
                instruction = new Instruction(InstructionType.setLt, null);
                AnalyserTable.getInstructionList().add(instruction);
                break;
            case LE:
                instruction = new Instruction(InstructionType.cmp, null);
                AnalyserTable.getInstructionList().add(instruction);
                instruction = new Instruction(InstructionType.setGt, null);
                AnalyserTable.getInstructionList().add(instruction);
                instruction = new Instruction(InstructionType.not, null);
                AnalyserTable.getInstructionList().add(instruction);
                break;
            case GT:
                instruction = new Instruction(InstructionType.cmp, null);
                AnalyserTable.getInstructionList().add(instruction);
                instruction = new Instruction(InstructionType.setGt, null);
                AnalyserTable.getInstructionList().add(instruction);
                break;
            case GE:
                instruction = new Instruction(InstructionType.cmp, null);
                AnalyserTable.getInstructionList().add(instruction);
                instruction = new Instruction(InstructionType.setLt, null);
                AnalyserTable.getInstructionList().add(instruction);
                instruction = new Instruction(InstructionType.not, null);
                AnalyserTable.getInstructionList().add(instruction);
                break;
            case PLUS:
                instruction = new Instruction(InstructionType.add, null);
                AnalyserTable.getInstructionList().add(instruction);
                break;
            case MINUS:
                instruction = new Instruction(InstructionType.sub, null);
                AnalyserTable.getInstructionList().add(instruction);
                break;
            case MUL:
                instruction = new Instruction(InstructionType.mul, null);
                AnalyserTable.getInstructionList().add(instruction);
                break;
            case DIV:
                instruction = new Instruction(InstructionType.div, null);
                AnalyserTable.getInstructionList().add(instruction);
                break;
            case EQ:
                instruction = new Instruction(InstructionType.cmp, null);
                AnalyserTable.getInstructionList().add(instruction);
                instruction = new Instruction(InstructionType.not, null);
                AnalyserTable.getInstructionList().add(instruction);
                break;
            case NEQ:
                instruction = new Instruction(InstructionType.cmp, null);
                AnalyserTable.getInstructionList().add(instruction);
                break;
            default:
                break;
        }

    }

    // 获取函数的地址
    public static int getFunctionAddr(String funcName) {
        for(int i=0; i < AnalyserTable.getFunctionList().size();i++){
            if(AnalyserTable.getFunctionList().get(i).getFuncName().equals(funcName))
                return i;
        }
        return -1;
    }

    // 是否是变量
    public static boolean isVariable(String varName) {
        for(int i = 0; i < AnalyserTable.getVarList().size(); i++){
            if(AnalyserTable.getVarList().get(i).getVarName().equals(varName))
                return true;
        }
        return false;
    }

    // 是否是常量
    public static boolean isConstant(String consName) {
        for(int i = 0; i < AnalyserTable.getConstList().size(); i++){
            if(AnalyserTable.getConstList().get(i).getConstName().equals(consName))
                return true;
        }
        return false;
    }

    // 是否是函数名
    public static boolean isFunction(String funcName) {
        if(isLibraryFuncName(funcName)) return true;
        for(int i = 0; i < AnalyserTable.getFunctionList().size(); i++){
            if(AnalyserTable.getFunctionList().get(i).getFuncName().equals(funcName))
                return true;
        }
        return false;
    }

    // 是否是参数
    public static boolean isParam(String paramName) {
        for(int i = 0; i < AnalyserTable.getParamList().size(); i++){
            if(AnalyserTable.getParamList().get(i).getParamName().equals(paramName))
                return true;
        }
        return false;
    }

    // 是否为本地变量
    public static boolean isLocal(String varName) {
        for(int i=0; i<AnalyserTable.getVarList().size();i++){
            if(AnalyserTable.getVarList().get(i).getVarName().equals(varName)){
                return true;
            }
        }
        for(int i=0; i<AnalyserTable.getConstList().size();i++){
            if(AnalyserTable.getConstList().get(i).getConstName().equals(varName)){
                return true;
            }
        }
        return false;
    }
    // 获取变量的地址
    public static int getAddr(String varName, Integer level) {
        for (int i = AnalyserTable.getVarList().size() - 1; i >= 0; --i) {
            if (AnalyserTable.getVarList().get(i).getVarName().equals(varName) && AnalyserTable.getVarList().get(i).getVarLevel() <= level)
                return AnalyserTable.getVarList().get(i).getVarId();
        }
        for (int i = AnalyserTable.getConstList().size() - 1; i >= 0; --i) {
            if (AnalyserTable.getConstList().get(i).getConstName().equals(varName) && AnalyserTable.getConstList().get(i).getConstLevel() <= level)
                return AnalyserTable.getConstList().get(i).getConstId();
        }
        return -1;
    }
    // 获取参数的地址
    public static int getParamAddr(String paramName) {
        for (int i = AnalyserTable.getParamList().size() - 1; i >= 0; --i) {
            if (AnalyserTable.getParamList().get(i).getParamName().equals(paramName))
                return i;
        }
        return -1;
    }
    // 检查传入放入参数是否符合定义
    public static boolean checkParam(String funcName, Integer count) {
        if (isLibraryFuncName(funcName)) {
            if (funcName.equals("getint") || funcName.equals("getdouble") || funcName.equals("getchar") || funcName.equals("putln")) {
                // 没有参数
                return count == 0;
            }
            // 其他库函数只有一个参数
            else return count == 1;
        }
        for(int i=0;i<AnalyserTable.getFunctionList().size();i++){
            if(AnalyserTable.getFunctionList().get(i).getFuncName().equals(funcName)){
                if(count == AnalyserTable.getParamList().size())
                    return true;
            }
        }
        return false;
    }
    // 去掉局部变量
    public static void clearLocal() {
        int len = AnalyserTable.getVarList().size();
        for (int i = len - 1; i >= 0; --i) {
            Variable variable = AnalyserTable.getVarList().get(i);
            if (variable.getVarLevel() > 1)
                AnalyserTable.getVarList().remove(i);
        }
        len = AnalyserTable.getConstList().size();
        for (int i = len - 1; i >= 0; --i) {
            Constant constant = AnalyserTable.getConstList().get(i);
            if (constant.getConstLevel() > 1)
                AnalyserTable.getConstList().remove(i);
        }
    }

}
