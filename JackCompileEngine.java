import java.util.*;

import org.w3c.dom.*;

class JackCompileEngine {
    public static final JackCompileEngine instance = new JackCompileEngine();

    private int i;
    private NodeList tokens;
    private SymbolTable classLevelSymbolTable;
    private SymbolTable methodLevelSymbolTable;
    private String currentClassName;
    private VMCodeGenerator codeGenerator;

    private JackCompileEngine() {
        codeGenerator = VMCodeGenerator.instance;
    }

    public String compile(NodeList tokens) throws Exception {
        this.tokens = tokens;
        this.i = 1;
        try {
            return compileClass();
        } catch (Exception e) {
            throw e;
        }
    }

    private void nextToken() {
        i++;
        Node curNode = tokens.item(i);
        while (curNode.getNodeType() != Node.ELEMENT_NODE) {
            i++;
            if (i == tokens.getLength()) break;
            curNode = tokens.item(i);
        }
    }

    private String compileClass() throws Exception {
        codeGenerator.initialize();
        Node curNode = tokens.item(i);
        String s = "";
        if (curNode.getNodeName().equals("keyword") && curNode.getTextContent().trim().equals("class")) {
            try {
                s = s + "<class>\n";
                classLevelSymbolTable = new SymbolTable();
                s = s + compileKeyWord();
                curNode = tokens.item(i);
                currentClassName = curNode.getTextContent().trim();
                s = s + compileIdentifier();
                curNode = tokens.item(i);
                if (curNode.getNodeName().equals("symbol") && curNode.getTextContent().trim().equals("{")) {
                    s = s + compileSymbol();
                }
                curNode = tokens.item(i);
                while (curNode.getNodeName().equals("keyword") && (curNode.getTextContent().trim().equals("static") || curNode.getTextContent().trim().equals("field"))) {
                    s = s + compileClassVarDec();
                    curNode = tokens.item(i);
                }
                String value = curNode.getTextContent().trim();
                while (curNode.getNodeName().equals("keyword") && (value.equals("constructor") || value.equals("function") || value.equals("method"))) {
                    s = s + compileSubroutineDec();
                    curNode = tokens.item(i);
                    value = curNode.getTextContent().trim();
                }
                if (curNode.getNodeName().equals("symbol") && curNode.getTextContent().trim().equals("}")) {
                    s = s + compileSymbol();
                }
                s = s + "</class>\n";
                System.out.println(codeGenerator.getCode());
            } catch (Exception e) {
                throw e;
            }
        } else {
            throw new Exception("Class token not found: " + curNode.getNodeName());
        }
        return s;
    }

    private String compileSymbol() throws Exception {
        Node curNode = tokens.item(i);
        String s = "";
        if (curNode.getNodeName().equals("symbol")) {
            String value = curNode.getTextContent().trim();
            if (value.equals("<")) {
                value = "&lt;";
            }
            if (value.equals(">")) {
                value = "&gt;";
            }
            if (value.equals("&")) {
                value = "&amp;";
            }
            s = s + "<symbol>" + value + "</symbol>\n";
            nextToken();
        } else {
            throw new Exception("The token is not of type symbol: " + curNode.getNodeName() + " -> " + curNode.getTextContent());
        }
        return s;
    }

    private String compileKeyWord() throws Exception {
        Node curNode = tokens.item(i);
        String s = "";
        if (curNode.getNodeName().equals("keyword")) {
            s = s + "<keyword>" + curNode.getTextContent() + "</keyword>\n";
            nextToken();
        } else {
            throw new Exception("The token is not of type keyword: " + curNode.getNodeName() + " -> " + curNode.getTextContent());
        }

        return s;
    }

    private String compileIdentifier() throws Exception {
        Node curNode = tokens.item(i);
        String s = "";
        if (curNode.getNodeName().equals("identifier")) {
            s = s + "<identifier>" + curNode.getTextContent() + "</identifier>\n";
            nextToken();
        } else {
            throw new Exception("The token is not of type identifier: " + curNode.getNodeName() + " -> " + curNode.getTextContent());
        }
        return s;
    }

    private String compileClassVarDec() throws Exception {
        Node curNode = tokens.item(i);
        String s = "<classVarDec>\n";
        try {
            String kind = curNode.getTextContent().trim();
            s = s + compileKeyWord();
            curNode = tokens.item(i);
            String type = curNode.getTextContent().trim();
            s = s + compileType();
            ArrayList<String> varNames = new ArrayList<>();
            curNode = tokens.item(i);
            varNames.add(curNode.getTextContent().trim());
            s = s + compileIdentifier();
            curNode = tokens.item(i);
            while (curNode.getNodeName().equals("symbol") && curNode.getTextContent().trim().equals(",")) {
                s = s + compileSymbol();
                curNode = tokens.item(i);
                varNames.add(curNode.getTextContent().trim());
                s = s + compileIdentifier();
                curNode = tokens.item(i);
            }
            if (curNode.getNodeName().equals("symbol") && curNode.getTextContent().trim().equals(";")) {
                s = s + compileSymbol();
            } else {
                throw new Exception("Expected ; but found " + curNode.getTextContent().trim());
            }
            for (int j = 0; j < varNames.size(); j++) {
                classLevelSymbolTable.add(varNames.get(j), type, kind);
            }
        } catch (Exception e) {
            throw e;
        }
        s = s + "</classVarDec>\n";
        return s;
    }

    private String compileSubroutineDec() throws Exception {
        String s = "<subroutineDec>\n";
        Node curNode = tokens.item(i);
        methodLevelSymbolTable = new SymbolTable();
        try {
            String functionType = curNode.getTextContent().trim();
            s = s + compileKeyWord();
            if (functionType.equals("method")) {
                methodLevelSymbolTable.add("this", currentClassName, "argument");
            }
            String returnType = "";
            curNode = tokens.item(i);
            if (curNode.getNodeName().equals("keyword") && curNode.getTextContent().trim().equals("void")) {
                returnType = "void";
                s = s + compileKeyWord();
            } else {
                s = s + compileType();
            }
            curNode = tokens.item(i);
            String functionName = curNode.getTextContent().trim();
            s = s + compileIdentifier();
            curNode = tokens.item(i);
            if (curNode.getNodeName().equals("symbol") && curNode.getTextContent().trim().equals("(")) {
                s = s + compileSymbol();
            } else {
                throw new Exception("Expected ( but found " + curNode.getTextContent());
            }
            s = s + compileParameterList();
            curNode = tokens.item(i);
            if (curNode.getNodeName().equals("symbol") && curNode.getTextContent().trim().equals(")")) {
                s = s + compileSymbol();
            } else {
                throw new Exception("Expected ) but found " + curNode.getTextContent());
            }

            s = s + compileSubroutineBody(functionName, functionType);
            if (functionType.equals("constructor")) {
                codeGenerator.handlePush("pointer", 0);
            }
            if (returnType.equals("void")) {
                codeGenerator.handlePush("constant", 0);
            }
            codeGenerator.addCodeLine("return\n");
        } catch (Exception e) {
            throw e;
        }
        s = s + "</subroutineDec>\n";
        return s;
    }

    private String compileType() throws Exception {
        String s = "";
        Node curNode = tokens.item(i);
        String value = curNode.getTextContent().trim();
        try {
            if (curNode.getNodeName().equals("identifier")) {
                s = s + compileIdentifier();
            } else if (curNode.getNodeName().equals("keyword")) {
                if (value.equals("int") || value.equals("char") || value.equals("boolean")) {
                    s = s + compileKeyWord();
                } else {
                    throw new Exception("Expected type but found : " + value);
                }
            } else {
                throw new Exception("Expected type but found : " + value);
            }
        } catch (Exception e) {
            throw e;
        }
        return s;
    }

    private String compileParameterList() throws Exception {
        String s = "<parameterList>\n";
        Node curNode = tokens.item(i);
        try {
            if (curNode.getNodeName().equals("symbol") && curNode.getTextContent().trim().equals(")")) {
                s = s + "</parameterList>\n";
                return s;
            } else {
                String type = curNode.getTextContent().trim();
                s = s + compileType();
                curNode = tokens.item(i);
                String name = curNode.getTextContent().trim();
                s = s + compileIdentifier();
                methodLevelSymbolTable.add(name, type, "argument");
            }
            curNode = tokens.item(i);
            while (curNode.getNodeName().equals("symbol") && curNode.getTextContent().trim().equals(",")) {
                s = s + compileSymbol();
                curNode = tokens.item(i);
                String type = curNode.getTextContent().trim();
                s = s + compileType();
                curNode = tokens.item(i);
                String name = curNode.getTextContent().trim();
                s = s + compileIdentifier();
                methodLevelSymbolTable.add(name, type, "argument");
                curNode = tokens.item(i);
            }
        } catch (Exception e) {
            throw e;
        }
        s = s + "</parameterList>\n";
        return s;
    }

    private String compileSubroutineBody(String functionName, String functionType) throws Exception {

        String s = "<subroutineBody>\n";
        Node curNode = tokens.item(i);
        try {
            if (curNode.getNodeName().equals("symbol") && curNode.getTextContent().trim().equals("{")) {
                s = s + compileSymbol();
            } else {
                throw new Exception("Expected { found " + curNode.getTextContent());
            }
            curNode = tokens.item(i);
            while (curNode.getNodeName().equals("keyword") && curNode.getTextContent().trim().equals("var")) {
                s = s + compileVarDec();
                curNode = tokens.item(i);
            }
            codeGenerator.addCodeLine("function " + currentClassName + "." + functionName + " " + methodLevelSymbolTable.getLocalCount() + "\n");
            if (functionType.equals("constructor")) {
                int memoryNeeded = classLevelSymbolTable.getFieldCount();
                codeGenerator.handlePush("constant", memoryNeeded);
                codeGenerator.addCodeLine("call Memory.alloc 1\n");
                codeGenerator.handlePop("pointer", 0);
            } else if (functionType.equals("method")) {
                codeGenerator.handlePush("argument", 0);
                codeGenerator.handlePop("pointer", 0);
            }
            s = s + compileStatements();
//            System.out.println(s);
            curNode = tokens.item(i);
            if (curNode.getNodeName().equals("symbol") && curNode.getTextContent().trim().equals("}")) {
                s = s + compileSymbol();
            } else {
                throw new Exception("Expected } found " + curNode.getTextContent());
            }
        } catch (Exception e) {
            throw e;
        }
        s = s + "</subroutineBody>\n";
        return s;
    }

    private String compileVarDec() throws Exception {
        String s = "<varDec>\n";
        Node curNode = tokens.item(i);
        try {
            s = s + compileKeyWord();
            curNode = tokens.item(i);
            String type = curNode.getTextContent().trim();
            s = s + compileType();
            ArrayList<String> varNames = new ArrayList<>();
            curNode = tokens.item(i);
            varNames.add(curNode.getTextContent().trim());
            s = s + compileIdentifier();
            curNode = tokens.item(i);
            while (curNode.getNodeName().equals("symbol") && curNode.getTextContent().trim().equals(",")) {
                s = s + compileSymbol();
                curNode = tokens.item(i);
                varNames.add(curNode.getTextContent().trim());
                s = s + compileIdentifier();
                curNode = tokens.item(i);
            }
            if (curNode.getNodeName().equals("symbol") && curNode.getTextContent().trim().equals(";")) {
                s = s + compileSymbol();
            } else {
                throw new Exception("Expected ; but found " + curNode.getTextContent());
            }
            for (int j = 0; j < varNames.size(); j++) {
                methodLevelSymbolTable.add(varNames.get(j), type, "local");
            }
        } catch (Exception e) {
            throw e;
        }
        s = s + "</varDec>\n";
        return s;
    }

    private String compileStatements() throws Exception {
        String s = "<statements>\n";
        Node curNode = tokens.item(i);
        try {
            while (curNode.getNodeName().equals("keyword")) {
                String value = curNode.getTextContent().trim();
                if (value.equals("let")) {
                    s = s + compileLetStatement();
                } else if (value.equals("if")) {
                    s = s + compileIfStatement();
                } else if (value.equals("while")) {
                    s = s + compileWhileStatement();
                } else if (value.equals("do")) {
                    s = s + compileDoStatement();
                } else if (value.equals("return")) {
                    s = s + compileReturnStatement();
                } else {
                    throw new Exception("Expected statement found " + value);
                }
                curNode = tokens.item(i);
            }
        } catch (Exception e) {
            throw e;
        }
        s = s + "</statements>\n";
        return s;
    }

    private SymbolDetails getVariableDetails(String name) {
        if (methodLevelSymbolTable.hasVariable(name)) {
            return methodLevelSymbolTable.get(name);
        } else {
            return classLevelSymbolTable.get(name);
        }
    }

    private String compileLetStatement() throws Exception {
        String s = "<letStatement>\n";
        boolean accessArray = false;
        Node curNode = tokens.item(i);
        try {
            s = s + compileKeyWord();
            curNode = tokens.item(i);
            SymbolDetails curVariable = getVariableDetails(curNode.getTextContent().trim());
            s = s + compileIdentifier();
            curNode = tokens.item(i);
            if (curNode.getNodeName().equals("symbol")) {
                if (curNode.getTextContent().trim().equals("[")) {
                    codeGenerator.handlePush(curVariable.getKind(), curVariable.getCount());
                    s = s + compileSymbol();
                    accessArray = true;
                    s = s + compileExpression();
                    curNode = tokens.item(i);
                    if (curNode.getNodeName().equals("symbol") && curNode.getTextContent().trim().equals("]")) {
                        s = s + compileSymbol();
                    } else {
                        throw new Exception("Expected ] but found " + curNode.getTextContent());
                    }
                    codeGenerator.handleExpression("+", "operation");
                    curNode = tokens.item(i);
                }
                if (curNode.getNodeName().equals("symbol") && curNode.getTextContent().trim().equals("=")) {
                    s = s + compileSymbol();
                    s = s + compileExpression();
                    if (accessArray) {
                        codeGenerator.handlePop("temp", 0);
                        codeGenerator.handlePop("pointer", 1);
                        codeGenerator.handlePush("temp", 0);
                        codeGenerator.handlePop("that", 0);
                    } else {
                        codeGenerator.handlePop(curVariable.getKind(), curVariable.getCount());
                    }
                } else {
                    throw new Exception("Expected = but found " + curNode.getTextContent());
                }
            } else {
                throw new Exception("Expected symbol but found " + curNode.getNodeName());
            }
            curNode = tokens.item(i);
            if (curNode.getNodeName().equals("symbol") && curNode.getTextContent().trim().equals(";")) {
                s = s + compileSymbol();
            } else {
                throw new Exception("Expected ; but found " + curNode.getTextContent());
            }
        } catch (Exception e) {
            throw e;
        }
        s = s + "</letStatement>\n";
        return s;
    }

    private String compileIfStatement() throws Exception {
        String s = "<ifStatement>\n";
        Node curNode = tokens.item(i);
        try {
            s = s + compileKeyWord();
            curNode = tokens.item(i);
            String currentLabel = currentClassName + "$ifLabel" + i;
            if (curNode.getNodeName().equals("symbol") && curNode.getTextContent().trim().equals("(")) {
                s = s + compileSymbol();
            } else {
                throw new Exception("Expected ( but found " + curNode.getTextContent());
            }
            s = s + compileExpression();
            curNode = tokens.item(i);
            if (curNode.getNodeName().equals("symbol") && curNode.getTextContent().trim().equals(")")) {
                s = s + compileSymbol();
            } else {
                throw new Exception("Expected ) but found " + curNode.getTextContent());
            }
            codeGenerator.handleExpression("~", "unaryOp");
            codeGenerator.addIfGoto(currentLabel + "$false");
            curNode = tokens.item(i);
            if (curNode.getNodeName().equals("symbol") && curNode.getTextContent().trim().equals("{")) {
                s = s + compileSymbol();
            } else {
                throw new Exception("Expected { but found " + curNode.getTextContent());
            }
            s = s + compileStatements();
            curNode = tokens.item(i);
            if (curNode.getNodeName().equals("symbol") && curNode.getTextContent().trim().equals("}")) {
                s = s + compileSymbol();
            } else {
                throw new Exception("Expected } but found " + curNode.getTextContent());
            }
            codeGenerator.addGoto(currentLabel + "$end");
            codeGenerator.addLabel(currentLabel + "$false");
            curNode = tokens.item(i);
            if (curNode.getNodeName().equals("keyword")) {
                if (curNode.getTextContent().trim().equals("else")) {
                    s = s + compileKeyWord();
                    curNode = tokens.item(i);
                    if (curNode.getNodeName().equals("symbol") && curNode.getTextContent().trim().equals("{")) {
                        s = s + compileSymbol();
                    } else {
                        throw new Exception("Expected { but found " + curNode.getTextContent());
                    }
                    s = s + compileStatements();
                    curNode = tokens.item(i);
                    if (curNode.getNodeName().equals("symbol") && curNode.getTextContent().trim().equals("}")) {
                        s = s + compileSymbol();
                    } else {
                        throw new Exception("Expected } but found " + curNode.getTextContent());
                    }
                }
            }
            codeGenerator.addLabel(currentLabel + "$end");
        } catch (Exception e) {
            throw e;
        }
        s = s + "</ifStatement>\n";
        return s;
    }

    private String compileWhileStatement() throws Exception {
        String s = "<whileStatement>\n";
        Node curNode = tokens.item(i);
        try {
            s = s + compileKeyWord();
            curNode = tokens.item(i);
            if (curNode.getNodeName().equals("symbol") && curNode.getTextContent().trim().equals("(")) {
                s = s + compileSymbol();
            } else {
                throw new Exception("Expected ( but found " + curNode.getTextContent());
            }
            String currentLabel = currentClassName + "$whileLabel" + i;
            codeGenerator.addLabel(currentLabel + "$start");
            s = s + compileExpression();
            curNode = tokens.item(i);
            if (curNode.getNodeName().equals("symbol") && curNode.getTextContent().trim().equals(")")) {
                s = s + compileSymbol();
            } else {
                throw new Exception("Expected ) but found " + curNode.getTextContent());
            }
            codeGenerator.handleExpression("~", "unaryOp");
            codeGenerator.addIfGoto(currentLabel + "$end");
            curNode = tokens.item(i);
            if (curNode.getNodeName().equals("symbol") && curNode.getTextContent().trim().equals("{")) {
                s = s + compileSymbol();
            } else {
                throw new Exception("Expected { but found " + curNode.getTextContent());
            }
            s = s + compileStatements();
            curNode = tokens.item(i);
            if (curNode.getNodeName().equals("symbol") && curNode.getTextContent().trim().equals("}")) {
                s = s + compileSymbol();
            } else {
                throw new Exception("Expected } but found " + curNode.getTextContent());
            }
            codeGenerator.addGoto(currentLabel + "$start");
            codeGenerator.addLabel(currentLabel + "$end");
        } catch (Exception e) {
            throw e;
        }
        s = s + "</whileStatement>\n";
        return s;
    }

    private String compileDoStatement() throws Exception {
        String s = "<doStatement>\n";
        Node curNode;
        try {
            s = s + compileKeyWord();
            s = s + compileSubroutineCall();
            curNode = tokens.item(i);
            if (curNode.getNodeName().equals("symbol") && curNode.getTextContent().trim().equals(";")) {
                s = s + compileSymbol();
            } else {
                throw new Exception("Expected ; but found " + curNode.getTextContent());
            }
            codeGenerator.handlePop("temp", 0);
        } catch (Exception e) {
            throw e;
        }
        s = s + "</doStatement>\n";
        return s;
    }

    private String compileReturnStatement() throws Exception {
        String s = "<returnStatement>\n";
        Node curNode;
        try {
            s = s + compileKeyWord();
            curNode = tokens.item(i);
            if (curNode.getNodeName().equals("symbol") && curNode.getTextContent().trim().equals(";")) {
                s = s + compileSymbol();
                codeGenerator.handlePush("constant",0);
            } else {
                s = s + compileExpression();
                curNode = tokens.item(i);
                if (curNode.getNodeName().equals("symbol") && curNode.getTextContent().trim().equals(";")) {
                    s = s + compileSymbol();
                } else {
                    throw new Exception("Expected ; but found " + curNode.getTextContent());
                }

            }
            codeGenerator.addCodeLine("return\n");
        } catch (Exception e) {
            throw e;
        }
        s = s + "</returnStatement>\n";
        return s;
    }

    private String compileExpression() throws Exception {
        String s = "<expression>\n";
        Node curNode = tokens.item(i);
        try {
            s = s + compileTerm();
            curNode = tokens.item(i);
            String value = curNode.getTextContent().trim();

            while (value.equals("+") || value.equals("-") || value.equals("*") || value.equals("/") || value.equals("&") || value.equals("|") || value.equals("<") || value.equals(">") || value.equals("=")) {
                s = s + compileSymbol();
                s = s + compileTerm();
                codeGenerator.handleExpression(value, "operation");
                curNode = tokens.item(i);
                value = curNode.getTextContent().trim();
            }
        } catch (Exception e) {
            throw e;
        }
        s = s + "</expression>\n";
        return s;
    }

    private String compileTerm() throws Exception {
        String s = "<term>\n";
        Node curNode = tokens.item(i);
        try {
            if (curNode.getNodeName().equals("integerConstant")) {
                String integer = curNode.getTextContent().trim();
                s = s + compileIntegerConstant();
                codeGenerator.handleExpression(integer, "integerConstant");
            } else if (curNode.getNodeName().equals("stringConstant")) {
                String value = curNode.getTextContent();
                s = s + compileStringConstant();
                codeGenerator.handleExpression(value, "stringConstant");
            } else if (curNode.getNodeName().equals("keyword")) {
                String value = curNode.getTextContent().trim();
                if (value.equals("true") || value.equals("false") || value.equals("null") || value.equals("this")) {
                    codeGenerator.handleExpression(value, "keywordConstant");
                    s = s + compileKeyWord();
                } else {
                    throw new Exception("Invalid term in expression: " + value);
                }
            } else if (curNode.getNodeName().equals("symbol") && curNode.getTextContent().trim().equals("(")) {
                s = s + compileSymbol();
                s = s + compileExpression();
                curNode = tokens.item(i);
                if (curNode.getNodeName().equals("symbol") && curNode.getTextContent().trim().equals(")")) {
                    s = s + compileSymbol();
                } else {
                    throw new Exception("Expected ( but found " + curNode.getTextContent());
                }
            } else if (curNode.getNodeName().equals("symbol") && (curNode.getTextContent().trim().equals("-") || curNode.getTextContent().trim().equals("~"))) {
                s = s + compileSymbol();
                s = s + compileTerm();
                codeGenerator.handleExpression(curNode.getTextContent().trim(), "unaryOp");
            } else if (curNode.getNodeName().equals("identifier")) {
                int j = i + 1;
                Node nextNode = tokens.item(j);
                while (nextNode.getNodeType() != Node.ELEMENT_NODE) {
                    j++;
                    nextNode = tokens.item(j);
                }
                if (nextNode.getNodeName().equals("symbol") && (nextNode.getTextContent().trim().equals(".") || nextNode.getTextContent().trim().equals("("))) {
                    s = s + compileSubroutineCall();
                } else {
                    SymbolDetails variable = methodLevelSymbolTable.hasVariable(curNode.getTextContent().trim()) ? methodLevelSymbolTable.get(curNode.getTextContent().trim()) : classLevelSymbolTable.get(curNode.getTextContent().trim());
                    s = s + compileIdentifier();
                    codeGenerator.handleExpression(String.valueOf(variable.getCount()), variable.getKind());
                    if (nextNode.getNodeName().equals("symbol") && nextNode.getTextContent().trim().equals("[")) {
                        s = s + compileSymbol();
                        s = s + compileExpression();
                        curNode = tokens.item(i);
                        if (curNode.getNodeName().equals("symbol") && curNode.getTextContent().trim().equals("]")) {
                            s = s + compileSymbol();
                        } else {
                            throw new Exception("Expected ] but found " + curNode.getTextContent());
                        }
                        codeGenerator.handleExpression("+", "operation");
                        codeGenerator.addCodeLine("pop pointer 1\n");
                        codeGenerator.addCodeLine("push that 0\n");
                    }
                }
            } else {
                throw new Exception("Not a valid term: " + curNode.getTextContent());
            }
        } catch (Exception e) {
            throw e;
        }
        s = s + "</term>\n";
        return s;
    }

    private String compileIntegerConstant() throws Exception {
        String s = "";
        Node curNode = tokens.item(i);
        if (curNode.getNodeName().equals("integerConstant")) {
            s = s + "<integerConstant>" + curNode.getTextContent() + "</integerConstant>\n";
            nextToken();
        } else {
            throw new Exception("Expected integer but found " + curNode.getTextContent());
        }
        return s;
    }

    private String compileStringConstant() throws Exception {
        String s = "";
        Node curNode = tokens.item(i);
        if (curNode.getNodeName().equals("stringConstant")) {
            s = s + "<stringConstant>" + curNode.getTextContent() + "</stringConstant>\n";
            nextToken();
        } else {
            throw new Exception("Expected string but found " + curNode.getTextContent());
        }
        return s;
    }

    private String compileSubroutineCall() throws Exception {
        String s = "";
        Node curNode = tokens.item(i);
        try {
            String newClassName = curNode.getTextContent().trim();
            String functionName = "";
            int argument = 0;
            if (curNode.getNodeName().equals("identifier")) {
                s = s + compileIdentifier();
            } else {
                throw new Exception("Expected identifier found " + curNode.getNodeName());
            }
            curNode = tokens.item(i);
            if (curNode.getNodeName().equals("symbol") && curNode.getTextContent().trim().equals(".")) {
                s = s + compileSymbol();
                curNode = tokens.item(i);
                if (methodLevelSymbolTable.hasVariable(newClassName)||classLevelSymbolTable.hasVariable(newClassName)) {
                    argument++;
                    SymbolDetails currentObj = methodLevelSymbolTable.hasVariable(newClassName) ? methodLevelSymbolTable.get(newClassName) : classLevelSymbolTable.get(newClassName);
                    codeGenerator.handlePush(currentObj.getKind().equals("field") ? "this" : currentObj.getKind(), currentObj.getCount());
                    functionName = currentObj.getType() + "." + curNode.getTextContent().trim();
                } else {
//                    System.out.println("here");
                    functionName = newClassName + "." + curNode.getTextContent().trim();
                }
                s = s + compileIdentifier();
            }
            curNode = tokens.item(i);
            if (curNode.getNodeName().equals("symbol") && curNode.getTextContent().trim().equals("(")) {
                s = s + compileSymbol();
                s = s + compileExpressionList(functionName, argument);
            } else {
                throw new Exception("Expected ( but found " + curNode.getTextContent());
            }
            curNode = tokens.item(i);
            if (curNode.getNodeName().equals("symbol") && curNode.getTextContent().trim().equals(")")) {
                s = s + compileSymbol();
            } else {
                throw new Exception("Expected ) but found " + curNode.getTextContent());
            }
        } catch (Exception e) {
            throw e;
        }
        return s;
    }

    private String compileExpressionList(String functionName, int argument) throws Exception {
        String s = "<expressionList>\n";
        Node curNode = tokens.item(i);
        try {
            if (curNode.getNodeName().equals("symbol") && curNode.getTextContent().trim().equals(")")) {
                codeGenerator.addCodeLine("call " + functionName + " " + argument + "\n");
                s = s + "</expressionList>\n";
                return s;
            }
            s = s + compileExpression();
            argument++;
            curNode = tokens.item(i);
            while (curNode.getNodeName().equals("symbol") && curNode.getTextContent().trim().equals(",")) {
                s = s + compileSymbol();
                s = s + compileExpression();
                argument++;
                curNode = tokens.item(i);
            }
            codeGenerator.addCodeLine("call " + functionName + " " + argument + "\n");
        } catch (Exception e) {
            throw e;
        }
        s = s + "</expressionList>\n";
        return s;
    }
}
