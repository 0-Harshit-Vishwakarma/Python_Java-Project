FROM eclipse-temurin:21-jdk

WORKDIR /app

COPY src ./src
COPY public ./public
COPY data ./data

RUN mkdir -p /app/data && javac -d /app src/LifeLineServer.java

ENV PORT=8787

EXPOSE 8787

CMD ["java", "LifeLineServer"]
