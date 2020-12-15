
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Stack;

public class Analyser {
    // 引入词法分析出来的token，进行语法分析
    private static ArrayList<Token> tokenList;
    // 指向tokenList的指针
    private static Iterator<Token> token_iter;
    private static StringIter string_iter;
    // 判断表达式的栈
    private static Stack<Token> tokenStack;
    // 读取的当前token
    private static Token token;
    //是否为赋值表达式
    private static boolean isAssign = false;
    //全局变量个数
    private static int globalCount = 0;
    //局部变量个数
    private static int localCount = 0;
    //函数个数
    private static int functionCount = 1;
    //参数起始地址
    private static int alloc = 0;
    //函数是否有返回值
    private static boolean isReturn = false;
    // 算符优先表
    private static int priority[][];

    //用于二进制输出
    private static FunctionDef startFunction;


    private static void initProgram(){
        TokenIter.initTokenList();
        TokenIter.initToken_iter();
        TokenIter.initToken();
        tokenList = TokenIter.getTokenList();
        token_iter = TokenIter.getToken_iter();
        token = TokenIter.getToken();
        tokenStack = new Stack<>();
        priority = OperatorTable.getPriority();
    }
    // progrom -> item*
    // item -> function | decl_stmt
    public static void analyseProgram() throws Exception {
        // 加载进词法分析的tokenList和迭代器
        initProgram();
        // 初始化表
        AnalyserTable.initAnalyserTable();
        // 如果是定义语句
        while(token.getTokenType() == TokenType.CONST_KW || token.getTokenType() == TokenType.LET_KW){
            // level 层数为1
            analyseDecl(1);
        }
        ArrayList<Instruction> instructionList1 = AnalyserTable.getInstructionList();
        // 如果不是是函数
        while(token.getTokenType() != TokenType.EOF) {
            if (token.getTokenType() != TokenType.FN_KW)
                throw new AnalyzeError(ErrorCode.ExpectedToken, string_iter.currentPos());
            // 开始分析函数，重置指令集
            AnalyserTable.setInstructionList(new ArrayList<>());
            // 初始化当前函数的参数表
            AnalyserTable.setParamList(new ArrayList<>());
            // 始化值局部变量个数
            localCount = 0;
            // 初始化函数是否有返回值
            isReturn = false;

            analyseFunction();
//            System.out.println(AnalyserTable.getFunctionList().get(0).getParams());
            // 若没有异常，全局变量加1，函数个数加一
            globalCount++;
            functionCount++;
        }
        if(AnalyserTable.findFunc("main") == null)
            throw new AnalyzeError(ErrorCode.ExpectedToken, string_iter.currentPos());
        // 填入主函数
        GlobalDef globalDef = new GlobalDef(1,6, "_start");
        AnalyserTable.getGlobalDefList().add(globalDef);
        // 加入指令
        // 一开始设置分配空间为0
        Instruction instruction = new Instruction(InstructionType.stackalloc, 0);
        instructionList1.add(instruction);
        // 加入函数返回指令
        if(AnalyserTable.isFuncReturn("main")){
            // 分配一个返回地址的空间
            instruction.setInstrId(1);
            // call main
            instruction = new Instruction(InstructionType.call, functionCount-1);
            instructionList1.add(instruction);
//                // 返回后把栈中关于返回函数中所有的变量全部弹出
            instruction = new Instruction(InstructionType.popn, 1);
            AnalyserTable.getInstructionList().add(instruction);
        }
        else{
            // call main
            instruction = new Instruction(InstructionType.call, functionCount-1);
            instructionList1.add(instruction);
        }
        startFunction = new FunctionDef(globalCount, "_start",0, 0, 0, instructionList1);
        globalCount++;
        // 对函数分析
    }
    // 分析定义语句
    public static void analyseDecl(Integer level) throws Exception {
        if(token.getTokenType() == TokenType.CONST_KW)
            analyseConstDecl(level);
        else if(token.getTokenType() == TokenType.LET_KW)
            analyseLetDecl(level);
        else throw new AnalyzeError(ErrorCode.ExpectedToken, string_iter.currentPos());
        if(level == 1) globalCount++;
        else localCount++;
        // cxy
    }

    // 分析Let定义语句
    // let_decl_stmt -> 'let' IDENT ':' ty ('=' expr)? ';'
    public static void analyseLetDecl(Integer level) throws Exception {
        if(token.getTokenType() != TokenType.LET_KW)
            throw new AnalyzeError(ErrorCode.ExpectedToken, string_iter.currentPos());

        token = TokenIter.currentToken();
        if(token.getTokenType() != TokenType.IDENT)
            throw new AnalyzeError(ErrorCode.ExpectedToken, string_iter.currentPos());

        if(!AnalyserTable.isValidVarName(token.getValue(), level))
            throw new AnalyzeError(ErrorCode.ExpectedToken, string_iter.currentPos());

        if(level == 1){
            Variable var = new Variable(token.getValue(), globalCount, level);
            AnalyserTable.getVarList().add(var);
            GlobalDef glo = new GlobalDef(0,token.getValue().length(), token.getValue());
            AnalyserTable.getGlobalDefList().add(glo);
//            globalCount++; // cxy
        }
        else{
            Variable var = new Variable(token.getValue(), localCount, level);
            AnalyserTable.getVarList().add(var);
//            localCount++; // cxy
        }


        token = TokenIter.currentToken();
        if(token.getTokenType() != TokenType.COLON)
            throw new AnalyzeError(ErrorCode.ExpectedToken, string_iter.currentPos());

        token = TokenIter.currentToken();
        Token record = token;
        // 变量或常量的类型不能为 void
        if(!token.getValue().equals("int"))
            throw new AnalyzeError(ErrorCode.ExpectedToken, string_iter.currentPos());

        // expr语句
        // let_decl_stmt -> 'let' IDENT ':' ty ('=' expr)? ';'
        token = TokenIter.currentToken();
        if(token.getTokenType() == TokenType.ASSIGN){
            // 取到值
            isAssign = true;
            // 加载地址
            if(level == 1){
                // cxy -1
                Instruction instruction = new Instruction(InstructionType.globa, globalCount);
                AnalyserTable.getInstructionList().add(instruction);
            }
            else{
                Instruction instruction = new Instruction(InstructionType.loca, localCount);
                AnalyserTable.getInstructionList().add(instruction);
            }
            token = TokenIter.currentToken();
            analyseExpr(level);
            //弹栈
            Token token1;
            while (!tokenStack.empty()) {
                token1 = tokenStack.pop();
                AnalyserTable.operationInstruction(token1.getTokenType());
            }
            // 紧接着加载地址，在加载的地址存值
            Instruction instruction = new Instruction(InstructionType.store, null);
            AnalyserTable.getInstructionList().add(instruction);
            // 重置
            isAssign = false;
        }

        // 不用继续都下一个token，因为调用子程序后会多读一个
        if (token.getTokenType() != TokenType.SEMICOLON)
            throw new AnalyzeError(ErrorCode.ExpectedToken, string_iter.currentPos());

        // 读取下一个token
        token = TokenIter.currentToken();
    }

    // 分析Const定义语句
    // 'const' IDENT ':' ty '=' expr ';
    public static void analyseConstDecl(Integer level) throws Exception {
        if(token.getTokenType() != TokenType.CONST_KW)
            throw new AnalyzeError(ErrorCode.ExpectedToken, string_iter.currentPos());

        token = TokenIter.currentToken();
        if(token.getTokenType() != TokenType.IDENT)
            throw new AnalyzeError(ErrorCode.ExpectedToken, string_iter.currentPos());
        // 检查名字是否合法
        if(!AnalyserTable.isValidVarName(token.getValue(), level))
            throw new AnalyzeError(ErrorCode.ExpectedToken, string_iter.currentPos());
        // 加入变量表
        if(level == 1){
            Constant cons = new Constant(token.getValue(), globalCount, level);
            AnalyserTable.getConstList().add(cons);
            GlobalDef glo = new GlobalDef(1,token.getValue().length(), token.getValue());
            AnalyserTable.getGlobalDefList().add(glo);
            //生成 globa 指令，准备赋值
            Instruction instruction = new Instruction(InstructionType.globa, globalCount);
            AnalyserTable.getInstructionList().add(instruction);
//            globalCount++; // cxy
            // 赋值指令cxy
        }
        else{
            Constant cons = new Constant(token.getValue(), localCount, level);
            AnalyserTable.getConstList().add(cons);
            //生成 loca 指令，准备赋值
            Instruction instruction = new Instruction(InstructionType.loca, localCount);
            AnalyserTable.getInstructionList().add(instruction);
//            localCount++; // cxy
        }
        //'const' IDENT ':' ty '=' expr ';
        token = TokenIter.currentToken();
        if(token.getTokenType() != TokenType.COLON)
            throw new AnalyzeError(ErrorCode.ExpectedToken, string_iter.currentPos());

        token = TokenIter.currentToken();
        // 变量或常量的类型不能为 void
        if(!token.getValue().equals("int"))
            throw new AnalyzeError(ErrorCode.ExpectedToken, string_iter.currentPos());

        // const必须有等号
        token = TokenIter.currentToken();
        if(token.getTokenType() != TokenType.ASSIGN)
            throw new AnalyzeError(ErrorCode.ExpectedToken, string_iter.currentPos());

        // 加载地址 cxy
        isAssign = true;
//        if(level == 1){
//            // cxy -1
//            Instruction instruction = new Instruction(InstructionType.globa, globalCount-1);
//            AnalyserTable.getInstructionList().add(instruction);
//        }
//        else{
//            Instruction instruction = new Instruction(InstructionType.loca, globalCount-1);
//            AnalyserTable.getInstructionList().add(instruction);
//        }
        token = TokenIter.currentToken();
        analyseExpr(level);
        //弹栈
        Token token1;
        while (!tokenStack.empty()) {
            token1 = tokenStack.pop();
            AnalyserTable.operationInstruction(token1.getTokenType());
        }
        isAssign = false;
        // 不用继续都下一个token，因为调用子程序后会多读一个
        if (token.getTokenType() != TokenType.SEMICOLON)
            throw new AnalyzeError(ErrorCode.ExpectedToken, string_iter.currentPos());

        // 赋值完成后，将值加载到地址
        Instruction instruction = new Instruction(InstructionType.store, null);
        AnalyserTable.getInstructionList().add(instruction);
        // 读取下一个token
        token = TokenIter.currentToken();
    }

    //expr_stmt -> expr ';'
    public static void analyseExprStmt(Integer level) throws Exception {
        analyseExpr(level);
        //弹栈
        while (!tokenStack.empty()) {
            AnalyserTable.operationInstruction(tokenStack.pop().getTokenType());
        }
        if (token.getTokenType() != TokenType.SEMICOLON)
            throw new AnalyzeError(ErrorCode.ExpectedToken, string_iter.ptr);

        token = TokenIter.currentToken();
    }

    // 分析表达式
    // expr -> operator_expr | negate_expr | assign_expr | as_expr
    // | call_expr | literal_expr | ident_expr | group_expr
    public static void analyseExpr(Integer level) throws Exception {
        if (token.getTokenType() == TokenType.MINUS) {
            // 读入下一个token
            Instruction minus_instr = new Instruction(InstructionType.neg, null);
            token = TokenIter.currentToken();
            if (token.getTokenType() == TokenType.MINUS) {
                analyseExpr(level);
                // 对整个表达式加 -
                AnalyserTable.getInstructionList().add(minus_instr);
            }
            else if (token.getTokenType() == TokenType.UINT_LITERAL) {
                analyseLiteralExpr();
                AnalyserTable.getInstructionList().add(minus_instr);
                if (analyseBinaryOperator(token)) {
                    analyseOperatorExpr(level);
                }
            }
            else if (token.getTokenType() == TokenType.IDENT) {
                // 记录变量名
                Token record = token;
                token = TokenIter.currentToken();
                if (token.getTokenType() == TokenType.L_PAREN) {
                    tokenStack.push(token);
                    // 是否存在该函数名
                    if(AnalyserTable.findFunc(record.getValue()) != null){
                        Instruction instruction;
                        Integer inte;
                        // 是库函数
                        if (AnalyserTable.isLibraryFuncName(record.getValue())) {
                            LibraryFunction libraryFunction = new LibraryFunction(record.getValue(), globalCount);
                            AnalyserTable.getLibraryFuncList().add(libraryFunction);
                            inte = globalCount;
                            globalCount++;
                            // cxy
                            GlobalDef globalDef = new GlobalDef(1, record.getValue().length(), record.getValue());
                            // 全局符号表
                            AnalyserTable.getGlobalDefList().add(globalDef);
                            instruction = new Instruction(InstructionType.callname, globalCount);
//                            globalCount++;
                        }
                        //自定义函数
                        else {
                            inte = AnalyserTable.getParamAddr(record.getValue());
                            instruction = new Instruction(InstructionType.call, inte);
                        }
                        analyseCallExpr(record.getValue(), level);

                        //弹栈
                        Token token1;
                        while (tokenStack.peek().getTokenType() != TokenType.L_PAREN){
                           token1 = tokenStack.pop();
                           AnalyserTable.operationInstruction(token1.getTokenType());
                        }
                        tokenStack.pop();
                        AnalyserTable.getInstructionList().add(instruction);
                    }
                    else
                        throw new AnalyzeError(ErrorCode.ExpectedToken, string_iter.currentPos());
                    AnalyserTable.getInstructionList().add(minus_instr);
                    if (analyseBinaryOperator(token)) {
                        analyseOperatorExpr(level);
                    }
                }
                else if (analyseBinaryOperator(token)) {
                    analyseIdentExpr(record.getValue(), level);
                    AnalyserTable.getInstructionList().add(minus_instr);
                    analyseOperatorExpr(level);
                }
                else {
                    analyseIdentExpr(record.getValue(), level);
                    AnalyserTable.getInstructionList().add(minus_instr);
                }
            }
        }
        else if (token.getTokenType() == TokenType.IDENT) {
            // 记录名字
            Token record = token;
            token = TokenIter.currentToken();
            // assign_expr -> l_expr '=' expr
            if (token.getTokenType() == TokenType.ASSIGN) {
                // 如果已经是赋值函数，不允许多个=
                if (isAssign)
                    throw new AnalyzeError(ErrorCode.InvalidAssignment, string_iter.currentPos());
                // 赋值表达式左边只能为变量 参数
                if((!AnalyserTable.isConstant(record.getValue()))&&(AnalyserTable.isVariable(record.getValue())||AnalyserTable.isParam(record.getValue()))){
                    // cxy
                    if(AnalyserTable.isLocal(record.getValue())){
                        // 赋值，需要加载地址
                        Instruction instruction = new Instruction(InstructionType.loca, AnalyserTable.getAddr(record.getValue(), level));
                        AnalyserTable.getInstructionList().add(instruction);
                    }
                    else if(AnalyserTable.isParam(record.getValue())){
                        // 赋值，需要加载地址
                        Instruction instruction = new Instruction(InstructionType.arga, AnalyserTable.getParamAddr(record.getValue()));
                        AnalyserTable.getInstructionList().add(instruction);
                    }
                    else{ // 全局变量
                        // 赋值，需要加载地址
                        Instruction instruction = new Instruction(InstructionType.globa, AnalyserTable.getAddr(record.getValue(), level));
                        AnalyserTable.getInstructionList().add(instruction);
                    }
                    isAssign = true;
                    analyseAssignExpr(record.getValue(), level);
                }
                else throw new AnalyzeError(ErrorCode.ExpectedToken, string_iter.currentPos());
                // 分析完赋值表达式后，重置
                isAssign = false;
                if (analyseBinaryOperator(token)) {
                    analyseOperatorExpr(level);
                }
            }
            else if (token.getTokenType() == TokenType.L_PAREN) {
                tokenStack.push(token);
                // call_expr -> IDENT '(' call_param_list? ')'
                // call函数表达式
                if(AnalyserTable.isFunction(record.getValue())){
                    Instruction instruction;
                    Integer inte;
                    // 是库函数
                    if (AnalyserTable.isLibraryFuncName(record.getValue())) {
                        LibraryFunction libraryFunction = new LibraryFunction(record.getValue(), globalCount);
                        AnalyserTable.getLibraryFuncList().add(libraryFunction);
                        // cxy
                        inte = globalCount;
                        globalCount++;

                        GlobalDef globalDef = new GlobalDef(1, record.getValue().length(), record.getValue());
                        // 全局符号表
                        AnalyserTable.getGlobalDefList().add(globalDef);
                        instruction = new Instruction(InstructionType.callname, inte);
//                        globalCount++;
                    }
                    //自定义函数
                    else {
                        instruction = new Instruction(InstructionType.call, AnalyserTable.getFunctionAddr(record.getValue()));
//                        System.out.println(AnalyserTable.getFunctionList().get(0).getParams());
                    }
                    analyseCallExpr(record.getValue(), level);
                    //弹栈
                    Token token1;
                    while (tokenStack.peek().getTokenType() != TokenType.L_PAREN) {
                        token1 = tokenStack.pop();
                        AnalyserTable.operationInstruction(token1.getTokenType());
                    }
                    tokenStack.pop();
                    AnalyserTable.getInstructionList().add(instruction);
                }
                else throw new AnalyzeError(ErrorCode.ExpectedToken, string_iter.currentPos());

                if (analyseBinaryOperator(token)) {
                    analyseOperatorExpr(level);
                }
            }
            else if (analyseBinaryOperator(token)) {
                analyseIdentExpr(record.getValue(), level);
                analyseOperatorExpr(level);
            }
            else if (token.getTokenType() == TokenType.AS_KW) {
                analyseAsExpr();
            }
            else {
                analyseIdentExpr(record.getValue(), level);
            }
        }
        else if (token.getTokenType() == TokenType.UINT_LITERAL ||
                token.getTokenType() == TokenType.STRING_LITERAL) {
            analyseLiteralExpr();
            if (analyseBinaryOperator(token)) analyseOperatorExpr(level);
        }
        else if (token.getTokenType() == TokenType.L_PAREN) {
            tokenStack.push(token);
            analyseGroupExpr(level);
            if (analyseBinaryOperator(token)) {
                analyseOperatorExpr(level);
            }
        }
        else throw new AnalyzeError(ErrorCode.ExpectedToken, string_iter.currentPos());
    }

    // 表达式 binary_operator -> '+' | '-' | '*' | '/' | '==' | '!=' | '<' | '>' | '<=' | '>='
    public static boolean analyseBinaryOperator(Token token) {
        if (token.getTokenType() != TokenType.PLUS &&
                token.getTokenType() != TokenType.MINUS &&
                token.getTokenType() != TokenType.MUL &&
                token.getTokenType() != TokenType.DIV &&
                token.getTokenType() != TokenType.EQ &&
                token.getTokenType() != TokenType.NEQ &&
                token.getTokenType() != TokenType.LE &&
                token.getTokenType() != TokenType.LT &&
                token.getTokenType() != TokenType.GE &&
                token.getTokenType() != TokenType.GT) return false;
        return true;
    }

    // 表达式 operator_expr -> expr binary_operator expr
    public static void analyseOperatorExpr(Integer level) throws Exception {
        if(!analyseBinaryOperator(token))
            throw new AnalyzeError(ErrorCode.ExpectedToken, string_iter.currentPos());
        Token token1;
        if(!tokenStack.empty()){
            int i = OperatorTable.get_int(tokenStack.peek().getTokenType());
            int j = OperatorTable.get_int(token.getTokenType());
            if (priority[i][j] > 0) { //
                token1 = tokenStack.pop();
                AnalyserTable.operationInstruction(token1.getTokenType());
            }
        }
        tokenStack.push(token);
        token = TokenIter.currentToken();
        analyseExpr(level);
    }

    // 表达式 negate_expr -> '-' expr
    public static void analyseNegateExpr(Integer level) throws Exception {
        token = TokenIter.currentToken();
        analyseExpr(level);
    }

    // 表达式 assign_expr -> l_expr '=' expr
    public static void analyseAssignExpr(String varName, Integer level) throws Exception {
        token = TokenIter.currentToken();
        analyseExpr(level);
        Token token1;
        while (!tokenStack.empty()) {
            token1 = tokenStack.pop();
            AnalyserTable.operationInstruction(token1.getTokenType());
        }
        // 调用这个函数之前便加载了地址
        // 此处只需要把值存到加载的地址中
        Instruction instruction = new Instruction(InstructionType.store, null);
        AnalyserTable.getInstructionList().add(instruction);
    }

    // 表达式 as_expr -> expr 'as' ty
    public static void analyseAsExpr() throws Exception {
        token = TokenIter.currentToken();
        if (!token.getValue().equals("int"))
            throw new AnalyzeError(ErrorCode.ExpectedToken, string_iter.currentPos());
    }

    // 表达式 call_param_list -> expr (',' expr)*
    public static int analyseCallParamList(Integer level) throws Exception {
        analyseExpr(level);
        while (!tokenStack.empty() && tokenStack.peek().getTokenType() != TokenType.L_PAREN) {
            AnalyserTable.operationInstruction(tokenStack.pop().getTokenType());
        }
        int count = 1;
        while (token.getTokenType() == TokenType.COMMA) {
            token = TokenIter.currentToken();
            analyseExpr(level);
            while (!tokenStack.empty() && tokenStack.peek().getTokenType() != TokenType.L_PAREN) {
                AnalyserTable.operationInstruction(tokenStack.pop().getTokenType());
            }
            count++;
        }
        return count;
    }

    // 表达式 call_expr -> IDENT '(' call_param_list? ')'
    public static void analyseCallExpr(String varName, Integer level) throws Exception {
        //参数个数
        int count = 0;
        Instruction instruction;
        // 已读入一个token -> IDENT
        // 调用函数时，调用前为返回值和参数分配slot
        // 若有返回值
        if(AnalyserTable.isFuncReturn(varName)){
            // 分配一个地址空间，存储返回值
            instruction = new Instruction(InstructionType.stackalloc, 1);
        }
        else{ //若没有返回值，不能出现 = ,且不用分配返回值空间
            if(isAssign)
                throw new AnalyzeError(ErrorCode.ExpectedToken, string_iter.currentPos());
            instruction = new Instruction(InstructionType.stackalloc, 0);
        }
        AnalyserTable.getInstructionList().add(instruction);
        token = TokenIter.currentToken();

        if (token.getTokenType() != TokenType.R_PAREN)
            count = analyseCallParamList(level);


        // 检查传入的参数是否合法：类型 数量......
        if(!AnalyserTable.checkParam(varName, count))
            throw new AnalyzeError(ErrorCode.ExpectedToken, string_iter.currentPos());

        if (token.getTokenType() != TokenType.R_PAREN)
            throw new AnalyzeError(ErrorCode.ExpectedToken, string_iter.currentPos());

        token = TokenIter.currentToken();
    }

    // 表达式 literal_expr -> UINT_LITERAL | DOUBLE_LITERAL | STRING_LITERAL
    public static void analyseLiteralExpr() throws Exception {
        if (!(token.getTokenType() == TokenType.UINT_LITERAL
        ||token.getTokenType()  == TokenType.STRING_LITERAL)) {
            throw new AnalyzeError(ErrorCode.ExpectedToken, string_iter.currentPos());
        }
        if (token.getTokenType() == TokenType.UINT_LITERAL) {
            //加载常数
            Instruction instruction = new Instruction(InstructionType.push, Integer.parseInt(token.getValue()));
            AnalyserTable.getInstructionList().add(instruction);
        }
        else if (token.getTokenType() == TokenType.STRING_LITERAL) {
            //填入全局符号表 cxy
            GlobalDef globalDef = new GlobalDef(1, token.getValue().length(), token.getValue());
            AnalyserTable.getGlobalDefList().add(globalDef);

            //加入指令集
            Instruction instruction = new Instruction(InstructionType.push, globalCount);
            AnalyserTable.getInstructionList().add(instruction);
            globalCount++;

        }
        else throw new AnalyzeError(ErrorCode.ExpectedToken, string_iter.currentPos());
        token = TokenIter.currentToken();
    }

    //ident_expr -> IDENT
    public static void analyseIdentExpr(String varName, Integer level) throws Exception {
        if (!(AnalyserTable.isVariable(varName) || AnalyserTable.isConstant(varName) || AnalyserTable.isParam(varName)))
            throw new AnalyzeError(ErrorCode.NotDeclared, string_iter.currentPos());

        Instruction instruction;
        //局部变量
        if (AnalyserTable.isLocal(varName)) {
            instruction = new Instruction(InstructionType.loca, AnalyserTable.getAddr(varName, level));
            AnalyserTable.getInstructionList().add(instruction);
        }
        //参数
        else if (AnalyserTable.isParam(varName)) {
            instruction = new Instruction(InstructionType.arga, alloc + AnalyserTable.getParamAddr(varName));
            AnalyserTable.getInstructionList().add(instruction);
        }
        //全局变量
        else {
            instruction = new Instruction(InstructionType.globa, AnalyserTable.getAddr(varName, level));
            AnalyserTable.getInstructionList().add(instruction);
        }
        instruction = new Instruction(InstructionType.load, null);
        AnalyserTable.getInstructionList().add(instruction);
    }

    //group_expr -> '(' expr ')'
    public static void analyseGroupExpr(Integer level) throws Exception {
        if (token.getTokenType() != TokenType.L_PAREN)
            throw new AnalyzeError(ErrorCode.ExpectedToken, string_iter.currentPos());

        token = TokenIter.currentToken();
        analyseExpr(level);

        if (token.getTokenType() != TokenType.R_PAREN)
            throw new AnalyzeError(ErrorCode.ExpectedToken, string_iter.currentPos());

        while (tokenStack.peek().getTokenType() != TokenType.L_PAREN) {
            AnalyserTable.operationInstruction(tokenStack.pop().getTokenType());
        }
        tokenStack.pop();
        token = TokenIter.currentToken();
    }

    // 分析函数
    // function -> 'fn' IDENT '(' function_param_list? ')' '->' ty block_stmt
    // function_param_list -> function_param (',' function_param)*
    // function_param -> 'const'? IDENT ':' ty
    public static void analyseFunction() throws Exception {
        // 调用程序之前都先读取一个token
        token = TokenIter.currentToken();
        if(token.getTokenType() != TokenType.IDENT)
            throw new AnalyzeError(ErrorCode.ExpectedToken, string_iter.currentPos());
        Token record = token;
        // 检查函数名是否合法
        if(AnalyserTable.isFunction(token.getValue()))
            throw new AnalyzeError(ErrorCode.ExpectedToken, string_iter.currentPos());

        // 继续读取下一个token -> (
        token = TokenIter.currentToken();
        if(token.getTokenType() != TokenType.L_PAREN)
            throw new AnalyzeError(ErrorCode.ExpectedToken, string_iter.currentPos());

        // 继续读取下一个token -> function_param_list
        token = TokenIter.currentToken();
        if(token.getTokenType() != TokenType.R_PAREN)
            analyseFunctionParamList();

        // 继续读取下一个token -> )
//        token = TokenIter.currentToken();
        if(token.getTokenType() != TokenType.R_PAREN)
            throw new AnalyzeError(ErrorCode.ExpectedToken, string_iter.currentPos());

        // 继续读取下一个token -> ->
        token = TokenIter.currentToken();
        if(token.getTokenType() != TokenType.ARROW)
            throw new AnalyzeError(ErrorCode.ExpectedToken, string_iter.currentPos());

        // 继续读取下一个token -> ->
        token = TokenIter.currentToken();
        String type = token.getValue();
        if(!(type.equals("int") || type.equals("void")))
            throw new AnalyzeError(ErrorCode.ExpectedToken, string_iter.currentPos());
        // 是否有返回的分别处理
        Integer returnSlot;
        if(type.equals("int")){
            returnSlot = 1;
            alloc = 1;
        }
        else{
            returnSlot = 0;
            alloc = 0;
            isReturn = true;
        }
        Function function = new Function(record.getValue(), type, functionCount, AnalyserTable.getParamList());
        AnalyserTable.getFunctionList().add(function);
//        System.out.println(AnalyserTable.getFunctionList().get(0).getParams());
        // 继续读取下一个token -> block——stmt
        token = TokenIter.currentToken();
        analyseBlock(type, 2);

        // 针对int,必须有return语句
        if (!isReturn)
            throw new AnalyzeError(ErrorCode.ExpectedToken, string_iter.currentPos());

        if (type.equals("void")) {
            //ret
            Instruction instruction = new Instruction(InstructionType.ret, null);
            AnalyserTable.getInstructionList().add(instruction);
        }

        GlobalDef globalDef = new GlobalDef(1, record.getValue().length(), record.getValue());
        AnalyserTable.getGlobalDefList().add(globalDef);

        FunctionDef functionDef = new FunctionDef(globalCount, record.getValue(), returnSlot, AnalyserTable.getParamList().size(), localCount, AnalyserTable.getInstructionList());
        AnalyserTable.getFunctionDefList().add(functionDef);

        //从列表中去掉局部变量
        AnalyserTable.clearLocal();
    }

    // function_param_list -> function_param (',' function_param)*
    public static void analyseFunctionParamList() throws AnalyzeError{
        analyseFunctionParam();
        while(token.getTokenType() == TokenType.COMMA){
            token = TokenIter.currentToken();
            analyseFunctionParam();
        }
    }
    // function_param -> 'const'? IDENT ':' ty
    public static void analyseFunctionParam() throws AnalyzeError{
        if(token.getTokenType() == TokenType.CONST_KW)
            token = TokenIter.currentToken();
        if(token.getTokenType() != TokenType.IDENT)
            throw new AnalyzeError(ErrorCode.ExpectedToken, string_iter.currentPos());
        // 记录变量
        Token record = token;

        token = TokenIter.currentToken();
        if(token.getTokenType() != TokenType.COLON)
            throw new AnalyzeError(ErrorCode.ExpectedToken, string_iter.currentPos());

        token = TokenIter.currentToken();
        if(!(token.getValue().equals("int") || token.getValue().equals("void")))
            throw new AnalyzeError(ErrorCode.ExpectedToken, string_iter.currentPos());

        // 添加参数
        Param param = new Param(token.getValue(), record.getValue());
        AnalyserTable.getParamList().add(param);
        // cxy
        token = TokenIter.currentToken();
    }
    // '{' stmt* '}'
    public static void analyseBlock(String type, Integer level) throws Exception {
        if (token.getTokenType() != TokenType.L_BRACE)
            throw new AnalyzeError(ErrorCode.NotDeclared, string_iter.currentPos());
        token = TokenIter.currentToken();
        while (token.getTokenType() != TokenType.R_BRACE) {
            analyseStmt(type, level);
        }
        token = TokenIter.currentToken();
    }

    // 语句 stmt ->
    //      expr_stmt
    //    | decl_stmt
    //    | if_stmt
    //    | while_stmt
    //    | break_stmt
    //    | continue_stmt
    //    | return_stmt
    //    | block_stmt
    //    | empty_stmt
    public static void analyseStmt(String type, Integer level) throws Exception {
        if (token.getTokenType() == TokenType.CONST_KW || token.getTokenType() == TokenType.LET_KW)
            analyseDecl(level);
        else if (token.getTokenType() == TokenType.IF_KW)
            analyseIf(type, level);
        else if (token.getTokenType() == TokenType.WHILE_KW)
            analyseWhile(type, level);
        else if (token.getTokenType() == TokenType.RETURN_KW)
            analyseReturn(type, level);
        else if (token.getTokenType() == TokenType.SEMICOLON)
            analyseEmpty();
        else if (token.getTokenType() == TokenType.L_BRACE)
            analyseBlock(type, level + 1);
        else
            analyseExprStmt(level);
    }
    // if_stmt -> 'if' expr block_stmt ('else' 'if' expr block_stmt)* ('else' block_stmt)?
    public static void analyseIf(String type, Integer level) throws Exception {
        if (token.getTokenType() != TokenType.IF_KW)
            throw new AnalyzeError(ErrorCode.NotDeclared, string_iter.currentPos());
        token = TokenIter.currentToken();
        analyseExpr(level);
        // 弹栈，把expr中入栈的东西全部弹出
        while (!tokenStack.empty()) {
            AnalyserTable.operationInstruction(tokenStack.pop().getTokenType());
        }

        //brTrue
        Instruction instruction = new Instruction(InstructionType.brTrue, 1);
        AnalyserTable.getInstructionList().add(instruction);
        //br
        Instruction ifInstruction = new Instruction(InstructionType.br, 0);
        AnalyserTable.getInstructionList().add(ifInstruction);
        int index = AnalyserTable.getInstructionList().size();

        // 调用表达式时会多读入一个符号
        analyseBlock(type, level + 1);

        int size = AnalyserTable.getInstructionList().size();
        if (AnalyserTable.getInstructionList().get(size -1).getInstr().getInstructionNum()== 0x49) {
            int dis = AnalyserTable.getInstructionList().size() - index;
            ifInstruction.setInstrId(dis);

            if (token.getTokenType() == TokenType.ELSE_KW) {
                token = TokenIter.currentToken();
                if (token.getTokenType() == TokenType.IF_KW)
                    analyseIf(type, level);
                else {
                    analyseBlock(type, level+1);
                    size = AnalyserTable.getInstructionList().size();
                    instruction = new Instruction(InstructionType.br, 0);
                    AnalyserTable.getInstructionList().add(instruction);
                }
            }
        }
        else {
            Instruction jumpInstruction = new Instruction(InstructionType.br, null);
            AnalyserTable.getInstructionList().add(jumpInstruction);
            int jump = AnalyserTable.getInstructionList().size();

            int dis = AnalyserTable.getInstructionList().size() - index;
            ifInstruction.setInstrId(dis);
            if (token.getTokenType() == TokenType.ELSE_KW) {
                token = TokenIter.currentToken();
                if (token.getTokenType() == TokenType.IF_KW)
                    analyseIf(type, level);
                else {
                    analyseBlock(type, level+1);
                    size = AnalyserTable.getInstructionList().size();
                    instruction = new Instruction(InstructionType.br, 0);
                    AnalyserTable.getInstructionList().add(instruction);
                }
            }
            dis = AnalyserTable.getInstructionList().size() - jump;
            jumpInstruction.setInstrId(dis);
        }
    }

    // while_stmt -> 'while' expr block_stmt
    public static void analyseWhile(String type, Integer level) throws Exception {
        if (token.getTokenType() != TokenType.WHILE_KW)
            throw new AnalyzeError(ErrorCode.NotDeclared, string_iter.currentPos());

        Instruction instruction = new Instruction(InstructionType.br, 0);
        AnalyserTable.getInstructionList().add(instruction);

        int whileStart = AnalyserTable.getInstructionList().size();

        token = TokenIter.currentToken();
        analyseExpr(level);
        // 弹栈，把expr中入栈的东西全部弹出???
        while (!tokenStack.empty()) {
            AnalyserTable.operationInstruction(tokenStack.pop().getTokenType());
        }
        //brTrue
        instruction = new Instruction(InstructionType.brTrue, 1);
        AnalyserTable.getInstructionList().add(instruction);
        //br
        Instruction jumpInstruction = new Instruction(InstructionType.br, 0);
        AnalyserTable.getInstructionList().add(jumpInstruction);
        int index = AnalyserTable.getInstructionList().size();
        // 调用表达式时会多读入一个符号
        analyseBlock(type, level + 1);

        //跳至while 判断语句
        instruction = new Instruction(InstructionType.br, 0);
        AnalyserTable.getInstructionList().add(instruction);
        int whileEnd = AnalyserTable.getInstructionList().size();
        int dis = whileStart - whileEnd;
        instruction.setInstrId(dis);

        dis = AnalyserTable.getInstructionList().size() - index;
        jumpInstruction.setInstrId(dis);
    }

    // return_stmt -> 'return' expr? ';'
    public static void analyseReturn(String type, Integer level) throws Exception {
        if (token.getTokenType() != TokenType.RETURN_KW)
            throw new AnalyzeError(ErrorCode.NotDeclared, string_iter.currentPos());
        token = TokenIter.currentToken();

        if(token.getTokenType() != TokenType.SEMICOLON){
            if(type.equals("int")){
                //取返回地址
                Instruction instruction = new Instruction(InstructionType.arga, 0);
                AnalyserTable.getInstructionList().add(instruction);

                analyseExpr(level);
                // 弹栈，把expr中入栈的东西全部弹出???
                while (!tokenStack.empty()) {
                    AnalyserTable.operationInstruction(tokenStack.pop().getTokenType());
                }
                //放入地址中
                instruction = new Instruction(InstructionType.store, null);
                AnalyserTable.getInstructionList().add(instruction);
                isReturn = true;
            }
            else if(type.equals("void"))
                throw new AnalyzeError(ErrorCode.NotDeclared, string_iter.currentPos());
        }
        if (token.getTokenType() != TokenType.SEMICOLON)
            throw new AnalyzeError(ErrorCode.NotDeclared, string_iter.currentPos());
        while (!tokenStack.empty()) {
            AnalyserTable.operationInstruction(tokenStack.pop().getTokenType());
        }
        //ret
        Instruction instructions = new Instruction(InstructionType.ret, null);
        AnalyserTable.getInstructionList().add(instructions);
        token = TokenIter.currentToken();
    }

    //空语句
    public static void analyseEmpty() throws Exception {
        if (token.getTokenType() != TokenType.SEMICOLON)
            throw new AnalyzeError(ErrorCode.NotDeclared, string_iter.currentPos());
        token = TokenIter.currentToken();
    }
    // 用于二进制输出
    public static FunctionDef getStartFunction(){
        return startFunction;
    }
}
