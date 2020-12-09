
import java.io.*;
import java.util.ArrayList;

public class Binary {
    private ArrayList<GlobalDef> globals;
    private FunctionDef start;
    private ArrayList<FunctionDef> functionDefs;
    private ArrayList<Byte> output;

    //DataOutputStream out = new DataOutputStream(new FileOutputStream(new File("src/out.txt")));

    int magic=0x72303b3e;
    int version=0x00000001;

    public Binary(ArrayList<GlobalDef> globals, FunctionDef start, ArrayList<FunctionDef> functionDefs) throws FileNotFoundException {
        this.globals = globals;
        this.start = start;
        this.functionDefs = functionDefs;
        output = new ArrayList<>();
    }

    public ArrayList<Byte> generate() throws IOException {
        //magic
        ArrayList<Byte> magic=int2bytes(4,this.magic);
        output.addAll(magic);
        //version
        ArrayList<Byte> version=int2bytes(4,this.version);
        output.addAll(version);

        //globals.count
        ArrayList<Byte> globalCount=int2bytes(4, globals.size());
        output.addAll(globalCount);

        //out.writeBytes(globalCount.toString());

        for(GlobalDef global : globals){
            //isConst
            ArrayList<Byte> isConst=int2bytes(1, global.getIsConst());
            output.addAll(isConst);

            //out.writeBytes(isConst.toString());

            // value count
            ArrayList<Byte> globalValueCount;
            //out.writeBytes(globalValueCount.toString());

            //value items
            ArrayList<Byte> globalValue;
            if (global.getValueItems() == null) {
                globalValueCount = int2bytes(4, 8);
                globalValue = long2bytes(8,0L);
            }
            else {
                globalValue = String2bytes(global.getValueItems());
                globalValueCount = int2bytes(4, globalValue.size());
            }


            output.addAll(globalValueCount);
            output.addAll(globalValue);
            //System.out.println(globalValue.toString());
            //System.out.println("globalValueCount="+globalValueCount);
            //System.out.println(global.getValueItems());
        }

        //functions.count
        ArrayList<Byte> functionsCount=int2bytes(4, functionDefs.size() + 1);
        output.addAll(functionsCount);
        //out.writeBytes(functionsCount.toString());

        generateFunction(start);

        for(FunctionDef functionDef : functionDefs){
            generateFunction(functionDef);
        }
        return output;
    }

    private void generateFunction(FunctionDef functionDef) throws IOException {
        //name
        ArrayList<Byte> name = int2bytes(4,functionDef.getFuncId());
        output.addAll(name);
        //out.writeBytes(name.toString());

        //retSlots
        ArrayList<Byte> retSlots = int2bytes(4,functionDef.getRetSlots());
        output.addAll(retSlots);
        //out.writeBytes(retSlots.toString());

        //paramsSlots;
        ArrayList<Byte> paramsSlots=int2bytes(4,functionDef.getParamSlots());
        output.addAll(paramsSlots);
        //out.writeBytes(paramsSlots.toString());

        //locSlots;
        ArrayList<Byte> locSlots=int2bytes(4,functionDef.getLocSlots());
        output.addAll(locSlots);
        //out.writeBytes(locSlots.toString());

        ArrayList<Instruction> instructions = functionDef.getBody();

        //bodyCount
        ArrayList<Byte> bodyCount=int2bytes(4, instructions.size());
        output.addAll(bodyCount);
        //out.writeBytes(bodyCount.toString());

        //instructions
        for(Instruction instruction : instructions){
            //type
            ArrayList<Byte> type = int2bytes(1, instruction.getInstr().getInstructionNum());
            output.addAll(type);
            //out.writeBytes(type.toString());

            if(instruction.getInstrId() != null){
                ArrayList<Byte>  x;
                if(instruction.getInstr().getInstructionNum() == 1)
                    x = long2bytes(8,instruction.getInstrId());
                else
                    x = int2bytes(4,instruction.getInstrId());
                output.addAll(x);
                //out.writeBytes(x.toString());
            }
        }
    }

    private ArrayList<Byte> Char2bytes(char value) {
        ArrayList<Byte>  AB=new ArrayList<>();
        AB.add((byte)(value&0xff));
        return AB;
    }

    private ArrayList<Byte> String2bytes(String valueString) {
        ArrayList<Byte>  AB=new ArrayList<>();
        for (int i=0;i<valueString.length();i++){
            char ch=valueString.charAt(i);
            AB.add((byte)(ch&0xff));
        }
        return AB;
    }

    private ArrayList<Byte> long2bytes(int length, long target) {
        ArrayList<Byte> bytes = new ArrayList<>();
        int start = 8 * (length-1);
        for(int i = 0 ; i < length; i++){
            bytes.add((byte) (( target >> ( start - i * 8 )) & 0xFF ));
        }
        return bytes;
    }

    private ArrayList<Byte> int2bytes(int length,int target){
        ArrayList<Byte> bytes = new ArrayList<>();
        int start = 8 * (length-1);
        for(int i = 0 ; i < length; i++){
            bytes.add((byte) (( target >> ( start - i * 8 )) & 0xFF ));
        }
        return bytes;
    }
}
