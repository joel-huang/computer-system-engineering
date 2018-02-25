# computer-systems-engineering
# lab1 - cshell
* Test cases for commands implemented: all system() executable commands, history, cd, any integer

![Demo](https://github.com/joel-huang/computer-systems-engineering/blob/master/shell_demo.jpg "Demo") 

* Functions
`int main()` Main loop. Enables the special commands cd [dir], !!, history, and [any integer].  
`int exec_cd(char* cmdPntr)` Adds shortcut to cd to /home, and handles the cd input.  
`int exec_history()` Prints the history of commands entered.  
`char** get_args(char* input)` Obtains the arguments from the input string, delimited by ‘ \t\r\n\a’.  
`int store(char* cmd)` Stores a command into history.  


* Notes
1. Shell can handle integer inputs such as 000001 and 0.
2. Shell is not fail-proof: history saves blank spaces and tabs into history because they are valid commands that cause system() to return 0. One way to circumvent this would be to check for a printed statement instead of checking for a return value of 0.
3. Memory allocation can be improved dynamically.

#lab2 - Multithread
* These running times were recorded based on the best of 5 trials, for every thread count.

* Overall, the running time is proportional to thread count. In MeanThread.java, the running time increased from 1>2>4>8 threads but saw a small improvement at 16 and 32 threads, after which increasing the thread count was detrimental to running time. In MedianThread.java, the running time actually decreased from 1>2>4 threads, then was fairly constant from 8>16>32>64>128 threads, after which the same phenomena occurred where increasing thread count was detrimental to running time.

* This could be due to thread creation: after a certain threshold, the overhead of setting up new threads may outweigh the relatively small time saved by computing in different threads.

* Also, after running the programs multiple times, the running time seems to have a high variance for larger thread counts, with deviations of ±100-150ms being fairly common.

#lab3 - Banker's algorithm

* The time complexity of Banker’s algorithm is O(n^2 m).
1. Banker(): O(1)
2. printState(): O(n)
3. requestResources() – includes one call to checkSafe(): O(n^2 m)
4. releaseResources(): O(m)
5. checkSafe() – due to while loop with O(n) containing nested for loop with O(nm): O(n^2 m)

* Total: O(n^2 m)
