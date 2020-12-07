package test;

import analyser.Analyser;
import error.TokenizeError;
import tokenizer.StringIter;
import tokenizer.Token;
import tokenizer.Tokenizer;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Scanner;


public class Test {
    private static ArrayList<Token> tokenList;

    public static ArrayList<Token> getTokenList() {
        return tokenList;
    }

    public static void main(String[] args) throws FileNotFoundException, TokenizeError {
        try{
            Scanner sc = new Scanner(new File(args[0]));
            StringIter it = new StringIter(sc);
            Tokenizer token = new Tokenizer(it);
            tokenList = new ArrayList<>();
            while (true) {
//                System.out.println(token.nextToken());
//                token.nextToken();
                // 将识别的每一个token放入一个tokenlist中
                tokenList.add(token.nextToken());
                System.out.println(tokenList.get(tokenList.size()-1).getValue());
                if(it.isEOF())
                    break;
            }
            Analyser.analyseProgram();
        }catch (Exception e) {
            e.printStackTrace();
        }
    }
}
