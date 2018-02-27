import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Date;

public class FileOperation {

    private static File currentDirectory = new File(System.getProperty("user.dir"));
    private static int referenceDepth;

    public static void main(String[] args) throws java.io.IOException {

        String commandLine;

        BufferedReader console = new BufferedReader
                (new InputStreamReader(System.in));

        while (true) {
            // read what the user entered
            System.out.print("jsh>");
            commandLine = console.readLine();

            // clear the space before and after the command line
            commandLine = commandLine.trim();

            // if the user entered a return, just loop again
            if (commandLine.equals("")) {
                continue;
            }
            // if exit or quit
            else if (commandLine.equalsIgnoreCase("exit") | commandLine.equalsIgnoreCase("quit")) {
                System.exit(0);
            }

            // check the command line, separate the words
            String[] commandStr = commandLine.split(" ");
            ArrayList<String> command = new ArrayList<String>();
            for (int i = 0; i < commandStr.length; i++) {
                // allow space errors
                if (!commandStr[i].equalsIgnoreCase("")) {
                    command.add(commandStr[i]);
                }
            }

            if (command.get(0).equalsIgnoreCase("create")) {
                if (command.size() == 2) {
                    Java_create(currentDirectory, command.get(1));
                } else {
                    System.out.println("Invaild arguments.");
                }
            }

            else if (command.get(0).equalsIgnoreCase("delete")) {
                if (command.size() == 2) {
                    Java_delete(currentDirectory, command.get(1));
                } else {
                    System.out.println("Invaild arguments.");
                }
            }

            else if (command.get(0).equalsIgnoreCase("display")) {
                if (command.size() == 2) {
                    Java_cat(currentDirectory, command.get(1));
                } else {
                    System.out.println("Invaild arguments.");
                }
            }

            else if (command.get(0).equalsIgnoreCase("list")) {
                if (command.size() == 1) Java_ls(currentDirectory, "none", "none");

                else if (command.size() == 2) {
                    if (command.get(1).equalsIgnoreCase("property")) Java_ls(currentDirectory, command.get(1), "none");
                    else System.out.println("Invalid argument.");
                }
                else if (command.size() == 3) {
                    if (command.get(1).equalsIgnoreCase("property")
                            && (command.get(2).equalsIgnoreCase("time")
                            || command.get(2).equalsIgnoreCase("size")
                            || command.get(2).equalsIgnoreCase("name")
                    )) Java_ls(currentDirectory, command.get(1), command.get(2));
                    else System.out.println("Invalid arguments.");
                } else {
                    System.out.println("Invalid arguments.");
                }
            }

            else if (command.get(0).equalsIgnoreCase("find")) {
                if (command.size() == 2) {
                    Java_find(currentDirectory, command.get(1));
                } else {
                    System.out.println("Invaild arguments.");
                }
            }

            else if (command.get(0).equalsIgnoreCase("tree")) {
                if (command.size() == 2) {
                    if (Integer.valueOf(command.get(1)) instanceof Integer) {
                        referenceDepth = Integer.valueOf(command.get(1));
                        Java_tree(currentDirectory, Integer.valueOf(command.get(1)), "none");
                    } else System.out.println("Invalid argument.");
                }
                else if (command.size() == 3) {
                    if (Integer.valueOf(command.get(1)) instanceof Integer) {
                        referenceDepth = Integer.valueOf(command.get(1));
                        Java_tree(currentDirectory, Integer.valueOf(command.get(1)), command.get(2));
                    } else System.out.println("Invalid argument.");
                } else {
                    System.out.println("Invalid argument(s).");
                }
            }

            else {

                ProcessBuilder pBuilder = new ProcessBuilder(command);
                pBuilder.directory(currentDirectory);

                try{
                    Process process = pBuilder.start();
                    // obtain the input stream
                    InputStream is = process.getInputStream();
                    InputStreamReader isr = new InputStreamReader(is);
                    BufferedReader br = new BufferedReader(isr);

                    // read what is returned by the command
                    String line;
                    while ( (line = br.readLine()) != null)
                        System.out.println(line);

                    // close BufferedReader
                    br.close();
                }
                // catch the IOexception and resume waiting for commands
                catch (IOException ex){
                    System.out.println(ex);
                    continue;
                }
            }

        }
    }

    /**
     * Create a file
     * @param dir - current working directory
     * @param name - name of the file to be created
     */
    public static void Java_create(File dir, String name) throws IOException {
        File file = new File(dir, name);
        file.createNewFile();
    }

    /**
     * Delete a file
     * @param dir - current working directory
     * @param name - name of the file to be deleted
     */
    public static void Java_delete(File dir, String name) {
        File file = new File(dir, name);
        file.delete();
    }

    /**
     * Display the file
     * @param dir - current working directory
     * @param name - name of the file to be displayed
     */
    public static void Java_cat(File dir, String name) {
        try {
        File file = new File(dir, name);
        FileReader fileReader = new FileReader(file);
        BufferedReader in = new BufferedReader(fileReader);
        String line;
        while((line = in.readLine())!= null){
            System.out.println(line);
        }
        in.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Function to sort the file list
     * @param list - file list to be sorted
     * @param sort_method - control the sort type
     * @return sorted list - the sorted file list
     */
    private static File[] sortFileList(File[] list, String sort_method) {
        // sort the file list based on sort_method
        // if sort based on name
        if (sort_method.equalsIgnoreCase("name")) {
            Arrays.sort(list, new Comparator<File>() {
                public int compare(File f1, File f2) {
                    return (f1.getName()).compareTo(f2.getName());
                }
            });
        }
        else if (sort_method.equalsIgnoreCase("size")) {
            Arrays.sort(list, new Comparator<File>() {
                public int compare(File f1, File f2) {
                    return Long.valueOf(f1.length()).compareTo(f2.length());
                }
            });
        }
        else if (sort_method.equalsIgnoreCase("time")) {
            Arrays.sort(list, new Comparator<File>() {
                public int compare(File f1, File f2) {
                    return Long.valueOf(f1.lastModified()).compareTo(f2.lastModified());
                }
            });
        }
        return list;
    }

    /**
     * List the files under directory
     * @param dir - current directory
     * @param display_method - control the list type
     * @param sort_method - control the sort type
     */
    public static void Java_ls(File dir, String display_method, String sort_method) {
        File[] list = dir.listFiles();

        if (display_method.equalsIgnoreCase("property")) {
            if (!sort_method.equalsIgnoreCase("none")) sortFileList(list, sort_method);
            for (File file : list) {
                String fileName = file.getName();
                long fileLength = file.length();
                Date lastModified = new Date(file.lastModified());
                System.out.println(fileName + " Size: " + fileLength + " Last Modified: " + lastModified);
            }
        } else {
            for (File file : list) {
                System.out.println(file.getName());
            }
        }
    }

    /**
     * Find files based on input string
     * @param dir - current working directory
     * @param name - input string to find in file's name
     * @return flag - whether the input string is found in this directory and its subdirectories
     */
    public static boolean Java_find(File dir, String name) {
        boolean flag = false;

        // if is dir, enter it, iterate through it, then exit it.
        for (File file : dir.listFiles()) {
            if (file.isDirectory()) {
                Java_find(file, name);
            }
            if (file.getAbsolutePath().contains(name)) {
                flag = true;
                System.out.println(file.getAbsolutePath());
            }
        }

        return flag;
    }

    /**
     * Print file structure under current directory in a tree structure
     * @param dir - current working directory
     * @param depth - maximum sub-level file to be displayed
     * @param sort_method - control the sort type
     */
    public static void Java_tree(File dir, int depth, String sort_method) {

        if (depth <= 0) return;

        File[] fileList = dir.listFiles();
        if (sort_method.equalsIgnoreCase("time")) sortFileList(fileList, "time");
        else if (sort_method.equalsIgnoreCase("size")) sortFileList(fileList, "size");
        else if (sort_method.equalsIgnoreCase("name")) sortFileList(fileList, "name");

        String drawBranch = "|-";
        for (int i = 0; i < referenceDepth - depth; i++) {
            drawBranch = " " + drawBranch;
        }

        for (File file: fileList) {
            if (file.isDirectory()) {
                System.out.println(drawBranch + file.getAbsolutePath());
                depth--;
                Java_tree(file, depth, sort_method);
                depth++;
            } else {
                System.out.println(drawBranch + file.getAbsolutePath());
            }
        }
    }
}