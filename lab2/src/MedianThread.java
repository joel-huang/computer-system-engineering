import com.sun.scenario.effect.Merge;

import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class MedianThread {

    public static void main(String[] args) throws InterruptedException, FileNotFoundException {

        String fileName = String.valueOf(args[0]);
        File file = new File(fileName);
        BufferedReader reader = new BufferedReader(new FileReader(file));
        List<String> lines;
        List<Integer> ints = new ArrayList<>();

        try {
            String line = reader.readLine();
            lines = Arrays.asList(line.split("\t\\s"));
            for (String s : lines) {
                ints.add(Integer.valueOf(s.trim()));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        // define number of threads
        int numThreads = Integer.valueOf(args[1]);
        int partitionIndex = ints.size() / numThreads;
        int indexToSplit = 0;
        ArrayList<List> arrayList = new ArrayList<>();

        for (int i = 0; i < numThreads; i++) {
            int newIndex;
            if (numThreads - i != 1) {
                newIndex = partitionIndex + i * partitionIndex;
            } else {
                newIndex = ints.size();
            }
            arrayList.add(ints.subList(indexToSplit, newIndex));
            indexToSplit = newIndex;
        }

        ArrayList<MedianMultiThread> threadList = new ArrayList<>();
        for (int i = 0; i < numThreads; i++) {
            ArrayList<Integer> sublist = new ArrayList<>(arrayList.get(i));
            threadList.add(new MedianMultiThread(sublist));
        }

        final long time = System.currentTimeMillis();

        for (MedianMultiThread t : threadList) {
            t.start();
        }

        for (MedianMultiThread t : threadList) {
            t.join();
        }

        ArrayList<Integer> sortedArray = new ArrayList<>();
        ArrayList<MergeThread> mergeThreadList = new ArrayList<>();
        int times = (int) (Math.log(numThreads)/Math.log(2));

        if (times == 0){
            sortedArray = threadList.get(0).getInternal();
        } else {
            for (int i = 0; i < times; i++) {
                ArrayList<MergeThread> tempList = new ArrayList<>();
                if (i == 0) {
                    for (int x = 0; x < threadList.size(); x += 2) {
                        MergeThread newThread = new MergeThread(threadList.get(x).getInternal(), threadList.get(x + 1).getInternal());
                        tempList.add(newThread);
                    }
                } else {
                    for (int x = 0; x < mergeThreadList.size(); x += 2) {
                        MergeThread newThread = new MergeThread(mergeThreadList.get(x).getInternal(), mergeThreadList.get(x + 1).getInternal());
                        tempList.add(newThread);
                    }
                }
                for (MergeThread thread : tempList) {
                    thread.start();
                }
                for (MergeThread thread : tempList) {
                    thread.join();
                }

                mergeThreadList = new ArrayList<>(tempList);
                tempList.clear();
            }
            sortedArray = mergeThreadList.get(0).getInternal();
        }


        double median = computeMedian(sortedArray);
        long elapsedTime = System.currentTimeMillis() - time;
        System.out.println("Computed Median: " + median);
        System.out.println("Time elapsed: " + elapsedTime + "ms");
    }

    public static double computeMedian(ArrayList<Integer> inputArray) {
        double median = 0;
        if (inputArray.size() % 2 == 0) {
            median = (inputArray.get(inputArray.size()/2) + inputArray.get((inputArray.size()/2)-1))/2;
        } else {
            median = Math.ceil(inputArray.get(inputArray.size()/2));
        } return median;
    }

    }

// extend Thread
class MedianMultiThread extends Thread {
    private ArrayList<Integer> list;

    public ArrayList<Integer> getInternal() {
        return list;
    }

    MedianMultiThread(ArrayList<Integer> array) {
        list = array;
    }

    public void run() {
        mergeSort(list);
    }

    public void mergeSort(ArrayList<Integer> array) {
        Collections.sort(array);
    }
}

class MergeThread extends Thread {
    private ArrayList<Integer> list1;
    private ArrayList<Integer> list2;
    private ArrayList<Integer> merged;

    public ArrayList<Integer> getInternal() {
        return merged;
    }

   MergeThread(ArrayList<Integer> array1, ArrayList<Integer> array2) {
        list1 = array1;
        list2 = array2;
    }

    public void run() {
        merged = merge(list1, list2);
    }

    public ArrayList<Integer> merge(ArrayList<Integer> array1, ArrayList<Integer> array2) {
        ArrayList<Integer> temp = new ArrayList<>();
        temp.addAll(array1);
        temp.addAll(array2);
        Collections.sort(temp);
        return temp;
    }

}