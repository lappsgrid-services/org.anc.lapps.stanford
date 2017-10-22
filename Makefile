VERSION=$(shell cat VERSION)
WAR=StanfordServices\#$(VERSION).war

include ../master.mk

run:
	docker run -d -p 8080:8080 --name tomcat -v target:/var/lib/tomcat7/webapps -v /usr/local/lapps:/usr/local/lapps lappsgrid/tomcat7:1.1.0
	
