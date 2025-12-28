#!/bin/bash

set -e

RUN_PATH="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"
cd "$RUN_PATH"

echo ----[ Report ]----
./gradlew dependencyUpdates --refresh-dependencies -Drevision=release | tee _report-latest-gradle.txt
