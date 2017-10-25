#!/bin/bash

set -e

# Check params
if [ $# -ne 1 ]
	then
		echo Usage: $0 version;
    echo E.g: $0 0.1.0
		echo Version is MAJOR.MINOR.BUGFIX
		echo Latest versions:
		git tag | tail -n 5
		exit 1;
fi

# Set environment
export LANG="C.UTF-8"
export VERSION=$1

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

echo ----[ Upload docker image ]----
docker login
docker tag $DOCKER_IMAGE foilen/$DOCKER_IMAGE
docker tag $DOCKER_IMAGE foilen/replace-php-serialize-safe:latest
docker push foilen/$DOCKER_IMAGE
docker push foilen/replace-php-serialize-safe:latest

echo ----[ Git Tag ]==----
git tag -a -m $VERSION $VERSION

echo ----[ Operation completed successfully ]==----

echo
echo You can see published items on https://hub.docker.com/r/foilen/replace-php-serialize-safe/tags/
echo You can send the tag: git push --tags
