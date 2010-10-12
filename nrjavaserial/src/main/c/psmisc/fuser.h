/* fuser.c - identify processes using files */

/* Copyright 1993-1998 Werner Almesberger. See file COPYING for details. 
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

#ifndef FUSER_H
#define FUSER_H


extern void show_user(char[]);
extern void parse_args(char *);
extern void scan_fd(void);
extern void scan_mounts(void);
extern void scan_swaps(void);
extern void show_files(void);

#endif FUSER_H
