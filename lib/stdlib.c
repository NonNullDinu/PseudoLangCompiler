/*
 * Copyright (C) 2018-2019  Dinu Blanovschi
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

#include <stdlib.h>
#include <stdio.h>

long long MERGE_MEMORY[1<<18];

int (*comp_func)(void* a, void* b);

int _ll_i_cmp(void* a, void* b){
	return *((long long*)a) - *((long long*)b);
}

int _ll_i_cmp_less(void* a, void *b){
	return *((long long*)b) - *((long long*)a);
}

void _merge(register long long* a, register long long *b, register long long s1, register long long s2){
	register long long i=0, j=0, k=0;
	while(i < s1 && j < s2){
		if(comp_func((a+i), (b+j)) < 0){
			MERGE_MEMORY[k++] = *(a+(i++));
		}
		else{
			MERGE_MEMORY[k++] = *(b+(j++));
		}
	}
	if(i == s1){
		while(j < s2){
			MERGE_MEMORY[k++] = *(b+(j++));
		}
	}
	else{
		while(i < s1){
			MERGE_MEMORY[k++] = *(a+(i++));
		}
	}
	k = 0;
	for(i = 0; i < s1; i++){
		*(a+i) = MERGE_MEMORY[k++];
	}
	for(j = 0; j < s2; j++){
		*(b+j) = MERGE_MEMORY[k++];
	}
}

void _merge_sort(register long long* a, register long long s){
	if(s <= 1)
		return;
	register int middlepos = s / 2;
	register long long *midadr = a + middlepos;
	_merge_sort(a, middlepos);
	_merge_sort((void*)(a + middlepos), s - middlepos);
	_merge(a,  midadr, middlepos, s - middlepos);
}

void _prepare_for_sort(register int (*comp)(void* a, void* b)){
	comp_func = comp;
}