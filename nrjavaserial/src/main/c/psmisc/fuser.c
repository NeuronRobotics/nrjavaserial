#ifndef __linux__
JNIEXPORT jstring JNICALL Java_javax_comm_CommPortIdentifier_native_1psmisc_1report_1owner (JNIEnv *env, jobject obj, jstring arg)
{
	return (*env)->NewStringUTF(env, "Unknown Application");
}
#else

/* loosly based on fuser.c by Werner Almesberger. */
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
#define PROC_BASE  "/proc"
#define UID_UNKNOWN -1
#define NAME_FIELD 20 /* space reserved for file name */

#define MAX_LINE 256 /* longest line we may ever find in /proc */


#ifndef LOOP_MAJOR /* don't count on the headers too much ... */
#define LOOP_MAJOR 7
#endif


#define REF_FILE   1	/* an open file */
#define REF_ROOT   2	/* current root */
#define REF_CWD    4	/* current directory */
#define REF_EXE    8	/* executable */
#define REF_MMAP  16	/* mmap'ed file or library */

#define FLAG_KILL  1	/* kill process */
#define FLAG_UID   2	/* show uid */
#define FLAG_VERB  4	/* show verbose output */
#define FLAG_DEV   8	/* show all processes using this device */
#define FLAG_ASK  16	/* ask before killing a process */



typedef struct _net_cache {
    int lcl_port;
    int rmt_port;
    unsigned long rmt_addr;
    ino_t ino;
    struct _net_cache *next;
} NET_CACHE;

typedef struct _unix_cache {
    dev_t fs_dev;
    ino_t fs_ino;
    ino_t net_ino;
    struct _unix_cache *next;
} UNIX_CACHE;

typedef struct {
    const char *name;
    NET_CACHE *cache;
    int once;
} SPACE_DSC;

typedef enum { it_proc,it_mount,it_loop,it_swap } ITEM_TYPE;

typedef struct item_dsc {
    ITEM_TYPE type;
    union {
	struct {
	    pid_t pid;
	    int uid; /* must also accept UID_UNKNOWN */
	    int ref_set;
	} proc;
	struct {
	    const char *path;
	} misc;
    } u;
    struct item_dsc *next;
} ITEM_DSC;

typedef struct file_dsc {
    const char *name;  /* NULL if previous entry has name */
    dev_t dev;
    ino_t ino;
    int flags,sig_num;
    SPACE_DSC *name_space; /* or NULL if no indication */
    ITEM_DSC *items;
    struct file_dsc *named,*next;
} FILE_DSC;

static SPACE_DSC name_spaces[] = {
    { "file", NULL, 0 }, /* must be first */
    { "tcp",  NULL, 0 },
    { "udp",  NULL, 0 },
    { NULL,   NULL, 0 }
};


static FILE_DSC *files = NULL;
static FILE_DSC *last_named = NULL;
static UNIX_CACHE *unix_cache = NULL;
static int all = 0,found_item = 0;

static void fill_net_cache(SPACE_DSC *dsc)
{
    FILE *file;
    NET_CACHE *new,*last;
    char buffer[PATH_MAX+1],line[MAX_LINE+1];

    if (dsc->once) return;
    dsc->once = 1;
    sprintf(buffer,PROC_BASE "/net/%s",dsc->name);
    if (!(file = fopen(buffer,"r"))) {
	perror(buffer);
	exit(1);
    }
    last = NULL;
    (void) fgets(line,MAX_LINE,file);
    while (fgets(line,MAX_LINE,file)) {
	new = malloc(sizeof(NET_CACHE));
	if (!new) {
	    perror("malloc");
	    exit(1);
	}
	if (sscanf(line,"%*d: %*x:%x %lx:%x %*x %*x:%*x %*x:%*x %*x %*d %*d "
	  "%ld",&new->lcl_port,&new->rmt_addr,&new->rmt_port,&new->ino) != 4) {
	    free(new);
	    continue;
	}
	if (!new->ino) {
	    free(new);
	    continue;
	}
	new->next = NULL;
	if (last) last->next = new;
	else dsc->cache = new;
	last = new;
    }
    (void) fclose(file);
}


static void fill_unix_cache(void)
{
    static int once;
    FILE *file;
    UNIX_CACHE *new,*last;
    struct stat st;
    char path[PATH_MAX+1],line[MAX_LINE+1];
    int ino;

    if (once) return;
    once = 1;
    if (!(file = fopen(PROC_BASE "/net/unix","r"))) {
	perror(PROC_BASE "/net/unix");
	exit(1);
    }
    last = NULL;
    (void) fgets(line,MAX_LINE,file);
    while (fgets(line,MAX_LINE,file)) {
	if (sscanf(line,"%*x: %*x %*x %*x %*x %*x %d %s",&ino,path) != 2)
	    continue;
	if (stat(path,&st) < 0) continue;
	new = malloc(sizeof(UNIX_CACHE));
	new->fs_dev = st.st_dev;
	new->fs_ino = st.st_ino;
	new->net_ino = ino;
	new->next = NULL;
	if (last) last->next = new;
	else unix_cache = new;
	last = new;
    }
    (void) fclose(file);
    
}


static unsigned long try_to_find_unix_dev(unsigned long inode)
{
    UNIX_CACHE *walk;

    for (walk = unix_cache; walk; walk = walk->next)
	if (walk->net_ino == inode) return walk->fs_dev;
    return 0;
}


static void add_file(const char *path,unsigned long device,unsigned long inode, pid_t pid,int ref)
{
    struct stat st;
    FILE_DSC *file,*next;
    ITEM_DSC **item,*this;
    unsigned long mount_dev;

    if (device) mount_dev = device;
    else mount_dev = try_to_find_unix_dev(inode);
    for (file = files; file; file = next) {
	next = file->next;
	if (file->flags & FLAG_DEV ? mount_dev && mount_dev == file->dev :
	  device == file->dev && inode == file->ino) {
	    if (!file->name) file = file->named;
	    for (item = &file->items; *item; item = &(*item)->next)
		if ((*item)->type == it_proc && (*item)->u.proc.pid >= pid)
		    break;
	    if (*item && (*item)->u.proc.pid == pid) this = *item;
	    else {
		if (!(this = malloc(sizeof(ITEM_DSC)))) {
		    perror("malloc");
		    exit(1);
		}
		this->type = it_proc;
		this->u.proc.pid = pid;
		this->u.proc.uid = UID_UNKNOWN;
		this->u.proc.ref_set = 0;
		this->next = *item;
		*item = this;
		found_item = 1;
	    }
	    this->u.proc.ref_set |= ref;
	    if ((file->flags & (FLAG_UID | FLAG_VERB)) && this->u.proc.uid == UID_UNKNOWN && lstat(path,&st) >= 0) 
		this->u.proc.uid = st.st_uid;
	}
    }
}


static void add_other(ITEM_TYPE type,unsigned long mount_dev,
  unsigned long device,unsigned long inode,const char *path)
{
    FILE_DSC *file,*next;
    ITEM_DSC **item,*this;

    for (file = files; file; file = next) {
	next = file->next;
	if (file->flags & FLAG_DEV ? mount_dev == file->dev :
	  device == file->dev && inode == file->ino) {
	    if (!file->name) file = file->named;
	    for (item = &file->items; *item; item = &(*item)->next);
	    /* there's no easy way to suppress duplicates, so we don't */
	    if (!(this = malloc(sizeof(ITEM_DSC)))) {
		perror("malloc");
		exit(1);
	    }
	    this->type = type;
	    if (!(this->u.misc.path = strdup(path))) {
		perror("strdup");
		exit(1);
	    }
	    this->next = *item;
	    *item = this;
	    found_item = 1;
	}
    }
}


static void check_link(const char *path,pid_t pid,int type)
{
    struct stat st;

    if (stat(path,&st) >= 0)
	add_file(path,st.st_dev,st.st_ino,pid,type);
}


static void check_map(const char *rel,pid_t pid,int type)
{
    FILE *file;
    char line[MAX_LINE+1];
    int major,minor;
    unsigned long inode;

    if (!(file = fopen(rel,"r"))) return;
    while (fgets(line,MAX_LINE,file)) {
	if (sscanf(line,"%*s %*s %*s %x:%x %ld",&major,&minor,&inode) != 3)
	    continue;
	if (major || minor || inode)
	    add_file(rel,MKDEV(major,minor),inode,pid,type);
    }
    fclose(file);
}


static void check_dir(const char *rel,pid_t pid,int type)
{
    DIR *dir;
    struct dirent *de;
    char path[PATH_MAX+1];

    if (!(dir = opendir(rel))) return;
    while (de = readdir(dir))
	if (strcmp(de->d_name,".") && strcmp(de->d_name,"..")) {
	    sprintf(path,"%s/%s",rel,de->d_name);
	    check_link(path,pid,type);
	}
    (void) closedir(dir);
}


extern void scan_fd(void)
{
    DIR *dir;
    struct dirent *de;
    char path[PATH_MAX+1];
    pid_t pid;
    int empty;

    if (!(dir = opendir(PROC_BASE))) {
	perror(PROC_BASE);
	exit(1);
    }
    empty = 1;
    while (de = readdir(dir))
	if (pid = atoi(de->d_name)) {
	    empty = 0;
	    sprintf(path,"%s/%d",PROC_BASE,pid);
	    if (chdir(path) >= 0) {
		check_link("root",pid,REF_ROOT);
		check_link("cwd",pid,REF_CWD);
		check_link("exe",pid,REF_EXE);
		check_dir("lib",pid,REF_MMAP);
		check_dir("mmap",pid,REF_MMAP);
		check_map("maps",pid,REF_MMAP);
		check_dir("fd",pid,REF_FILE);
	    }
	}
    (void) closedir(dir);
    if (empty) {
	fprintf(stderr,PROC_BASE " is empty (not mounted ?)\n");
	exit(1);
    }
}


extern void scan_mounts(void)
{
    FILE *file;
    struct stat st_dev,st_parent,st_mounted;
    char line[MAX_LINE+1],path[PATH_MAX+1],mounted[PATH_MAX+3];
    char tmp[MAX_LINE+1];
    char *end;

    if (!(file = fopen(PROC_BASE "/mounts","r"))) return; /* old kernel */
    while (fgets(line,MAX_LINE,file)) {
	if (sscanf(line,"%s %s",path,mounted) != 2) continue;
		/* new kernel :-) */
	if (stat(path,&st_dev) < 0) continue; /* might be NFS or such */
	if (S_ISBLK(st_dev.st_mode) && MAJOR(st_dev.st_rdev) == LOOP_MAJOR) {
	    FILE *pipe;

	    sprintf(tmp,"losetup %s",path);
	    if (!(pipe = popen(tmp,"r")))
		fprintf(stderr,"popen(%s) failed\n",tmp);
	    else {
		int dev,ino;

		if (fscanf(pipe,"%*s [%x]:%d",&dev,&ino) == 2)
		    add_other(it_loop,dev,dev,ino,path);
		(void) fclose(pipe);
	    }
	}
	if (stat(mounted,&st_mounted) < 0) {
	    perror(mounted);
	    continue;
	}
	end = strchr(mounted,0);
	strcpy(end,"/..");
	if (stat(mounted,&st_parent) >= 0) {
	    *end = 0;
	    add_other(it_mount,st_parent.st_dev,st_mounted.st_dev,
	      st_mounted.st_ino,mounted);
	}
    }
    (void) fclose(file);
}


extern void scan_swaps(void)
{
    FILE *file;
    struct stat st;
    char line[MAX_LINE+1],path[PATH_MAX+1],type[MAX_LINE+1];

    if (!(file = fopen(PROC_BASE "/swaps","r"))) return; /* old kernel */
    (void) fgets(line,MAX_LINE,file);
    while (fgets(line,MAX_LINE,file)) {
	if (sscanf(line,"%s %s",path,type) != 2) continue; /* new kernel :-) */
	if (strcmp(type,"file")) continue;
	if (stat(path,&st) >= 0)
	    add_other(it_swap,st.st_dev,st.st_dev,st.st_ino,path);
    }
    (void) fclose(file);
}


static int ask(pid_t pid)
{
    int ch,c;

    fflush(stdout);
    do {
	fprintf(stderr,"Kill process %d ? (y/n) ",pid);
	fflush(stderr);
	do if ((ch = getchar()) == EOF) exit(0);
	while (ch == '\n' || ch == '\t' || ch == ' ');
	do if ((c = getchar()) == EOF) exit(0);
	while (c != '\n');
    }
    while (ch != 'y' && ch != 'n' && ch != 'Y' && ch != 'N');
    return ch == 'y' || ch == 'Y';
}


extern void show_user(char tstring[])
{
    const ITEM_DSC *item;
    FILE *f;
    const struct passwd *pw;
    const char *user,*scan;
    char tmp[10],path[PATH_MAX+1],comm[COMM_LEN+1];
    int length,dummy;
    pid_t self;
    const char *name;
    int uid;

    parse_args(tstring);
    scan_fd();
    scan_mounts();
    scan_swaps();
    if (seteuid(getuid()) < 0) {
            perror("seteuid");
            return 1;
    }
    self = getpid();
	if (files->name && (files->items || all)) {
	    printf("DEVICE               USER        PID ACCESS COMMAND\n");
	    length = 0;
	    for (scan = files->name; *scan; scan++)
		if (*scan == '\\') length += printf("\\\\");
		else if (*scan > ' ' && *scan <= '~') {
			putchar(*scan);
			length++;
		    }
		    else length += printf("\\%03o",*scan);
	    if (files->name_space)
		length += printf("/%s",files->name_space->name);
	    while (length < NAME_FIELD) {
		putchar(' ');
		length++;
	    }
	    for (item = files->items; item; item = item->next) {
		    sprintf(path,PROC_BASE "/%d/stat",item->u.proc.pid);
		    strcpy(comm,"???");
		    if (f = fopen(path,"r")) {
			(void) fscanf(f,"%d (%[^)]",&dummy,comm);
			(void) fclose(f);
		    }
		    name = comm;
		    uid = item->u.proc.uid;
		    if (uid == UID_UNKNOWN) user = "???";
		    else if (pw = getpwuid(uid)) user = pw->pw_name;
			else {
			    sprintf(tmp,"%d",uid);
			    user = tmp;
			}
		    if (length > NAME_FIELD)
			    printf("\n%*s",NAME_FIELD,"");
		    printf(" %-8s ",user);
		    printf("%6d %c%c%c%c%c  ",item->u.proc.pid,
		       item->u.proc.ref_set & REF_FILE ? 'f' : '.',
		       item->u.proc.ref_set & REF_ROOT ? 'r' : '.',
		       item->u.proc.ref_set & REF_CWD ? 'c' : '.',
		       item->u.proc.ref_set & REF_EXE ? 'e' : '.',
		       (item->u.proc.ref_set & REF_MMAP) &&
		       !(item->u.proc.ref_set & REF_EXE) ? 'm' : '.');
		    if (name)
			for (scan = name; *scan; scan++)
			    if (*scan == '\\') printf("\\\\");
			    else if (*scan > ' ' && *scan <= '~')
				    putchar(*scan);
				else printf("\\%03o",(unsigned char) *scan);
		    putchar('\n');
		}
	}
}

static void show_files(void)
{
    const FILE_DSC *file;
    const ITEM_DSC *item;
    FILE *f;
    const struct passwd *pw;
    const char *user,*scan;
    char tmp[10],path[PATH_MAX+1],comm[COMM_LEN+1];
    int length,header,first,dummy;
    pid_t self;

    self = getpid();
    header = 1;
    for (file = files; file; file = file->next)
	if (file->name && (file->items || all)) {
	    if (header && (file->flags & FLAG_VERB)) {
		printf("\n%*s USER        PID ACCESS COMMAND\n",NAME_FIELD,"");
		header = 0;
	    }
	    length = 0;
	    for (scan = file->name; *scan; scan++)
		if (*scan == '\\') length += printf("\\\\");
		else if (*scan > ' ' && *scan <= '~') {
			putchar(*scan);
			length++;
		    }
		    else length += printf("\\%03o",*scan);
	    if (file->name_space)
		length += printf("/%s",file->name_space->name);
	    if (!(file->flags & FLAG_VERB)) {
		putchar(':');
		length++;
	    }
	    while (length < NAME_FIELD) {
		putchar(' ');
		length++;
	    }
	    first = 1;
	    for (item = file->items; item; item = item->next) {
		if (!(file->flags & FLAG_VERB)) {
		    if (item->type != it_proc) continue;
		    if (item->u.proc.ref_set & REF_FILE)
			printf("%6d",item->u.proc.pid);
		    if (item->u.proc.ref_set & REF_ROOT)
			printf("%6dr",item->u.proc.pid);
		    if (item->u.proc.ref_set & REF_CWD)
			printf("%6dc",item->u.proc.pid);
		    if (item->u.proc.ref_set & REF_EXE)
			printf("%6de",item->u.proc.pid);
		    else if (item->u.proc.ref_set & REF_MMAP)
			printf("%6dm",item->u.proc.pid);
		    if ((file->flags & FLAG_UID) && item->u.proc.uid !=
		      UID_UNKNOWN)
			if (pw = getpwuid(item->u.proc.uid))
			    printf("(%s)",pw->pw_name);
			else printf("(%d)",item->u.proc.uid);
		    first = 0;
		}
		else {
		    const char *name;
		    int uid;

		    switch (item->type) {
			case it_proc:
			    sprintf(path,PROC_BASE "/%d/stat",item->u.proc.pid);
			    strcpy(comm,"???");
			    if (f = fopen(path,"r")) {
				(void) fscanf(f,"%d (%[^)]",&dummy,comm);
				(void) fclose(f);
			    }
			    name = comm;
			    uid = item->u.proc.uid;
			    break;
			case it_mount:
			case it_loop:
			case it_swap:
			    name = item->u.misc.path;
			    uid = 0;
			    break;
			default:
			    fprintf(stderr,"Internal error (type %d)\n",
			      item->type);
			    exit(1);
		    }
		    if (uid == UID_UNKNOWN) user = "???";
		    else if (pw = getpwuid(uid)) user = pw->pw_name;
			else {
			    sprintf(tmp,"%d",uid);
			    user = tmp;
			}
		    if (!first) printf("%*s",NAME_FIELD,"");
		    else if (length > NAME_FIELD)
			    printf("\n%*s",NAME_FIELD,"");
		    printf(" %-8s ",user);
		    switch (item->type) {
			case it_proc:
			    printf("%6d %c%c%c%c%c  ",item->u.proc.pid,
			      item->u.proc.ref_set & REF_FILE ? 'f' : '.',
			      item->u.proc.ref_set & REF_ROOT ? 'r' : '.',
			      item->u.proc.ref_set & REF_CWD ? 'c' : '.',
			      item->u.proc.ref_set & REF_EXE ? 'e' : '.',
			      (item->u.proc.ref_set & REF_MMAP) &&
			      !(item->u.proc.ref_set & REF_EXE) ? 'm' : '.');
			    break;
			case it_mount:
			    printf("kernel mount  ");
			    break;
			case it_loop:
			    printf("kernel loop   ");
			    break;
			case it_swap:
			    printf("kernel swap   ");
			    break;
		    }
		    if (name)
			for (scan = name; *scan; scan++)
			    if (*scan == '\\') printf("\\\\");
			    else if (*scan > ' ' && *scan <= '~')
				    putchar(*scan);
				else printf("\\%03o",(unsigned char) *scan);
		    putchar('\n');
		}
		first = 0;
	    }
	    if (!(file->flags & FLAG_VERB) || first) putchar('\n');
	    if (first)
		fprintf(stderr,"No process references; use -v for the complete"
		  " list\n");
	    if (file->flags & FLAG_KILL)
		for (item = file->items; item; item = item->next)
		    switch (item->type) {
			case it_proc:
		    	    if (item->u.proc.pid == self) continue;
			    if ((file->flags & FLAG_ASK) &&
			      !ask(item->u.proc.pid))
				continue;
			    if (kill(item->u.proc.pid,file->sig_num) >= 0)
				break;
			    sprintf(tmp,"kill %d",item->u.proc.pid);
			    perror(tmp);
			    break;
			case it_mount:
			    fprintf(stderr,"No automatic removal. Please use  "
			      "umount %s\n",item->u.misc.path);
			    break;
			case it_loop:
			    fprintf(stderr,"No automatic removal. Please use  "
			      "umount %s\n",item->u.misc.path);
			    break;
			case it_swap:
			    fprintf(stderr,"No automatic removal. Please use  "
			      "swapoff %s\n",file->name);
			    break;
		    }
	}
}

static void enter_item(const char *name,int flags,int sig_number,dev_t dev, ino_t ino,SPACE_DSC *name_space)
{
    static FILE_DSC *last = NULL;
    FILE_DSC *new;

    if (!(new = malloc(sizeof(FILE_DSC)))) {
	perror("malloc");
	exit(1);
    }
    if (last_named && !strcmp(last_named->name,name) &&
      last_named->name_space == name_space) new->name = NULL;
    else if (!(new->name = strdup(name))) {
	    perror("strdup");
	    exit(1);
	}
    new->flags = flags;
    new->sig_num = sig_number;
    new->items = NULL;
    new->next = NULL;
    new->dev = dev;
    new->ino = ino;
    new->name_space = name_space;
    if (last) last->next = new;
    else files = new;
    last = new;
    new->named = last_named;
    if (new->name) last_named = new;
}


static int parse_inet(const char *spec,const char *name_space,int *lcl_port,
  unsigned long *rmt_addr,int *rmt_port)
{
    char *s,*here,*next,*end;
    int port,field;

    if (!(s = strdup(spec))) {
	perror("strdup");
	exit(1);
    }
    *lcl_port = *rmt_port = -1;
    *rmt_addr = 0;
    field = 0;
    for (here = s; here; here = next ? next+1 : NULL) {
	next = strchr(here,',');
	if (next) *next = 0;
	switch (field) {
	    case 0:
		/* fall through */
	    case 2:
		if (!*here) break;
		port = strtoul(here,&end,0);
		if (*end) {
		    struct servent *se;

		    if (!(se = getservbyname(here,name_space)))
			return 0;
		    port = ntohs(se->s_port);
		}
		if (field) *rmt_port = port;
		else *lcl_port = port;
		break;
	    case 1:
		if (!*here) break;
		if ((long) (*rmt_addr = inet_addr(here)) == -1) {
		    struct hostent *hostent;

		    if (!(hostent = gethostbyname(here))) return 0;
		    if (hostent->h_addrtype != AF_INET) return 0;
		    memcpy(rmt_addr,hostent->h_addr,hostent->h_length);
		}
		break;
	    default:
		return 0;
	}
	field++;
    }
    return 1;
}


static void usage(void)
{
    fprintf(stderr,"usage: fuser [ -a | -s ] [ -n space ] [ -signal ] "
      "[ -kimuv ] name ...\n%13s[ - ] [ -n space ] [ -signal ] [ -kimuv ] "
      "name ...\n","");
    fprintf(stderr,"       fuser -l\n");
    fprintf(stderr,"       fuser -V\n\n");
    fprintf(stderr,"    -a        display unused files too\n");
    fprintf(stderr,"    -k        kill processes accessing that file\n");
    fprintf(stderr,"    -i        ask before killing (ignored without -k)\n");
    fprintf(stderr,"    -l        list signal names\n");
    fprintf(stderr,"    -m        mounted FS\n");
    fprintf(stderr,"    -n space  search in the specified name space (file, "
      "udp, or tcp)\n");
    fprintf(stderr,"    -s        silent operation\n");
    fprintf(stderr,"    -signal   send signal instead of SIGKILL\n");
    fprintf(stderr,"    -u        display user ids\n");
    fprintf(stderr,"    -v        verbose output\n");
    fprintf(stderr,"    -V        display version information\n");
    fprintf(stderr,"    -         reset options\n\n");
    fprintf(stderr,"  udp/tcp names: [local_port][,[rmt_host][,[rmt_port]]]"
      "\n\n");
    exit(1);
}

extern void parse_args(char *argv)
{
    SPACE_DSC *name_space;
    char path[PATH_MAX+1];
    int flags,silent,sig_number,no_files;
    SPACE_DSC *this_name_space;
    struct stat st;
    char *here;

    flags = silent = 0;
    sig_number = SIGKILL;
    name_space = name_spaces;
    no_files = 1;
    flags |= FLAG_UID;
    no_files = 0;
    last_named = NULL;
    this_name_space = name_space;
    if (name_space != name_spaces || stat(argv,&st) < 0) {
		here = strchr(argv,'/');
		if (here && here != argv) {
		    for (this_name_space = name_spaces; this_name_space->name;
		      this_name_space++)
			if (!strcmp(here+1,this_name_space->name)) {
			    *here = 0;
			    break;
			}
		    if (!this_name_space->name) this_name_space = name_spaces;
		}
	    }
    if (this_name_space == name_spaces) {
		if (stat(argv,&st) < 0) {
		    perror(argv);
		    exit(0);
		}
		if (flags & FLAG_DEV)
		    if (S_ISBLK(st.st_mode)) st.st_dev = st.st_rdev;
		    else if (S_ISDIR(st.st_mode)) {
			    sprintf(path,"%s/.",argv);
			    if (stat(argv,&st) < 0) {
				perror(argv);
				exit(0);
			    }
			}
		if (S_ISSOCK(st.st_mode) || (flags & FLAG_DEV))
		    fill_unix_cache();
		if (!S_ISSOCK(st.st_mode) || (flags & FLAG_DEV))
		    enter_item(argv,flags,sig_number,st.st_dev,st.st_ino,NULL);
		else {
		    UNIX_CACHE *walk;

		    for (walk = unix_cache; walk; walk = walk->next)
			if (walk->fs_dev == st.st_dev && walk->fs_ino ==
			  st.st_ino)
			    enter_item(argv,flags,sig_number,0,walk->net_ino,
			      NULL);
		}
	    }
	    else {
		NET_CACHE *walk;
		unsigned long rmt_addr;
		int lcl_port,rmt_port;

		if (flags & FLAG_DEV) {
		    fprintf(stderr,"ignoring -m in name space \"%s\"\n",
		      this_name_space->name);
		    flags &= ~FLAG_DEV;
		}
		fill_net_cache(this_name_space);
		if (!parse_inet(argv,this_name_space->name,&lcl_port,
		  &rmt_addr,&rmt_port)) {
		    fprintf(stderr,"%s/%s: invalid specificiation\n",argv,
		      this_name_space->name);
		    exit(0);
		}
		for (walk = this_name_space->cache; walk; walk = walk->next)
		    if ((lcl_port == -1 || walk->lcl_port == lcl_port) &&
		      (!rmt_addr || walk->rmt_addr == rmt_addr) &&
		      (rmt_port == -1 || walk->rmt_port == rmt_port))
			enter_item(argv,flags,sig_number,0,walk->ino,
			    this_name_space);
	    }
    if (no_files || (all && silent)) usage();
}


