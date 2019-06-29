FROM gradle:jdk12

MAINTAINER Christopher A. Mosher <cmosher01@gmail.com>

EXPOSE 8080
VOLUME /home/gradle/srv



RUN echo "org.gradle.daemon=false" >gradle.properties

USER root

RUN chown -R gradle: /usr/local

COPY settings.gradle ./
COPY build.gradle ./
COPY src/ ./src/

RUN chown -R gradle: ./

USER gradle

RUN gradle build

RUN tar xf /home/gradle/build/distributions/*.tar --strip-components=1 -C /usr/local

WORKDIR /home/gradle/srv

ENTRYPOINT ["/usr/local/bin/tei-server"]
