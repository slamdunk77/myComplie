FROM gradle:jdk14
WORKDIR /app/
COPY ./* ./
RUN javac -encoding UTF-8 *.java
RUN chmod +x TokenizerTest