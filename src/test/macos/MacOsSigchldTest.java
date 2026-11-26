import gnu.io.RXTXPort;

import java.util.concurrent.TimeUnit;

public final class MacOsSigchldTest {
    private static native String openPty();

    private static native void closePty();

    private static native void saveChildSignalAction();

    private static native void assertChildSignalActionUnchanged();

    public static void main(String[] args) throws Exception {
        System.load(args[0]);
        // Start a child first so the JDK initializes its process handling.
        Process sibling = new ProcessBuilder("/bin/sleep", "30").start();
        try {
            String portName = openPty();
            if (portName == null) {
                throw new IllegalStateException("Could not allocate a private pseudo-terminal");
            }

            saveChildSignalAction();
            RXTXPort port = new RXTXPort(portName);
            try {
                assertChildSignalActionUnchanged();
                Process child = new ProcessBuilder("/bin/sh", "-c", "exit 7").start();
                try {
                    if (!child.waitFor(5, TimeUnit.SECONDS)) {
                        throw new AssertionError("Java timed out waiting for the short-lived child");
                    }
                    if (child.exitValue() != 7) {
                        throw new AssertionError("Expected child exit code 7, got " + child.exitValue());
                    }
                    if (!sibling.isAlive()) {
                        throw new AssertionError("The sibling must still be running");
                    }
                } finally {
                    if (child.isAlive()) {
                        child.destroyForcibly();
                    }
                }
            } finally {
                port.close();
            }
            assertChildSignalActionUnchanged();
            System.out.println("PASS: SIGCHLD preserved; short-lived child exited with 7 while its sibling was alive");
        } finally {
            closePty();
            sibling.destroyForcibly();
            sibling.waitFor(5, TimeUnit.SECONDS);
        }
    }
}
