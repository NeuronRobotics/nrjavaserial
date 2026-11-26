#!/bin/sh
set -eu

if [ "$#" -ne 1 ]; then
    echo "Usage: $0 <nrjavaserial.jar>" >&2
    exit 2
fi
if [ "$(uname -s)" != Darwin ]; then
    echo "The SIGCHLD regression test requires macOS." >&2
    exit 2
fi

test_sources=$(CDPATH= cd -- "$(dirname -- "$0")" && pwd)
test_jdk=${JAVA_HOME:-$(/usr/libexec/java_home)}
test_dir=$(mktemp -d "${TMPDIR:-/tmp}/nrjavaserial-sigchld.XXXXXXXX")
trap 'rm -rf -- "$test_dir"' EXIT

test_arch=$("$test_jdk/bin/java" -XshowSettings:properties -version 2>&1 |
    awk '/^[[:space:]]*os\.arch = / { print $NF; exit }')
case "$test_arch" in
    amd64|x86_64) test_arch=x86_64 ;;
    aarch64|arm64) test_arch=arm64 ;;
    *) echo "Unsupported Java architecture: $test_arch" >&2; exit 2 ;;
esac

clang -arch "$test_arch" -dynamiclib -I"$test_jdk/include" -I"$test_jdk/include/darwin" \
    "$test_sources/sigchld_test.c" -o "$test_dir/sigchld_test.dylib"
"$test_jdk/bin/javac" -cp "$1" -d "$test_dir" "$test_sources/MacOsSigchldTest.java"
"$test_jdk/bin/java" "-Djava.io.tmpdir=$test_dir" -cp "$test_dir:$1" \
    MacOsSigchldTest "$test_dir/sigchld_test.dylib"
