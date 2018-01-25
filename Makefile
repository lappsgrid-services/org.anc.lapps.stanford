VERSION=$(shell cat VERSION)
WAR=StanfordServices\#$(VERSION).war

include ../master.mk

docker:
	cd src/docker && ./build.sh
#
run:
	docker run -d -p 8080:8080 --name stanford lappsgrid/stanford-vassar:2.1.0


