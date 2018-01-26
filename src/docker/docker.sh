#!/usr/bin/env bash

command=$1
version=$2

IMAGE=lappsgrid/stanford-vassar

case $command in
    build)
        base_version=`echo $version | sed 's/-SNAPSHOT//'`
        echo "Building lappsgrid/stanford-vassar:$base_version"
        docker build --build-arg VERSION=$version -t $IMAGE .
        ;;
    push)
        docker push $IMAGE
        ;;
    tag)
        docker tag $IMAGE $IMAGE:$version
        docker push $IMAGE:$version
        ;;
esac

