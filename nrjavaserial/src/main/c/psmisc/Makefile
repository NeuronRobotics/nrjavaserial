CC=cc
CFLAGS=-O -DPSMISC_VERSION=\"`cat VERSION`\" \
  -D_GNU_SOURCE \
  -Wall -Wno-parentheses -Wwrite-strings -Wpointer-arith \
  # -Wcast-align -Wconversion -g
LDFLAGS=#-s -N #-Xlinker -qmagic
REAL_CPP=/lib/cpp
LIBTOOL= libtool 
LINK=$(LIBTOOL) --mode=link $(CC) $(CFLAGS) $(LDFLAGS) --version-info 1:0:1
PROGS=killall pstree fuser
LIBS=libpsmisc.la
EBINDIR=/bin		# essential binaries
BINDIR=/usr/bin		# not so essential ones
MANDIR=/usr/man/man1	# all man pages
LIBDIR=/usr/lib         # optional libraries

all:		$(LIBS) testing

signames.h:	/usr/include/signal.h
		$(REAL_CPP) -dM </usr/include/signal.h | \
		tr -s '\t ' '  ' | sort -n +2 | sed \
	's:#define SIG\([A-Z]\+[0-9]*\) \([0-9]\+\) *\(\|/\*.*\)$$:{\
\2,"\1" },:p;d' \
		  > signames.h

signals.o:	signals.h signals.c signames.h Makefile

fuser.o:	fuser.c comm.h signals.h fuser.h Makefile VERSION
killall.o:	killall.c comm.h signals.h Makefile VERSION

fuser:		fuser.o signals.o
killall:	killall.o signals.o

pstree:		pstree.c comm.h Makefile VERSION
		$(CC) $(CFLAGS) $(LDFLAGS) -o pstree pstree.c -ltermcap

testing:	testing.c libpsmisc.la
	$(CC) $(CFLAGS)  -lpsmisc -o testing testing.c 

libpsmisc.la:	fuser.lo signals.lo
	$(LINK) -o libpsmisc.la -rpath $(LIBDIR) fuser.lo signals.lo
fuser.lo:	fuser.c fuser.h comm.h signals.h  Makefile
	$(LIBTOOL) --mode=compile $(CC)  $(CFLAGS)  -c fuser.c
signals.lo:	signals.c signals.h comm.h signals.h Makefile
	$(LIBTOOL) --mode=compile $(CC)  $(CFLAGS)  -c signals.c

install:	$(PROGS)
#		EBINDIR is expected to exist, so we don't try to create it
		install -o 0 -g 0 -m 555 fuser $(INSTPREFIX)/$(EBINDIR)
		[ -d $(BINDIR) ] || install -d -o 0 -g 0 -m 755 $(INSTPREFIX)/$(BINDIR)
		install -o 0 -g 0 -m 555 killall $(INSTPREFIX)/$(BINDIR)
		install -o 0 -g 0 -m 555 pstree $(INSTPREFIX)/$(BINDIR)
		[ -d $(MANDIR) ] || install -d -o 0 -g 0 -m 755 $(INSTPREFIX)/$(MANDIR)
		install -o 0 -g 0 -m 444 fuser.1 $(INSTPREFIX)/$(MANDIR)
		install -o 0 -g 0 -m 444 killall.1 $(INSTPREFIX)/$(MANDIR)
		install -o 0 -g 0 -m 444 pstree.1 $(INSTPREFIX)/$(MANDIR)

install-pidof:
		@[ -x `echo $(BINDIR)`/killall ] || { \
		  echo "Need `echo $(BINDIR)`/killall to install pidof"; \
		  exit 1; }
		ln -sf killall `echo $(BINDIR)`/pidof
		install -o 0 -g 0 -m 444 pidof.1 $(MANDIR)

clean:
		rm -f *.o signames.h
		rm -rf .libs/*

spotless:	clean
		rm -f $(PROGS)
		rm -f testing
		rm -f *.l?
		rm -f core 
