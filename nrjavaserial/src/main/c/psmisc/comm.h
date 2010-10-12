/* comm.h - command name length definition */
 
/* Copyright 1995 Werner Almesberger. See file COPYING for details. */
 

#ifndef COMM_H
#define COMM_H

#if 0 /* broken in 1.3.xx */
#include <linux/sched.h>
#define COMM_LEN sizeof(dummy.comm)
extern struct task_struct dummy;
#else
#define COMM_LEN 16 /* synchronize with size of comm in struct task_struct in
		       /usr/include/linux/sched.h */
#endif

#endif
