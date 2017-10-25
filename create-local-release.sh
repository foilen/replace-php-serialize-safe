#!/bin/bash

set -e

# Set environment
export LANG="C.UTF-8"
export VERSION=$1

if [ -z "$VERSION" ]; then
	export VERSION=master-SNAPSHOT
fi

RUN_PATH="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"
cd $RUN_PATH

echo ----[ Update copyrights ]----
./scripts/javaheaderchanger.sh > /dev/null

echo ----[ Compile ]----
./gradlew build

echo ----[ Prepare folder for docker image ]----
DOCKER_BUILD=$RUN_PATH/build/docker

rm -rf $DOCKER_BUILD
mkdir -p $DOCKER_BUILD/app

cp -v build/libs/replace-php-serialize-safe-$VERSION.jar $DOCKER_BUILD/app/replace-php-serialize-safe.jar
cp -v docker-release/* $DOCKER_BUILD

echo ----[ Docker image folder content ]----
find $DOCKER_BUILD

echo ----[ Build docker image ]----
DOCKER_IMAGE=replace-php-serialize-safe:$VERSION
docker build -t $DOCKER_IMAGE $DOCKER_BUILD

rm -rf $DOCKER_BUILD 
