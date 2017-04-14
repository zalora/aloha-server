#FROM isuper/java-oracle
FROM openjdk:8
VOLUME /tmp
ADD aloha-server*.jar /aloha-server.jar
RUN sh -c 'touch /aloha-server.jar'
ENV JAVA_OPTS="-server -Xms3g -Xmx3g -Djgroups.tcp.address=SITE_LOCAL -Djava.net.preferIPv4Stack=true"
EXPOSE 11222 7800 8080
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar /aloha-server.jar" ]
