#KnightCode Compiler

-Overview
   -KnightCode is a simple, custom designed programming language created for learning and practicing compiler construction.
   
-Features
   -Basic Data types:
      -It only incorporates INTEGER and STRING
   -Control Structure:
      -Uses IF-ELSE decisions and WHILE loops
   -Arithmetic Operations:
      -Performs basic Addition, Subtraction, Multiplication, and Division
   -Comparison:
      -Performs comparisons of Greater than, Less than, Equals, and Not equals
   -Input/Output operations:
      -Read input and Print output

-Prerequisites
   -Java JDK
   -ANTLR 4.9.2

-Using the Compiler
   -To compile a KnightCode program, you will need Java installed. The compiler then runs using a command line argument with an input and output.  The structure of the command line and the file paths must be implemented exactly using this structure.  Additionally ensure that ANTLR is in the primary directory
   
      #Directory:
      -KnightCodeCompiler
         -compiler
         -lexparse
         -tests
         -output
         -'antlr-4.9.2-complete.jar'
      
      #Command line argument:
         java compiler/kcc tests/<test program> output/<desired output file name>
   
   -Once the output file is generated, a command line will be used to run the file.
   
      #Command line argument:
         java output/<name of generated output file>

