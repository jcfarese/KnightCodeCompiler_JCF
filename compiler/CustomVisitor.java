package compiler;

import java.io.FileOutputStream;
import java.io.IOException;

import java.util.HashMap;

import lexparse.KnightCodeBaseVisitor;
import lexparse.KnightCodeParser;

import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

/**
* CustomVisitor Class extends KnightCodeBaseVisitor to provde custom methods for each visit to compile kc into bytecode using a parse tree to generate bytecode for specific circumstances

* @author Jim Farese
* @version 1.0
* Assignment 5
* CS322 - Compiler Construction
* Spring 2024
**/
public class CustomVisitor extends KnightCodeBaseVisitor<Object> implements Opcodes {

    private ClassWriter cw;
    private MethodVisitor mv;
    private SymbolTable symbolTable;
    private int nextLocalInt;
    private String outputFile;

    /**
     * Constructor
     */
    public CustomVisitor() {
        symbolTable = new SymbolTable();
        nextLocalInt = 1;
        cw = new ClassWriter(ClassWriter.COMPUTE_FRAMES);
    }

    /**
     * Sets the output file name and initialize the bytecode generation
     * 
     * @param outputFile the name of the file where the compiled bytecode will be stored
     */
    public void setOutputFile(String outputFile) {
        this.outputFile = outputFile;  

        cw.visit(V1_8, ACC_PUBLIC, "output/" + outputFile, null, "java/lang/Object", null);

        {   
            // Setup constructor
            MethodVisitor mv = cw.visitMethod(Opcodes.ACC_PUBLIC, "<init>", "()V", null, null);
            mv.visitCode();
            mv.visitVarInsn(Opcodes.ALOAD, 0);
            mv.visitMethodInsn(Opcodes.INVOKESPECIAL, "java/lang/Object", "<init>", "()V", false);

            mv.visitInsn(Opcodes.RETURN);
            mv.visitMaxs(1, 1);
            mv.visitEnd();
        }

    }

    /**
     * Visits the file, starting bytecode main method
     * 
     * @param ctx: the context of the file from the parse tree
     * @return null
     */
    @Override
    public Object visitFile(KnightCodeParser.FileContext ctx) {
        System.out.println("visiting File");
    
        mv = cw.visitMethod(ACC_PUBLIC + ACC_STATIC, "main", "([Ljava/lang/String;)V", null, null);
        mv.visitCode();

        if (ctx.declare() != null) {
            visit(ctx.declare());
        }

        visit(ctx.body());
        mv.visitInsn(RETURN);
        mv.visitMaxs(0, 0);
        mv.visitEnd();

        finish();

        return null;
    }

    /**
     * Completes the bytecode generation and write it to a specific file path
     */
    public void finish() {
        System.out.println("Visiting Finish");
    
        cw.visitEnd();
    
        byte[] code = cw.toByteArray();

        writeClassToFile(code, "./output/" + this.outputFile + ".class");
        System.out.println("Finished generating output file: " + this.outputFile + ".class");

    }

    /**
     * Writes the specific bytecode to the file
     * 
     * @param code: the bytecode
     * @param fileName: the name of the file to write to
     */
    private void writeClassToFile(byte[] code, String fileName) {
        try (FileOutputStream fos = new FileOutputStream(fileName)) {
            fos.write(code);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Visits the print statement in the KnightCode
     * 
     * @param ctx: the print context from the parse tree
     * @return  returns the print
     */
    @Override
    public Object visitPrint(KnightCodeParser.PrintContext ctx) {
        System.out.println("Visiting Print");
    
        mv.visitFieldInsn(Opcodes.GETSTATIC, "java/lang/System", "out", "Ljava/io/PrintStream;");

            if(ctx.STRING() != null){
            String text = ctx.STRING().getText();
            text = text.substring(1, text.length()-1).replace("\\\"", "\"").replace("\\\\", "\\");

            mv.visitLdcInsn(text);                
            mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "java/io/PrintStream", "println", "(Ljava/lang/String;)V", false);
            }
            else if(ctx.ID() != null){
                Variable var = symbolTable.getVariable(ctx.ID().getText());

                if(var == null){
                    System.out.println("***Variable " + ctx.ID().getText() + " not found***");
                    return null;
                }
                if(var.isInt()){
                    mv.visitVarInsn(Opcodes.ILOAD, var.getIndex());
                    mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "java/io/PrintStream", "println", "(I)V", false);
                }
                else if(!var.isInt()){
                    mv.visitVarInsn(Opcodes.ALOAD, var.getIndex());
                    mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "java/io/PrintStream", "println", "(Ljava/lang/String;)V", false);
                }
            }
            else{
                System.out.println("***No valid string or variable***");
        
            }
        return super.visitPrint(ctx);
    }

    /**
     * Loads a variable from the local variable array into the stack based on its type.  This method is used to load the variables value before it needs to be used
     * 
     * @param varName: the name of the variable to be loaded
     * @param type: the type of the variable
     */
    private void loadVariable(String varName, String type) {
        System.out.println("Loading Variable");
        int index = getVariableIndex(varName);
        if (type.equals("INTEGER")) {
            mv.visitVarInsn(ILOAD, index);
        } 
        else if (type.equals("STRING")) {
            mv.visitVarInsn(ALOAD, index);
        }
    }

    /**
     * Gets the index of a variable from the array
     * 
     * @param varName: name of the variable whose index is needed 
     * @return the index of the variable
     * @throws IllegalArgumentException if the variable isn't found
     */
    private int getVariableIndex(String varName) {
        int index = 1;
        for (HashMap<String, Variable> scope : symbolTable.getSpans()) {
            if (scope.containsKey(varName)) {
                for (String key : scope.keySet()) {
                    if (key.equals(varName)) {
                        return index;
                    }
                    index++;
                }
            } 
            else {
                index += scope.size();
            }
        }
        throw new IllegalArgumentException("***Variable '" + varName + "' not found.***");
    }

    /**
     * Visits the variable context and loads its value if it is found
     * 
     * @param ctx: the context of the variable from the parse tree
     * @return null
     * @throws RuntimeException if the variable is not found
     */
    @Override
    public Void visitVariable(KnightCodeParser.VariableContext ctx) {
        System.out.println("Visiting Variable");
        String varName = ctx.identifier().ID().getText();
        
        if (!symbolTable.isDeclared(varName)) {
            throw new RuntimeException("***Variable '" + varName + "' not found.***");
        }

        Variable var = symbolTable.getVariable(varName);
        loadVariable(varName, var.getType());
        return null;
    }

    /**
     * Visits the Read instruction to recieve an input from the user and store it to a specific variable
     * 
     * @param ctx: the read context from the parse tree
     * @return null
     * @throws RuntimeException if the variable is not found
     */
    @Override
    public Void visitRead(KnightCodeParser.ReadContext ctx) {
        System.out.println("Visting Read");
        String varName = ctx.ID().getText();
        Variable var = symbolTable.getVariable(varName);
        if (var == null) {
            throw new RuntimeException("***Variable '" + varName + "' not found.***");
        }
    
        int scanLocation = nextLocalInt++;
        mv.visitTypeInsn(Opcodes.NEW, "java/util/Scanner");
        mv.visitInsn(Opcodes.DUP);
        mv.visitFieldInsn(Opcodes.GETSTATIC, "java/lang/System", "in", "Ljava/io/InputStream;");
        mv.visitMethodInsn(Opcodes.INVOKESPECIAL, "java/util/Scanner", "<init>", "(Ljava/io/InputStream;)V", false);
        mv.visitVarInsn(Opcodes.ASTORE, scanLocation);
    
        //Stores input as an integer if input is an integer
        if ("INTEGER".equals(var.getType())) {
            mv.visitVarInsn(Opcodes.ALOAD, scanLocation);
            mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "java/util/Scanner", "nextInt", "()I", false);
            mv.visitVarInsn(Opcodes.ISTORE, var.getIndex());
            
        }
        //Stores input as a string if input is a string 
        else if ("STRING".equals(var.getType())) {
            mv.visitVarInsn(Opcodes.ALOAD, scanLocation);
            mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "java/util/Scanner", "nextLine", "()Ljava/lang/String;", false);
            mv.visitVarInsn(Opcodes.ASTORE, var.getIndex());
        }
    
        return null;
    }
    
    /**
     * Visits the declare instruction to declare a new variable
     * 
     * @param ctx declare context from the parse tree
     * @returnn null
     * @throws Exception if the variable is already declared
     */
    @Override
    public Void visitDeclare(KnightCodeParser.DeclareContext ctx) {
        System.out.println("Visiting Declare");
        for (KnightCodeParser.VariableContext varCtx : ctx.variable()) {
            String varName = varCtx.identifier().ID().getText();
            String type = varCtx.vartype().getText();
            try {
                if (!symbolTable.isDeclared(varName)) {
                    symbolTable.declareVariable(varName, type, getDefaultInitialValue(type));
                    Variable var = symbolTable.getVariable(varName);

                    if ("INTEGER".equals(type)) {
                        mv.visitInsn(ICONST_0);
                        mv.visitVarInsn(ISTORE, var.getIndex());
                    } 
                    else if ("STRING".equals(type)) {
                        mv.visitLdcInsn("");
                        mv.visitVarInsn(ASTORE, var.getIndex());
                    }
                } 
                else {
                    System.out.println("***Variable '" + varName + "' already declared.***");
                }
            }   catch (Exception e) {
                System.err.println(e.getMessage());
            }
        }
        return null;
    }


    /**
     * Returns the default value of a variable depending on its type
     * 
     * @param type the type of the variable 
     * @return the default value of the variable depending on its type
     */
    private Object getDefaultInitialValue(String type) {
        if (type.equals("INTEGER")) {
            return 0; 
        } else if (type.equals("STRING")) {
            return ""; 
        }
        return null;
    }
    
    /**
     * Visits the body instruction, processing through each statement context from a parse tree
     * 
     * @param ctx: the body context from the parse tree
     * @returnn null
     * 
     */
    @Override
    public Void visitBody(KnightCodeParser.BodyContext ctx) {
        System.out.println("Visiting Body");
        for (KnightCodeParser.StatContext statCtx : ctx.stat()) {
            visit(statCtx);
        }
        return null;
    }

    /**
     * Visiting the Setvar instruction, evaluating expressions and assigning their values to variables or directly setting string values
     * 
     * @param ctx the context for setting a variable 
     * @return null
     * @throws RuntimeException if the variable isnt found
     */
    @Override
    public Void visitSetvar(KnightCodeParser.SetvarContext ctx) {
        System.out.println("Visiting Setvar");
        String varName = ctx.ID().getText();
        System.out.println("Setting variable: " + varName);

        Variable var = symbolTable.getVariable(varName);
        if (var == null) {
            throw new RuntimeException("***Variable '" + varName + "' not found.***");
        }

        //Evaluate the expression or set the string directly
        if (ctx.expr() != null) {
            System.out.println("Evaluating expression for: " + varName);
            visit(ctx.expr());
        } 
        else if (ctx.STRING() != null) {
            String stringValue = ctx.STRING().getText();
            stringValue = stringValue.substring(1, stringValue.length() - 1).replace("\\\"", "\"").replace("\\\\", "\\");
            System.out.println("Setting string value: " + stringValue + " to " + varName);
            mv.visitLdcInsn(stringValue);
        }

        //Stores the value or string to the specified variable
        System.out.println("Storing value to " + varName + " at index " + var.getIndex());
        if ("INTEGER".equals(var.getType())) {
            mv.visitVarInsn(ISTORE, var.getIndex());
        } 
        else if ("STRING".equals(var.getType())) {
            mv.visitVarInsn(ASTORE, var.getIndex());
        }

        return null;
    }

    /**
     * Loads the integer value depending on if it is on the stack or not
     * 
     * @param data: the data to be loaded 
     */
    public void loadInteger(String data){
        //Load from variable if declared
        if( symbolTable.isDeclared(data)) {
            mv.visitVarInsn(Opcodes.ILOAD, symbolTable.getVariable(data).getIndex());
        }
        //Directly load integer value
        else {
            mv.visitLdcInsn(Integer.valueOf(data));
        }
    }

    /**
     * Handles the decision making statements by evaluating conditions and managing branches
     * 
     * @param ctx: The decision context which includes conditions and branching
     * @return null
     */
    @Override
    public Void visitDecision(KnightCodeParser.DecisionContext ctx) {
        System.out.println("Visiting Decision");
    
        Label trueLabel = new Label();
        Label endLabel = new Label();
    
        //Loads the operands for comparison
        String leftSide = ctx.getChild(1).getText();
        String operator = ctx.getChild(2).getText();
        String rightSide = ctx.getChild(3).getText();
        
        loadInteger(leftSide);
        loadInteger(rightSide);
    
        //Determins the jump based on the comparison
        switch (operator) {
            case ">":
                mv.visitJumpInsn(Opcodes.IF_ICMPGT, trueLabel);
                break;
            case "<":
                mv.visitJumpInsn(Opcodes.IF_ICMPLT, trueLabel);
                break;
            case "=":
                mv.visitJumpInsn(Opcodes.IF_ICMPEQ, trueLabel);
                break;
            case "<>":
                mv.visitJumpInsn(Opcodes.IF_ICMPNE, trueLabel);
                break;
        }
    
        //Handle else condition if present, starting at index 5 when the THEN statement is expected 
        boolean hasElse = false;
        int elseIndex = -1;
        //Last child shoul be end of the IF statement
        int endifIndex = ctx.getChildCount() - 1;
    
        //Loop through the children from THEN statement looking for ELSE  
        for (int i = 5; i < ctx.getChildCount(); i++) {
            if ("ELSE".equals(ctx.getChild(i).getText())) {
                hasElse = true;
                elseIndex = i;
                break;
            }
        }
        //If there is an ELSE block, process the statements within it
        if (hasElse) {
            for (int i = elseIndex + 1; i < endifIndex; i++) {
                //visit each statement in the ELSE 
                visit(ctx.getChild(i));
            }
            //Jump to end when done processing
            mv.visitJumpInsn(Opcodes.GOTO, endLabel); 
        }
        //Label and process statements for true condition
        mv.visitLabel(trueLabel);
        if (hasElse) {
            //Process statement between THEN and ELSE
            for (int i = 5; i < elseIndex; i++) {
                visit(ctx.getChild(i));
            }
        }
        //If no ELSE, process all statements after THEN until ENDIF 
        else {
            for (int i = 5; i < endifIndex; i++) {
                visit(ctx.getChild(i));
            }
        }
    
        //Set label for end of decision block
        mv.visitLabel(endLabel);
        return null;
    }

    /**
     * Process the loop construction from the parse tree that uses conditional loops based on certain conditions
     * 
     * @param ctx: the context of the loop from the parse tree
     * @return null
     */
    @Override
    public Void visitLoop(KnightCodeParser.LoopContext ctx) {
        System.out.println("Visiting Loop");

        Label startLoopLabel = new Label();
        Label endLoopLabel = new Label();

        //Start the loop
        mv.visitLabel(startLoopLabel);

        //Evaluate the loop
        String leftSide = ctx.getChild(1).getText();
        String operator = ctx.getChild(2).getText();
        String rightSide = ctx.getChild(3).getText();

        loadInteger(leftSide);
        loadInteger(rightSide);

        //Determins the jump based on the comparison
        switch (operator) {
            case ">":
                mv.visitJumpInsn(Opcodes.IF_ICMPLE, endLoopLabel);
                break;
            case "<":
                mv.visitJumpInsn(Opcodes.IF_ICMPGE, endLoopLabel);
                break;
            case "=":
                mv.visitJumpInsn(Opcodes.IF_ICMPNE, endLoopLabel);
                break;
            case "<>":
                mv.visitJumpInsn(Opcodes.IF_ICMPEQ, endLoopLabel);
                break;
        }

        //Process all statements inside the loop body
        ctx.stat().forEach(this::visit);

        //Jump to the start of the loop
        mv.visitJumpInsn(Opcodes.GOTO, startLoopLabel);

        //End of the loop
        mv.visitLabel(endLoopLabel);

        return null;
    }

    /**
     * Handles the inside of the parentheses 
     * 
     * @param ctx: the context of the parenthesis from the parse tree
     * @return null
     */
    @Override
    public Object visitParenthesis(KnightCodeParser.ParenthesisContext ctx){
        System.out.println("Handling Parenthesis");
        //Evaluate the inner expression
        visit(ctx.getChild(1)); 
        return null;
    }

    /**
     * Process multiplication expression by evaluating both sides and multiplying the results
     * 
     * @param ctx the context of the multiplication from the parse tree
     * @return null
     */
    @Override
    public Object visitMultiplication(KnightCodeParser.MultiplicationContext ctx){
        System.out.println("Multiplying");
        visit(ctx.getChild(0));
        visit(ctx.getChild(2));
        mv.visitInsn(Opcodes.IMUL);
        return null;
    }

    /**
     * Process division expression by evaluating both sides and dividing the results
     * 
     * @param ctx the context of the division from the parse tree
     * @return null
     */
    @Override
    public Object visitDivision(KnightCodeParser.DivisionContext ctx){
        System.out.println("Dividing");
        visit(ctx.getChild(0));
        visit(ctx.getChild(2));
        mv.visitInsn(Opcodes.IDIV);
        return null;
    }

    /**
     * Process addition expression by evaluating both sides and adding the results
     * 
     * @param ctx the context of the addition from the parse tree
     * @return null
     */
    @Override
    public Object visitAddition(KnightCodeParser.AdditionContext ctx){
        System.out.println("Adding");
        visit(ctx.getChild(0));
        visit(ctx.getChild(2));
        mv.visitInsn(Opcodes.IADD);
        return null;
    }

    /**
     * Process sbutraction expression by evaluating both sides and subtracting the results
     * 
     * @param ctx the context of the subtraction from the parse tree
     * @return null
     */
    @Override
    public Object visitSubtraction(KnightCodeParser.SubtractionContext ctx){
        System.out.println("Sutraction");
        visit(ctx.getChild(0));
        visit(ctx.getChild(2));
        mv.visitInsn(Opcodes.ISUB);
        return null;
    }

    /**
     * Process comparison expression by evaluating both sides and comparing the results
     * 
     * @param ctx the context of the comparison from the parse tree
     * @return null
     */
    @Override
    public Object visitComparison(KnightCodeParser.ComparisonContext ctx){
        System.out.println("Comparing");

        visit(ctx.getChild(0));
        visit(ctx.getChild(2));

            Label trueLabel = new Label();
            Label endLabel = new Label();

            //Determins the jump based on the comparison
            String operator = ctx.getChild(1).getText();
            switch(operator){
                case ">":
                    mv.visitJumpInsn(Opcodes.IF_ICMPGT, trueLabel);
                    break;
                case "<":
                    mv.visitJumpInsn(Opcodes.IF_ICMPLT, trueLabel);
                    break;
                case "=":
                    mv.visitJumpInsn(Opcodes.IF_ICMPEQ, trueLabel);
                    break;
                case "<>":
                    mv.visitJumpInsn(Opcodes.IF_ICMPNE, trueLabel);
                    break;
            }

            mv.visitJumpInsn(Opcodes.GOTO, endLabel);
            mv.visitLabel(trueLabel);
            mv.visitLdcInsn(1);
            mv.visitJumpInsn(Opcodes.GOTO, endLabel);
            mv.visitLabel(endLabel);
            mv.visitLdcInsn(0);

    return null;

    }

    /**
     * Process numeric literals by loading them onto the stack
     * 
     * @param ctx the context of the number from the parse tree
     * @return null
     */
    @Override
    public Object visitNumber(KnightCodeParser.NumberContext ctx){
        System.out.println("Number context");
        String value = ctx.getText();
        //load the number
        mv.visitLdcInsn(Integer.valueOf(value));
        return null;

    }

    /**
     * Handles identifier access by loading values if they've been delcared
     * 
     * @param ctx the context of the identifier from the parse tree
     * @return null
     */
    @Override
    public Object visitId(KnightCodeParser.IdContext ctx){
        System.out.println("ID context");

        String id = ctx.getText();
        if( symbolTable.isDeclared(id)) {
            mv.visitVarInsn(Opcodes.ILOAD, symbolTable.getVariable(id).getIndex());
            } 
            else {
                System.err.println("***Variable '" +  id + "' not found***");
            }
            
            return null;
    }

    /**
     * Process comparison expression by evaluating both sides and comparing the results
     * 
     * @param ctx the context of the comparison from the parse tree
     * @return null
     */
    @Override
    public Void visitComp(KnightCodeParser.CompContext ctx){
        System.out.println("Visiting Comp");

        visit(ctx.getChild(0));
        visit(ctx.getChild(2));

        Label trueLabel = new Label();
        Label endLabel = new Label();

        //Determins the jump based on the comparison
        String operator = ctx.getChild(1).getText();
        switch(operator){
            case ">":
                mv.visitJumpInsn(Opcodes.IF_ICMPGT, trueLabel);
                break;
            case "<":
                mv.visitJumpInsn(Opcodes.IF_ICMPLT, trueLabel);
                break;
            case "=":
                mv.visitJumpInsn(Opcodes.IF_ICMPEQ, trueLabel);
                break;
            case "<>":
                mv.visitJumpInsn(Opcodes.IF_ICMPNE, trueLabel);
                break;
        }

        mv.visitJumpInsn(Opcodes.GOTO, endLabel);
        mv.visitLabel(trueLabel);
        mv.visitLdcInsn(1);
        mv.visitJumpInsn(Opcodes.GOTO, endLabel);
        mv.visitLabel(endLabel);
        mv.visitLdcInsn(0);

        return null;
    }

}
