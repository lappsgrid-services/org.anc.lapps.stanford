VERSION=$(shell cat VERSION)
WAR=StanfordServices\#$(VERSION).war

include ../master.mk

docker:
	cp target/$(WAR) src/docker
	cd src/docker && ./docker.sh build $(VERSION)

push:
	cd src/docker && ./docker.sh push

tag:
	cd src/docker && .docker.sh tag $(VERSION)

run:
	docker run -d -p 8080:8080 --name stanford lappsgrid/stanford-vassar:$(VERSION)


