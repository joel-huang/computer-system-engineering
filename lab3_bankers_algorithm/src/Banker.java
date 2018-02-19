import com.sun.scenario.effect.impl.sw.sse.SSEBlend_SRC_OUTPeer;
import jdk.nashorn.internal.runtime.arrays.ArrayIndex;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.lang.reflect.Array;
import java.util.Arrays;

public class Banker {
	private int numberOfCustomers;	// the number of customers
	private int numberOfResources;	// the number of resources

	private int[] available; 	// the available amount of each resource
	private int[][] maximum; 	// the maximum demand of each customer
	private int[][] allocation;	// the amount currently allocated
	private int[][] need;		// the remaining needs of each customer

	/**
	 * Constructor for the Banker class.
	 * @param resources          An array of the available count for each resource.
	 * @param numberOfCustomers  The number of customers.
	 */
	public Banker (int[] resources, int numberOfCustomers) {

	    numberOfResources = resources.length;
        this.numberOfCustomers = numberOfCustomers;

        available = resources;
        maximum = new int[numberOfCustomers][numberOfResources];
        allocation = new int[numberOfCustomers][numberOfResources];
        need = new int[numberOfCustomers][numberOfResources];
	}

	/**
	 * Sets the maximum number of demand of each resource for a customer.
	 * @param customerIndex  The customer's index (0-indexed).
	 * @param maximumDemand  An array of the maximum demanded count for each resource.
	 */
	public void setMaximumDemand(int customerIndex, int[] maximumDemand) {
        maximum[customerIndex] = maximumDemand;
        for (int i = 0; i < numberOfCustomers; i++) {
            for (int j = 0; j < numberOfResources; j++) {
                need[i][j] = maximum[i][j] - allocation[i][j];
            }
        }
    }

	/**
	 * Prints the current state of the bank.
	 */
	public void printState() {
        System.out.println("Available: " + Arrays.toString(available));

        String max = "Maximum: ";
        String alloc = "Allocation: ";
        String nd = "Need: ";

        for (int i = 0; i < numberOfCustomers ; i++) {
            if (i != numberOfCustomers - 1) {
                max += Arrays.toString(maximum[i]) + ", ";
                alloc += Arrays.toString(allocation[i]) + ", ";
                nd += Arrays.toString(need[i]) + ", ";
            } else {
                max += Arrays.toString(maximum[i]);
                alloc += Arrays.toString(allocation[i]);
                nd += Arrays.toString(need[i]) + "\n";
            }
        }

        System.out.println(max);
        System.out.println(alloc);
        System.out.println(nd);
    }

	/**
	 * Requests resources for a customer loan.
	 * If the request leave the bank in a safe state, it is carried out.
	 * @param customerIndex  The customer's index (0-indexed).
	 * @param request        An array of the requested count for each resource.
	 * @return true if the requested resources can be loaned, else false.
	 */
	public synchronized boolean requestResources(int customerIndex, int[] request) {

        System.out.println("Customer " + customerIndex + " requested " + Arrays.toString(request));
        for (int i = 0; i < numberOfResources; i++) {
            if (request[i] > need[customerIndex][i]) {
                return false;
            }
            if (request[i] > available[i]) {
                return false;
            }
        }
        if (checkSafe(customerIndex, request)) {
            System.out.println("Checked safe.\n");
            for (int j = 0; j < numberOfResources ; j++) {
                available[j] -= request[j];
                allocation[customerIndex][j] += request[j];
                need[customerIndex][j] -= request[j];
            }
            return true;
        } else {
            System.out.println("Checked unsafe.\n");
            return false;
        }

	}

	/**
	 * Releases resources borrowed by a customer. Assume release is valid for simplicity.
	 * @param customerIndex  The customer's index (0-indexed).
	 * @param release        An array of the release count for each resource.
	 */
	public synchronized void releaseResources(int customerIndex, int[] release) {
        System.out.println("Released " + Arrays.toString(release) + " from Customer " + customerIndex);
        for (int j = 0; j < numberOfResources; j++) {
            available[j] += release[j];
            allocation[customerIndex][j] -= release[j];
            need[customerIndex][j] += release[j];
        }
    }

	/**
	 * Checks if the request will leave the bank in a safe state.
	 * @param customerIndex  The customer's index (0-indexed).
	 * @param request        An array of the requested count for each resource.
	 * @return true if the requested resources will leave the bank in a
	 *         safe state, else false
	 */
	private synchronized boolean checkSafe(int customerIndex, int[] request) {

        int[] temp_avail = new int[numberOfResources];
        int[][] temp_need = new int[numberOfCustomers][numberOfResources];
        int[][] temp_allocation = new int[numberOfCustomers][numberOfResources];
        int[] work = new int[numberOfCustomers];

        boolean[] finish = new boolean[numberOfCustomers];
        boolean possible;


        for (int j = 0; j < this.numberOfResources; j++) {
            temp_avail[j] = this.available[j] - request[j];
            work[j] = temp_avail[j];

            for (int i = 0; i < numberOfCustomers; i++) {
                if (i == customerIndex) {
                    temp_need[customerIndex][j] = this.need[customerIndex][j] - request[j];
                    temp_allocation[customerIndex][j] = this.allocation[customerIndex][j] + request[j];
                } else {
                    temp_need[i][j] = this.need[i][j];
                    temp_allocation[i][j] = this.allocation[i][j];
                }
            }
        }

        for (int i = 0; i < numberOfCustomers; i++) {
            finish[i] = false;
        }

        possible = true;

        while (possible) {
            possible = false;
            for (int i = 0; i < this.numberOfCustomers; i++) {

                boolean notExceed = true;

                for (int j = 0; j < this.numberOfResources; j++) {
                    if (temp_need[i][j] > work[j]) {
                        notExceed = false;
                    }
                }
                if (finish[i] == false && notExceed) {
                    possible = true;
                    for (int j = 0; j < this.numberOfResources; j++) {
                        work[j] += temp_allocation[i][j];
                    }
                    finish[i] = true;
                }
            }
        }
        // return (finish(all) == true)
        boolean safe = true;
        for (int i = 0; i < this.numberOfCustomers; i++) {
            if (finish[i] == false) {
                safe = false;
            }
        }
        return safe;
    }

	/**
	 * Parses and runs the file simulating a series of resource request and releases.
	 * Provided for your convenience.
	 * @param filename  The name of the file.
	 */
	public static void runFile(String filename) {

		try {
			BufferedReader fileReader = new BufferedReader(new FileReader(filename));

			String line = null;
			String [] tokens = null;
			int [] resources = null;

			int n, m;

			try {
				n = Integer.parseInt(fileReader.readLine().split(",")[1]);
			} catch (Exception e) {
				System.out.println("Error parsing n on line 1.");
				fileReader.close();
				return;
			}

			try {
				m = Integer.parseInt(fileReader.readLine().split(",")[1]);
			} catch (Exception e) {
				System.out.println("Error parsing n on line 2.");
				fileReader.close();
				return;
			}

			try {
				tokens = fileReader.readLine().split(",")[1].split(" ");
				resources = new int[tokens.length];
                for (int i = 0; i < tokens.length; i++)
					resources[i] = Integer.parseInt(tokens[i]);
            } catch (Exception e) {
				System.out.println("Error parsing resources on line 3.");
				fileReader.close();
				return;
			}

			Banker theBank = new Banker(resources, n);

			int lineNumber = 4;
			while ((line = fileReader.readLine()) != null) {
				tokens = line.split(",");
				if (tokens[0].equals("c")) {
					try {
						int customerIndex = Integer.parseInt(tokens[1]);
						tokens = tokens[2].split(" ");
						resources = new int[tokens.length];
						for (int i = 0; i < tokens.length; i++)
							resources[i] = Integer.parseInt(tokens[i]);
						theBank.setMaximumDemand(customerIndex, resources);
					} catch (Exception e) {
						System.out.println("Error parsing resources on line "+lineNumber+".");
						fileReader.close();
						e.printStackTrace();
						return;
					}
				} else if (tokens[0].equals("r")) {
					try {
						int customerIndex = Integer.parseInt(tokens[1]);
						tokens = tokens[2].split(" ");
						resources = new int[tokens.length];
						for (int i = 0; i < tokens.length; i++)
							resources[i] = Integer.parseInt(tokens[i]);
						theBank.requestResources(customerIndex, resources);
					} catch (Exception e) {
						System.out.println("Error parsing resources on line "+lineNumber+".");
						fileReader.close();
						e.printStackTrace();
						return;
					}
				} else if (tokens[0].equals("f")) {
					try {
						int customerIndex = Integer.parseInt(tokens[1]);
						tokens = tokens[2].split(" ");
						resources = new int[tokens.length];
						for (int i = 0; i < tokens.length; i++)
							resources[i] = Integer.parseInt(tokens[i]);
						theBank.releaseResources(customerIndex, resources);
					} catch (Exception e) {
						System.out.println("Error parsing resources on line "+lineNumber+".");
						fileReader.close();
						e.printStackTrace();
						return;
					}
				} else if (tokens[0].equals("p")) {
					theBank.printState();
				}
			}
			fileReader.close();
		} catch (IOException e) {
			System.out.println("Error opening: "+filename);
		}

	}

	/**
	 * Main function
	 * @param args  The command line arguments
	 */
	public static void main(String[] args) {
		if (args.length > 0) {
			runFile(args[0]);
		}
	}

}