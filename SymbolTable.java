import java.util.HashMap;

class SymbolTable {
    private HashMap<String,SymbolDetails> table;
    private int fieldCount;
    private int staticCount;
    private int localCount;
    private int argumentCount;
    SymbolTable(){
        fieldCount = 0;
        staticCount = 0;
        localCount = 0;
        argumentCount = 0;
        table = new HashMap<String, SymbolDetails>();
    }

    public int getFieldCount() {
        return fieldCount;
    }

    public int getStaticCount() {
        return staticCount;
    }

    public int getLocalCount() {
        return localCount;
    }

    public int getArgumentCount() {
        return argumentCount;
    }

    public void add(String name, String type, String kind){
        if(kind.equals("field")){
            table.put(name,new SymbolDetails(name,type,kind,fieldCount));
            fieldCount++;
        }
        if(kind.equals("static")){
            table.put(name,new SymbolDetails(name,type,kind,staticCount));
            staticCount++;
        }
        if(kind.equals("local")){
            table.put(name,new SymbolDetails(name,type,kind,localCount));
            localCount++;
        }
        if(kind.equals("argument")){
            table.put(name,new SymbolDetails(name,type,kind,argumentCount));
            argumentCount++;
        }
    }

    public SymbolDetails get(String name){
        return table.get(name);
    }

    public boolean hasVariable(String name){
        return table.containsKey(name);
    }
    @Override
    public String toString() {
        return "SymbolTable{" +
                ", fieldCount=" + fieldCount +
                ", staticCount=" + staticCount +
                ", localCount=" + localCount +
                ", argumentCount=" + argumentCount +
                '}';
    }
}
