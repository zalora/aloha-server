FROM openjdk:8
VOLUME /tmp
ADD aloha-server*.jar /aloha-server.jar
ADD start /start

RUN sh -c 'touch /aloha-server.jar'
RUN sh -c 'touch /start'
RUN sh -c 'chmod +x /start'

EXPOSE 11222 7800 8080
ENTRYPOINT ["bash", "-c", "/start" ]
