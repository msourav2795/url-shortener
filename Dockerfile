FROM openjdk:8-jdk-alpine
EXPOSE 8080
ADD target/url-shortener-docker.jar url-shortener-docker.jar
ENTRYPOINT ["java","-jar","/url-shortener-docker.jar"]