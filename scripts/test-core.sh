#!/usr/bin/env bash
# Pure Java regressions without downloading Gradle or starting Minecraft.
set -euo pipefail
cd "$(dirname "$0")/.."

require_gson=false
case "${1:-}" in
  "") ;;
  --require-gson) require_gson=true ;;
  *) echo "Usage: GSON_JAR=/path/to/gson.jar bash scripts/test-core.sh [--require-gson]" >&2; exit 2 ;;
esac

if command -v javac >/dev/null 2>&1; then
  compiler=(javac --release 17)
else
  # Some minimal JDK images retain jdk.compiler but omit the javac launcher/ct.sym.
  compiler=(java com.sun.tools.javac.Main -source 17 -target 17)
  echo "Using compiler module fallback; Java 17 API compatibility still requires the Gradle build."
fi

output="$(mktemp -d)"
trap 'rm -rf "$output"' EXIT
package=dev/kurowater/kuradialmenu/client
sources=(
  "src/main/java/$package/config/ModConfig.java"
  "src/main/java/$package/keybind/MenuKeyState.java"
  "src/main/java/$package/util/MathUtil.java"
)
tests=(
  "config/ModConfigTest"
  "keybind/MenuKeyStateTest"
  "util/MathUtilTest"
)
for test in "${tests[@]}"; do
  sources+=("src/test/java/$package/$test.java")
done
"${compiler[@]}" -encoding UTF-8 -d "$output" "${sources[@]}"
for test in "${tests[@]}"; do
  class="dev.kurowater.kuradialmenu.client.${test//\//.}"
  java -ea -cp "$output" "$class"
  echo "PASS $class"
done

if [[ -n "${GSON_JAR:-}" ]]; then
  if [[ ! -f "$GSON_JAR" ]]; then
    echo "GSON_JAR does not identify a readable jar: $GSON_JAR" >&2
    exit 2
  fi
  classpath="$output:$GSON_JAR"
  "${compiler[@]}" -encoding UTF-8 -cp "$classpath" -d "$output" \
    "src/main/java/$package/config/ConfigFileStore.java" \
    "src/test/java/$package/config/ConfigFileStoreTest.java"
  java -ea -cp "$classpath" dev.kurowater.kuradialmenu.client.config.ConfigFileStoreTest
  echo "PASS ConfigFileStoreTest"
elif $require_gson; then
  echo "GSON_JAR is required for configuration I/O regression tests." >&2
  exit 2
else
  echo "SKIP ConfigFileStoreTest: set GSON_JAR, or run Gradle regressionTest with dependencies available."
fi
