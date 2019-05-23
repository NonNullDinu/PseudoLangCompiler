#include <unistd.h>
#include <fcntl.h>
#include <stdlib.h>
#include <stdio.h>

FILE *out;

int main(){
    out = fopen("constants", "w");
    fprintf(out, "O_CREAT:%d\n", O_CREAT);
    fprintf(out, "O_WRONLY:%d\n", O_WRONLY);
    fprintf(out, "O_RDONLY:%d\n", O_RDONLY);
    fprintf(out, "O_TRUNC:%d\n", O_TRUNC);
    fclose(out);
    return 0;
}