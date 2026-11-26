#include <fcntl.h>
#include <jni.h>
#include <signal.h>
#include <stdio.h>
#include <stdlib.h>
#include <unistd.h>

static int pty_master = -1;
static struct sigaction saved_action;

static void fail(JNIEnv *env, const char *message)
{
    jclass error = (*env)->FindClass(env, "java/lang/AssertionError");
    if (error != NULL) {
        (*env)->ThrowNew(env, error, message);
    }
}

JNIEXPORT jstring JNICALL Java_MacOsSigchldTest_openPty(JNIEnv *env, jclass type)
{
    pty_master = posix_openpt(O_RDWR | O_NOCTTY | O_CLOEXEC);
    if (pty_master < 0 || grantpt(pty_master) != 0 || unlockpt(pty_master) != 0) {
        return NULL;
    }
    char *name = ptsname(pty_master);
    return name == NULL ? NULL : (*env)->NewStringUTF(env, name);
}

JNIEXPORT void JNICALL Java_MacOsSigchldTest_closePty(JNIEnv *env, jclass type)
{
    if (pty_master >= 0) {
        close(pty_master);
        pty_master = -1;
    }
}

JNIEXPORT void JNICALL Java_MacOsSigchldTest_saveChildSignalAction(JNIEnv *env, jclass type)
{
    if (sigaction(SIGCHLD, NULL, &saved_action) != 0) {
        fail(env, "Could not read the initial SIGCHLD action");
    }
}

JNIEXPORT void JNICALL Java_MacOsSigchldTest_assertChildSignalActionUnchanged(JNIEnv *env, jclass type)
{
    struct sigaction actual = { 0 };
    if (sigaction(SIGCHLD, NULL, &actual) != 0) {
        fail(env, "Could not read the current SIGCHLD action");
        return;
    }
    if (actual.sa_flags != saved_action.sa_flags || actual.sa_mask != saved_action.sa_mask
            || ((actual.sa_flags & SA_SIGINFO) != 0
                ? actual.sa_sigaction != saved_action.sa_sigaction
                : actual.sa_handler != saved_action.sa_handler)) {
        char message[128];
        snprintf(message, sizeof message, "Serial port changed SIGCHLD (flags 0x%x -> 0x%x)",
                saved_action.sa_flags, actual.sa_flags);
        fail(env, message);
    }
}
