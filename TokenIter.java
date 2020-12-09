
import java.util.Iterator;
import java.util.ArrayList;

public class TokenIter {

    private static ArrayList<Token> tokenList;
    private static Iterator<Token> token_iter;

    private static Token token;

    public static Iterator<Token> getToken_iter() {
        return token_iter;
    }
    public static ArrayList<Token> getTokenList(){
        return tokenList;
    }

    public static Token getToken() {
        return token;
    }

    public static void initTokenList(){
        tokenList = Test.getTokenList();
    }

    public static void initToken_iter(){
        token_iter = tokenList.iterator();
    }

    public static void initToken(){
        token = currentToken();
    }

    /**
     * 查看当前token
     */
    public static Token currentToken() {
        if(token_iter.hasNext()) {
            token = token_iter.next();
            return token;
        }
        return null;
    }

    /**
     * 获取当前字符的位置
     */
    public Iterator<Token> currentTokenPos() {
        return token_iter;
    }

    /**
     * 查看下一个字符，但不移动指针
     */
    public Token peekToken() {
        if(token_iter.hasNext()){
            Iterator<Token> token_iter1 = token_iter;
            token  = token_iter.next();
            // 恢复token_iter
            token_iter = token_iter1;
            return token;
        }
        return null;
    }

    public Boolean isTokenEOF() {
        return !token_iter.hasNext();
    }

}
