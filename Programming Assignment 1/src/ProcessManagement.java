/* 
Programming Assignment 1
Author : Huang Zhiquan Joel
ID     : 1002530
Date   : 03/08/2018 */

import java.io.File;
import java.io.IOException;
import java.util.Arrays;


// commands tested: echo, grep, cat, ls, ...



public class ProcessManagement {

    //set the working directory
    private static File currentDirectory = new File("");
    //set the instructions file
    private static File instructionSet = new File("src/test/test3.txt");


    // function to return the appropriate String[] if grep or echo is used
    private static String[] parseCommand(String command) {
        String comd = command.split(" ")[0];
        if (comd.equalsIgnoreCase("echo") ||
                comd.equalsIgnoreCase("grep")) {
            System.out.println(comd + " detected. Array returned: " + Arrays.toString(command.split(" ", 2)));
            return command.split(" ", 2);
        } return command.split(" ");
    }

    // function to check that every node in the ProcessGraph is executed
    private static boolean checkAllExecuted() {
        for (ProcessGraphNode node : ProcessGraph.nodes) {
            if (!node.isExecuted()) return false;
        }
        return true;
    }

    public static void main(String[] args) throws InterruptedException {

        ParseFile.generateGraph(new File(currentDirectory.getAbsolutePath() + "/"+instructionSet));
        ProcessGraph.printGraph();

        outerloop:
        while (true) {

            // if everything is executed, we can stop here.
            if (checkAllExecuted()) break;

            /*
            preparation: update the status of the nodes.
            check for unexecuted nodes, and determine if they are runnable.
            if they are, set them to runnable, else set not runnable.
            something is runnable if all its parents have executed.
            */
            System.out.println("Checking for unexecuted nodes...");
            for (ProcessGraphNode node : ProcessGraph.nodes) {
                if (node.getParents().isEmpty()) {
                    System.out.println("Node " + node.getNodeId() + " has no parents. setting runnable");
                    node.setRunnable();
                } else if (!node.getParents().isEmpty() && node.allParentsExecuted()) {
                    System.out.println("Node " + node.getNodeId() + " has parents && all parents executed. setting runnable");
                    node.setRunnable();
                } else if (!node.getParents().isEmpty() && !node.allParentsExecuted()) {
                    System.out.printf("Node " + node.getNodeId() + " not runnable... Culprits: ");
                    for (ProcessGraphNode parent : node.getParents()) {
                        System.out.printf(parent.getNodeId() + " (" + parent.isExecuted() + ") ");
                    } System.out.println("");
                    node.setNotRunnable();
                }
            }

            System.out.println("\n");


            /*
            check and run: iterate through all nodes,
            check each if runnable and not executed.
            if so, execute them and wait for them to terminate.
            */
            for (ProcessGraphNode node : ProcessGraph.nodes) {

                System.out.println("Checking... Node " + node.getNodeId() + " isRunnable: " + node.isRunnable() + " and isExecuted: " + node.isExecuted());

                if (node.isRunnable() && !node.isExecuted()) {
                    try {

                        ProcessBuilder processBuilder = new ProcessBuilder();

                        System.out.println("");

                        // check for stdin, pass the appropriate args
                        if (node.getInputFile().getName().equalsIgnoreCase("stdin")) {
                            System.out.println("got stdin, command: " + Arrays.toString(parseCommand(node.getCommand())));
                            processBuilder.command(parseCommand(node.getCommand()));
                        } else {
                            System.out.println("no stdin, file name: " + node.getInputFile().getName());
                            processBuilder.command(node.getCommand().split(" "));
                            if (!processBuilder.command().get(0).equals("ls"))
                            processBuilder.redirectInput(node.getInputFile());
                        }

                        // check for stdout, pass the appropriate args
                        if (node.getOutputFile().getName().equalsIgnoreCase("stdout")) {
                            System.out.println("stdout detected, inheriting IO");
                            processBuilder.inheritIO();
                        } else {
                            System.out.println("no stdout, output to " + node.getOutputFile());
                            processBuilder.redirectOutput(node.getOutputFile());
                        }

                        // attempt to start the process and print relevant info
                        System.out.println("Attempting to execute command [" + processBuilder.command() +"]");
                        Process process = processBuilder.start();
                        process.waitFor();
                        System.out.println("Executed node: " + node.getNodeId() + "\n"
                                            + "Input file: " + node.getInputFile().getAbsolutePath() + "\n"
                                            + "Output file: " + node.getOutputFile().getAbsolutePath() + "\n");
                        // set the node to be executed once it has been executed
                        node.setExecuted();

                    } catch (IOException e) {
                        /*
                        break the while loop if at any point, a command fails to execute properly.
                        the program will end, with a summary of what nodes succeeded and
                        what nodes failed.
                        */
                        e.printStackTrace();
                        break outerloop;
                    }

                }
            }
            System.out.println("");
        }

        System.out.println("Summary of execution:");
        ProcessGraph.printGraph();
    }
}