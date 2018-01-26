#!/usr/bin/env bash

#version=`cat ../../VERSION`
version=$1

#war=../../target/StanfordServices\#$version.war

#if [ ! -e "$war" ] ; then
#    echo "Unable to find the war file."
#    exit 1
#fi

#cp $war .
base_version=`echo $version | sed 's/-SNAPSHOT//'`
echo "Building lappsgrid/stanford-vassar:$base_version"
docker build --build-arg VERSION=$version -t lappsgrid/stanford-vassar:$base_version .
