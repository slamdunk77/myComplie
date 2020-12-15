import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;


public class Test {
    private static ArrayList<Token> tokenList;

    public static ArrayList<Token> getTokenList() {
        return tokenList;
    }

    public static void main(String[] args) throws IOException, TokenizeError {
        try{
            File fin=new File(args[0]);        //转入的文件对象
            BufferedReader in = new BufferedReader(new FileReader(fin));  //打开输入流
            String s;
            while((s = in.readLine()) != null){//读字符串
                System.out.println(s);          //写出
            }
            in.close(); //关闭缓冲读入流及文件读入流的连接
        }catch (FileNotFoundException e1){           //异常处理
            e1.printStackTrace();
        }catch(IOException e2){
            e2.printStackTrace();
        }
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
            System.out.println(AnalyserTable.getGlobalDefList().size());
            for (GlobalDef global : AnalyserTable.getGlobalDefList()) {
                System.out.println(global);
            }
            System.out.println("-----------------------------function");
            System.out.println(Analyser.getStartFunction());
            for (FunctionDef functionDef : AnalyserTable.getFunctionDefList()) {
                System.out.println(functionDef);
            }
            for(int i=0;i<AnalyserTable.getInstructionList().size();i++){
                System.out.println(AnalyserTable.getInstructionList().get(i).getInstr());
                System.out.println(AnalyserTable.getInstructionList().get(i).getInstrId());

            }

            Binary binary = new Binary(AnalyserTable.getGlobalDefList(), Analyser.getStartFunction(), AnalyserTable.getFunctionDefList());
            DataOutputStream out = new DataOutputStream(new FileOutputStream(new File(args[1])));
            ArrayList<Byte> bytes = binary.generate();
            byte[] resultBytes = new byte[bytes.size()];
            for (int i = 0; i < bytes.size(); ++i) {
                resultBytes[i] = bytes.get(i);
            }
            out.write(resultBytes);
        }catch (Exception e) {
            System.out.println("hhh");
            e.printStackTrace();
            System.exit(-1);
        }
    }
}
