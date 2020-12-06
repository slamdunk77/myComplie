FROM openjdk:11
WORKDIR /app/
COPY ./* ./
COPY src /app/src
RUN javac -encoding UTF-8 *.java
RUN chmod +x TokenizerTest
