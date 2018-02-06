FROM lappsgrid/tomcat7:1.2.3

ARG VERSION

ADD stanford-models.tgz /
COPY StanfordServices#$VERSION.war /var/lib/tomcat7/webapps
