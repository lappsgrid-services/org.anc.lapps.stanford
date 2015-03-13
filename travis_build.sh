#!/bin/bash
pwd

mvn compile --settings settings.xml
if [ ! -e /src/main/resources/metadata/org.anc.lapps.standford.NamedEntityRecognizer.json ] ; then
    echo "Metadata not found."
    exit 1
fi

mvn clean test --settings settings.xml
