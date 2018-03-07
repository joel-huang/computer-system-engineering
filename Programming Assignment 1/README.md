<!--
Programming Assignment 1
Author  :  Huang Zhiquan Joel
ID      :  1002530
Date    :  08/03/2018
-->

# Purpose of the program

* This program manages a list of processes and makes sure no process dependent on another will execute. This is achieved by flagging processes as runnable or not based on their dependencies' execution status. Output is verbose, so the user will be able to see what has occurred in attempting to run the list of processes.

# How to compile and run

* `cd` to `Programming Assignment 1/src`
* Compile: `javac ProcessManagement.java`
* Run: `java ProcessManagement`
* Make sure all the test cases are in the `src/test` directory!
* Outputs will be generated in the `Programming Assignment 1` directory.

# What it does

* This program `ProcessManagement.java` takes in an input file, with commands in the format `command:dependencies:inputs:outputs`. These are parsed into a ProcessGraph object, which then can be run as a directed acyclic graph (DAG). 
* The nodes in the DAG are iterated through, and those with no dependencies are executed. While all the nodes have not been executed yet, the program keeps checking if nodes are runnable and not executed, and executes them if so. A node can only be runnable if its dependencies are all executed to completion. 
* The inputs and outputs of the nodes are specified with redirections, and if set to `stdout`, the output is inherited from the parent process. This allows outputs of commands like `ls` and `ifconfig` to display in the console.