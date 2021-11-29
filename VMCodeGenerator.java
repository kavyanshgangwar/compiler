class VMCodeGenerator {
    public static final VMCodeGenerator instance = new VMCodeGenerator();

    private String code = "";

    private VMCodeGenerator() {
    }

    public void initialize() {
        code = "";
    }

    public String getCode() {
        return code;
    }


    public void handleExpression(String expression,String type) {
        if (type.equals("integerConstant")) {
            code = code + "push constant " + expression + "\n";
            return;
        }
        if(type.equals("stringConstant")){
            code = code + "push constant "+expression.length()+"\n";
            code = code + "call String.new 1"+"\n";
            for(int i=0;i<expression.length();i++){
                code += "push constant "+((expression.charAt(i)==' ')?"32":(int)expression.charAt(i)) +"\n";
                code += "call String.appendChar 2\n";
            }
        }
        if(type.equals("keywordConstant")){
            if(expression.equals("true")){
                code = code + "push constant 1\n";
                code = code + "neg\n";
            }else if(expression.equals("false")){
                code = code + "push constant 0\n";
            }else if(expression.equals("null")){
                code = code + "push constant 0\n";
            }else{
                code = code + "push pointer 0\n";
            }
            return;
        }
        if(type.equals("unaryOp")){
            if(expression.equals("-")){
                code  =code + "neg\n";
            }else{
                code = code + "not\n";
            }
            return;
        }
        if(type.equals("local")){
            code = code + "push local "+expression+"\n";
        }
        if(type.equals("argument")){
            code = code + "push argument "+expression+"\n";
        }
        if(type.equals("static")){
            code = code + "push static "+expression+"\n";
        }
        if(type.equals("field")){
            code = code + "push argument 0\n";
            code = code + "pop pointer 0\n";
            code = code + "push this "+expression+"\n";
        }
        if(type.equals("operation")){
            switch (expression.charAt(0)){
                case '+':
                    code = code + "add\n";
                    break;
                case '-':
                    code = code + "sub\n";
                    break;
                case '*':
                    code = code + "call Math.multiply 2\n";
                    break;
                case '/':
                    code = code + "call Math.divide 2\n";
                    break;
                case '&':
                    code = code + "and\n";
                    break;
                case '|':
                    code = code + "or\n";
                    break;
                case '<':
                    code = code + "lt\n";
                    break;
                case '>':
                    code = code + "gt\n";
                    break;
                case '=':
                    code = code + "eq\n";
                    break;
            }
        }


    }

    public void handlePush(String type,int value){
        code = code + "push "+(type.equals("field")?"this":type)+" "+value+"\n";
    }

    public  void addLabel(String label){
        code += "label "+label+"\n";
    }

    public void addIfGoto(String label){
        code += "if-goto "+label+"\n";
    }

    public  void addGoto(String label){
        code += "goto "+label+"\n";
    }

    public void handlePop(String type,int offset){
        code += "pop "+(type.equals("field")?"this":type)+" "+offset+"\n";
    }

    public  void addCodeLine(String codeLine){
        code += codeLine;
    }
}
