/*-------------------------------------------------------------------------
|   RXTX License v 2.1 - LGPL v 2.1 + Linking Over Controlled Interface.
|   RXTX is a native interface to serial ports in java.
|   Copyright 1997-2009 by Trent Jarvi tjarvi@qbang.org and others who
|   actually wrote it.  See individual source files for more information.
|
|   A copy of the LGPL v 2.1 may be found at
|   http://www.gnu.org/licenses/lgpl.txt on March 4th 2007.  A copy is
|   here for your convenience.
|
|   This library is free software; you can redistribute it and/or
|   modify it under the terms of the GNU Lesser General Public
|   License as published by the Free Software Foundation; either
|   version 2.1 of the License, or (at your option) any later version.
|
|   This library is distributed in the hope that it will be useful,
|   but WITHOUT ANY WARRANTY; without even the implied warranty of
|   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
|   Lesser General Public License for more details.
|
|   An executable that contains no derivative of any portion of RXTX, but
|   is designed to work with RXTX by being dynamically linked with it,
|   is considered a "work that uses the Library" subject to the terms and
|   conditions of the GNU Lesser General Public License.
|
|   The following has been added to the RXTX License to remove
|   any confusion about linking to RXTX.   We want to allow in part what
|   section 5, paragraph 2 of the LGPL does not permit in the special
|   case of linking over a controlled interface.  The intent is to add a
|   Java Specification Request or standards body defined interface in the
|   future as another exception but one is not currently available.
|
|   http://www.fsf.org/licenses/gpl-faq.html#LinkingOverControlledInterface
|
|   As a special exception, the copyright holders of RXTX give you
|   permission to link RXTX with independent modules that communicate with
|   RXTX solely through the Sun Microsytems CommAPI interface version 2,
|   regardless of the license terms of these independent modules, and to copy
|   and distribute the resulting combined work under terms of your choice,
|   provided that every copy of the combined work is accompanied by a complete
|   copy of the source code of RXTX (the version of RXTX used to produce the
|   combined work), being distributed under the terms of the GNU Lesser General
|   Public License plus this exception.  An independent module is a
|   module which is not derived from or based on RXTX.
|
|   Note that people who make modified versions of RXTX are not obligated
|   to grant this special exception for their modified versions; it is
|   their choice whether to do so.  The GNU Lesser General Public License
|   gives permission to release a modified version without this exception; this
|   exception also makes it possible to release a modified version which
|   carries forward this exception.
|
|   You should have received a copy of the GNU Lesser General Public
|   License along with this library; if not, write to the Free
|   Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
|   All trademarks belong to their respective owners.
--------------------------------------------------------------------------*/

/**
 * \file slf4j.h
 *
 * \brief Functions to enable JNI code to log via SLF4J.
 */

#ifndef SLF4J_H
#define SLF4J_H

#include <jni.h>

/**
 * \brief The log levels used by SLF4J.
 */
typedef enum LogLevel
{
	LOG_ERROR,
	LOG_WARN,
	LOG_INFO,
	LOG_DEBUG,
	LOG_TRACE,
	LOG_LEVELS
} LogLevel;

/** \brief Trace-level log to denote entering a function. */
#define ENTER(x) slf4j_log(LOG_TRACE, "entering " x)
/** \brief Trace-level log to denote leaving a function. */
#define LEAVE(x) slf4j_log(LOG_TRACE, "leaving " x)

/**
 * \brief Log a ERROR-level message through SLF4J.
 *
 * \param [in] msg the message to log
 * \see slf4j_log() for prerequisites
 */
#define report_error(msg) slf4j_log(LOG_ERROR, msg)

/**
 * \brief Log a WARN-level message through SLF4J.
 *
 * \param [in] msg the message to log
 * \see slf4j_log() for prerequisites
 */
#define report_warning(msg) slf4j_log(LOG_WARN, msg)

/**
 * \brief Log a DEBUG-level message through SLF4J.
 *
 * \param [in] msg the message to log
 * \see slf4j_log() for prerequisites
 */
#define report(msg) slf4j_log(LOG_DEBUG, msg)

/**
 * \brief Log a TRACE-level message through SLF4J.
 *
 * \param [in] msg the message to log
 * \see slf4j_log() for prerequisites
 */
#define report_verbose(msg) slf4j_log(LOG_TRACE, msg)

/**
 * \brief Log a message through SLF4J from within the scope of a JNI call.
 *
 * On entering a JNI method, the user _must_ call either slf4j_setup_instance()
 * or slf4j_setup_static() before any logging calls. This includes logging
 * calls performed by other functions called by the JNI function, so it's best
 * to always call the appropriate setup function even if you don't think you're
 * going to be doing any logging yourself.
 *
 * \param [in] level the level at which to log
 * \param [in] msg the message to log
 */
void slf4j_log(LogLevel level, const char *msg);

/**
 * \brief Reconfigure the SLF4J logging context for the current thread based on
 * the given Java object instance.
 *
 * Determines the class of the given object, then calls slf4j_setup_static().
 * See the documentation of that function for details.
 *
 * \param [in] env the JNI environment
 * \param [in] obj an object whose class has a static logger instance
 */
void slf4j_setup_instance(JNIEnv *env, jobject jobj);

/**
 * \brief Reconfigure the SLF4J logging context for the current thread based on
 * the given Java class.
 *
 * Expects that the given class will have a static field named `log` which
 * holds an instance of `org.slf4j.Logger`.
 *
 * This populates a thread-local variable with the JNI environment and class
 * reference, so that those two parameters don't need to be passed into every
 * function for logging to be possible. That context information is retained
 * until overwritten by a subsequent call to slf4j_setup_instance() or
 * slf4j_setup_static(), or until it is explicitly wiped by slf4j_teardown().
 *
 * \param [in] env the JNI environment
 * \param [in] jclazz a Java class which has a static logger instance
 * \see slf4j_teardown() for information on when and where it should be called
 */
void slf4j_setup_static(JNIEnv *env, jclass jclazz);

/**
 * \brief Tears down/forgets the SLF4J logging context for the current thread.
 *
 * If your native code only runs when called from Java – you don't have any
 * threads or signal handlers which may run outside of a JNI context – then you
 * don't need to worry about this. On the other hand, if you have code which
 * might run outside of the scope of a JNI call, and which may try to call
 * slf4j_log(), then just as you call one of the two setup functions when
 * entering every JNI method, you should call this before leaving them.
 */
void slf4j_teardown();

#endif /* SLF4J_H */
