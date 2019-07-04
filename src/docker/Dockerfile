FROM lappsgrid/tomcat7:1.2.3

ARG VERSION

ADD stanford-models.tgz /
COPY server.xml /var/lib/tomcat7/conf
COPY StanfordServices#$VERSION.war /var/lib/tomcat7/webapps
