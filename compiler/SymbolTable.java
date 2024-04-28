package compiler;

import java.util.HashMap;
import java.util.Stack;

/**
* Symbol Class that manages stacks of variables

* @author Jim Farese
* @version 1.0
* Assignment 5
* CS322 - Compiler Construction
* Spring 2024
**/
public class SymbolTable {

    private Stack<HashMap<String, Variable>> spans;

    /**
     * Constructs a new SymbolTable and enters the span
     */
    public SymbolTable() {
        spans = new Stack<>();
        enterSpan();
    }

    /**
     * Creates a span by pushing a hashmap onto the stack
     */
    public void enterSpan() {
        spans.push(new HashMap<>());
    }

    /**
     * Exits the span by popping the top hashmap from the stack
     */
    public void exitSpan() {
        if (!spans.isEmpty()) {
            spans.pop();
        }
    }

    /**
     * Declares a new variable within the span
     * 
     * @param name: the name of the variable to declare
     * @param type: the type of the variable
     * @param value: the value of the variable
     * @throws Exception: exception if the variable is already in the span
     */
    public void declareVariable(String name, String type, Object value) throws Exception {
        HashMap<String, Variable> currentSpan = spans.peek();
        if (currentSpan.containsKey(name)) {
            throw new Exception("***Variable '" + name + "' is already declared in this span.***");
        }
        int newIndex = currentSpan.size();
        System.out.println("Declaring variable: " + name + " at index: " + newIndex);
        Variable newVar = new Variable(name, type, value, newIndex);
        currentSpan.put(name, newVar);
    }
    
    /**
     * Gets a variable from the span
     * 
     * @param name: the name of the variable to retrieve
     * @return: the Variable object
     */
    public Variable getVariable(String name) {
        for (int i = spans.size() - 1; i >= 0; i--) {
            HashMap<String, Variable> span = spans.get(i);
            if (span.containsKey(name)) {
                return span.get(name);
            }
        }
        return null;
    }

    /**
     * Sets the value of the variables
     * 
     * @param name: the name of the variable whos value is to be set
     * @param value: the value to set the variable at
     * @throws Exception: exception if the variable hasnt been declared in the span
     */
    public void setVariableValue(String name, Object value) throws Exception {
        for (int i = spans.size() - 1; i >= 0; i--) {
            HashMap<String, Variable> span = spans.get(i);
            if (span.containsKey(name)) {
                Variable var = span.get(name);
                var.setValue(value);
                return;
            }
        }
        throw new Exception("***Variable '" + name + "' has not been declared.***");
    }

    /**
     * Checks if the variable has been declared
     * 
     * @param name: the name of the variable being checked
     * @return true if the variable has been delcared or false if not
     */
    public boolean isDeclared(String name) {
        for (HashMap<String, Variable> span : spans) {
            if (span.containsKey(name)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Gets the stack in the span
     * 
     * @return the stack in the span
     */
    public Stack<HashMap<String, Variable>> getSpans() {
        return spans;
    }
    
}
