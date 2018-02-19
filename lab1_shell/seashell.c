#include <stdio.h>
#include <stdlib.h>
#include <errno.h>
#include <unistd.h>
#include <string.h>
#include <ctype.h>

#define MAX_INPUT 1024
#define HISTORY_BUF_SIZE 15
#define DELIMITER " \t\r\n\a"

// defintions
int exec_cd(char* cmdPntr);
int exec_history();
char** get_args(char* input);
int store(char* cmd);
int hist_count = 0;
char history[HISTORY_BUF_SIZE][MAX_INPUT];

int main() {

    char command[MAX_INPUT], copy[MAX_INPUT];
    char directory[128];
    char** args;
    int position = 0;
    int digits = 0;

    while(1) {     

        printf("seashell>");
        fgets(command, MAX_INPUT, stdin);
        strcpy(copy, command);
        copy[strlen(copy)-1] = '\0';
        args = get_args(copy);
        if (strcmp(copy, "cd") == 0) {
            if (args[1] != NULL) {
                exec_cd(args[1]);
            } else {
                printf("no args\n");
            }
        } else if (strcmp(copy, "history") == 0) {
            exec_history();
        } else if (strcmp(copy, "!!") == 0) {
            if (hist_count != 0) {
                system(history[hist_count-1]);
            } else {    
                printf("history list empty\n");
            }
        } else if (isdigit(copy[0])) {
            for (int i=0; i<strlen(copy); i++) {
                if (!isdigit(copy[i])) {
                    system(copy);
                    digits = 0;
                    break;
                } else if (isdigit(copy[i])) {
                    digits++;
                }
            }

            if (digits == strlen(copy)) {
                if (atoi(copy) == 0) {
                    printf("invalid integer\n");
                } else if (atoi(copy) <= hist_count) {
                        system(history[hist_count - atoi(copy)]);
                } else {
                    if (atoi(copy)/10 %10 == 1) {
                        printf("there is no %ith most recent command\n", atoi(copy));
                    } else if (atoi(copy) % 10 == 3) {
                        printf("there is no %ird most recent command\n", atoi(copy));
                    } else if (atoi(copy) % 10 == 2) {
                        printf("there is no %ind most recent command\n", atoi(copy));
                    } else if (atoi(copy) % 10 == 1) {
                        printf("there is no %ist most recent command\n", atoi(copy));
                    } else {
                        printf("there is no %ith most recent command\n", atoi(copy));
                    }
                }
                digits = 0;
            }
               
        } else {
            if (system(command) == 0) {
                store(command);
            }
        }  
    }
    return 0;
}

char** get_args(char* input) {

    int position = 0;
    char* token;
    char** tokens = malloc(64*sizeof(char*));

    token = strtok(input, DELIMITER);

    do {
        tokens[position] = token;
        position++;
        token = strtok(NULL, DELIMITER);
    } while (token != NULL);
    return tokens;
}

int exec_cd(char* cmdPntr) {

    char path[1024];
    char* pwd;

    if (strcmp(cmdPntr, "-") == 0) {
        pwd = getcwd(path,1023);
        while (strcmp(pwd, "/home") != 0) {
            chdir("..");
            pwd = getcwd(path, 1023);
        }
    } else if (chdir(cmdPntr) != 0) {
       perror("Unable to cd");
       return -1;
    }

    printf("directory changed to: [%s]\n", getcwd(path, 1023)); 
    return 0;
}

int exec_history() {
    printf("-------------\n");
    printf("id    command\n");
    printf("-------------\n");
    for (int i=0; i < hist_count; i++) {
        printf("%i     %s\n",i,history[i]);
    }
    return 0;
}

int store(char* cmd) {
    strcpy(history[hist_count], cmd);
    hist_count++;
    return 0;
}