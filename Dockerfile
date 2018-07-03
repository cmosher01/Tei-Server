FROM gradle:jdk9

MAINTAINER Christopher A. Mosher <cmosher01@gmail.com>

EXPOSE 8080
VOLUME /home/gradle/srv


USER root
RUN chmod -R a+w /usr/local

RUN echo "org.gradle.daemon=false" >gradle.properties

COPY settings.gradle ./
COPY build.gradle ./
COPY src/ ./src/

RUN chown -R gradle: ./

USER gradle
RUN gradle build

USER root
RUN tar xf /home/gradle/build/distributions/*.tar --strip-components=1 -C /usr/local

USER gradle
WORKDIR /home/gradle/srv

ENTRYPOINT ["/usr/local/bin/tei-server"]
#ENTRYPOINT ["/bin/bash"]
