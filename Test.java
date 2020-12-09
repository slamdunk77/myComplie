package test;

import analyser.Analyser;
import analyser.AnalyserTable;
import error.TokenizeError;
import tokenizer.StringIter;
import tokenizer.Token;
import tokenizer.Tokenizer;

import java.io.DataOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.List;
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
            Binary binary = new Binary(AnalyserTable.getGlobalDefList(), Analyser.getStartFunction(), AnalyserTable.getFunctionDefList());
            DataOutputStream out = new DataOutputStream(new FileOutputStream(new File(args[1])));
            ArrayList<Byte> bytes = binary.generate();
            byte[] resultBytes = new byte[bytes.size()];
            for (int i = 0; i < bytes.size(); ++i) {
                resultBytes[i] = bytes.get(i);
            }
            out.write(resultBytes);
        }catch (Exception e) {
            System.exit(-1);
        }
    }
}
