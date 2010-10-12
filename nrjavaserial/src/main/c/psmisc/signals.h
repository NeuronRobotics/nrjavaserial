/* signals.h - signal name handling */

/* Copyright 1993-1995 Werner Almesberger. See file COPYING for details. */


#ifndef SIGNALS_H
#define SIGNALS_H
extern void list_signals(void);
extern int get_signal(char *name,const char *cmd);
#endif
