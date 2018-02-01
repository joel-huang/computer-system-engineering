# computer-systems-engineering
# C-shell
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
