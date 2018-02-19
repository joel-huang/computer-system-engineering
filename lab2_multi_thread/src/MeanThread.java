import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class MeanThread {
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

        final long time = System.currentTimeMillis();

        for (int i = 0; i < numThreads; i++) {
            int newIndex;
            if (numThreads - i != 1) {
                newIndex = partitionIndex + i * partitionIndex;
            } else {
                newIndex = ints.size();
            }
            int subListSize = ints.subList(indexToSplit, newIndex).size();
            arrayList.add(ints.subList(indexToSplit, newIndex));
            indexToSplit = newIndex;
        }

        ArrayList<MeanMultiThread> threadList = new ArrayList<>();
        for (int i = 0; i < numThreads; i++) {
            ArrayList<Integer> sublist = new ArrayList<>(arrayList.get(i));
            threadList.add(new MeanMultiThread(sublist));
        }


        ArrayList<Double> temporalMeanValues = new ArrayList<>();
        int threadCtr = 0;

        for (MeanMultiThread t : threadList) {
            t.start();
        }

        for (MeanMultiThread t : threadList) {
            t.join();
        }

        for (MeanMultiThread t : threadList) {
            System.out.println("Temporal mean value of thread " + threadCtr + " is: " + t.getMean() + ", calculated at " + (System.currentTimeMillis() - time) + "ms");
            temporalMeanValues.add(t.getMean());
            threadCtr++;
        }

        double sum = 0;
        for (double d : temporalMeanValues) {
            sum += d;
        }
        System.out.println("The global mean value is: "+ sum/numThreads);

        long newTime = System.currentTimeMillis();
        System.out.println("Time elapsed: " + (newTime - time) + "ms");

    }
}


class MeanMultiThread extends Thread {

    private ArrayList<Integer> list;
	private double mean;

	MeanMultiThread(ArrayList<Integer> array) {
		list = array;
	}

	public double getMean() {
		return mean;
	}

	public void run() {
		mean = computeMean(list);
	}

	public double computeMean(ArrayList<Integer> array) {
	    double sum = 0;
	    for (int i : array) {
	        sum += i;
        }
        return sum/array.size();
    }
}