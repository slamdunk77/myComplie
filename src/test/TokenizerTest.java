package test;

import error.TokenizeError;
import tokenizer.StringIter;
import tokenizer.Tokenizer;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Scanner;


public class TokenizerTest {
    public static void main(String[] args) throws FileNotFoundException, TokenizeError {
        try{
            Scanner sc = new Scanner(new File(args[0]));
            StringIter it = new StringIter(sc);
            Tokenizer token = new Tokenizer(it);
            while (true) {
                System.out.println(token.nextToken());
//                token.nextToken();
                if(it.isEOF())
                    break;
            }
        }catch (FileNotFoundException e){
//            System.out.println("没有找到文件");

        }catch (TokenizeError e){
//            System.out.println("字符识别出错");
        }
    }
}
