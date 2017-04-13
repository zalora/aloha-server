FROM isuper/java-oracle
VOLUME /tmp
ADD aloha-*.jar /aloha.jar
RUN sh -c 'touch /aloha.jar'
ENV JAVA_OPTS="-server -Xms1g -Xmx1g -Dinfinispan.cluster.jgroups.config=jgroups-jdbc.xml -Djgroups.tcp.address=SITE_LOCAL -Djava.net.preferIPv4Stack=true -Djgroups.jdbc.connection_url=jdbc:mysql://172.17.0.1/bob_live_sg -Djgroups.jdbc.connection_username=root -Djgroups.jdbc.connection_password="
#ENV JAVA_OPTS="-server -Xms1g -Xmx1g -Djgroups.tcp.address=SITE_LOCAL -Djava.net.preferIPv4Stack=true"
EXPOSE 11211 7800
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar /aloha.jar" ]
