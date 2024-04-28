package compiler;

/**
* Variable class that takes a variable and specifies a value and type to it

* @author Jim Farese
* @version 1.0
* Assignment 5
* CS322 - Compiler Construction
* Spring 2024
**/
public class Variable {

    private String name;
    private String type;
    private Object value;
    private int index;

    /**
     * Consturctor
     * @param name: the name of the variable
     * @param type: the type of the variable
     * @param value: the value of the variable
     * @param index: the location of the variable in the array
     */
    public Variable(String name, String type, Object value, int index) {
        this.name = name;
        this.type = type;
        this.value = value;
        this.index = index;
    
    }

    /**
     * Gets the name of the variable
     * 
     * @return the name of the variable
     */
    public String getName() {
        return name;
    }

    /**
     * Gets the type of the variable
     * 
     * @return the type of the variable
     */
    public String getType() {
        return type;
    }


    /**
     * Gets the value of the variable
     * 
     * @return the value of the variable
     */
    public Object getValue() {
        return value;
    }


    /**
     * Sets the value of the variable
     * 
     * @param value: the new value of the variable
     */
    public void setValue(Object value) {
        this.value = value;
    }


    /**
     * Checks if the variable is an integer or string since the compiler only handles integers and strings 
     * 
     * @return true if the variable is an integer, false if it is a string
     */
    public boolean isInt(){
        return "INTEGER".equals(this.type);
    }

    /**
     * Gets the index of the variable in the array
     * 
     * @return the location where the variable is stored 
     */
    public int getIndex(){
        return index;
    }

}
