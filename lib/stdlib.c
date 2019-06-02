#include <stdlib.h>
#include <stdio.h>

long long MERGE_MEMORY[65536];

void merge(register long long* a, register long long *b, register long long s1, register long long s2){
	register long long i=0, j=0, k=0;
	register long long* mem = MERGE_MEMORY;
	while(i < s1 && j < s2){
		if(*(a+i) < *(b+j)){
			mem[k++] = *(a+(i++));
		}
		else{
			mem[k++] = *(b+(j++));
		}
	}
	if(i == s1){
		while(j < s2){
			mem[k++] = *(b+(j++));
		}
	}
	else{
		while(i < s1){
			mem[k++] = *(a+(i++));
		}
	}
	k = 0;
	for(i = 0; i < s1; i++){
		*(a+i) = mem[k++];
	}
	for(j = 0; j < s2; j++){
		*(b+j) = mem[k++];
	}
}

void merge_sort(register void* a, register long long s){
	if(s <= 1)
		return;
	register long long middlepos = s / 2;
	register long long *midadr = (long long*)a + middlepos;
	merge_sort(a, middlepos);
	merge_sort((void*)((long long*)a + middlepos), s - middlepos);
	merge(a,  midadr, middlepos, s - middlepos);
}
