/* fuser.c - identify processes using files */

/* Copyright 1993-1998 Werner Almesberger. See file COPYING for details. */


#include <stdlib.h>
#include <stdio.h>
#include <string.h>
#include <ctype.h>
#include <unistd.h>
#include <dirent.h>
#include <pwd.h>
#include <signal.h>
#include <limits.h>
#include <netdb.h>
#include <sys/types.h>
#include <sys/stat.h>
#include <netinet/in.h>
#include <arpa/inet.h>
#include <linux/kdev_t.h> /* for MKDEV */
#include <linux/major.h> /* for LOOP_MAJOR */

#include "comm.h"
#include "signals.h"
#include "fuser.h"


int main(int argc,char **argv)
{
    
    show_user(argv[1]);
    exit(0);
}
