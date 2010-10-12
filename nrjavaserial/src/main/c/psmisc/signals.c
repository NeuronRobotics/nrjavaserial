/* signals.c - signal name handling */

/* Copyright 1993-1995 Werner Almesberger. See file COPYING for details. 
psmisc (fuser, killall and pstree) program code, documentation and
auxiliary programs are
Copyright 1993-1998 Werner Almesberger.
All rights reserved.

Redistribution and use in source and binary forms of parts of or the
whole original or derived work are permitted provided that the
original work is properly attributed to the author. The name of the
author may not be used to endorse or promote products derived from
this software without specific prior written permission. This work
is provided "as is" and without any express or implied warranties.

*/


#include <stdlib.h>
#include <stdio.h>
#include <string.h>
#include <ctype.h>
#include "signals.h"


typedef struct {
    int number;
    const char *name;
} SIGNAME;


static SIGNAME signals[] = {
#include "signames.h"
  { 0,NULL }};


extern void list_signals(void)
{
    SIGNAME *walk;
    int col;

    col = 0;
    for (walk = signals; walk->name; walk++) {
	if (col+strlen(walk->name)+1 > 80) {
	    putchar('\n');
	    col = 0;
	}
	printf("%s%s",col ? " " : "",walk->name);
	col += strlen(walk->name)+1;
    }
    putchar('\n');
}


extern int get_signal(char *name,const char *cmd)
{
    SIGNAME *walk;

    if (isdigit(*name))
	return atoi(name);
    for (walk = signals; walk->name; walk++)
	if (!strcmp(walk->name,name)) break;
    if (walk->name) return walk->number;
    fprintf(stderr,"%s: unknown signal; %s -l lists signals.\n",name,cmd);
    exit(1);
}
