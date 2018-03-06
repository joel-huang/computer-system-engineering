import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class ProcessManagement {

    //set the working directory
    private static File currentDirectory = new File("");
    //set the instructions file
    private static File instructionSet = new File("test/test1.txt");
    public static Object lock = new Object();

    public static void main(String[] args) throws InterruptedException {

        //parse the instruction file and construct a data structure, stored inside ProcessGraph class
        ParseFile.generateGraph(new File(currentDirectory.getAbsolutePath() + "/"+instructionSet));

        ProcessGraph.printGraph();
        // Print the graph information
        // WRITE YOUR CODE


        // Using index of ProcessGraph, loop through each ProcessGraphNode, to check whether it is ready to run
        // check if all the nodes are executed
        // WRITE YOUR CODE
        boolean done = false;
        int executed = 0;

        while (!done) {

            for (ProcessGraphNode node : ProcessGraph.nodes) {
                if (node.isExecuted()) executed++;
            }

            if (executed == ProcessGraph.nodes.size()) {
                executed = 0;
                done = true;
                break;
            }

            for (ProcessGraphNode node : ProcessGraph.nodes) {
                if (node.allParentsExecuted()) {
                    node.setRunnable();
                }
            }

            for (ProcessGraphNode node : ProcessGraph.nodes) {
                if (node.isRunnable()) {

                    ProcessBuilder processBuilder = new ProcessBuilder(node.getCommand());
                    try {
                        Process process = processBuilder.start();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                    node.setExecuted();
                }
            }

            //mark all the runnable nodes
            // WRITE YOUR CODE

            //run the node if it is runnable
            // WRITE YOUR CODE

        }

        System.out.println("All process finished successfully");
    }

}