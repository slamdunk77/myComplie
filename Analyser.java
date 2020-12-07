package analyser;

import error.AnalyzeError;
import error.ErrorCode;
import tokenizer.*;

import java.util.ArrayList;
import java.util.Iterator;
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
    // 算符优先表
    private static int priority[][];

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
        // 如果是定义语句
        while(token.getTokenType() == TokenType.CONST_KW || token.getTokenType() == TokenType.LET_KW){
            analyseDecl();
        }
        // 如果不是是函数
        while(token.getTokenType() != TokenType.EOF){
            if(token.getTokenType() != TokenType.FN_KW)
                throw new AnalyzeError(ErrorCode.ExpectedToken, string_iter.currentPos());
            analyseFunction();
        }


    }
    // 分析定义语句
    public static void analyseDecl() throws Exception {
        if(token.getTokenType() == TokenType.CONST_KW)
            analyseConstDecl();
        else if(token.getTokenType() == TokenType.LET_KW)
            analyseLetDecl();
        else throw new AnalyzeError(ErrorCode.ExpectedToken, string_iter.currentPos());
    }

    // 分析Let定义语句
    // let_decl_stmt -> 'let' IDENT ':' ty ('=' expr)? ';'
    public static void analyseLetDecl() throws Exception {
        if(token.getTokenType() != TokenType.LET_KW)
            throw new AnalyzeError(ErrorCode.ExpectedToken, string_iter.currentPos());

        token = TokenIter.currentToken();
        if(token.getTokenType() != TokenType.IDENT)
            throw new AnalyzeError(ErrorCode.ExpectedToken, string_iter.currentPos());

        token = TokenIter.currentToken();
        if(token.getTokenType() != TokenType.COLON)
            throw new AnalyzeError(ErrorCode.ExpectedToken, string_iter.currentPos());

        token = TokenIter.currentToken();
        // 变量或常量的类型不能为 void
        if(!token.getValue().equals("int"))
            throw new AnalyzeError(ErrorCode.ExpectedToken, string_iter.currentPos());

        // expr语句
        token = TokenIter.currentToken();
        if(token.getTokenType() == TokenType.ASSIGN){
            isAssign = true;
            token = TokenIter.currentToken();
            analyseExpr();
            //弹栈
            while (!tokenStack.empty()) {
                tokenStack.pop();
            }
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
    public static void analyseConstDecl() throws Exception {
        if(token.getTokenType() != TokenType.CONST_KW)
            throw new AnalyzeError(ErrorCode.ExpectedToken, string_iter.currentPos());

        token = TokenIter.currentToken();
        if(token.getTokenType() != TokenType.IDENT)
            throw new AnalyzeError(ErrorCode.ExpectedToken, string_iter.currentPos());

        token = TokenIter.currentToken();
        if(token.getTokenType() != TokenType.COLON)
            throw new AnalyzeError(ErrorCode.ExpectedToken, string_iter.currentPos());

        token = TokenIter.currentToken();
        // 变量或常量的类型不能为 void
        if(!token.getValue().equals("int"))
            throw new AnalyzeError(ErrorCode.ExpectedToken, string_iter.currentPos());

        // const必须有等号
        token = TokenIter.currentToken();
        if(token.getValue() != TokenType.ASSIGN)
            throw new AnalyzeError(ErrorCode.ExpectedToken, string_iter.currentPos());
        isAssign = true;
        token = TokenIter.currentToken();
        analyseExpr();
        //弹栈
        while (!tokenStack.empty()) {
            tokenStack.pop();
        }
        isAssign = false;
        // 不用继续都下一个token，因为调用子程序后会多读一个
        if (token.getTokenType() != TokenType.SEMICOLON)
            throw new AnalyzeError(ErrorCode.ExpectedToken, string_iter.currentPos());

        // 读取下一个token
        token = TokenIter.currentToken();
    }

    // 分析表达式
    // expr -> operator_expr | negate_expr | assign_expr | as_expr
    // | call_expr | literal_expr | ident_expr | group_expr
    public static void analyseExpr() throws Exception {
        if (token.getTokenType() == TokenType.MINUS) {
            // 读入下一个token
            token = TokenIter.currentToken();
            if (token.getTokenType() == TokenType.MINUS) {

                analyseExpr();
            }
            else if (token.getTokenType() == TokenType.UINT_LITERAL) {
                analyseLiteralExpr();
                if (analyseBinaryOperator(token)) {
                    analyseOperatorExpr();
                }
            }
            else if (token.getTokenType() == TokenType.IDENT) {
                token = TokenIter.currentToken();
                if (token.getTokenType() == TokenType.L_PAREN) {
                    tokenStack.push(token);
                    analyseCallExpr();
                    //弹栈
                    while (tokenStack.peek().getTokenType() != TokenType.L_PAREN)
                        tokenStack.pop();
                    tokenStack.pop();
                    if (analyseBinaryOperator(token)) {
                        analyseOperatorExpr();
                    }
                }
                else if (analyseBinaryOperator(token)) {
                    analyseIdentExpr();
                    analyseOperatorExpr();
                }
                else analyseIdentExpr();
            }
        }
        else if (token.getTokenType() == TokenType.IDENT) {
            token = TokenIter.currentToken();
            if (token.getTokenType() == TokenType.ASSIGN) {
                if (isAssign)
                    throw new AnalyzeError(ErrorCode.InvalidAssignment, string_iter.currentPos());
                isAssign = true;
                analyseAssignExpr();
                isAssign = false;
                if (analyseBinaryOperator(token)) {
                    analyseOperatorExpr();
                }
            }
            else if (token.getTokenType() == TokenType.L_PAREN) {
                tokenStack.push(token);
                analyseCallExpr();
                //弹栈
                while (tokenStack.peek().getTokenType() != TokenType.L_PAREN) {
                    tokenStack.pop();
                }
                tokenStack.pop();
                if (analyseBinaryOperator(token)) {
                    analyseOperatorExpr();
                }
            }
            else if (analyseBinaryOperator(token)) {
                analyseIdentExpr();
                analyseOperatorExpr();
            }
            else if (token.getTokenType() == TokenType.AS_KW) {
                analyseAsExpr();
            }
            else {
                analyseIdentExpr();
            }
        }
        else if (token.getTokenType() == TokenType.UINT_LITERAL ||
                token.getTokenType() == TokenType.STRING_LITERAL) {
            analyseLiteralExpr();
            if (analyseBinaryOperator(token)) analyseOperatorExpr();
        }
        else if (token.getTokenType() == TokenType.L_PAREN) {
            tokenStack.push(token);
            analyseGroupExpr();
            if (analyseBinaryOperator(token)) {
                analyseOperatorExpr();
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
    public static void analyseOperatorExpr() throws Exception {
        if(!analyseBinaryOperator(token))
            throw new AnalyzeError(ErrorCode.ExpectedToken, string_iter.currentPos());
        if(!tokenStack.empty()){
            int i = OperatorTable.get_int(tokenStack.peek().getTokenType());
            int j = OperatorTable.get_int(token.getTokenType());
            if (priority[i][j] > 0) { //
                tokenStack.pop();
            }
        }
        tokenStack.push(token);
        token = TokenIter.currentToken();
        analyseExpr();
    }

    // 表达式 negate_expr -> '-' expr
    public static void analyseNegateExpr() throws Exception {
        token = TokenIter.currentToken();
        analyseExpr();
    }

    // 表达式 assign_expr -> l_expr '=' expr
    public static void analyseAssignExpr() throws Exception {
        token = TokenIter.currentToken();
        analyseExpr();
        while (!tokenStack.empty()) {
            tokenStack.pop();
        }
    }

    // 表达式 as_expr -> expr 'as' ty
    public static void analyseAsExpr() throws Exception {
        token = TokenIter.currentToken();
        if (!token.getValue().equals("int"))
            throw new AnalyzeError(ErrorCode.ExpectedToken, string_iter.currentPos());
    }

    // 表达式 call_param_list -> expr (',' expr)*
    public static int analyseCallParamList() throws Exception {
        analyseExpr();
        while (!tokenStack.empty() && tokenStack.peek().getTokenType() != TokenType.L_PAREN) {
            tokenStack.pop();
        }
        int count = 1;
        while (token.getTokenType() == TokenType.COMMA) {
            token = TokenIter.currentToken();
            analyseExpr();
            while (!tokenStack.empty() && tokenStack.peek().getTokenType() != TokenType.L_PAREN) {
                tokenStack.pop();
            }
            count++;
        }
        return count;
    }

    // 表达式 call_expr -> IDENT '(' call_param_list? ')'
    public static void analyseCallExpr() throws Exception {
        //参数个数
        int count = 0;

        token = TokenIter.currentToken();

        if (token.getTokenType() != TokenType.R_PAREN)
            count = analyseCallParamList();

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
        token = TokenIter.currentToken();
    }

    //ident_expr -> IDENT
    public static void analyseIdentExpr() throws Exception {
    }

    //group_expr -> '(' expr ')'
    public static void analyseGroupExpr() throws Exception {
        if (token.getTokenType() != TokenType.L_PAREN)
            throw new AnalyzeError(ErrorCode.ExpectedToken, string_iter.currentPos());

        token = TokenIter.currentToken();
        analyseExpr();

        if (token.getTokenType() != TokenType.R_PAREN)
            throw new AnalyzeError(ErrorCode.ExpectedToken, string_iter.currentPos());

        while (tokenStack.peek().getTokenType() != TokenType.L_PAREN) {
            tokenStack.pop();
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
        if(!(token.getValue().equals("int") || token.getValue().equals("void")))
            throw new AnalyzeError(ErrorCode.ExpectedToken, string_iter.currentPos());

        // 继续读取下一个token -> block——stmt
        token = TokenIter.currentToken();
        analyseBlock();
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
        token = TokenIter.currentToken();
        if(token.getTokenType() != TokenType.COLON)
            throw new AnalyzeError(ErrorCode.ExpectedToken, string_iter.currentPos());
        token = TokenIter.currentToken();
        if(!(token.getValue().equals("int") || token.getValue().equals("void")))
            throw new AnalyzeError(ErrorCode.ExpectedToken, string_iter.currentPos());
        token = TokenIter.currentToken();
    }
    // '{' stmt* '}'
    public static void analyseBlock() throws Exception {
        if (token.getTokenType() != TokenType.L_BRACE)
            throw new AnalyzeError(ErrorCode.NotDeclared, string_iter.currentPos());
        token = TokenIter.currentToken();
        while (token.getTokenType() != TokenType.R_BRACE) {
            analyseStmt();
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
    public static void analyseStmt() throws Exception {
        if (token.getTokenType() == TokenType.CONST_KW || token.getTokenType() == TokenType.LET_KW)
            analyseDecl();
        else if (token.getTokenType() == TokenType.IF_KW)
            analyseIf();
        else if (token.getTokenType() == TokenType.WHILE_KW)
            analyseWhile();
        else if (token.getTokenType() == TokenType.RETURN_KW)
            analyseReturn();
        else if (token.getTokenType() == TokenType.SEMICOLON)
            analyseEmpty();
        else if (token.getTokenType() == TokenType.L_BRACE)
            analyseBlock();
        else
            analyseExpr();
    }
    // if_stmt -> 'if' expr block_stmt ('else' 'if' expr block_stmt)* ('else' block_stmt)?
    public static void analyseIf() throws Exception {
        if (token.getTokenType() != TokenType.IF_KW)
            throw new AnalyzeError(ErrorCode.NotDeclared, string_iter.currentPos());
        token = TokenIter.currentToken();
        analyseExpr();
        // 弹栈，把expr中入栈的东西全部弹出???
        while (!tokenStack.empty()) {
            tokenStack.pop();
        }
        // 调用表达式时会多读入一个符号
        analyseBlock();
        if (token.getTokenType() == TokenType.ELSE_KW) {
            token = TokenIter.currentToken();
            if (token.getTokenType() == TokenType.IF_KW)
                analyseIf();
            else {
                analyseBlock();
            }
        }
    }

    // while_stmt -> 'while' expr block_stmt
    public static void analyseWhile() throws Exception {
        if (token.getTokenType() != TokenType.WHILE_KW)
            throw new AnalyzeError(ErrorCode.NotDeclared, string_iter.currentPos());
        token = TokenIter.currentToken();
        analyseExpr();
        // 弹栈，把expr中入栈的东西全部弹出???
        while (!tokenStack.empty()) {
            tokenStack.pop();
        }
        // 调用表达式时会多读入一个符号
        analyseBlock();
    }

    // return_stmt -> 'return' expr? ';'
    public static void analyseReturn() throws Exception {
        if (token.getTokenType() != TokenType.RETURN_KW)
            throw new AnalyzeError(ErrorCode.NotDeclared, string_iter.currentPos());
        token = TokenIter.currentToken();
        if(token.getTokenType() != TokenType.SEMICOLON){
            if(token.getValue().equals("int")){
                analyseExpr();
                // 弹栈，把expr中入栈的东西全部弹出???
                while (!tokenStack.empty()) {
                    tokenStack.pop();
                }
            }
            else if(token.getValue().equals("void"))
                throw new AnalyzeError(ErrorCode.NotDeclared, string_iter.currentPos());
        }
        if (token.getTokenType() != TokenType.SEMICOLON)
            throw new AnalyzeError(ErrorCode.NotDeclared, string_iter.currentPos());
        while (!tokenStack.empty()) {
            tokenStack.pop();
        }
        token = TokenIter.currentToken();
    }

    //空语句
    public static void analyseEmpty() throws Exception {
        if (token.getTokenType() != TokenType.SEMICOLON)
            throw new AnalyzeError(ErrorCode.NotDeclared, string_iter.currentPos());
        token = TokenIter.currentToken();
    }
}
