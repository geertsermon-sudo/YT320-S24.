#!/usr/bin/env sh
set -eu
ROOT=$(CDPATH= cd -- "$(dirname -- "$0")/.." && pwd)
OUT=$(mktemp -d)
trap 'rm -rf "$OUT"' EXIT HUP INT TERM
javac -d "$OUT" "$ROOT/app/src/main/java/be/yt320/s24/YoutubeLinks.java" "$ROOT/tests/YoutubeLinksTest.java"
java -cp "$OUT" YoutubeLinksTest
