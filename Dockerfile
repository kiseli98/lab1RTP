FROM openjdk:8-jdk

COPY pom.xml /usr/local/lab1rtp/pom.xml
COPY src /usr/local/lab1rtp/src

RUN apt-get update -y
RUN apt-get install maven -y

RUN  cd /usr/local/lab1rtp/ && mvn package
WORKDIR /usr/local/lab1rtp/
CMD mvn exec:java -Dexec.mainClass="app.Application"