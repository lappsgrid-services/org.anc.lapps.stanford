#!/usr/bin/env bash
#set -eu

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
        docker push docker.lappsgrid.org/$IMAGE
        ;;
    tag)
        docker tag $IMAGE docker.lappsgrid.org/$IMAGE:$version
        docker push docker.lappsgrid.org$IMAGE:$version
        ;;
    run)
        docker run -d -p 8080:8080 --name stanford $IMAGE
        ;;
    stop)
        docker rm -f stanford
        ;;
    *)
        echo "Invalid command: $command"
        ;;
esac

