package compiler;

import java.io.IOException;

import org.antlr.v4.gui.Trees;
import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.tree.ParseTree;

import lexparse.KnightCodeLexer;
import lexparse.KnightCodeParser;

/**
* kcc Class that contains the main method to run the compiler.  It uses arguments to take an input file and process it through the compiler to create an output file

* @author Jim Farese
* @version 1.0
* Assignment 5
* CS322 - Compiler Construction
* Spring 2024
**/
public class kcc {

    /**
    * Main method that takes 2 command-line arguments; an input argument to import a file and an output argumnet to designate the location to create the output file to
    
    * @param args: Command line arguments that designates input and output paths 
    */
    public static void main(String[] args) {
        //Checks for the correct number of command line arguments
        if (args.length != 2) {
            System.out.println("***Usage: java compiler/kcc <input file> <output class file>***");
            return;
        }

        String inputFile = args[0];
        String outputFile = args[1].replace("output/", "");

        try {
            //Setup ANTLR for lexing and parsing
            CharStream input = CharStreams.fromFileName(inputFile);
            KnightCodeLexer lexer = new KnightCodeLexer(input);
            CommonTokenStream tokens = new CommonTokenStream(lexer);
            KnightCodeParser parser = new KnightCodeParser(tokens);

            //Parse the input file to a parse tree
            ParseTree tree = parser.file();

            //Visit the parse tree to generate code
            CustomVisitor visitor = new CustomVisitor();
            visitor.setOutputFile(outputFile);
            visitor.visit(tree);

            //Display the parse tree GUI
            Trees.inspect(tree, parser);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
}
