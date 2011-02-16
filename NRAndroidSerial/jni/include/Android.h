#define DEBUG 1
#include <sys/utsname.h>
#define UTS_RELEASE "2.6.17-1.2630.fc6"
#include "/usr/include/linux/serial.h"
#include "config.h"
#include "gnu_io_RXTXPort.h"
#include <time.h>
#include <stdio.h>
#include <string.h>
#include <stdlib.h>
#include <errno.h>
#include <limits.h>
#include <sys/stat.h>

/*  FIXME  returns 0 in all cases on win32
#define S_ISCHR(m)	(((m)&S_IFMT) == S_IFCHR)
*/
#	if !defined(S_ISCHR)
#		define S_ISCHR(m) (1)
#	endif /* S_ISCHR(m) */

#ifdef HAVE_TERMIOS_H
#	include <termios.h>
#endif /* HAVE_TERMIOS_H */
#   include <signal.h>
#ifdef HAVE_SIGNAL_H
#   include <signal.h>
#endif /* HAVE_SIGNAL_H */
#ifdef HAVE_SYS_SIGNAL_H
#   include <sys/signal.h>
#endif /* HAVE_SYS_SIGNAL_H */
#include <sys/types.h>
#ifdef HAVE_SYS_TIME_H
#   include <sys/time.h>
#endif /* HAVE_SYS_TIME_H */
#   include <fcntl.h>
#ifdef HAVE_SYS_FCNTL_H
#   include <sys/fcntl.h>
#endif /* HAVE_SYS_FCNTL_H */
#ifdef HAVE_SYS_FILE_H
#   include <sys/file.h>
#endif /* HAVE_SYS_FILE_H */
#ifdef LFS  /* File Lock Server */
#	include <sys/socket.h>
#	include <netinet/in.h>
#	include <arpa/inet.h>
#endif /* FLS */

#include <linux/types.h> /* fix for linux-2.3.4? kernels */

//#include <linux/serial.h>
#include <linux/version.h>


/* FIXME -- new file */

#ifdef HAVE_PWD_H
#include	<pwd.h>
#endif /* HAVE_PWD_H */
#ifdef HAVE_GRP_H
#include 	<grp.h>
#endif /* HAVE_GRP_H */
#include <math.h>
#ifdef LIBLOCKDEV
#include	<lockdev.h>
#endif /* LIBLOCKDEV */

//extern int errno;

#include "SerialImp.h"
