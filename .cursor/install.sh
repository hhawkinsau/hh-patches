#!/usr/bin/env bash
# Idempotent Cloud Agent bootstrap for HH Patches.
#
# Resolves the JS release tooling (npm) and the Morphe Gradle toolchain, then
# compiles the patches so the Gradle/Kotlin caches are warm and the setup is
# validated end-to-end.
set -euo pipefail

cd "$(dirname "$0")/.."

# ---------------------------------------------------------------------------
# GitHub Packages credentials for Gradle.
#
# The Morphe patcher plugin/library live on GitHub Packages
# (maven.pkg.github.com/MorpheApp/registry). Even though those packages are
# public, the Maven endpoint requires a token with the `read:packages` scope,
# so a GitHub PAT must be supplied. settings.gradle.kts reads the `gpr.user` /
# `gpr.key` Gradle properties (falling back to GITHUB_ACTOR / GITHUB_TOKEN),
# so mirror the provided secrets into ~/.gradle/gradle.properties.
# ---------------------------------------------------------------------------
GPR_USER_VALUE="${GPR_USER:-${GITHUB_ACTOR:-}}"
GPR_KEY_VALUE="${GPR_KEY:-${GITHUB_TOKEN:-}}"

mkdir -p "$HOME/.gradle"
GRADLE_PROPS="$HOME/.gradle/gradle.properties"
touch "$GRADLE_PROPS"

if [[ -n "$GPR_KEY_VALUE" ]]; then
  # Rewrite the gpr.* lines idempotently, preserving any other properties.
  grep -v -E '^gpr\.(user|key)=' "$GRADLE_PROPS" > "$GRADLE_PROPS.tmp" 2>/dev/null || true
  mv "$GRADLE_PROPS.tmp" "$GRADLE_PROPS"
  {
    echo "gpr.user=${GPR_USER_VALUE:-cursor}"
    echo "gpr.key=${GPR_KEY_VALUE}"
  } >> "$GRADLE_PROPS"
  HAVE_GPR=1
else
  echo "WARNING: No GPR_KEY (or GITHUB_TOKEN) found in the environment." >&2
  echo "         Gradle cannot resolve the Morphe packages and the build will be skipped." >&2
  echo "         Add a GitHub PAT with the read:packages scope as the GPR_KEY secret." >&2
  HAVE_GPR=0
fi

# ---------------------------------------------------------------------------
# JavaScript release tooling (semantic-release, changelog generator, etc.).
# ---------------------------------------------------------------------------
npm install

# ---------------------------------------------------------------------------
# Compile the patches. This warms the Gradle wrapper + dependency caches and
# validates that the Morphe toolchain resolves correctly.
# ---------------------------------------------------------------------------
if [[ "$HAVE_GPR" == "1" ]]; then
  ./gradlew :patches:buildAndroid --no-daemon
fi
