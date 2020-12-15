

public class Tokenizer {

    private StringIter it;
    String str_token="";
    public Tokenizer(StringIter it) {
        this.it = it;
    }
    public String[] tokenList = new String[1000];
    public int i = 0;

    // 这里本来是想实现 Iterator<Token> 的，但是 Iterator 不允许抛异常，于是就这样了
    /**
     * 获取下一个 Token
     * 
     * @return
     * @throws TokenizeError 如果解析有异常则抛出
     */
    public Token nextToken() throws TokenizeError {
        it.readAll();

        // 跳过之前的所有空白字符
        skipSpaceCharacters();

        if (it.isEOF()) {
            return new Token(TokenType.EOF, "", it.currentPos(), it.currentPos());
        }

        char peek = it.peekChar();
        // 如果第一个单词为数字，就去查看是否为一个无符号整数或者是浮点数常量
        if (Character.isDigit(peek)) {
            return lexDigit();
        }
        // 如果第一个单词为字母，就去查看是否为标识符
        else if (Character.isAlphabetic(peek) || peek == '_') {
            return lexIdentOrKeyword();
        }
        // 字符串常量
        else if (peek == '"'){
            return lexString();
        }
        // 字符常量
        else if (peek == '\''){
            return lexChar();
        }
        // 运算符 or 注释
        else {
            return lexOperatorOrUnknown();
        }
    }

    // 判断读到token的是不是一个无符号整数或者浮点数常量
    private Token lexDigit() throws TokenizeError {
        // 直到查看下一个字符不是数字为止
        // 定义区分，0为无符号整数，1为浮点数常量
        int distinguish = 0;
        // 清空字符串
        str_token = "";
        //记录初始位置
        Pos p1 = it.ptr;
        // 前进一个字符，并存储这个字符
        char peek = it.nextChar();
        // 将字符串和字符连接起来
        str_token += peek;
        // 查看下一个字符 但是不移动指针
        peek = it.peekChar();
        while(Character.isDigit(peek)){
            // 前进一个字符，并存储这个字符
            peek = it.nextChar();
            // 将字符串和字符连接起来
            str_token += peek;
            // 查看下一个字符 但是不移动指针
            peek = it.peekChar();
        }
        if(peek == '.'){ //只能有一个小数点
            it.nextChar();
            // 将字符串和字符连接起来
            str_token += peek;
            // 查看下一个字符 但是不移动指针
            peek = it.peekChar();
            while(Character.isDigit(peek)){
                // 前进一个字符，并存储这个字符
                peek = it.nextChar();
                // 将字符串和字符连接起来
                str_token += peek;
                // 查看下一个字符 但是不移动指针
                peek = it.peekChar();
            }
            distinguish = 1;
        }
        try{
            // 解析存储的字符串为无符号整数
            if(distinguish == 0){
                // cxy
//                Integer num = Integer.parseInt(str_token);
                // 解析成功则返回无符号整数类型的token，否则返回编译错误
                return new Token(TokenType.UINT_LITERAL, str_token, p1,it.ptr);
            }
            else {
                Double num = Double.parseDouble(str_token);
                // 解析成功则返回无符号整数类型的token，否则返回编译错误
                return new Token(TokenType.DOUBLE_LITERAL, str_token, p1,it.ptr);
            }

        }catch(Exception e){
            // Token 的 Value 应填写数字的值
            throw new TokenizeError(ErrorCode.ExpectedToken, p1);
        }
    }

    // 判断读到的是否为一个标识符
    private Token lexIdentOrKeyword() throws TokenizeError {
        // 直到查看下一个字符不是数字或字母为止
        // 清空字符串
        str_token = "";
        //记录初始位置
        Pos p1 = it.ptr;
        // 前进一个字符，并存储这个字符
        char peek = it.nextChar();
        // 将字符串和字符连接起来
        str_token += peek;
        // 查看下一个字符 但是不移动指针
        peek = it.peekChar();
        while(Character.isDigit(peek) || Character.isAlphabetic(peek) || peek == '_'){
            // 前进一个字符，并存储这个字符
            peek = it.nextChar();
            // 将字符串和字符连接起来
            str_token += peek;
            // 查看下一个字符 但是不移动指针
            peek = it.peekChar();
        }
        try{
            // 尝试将存储的字符串解释为关键字
            // 如果是关键字，则返回关键字类型的token
            if(isKeepWord(str_token) != null)
                return new Token(isKeepWord(str_token), isKeepWord(str_token).toString(), p1, it.ptr);
                // 否则，返回标识符
            else return new Token(TokenType.IDENT, str_token, p1, it.ptr);
        }catch(Exception e){
            // Token 的 Value 应填写标识符或关键字的字符串
            throw new TokenizeError(ErrorCode.ExpectedToken,p1);
        }
    }

    // 判断读到的是否为一个字符串常量
    private Token lexString() throws TokenizeError {
        // 直到查看下一个字符是"为止
        // 清空字符串
        str_token = "";
        //记录初始位置
        Pos p1 = it.ptr;
        // 前进一个字符，并存储这个字符
        it.nextChar();
        // 查看下一个字符 但是不移动指针
        char peek = it.peekChar();
        while(peek != '"'){
            // 前进一个字符，并存储这个字符
            peek = it.nextChar();
            if(peek == '\\'){
                peek = it.peekChar();
                if(peek == '\\' || peek == '"' || peek == '\''){
                    it.nextChar();
                    str_token += peek;
                    peek = it.peekChar();
                    continue;
                }
                else if(peek == 'r'){
                    it.nextChar();
                    str_token += "\r";
                    peek = it.peekChar();
                    continue;
                }
                else if(peek == 'n'){
                    it.nextChar();
                    str_token += "\n";
                    peek = it.peekChar();
                    continue;
                }
                else if(peek == 't'){
                    it.nextChar();
                    str_token += "\t";
                    peek = it.peekChar();
                    continue;
                }
                else
                    throw new TokenizeError(ErrorCode.ExpectedToken,p1);
            }
            // 将字符串和字符连接起来
            str_token += peek;
            // 查看下一个字符 但是不移动指针
            peek = it.peekChar();
        }
        it.nextChar();
        peek = it.peekChar();
        try{
            // 尝试将存储的字符串解释为关键字
            // 如果是关键字，则返回关键字类型的token
            if(isKeepWord(str_token) != null)
                return new Token(isKeepWord(str_token), isKeepWord(str_token).toString(), p1, it.ptr);
                // 否则，返回标识符
            else return new Token(TokenType.STRING_LITERAL, str_token, p1, it.ptr);
        }catch(Exception e){
            // Token 的 Value 应填写标识符或关键字的字符串
            throw new TokenizeError(ErrorCode.ExpectedToken,p1);
        }
    }

    // 判断是否为一个字符常量
    private Token lexChar() throws TokenizeError {
        // 直到查看下一个字符是\'为止
        // 记录字符的长度
        // 记录字符
        String str_token = "";
        //记录初始位置
        Pos p1 = it.ptr;
        // 前进一个字符，并存储这个字符
        it.nextChar();
        // 查看下一个字符 但是不移动指针
        char peek = it.peekChar();
        while(peek != '\''){
            // 前进一个字符，并存储这个字符
            peek = it.nextChar();
            if(peek == '\\'){
                peek = it.peekChar();
                if(peek == '\\'){
                    it.nextChar();
                    str_token += "\\";
                    continue;
                }
                if(peek == 'r'){
                    it.nextChar();
                    str_token += "\r";
                    continue;
                }
                if(peek == 'n'){
                    it.nextChar();
                    str_token += "\n";
                    continue;
                }
                if (peek == 't'){
                    it.nextChar();
                    str_token += "\t";
                    continue;
                }
                if(peek == '"'){
                    it.nextChar();
                    str_token += "\"";
                    continue;
                }
                if (peek == '\''){
                    it.nextChar();
                    str_token += "\'";
                    continue;
                }
                else
                    throw new TokenizeError(ErrorCode.ExpectedToken,p1);

            }
            // 将字符串和字符连接起来
            str_token += peek;
            // 查看下一个字符 但是不移动指针
            peek = it.peekChar();
        }
        it.nextChar();
        peek = it.peekChar();
        try{
            // 查看是否是合法的字符
            if(str_token.length() > 2 || (str_token.length() == 2 && str_token.charAt(0) != '\\'))
                throw new TokenizeError(ErrorCode.ExpectedToken,p1);
            if(str_token.length() == 2 && str_token.charAt(0) == '\\'){
                if(str_token.charAt(1) == '\'' || str_token.charAt(1) == '\"' || str_token.charAt(1) == '\\')
                    str_token = str_token.charAt(1) + "";
                else if(str_token.charAt(1) != 'n' && str_token.charAt(1) != 't' && str_token.charAt(1) != 'r')
                    throw new TokenizeError(ErrorCode.ExpectedToken,p1);
            }
            // 尝试将存储的字符串解释为关键字
            // 如果是关键字，则返回关键字类型的token
            if(isKeepWord(str_token) != null)
                return new Token(isKeepWord(str_token), isKeepWord(str_token).toString(), p1, it.ptr);
                // 否则，返回标识符
            else return new Token(TokenType.CHAR_LITERAL, str_token, p1, it.ptr);
        }catch(Exception e){
            // Token 的 Value 应填写标识符或关键字的字符串
            throw new TokenizeError(ErrorCode.ExpectedToken,p1);
        }
    }

    // 判断是否为运算符
    private Token lexOperatorOrUnknown() throws TokenizeError {
        switch (it.nextChar()) {
            case '+':
                try{
                    return new Token(TokenType.PLUS, "+", it.previousPos(), it.currentPos());
                }catch (Exception e){
                    throw new TokenizeError(ErrorCode.ExpectedToken,it.previousPos());
                }

            case '-':
                try{
                    if(it.peekChar() == '>') {
                        it.nextChar();
                        return new Token(TokenType.ARROW, "->", it.previousPos(), it.currentPos());
                    }
                    return new Token(TokenType.MINUS, "-", it.previousPos(), it.currentPos());
                }catch (Exception e){
                    throw new TokenizeError(ErrorCode.ExpectedToken,it.previousPos());
                }

            case '*':
                try{
                    return new Token(TokenType.MUL, "*", it.previousPos(), it.currentPos());
                }catch (Exception e){
                    throw new TokenizeError(ErrorCode.ExpectedToken,it.previousPos());
                }

            case '/':
                try{
                    // 判断是否为注释
                    if(it.peekChar() == '/'){
                        while(it.nextChar() != '\n') ;
                        return nextToken();
                    }
                    return new Token(TokenType.DIV, "/", it.previousPos(), it.currentPos());
                }catch (Exception e){
                    throw new TokenizeError(ErrorCode.ExpectedToken,it.previousPos());
                }
            case '=':
                try{
                    if(it.peekChar() == '=') {
                        it.nextChar();
                        return new Token(TokenType.EQ, "==", it.previousPos(), it.currentPos());
                    }
                    return new Token(TokenType.ASSIGN, "=", it.previousPos(), it.currentPos());
                }catch (Exception e){
                    throw new TokenizeError(ErrorCode.ExpectedToken,it.previousPos());
                }
            case '!':
                try{
                    if(it.peekChar() == '=') {
                        it.nextChar();
                        return new Token(TokenType.EQ, "!=", it.previousPos(), it.currentPos());
                    }
                }catch (Exception e){
                    throw new TokenizeError(ErrorCode.ExpectedToken,it.previousPos());
                }
            case '<':
                try{
                    if(it.peekChar() == '='){
                        it.nextChar();
                        return new Token(TokenType.LE , "<=", it.previousPos(), it.currentPos());
                    }
                    return new Token(TokenType.LT, "<", it.previousPos(), it.currentPos());
                }catch (Exception e){
                    throw new TokenizeError(ErrorCode.ExpectedToken,it.previousPos());
                }
            case '>':
                try{
                    if(it.peekChar() == '='){
                        it.nextChar();
                        return new Token(TokenType.GE , ">=", it.previousPos(), it.currentPos());
                    }
                    return new Token(TokenType.GT, ">", it.previousPos(), it.currentPos());
                }catch (Exception e){
                    throw new TokenizeError(ErrorCode.ExpectedToken,it.previousPos());
                }
            case '(':
                try{
                    return new Token(TokenType.L_PAREN, "(", it.previousPos(), it.currentPos());
                }catch (Exception e){
                    throw new TokenizeError(ErrorCode.ExpectedToken,it.previousPos());
                }
            case ')':
                try{
                    return new Token(TokenType.R_PAREN, ")", it.previousPos(), it.currentPos());
                }catch (Exception e){
                    throw new TokenizeError(ErrorCode.ExpectedToken,it.previousPos());
                }
            case '{':
                try{
                    return new Token(TokenType.L_BRACE, "{", it.previousPos(), it.currentPos());
                }catch (Exception e){
                    throw new TokenizeError(ErrorCode.ExpectedToken,it.previousPos());
                }
            case '}':
                try{
                    return new Token(TokenType.R_BRACE, "}", it.previousPos(), it.currentPos());
                }catch (Exception e){
                    throw new TokenizeError(ErrorCode.ExpectedToken,it.previousPos());
                }
            case ',':
                try{
                    return new Token(TokenType.COMMA, ",", it.previousPos(), it.currentPos());
                }catch (Exception e){
                    throw new TokenizeError(ErrorCode.ExpectedToken,it.previousPos());
                }
            case ':':
                try{
                    return new Token(TokenType.COLON, ":", it.previousPos(), it.currentPos());
                }catch (Exception e){
                    throw new TokenizeError(ErrorCode.ExpectedToken,it.previousPos());
                }
            case ';':
                try{
                    return new Token(TokenType.SEMICOLON, ";", it.previousPos(), it.currentPos());
                }catch (Exception e){
                    throw new TokenizeError(ErrorCode.ExpectedToken,it.previousPos());
                }
            default:
                // 不认识这个输入，摸了
                throw new TokenizeError(ErrorCode.InvalidInput, it.previousPos());
        }
    }
    // 跳过空白字符
    private void skipSpaceCharacters() {
        while (!it.isEOF() && Character.isWhitespace(it.peekChar())) {
            it.nextChar();
        }
    }
    // 判断是否为保留字
    private TokenType isKeepWord(String str){
        if(str.toLowerCase().equals(TokenType.FN_KW.toString().toLowerCase()))
            return TokenType.FN_KW;
        else if(str.toLowerCase().equals(TokenType.LET_KW.toString().toLowerCase()))
            return TokenType.LET_KW;
        else if(str.toLowerCase().equals(TokenType.CONST_KW.toString().toLowerCase()))
            return TokenType.CONST_KW;
        else if(str.toLowerCase().equals(TokenType.AS_KW.toString().toLowerCase()))
            return TokenType.AS_KW;
        else if(str.toLowerCase().equals(TokenType.WHILE_KW.toString().toLowerCase()))
            return TokenType.WHILE_KW;
        else if(str.toLowerCase().equals(TokenType.IF_KW.toString().toLowerCase()))
            return TokenType.IF_KW;
        else if(str.toLowerCase().equals(TokenType.ELSE_KW.toString().toLowerCase()))
            return TokenType.ELSE_KW;
        else if(str.toLowerCase().equals(TokenType.RETURN_KW.toString().toLowerCase()))
            return TokenType.RETURN_KW;
        else if(str.toLowerCase().equals(TokenType.BREAK_KW.toString().toLowerCase()))
            return TokenType.BREAK_KW;
        else if(str.toLowerCase().equals(TokenType.CONTINUE_KW.toString().toLowerCase()))
            return TokenType.CONTINUE_KW;
        else return null;
    };
}
