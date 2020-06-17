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

#include <ctype.h>
#include <stdlib.h>
#include <string.h>
#include "slf4j.h"

/** \brief The maximum length of a log message. */
#define MESSAGE_MAX 4096

typedef struct Slf4jContext
{
	JNIEnv *env;
	jobject log;
} Slf4jContext;

static __thread Slf4jContext context = {0};

void slf4j_log(LogLevel level, const char *msg)
{
	if (context.env == NULL)
	{
		/* No context? No logging. */
		return;
	}

	const char *methodName;
	switch (level)
	{
	case LOG_ERROR:
		methodName = "error";
		break;
	case LOG_WARN:
		methodName = "warn";
		break;
	case LOG_INFO:
		methodName = "info";
		break;
	case LOG_DEBUG:
		methodName = "debug";
		break;
	case LOG_TRACE:
	default:
		methodName = "trace";
		break;
	}

	/* The report() functions used to just conditionally printf whatever was
	 * handed to them, so all of those invocations pass strings with trailing
	 * newlines. We'll trim any trailing whitespace before passing to SLF4J. */
	int messageLength = strlen(msg);
	while (isspace(msg[messageLength - 1]))
	{
		--messageLength;
	}

	/* A log message 4K in length is likely already an error. Anything longer
	 * is _definitely_ an error. */
	if (messageLength > MESSAGE_MAX)
	{
		messageLength = MESSAGE_MAX;
	}

	char *trimmedMessage = strndup(msg, messageLength);
	jstring jmsg = (*context.env)->NewStringUTF(
		context.env,
		trimmedMessage);
	free(trimmedMessage);

	jclass logger = (*context.env)->FindClass(context.env, "org/slf4j/Logger");
	jmethodID logMethod = (*context.env)->GetMethodID(
		context.env,
		logger,
		methodName,
		"(Ljava/lang/String;)V");

	(*context.env)->CallVoidMethod(context.env, context.log, logMethod, jmsg);
}

void slf4j_setup_instance(JNIEnv *env, jobject jobj)
{
	jclass jclazz = (*env)->GetObjectClass(env, jobj);
	slf4j_setup_static(env, jclazz);
}

void slf4j_setup_static(JNIEnv *env, jclass jclazz)
{
	jfieldID logId = (*env)->GetStaticFieldID(env, jclazz, "log", "Lorg/slf4j/Logger;");
	jobject log = (*env)->GetStaticObjectField(env, jclazz, logId);

	context.env = env;
	context.log = log;
}

void slf4j_teardown()
{
	memset(&context, 0, sizeof(Slf4jContext));
}
